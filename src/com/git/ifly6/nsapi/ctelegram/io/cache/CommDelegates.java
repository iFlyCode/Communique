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
import java.time.Instant;
import java.util.List;

/** Holds a time-stamped version of NationStates delegates. */
public class CommDelegates implements NSTimeStamped {

    private Instant timestamp;
    private List<String> delegates;

    public CommDelegates() {
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
