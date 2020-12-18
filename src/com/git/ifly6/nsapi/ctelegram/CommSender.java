/*
 * Copyright (c) 2020 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.nsapi.ctelegram;

import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.ctelegram.io.NSTGSettingsException;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommNationCache;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;
import com.git.ifly6.nsapi.telegram.JTelegramConnection;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramType;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Sends telegrams on the NationStates API. Instantiation is not restricted; regardless, there should never be more than
 * one. Sender sends indefinitely or until linked {@link CommMonitor} {@link CommMonitor#recipientsExhausted()
 * exhuasts}.
 * @since version 3.0 (build 13)
 */
public class CommSender {

    public static final Logger LOGGER = Logger.getLogger(CommSender.class.getName());

    /** One thread for many clients. */
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> job;

    private final CommSenderOutput outputInterface;
    private final Instant initAt = Instant.now();
    private final JTelegramKeys keys;
    private final JTelegramType telegramType;
    private final CommMonitor monitor;

    /** See {@link #setFeedLimit(int)}. */
    private int feedLimit = Integer.MAX_VALUE;

    /** If true, does not send telegrams. */
    private boolean dryRun = false;

    /** Processing action to apply to {@link CommMonitor} output. */
    private Function<List<String>, List<String>> processingAction;

    /** First-in-first-out send queue. */
    private final Queue<String> sendQueue = new LinkedList<>();

    /** Recipients to which the telegram has already been sent are put in the sent list. */
    private Set<String> sentList = new LinkedHashSet<>(); // ordered

    /**
     * Constructs a {@link CommSender}.
     * @param anInterface  to report to
     * @param keys         to send with
     * @param monitor      to provide recipients
     * @param telegramType to provide delay timings
     */
    public CommSender(JTelegramKeys keys, CommMonitor monitor, JTelegramType telegramType,
                      CommSenderOutput anInterface) {
        this.keys = keys;
        this.monitor = monitor;
        this.telegramType = telegramType;
        outputInterface = anInterface;
    }

    /** Feeds the queue until the feed limit is exceeded. Queue is fed whenever the queue is empty. */
    private void feedQueue() {
        LOGGER.fine("Feeding queue");
        List<String> recipients = processingAction.apply(monitor.getRecipients());

        int recipientsAdded = 0;
        for (String s : recipients) {
            if (recipientsAdded > feedLimit) break;
            if (!sendQueue.contains(s) && !sentList.contains(s)) {
                // prevent double-queueing
                sendQueue.add(s);
                LOGGER.finest(String.format("Fed queue element %s", s));
            }
        }

        LOGGER.info(String.format("Fed queue with %d recipients", recipientsAdded));
    }

    /** @return feed limit, ie number of recipients taken from the monitor in each step */
    public int getFeedLimit() {
        return feedLimit;
    }

    /**
     * Sets feed limit, ie number of recipients taken from monitor in each feed step. Because sender sends until queue
     * is empty before feeding the queue, a high feed limit may force long inter-feed intervals. It also may force
     * longer delays if the queue is exhausted when encountering a long series of recipients which refuse telegrams.
     * @param feedLimit to apply
     */
    public void setFeedLimit(int feedLimit) {
        this.feedLimit = feedLimit;
    }

    /**
     * Sets the processing action applied to {@link CommMonitor#getRecipients()} results when called by {@link
     * #feedQueue()}. Processing action takes a list and returns a list. Its implementation is left extremely open-ended
     * on purpose.
     * @param processingAction to apply on recipients feed
     * @see CommNationCache
     */
    public void setProcessingAction(Function<List<String>, List<String>> processingAction) {
        this.processingAction = processingAction;
    }

