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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Defines a number of filter types which can be used in {@link Communique7Parser} to effect the recipients list. All of
 * the exact definitions of what occurs are kept here.
 * @author ifly6
 * @since version 2.0 (build 7)
 */
public enum CommuniqueFilterType {

    /**
     * Filters out nations <b>not</b> matching the given regex.
     * @since version 2.3 (build 10)
     */
    REQUIRE_REGEX("+regex", true) {
        @Override
        public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                              CommuniqueRecipient provided) {
            String regex = provided.getName();
            Pattern p = Pattern.compile(regex);
            return recipients.stream()
                    .filter(r -> p.matcher(r.getName()).matches()) // if matches, keep
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    },

    /**
     * Filters out nations matching the given regex.
     * @since version 2.3 (build 10)
     */
    EXCLUDE_REGEX("-regex", true) {
        @Override
        public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                              CommuniqueRecipient provided) {
            Pattern p = Pattern.compile(provided.getName()); // make pattern from regex
            return recipients.stream()
                    .filter(r -> !p.matcher(r.getName()).matches()) // if it matches, make false, so exclude
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    },

    // Note that the NORMAL type, because it does not have a prefix, must be kept last in order for parsing.
    /**
     * Provides equivalent functionality to the <code>+</code> command used in NationStates and the <code>-></code>
     * command used in past versions of Communique. Basically, it filter the recipients list to be an intersection of
     * the list and the token provided.
     * @since version 2.0 (build 7)
     */
    INCLUDE("+", false) {
        @Override
        public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                              CommuniqueRecipient provided) {
            // match by names, not by recipient type
            Set<String> set = decomposeToNameSet(provided);
            return recipients.stream()
                    .filter(r -> set.contains(r.getName())) // provided nation-set contains recipient name, keep
                    .collect(Collectors.toCollection(LinkedHashSet::new)); // ordered, remove duplicates
        }
    },

    /**
     * Excludes nations from the recipients list based on the token provided. Provides equivalent functionality as the
     * NationStates "{@code -}" prefix (e.g. {@code -region:Europe}) in telegram queries.
     * @since version 2.0 (build 7)
     */
    EXCLUDE("-", false) {
        @Override
        public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                              CommuniqueRecipient provided) {
            Set<String> set = decomposeToNameSet(provided);
            return recipients.stream()
                    .filter(r -> !set.contains(r.getName())) // provided nation-set contains recipient name, discard
                    .collect(Collectors.toCollection(LinkedHashSet::new)); // ordered, remove duplicates
        }
    },

    /**
     * Adds the provided {@link CommuniqueRecipient} to the recipients list. This should be the default action for all
     * such tokens.
     * <p>This {@code enum} value must be at the bottom or {@link CommuniqueRecipient#parseRecipient} will break.</p>
     * @since version 2.0 (build 7)
     */
    NORMAL("", false) {
        @Override
        public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                              CommuniqueRecipient provided) {
            recipients.addAll(provided.decompose());
            return recipients;
        }
    };

    private final boolean caseSensitive;
    private final String stringRep;

    /**
     * Constructs with given final values
     * @param stringRep     to use for parsing
     * @param caseSensitive whether tag information is case sensitive
     * @since version 3.0 (build 13)
     */
    CommuniqueFilterType(String stringRep, boolean caseSensitive) {
        this.stringRep = stringRep;
        this.caseSensitive = caseSensitive;
    }

    /**
     * Applies the provided <code>CommuniqueRecipient</code> to the provided recipients list. Without a provided
     * <code>enum</code> state, this defaults to {@link CommuniqueFilterType#NORMAL}.
     * @param recipients upon which the token is to be applied
     * @param provided   token
     * @return recipients after the token is applied
     */
    public abstract Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                                   CommuniqueRecipient provided);

    /**
     * Transforms {@link CommuniqueRecipient} its decomposed set, as names.
     * @param recipient to decompose
     * @return decomposed, to name set
     */
    private static Set<String> decomposeToNameSet(CommuniqueRecipient recipient) {
        return recipient.decompose().stream() // turn it into the raw recipients
                .map(CommuniqueRecipient::getName) // get strings for matching
                .collect(Collectors.toCollection(HashSet::new)); // for fast Set#contains()
    }

    /**
     * Returns string as required for {@link CommuniqueRecipient#parseRecipient(String) parser}.
     * @return string representation
     * @since version 2.0 (build 7)
     */
    @Override
    public String toString() {
        return this.stringRep;
    }

    /**
     * Returns whether tag has case sensitive input. Eg {@code +regex:[A-Z].*$} is case-sensitive, while {@code
     * +region:europe} is not. Used in {@link CommuniqueRecipient#CommuniqueRecipient(CommuniqueFilterType,
     * CommuniqueRecipientType, String)}.
     * @return whether tag is case sensitive
     * @since version 3.0 (build 13)
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
}
