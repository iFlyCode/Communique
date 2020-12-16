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

import com.git.ifly6.nsapi.ctelegram.io.cache.CommRegionCache;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Monitors voters in specified regions for votes for or against a resolution at vote. */
public class CommRegionalVoteMonitor extends CommAssemblyMonitor implements CommMonitor {

    public static final Logger LOGGER = Logger.getLogger(CommRegionalVoteMonitor.class.getName());

    private List<String> regions;
    private CommRegionCache regionCache = CommRegionCache.getInstance();

    /**
     * @param chamber       to check for voters
     * @param regions       to check for voters; cannot be empty
     * @param voting        direction; choose one
     * @param ignoreInitial if true, does not include people who already voted
     */
    public CommRegionalVoteMonitor(CommWorldAssembly.Chamber chamber, List<String> regions,
                                   CommWorldAssembly.Vote voting, boolean ignoreInitial) {
        if (regions.isEmpty()) throw new UnsupportedOperationException("Must provide regions to monitor");
        this.chamber = chamber;
        this.regions = regions;
        this.voting = voting;

        if (ignoreInitial) {
            Set<String> voters = getMonitoredRecipients();
            previousVoters = voters;
            currentVoters = voters;
        }
    }

    /**
     * Gets current voters who are residents of the specified regions.
     * @return list of voters
     */
    @Override
    protected Set<String> getMonitoredRecipients() {
        Set<String> voters = new HashSet<>(CommWorldAssembly.getVoters(chamber, voting));
        Set<String> regionResidents = regions.stream()
                .map(s -> regionCache.lookupObject(s).getRegionMembers())
                .flatMap(List::stream)
                .collect(Collectors.toCollection(HashSet::new));

        return voters.stream()
                .filter(regionResidents::contains)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