    /**
     * If true, no telegrams are actually sent. Should be used for debugging purposes only. Default is false.
     * @param dryRun if true
     */
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    /** Sends telegram to recipient, with recipient determined as first thing in the queue. */
    private void executeSend() {
        LOGGER.finer("Send starting");
        if (dryRun) LOGGER.warning("SENDING AS DRY RUN!");

        String recipient = sendQueue.poll();
        if (recipient == null)
            throw new EmptyQueueException();

        // if we have a recipient...
        LOGGER.finer(String.format("Got recipient %s from queue", recipient));
        if (!CommRecipientChecker.doesRecipientAccept(recipient, telegramType))
            try {
                LOGGER.finer(String.format("Recipient %s invalid; try next in queue", recipient));
                executeSend(); // try again
            } catch (StackOverflowError error) {
                // might happen if cannot get recipients over and over again?
                throw new NSIOException("Encountered stack overflow error");
            }

        // if the recipient will accept our telegram...
        try {
            LOGGER.finer("Creating telegram connection ");
            JTelegramConnection connection = new JTelegramConnection(keys, recipient, dryRun);

            // get response code
            int responseCode = connection.verify();
            LOGGER.finer(String.format("Received response code %d", responseCode));

            // if there is an error
            if (responseCode != JTelegramConnection.QUEUED)
                throw NSTGSettingsException.createException(keys, responseCode);

            // we sent to this recipient
            reportSent(recipient);
            sentList.add(recipient);
            LOGGER.info(String.format("Sent telegram to recipient %s", recipient));

        } catch (IOException e) { // rethrow
            throw new NSIOException("Encountered IO exception when connecting to NationStates", e);
        }
    }

    private void reportSent(String recipient) {
        sentList.add(recipient);
        outputInterface.sentTo(recipient, sentList.size());
    }

    public void startSend() {
        if (isRunning())
            throw new UnsupportedOperationException("Cannot start sending after it already started");

        job = scheduler.scheduleWithFixedDelay(() -> {
            // if no recipient in queue, try to get some
            // if monitor is exhausted, it will automatically throw an exhausted exception

            if (sendQueue.peek() == null) {
                if (monitor.recipientsExhausted()) {
                    LOGGER.info("No recipients in queue; attempting to feed");
                    feedQueue();

                    // if still there are no recipients to queue
                    if (sendQueue.peek() == null) {
                        LOGGER.info("Mission to feed failed; we'll get recipients next time");
                        return;
                    }

                } else {
                    LOGGER.info("No recipients in queue; cannot feed as monitor is exhausted");
                    this.stopSend();

                    LOGGER.info("Calling interface onTerminate action");
                    this.outputInterface.onTerminate();
                    return; // end
                }
            }

            // execute send
            try {
                executeSend();
            } catch (EmptyQueueException e) {
                /* Should not catch NSTGSettingsException, as if settings are wrong, you should know immediately.
                 * Otherwise, log no recipient exception, though it shouldn't happen due to filtering above. */
                LOGGER.warning("Exhausted loaded queue; were all queued recipients invalid?");
            }
        }, 0, telegramType.getWaitDuration().toMillis(), TimeUnit.MILLISECONDS);

        // log sending thread creation
        LOGGER.info(String.format("Scheduled sending thread start with wait duration %d ms",
                telegramType.getWaitDuration().toMillis()));
    }

    public void restartSend() {
        LOGGER.fine("Restarting send thread");
        if (monitor instanceof CommUpdatingMonitor)
            ((CommUpdatingMonitor) monitor).restart();

        this.startSend();
    }

    public void stopSend() {
        LOGGER.fine("Stopping send thread");
        if (job != null) {
            job.cancel(false); // must allow completion!!
            if (monitor instanceof CommUpdatingMonitor)
                ((CommUpdatingMonitor) monitor).stop();
        }
    }

    /** Returns list of sent recipients. */
    public Set<String> getSentList() {
        return sentList;
    }

    /** @returns {@link CommSender} initialisation time */
    public Instant getInitAt() {
        return initAt;
    }

    /** @return true if sending telegrams. */
    public boolean isRunning() {
        if (job == null) return false;
        if (job.isDone() || job.isCancelled()) return false;
        if (scheduler.isShutdown() || scheduler.isTerminated()) return false;
        return true;
    }

    /** @return {@link Duration} until next telegram is sent; {@code null} if not running. */
    public Duration nextIn() {
        if (isRunning()) return Duration.ofMillis(job.getDelay(TimeUnit.MILLISECONDS));
        throw new UnsupportedOperationException("No duration to next telegram; no telegrams are being sent");
    }

    /** Thrown if no recipient is found in the queue */
    public static class EmptyQueueException extends NoSuchElementException {
    }
}
