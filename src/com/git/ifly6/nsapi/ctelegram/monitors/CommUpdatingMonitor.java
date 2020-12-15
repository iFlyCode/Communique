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

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Monitors <a href="https://www.nationstates.net/cgi-bin/api.cgi?q=newnations">new nations</a> API call to provide a
 * stream of new nations to which telegrams can be dispatched. Monitor has a default update interval {@link
 * #DEFAULT_UPDATE_INTERVAL}; interval can be changed {@link #setUpdateInterval(int)}. Data is only updated after the
 * update interval elapses.
 */
public abstract class CommUpdatingMonitor implements CommMonitor {

    public static final int DEFAULT_UPDATE_INTERVAL = 120 * 1000;
    private Integer updateInterval = null;

    protected Instant lastUpdate;

    private Timer timer;
    private TimerTask updateAction = new TimerTask() {
        @Override
        public void run() {
            updateAction();
        }
    };

    /**
     * Constructs an updating monitor with {@link #DEFAULT_UPDATE_INTERVAL} which, when it starts, calls an update
     * action. Monitors should automatically be started in their constructors; manual start is necessary to allow
     * assignment of needed variables.
     */
    public CommUpdatingMonitor() {
        timer = new Timer(true);
    }

    /**
     * {@code updateAction()} defines what the monitor should do to update. The programmer should place the necessary
     * code in this location such that when it is called at the {@link #DEFAULT_UPDATE_INTERVAL} or whatever interval is
     * specified, the internals of the class are updated to reflect the new reality on the ground.
     */
    protected abstract void updateAction();

    /** Starts the monitor. */
    public void start() {
        timer.schedule(updateAction, 0,
                updateInterval == null ? DEFAULT_UPDATE_INTERVAL : updateInterval);
    }

    /** Stops the monitor. */
    public void stop() {
        timer.cancel();
    }

    /** @return {@link Instant} of last update. */
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Sets the monitor's update interval
     * @param seconds to wait between updating
     */
    public void setUpdateInterval(int seconds) {
        updateInterval = seconds;
    }

    /**
     * Gets current update interval
     * @return timer update interval
     */
    public int getUpdateInterval() {
        int delay = updateInterval == null ? DEFAULT_UPDATE_INTERVAL : updateInterval;
        return (int) Math.round((double) delay / 1000);
    }

}
