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

import com.git.ifly6.nsapi.NSTimeStamped;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Caches objects which can be timestamped. Also implicitly requires, due to {@link #createNewObject(String)} that the
 * key (a string) be mappable 1:1 to the object.
 * @param <T> extends {@link NSTimeStamped}
 */
public abstract class CommCache<T extends NSTimeStamped> {

    private static final Logger LOGGER = Logger.getLogger(CommCache.class.getName());
    protected static final Duration TEN_MINUTES = Duration.ofMinutes(10);

    private Map<String, T> cache = new HashMap<>();

    /**
     * Supplies a new {@link NSTimeStamped} instance to the cache. Without a supplier, the cache cannot load data.
     * @param s is name of the object
     * @return the new instance
     */
    protected abstract T createNewObject(String s);

    /**
     * Gets object; if not present, caches object.
     * @param s pointing to object
     * @return cached object
     */
    private T getOrCacheObject(String s) {
        if (!cache.containsKey(s)) cacheObject(s);
        return cache.get(s);
    }

    /**
     * Adds an object to the cache.
     * @param s to add to cache
     */
    private void cacheObject(String s) {
        T object = createNewObject(s);
        T oldObject = cache.put(s, object);
        if (object != oldObject) // if they have different REFERENCES, they must be different objects
            LOGGER.fine(String.format("overwrote cache for element <%s>", s));
    }

    /**
     * Gets information for an object. If it does not exist, adds that object to the cache. If the cached information is
     * older than {@link #TEN_MINUTES}, it updates the cache.
     * @param s is the ref name of the object to get data for
     * @return {@link NSTimeStamped} with reasonably up-to-date cached data
     */
    public T lookupObject(String s) {
        T n = getOrCacheObject(s);

        /* If the duration between data acquisition and present is greater than ten minutes, mark expired. */
        boolean expired = Duration.between(n.dataTimestamp(), Instant.now()).compareTo(TEN_MINUTES) > 0;
        if (expired)
            cacheObject(s); // update cache

        return getOrCacheObject(s);
    }
}
