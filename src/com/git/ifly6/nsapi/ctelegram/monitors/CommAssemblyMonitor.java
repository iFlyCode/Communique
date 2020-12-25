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

import com.git.ifly6.nsapi.ctelegram.io.CommParseException;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Chamber;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Vote;
import org.javatuples.Pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.git.ifly6.nsapi.ApiUtils.shuffled;

/**
 * Framework for monitoring World Assembly actions.
 * @since version 3.0 (build 13)
 */
public abstract class CommAssemblyMonitor extends CommUpdatingMonitor implements CommMonitor {

    private static final Logger LOGGER = Logger.getLogger(CommAssemblyMonitor.class.getName());
    private static final Comparator<Entry<String, Instant>> entryComparator =
            Entry.<String, Instant>comparingByValue()
                    .reversed().thenComparing(Entry::getKey);

    private boolean exhausted = false;

    protected Chamber chamber;
    protected Vote voting;
    private String resolutionID;

    protected Map<String, Instant> voters = new HashMap<>();

    /**
     * {@inheritDoc} Recipients are <b>ALL</b> current voters.
     * @return recipients, ordered with the most recently added voters first
     */
    @Override
    public List<String> getRecipients() {
        if (exhausted)
            throw new ExhaustedException("Assembly monitor exhausted; check logs for cause");

        return voters.entrySet().stream()
                .sorted(entryComparator)
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    protected void updateAction() {
        exhausted = determineIfExhausted();
        if (!exhausted) {
            Set<String> currentVoters = getMonitoredRecipients();
            List<String> currentVoterList = shuffled(new ArrayList<>(currentVoters));

            // update old voters by adding new voters
            currentVoterList.stream()
                    .filter(newVoter -> !voters.containsKey(newVoter))
                    .forEach(newVoter -> voters.put(newVoter, Instant.now()));

            // update old voters by removing people no longer voting
            voters.keySet().removeIf(oldVoter -> !currentVoters.contains(oldVoter));
        }
    }

    /** Determines whether the {@link CommAssemblyMonitor} is exhausted. */
    protected boolean determineIfExhausted() {
        try {
            String currentProposal = CommWorldAssembly.getProposalID(this.chamber);
            if (resolutionID != null)
                return !resolutionID.equals(currentProposal); // must be NOT this; this is when it is NOT exhausted!

            // if resolutionID == null, initialise it
            LOGGER.info(String.format("Loading proposal ID into monitor for resolution %s", currentProposal));
            resolutionID = currentProposal;
            return false;

        } catch (CommWorldAssembly.NoSuchProposalException e) {
            LOGGER.warning("No proposal at vote in chamber! Automatically exhausting monitor");
            return true;
        }
    }

    @Override
    public boolean recipientsExhausted() {
        return exhausted;
    }

    /**
     * This is only called <i>after</i> checking {@link #determineIfExhausted()}.
     * @return set of recipients monitored.
     */
    protected abstract Set<String> getMonitoredRecipients();

    /**
     * Parses data from provided string
     * @param chamber {@link Chamber}
     * @param voting  {@link Vote}
     * @return internal representations in quartet
     */
    public static Pair<Chamber, Vote> parseStrings(String chamber, String voting) {
        Chamber chamberEnum;
        Vote voteEnum;

        try {
            chamberEnum = Chamber.valueOf(chamber.toUpperCase());
        } catch (IllegalArgumentException e) { throw CommParseException.make(chamber, Chamber.values(), e); }

        try {
            voteEnum = Vote.valueOf(voting.toUpperCase());
        } catch (IllegalArgumentException e) { throw CommParseException.make(voting, Vote.values(), e); }

        return new Pair<>(chamberEnum, voteEnum);
    }
}
