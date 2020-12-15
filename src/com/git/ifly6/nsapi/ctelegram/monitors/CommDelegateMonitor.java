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

import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.NSWorld;

import java.io.IOException;
import java.util.List;

/**
 * Provides up-to-date view of the list of NationStates delegates. Updates every 30 minutes, per {@link
 * #DELEGATE_UPDATE_INTERVAL}.
 */
public class CommDelegateMonitor extends CommUpdatingMonitor implements CommMonitor {

    public static final int DELEGATE_UPDATE_INTERVAL = 30 * 60 * 1000;

    private int repeatedFailures = 0;
    private List<String> delegates;

    public CommDelegateMonitor() {
        super();
        setUpdateInterval(DELEGATE_UPDATE_INTERVAL);
        start();
    }

    @Override
    protected void updateAction() {
        try {
            delegates = NSWorld.getDelegates();
            repeatedFailures = 0;
        } catch (IOException e) {
            repeatedFailures++;
            if (repeatedFailures > 10)
                throw new NSIOException("failed to get delegates", e);
        }
    }

    /**
     * {@inheritDoc} Provides an up-to-date list of NationStates delegates.
     */
    @Override
    public List<String> getRecipients() {
        return delegates;
    }
}
