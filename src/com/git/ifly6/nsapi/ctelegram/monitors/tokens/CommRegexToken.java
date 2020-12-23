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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommRegexToken implements CommToken {

    private CommRegexAction action;
    private String pattern;

    public CommRegexToken(CommRegexAction action, String pattern) {
        this.action = action;
        this.pattern = pattern;
    }

    @Override
    public Set<String> apply(Set<String> inputList) {
        return action.apply(inputList, Pattern.compile(pattern));
    }

    public enum CommRegexAction {
        REQUIRE_REGEX {
            @Override
            public Set<String> apply(Set<String> recipients, Pattern p) {
                return recipients.stream()
                        .filter(r -> p.matcher(r).matches()) // if matches, keep
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }

            @Override
            public String toString() {
                return "+regex";
            }
        },

        EXCLUDE_REGEX {
            @Override
            public Set<String> apply(Set<String> recipients, Pattern p) {
                return recipients.stream()
                        .filter(r -> !p.matcher(r).matches()) // if it matches, make false, so exclude
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }

            @Override
            public String toString() {
                return "-regex";
            }
        };

        public abstract Set<String> apply(Set<String> recipients, Pattern p);
    }
}
