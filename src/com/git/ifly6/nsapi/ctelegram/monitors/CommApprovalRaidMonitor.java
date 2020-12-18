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

import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.NoSuchProposalException;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommDelegatesCache;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommProposalCache;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Proposal;
import static com.git.ifly6.nsapi.ctelegram.io.cache.CommProposalCache.searchProposalListFor;

/**
 * Monitors for approval raids; returns list of recipients who are no longer approving a proposal, whose delegate status
 * changed (ie were bumped), who are currently delegates, whose approval was removed from an existing proposal.
 * @since version 3.0 (build 13)
 */
public class CommApprovalRaidMonitor extends CommUpdatingMonitor {

    private static CommApprovalRaidMonitor instance;
    private List<RemovedApproval> removedApprovals = new ArrayList<>();
    private List<Proposal> previousProposals;
    private List<Proposal> currentProposals;

    private CommApprovalRaidMonitor() {
        currentProposals = CommProposalCache.getInstance().getProposals();
    }

    /** One monitor for one site. */
    public static CommApprovalRaidMonitor getInstance() {
        if (instance == null) instance = new CommApprovalRaidMonitor();
        return instance;
    }

    @Override
    public List<String> getRecipients() {
        return removedApprovals.stream()
                .filter(RemovedApproval::isValid)
                .map(i -> i.delegate)
                .distinct()
                .collect(Collectors.toList());
    }

    /** Does not exhaust. */
    @Override
    public boolean recipientsExhausted() {
        return false;
    }

    @Override
    protected void updateAction() {
        for (RemovedApproval removedApproval : removedApprovals)
            removedApproval.checkDelegateStatus();

        previousProposals = currentProposals;
        currentProposals = CommProposalCache.getInstance().getProposals();

        for (Proposal p : currentProposals) {
            try {
                Proposal lastVersion = searchProposalListFor(previousProposals, p.id);
                List<String> previousApprovals = lastVersion.approvers;
                Set<String> lookupCurrentApprovals = new HashSet<>(p.approvers);

                for (String previousApprover : previousApprovals)
                    if (!lookupCurrentApprovals.contains(previousApprover))
                        // duplicate removed approvals could be added; but duplicate recipients _SHOULD_ be filtered
                        // out in getRecipients' distinct stage
                        removedApprovals.add(
                                new RemovedApproval(previousApprover, lastVersion.id));

            } catch (NoSuchProposalException e) {
                // do nothing
            }
        }
    }


    private static class RemovedApproval {
        private boolean delegateStatusChanged = false;

        private final String delegate;
        private final String approvalRemovedFrom;
        private final Instant removedAsOf = Instant.now();

        public RemovedApproval(String delegate, String approvalRemovedFrom) {
            this.delegate = delegate;
            this.approvalRemovedFrom = approvalRemovedFrom;
        }

        public void checkDelegateStatus() {
            final List<String> delegates = CommDelegatesCache.getInstance().getDelegates();
            if (!delegates.contains(this.delegate))
                delegateStatusChanged = true; // flag is set forever
        }

        @SuppressWarnings("RedundantIfStatement")
        public boolean isValid() {
            if (!CommProposalCache.getInstance().doesCacheContainProposalID(approvalRemovedFrom))
                // if the proposal no longer exists according to our cache, it's not valid anymore
                return false;

            if (!delegateStatusChanged)
                // if the delegate status has not changed, then it may have been a legitimate removal
                return false;

            boolean isDelegate = CommDelegatesCache.getInstance().getDelegates().contains(this.delegate);
            if (!isDelegate)
                // if the recipient is not a delegate, they can't approve. no reason to send
                return false;

            return true; // if all checks pass, return true
        }

        public Instant initialisedAt() {
            return removedAsOf;
        }
    }
}
