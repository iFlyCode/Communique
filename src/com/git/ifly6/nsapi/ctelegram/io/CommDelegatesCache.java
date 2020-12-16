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

import java.time.Duration;
import java.util.List;

/** Caches NationStates delegates for later access. <b>Only invoke with {@link #DELEGATE_KEY}!</b> */
public class CommDelegatesCache extends CommCache<CommDelegates> {

    public static final String DELEGATE_KEY = "__delegates__";
    private static CommDelegatesCache instance;

    private CommDelegatesCache(Duration minutes) {
        super(minutes);
    }

    /**
     * Cache expiration time for this cache is thirty minutes. {@link #lookupObject(String)} should only be invoked with
     * {@link #DELEGATE_KEY}.
     */
    public static CommDelegatesCache getInstance() {
        if (instance == null) instance = new CommDelegatesCache(Duration.ofMinutes(30));
        return instance;
    }

    @Override
    protected CommDelegates createNewObject(String s) {
        return new CommDelegates();
    }

    /** Prefer {@link #getDelegates()}. */
    @Override
    public CommDelegates lookupObject(String s) {
        if (s.equals(DELEGATE_KEY))
            return super.lookupObject(s);
        throw new UnsupportedOperationException(
                "Delegates cache only supports looking up with CommDelegatesCache.DELEGATE_KEY");
    }

    /**
     * Gets cached delegates list.
     * @return cached delegates list.
     * @see CommDelegates
     */
    public List<String> getDelegates() {
        return lookupObject(DELEGATE_KEY).getDelegates();
    }

}
