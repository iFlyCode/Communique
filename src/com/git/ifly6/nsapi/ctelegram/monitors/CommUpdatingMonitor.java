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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Monitors <a href="https://www.nationstates.net/cgi-bin/api.cgi?q=newnations">new nations</a> API call to provide a
 * stream of new nations to which telegrams can be dispatched. Monitor has a default update interval {@link
 * #DEFAULT_UPDATE_INTERVAL}; interval can be changed {@link #setUpdateInterval(Duration)}. Data is only updated after
 * the update interval elapses.
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
            e.printStackTrace();
            throw new CommUpdateException("encountered error in update", e);
        }
    };

    /**
     * Constructs an updating monitor with {@link #DEFAULT_UPDATE_INTERVAL} which, when it starts, calls an update
     * action. Monitors should automatically be started in their constructors; manual start is necessary to allow
     * assignment of needed variables.
     */
    public CommUpdatingMonitor() {
        ex = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * {@code updateAction()} defines what the monitor should do to update. Place the necessary code in this location
     * such that when it is called at the {@link #DEFAULT_UPDATE_INTERVAL} or {@link #updateInterval} an update occurs.
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
    public void start(Duration initialDelay) {
        if (ex.isShutdown()) { // if it is shut down, create a new thread and restart
            LOGGER.info("update action executor service was shut down somehow; allocating new...");
            ex = Executors.newSingleThreadScheduledExecutor();
        }
        if (job == null || job.isDone()) {
            job = ex.scheduleWithFixedDelay(runnableAction,
                    initialDelay.toMillis(),
                    (updateInterval == null
                            ? DEFAULT_UPDATE_INTERVAL
                            : updateInterval).toMillis(),
                    TimeUnit.MILLISECONDS);

        } else {
            LOGGER.info("Attempted to start after job already started!");
            throw new UnsupportedOperationException("Cannot start monitor after it already started");
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
     * @param d duration to wait between updating
     */
    public void setUpdateInterval(Duration d) {
        final Duration oldUpdateInterval = updateInterval;
        updateInterval = d;

        if (job != null) {
            LOGGER.info(String.format("changing delay interval from %d ms to %d ms",
                    oldUpdateInterval.toMillis(),
                    updateInterval.toMillis()));

            // job.getDelay returns how many milliseconds until next; neededDelay is 10 milliseconds before that
            final long neededDelay = job.getDelay(TimeUnit.MILLISECONDS);
            this.stop();
            this.start(Duration.ofMillis(neededDelay));
        }
    }

    /**
     * Gets current update interval in seconds
     * @return timer update interval in integer seconds
     */
    public Duration getUpdateInterval() {
        return updateInterval == null ? DEFAULT_UPDATE_INTERVAL : updateInterval;
    }

    /** Thrown on exception in update thread. */
    private static class CommUpdateException extends RuntimeException {
        public CommUpdateException(String message, Throwable e) {
            super(message, e);
        }
    }
}
