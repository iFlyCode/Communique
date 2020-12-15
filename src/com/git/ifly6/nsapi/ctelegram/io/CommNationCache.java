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

package com.git.ifly6.nsapi.ctelegram.io;

import com.git.ifly6.nsapi.NSNation;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Monitors are likely to encounter the same nation over and over again. This is especially the case if applying complex
 * filters that require lots of information. Caching nation level information with an expiration of {@link #TEN_MINUTES}
 * should greatly lower the number of API calls, improving performance.
 */
public class CommNationCache {

    private static final Logger LOGGER = Logger.getLogger(CommNationCache.class.getName());

    private static CommNationCache instance;
    private static final int TEN_MINUTES = 10 * 60;

    private Map<String, NSNation> cache = new HashMap<>();

    /** Only one cache. */
    private CommNationCache() {}

    /** @return instance of {@code CommNationCache}. */
    public static CommNationCache getInstance() {
        if (instance == null) instance = new CommNationCache();
        return instance;
    }

    /**
     * Gets nation; if not present, caches nation.
     * @param s pointing to nation
     * @return cached nation
     */
    private NSNation getOrCacheNation(String s) {
        if (!cache.containsKey(s)) cacheNation(s);
        return cache.get(s);
    }

    /**
     * Adds a nation to the cache.
     * @param s to add to cache
     */
    private void cacheNation(String s) {
        NSNation n = new NSNation(s).populateData();
        NSNation oldNation = cache.put(s, n);
        if (n != oldNation) // if they have different REFERENCES, they must be different objects
            LOGGER.fine(String.format("overwrote nation cache for element <%s>", s));
    }

    /**
     * Gets information for a nation. If it does not exist, adds that nation to the cache. If the cached information is
     * older than {@link #TEN_MINUTES}, it updates the cache.
     * @param s to get data for
     * @return {@link NSNation} with reasonably up-to-date cached data
     */
    public NSNation getNation(String s) {
        NSNation n = getOrCacheNation(s);
        Instant minutesAgo = Instant.now().minusSeconds(TEN_MINUTES);

        /* If the timestamp is before the stamp minutes ago, then it is older. Correspondingly, if true, the cached
         * information has expired. */
        boolean expired = n.dataTimestamp().isBefore(minutesAgo);
        if (expired)
            cacheNation(s); // update cache

        return getOrCacheNation(s);
    }
}
