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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is the new parser designed for Communique build 7, which implements the same way to declare recipients as used
 * in NationStates. It supersedes the old parser, {@code CommuniqueParser} (now removed), which used the custom
 * recipient declaration system in older builds of Communique. See also {@link CommuniqueRecipient#translateTokens(List)}.
 * <p>This class does <b>not</b> lazily load data. When invoking {@link #apply(CommuniqueRecipient)} or {@link
 * #apply(List)}, all elements are processed immediately. This class is meant to be used fluently, e.g.</p>
 * <pre>new Communique7Parser().apply(tokens).listRecipients()</pre>
 * @author ifly6
 * @since version 2.0 (build 7)
 */
public class Communique7Parser {

    /**
     * Declares parser build, which is based on two values: (1) the syntax of the Communique recipients language and (2)
     * the file syntax in which that information is held.
     * @since version 1.1 (build 2)
     */
    public static final long BUILD = 13;

    /**
     * Declares semantic version.
     * @since version 3.0 (build 13)
     */
    public static final String VERSION = "3.0";

    /** List of recipients changed by various actions and applications called by the parser. */
    private Set<CommuniqueRecipient> recipients = new LinkedHashSet<>();

    /**
     * Creates a new empty parser without any applied tokens. To actually use the parser, apply tokens using the apply
     * methods, either in the form of a {@code List<String>} or any number of {@link CommuniqueRecipient}.
     */
    public Communique7Parser() { }

    /**
     * Applies the tokens, specified in the <code>CommuniqueRecipient</code> object, to the recipients list in the
     * parser.
     * <p>Because everything has a filter, there is no sorting code necessary to tell everything what to do, it simply
     * does it on its own accord, unlike the old parser. This greatly simplifies the linking logic.</p>
     * @param cRecipient a {@link CommuniqueRecipient}
     * @return this parser
     */
    public Communique7Parser apply(CommuniqueRecipient cRecipient) throws JTelegramException {
        recipients = cRecipient.getFilterType().apply(recipients, cRecipient);
        return this;
    }

    /**
     * Applies the tokens to the recipients list with a specified list of tokens.
     * @param list of {@link CommuniqueRecipient}
     * @return this parser
     */
    public Communique7Parser apply(List<CommuniqueRecipient> list) throws JTelegramException {
        list.forEach(this::apply);
        return this;
    }

    /**
     * Returns a list of all the recipients in standard NationStates reference name form.
     * @return list of recipients
     */
    public List<String> listRecipients() {
        return recipients.stream()
                .map(CommuniqueRecipient::getName)
                .collect(Collectors.toList());
    }
}
