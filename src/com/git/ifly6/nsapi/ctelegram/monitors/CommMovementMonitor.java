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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Monitors movement in or out, see {@link Direction}, of a specified region.
 */
public class CommMovementMonitor extends CommUpdatingMonitor implements CommMonitor {

    public enum Direction {
        ENTER, EXIT,
    }

    private String region;
    private Direction direction;

    private Set<String> inhabitantsBefore;
    private Set<String> inhabitantsNow;

    /**
     * Refuse instantiation without providing region and direction.
     */
    private CommMovementMonitor() {
    }

    /**
     * Creates a movement monitor.
     * @param region    to monitor
     * @param direction of travel to monitor
     */
    public CommMovementMonitor(String region, Direction direction) {
        super();
        this.region = region;
        this.direction = direction;
        start();
    }

    @Override
    protected void updateAction() {
        if (Objects.isNull(region)) throw new NullPointerException("timer started without specifying region");
        inhabitantsBefore = inhabitantsNow;
        inhabitantsNow = new HashSet<>(new NSRegion(region).populateData().getRegionMembers());
        lastUpdate = Instant.now();
    }

    /**
     * {@inheritDoc} Recipients can only be found after {@link #DEFAULT_UPDATE_INTERVAL} or update interval established
     * by {@link #setUpdateInterval(int)}. If not enough time has elapsed, returns an empty list; otherwise, returns
     * nations depending on {@link Direction}.
     */
    @Override
    public List<String> getRecipients() {
        if (direction == null) throw new NullPointerException("movement information queried without direction");
        if (inhabitantsNow == null || inhabitantsBefore == null)
            return new ArrayList<String>();

        if (direction == Direction.EXIT) // if exiting, look at ones who WERE present and NOW are not
            return inhabitantsBefore.stream()
                    .filter(s -> !inhabitantsNow.contains(s))
                    .collect(Collectors.toList());

        if (direction == Direction.ENTER) // if entering, look at ones who ARE present and WERE not
            return inhabitantsNow.stream()
                    .filter(s -> !inhabitantsBefore.contains(s))
                    .collect(Collectors.toList());

        throw new UnsupportedOperationException(
                String.format("no operation available for direction <%s>", direction));
    }

    /** @return region at which the movement monitor is pointed. */
    public String getRegion() {
        return region;
    }
}


