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
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;
import com.git.ifly6.nsapi.telegram.JTelegramConnection;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramType;

import java.io.IOException;
import java.util.HashSet;
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
 * one. Sender sends until queue is empty before refresh.
 */
public class CommSender {

    public static final Logger LOGGER = Logger.getLogger(CommSender.class.getName());

    /** One thread for many clients. */
    private static ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> job;

    private CommSenderOutput outputInterface;

    private final JTelegramKeys keys;
    private final CommMonitor monitor;
    private final JTelegramType telegramType;

    /** See {@link #setFeedLimit(int)}. */
    private int feedLimit = Integer.MAX_VALUE;

    /** If true, does not send telegrams. */
    private boolean dryRun = false;

    /** Processing action to apply to {@link CommMonitor} output. */
    private Function<List<String>, List<String>> processingAction;

    /** First-in-first-out send queue. */
    private final Queue<String> sendQueue = new LinkedList<>();

    /** Recipients to which the telegram has already been sent are put in the sent list. */
    private Set<String> sentList = new HashSet<>();

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
        List<String> recipients = processingAction.apply(monitor.getRecipients());

        int recipientsAdded = 0;
        for (String s : recipients) {
            if (recipientsAdded > feedLimit) break;
            if (!sendQueue.contains(s) && !sentList.contains(s)) // prevent double-queueing
                sendQueue.add(s);
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
     * @see com.git.ifly6.nsapi.ctelegram.io.CommNationCache
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
        String recipient = sendQueue.poll();
        if (recipient == null)
            throw new EmptyQueueException();

        // if we have a recipient...
        if (!CommRecipientChecker.doesRecipientAccept(recipient, telegramType))
            try {
                executeSend(); // immediately try again
            } catch (StackOverflowError error) {
                // might happen if cannot get recipients over and over again?
                throw new NSIOException("Encountered stack overflow error");
            }

        // if the recipient will accept our telegram...
        try {
            JTelegramConnection connection = new JTelegramConnection(keys, recipient, dryRun);
            int responseCode = connection.verify();

            // if there is an error
            if (responseCode != JTelegramConnection.QUEUED)
                throw NSTGSettingsException.createException(keys, responseCode);

            // we sent to this recipient
            reportSent(recipient);
            sentList.add(recipient);

        } catch (IOException e) {
            // rethrow
            throw new NSIOException("Encountered IO exception when connecting to NationStates", e);
        }
    }

    private void reportSent(String recipient) {
        sentList.add(recipient);
        outputInterface.sentTo(recipient, sentList.size());
    }

    public void startSend() {
        job = timer.scheduleWithFixedDelay(() -> {
            // if no recipient in queue, try to get some
            if (sendQueue.peek() == null) {
                LOGGER.info("No recipients in queue; attempting to feed.");
                feedQueue();

                // if still there are no recipients to queue
                if (sendQueue.peek() == null) {
                    LOGGER.info("Mission failed; we'll get recipients next time");
                    return;
                }
            }

            // execute send
            try {
                executeSend();
            } catch (EmptyQueueException e) {
                /* Should not catch NSTGSettingsException, as if settings are wrong, you should know immediately.
                 * Otherwise, log no recipient exception, though it shouldn't happen due to filtering above. */
                LOGGER.warning("Exhausted loaded queue; were all recipients invalid?");
            }
        }, 0, telegramType.getWaitDuration().toMillis(), TimeUnit.MILLISECONDS);
    }

    public void restartSend() {
        if (monitor instanceof CommUpdatingMonitor)
            ((CommUpdatingMonitor) monitor).start();

        this.startSend();
    }

    public void stopSend() {
        if (job != null) {
            job.cancel(false); // allow completion
            if (monitor instanceof CommUpdatingMonitor)
                ((CommUpdatingMonitor) monitor).stop();
        }
    }

    /** Thrown if no recipient is found in the queue */
    public static class EmptyQueueException extends NoSuchElementException {
    }
}
