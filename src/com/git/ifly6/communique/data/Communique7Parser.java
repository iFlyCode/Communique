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

import com.git.ifly6.nsapi.telegram.JTelegramException;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <code>Communique7Parser</code> is the new parser designed for Communique 7, which implements the same way to declare
 * recipients as used in NationStates. It supersedes the old parser, {@code CommuniqueParser}, which used the custom
 * recipient declaration system in older builds of Communique.
 * <p><code>Communique7Parser</code> also provides methods to translate between the old and new Communique address
 * tokens, allowing for a seamless transition between the old and new token systems.</p>
 * <p>This class does not lazily load data. When invoking <code>apply</code>, all elements are processed
 * immediately. This class is meant to be used fluently, e.g.</p>
 * <pre>new Communique7Parser().apply(tokens).listRecipients()</pre>
 * @since version 2 (build 7)
 * @author ifly6
 */
public class Communique7Parser {

    /**
     * Declares parser build, which is based on two values: (1) the syntax of the Communique recipients
      language and (2) the file syntax in which that information is held.
     */
    public static final long BUILD = 13;

    /** Declares semantic version. */
    public static final String VERSION = "3.0";

    /** List of recipients changed by various actions and applications called by the parser. */
    private Set<CommuniqueRecipient> recipients;

    /**
     * Creates a new empty parser without any applied tokens. To actually use the parser, apply tokens using the apply
     * methods, either in the form of a <code>List&lt;String&gt;</code> or any number of
     * <code>CommuniqueRecipient</code>.
     */
    public Communique7Parser() {
        recipients = new LinkedHashSet<>();
    }

    /**
     * Applies the tokens, specified in the <code>CommuniqueRecipient</code> object, to the recipients list in the
     * parser.
     * @param token a <code>CommuniqueRecipient</code>
     * @return this parser
     */
    public Communique7Parser apply(CommuniqueRecipient token) throws JTelegramException {
        recipients = token.getFilterType().apply(recipients, token);
        /* This is the beautiful part, because I've chained everything to a filter, this means that I don't have to
         * write any code whatsoever to sort things into what they have to do, unlike the old parser. Now, everything is
         * chained to an ENUM which already knows exactly what it has to do, and therefore, everything is already dealt
         * with. */
        return this;
    }

    /**
     * Applies the tokens to the recipients list with a specified list of tokens.
     * @param list of <code>CommuniqueRecipient</code>s
     * @return this parser
     */
    public Communique7Parser apply(List<CommuniqueRecipient> list) throws JTelegramException {
        list.forEach(this::apply);
        return this;
    }

    /**
     * Applies tokens based on a variable number of <code>CommuniqueRecipient</code>s.
     * @param tokens to apply
     * @return this parser
     */
    public Communique7Parser apply(CommuniqueRecipient... tokens) {
        Arrays.stream(tokens).forEach(this::apply);
        return this;
    }

    /**
     * Returns a list of all the recipients in standard NationStates reference name form
     * @return list of recipients
     */
    public List<String> listRecipients() {
        return recipients.stream()
                .map(CommuniqueRecipient::getName)
                .collect(Collectors.toList());
    }

}
