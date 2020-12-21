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

package com.git.ifly6.communique.io;

import com.git.ifly6.commons.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramType;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <code>CommuniqueConfig</code> creates a unified object for the storage and retrieval of configuration information
 * necessary to have persistent states between Communiqué or Marconi instances.
 */
public class CommuniqueConfig implements java.io.Serializable {

    // NOTE! For backwards compatibility, do not change field names!
    private static final long serialVersionUID = Communique7Parser.BUILD;

    @SuppressWarnings("unused")
    public static final String HEADER = MessageFormat.format(
            "Communiqué configuration file. Do not edit by hand. Produced at: {0}. Communiqué build {1}",
            CommuniqueUtilities.getDate(), Communique7Parser.BUILD);

    public long version; // do not change to 'build'

    public CommuniqueProcessingAction processingAction;
    public JTelegramKeys keys;
    public JTelegramType telegramType;

    /**
     * Holds {@link CommuniqueRecipient}s as {@link String}s so that it can be edited by hand.
     * <p>To keep this editable by hand, the configuration system uses getters and setters to translate to and from the
     * string state representations to present to the programmer a {@link CommuniqueRecipient} API but actually store
     * everything in strings.</p>
     */
    private ArrayList<String> cRecipients = new ArrayList<>(); // must be mutable, use ArrayList

    /**
     * Empty constructor for {@link CommuniqueConfig}
     */
    public CommuniqueConfig() {
        this.keys = new JTelegramKeys(); // empty keys
        this.version = Communique7Parser.BUILD; // default version to current version
        this.processingAction = CommuniqueProcessingAction.NONE; // no processing action
        this.telegramType = JTelegramType.NONE; // no telegram type
    }

    /**
     * Constructor for <code>{@link CommuniqueConfig}</code>s. All the
     * <code>{@link CommuniqueRecipient}</code>s should be specified after the fact.
     * @param t          the type of telegrams configured to be sent
     * @param procAction is the applicable processing action
     * @param keys       are the keys
     */
    public CommuniqueConfig(JTelegramType t, CommuniqueProcessingAction procAction,
                            JTelegramKeys keys) {
        this();
        this.telegramType = t;
        this.processingAction = procAction;
        this.keys = keys;
    }

    /**
     * Converts internal {@link #cRecipients} to {@code List<{@link CommuniqueRecipient}>}
     * @return converted string representation
     */
    public List<CommuniqueRecipient> getcRecipients() {
        if (cRecipients == null) return new ArrayList<>(0); // deal with null case

        // use imperative for speed
        List<CommuniqueRecipient> list = new ArrayList<>(cRecipients.size());
        for (String s : cRecipients)
            list.add(CommuniqueRecipient.parseRecipient(s));
        return list;
    }

    /**
     * Returns raw <code>cRecipients</code>, which is <code>List&lt;String&gt;</code>
     * @return <code>cRecipients</code>
     */
    public List<String> getcRecipientsString() {
        return cRecipients;
    }

    /**
     * Sets <code>cRecipients</code> with <code>List&lt;CommuniqueRecipient&gt;</code>, translates to
     * <code>String</code> on the fly.
     * @param crs {@link CommuniqueRecipient}s to set
     */
    public void setcRecipients(List<CommuniqueRecipient> crs) {
        // NOTE: No setcRecipients(List<String> crs) because need for verification
        // use imperative for speed
        ArrayList<String> list = new ArrayList<>(crs.size());
        for (CommuniqueRecipient cr : crs)
            list.add(cr.toString());
        cRecipients = list;
    }

    public void addcRecipient(CommuniqueRecipient cr) {
        cRecipients.add(cr.toString());
    }

    /**
     * Gets processing action
     * @return processing action, {@link CommuniqueProcessingAction#NONE} if null
     */
    public CommuniqueProcessingAction getProcessingAction() {
        return processingAction == null ? CommuniqueProcessingAction.NONE : processingAction;
    }

    /**
     * Gets telegram type
     * @return telegram type, {@link JTelegramType#NONE} if null
     */
    public JTelegramType getTelegramType() {
        return telegramType == null ? JTelegramType.NONE : telegramType;
    }

    /**
     * Checks all the data kept in {@link CommuniqueConfig#cRecipients} and makes they are distinct and applicable to
     * save to the program. For backward compatibility, it also applies these changes to the old <code>recipients</code>
     * and the <code>sentList</code>. It also updates the <code>CommuniqueConfig</code> version <i>field</i>, not the
     * one in the header, to the version of the program on which it was saved.
     */
    void clean() {
        // proceeds to clean all of the fields
        cRecipients = cRecipients.stream().distinct()
                .map(CommuniqueConfig::cleanNation)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Cleans nation names that could have been prefixed accidentally in a previous version of Communique
     * @param recipientString is the string-name of the nation
     * @return the same with all the extra 'nation:'s removed.
     */
    private static String cleanNation(String recipientString) {
        return recipientString.replaceAll(":(nation:)+", ":");
    }

}
