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
 * Caches NationStates delegates for later access. <b>Only invoke with {@link #MEMBERS_KEY}!</b> Cache expiration time
 * for this cache is thirty minutes; prefer {@link #getWAMembers()}.
 * @since version 3.0 (build 13)
 */
public class CommMembersCache extends CommCache<CommMembersCache.Members> {

    public static final String MEMBERS_KEY = "__members__";
    private static CommMembersCache instance;

    private CommMembersCache(Duration minutes) {
        super(minutes);
    }

    /** Gets the one cache. */
    public static CommMembersCache getInstance() {
        if (instance == null) instance = new CommMembersCache(Duration.ofMinutes(30));
        return instance;
    }

    @Override
    protected Members createNewObject(String s) {
        return new Members();
    }

    /** Prefer {@link #getWAMembers()}. */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Override
    @Deprecated
    public Members lookupObject(String s) {
        if (s.equals(MEMBERS_KEY))
            return super.lookupObject(s);
        throw new UnsupportedOperationException(
                "Members cache only supports looking up with " + MEMBERS_KEY);
    }

    /**
     * Gets cached delegates list.
     * @return cached delegates list.
     * @see Members
     */
    public List<String> getWAMembers() {
        return lookupObject(MEMBERS_KEY).getMembers();
    }

    /** Holds a time-stamped version of NationStates delegates. */
    public static class Members implements NSTimeStamped {

        private Instant timestamp;
        private List<String> members;

        public Members() {
            try {
                members = NSWorld.getWAMembers();
                timestamp = Instant.now();
            } catch (IOException e) {
                throw new NSIOException("Could not get delegates from NationStates", e);
            }
        }

        public List<String> getMembers() {
            return members;
        }

        @Override
        public Instant timestamp() {
            return timestamp;
        }
    }
}
