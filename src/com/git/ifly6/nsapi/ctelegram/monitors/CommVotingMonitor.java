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
import com.git.ifly6.nsapi.ctelegram.io.CommRegionCache;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Monitors voters in specified regions for votes for or against a resolution at vote. */
public class CommVotingMonitor extends CommUpdatingMonitor implements CommMonitor {

    public static final Logger LOGGER = Logger.getLogger(CommVotingMonitor.class.getName());
    private static CommVotingMonitor instance;

    private CommWorldAssembly.Chamber chamber;
    private final CommWorldAssembly.Vote voting;

    private List<String> regions;
    private CommRegionCache regionCache = CommRegionCache.getInstance();

    private Set<String> previousVoters;
    private Set<String> currentVoters;

    /**
     * @param chamber       to check for voters
     * @param regions       to check for voters, if empty, all regions
     * @param voting        direction; choose one
     * @param ignoreInitial if true, does not include people who already voted
     */
    public CommVotingMonitor(CommWorldAssembly.Chamber chamber, List<String> regions, CommWorldAssembly.Vote voting,
                             boolean ignoreInitial) {
        this.chamber = chamber;
        this.regions = regions;
        this.voting = voting;

        if (ignoreInitial) {
            Set<String> voters = new HashSet<>(getCurrentVoters(this.chamber, this.voting));
            previousVoters = voters;
            currentVoters = voters;
        }
    }

    @Override
    public List<String> getRecipients() {
        if (previousVoters == null || currentVoters == null) {
            LOGGER.info("Not enough information to find any changes.");
            return new ArrayList<>();
        }
        return currentVoters.stream()
                .filter(s -> !previousVoters.contains(s))
                .collect(Collectors.toList());
    }

    @Override
    protected void updateAction() {
        Set<String> voters = getCurrentVoters(this.chamber, this.voting);
        previousVoters = currentVoters;
        currentVoters = voters;
    }

    /**
     * Gets current voters who are residents of the specified regions.
     * @param chamber to look at
     * @param voting position
     * @return list of voters
     */
    private Set<String> getCurrentVoters(CommWorldAssembly.Chamber chamber, CommWorldAssembly.Vote voting) {
        Set<String> voters = new HashSet<>(CommWorldAssembly.getVoters(chamber, voting));

        Set<String> regionResidents = new HashSet<>();
        for (String r : regions) {
            NSRegion region = regionCache.lookupObject(r);
            regionResidents.addAll(region.getRegionMembers());
        }

        return voters.stream()
                .filter(regionResidents::contains)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
