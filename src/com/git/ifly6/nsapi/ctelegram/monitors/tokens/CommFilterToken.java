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

package com.git.ifly6.nsapi.ctelegram.monitors.tokens;

import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CommFilterToken implements CommToken {

    private FilterAction action;
    private CommMonitor monitor;

    public CommFilterToken(FilterAction action, CommMonitor monitor) {
        this.action = action;
        this.monitor = monitor;
    }

    @Override
    public Set<String> apply(Set<String> inputList) {
        return action.apply(inputList, new HashSet<>(monitor.getRecipients()));
    }

    public enum FilterAction {
        /**
         * Elements output are elements in the input which appear in the filter's output set.
         */
        INCLUDE {
            @Override
            public Set<String> apply(Set<String> recipients, Set<String> applyingSet) {
                // match by names, not by recipient type
                return recipients.stream()
                        .filter(applyingSet::contains) // provided nation-set contains recipient name, keep
                        .collect(Collectors.toCollection(LinkedHashSet::new)); // ordered, remove duplicates
            }

            @Override
            public String toString() {
                return "+";
            }
        },

        /**
         * Elements output are elements in the input which do <b>not</b> appear in the filter's output set.
         */
        EXCLUDE {
            @Override
            public Set<String> apply(Set<String> recipients, Set<String> applyingSet) {
                return recipients.stream()
                        .filter(r -> !applyingSet.contains(r)) // provided nation-set contains recipient name, discard
                        .collect(Collectors.toCollection(LinkedHashSet::new)); // ordered, remove duplicates
            }

            @Override
            public String toString() {
                return "-";
            }
        };

        public abstract Set<String> apply(Set<String> recipients, Set<String> applyingSet);
    }
}
