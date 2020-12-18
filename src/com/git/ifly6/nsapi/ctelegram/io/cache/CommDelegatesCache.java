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

import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.NSTimeStamped;
import com.git.ifly6.nsapi.NSWorld;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Caches NationStates delegates for later access. <b>Only invoke with {@link #DELEGATE_KEY}!</b> Cache expiration time
 * for this cache is thirty minutes; prefer {@link #getDelegates()}.
 * @since version 3.0 (build 13)
 */
public class CommDelegatesCache extends CommCache<CommDelegatesCache.Delegates> {

    public static final String DELEGATE_KEY = "__delegates__";
    private static CommDelegatesCache instance;

    private CommDelegatesCache(Duration minutes) {
        super(minutes);
    }

    /**
     * Gets the one cache.
     */
    public static CommDelegatesCache getInstance() {
        if (instance == null) instance = new CommDelegatesCache(Duration.ofMinutes(30));
        return instance;
    }

    @Override
    protected Delegates createNewObject(String s) {
        return new Delegates();
    }

    /** Prefer {@link #getDelegates()}. */
    @Override
    public Delegates lookupObject(String s) {
        if (s.equals(DELEGATE_KEY))
            return super.lookupObject(s);
        throw new UnsupportedOperationException(
                "Delegates cache only supports looking up with CommDelegatesCache.DELEGATE_KEY");
    }

    /**
     * Gets cached delegates list.
     * @return cached delegates list.
     * @see Delegates
     */
    public List<String> getDelegates() {
        return lookupObject(DELEGATE_KEY).getDelegates();
    }

    /** Holds a time-stamped version of NationStates delegates. */
    public static class Delegates implements NSTimeStamped {

        private Instant timestamp;
        private List<String> delegates;

        public Delegates() {
            try {
                delegates = NSWorld.getDelegates();
                timestamp = Instant.now();
            } catch (IOException e) {
                throw new NSIOException("Could not get delegates from NationStates", e);
            }
        }

        public List<String> getDelegates() {
            return delegates;
        }

        @Override
        public Instant dataTimestamp() {
            return timestamp;
        }
    }
}
