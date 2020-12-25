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

package com.git.ifly6.communique.data;

import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;

import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Packages the recipients into a single for reproducibility.
 * @since version 3.0 (build 13)
 */
public class Communique7Monitor implements CommMonitor {

    private static final int TRUNCATION_LIMIT = 10;

    private State state;
    final private CommuniqueConfig theConfig;
    final private boolean repeats;

    private Set<String> alreadyPassed = new HashSet<>();
    private List<String> lastPassed;

    public Communique7Monitor(CommuniqueConfig communiqueConfig) {
        this.theConfig = communiqueConfig;
        this.repeats = communiqueConfig.repeats;
        this.state = State.INIT;
    }

    /**
     * {@inheritDoc} All recipients are those defined by the appropriate input of {@link CommuniqueRecipient}s. It only
     * passes a given recipient once. Moreover, this method passes <b>all</b> parsed recipients immediately.
     */
    public List<String> getRecipients() {
        if (this.state == State.RUNNING && !repeats) // we've already passed recipients and we do not repeat
            throw new ExhaustedException("Recipients exhausted!");

        List<String> recipients = (this.state == State.INIT && lastPassed != null)
                ? lastPassed // if we are initialising AND cached the data already
                : parseRecipients();

        this.state = State.RUNNING;
        return applyTruncation(recipients, true);
    }

    /**
     * Peeks at the recipients to be passed. Data is cached.
     * @return recipients to be passed
     * @throws UnsupportedOperationException if called after initialisation
     * @see #getRecipients()
     */
    public List<String> peek() {
        if (this.state == State.INIT) {
            return applyTruncation(parseRecipients(), false);

        } else throw new UnsupportedOperationException("Can only call peek during monitor initialisation!");
    }

    /**
     * Applies truncation if {@link CommuniqueConfig#repeats} is true; adds to passed cache if desired.
     * @param recipients     to deal with
     * @param addingToPassed if true, adds to the passed cache
     * @return truncated recipients if requested
     */
    private List<String> applyTruncation(List<String> recipients, boolean addingToPassed) {
        List<String> result = repeats
                ? recipients.stream().limit(TRUNCATION_LIMIT).collect(Collectors.toList())
                : recipients;
        if (addingToPassed)
            alreadyPassed.addAll(result);

        return result;
    }

    /**
     * Parses recipients based on the current configuration setting. Caches this information in {@link #lastPassed}.
     * @return parsed recipients
     */
    private List<String> parseRecipients() {
        List<String> rawParseOutput = new Communique7Parser().apply(theConfig.getcRecipients()).listRecipients();
        List<String> intermediateList = rawParseOutput.stream()
                .filter(s -> !alreadyPassed.contains(s))
                .collect(Collectors.toList());
        this.lastPassed = theConfig.getProcessingAction().apply(intermediateList);
        return this.lastPassed;
    }

    /** @return false if {@link #repeats}; true otherwise. */
    @Override
    public boolean recipientsExhausted() {
        return this.state == State.RUNNING && !repeats;
    }

    @Override
    public OptionalLong remainingIfKnown() {
        return repeats
                ? OptionalLong.empty()
                : OptionalLong.of(lastPassed.size());
    }

    /** Expresses the state – {@link #INIT} or {@link #RUNNING} – of the monitor. */
    private enum State {
        INIT, RUNNING
    }
}
