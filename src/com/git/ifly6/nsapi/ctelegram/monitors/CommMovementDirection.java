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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum CommMovementDirection {

    ENTER {
        /** {@inheritDoc} Something that enters present after but not present before. */
        @Override
        public List<String> apply(Set<String> before, Set<String> after) {
            return after.stream()
                    .filter(s -> !before.contains(s))
                    .collect(Collectors.toList());
        }
    },

    EXIT {
        /** {@inheritDoc} Something that exits is present before, but not after. */
        @Override
        public List<String> apply(Set<String> before, Set<String> after) {
            return before.stream()
                    .filter(s -> !after.contains(s))
                    .collect(Collectors.toList());
        }
    };

    /**
     * Applies differencing algorithm based on enumerated description
     * @param before elements present before
     * @param after  elements present after
     * @return appropriate difference
     */
    public abstract List<String> apply(Set<String> before, Set<String> after);
}
