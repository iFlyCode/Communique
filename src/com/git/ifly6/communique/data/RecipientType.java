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

import com.git.ifly6.communique.io.HappeningsParser;
import com.git.ifly6.nsapi.NSRegion;
import com.git.ifly6.nsapi.NSWorld;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommDelegatesCache;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommRegionCache;
import com.git.ifly6.nsapi.telegram.JTelegramException;
import com.git.ifly6.nsapi.telegram.util.JInfoCache;

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
public enum RecipientType {

    /** Declares the recipient is a nation and requires no further processing in decomposition. */
    NATION {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            // return singleton list
            return Collections.singletonList(cr);
        }
    },

    // This code block must be before the REGION code block otherwise it will get substring matched over :(
    /**
     * Declares that the recipient is a REGION TAG and that it needs decomposing into a list of regions which then is
     * decomposed into the nations therein.
     */
    REGION_TAG {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
            // new 2020-04-04
            List<String> regions = JInfoCache.getInstance().getRegionTag(cr.getName());
            LOGGER.info(String.format("Tag %s: %d regions", cr.getName(), regions.size()));
            return regions.stream()
                    .map(s -> new CommuniqueRecipient(cr.getFilterType(), RecipientType.REGION, s))
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
            LOGGER.info(String.format("Region %s: %d nations", cr.getName(), regionMembers.size()));
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
            if (tag.equals("wa")) return newRecipients(JInfoCache.getInstance().getWAMembers(), cr.getFilterType());
            if (tag.equals("delegates"))
                return newRecipients(CommDelegatesCache.getInstance().getDelegates(), cr.getFilterType());
            if (tag.equals("new")) return newRecipients(NSWorld.getNew(), cr.getFilterType());
            if (tag.equals("all")) return newRecipients(JInfoCache.getInstance().getAll(), cr.getFilterType());
            throw new JTelegramException("Invalid flag: \"" + cr.toString() + "\"");
        }
    },

    /** Declares that the recipient is an internal Commmunique flag. */
    FLAG {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            String tag = cr.getName();
            if (tag.equals("recruit"))
                return Collections.emptyList(); // recruit is handled by Communique logic, not here
            if (tag.equals("active")) return HappeningsParser.getActiveNations();  // active
            return Collections.emptyList();
        }
    },

    /** Declares that the recipient has no recipient type. This element is parsed last. */
    NONE {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) { return Collections.emptyList(); }

        @Override
        public String toString() { return ""; }
    };

    private static final Logger LOGGER = Logger.getLogger(RecipientType.class.getName());

    /**
     * Allows for the recipient type to be compatible with the NationStates telegram system by providing the same tag
     * nomenclature.
     */
    @Override
    public String toString() { return this.name().toLowerCase(); }

    /**
     * Decomposes a tag into a list of <code>CommuniqueRecipient</code> which can then be more easily used.
     * @param cr to be decomposed
     * @return a list of <code>CommuniqueRecipient</code>
     */
    public abstract List<CommuniqueRecipient> decompose(CommuniqueRecipient cr);

    /**
     * Translates a list of nation reference names into a list of valid <code>CommuniqueRecipient</code>s.
     * @param list       of nation reference names
     * @param filterType from which to extract type data
     * @return list of CommuniqueRecipients
     */
    private static List<CommuniqueRecipient> newRecipients(List<String> list, FilterType filterType) {
        // we use this a lot, probably better to use a loop for speed
        List<CommuniqueRecipient> result = new ArrayList<>();
        for (String s : list)
            result.add(CommuniqueRecipients.createNation(filterType, s));
        return result;
    }
}
