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

import com.git.ifly6.nsapi.NSRegion;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Monitors movement in or out, see {@link CommMovementDirection}, of a specified region.
 */
public class CommMovementMonitor extends CommUpdatingMonitor implements CommMonitor {

    private List<String> regions;
    private CommMovementDirection direction;

    private Set<String> inhabitantsBefore;
    private Set<String> inhabitantsNow;

    /**
     * Creates a movement monitor. Monitor starts immediately.
     * @param r regions to monitor
     * @param d direction of travel to check for
     */
    public CommMovementMonitor(List<String> r, CommMovementDirection d) {
        this(r, d, true);
    }

    /**
     * Creates a movement monitor.
     * @param regions          to monitor
     * @param direction        of travel to monitor
     * @param startImmediately if true
     */
    public CommMovementMonitor(List<String> regions, CommMovementDirection direction, boolean startImmediately) {
        super();
        this.regions = regions;
        this.direction = direction;
        if (startImmediately)
            start();
    }

    @Override
    protected void updateAction() {
        if (Objects.isNull(regions) || regions.isEmpty())
            throw new NullPointerException("timer started without specifying region");

        inhabitantsBefore = inhabitantsNow;
        Set<String> newInhabitants = new HashSet<>();
        for (final String regionName : regions)
            newInhabitants.addAll(
                    new NSRegion(regionName).populateData().getRegionMembers()
            );

        inhabitantsNow = newInhabitants;
    }

    /**
     * {@inheritDoc} Recipients can only be found after {@link #DEFAULT_UPDATE_INTERVAL} or update interval established
     * by {@link #setUpdateInterval(Duration)} If not enough time has elapsed, returns an empty list; otherwise, returns
     * nations depending on {@link CommMovementDirection}.
     * <p>Note that recipients returned for multiple regions are movements in and out of those regions taken together.
     * If monitoring Europe and the North Pacific, someone who moves from the North Pacific <b>to</b> Europe will not be
     * marked.</p>
     */
    @Override
    public List<String> getRecipients() {
        if (direction == null)
            throw new NullPointerException("movement information queried without direction");

        if (inhabitantsNow == null || inhabitantsBefore == null) {
            LOGGER.info("Not enough information to find any changes.");
            return new ArrayList<>();
        }

        return direction.apply(inhabitantsBefore, inhabitantsNow);
    }

    /** @return regions at which the movement monitor is pointed. */
    public List<String> getRegions() {
        return regions;
    }
}


