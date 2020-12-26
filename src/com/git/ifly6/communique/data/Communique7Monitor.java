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
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Packages the recipients into a single for reproducibility.
 * @since version 3.0 (build 13)
 */
public class Communique7Monitor implements CommMonitor {

    private static final Logger LOGGER = Logger.getLogger(Communique7Monitor.class.getName());
    private static final int TRUNCATION_LIMIT = 6; // every 3 minutes

    private State state;
    final private CommuniqueConfig theConfig;

    private Set<String> alreadyPassed = new HashSet<>();
    private List<String> lastParseResults;

    public Communique7Monitor(CommuniqueConfig communiqueConfig) {
        this.theConfig = communiqueConfig;
        this.state = State.INIT;
    }

    /**
     * {@inheritDoc} All recipients are those defined by the appropriate input of {@link CommuniqueRecipient}s. It only
     * passes a given recipient once. If {@link CommuniqueConfig#repeats} is true, it will truncate to providing the
     * first {@value #TRUNCATION_LIMIT} recipients.
     * <p>If {@link #peek()} is called before this method is first run – ie we are still initialising but {@link
     * #lastParseResults} is not null – it will use the cached last parse results and filter them.</p>
     */
    public List<String> getRecipients() {
        if (recipientsExhausted()) // we've already passed recipients and we do not repeat
            throw new ExhaustedException("Recipients exhausted!");

        List<String> recipients = this.state == State.INIT && lastParseResults != null
                ? preview() // if we are initialising AND cached the data already, use previewed data
                : filtered(parseRecipients()); // otherwise generate for yourself

        this.state = State.RUNNING;
        List<String> result = theConfig.repeats
                ? recipients.stream().limit(TRUNCATION_LIMIT).collect(Collectors.toList())
                : recipients;
        alreadyPassed.addAll(result);

        return result;
    }

    /**
     * Peeks at the last parse results; if there are none, creates them.
     * @return last parse results
     * @throws UnsupportedOperationException if called outside of initialisation
     * @see #getRecipients()
     * @see #parseRecipients()
     */
    public List<String> peek() {
        if (state != State.INIT) throw new UnsupportedOperationException("Cannot peek outside of initialisation");
        if (lastParseResults == null) parseRecipients();
        return lastParseResults;
    }

    /**
     * Previews recipients the monitor expects to provide. If not loaded, loads them.
     * @return {@link #peek()}, but after applying {@link #filtered(List)}
     * @throws UnsupportedOperationException from {@link #peek()}
     * @see #getRecipients()
     */
    public List<String> preview() {
        return filtered(peek());
    }

    /**
     * Creates new provided list, with elements in that list which are contained by {@link #alreadyPassed} removed.
     * @param list to filter
     * @return filtered list
     */
    private List<String> filtered(List<String> list) {
        return list.stream()
                .filter(s -> !alreadyPassed.contains(s))
                .collect(Collectors.toList());
    }

    /**
     * Parses recipients based on the current configuration setting. Caches this information in {@link
     * #lastParseResults}. Automatically filters out elements already passed.
     * @return parsed recipients
     */
    private List<String> parseRecipients() {
        this.lastParseResults = new Communique7Parser().apply(theConfig.getcRecipients()).listRecipients();
        LOGGER.finer(String.format("Monitor parsed %d recipients", lastParseResults.size()));
        return this.lastParseResults;
    }

    /** @return false if {@link CommuniqueConfig#repeats}; true otherwise. */
    @Override
    public boolean recipientsExhausted() {
        return this.state == State.RUNNING && !theConfig.repeats;
    }

    /**
     * {@inheritDoc} For Communique7Monitor, returns the size of {@link #lastParseResults}, ie all known possible
     * recipients. This number can change over time if repeating.
     * @return estimated number of remaining recipients
     */
    @Override
    public OptionalLong recipientsCountIfKnown() {
        return OptionalLong.of(lastParseResults.size());
    }

    /** Expresses the state – {@link #INIT} or {@link #RUNNING} – of the monitor. */
    public enum State {
        INIT, RUNNING
    }
}
