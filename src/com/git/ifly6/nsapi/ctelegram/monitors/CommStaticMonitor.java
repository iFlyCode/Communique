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

import com.git.ifly6.communique.io.CommuniqueProcessingAction;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * A static monitor does not update or monitor much of anything. After instantiation, it contains the data it contains
 * without any changes. The contents of the class are immutable.
 * @since version 3.0 (build 13)
 */
public class CommStaticMonitor implements CommMonitor {

    public static final Logger LOGGER = Logger.getLogger(CommStaticMonitor.class.getName());

    private final List<String> allRecipients;
    private final Queue<String> recipients;
    private boolean exhausted = false;

    /**
     * Creates a static monitor.
     * @param recipients to pass to monitor
     * @see #CommStaticMonitor(List, Function)
     */
    public CommStaticMonitor(List<String> recipients) {
        this(recipients, CommuniqueProcessingAction.NONE);
    }

    /**
     * Creates a static monitor.
     * @param recipients to pass to monitor
     * @param function   {@link CommuniqueProcessingAction} to apply
     */
    public CommStaticMonitor(List<String> recipients, Function<List<String>, List<String>> function) {
        allRecipients = recipients;
        this.recipients = new LinkedList<>(function.apply(allRecipients));
    }

    /**
     * {@inheritDoc}. Internal recipients ({@link #recipients}) in a static monitor do not change. Recipients are
     * provided one at a time, wrapped in a single element list.
     */
    @Override
    public List<String> getRecipients() {
        // todo what's better? provide one by one or provide as a single massive list?
        // latter would require setting up a boolean flag asking whether it has been called; if so, exhaust
        String nextRecipient = recipients.poll();
        if (nextRecipient != null) {
            LOGGER.info(String.format("Monitor sending <%s>; %d elements remain", nextRecipient, recipients.size()));
            return Collections.singletonList(nextRecipient);
        }

        this.exhausted = true;
        throw new ExhaustedException(String.format("Static recipients (init with %d) exhausted",
                allRecipients.size()));
    }

    /** Recipients exhaust when all recipients provided by initialiser have been got. */
    @Override
    public boolean recipientsExhausted() {
        return exhausted;
    }

    @Override
    public OptionalLong recipientsCountIfKnown() {
        return OptionalLong.of(recipients.size());
    }
}
