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

import com.git.ifly6.nsapi.NSException;

import java.util.List;

/**
 * Monitors generate a stream of recipients provided by {@link #getRecipients()} until exhausted {@link
 * #recipientsExhausted()}. If implemented correctly, if a monitor is exhausted, calling {@link #getRecipients()} should
 * throw {@link ExhaustedException}.
 * @since version 3.0 (build 13)
 */
public interface CommMonitor {

    /**
     * Gets recipients.
     * @return list of recipients
     */
    List<String> getRecipients();

    /**
     * Returns boolean indicating whether monitor is exhausted of recipients.
     * @return true if exhausted
     */
    boolean recipientsExhausted();

    /** Thrown if calling an exhausted monitor. */
    class ExhaustedException extends NSException {
        public ExhaustedException(String message) {
            super(message);
        }
    }
}
