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

import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.ctelegram.io.CommParseException;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommDelegatesCache;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;
import com.git.ifly6.nsapi.telegram.JTelegramException;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Monitors for currently active nations; updates on {@link #ACTIVE_UPDATE_INTERVAL}. Intended for use in contacting
 * nations (probably delegates) who are currently active.
 * @since version 3.0 (build 13)
 */
public class CommActiveMonitor extends CommUpdatingMonitor implements CommMonitor {

    public static final Duration ACTIVE_UPDATE_INTERVAL = Duration.ofMinutes(1);
    private static final String HAPPENINGS_URL = NSConnection.API_PREFIX
            + "q=happenings;filter=law+change+dispatch+rmb+embassy+admin+vote+resolution+member";

    private List<String> activeNations;
    private Mode delegateFilterMode;

    private CommActiveMonitor() {
        super(ACTIVE_UPDATE_INTERVAL);
    }

    public CommActiveMonitor(Mode delegateMode) {
        this();
        this.delegateFilterMode = delegateMode;
    }

    /**
     * Create method with strings for refelction
     * @param delegateMode {@link Mode}
     * @return new monitor
     */
    public CommActiveMonitor create(String delegateMode) {
        Mode mode;
        try {
            mode = Mode.valueOf(delegateMode);
        } catch (IllegalArgumentException e) {
            throw CommParseException.make(delegateMode, Mode.values(), e);
        }

        return new CommActiveMonitor(mode);
    }

    /** {@inheritDoc} Returns recipients which were recently mentioned in the Happenings API only. */
    @Override
    public List<String> getRecipients() {
        Set<String> delegates = new HashSet<>(CommDelegatesCache.getInstance().getDelegates());
        if (delegateFilterMode == Mode.DELEGATES_ONLY)
            return activeNations.stream()
                    .filter(delegates::contains)
                    .collect(Collectors.toList());

        else return activeNations;
    }

    /** Does not exhaust */
    @Override
    public boolean recipientsExhausted() {
        return false;
    }

    @Override
    protected void updateAction() {
        activeNations = getActiveNations();
    }

    /** Gets list of nations appearing in happenings right now. */
    public static List<String> getActiveNations() throws JTelegramException {
        try {
            NSConnection connection = new NSConnection(HAPPENINGS_URL).connect();
            Matcher matcher = Pattern.compile("@@(.*?)@@").matcher(connection.getResponse());

            List<String> matches = new ArrayList<>();
            while (matcher.find())
                matches.add(matcher.group());

            return matches;

        } catch (IOException e) {
            throw new NSIOException("Encountered IO exception when getting active nations", e);
        }
    }

    public enum Mode {
        DELEGATES_ONLY, ALL_NATIONS;
    }
}
