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

package com.git.ifly6.nsapi.ctelegram.io.cache;

import com.git.ifly6.nsapi.NSTimeStamped;
import com.git.ifly6.nsapi.NSWorld;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

/**
 * Caches region tags.
 */
public class CommRegionTagCache extends CommCache<CommRegionTagCache.CommRegionTag> {

    public static final Logger LOGGER = Logger.getLogger(CommRegionTagCache.class.getName());
    private static final Duration REGION_TAG_CACHE_DURATION = Duration.ofMinutes(60);

    private static CommRegionTagCache instance;

    private CommRegionTagCache() { super(REGION_TAG_CACHE_DURATION); }

    public static CommRegionTagCache getInstance() {
        if (instance == null) instance = new CommRegionTagCache();
        return instance;
    }

    @Override
    protected CommRegionTag createNewObject(String s) {
        return new CommRegionTag(s);
    }

    /**
     * {@inheritDoc}
     * @see #getRegionsWithTag(String)
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    @Override
    public CommRegionTag lookupObject(String s) {
        return super.lookupObject(s);
    }

    public List<String> getRegionsWithTag(String name) {
        return this.lookupObject(name).regionsWithTag;
    }

    public static class CommRegionTag implements NSTimeStamped {
        private final Instant timestamp = Instant.now();
        private List<String> regionsWithTag;

        public CommRegionTag(String s) {
            try {
                regionsWithTag = NSWorld.getRegionTag(s);
            } catch (IOException e) { LOGGER.warning("Could not get region tags from NationStates!"); }
        }

        @Override
        public Instant timestamp() {
            return timestamp;
        }
    }

}
