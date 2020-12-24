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

/**
 * Contains utility methods to make it less verbose to create a {@link CommuniqueRecipient}. Also provides functionality
 * to help with the fact that {@link CommuniqueRecipient}s are immutable.
 * @since version 2.0 (build 7)
 */
public class CommuniqueRecipients {

    // Prevent initialisation
    private CommuniqueRecipients() {
    }

    public static CommuniqueRecipient createNation(FilterType filter, String nationName) {
        return new CommuniqueRecipient(filter, RecipientType.NATION, nationName);
    }

    public static CommuniqueRecipient createNation(String nationName) {
        return createNation(FilterType.NORMAL, nationName);
    }

    public static CommuniqueRecipient createExcludedNation(String nationName) {
        return createNation(FilterType.EXCLUDE, nationName);
    }

    public static CommuniqueRecipient createRegion(FilterType filterType, String regionName) {
        return new CommuniqueRecipient(filterType, RecipientType.REGION, regionName);
    }

    public static CommuniqueRecipient createTag(FilterType filterType, String tag) {
        return new CommuniqueRecipient(filterType, RecipientType.TAG, tag);
    }

    /**
     * Creates new recipient with copied {@link RecipientType} and name, but with {@link FilterType#EXCLUDE}
     * @param recipient holding name and type
     * @return the recipient, excluded
     */
    public static CommuniqueRecipient exclude(CommuniqueRecipient recipient) {
        return new CommuniqueRecipient(FilterType.EXCLUDE, recipient.getRecipientType(), recipient.getName());
    }

    public static CommuniqueRecipient setFilter(CommuniqueRecipient recipient, FilterType filterType) {
        return new CommuniqueRecipient(filterType, recipient.getRecipientType(), recipient.getName());
    }

}
