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

import com.git.ifly6.nsapi.ctelegram.io.CommParseException;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommDelegatesCache;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Monitors approval actions related to a World Assembly proposal.
 * @since version 3.0 (build 13)
 */
public class CommApprovalMonitor extends CommUpdatingMonitor implements CommMonitor {

    private boolean exhausted;

    private String proposalID;
    private Action action;
    private List<String> previousApprovers;
    private List<String> currentApprovers;

    /** Creates monitor to monitor provided proposal ID for specified action. */
    public CommApprovalMonitor(String proposalID, Action action) {
        this.proposalID = proposalID;
        previousApprovers = new ArrayList<>();
        currentApprovers = new ArrayList<>();
    }

    /**
     * Create method from string input for reflection.
     * @param proposalID as passed
     * @param actionString {@link Action}
     * @return new monitor
     */
    public static CommApprovalMonitor create(String proposalID, String actionString) {
        Action action;
        try {
            action = Action.valueOf(actionString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw CommParseException.make(actionString, Action.values(), e);
        }
        return new CommApprovalMonitor(proposalID, action);
    }

    /**
     * {@inheritDoc} Only returns approvers which undertook the specified {@link Action} and which are (according to
     * cached data, see {@link CommDelegatesCache},) currently delegates.
     * @return list of delegates that took specified action.
     */
    @Override
    public List<String> getRecipients() {
        if (exhausted) throw new ExhaustedException("Proposal no longer exists; approval change stream exhausted.");
        Set<String> delegateSet = new HashSet<>(CommDelegatesCache.getInstance().getDelegates());
        List<String> changedApprovers = action.find(previousApprovers, currentApprovers);
        return changedApprovers.stream()
                .filter(delegateSet::contains)
                .collect(Collectors.toList());
    }

    @Override
    public boolean recipientsExhausted() {
        return exhausted;
    }

    @Override
    protected void updateAction() {
        previousApprovers = currentApprovers;
        try {
            currentApprovers = CommWorldAssembly.getApprovers(proposalID);
        } catch (CommWorldAssembly.NoSuchProposalException e) {
            exhausted = true;
        }
    }

    /** Enumerates actions undertaken by delegates. */
    public enum Action {
        APPROVED {
            @Override
            public List<String> find(List<String> before, List<String> after) {
                // elements in after that are were not in before
                Set<String> beforeSet = new HashSet<>(before);
                return after.stream().filter(s -> !beforeSet.contains(s))
                        .collect(Collectors.toList());
            }
        }, UNAPPROVED {
            @Override
            public List<String> find(List<String> before, List<String> after) {
                // elements in before that are not in afterSet
                Set<String> afterSet = new HashSet<>(before);
                return before.stream().filter(s -> !afterSet.contains(s))
                        .collect(Collectors.toList());
            }
        };

        public abstract List<String> find(List<String> before, List<String> after);
    }
}
