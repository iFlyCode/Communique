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

package com.git.ifly6.nsapi.ctelegram.monitors;

import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.NSConnection;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Framework for creating monitors which update. Each monitor caches data and provides it per {@link CommMonitor};
 * update monitors have defined update intervals where their cached data change.
 * @since version 3.0 (build 13)
 */
public abstract class CommUpdatingMonitor implements CommMonitor {

    public static final Logger LOGGER = Logger.getLogger(CommUpdatingMonitor.class.getName());
    public static final Duration DEFAULT_UPDATE_INTERVAL = Duration.ofSeconds(120);

    private Duration updateInterval;

    /** {@link Instant} of last update start. */
    private Instant lastUpdate;

    /**
     * Latch. If {@link CountDownLatch} is 0, monitor has fully initialised.
     * @see CommWaitingMonitor
     */
    private CountDownLatch latch = new CountDownLatch(1);

    private ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> job;

    /**
     * Constructs an updating monitor with {@link #DEFAULT_UPDATE_INTERVAL} which, when it starts, calls an update
     * action. Monitor automatically starts.
     */
    public CommUpdatingMonitor() {
        this(DEFAULT_UPDATE_INTERVAL);
    }

    /**
     * Constructs updating monitor with provided update interval. Monitor automatically starts.
     * @param updateInterval replacing {@link #DEFAULT_UPDATE_INTERVAL}
     */
    public CommUpdatingMonitor(final Duration updateInterval) {
        final int minimumWaitMillis = NSConnection.WAIT_TIME * 2;
        if (updateInterval.toMillis() < minimumWaitMillis)
            throw new IllegalArgumentException(
                    String.format("Cannot construct monitor with update interval less than %d ms",
                            minimumWaitMillis));

        this.updateInterval = updateInterval;
        this.start();
    }

    /**
     * Turns provided list, as string, in format {@code BLAH, BLAH, BLAH} into a list of strings. Splits only on commas.
     * All elements are normalised with {@link ApiUtils#ref(List)}.
     * @param listString to parse
     * @return {@code List<String>} representation thereof
     */
    public static List<String> parseList(String listString) {
        List<String> regions;
        if (!listString.contains(",")) regions = Collections.singletonList(ApiUtils.ref(listString));
        else regions = Arrays.stream(listString.split(",\\s+"))
                .map(ApiUtils::ref)
                .filter(s -> !ApiUtils.isEmpty(s))
                .collect(Collectors.toList());
        return regions;
    }

    @Override
    public OptionalLong remainingIfKnown() {
        return OptionalLong.empty();
    }

    @Override
    public abstract boolean recipientsExhausted();

    /** Things to do before calling {@link #updateAction()}. */
    private void preUpdateAction() {
        if (this.recipientsExhausted()) {
            LOGGER.info("Recipients are exhausted; cannot update. Stopping monitor");
            this.stop();

        } else
            try {
                LOGGER.info("Triggering update");
                updateAction();

                // trigger notifications about our update
                lastUpdate = Instant.now();
                latch.countDown();

            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "Encountered error in update! Shutting down monitor!", e);
                this.stop();
            }
    }

    /**
     * {@code updateAction()} defines what the monitor should do to update. Place the necessary code in this location
     * such that when it is called at the {@link #DEFAULT_UPDATE_INTERVAL} or {@link #updateInterval} an update occurs.
     * Before this is called, updating monitor calls {@link #preUpdateAction()} for exhaust check and general handling.
     */
    protected abstract void updateAction();

    /** Starts the monitor immediately. If start is called after job already started, does nothing. */
    public void start() {
        start(Duration.ZERO);
    }

    /**
     * Starts monitor after initial delay.
     * @throws UnsupportedOperationException if start called after already started
     */
    private void start(Duration initialDelay) {
        if (ex.isShutdown()) { // if it is shut down, make a new thread and restart
            LOGGER.warning("Updater action executor was shut down somehow; creating new one...");
            ex = Executors.newSingleThreadScheduledExecutor();
        }
        if (job == null || job.isDone()) {
            job = ex.scheduleWithFixedDelay(
                    this::preUpdateAction,
                    Math.max(0, initialDelay.toMillis()), // floor initial delay to 0
                    updateInterval.toMillis(),
                    TimeUnit.MILLISECONDS);
        }
    }

    /** Stops the monitor. */
    public void stop() {
        job.cancel(true); // don't allow completion of tasks
    }

    /** @return {@link Instant} of last update start. */
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    /** @return optional {@link Instant} of predicted next update trigger. */
    public Optional<Instant> getNextUpdate() {
        if (job == null) {
            // there is no next update
            LOGGER.info("Asked for next update time, but there is no scheduled future update");
            return Optional.empty();
        }

        // last update + delay
        return Optional.of(lastUpdate.plusMillis(job.getDelay(TimeUnit.MILLISECONDS)));
    }

    /**
     * Sets the monitor's update interval; if task is already running, creates task to wait until almost the end of the
     * current task's delay (ten milliseconds) until restarting the task with the new delay interval.
     * <p>If the job is executing while this method is called, it will be terminated and a new job substituted, with no
     * initial delay; {@link #start(Duration)} floors delays to 0 milliseconds.</p>
     * @param d {@link Duration} to wait between updating
     */
    public void setUpdateInterval(final Duration d) {
        final Duration oldInterval = updateInterval;
        updateInterval = d;

        if (job != null && !job.isDone()) {
            LOGGER.info(String.format("changing delay interval from %d ms to %d ms",
                    oldInterval.toMillis(),
                    updateInterval.toMillis()));

            // job.getDelay returns how many milliseconds until next
            // if job is executing while update interval c
            final Duration neededDelay = Duration.ofMillis(job.getDelay(TimeUnit.MILLISECONDS));
            this.stop();
            this.start(neededDelay);
        }
    }

    /**
     * Gets current update interval in seconds
     * @return timer update interval in integer seconds
     */
    public Duration getUpdateInterval() {
        return updateInterval;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    /** Thrown on exception in update thread. */
    private static class CommUpdateException extends RuntimeException {
        public CommUpdateException(String message, Throwable e) {
            super(message, e);
        }
    }
}
