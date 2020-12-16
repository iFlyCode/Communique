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

import com.git.ifly6.nsapi.NSWorld;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Monitors <a href="https://www.nationstates.net/cgi-bin/api.cgi?q=newnations">new nations</a> API call to provide a
 * stream of new nations to which telegrams can be dispatched. Monitor has a default update interval {@link
 * CommUpdatingMonitor#DEFAULT_UPDATE_INTERVAL}; interval can be changed {@link #setUpdateInterval(Duration)}.
 * Data is only updated after the update interval elapses.
 */
public class CommNewNationsMonitor extends CommUpdatingMonitor implements CommMonitor {

    private static final Logger LOGGER = Logger.getLogger(CommNewNationsMonitor.class.getName());

    private static CommNewNationsMonitor instance;
    private List<String> newNations;

    /**
     * Singleton. One monitor for one site. See also Plutarch, <i>Life of Demetrias</i> {@code
     * http://www.attalus.org/old/demetrius2.html#42}.
     */
    private CommNewNationsMonitor() {
        super();
        start();
    }

    @Override
    protected void updateAction() {
        newNations = NSWorld.getNew();
    }

    /**
     * Get instance of the monitor.
     * @return sole instance
     */
    public static CommNewNationsMonitor getInstance() {
        if (instance == null) instance = new CommNewNationsMonitor();
        return instance;
    }

    /**
     * {@inheritDoc} New nations are updated at interval {@link #setUpdateInterval(Duration)} but default behaviour is
     * to update cached data every 120 seconds. Calling this method multiple times within the update interval will not
     * yield new data.
     */
    @Override
    public List<String> getRecipients() {
        if (newNations == null || newNations.isEmpty()) {
            LOGGER.info("New nations data not yet populated or empty; returning empty set");
            return new ArrayList<>();
        }
        return newNations;
    }
}
