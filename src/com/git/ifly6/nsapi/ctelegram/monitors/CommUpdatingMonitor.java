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
 * #DEFAULT_UPDATE_INTERVAL}; interval can be changed {@link #setUpdateInterval(int)}. Data is only updated after the
 * update interval elapses.
 */
public abstract class CommUpdatingMonitor implements CommMonitor {

    public static final Logger LOGGER = Logger.getLogger(CommUpdatingMonitor.class.getName());

    public static final Duration DEFAULT_UPDATE_INTERVAL = Duration.ofSeconds(120);
    private Duration updateInterval = null;

    protected Instant lastUpdate;

    private ScheduledExecutorService ex;
    private ScheduledFuture<?> sf;
    private Runnable runnableAction = () -> {
        try {
            LOGGER.info("update called");
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
     * {@code updateAction()} defines what the monitor should do to update. The programmer should place the necessary
     * code in this location such that when it is called at the {@link #DEFAULT_UPDATE_INTERVAL} or whatever interval is
     * specified, the internals of the class are updated to reflect the new reality on the ground.
     */
    protected abstract void updateAction();

    /** Starts the monitor. Most implementations should start on instantiation. */
    public void start() {
        if (ex.isShutdown()) // if it is shut down, create a new thread and restart
            LOGGER.info("update action executor service was shut down; allocating new...");
        ex = Executors.newSingleThreadScheduledExecutor();

        if (sf == null || sf.isDone()) {
            sf = ex.scheduleWithFixedDelay(runnableAction, 0,
                    updateInterval == null ? DEFAULT_UPDATE_INTERVAL.toMillis() : updateInterval.toMillis(),
                    TimeUnit.MILLISECONDS);
        }
    }

    /** Stops the monitor. */
    public void stop() {
        sf.cancel(false); // allow completion of tasks
    }

    /** @return {@link Instant} of last update. */
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    /** @return {@link Instant} of predicted next update. */
    public Instant getNextUpdate() {
        if (sf == null) {
            // there is no next update
            LOGGER.info("Asked for next update time, but there is no scheduled future update");
            return null;
        }

        // last update + delay, with higher resolution
        return lastUpdate.plusMillis(sf.getDelay(TimeUnit.MILLISECONDS));
    }

    /**
     * Sets the monitor's update interval; if task is already running, creates task to wait until end of current task
     * before restarting with the new delay interval.
     * @param d duration to wait between updating
     */
    public void setUpdateInterval(Duration d) {
        updateInterval = d;
        if (sf != null) {
            LOGGER.info(String.format("updating delay interval from %d ms to %d ms",
                    sf.getDelay(TimeUnit.MILLISECONDS),
                    updateInterval.toMillis()));
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
        public CommUpdateException(String message, Throwable e) {}
    }
}
