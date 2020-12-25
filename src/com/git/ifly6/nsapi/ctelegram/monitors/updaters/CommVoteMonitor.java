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

package com.git.ifly6.nsapi.ctelegram.monitors.updaters;

import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Chamber;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Vote;
import com.git.ifly6.nsapi.ctelegram.io.permcache.CommPermanentCache;
import com.git.ifly6.nsapi.ctelegram.monitors.CommAssemblyMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import org.javatuples.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static com.git.ifly6.nsapi.ctelegram.io.permcache.CommPermanentCache.createKey;

/**
 * Monitors voters in specified regions for votes for or against a resolution at vote.
 * @since version 3.0 (build 13)
 */
public class CommVoteMonitor extends CommAssemblyMonitor implements CommMonitor {

    public static final Logger LOGGER = Logger.getLogger(CommVoteMonitor.class.getName());
    private static CommPermanentCache<CommVoteMonitor> cache = new CommPermanentCache<>();

    /**
     * @param chamber to check for voters
     * @param voting  direction; choose one
     */
    public CommVoteMonitor(Chamber chamber, Vote voting) {
        this.chamber = chamber;
        this.voting = voting;

        /* This code here comes from an earlier version of the monitor; it initally had a set-up phase where it
         * determined whether or not it should include recipients already voting at the start (ie, if you start it up
         * after voting has started, it won't include anyone who has already voted). For ease of instantiation, this
         * functionality was stripped. See commit 9b93b08c510f749f9b4e81c7bb150bc533f30a0b. */
//        if (ignoreInitial) {
//            Set<String> voters = getMonitoredRecipients();
//            previousVoters = voters;
//            currentVoters = voters;
//        }
    }

    /**
     * Gets or creates monitor from string input.
     * @return new monitor or existing instance if already exists
     */
    public static CommVoteMonitor getOrCreate(String chamber, String voting) {
        Pair<Chamber, Vote> values = CommAssemblyMonitor.parseStrings(chamber, voting);
        return cache.getOrCreate(
                createKey(chamber, voting),
                () -> new CommVoteMonitor(values.getValue0(), values.getValue1()));
    }

    /**
     * Gets current voters.
     * @return list of voters
     */
    @Override
    protected Set<String> getMonitoredRecipients() {
        return new HashSet<>(CommWorldAssembly.getVoters(chamber, voting));
    }
}
