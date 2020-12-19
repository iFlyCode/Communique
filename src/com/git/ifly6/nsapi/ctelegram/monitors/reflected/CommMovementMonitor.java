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

package com.git.ifly6.nsapi.ctelegram.monitors.reflected;

import com.git.ifly6.nsapi.NSRegion;
import com.git.ifly6.nsapi.ctelegram.io.CommParseException;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Monitors movement in or out, see {@link Direction}, of a specified region.
 * @since version 3.0 (build 13)
 */
public class CommMovementMonitor extends CommUpdatingMonitor implements CommMonitor {

    private List<String> regions;
    private Direction direction;

    private Set<String> inhabitantsBefore;
    private Set<String> inhabitantsNow;

    /**
     * Creates a movement monitor.
     * @param regions   to monitor
     * @param direction of travel to monitor
     */
    public CommMovementMonitor(List<String> regions, Direction direction) {
        this.regions = regions;
        this.direction = direction;
    }

    /**
     * Create method with strings for reflection.
     * @param regionList list of regions, separated by commas
     * @param dirString direction string {@link Direction}
     * @return new monitor
     */
    public static CommMovementMonitor create(String regionList, String dirString) {
        List<String> regions = parseList(regionList);
        Direction direction;

        try {
            direction = Direction.valueOf(dirString);
        } catch (IllegalArgumentException e) { throw CommParseException.make(dirString, Direction.values(), e); }

        return new CommMovementMonitor(regions, direction);
    }

    /** Recipients generate slowly, but never exhaust. */
    @Override
    public boolean recipientsExhausted() {
        return false;
    }

    @Override
    protected void updateAction() {
        if (Objects.isNull(regions) || regions.isEmpty())
            throw new NullPointerException("job started without specifying region");

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
     * nations depending on {@link Direction}.
     * <p>Note that recipients returned for multiple regions are movements in and out of those regions taken together.
     * If monitoring Europe and the North Pacific, someone who moves from the North Pacific <b>to</b> Europe will not be
     * marked.</p>
     */
    @Override
    public List<String> getRecipients() {
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

    public enum Direction {

        ENTER {
            /** {@inheritDoc} Something that enters present after but not present before. */
            @Override
            public List<String> apply(Set<String> before, Set<String> after) {
                return after.stream()
                        .filter(s -> !before.contains(s))
                        .collect(Collectors.toList());
            }
        },

        EXIT {
            /** {@inheritDoc} Something that exits is present before, but not after. */
            @Override
            public List<String> apply(Set<String> before, Set<String> after) {
                return before.stream()
                        .filter(s -> !after.contains(s))
                        .collect(Collectors.toList());
            }
        };

        /**
         * Applies differencing algorithm based on enumerated description
         * @param before elements present before
         * @param after  elements present after
         * @return appropriate difference
         */
        public abstract List<String> apply(Set<String> before, Set<String> after);
    }
}


