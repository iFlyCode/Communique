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

package com.git.ifly6.nsapi.ctelegram.io.cache;

import com.git.ifly6.nsapi.NSTimeStamped;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.NoSuchProposalException;
import static com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Proposal;
import static com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.getAllProposals;

/**
 * Caches proposals. Cache duration is {@link #PROPOSAL_CACHE_DURATION}.
 * @since version 3.0 (build 13)
 */
public class CommProposalCache extends CommCache<CommProposalCache.ProposalList> {

    public static final Duration PROPOSAL_CACHE_DURATION = Duration.ofMinutes(10);
    public static final String PROPOSAL_LIST_KEY = "__proposal_list__";
    private static CommProposalCache instance;

    private CommProposalCache(Duration duration) { super(duration); }

    public static CommProposalCache getInstance() {
        if (instance == null)
            instance = new CommProposalCache(PROPOSAL_CACHE_DURATION);
        return instance;
    }

    @Override
    protected ProposalList createNewObject(String s) {
        return new ProposalList();
    }

    /** Prefer {@link #getProposals()}. */
    @Override
    public ProposalList lookupObject(String s) {
        if (s.equals(PROPOSAL_LIST_KEY))
            return super.lookupObject(s);
        throw new UnsupportedOperationException(
                "Proposal cache only supports looking up with " + PROPOSAL_LIST_KEY);
    }

    /**
     * Gets proposals in the cache.
     * @return list of proposals
     */
    public List<Proposal> getProposals() {
        return this.lookupObject(PROPOSAL_LIST_KEY).getProposalList();
    }

    /**
     * Gets list of proposal IDs in the cache.
     * @return list of proposal IDs in the cache
     */
    public List<String> getCachedProposalIDs() {
        return this.getProposals()
                .stream()
                .map(i -> i.id)
                .collect(Collectors.toList());
    }

    /** Tests whether provided proposal ID is in the cache. */
    public boolean doesCacheContainProposalID(String proposalID) {
        return getCachedProposalIDs().contains(proposalID);
    }

    /**
     * Gets approvers of the provided proposal ID.
     * @return list of approvers
     * @throws NoSuchProposalException if cache does not contain proposal ID
     */
    public List<String> getApproversOf(String proposalID) {
        Proposal p = searchProposalListFor(this.getProposals(), proposalID);
        return p.approvers;
    }

    /**
     * Searches a list of proposals for a proposal corresponding to the provided id.
     * @param proposals  to search through
     * @param proposalID to search for
     * @return proposal in {@code proposals} corresponding to proposal ID
     * @throws NoSuchProposalException if provided list does not contain proposal ID
     */
    public static Proposal searchProposalListFor(List<Proposal> proposals, String proposalID) {
        for (Proposal p : proposals)
            if (p.id.equals(proposalID))
                return p;

        throw new NoSuchProposalException(
                String.format("Proposal %s does not appear in provided proposal list", proposalID));
    }

    /** Stores data on proposals as a whole. */
    public static class ProposalList implements NSTimeStamped {
        private List<Proposal> proposalList;
        private final Instant timestamp = Instant.now();

        public ProposalList() {
            this.proposalList = getAllProposals();
        }

        public List<Proposal> getProposalList() {
            return proposalList;
        }

        @Override
        public Instant timestamp() {
            return this.timestamp;
        }
    }
}
