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

import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.telegram.JTelegramException;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information about a recipient. It is based on three characteristics, a <code>FilterType</code>, a
 * <code>RecipientType</code>, and the name. The filter type can be used to exclude, include, or simply add. The
 * recipient type can be used to specify multiple recipients, like in a region or in the set of World Assembly
 * delegates. All <code>CommuniqueRecipient</code>s have names which are reference-name safe.
 * @since version 2.0 (build 7)
 * @author ifly6
 */
public class CommuniqueRecipient {

    public static final CommuniqueRecipient DELEGATES =
            new CommuniqueRecipient(FilterType.NORMAL, RecipientType.TAG, "delegates");
    public static final CommuniqueRecipient WA_MEMBERS =
            new CommuniqueRecipient(FilterType.NORMAL, RecipientType.TAG, "wa");

    private FilterType filterType;
    private RecipientType recipientType;
    private String name;
    private String raw;

    /** Creates a <code>CommuniqueRecipient</code> with certain characteristics. */
    public CommuniqueRecipient(FilterType filterType, RecipientType recipientType, String name, String raw) {
        this.filterType = filterType;
        this.recipientType = recipientType;
        this.name = ApiUtils.ref(name);    // convert to reference name
        this.raw = raw;

        // some format checking for the name
        if (name.contains(":")) throw new IllegalArgumentException("nation name [" + name + "] is invalid");
    }

    /**
     * Creates {@link CommuniqueRecipient} with null <code>raw</code> string
     */
    public CommuniqueRecipient(FilterType filterType, RecipientType recipientType, String name) {
        this(filterType, recipientType, name, null);
    }

    /**
     * Returns the name, which, for all elements, will be the reference name format.
     * @return the specific thing which is being requested
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the filter or token, defined in {@link com.git.ifly6.communique.data.FilterType FilterType}.
     * @return the type of filter or token
     */
    public FilterType getFilterType() {
        return filterType;
    }

    /**
     * Returns the type of the recipient, defined in {@link com.git.ifly6.communique.data.RecipientType RecipientType}.
     * @return the type of recipient
     */
    public RecipientType getRecipientType() {
        return recipientType;
    }

    /**
     * @return the original <code>String</code> used to construct this recipient
     */
    public String getRaw() {
        return this.raw;
    }

    /**
     * Returns a string representation of the recipient, in the same form which is used by the NationStates telegram
     * system to specify large numbers of nations. For example, <code>tag:wa</code> or
     * <code>nation:imperium_anglorum</code>.
     */
    @Override
    public String toString() {
        return filterType.toString() + recipientType.toString() + ":" + this.getName();
    }

    /**
     * Decomposes a tag to its constituent nations. All decompositions are done in {@link
     * com.git.ifly6.communique.data.RecipientType RecipientType} class.
     * @return a list of <code>CommuniqueRecipient</code>s
     */
    public List<CommuniqueRecipient> decompose() throws JTelegramException {
        return getRecipientType().decompose(this);
    }

