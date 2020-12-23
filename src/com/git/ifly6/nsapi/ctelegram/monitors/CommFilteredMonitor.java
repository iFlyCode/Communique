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

import com.git.ifly6.nsapi.NSNation;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommNationCache;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Wraps a {@link CommMonitor} and filters the recipients thereof with the provided predicates. All predicates should
 * amply use {@link CommNationCache}, {@link com.git.ifly6.nsapi.ctelegram.io.cache.CommRegionCache} and like caches.
 */
public class CommFilteredMonitor implements CommMonitor {

    private final CommMonitor monitor;
    private final List<Predicate<NSNation>> filters;

    public CommFilteredMonitor(CommMonitor monitor, List<Predicate<NSNation>> list) {
        this.monitor = monitor;
        filters = list;
    }

    /**
     * Gets recipients after applying a series of predicates.
     * @return filtered recipients
     */
    @Override
    public List<String> getRecipients() {
        final List<String> monitorRecipients = monitor.getRecipients();
        return monitorRecipients.stream()
                .filter(recipient -> doAllFiltersPass(recipient, filters))
                .collect(Collectors.toList());
    }

    /**
     * @param recipientName to test
     * @param filters       to apply
     * @return true if all filters pass; false otherwise
     */
    private boolean doAllFiltersPass(String recipientName, List<Predicate<NSNation>> filters) {
        NSNation nation = CommNationCache.getInstance().lookupObject(recipientName);
        return nonNull(filters).stream()
                .allMatch(filter -> filter.test(nation));
    }

    /**
     * @param filters to filter of nulls
     * @return non-null filters
     */
    @Nonnull
    private List<Predicate<NSNation>> nonNull(List<Predicate<NSNation>> filters) {
        return filters.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public boolean recipientsExhausted() {
        return monitor.recipientsExhausted();
    }

    @Override
    public OptionalLong remainingIfKnown() {
        return monitor.remainingIfKnown();
    }
}
