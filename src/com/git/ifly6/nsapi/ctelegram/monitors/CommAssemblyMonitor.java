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

import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class CommAssemblyMonitor extends CommUpdatingMonitor implements CommMonitor {

    private static final Logger LOGGER = Logger.getLogger(CommAssemblyMonitor.class.getName());
    private boolean exhausted = false;

    protected CommWorldAssembly.Chamber chamber;
    protected CommWorldAssembly.Vote voting;
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
        return currentVoters.stream()
                .filter(s -> !previousVoters.contains(s))
                .collect(Collectors.toList());
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
}
