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

package com.git.ifly6.communique.data;

import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.NSRegion;
import com.git.ifly6.nsapi.NSWorld;
import com.git.ifly6.nsapi.ctelegram.io.CommHappenings;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Chamber;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Vote;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommDelegatesCache;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommMembersCache;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommRegionCache;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommRegionTagCache;
import com.git.ifly6.nsapi.ctelegram.monitors.updaters.CommApprovalMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.updaters.CommApprovalRaidMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.updaters.CommMovementMonitor;
import com.git.ifly6.nsapi.telegram.JTelegramException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Defines a number of recipient types and provides methods to decompose those types into lists of
 * <code>CommuniqueRecipient</code>.
 * @author ifly6
 * @since version 2.0 (build 7)
 */
public enum CommuniqueRecipientType {

    /** Declares the recipient is a nation and requires no further processing in decomposition. */
    NATION {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            return Collections.singletonList(cr); // return singleton list
        }
    },

    // This code block must be before the REGION code block otherwise it will get substring matched over :(
    /**
     * Declares that the recipient is a REGION TAG and that it needs decomposing into a list of regions which then is
     * decomposed into the nations therein.
     * @since 2020-04-04
     */
    REGION_TAG {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
            List<String> regions = CommRegionTagCache.getInstance().getRegionsWithTag(cr.getName());
//            LOGGER.info(String.format("Tag %s: %d regions", cr.getName(), regions.size()));
            return regions.stream()
                    .map(s -> new CommuniqueRecipient(cr.getFilterType(), CommuniqueRecipientType.REGION, s))
                    .map(CommuniqueRecipient::decompose)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
    },

    /**
     * Declares the recipient is a region, allowing for decomposition into a list of {@link CommuniqueRecipient} nations
     * in the region.
     */
    REGION {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
            NSRegion region = CommRegionCache.getInstance().lookupObject(cr.getName());
            List<String> regionMembers = region.getRegionMembers();

//            LOGGER.info(String.format("Region %s: %d nations", cr.getName(), regionMembers.size()));
            return newRecipients(regionMembers, cr.getFilterType());
        }
    },

    /**
     * Declares the recipient is one of various tags, which can be used to get the members of the World Assembly,
     * delegates thereof, or new nations.
     */
    TAG {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
            String tag = cr.getName();
            if (tag.equals("wa"))
                return newRecipients(CommMembersCache.getInstance().getWAMembers(), cr.getFilterType());
            if (tag.equals("delegates"))
                return newRecipients(CommDelegatesCache.getInstance().getDelegates(), cr.getFilterType());
            if (tag.equals("new"))
                try {
                    return newRecipients(NSWorld.getNew(), cr.getFilterType());
                } catch (NSIOException e) { // in this case, we want it to ignore all errors
                    LOGGER.info("failed to get new recipients from tag:new");
                    return Collections.emptyList();
                }

            throw createIllegalArgumentException(cr);
        }
    },

    /** Happenings flags. */
    _HAPPENINGS {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            String tag = cr.getName();
            if (tag.equals("active")) // active nations
                return newRecipients(CommHappenings.getActiveNations(), cr.getFilterType());

            throw createIllegalArgumentException(cr);
        }
    },

    /**
     * Nations moving into or out of a list of regions. Eg, {@code _movement:into;europe,the_north_pacific} or {@code
     * _movement:out_of;europe}.
     */
    _MOVEMENT {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            String[] tags = cr.getName().split(";\\s+");
            String direction = tags[0];
            String region = tags[1];

            List<String> recipients = CommMovementMonitor.getOrCreate(direction, region).getRecipients();
            return newRecipients(recipients, cr.getFilterType());
        }
    },

    /**
     * Delegates approving a proposal. Eg, {@code _approval:__raids__} for all approval raids, {@code
     * _approval:given_to; PROPOSAL_ID}, or {@code _approval:removed_from; PROPOSAL_ID}.
     */
    _APPROVAL {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            if (cr.getName().equalsIgnoreCase("__RAIDS__"))
                return newRecipients(CommApprovalRaidMonitor.getInstance().getRecipients(), cr.getFilterType());

            String[] tags = cr.getName().split(";\\s+");
            String givenOrRemoved = tags[0];
            String proposal = tags[1];
            return newRecipients(CommApprovalMonitor.getOrCreate(givenOrRemoved, proposal).getRecipients(),
                    cr.getFilterType());
        }
    },

    /** Persons voting on a proposal. Eg, {@code _voting:ga; for} */
    _VOTING {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            final String[] split = cr.getName().split(";\\s+");
            Chamber chamber = Chamber.valueOf(split[0].toUpperCase());
            Vote voting = Vote.valueOf(split[1].toUpperCase());

            return newRecipients(CommWorldAssembly.getVoters(chamber, voting), cr.getFilterType());
        }
    },

    /** Declares that the recipient has no recipient type. This element is parsed last. */
    NONE {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) { return Collections.emptyList(); }

        @Override
        public String toString() { return ""; }
    };

    private static final Logger LOGGER = Logger.getLogger(CommuniqueRecipientType.class.getName());

    /** Recipient type prefixes should be compatible with the NationStates telegram system. */
    @Override
    public String toString() { return this.name().toLowerCase(); }

    /**
     * Decomposes tag into {@code List<{@link CommuniqueRecipient}>}.
     * @param cr tag to be decomposed
     * @return list of recipients
     */
    public abstract List<CommuniqueRecipient> decompose(CommuniqueRecipient cr);

    /**
     * Translates nation reference names into {@link CommuniqueRecipientType#NATION} {@code CommuniqueRecipient}s.
     * @param list       of nation reference names
     * @param filterType from which to extract filter type data
     * @return list of recipients
     */
    private static List<CommuniqueRecipient> newRecipients(List<String> list, CommuniqueFilterType filterType) {
        // we use this a lot, probably better to use a loop for speed
        List<CommuniqueRecipient> result = new ArrayList<>(list.size());
        for (String s : list)
            result.add(CommuniqueRecipients.createNation(filterType, s));
        return result;
    }

    /** Creates illegal argument exception with preformatted string. */
    private static IllegalArgumentException createIllegalArgumentException(CommuniqueRecipient s) {
        return new IllegalArgumentException(String.format("Invalid tag string in <%s>!", s));
    }
}
