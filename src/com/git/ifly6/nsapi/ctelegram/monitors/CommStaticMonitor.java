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

import java.util.Collections;
import java.util.List;

/**
 * A static monitor does not update or monitor much of anything. After instantiation, it contains the data it contains
 * without any changes. The contents of the class are immutable.
 */
public class CommStaticMonitor implements CommMonitor {
    private final List<String> recipients;

    /**
     * Creates a static monitor
     * @param recipients to contain
     */
    public CommStaticMonitor(List<String> recipients) {
        this.recipients = Collections.unmodifiableList(recipients);
    }

    /**
     * {@inheritDoc}. Recipients in a static monitor are immutable and do not change.
     */
    @Override
    public List<String> getRecipients() {
        return recipients;
    }
}
