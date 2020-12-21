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

    private Duration updateInterval = null;

    /** {@link Instant} of last update start. */
    private Instant lastUpdate;

    private ScheduledExecutorService ex;
    private ScheduledFuture<?> job;
    private Runnable runnableAction = () -> {
        try {
            lastUpdate = Instant.now();
            LOGGER.info("update triggered");
            updateAction();
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "Encountered error in update!", e);
            this.stop();
        }
    };

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

        ex = Executors.newSingleThreadScheduledExecutor();
        this.updateInterval = updateInterval;
        this.start();
    }

    /**
     * Turns provided list, as string, in format {@code BLAH, BLAH, BLAH} into a list of strings. All elements are
     * normalised with {@link ApiUtils#ref(List)}.
     * @param listString to parse
     * @return {@code List<String>} representation thereof
     */
    public static List<String> parseList(String listString) {
        List<String> regions;
        if (!listString.contains(",")) regions = Collections.singletonList(ApiUtils.ref(listString));
        else regions = Arrays.stream(listString.split(","))
                .map(ApiUtils::ref)
                .filter(s -> !ApiUtils.isEmpty(s))
                .collect(Collectors.toList());
        return regions;
    }

    @Override
    public abstract boolean recipientsExhausted();

    /**
     * {@code updateAction()} defines what the monitor should do to update. Place the necessary code in this location
     * such that when it is called at the {@link #DEFAULT_UPDATE_INTERVAL} or {@link #updateInterval} an update occurs.
     */
    protected abstract void updateAction();

    /** Starts the monitor immediately. If start is called after job already started, does nothing. */
    private void start() {
        start(Duration.ZERO);
    }

    /**
     * Starts monitor after initial delay.
     * @throws UnsupportedOperationException if start called after already started
     */
    private void start(Duration initialDelay) {
        if (ex.isShutdown()) { // if it is shut down, make a new thread and restart
            LOGGER.info("update action executor service was shut down somehow; allocating new...");
            ex = Executors.newSingleThreadScheduledExecutor();
        }
        if (job == null || job.isDone()) {
            job = ex.scheduleWithFixedDelay(runnableAction,
                    Math.max(0, initialDelay.toMillis()), // floor initial delay to 0
                    updateInterval.toMillis(),
                    TimeUnit.MILLISECONDS);

        } else {
            LOGGER.info("Attempted to start after job already started!");
            throw new UnsupportedOperationException("Cannot start monitor after it already started");
        }
    }

    /**
     * Restarts the monitor immediately. All monitors should initialise and start automatically. This method is provided
     * to restart a stopped monitor.
     */
    public void restart() {
        start();
    }

    /** Stops the monitor. */
    public void stop() {
        job.cancel(true); // don't allow completion of tasks
    }

    /** @return {@link Instant} of last update start. */
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    /** @return {@link Instant} of predicted next update trigger. */
    public Instant getNextUpdate() {
        if (job == null) {
            // there is no next update
            LOGGER.info("Asked for next update time, but there is no scheduled future update");
            return null;
        }

        // last update + delay
        return lastUpdate.plusMillis(job.getDelay(TimeUnit.MILLISECONDS));
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

    /** Thrown on exception in update thread. */
    private static class CommUpdateException extends RuntimeException {
        public CommUpdateException(String message, Throwable e) {
            super(message, e);
        }
    }
}