    /**
     * Parses a <code>CommuniqueRecipient</code> of the same form defined in the {@link
     * com.git.ifly6.communique.data.CommuniqueRecipient#toString toString()} method. Allows for fast and simple access
     * between <code>String</code> representations of a recipient and the computer's conception of the object.
     * <p>
     * If a reference name is provided without an accompanying recipient-type declaration, in the form
     * <code>imperium_anglorum</code>, it is assumed that this is a <code>FilterType.NORMAL</code> nation with that
     * name.
     * </p>
     * @return a <code>CommuniqueRecipient</code> representing that string
     */
    public static CommuniqueRecipient parseRecipient(String s) {

        String start = String.valueOf(s); // strings are immutable this is safe
        s = s.trim();

        FilterType fType = FilterType.NORMAL; // default
        for (FilterType type : FilterType.values())
            if (s.startsWith(type.toString())) {
                fType = type;
                s = s.substring(type.toString().length());
                break;
            }

        RecipientType rType = RecipientType.NATION; // default
        for (RecipientType type : RecipientType.values())
            if (s.startsWith(type.toString())) {
                rType = type;
                s = s.substring(type.toString().length());
                break;
            }

        // 2017-03-30 use lastIndexOf to deal with strange name changes, can cause error in name `+region:euro:pe`
        return new CommuniqueRecipient(fType, rType, s.substring(s.lastIndexOf(":") + 1), start);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (filterType == null ? 0 : filterType.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (recipientType == null ? 0 : recipientType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CommuniqueRecipient other = (CommuniqueRecipient) obj;
        if (filterType != other.filterType) return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return recipientType == other.recipientType;
    }

    private static final String RECRUIT_FLAG = "flag:recruit";

    /**
     * The old include flag, which served the purpose of something like the current `+` tag, e.g. `region:Europe,
     * +tag:wa` was a two-part flag on one line.
     * @see CommuniqueRecipient#translateTokens(List)
     */
    private static final String OLD_INCLUDE = "->";

    /**
     * The old exclude flag was badly designed. If used simply, i.e. `--` then it would fail to work with nations that
     * have prefixed hyphens in their names. Instead, here, we use the two hyphens with spaces on both sides, which only
     * partially solves the problem because spaces are allowed in names too. This is only done as a means to lower the
     * number of false positives.
     * @see CommuniqueRecipient#translateTokens(List)
     */
    private static final String OLD_EXCLUDE = " -- "; // must include whitespace on both sides

    /**
     * Translates a number of old tokens into the new Communique 7 tokens.
     * <p>This should translate tokens like the following:</p>
     * <table>
     * <tr>
     * <th>Old tag</th>
     * <th>New tag</th>
     * </tr>
     * <tr>
     * <td><code>region:Europe</code></td>
     * <td><code>region:Europe</code></td>
     * </tr>
     * <tr>
     * <td><code>wa:all</code></td>
     * <td><code>tag:wa</code></td>
     * </tr>
     * <tr>
     * <td><code>wa:delegates</code></td>
     * <td><code>tag:delegates</code></td>
     * </tr>
     * <tr>
     * <td><code>region:Europe -> wa:all</code></td>
     * <td><code>region:Europe, +tag:WA/code></td>
     * </tr>
     * <tr>
     * <td><code>region:Europe -- nation:imperium_anglorum</code></td>
     * <td><code>region:Europe, -nation:imperium_anglorum</code></td>
     * </tr>
     * </table>
     * @param oldTokens to translate
     * @return a list of tokens which means the same thing in the new system
     * @see Communique7Parser
     * @see CommuniqueParser
     */
    public static List<String> translateTokens(List<String> oldTokens) {
        List<String> tokens = new ArrayList<>();
        for (String oldToken : oldTokens) {


            if (oldToken.startsWith(RECRUIT_FLAG)) {
                tokens.add(RECRUIT_FLAG);
                if (oldToken.trim().equalsIgnoreCase(RECRUIT_FLAG)) {
                    // it's a recruit flag with nothing else
                    continue; // next

                } else {
                    // otherwise, there's some other flag buried in here, we need to find it
                    // `flag:recruit` already added, remove it and continue parsing
                    oldToken = oldToken.substring(RECRUIT_FLAG.length()).trim();
                }
            }

            // keep parsing
            if (oldToken.contains(OLD_INCLUDE)) {
                String[] split = oldToken.split(OLD_INCLUDE);
                tokens.add(translateToken(split[0]));
                tokens.add(translateToken(OLD_INCLUDE + split[1]));
                continue;    // to next!
            }

            if (oldToken.contains(OLD_EXCLUDE)) {
                String[] split = oldToken.split(OLD_EXCLUDE);
                if (split.length == 2) {
                    if (ApiUtils.isNotEmpty(split[0]))
                        tokens.add(translateToken(split[0].trim()));
                    if (ApiUtils.isNotEmpty(split[1]))
                        tokens.add(translateToken(OLD_EXCLUDE + split[1].trim()));
                    continue;    // to next!
                }
            }
            tokens.add(translateToken(oldToken));

        }
        return tokens;
    }

    /**
     * Translates a single token from the old system to the new Communique 7 system. This method should not change any
     * Communique 7 tokens and only translate applicable Communique 6 tokens.
     * @param oldToken in a <code>String</code> form, like "wa:delegates"
     * @return the token in the Communique 7 form, which, for "wa:delegates", would turn into "tag:delegates"
     */
    private static String translateToken(String oldToken) {

        // deal with mixed new and old tokens
        if (oldToken.startsWith("tag")) return oldToken;

        // logic tags, somewhat recursive to ease translation of sub-tokens
        // no need to use HashMap, that seems over-engineered for something this simple
        if (oldToken.startsWith("/")) return "-" + translateToken(oldToken.replaceFirst("/", "").trim());
        if (oldToken.startsWith("-- ")) return "-" + translateToken(oldToken.replaceFirst("-- ", "").trim());
        if (oldToken.startsWith("-> ")) return "+" + translateToken(oldToken.replaceFirst("->", "").trim());

        // translate tags which can be decomposed
        if (oldToken.equalsIgnoreCase("wa:delegates")) return "tag:delegates";
        if (oldToken.equalsIgnoreCase("wa:delegate")) return "tag:delegates";
        if (oldToken.equalsIgnoreCase("wa:members") || oldToken.equalsIgnoreCase("wa:nations"))
            return "tag:wa";
        if (oldToken.startsWith("world:new")) return "tag:new";

        // somewhat-direct recipient tags, like region and nation
        if (oldToken.startsWith("region:")) return oldToken;
        return "nation:" + oldToken;

    }

}
