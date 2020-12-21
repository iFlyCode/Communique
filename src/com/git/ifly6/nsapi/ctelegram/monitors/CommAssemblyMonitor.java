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
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Range;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Framework for monitoring World Assembly actions.
 * @since version 3.0 (build 13)
 */
public abstract class CommAssemblyMonitor extends CommUpdatingMonitor implements CommMonitor {

    private static final Logger LOGGER = Logger.getLogger(CommAssemblyMonitor.class.getName());
    private boolean exhausted = false;

    protected Chamber chamber;
    protected Vote voting;
    private String resolutionID;

    protected Set<String> previousVoters;
    protected Set<String> currentVoters;

    /**
     * {@inheritDoc} Recipients are current voters were not previously voting for the position specified.
     * @return recipients
     */
    @Override
    public List<String> getRecipients() {
        if (previousVoters == null || currentVoters == null) {
            LOGGER.info("Not enough information to find any changes.");
            return new ArrayList<>();
        }

        if (exhausted) throw new ExhaustedException("Assembly monitor exhausted; initialised resolution ID changed.");
        return new ArrayList<>(Sets.difference(currentVoters, previousVoters));
    }

    @Override
    protected void updateAction() {
        exhausted = determineIfExhausted();
        if (!exhausted) {
            previousVoters = currentVoters;
            currentVoters = getMonitoredRecipients();
        }
    }

    /** Determines whether the {@link CommAssemblyMonitor} is exhausted. */
    protected boolean determineIfExhausted() {
        String currentProposal = CommWorldAssembly.getProposalID(this.chamber);
        if (resolutionID != null)
            return resolutionID.equals(currentProposal);

        // if resolutionID == null, initialise it
        LOGGER.info(String.format("Loading proposal ID into monitor for resolution %s", currentProposal));
        resolutionID = currentProposal;
        return false;
    }

    @Override
    public boolean recipientsExhausted() {
        return exhausted;
    }

    /** @return set of recipients monitored. */
    protected abstract Set<String> getMonitoredRecipients();

    /**
     * Parses data from provided string
     * @param chamber       {@link Chamber}
     * @param voting        {@link Vote}
     * @param ignoreInitial boolean
     * @return internal representations in quartet
     */
    public static Triplet<Chamber, Vote, Boolean> parseStrings(String chamber, String voting, String ignoreInitial) {
        Chamber chamberEnum;
        Vote voteEnum;
        Range<Integer> weightRange;
        boolean boolIgnoreInitial;

        try {
            chamberEnum = Chamber.valueOf(chamber.toUpperCase());
        } catch (IllegalArgumentException e) { throw CommParseException.make(chamber, Chamber.values(), e); }

        try {
            voteEnum = Vote.valueOf(chamber.toUpperCase());
        } catch (IllegalArgumentException e) { throw CommParseException.make(voting, Vote.values(), e); }

        if (!ignoreInitial.equalsIgnoreCase("true")
                && !ignoreInitial.equalsIgnoreCase("false"))
            throw CommParseException.make(ignoreInitial, new String[] {"true", "false"});
        boolIgnoreInitial = Boolean.parseBoolean(ignoreInitial);

        return new Triplet<>(chamberEnum, voteEnum, boolIgnoreInitial);
    }
}
