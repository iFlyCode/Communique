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

package com.git.ifly6.nsapi.ctelegram.monitors.reflected;

import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommDelegatesCache;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

/**
 * Provides up-to-date view of the list of NationStates delegates. Updates every 30 minutes, per {@link
 * #DELEGATE_UPDATE_INTERVAL}. Monitor is meant to get delegates (and also to new delegates elected after
 * initialisation) for approvals. Exhausts after 36 hours.
 * @since version 3.0 (build 13)
 */
public class CommDelegateMonitor extends CommUpdatingMonitor implements CommMonitor {

    public static final Duration DELEGATE_UPDATE_INTERVAL = Duration.ofMinutes(30);
    private static CommDelegateMonitor instance;

    private static final Instant startTime = Instant.now();

    private int repeatedFailures = 0;
    private List<String> delegates;

    private CommDelegateMonitor() { super(DELEGATE_UPDATE_INTERVAL); }

    /** One monitor for one set of delegates. */
    public static CommDelegateMonitor getInstance() {
        if (instance == null) instance = new CommDelegateMonitor();
        return instance;
    }

    @Override
    protected void updateAction() {
        try {
            delegates = CommDelegatesCache.getInstance().getDelegates();
            Collections.shuffle(delegates); // randomise order
            repeatedFailures = 0;
        } catch (NSIOException e) {
            repeatedFailures++;
            if (repeatedFailures > 5)
                throw new NSIOException("Failed to get delegates more than five times!", e);
        }
    }

    /**
     * {@inheritDoc} Provides an up-to-date list of NationStates delegates.
     */
    @Override
    public List<String> getRecipients() {
        return delegates;
    }

    /** Monitor exhausts after one and a half days (36 hours). */
    @Override
    public boolean recipientsExhausted() {
        return startTime.plus(36, ChronoUnit.HOURS).isAfter(Instant.now());
    }
}
