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
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;
import com.git.ifly6.nsapi.telegram.JTelegramConnection;
import com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramType;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import java.util.logging.Level;
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

    private final CommSenderInterface outputInterface;
    private final Instant initAt = Instant.now();
    private final JTelegramKeys keys;
    private final JTelegramType telegramType;
    private final CommMonitor monitor;

    /** See {@link #setFeedLimit(int)}. */
    private int feedLimit = Integer.MAX_VALUE;

    /** If true, does not send telegrams. */
    private boolean dryRun = false;

    /** First-in-first-out send queue. */
    private final Queue<String> sendQueue = new LinkedList<>();

    /** Recipients to which the telegram has already been sent are put in the sent list. */
    private Set<String> sentList = new LinkedHashSet<>(); // ordered
    private Set<String> skipList = new LinkedHashSet<>();

    /**
     * Constructs a {@link CommSender}.
     * @param anInterface  to report to
     * @param keys         to send with
     * @param monitor      to provide recipients
     * @param telegramType to provide delay timings
     */
    public CommSender(JTelegramKeys keys, CommMonitor monitor, JTelegramType telegramType,
                      CommSenderInterface anInterface) {
        this.keys = keys;
        this.monitor = monitor;
        this.telegramType = telegramType;
        outputInterface = anInterface;
    }

    /** @return {@link CommMonitor} instantiating */
    public CommMonitor getMonitor() {
        return monitor;
    }

    /** Feeds the queue until the feed limit is exceeded. Queue is fed whenever the queue is empty. */
    private void feedQueue() {
        LOGGER.fine("Feeding queue");
        List<String> recipients = monitor.getRecipients();

        int recipientsAdded = 0;
        for (String s : recipients) {
            if (recipientsAdded > feedLimit) break;
            if (!sendQueue.contains(s) && !sentList.contains(s)) {  // prevent double-queueing
                sendQueue.add(s);
                recipientsAdded++;
                LOGGER.finest(String.format("Fed queue element %s", s));
            }
        }

        LOGGER.fine(String.format("Fed queue with %d recipients", recipientsAdded));
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

        // if no recipient in queue, try to get some
        // if monitor is exhausted, it will automatically throw an exhausted exception
        if (sendQueue.peek() == null) {
            if (monitor.recipientsExhausted()) {
                LOGGER.info("No recipients in queue; cannot feed as monitor is exhausted");
                this.stopSend();

                LOGGER.info("Calling interface onTerminate action");
                this.outputInterface.onTerminate();
                return; // end

            } else {
                // LOGGER.info("No recipients in queue; attempting to feed");
                feedQueue();

                // if still there are no recipients to queue
                if (sendQueue.peek() == null) {
                    LOGGER.info("Mission to feed failed; we'll get recipients next time");
                    return;
                }
            }
        }

        String recipient = sendQueue.poll();
        if (recipient == null)
            throw new EmptyQueueException();

        // if we have a recipient...
        // todo move to comm filtered monitor
        LOGGER.info(String.format("Got recipient <%s> from queue", recipient));
        if (!CommRecipientChecker.doesRecipientAccept(recipient, telegramType))
            try {
                LOGGER.info(String.format("Recipient <%s> invalid; trying next in queue", recipient));
                this.reportSkip(recipient);
                executeSend(); // try again
                return; // do not execute further!

            } catch (StackOverflowError error) {
                // might happen if cannot get recipients over and over again?
                throw new NSIOException("Encountered stack overflow error");
            }

        // if the recipient will accept our telegram...
        JTelegramConnection connection;
        try {
            LOGGER.finer("Creating telegram connection ");
            connection = new JTelegramConnection(keys, recipient, dryRun);
        } catch (IOException e) { // rethrow
            throw new NSIOException("Cannot initialise connection to telegram API", e);
        }

        try {
            // get response code
            ResponseCode responseCode = connection.verify();
            LOGGER.info(String.format("Received response code %s", responseCode));

            // if there is an error
            if (responseCode != ResponseCode.QUEUED)
                throw NSTGSettingsException.createException(keys, responseCode);

            // we sent to this recipient
            reportSent(recipient);
            sentList.add(recipient);

        } catch (IOException e) {
            throw new NSIOException("Cannot get response code from telegram API", e);
        }

    }

    private void reportSent(String recipient) {
        sentList.add(recipient);
        outputInterface.sentTo(recipient, sentList.size());
    }

    public void reportSkip(String recipient) {
        skipList.add(recipient);
        outputInterface.onSkip(recipient);
    }

    public void startSend() {
        LOGGER.info("Starting send thread");
        if (isRunning())
            throw new UnsupportedOperationException("Cannot start sending after it already started");

        if (monitor instanceof CommUpdatingMonitor)
            ((CommUpdatingMonitor) monitor).start();

        job = scheduler.scheduleWithFixedDelay(() -> {
            try {
                executeSend();

            } catch (CommMonitor.ExhaustedException e) {
                LOGGER.info("Recipients exhausted; shutting down sending facility");
                this.outputInterface.onTerminate();
                this.stopSend();

            } catch (Throwable e) {
                final String m = "Client sending thread encountered exception! Shutting down sending thread!";
                LOGGER.log(Level.SEVERE, m + "\n" + Throwables.getStackTraceAsString(e), e);
                this.outputInterface.onError(m, e);
                this.outputInterface.onTerminate();
                this.stopSend();
                e.printStackTrace();
            }
        }, 0, telegramType.getWaitDuration().toMillis(), TimeUnit.MILLISECONDS);

        // log sending thread creation
        LOGGER.info(String.format("Scheduled sending thread start with wait duration %d ms",
                telegramType.getWaitDuration().toMillis()));
    }

    public void stopSend() {
        LOGGER.fine("Stopping send thread");
        if (job != null && !job.isDone()) {
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
    public Instant nextAt() {
        if (isRunning()) return Instant.now().plus(job.getDelay(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS);
        throw new UnsupportedOperationException("No duration to next telegram; no telegrams are being sent");
    }

    public Set<String> getSkipList() {
        return skipList;
    }

    /** Thrown if no recipient is found in the queue */
    public static class EmptyQueueException extends NoSuchElementException {
    }
}
