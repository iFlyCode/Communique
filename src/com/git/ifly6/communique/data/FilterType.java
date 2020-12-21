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
public enum FilterType {

    REQUIRE_REGEX {
        @Override
        public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                              CommuniqueRecipient provided) {
            String regex = provided.getName();
            Pattern p = Pattern.compile(regex);
            return recipients.stream()
                    .filter(r -> p.matcher(r.getName()).matches()) // if matches, keep
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public String toString() {
            return "+regex";
        }
    },

    EXCLUDE_REGEX {
        @Override
        public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                              CommuniqueRecipient provided) {
            Pattern p = Pattern.compile(provided.getName()); // make pattern from regex
            return recipients.stream()
                    .filter(r -> !p.matcher(r.getName()).matches()) // if it matches, make false, so exclude
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public String toString() {
            return "-regex";
        }
    },

    // Note that the NORMAL type, because it does not have a prefix, must be kept last in order for parsing.
    /**
     * Provides equivalent functionality to the <code>+</code> command used in NationStates and the <code>-></code>
     * command used in past versions of Communique. Basically, it filter the recipients list to be an intersection of
     * the list and the token provided.
     */
    INCLUDE {
        @Override
        public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                              CommuniqueRecipient provided) {
            // match by names, not by recipient type
            Set<String> set = toSetDecompose(provided);
            return recipients.stream()
                    .filter(r -> set.contains(r.getName())) // provided nation-set contains recipient name, keep
                    .collect(Collectors.toCollection(LinkedHashSet::new)); // ordered, remove duplicates
        }

        @Override
        public String toString() {
            return "+";
        }
    },

    /**
     * Excludes nations from the recipients list based on the token provided. Provides equivalent functionality as the
     * NationStates "<code>-</code>" command (e.g. <code>-region:Europe</code>) in telegram queries.
     */
    EXCLUDE {
        @Override
        public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                              CommuniqueRecipient provided) {
            Set<String> set = toSetDecompose(provided);
            return recipients.stream()
                    .filter(r -> !set.contains(r.getName())) // provided nation-set contains recipient name, discard
                    .collect(Collectors.toCollection(LinkedHashSet::new)); // ordered, remove duplicates
        }

        @Override
        public String toString() {
            return "-";
        }
    },

    /**
     * Adds the provided <code>CommuniqueRecipient</code> to the end of the recipients list. This is the default action
     * for <code>CommuniqueRecipient</code> tokens, unless they are declared otherwise.
     * <p>
     * Please note that this portion of the <code>enum</code> should be kept at the bottom of the class, or otherwise,
     * {@link CommuniqueRecipient#parseRecipient} will break.
     * </p>
     */
    NORMAL {
        @Override
        public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                              CommuniqueRecipient provided) {
            recipients.addAll(provided.decompose());
            return recipients;
        }

        @Override
        public String toString() {
            return "";
        }
    };

    /**
     * Applies the provided <code>CommuniqueRecipient</code> to the provided recipients list. Without a provided
     * <code>enum</code> state, this defaults to {@link FilterType#NORMAL}.
     * @param recipients upon which the token is to be applied
     * @param provided   token
     * @return recipients after the token is applied
     */
    public abstract Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
                                                   CommuniqueRecipient provided);

    private static Set<String> toSetDecompose(CommuniqueRecipient recipient) {
        return recipient
                .decompose().stream() // turn it into the raw recipients
                .map(CommuniqueRecipient::getName) // get strings for matching
                .collect(Collectors.toCollection(HashSet::new)); // for fast Set#contains()
    }
}
