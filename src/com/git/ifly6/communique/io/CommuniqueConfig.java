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

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <code>CommuniqueConfig</code> creates a unified object for the storage and retrieval of configuration information
 * necessary to have persistent states between Communiqué or Marconi instances.
 */
public class CommuniqueConfig implements Serializable {

    // NOTE! For backwards compatibility, do not change field names!
    private static final long serialVersionUID = Communique7Parser.BUILD;

    @SuppressWarnings("unused")
    public static final String HEADER = MessageFormat.format(
            "Communiqué configuration file. Do not edit by hand. Produced at: {0}. Communiqué build {1}",
            CommuniqueUtilities.getDate(), Communique7Parser.BUILD);

    public long version = Communique7Parser.BUILD; // do not change to 'build'

    public JTelegramKeys keys = new JTelegramKeys(); // empty keys
    public JTelegramType telegramType = JTelegramType.NONE; // no telegram type
    public CommuniqueProcessingAction processingAction = CommuniqueProcessingAction.NONE; // no processing action

    public boolean repeats = false;
    public Duration autoStop = null;
    public Duration telegramDelay = null;

    /**
     * Holds {@link CommuniqueRecipient}s as {@link String}s so that it can be edited by hand.
     * <p>To keep this editable by hand, the configuration system uses getters and setters to translate to and from the
     * string state representations to present to the programmer a {@link CommuniqueRecipient} API but actually store
     * everything in strings.</p>
     */
    private ArrayList<String> cRecipients; // must be mutable, use ArrayList

    /** Empty constructor for {@link CommuniqueConfig}. Takes all defaults as given. */
    public CommuniqueConfig() { }

    /**
     * Constructor for {@link CommuniqueConfig}. All the {@code {@link CommuniqueRecipient}}s should be specified after
     * initialisation.
     * @param keys          are the keys
     * @param telegramType  the type of telegrams configured to be sent
     * @param procAction    is the applicable processing action
     * @param autoStop      duration, null if not applicable
     * @param telegramDelay duration, null if not applicable
     */
    public CommuniqueConfig(JTelegramKeys keys, JTelegramType telegramType, CommuniqueProcessingAction procAction,
                            Duration autoStop, Duration telegramDelay) {
        this();
        this.keys = keys;
        this.telegramType = telegramType;
        this.processingAction = procAction;
        this.autoStop = autoStop;
        this.telegramDelay = telegramDelay;
    }

    /**
     * Converts internal {@link #cRecipients} to {@code List<{@link CommuniqueRecipient}>}
     * @return converted string representation
     */
    public List<CommuniqueRecipient> getcRecipients() {
        return cRecipients == null
                ? new ArrayList<>(0) // deal with null case
                : CommuniqueRecipient.parseRecipients(cRecipients);
    }

    /**
     * Returns raw <code>cRecipients</code>, which is <code>List&lt;String&gt;</code>
     * @return <code>cRecipients</code>
     */
    public List<String> getcRecipientsString() {
        return cRecipients == null
                ? new ArrayList<>(Collections.singletonList(""))
                : cRecipients;
    }

    /**
     * Sets {@link #cRecipients} with {@link List} of {@link CommuniqueRecipient}, automatically translates to {@link
     * String} on the fly.
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
     * Checks all the data kept in {@link CommuniqueConfig#cRecipients}, makes they are distinct, cleans them for
     * character errors, acting in place.
     */
    void clean() {
        // proceeds to clean all of the fields
        cRecipients = cRecipients.stream().distinct()
                .map(CommuniqueConfig::cleanNation)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Cleans nation names that could have been prefixed accidentally in a previous version of Communique.
     * @param recipientString is the string representation of the nation
     * @return the same with all the extra '{@code nation:}'s removed.
     */
    private static String cleanNation(String recipientString) {
        return recipientString.replaceAll(":(nation:)+", ":");
    }

    /**
     * Gets processing action.
     * @return processing action, {@link CommuniqueProcessingAction#NONE} if null
     */
    @Nonnull
    public CommuniqueProcessingAction getProcessingAction() {
        return processingAction == null ? CommuniqueProcessingAction.NONE : processingAction;
    }

    /**
     * Gets keys.
     * @return keys, {@link JTelegramKeys} defaults if null
     */
    @Nonnull
    public JTelegramKeys getKeys() {
        return keys == null ? new JTelegramKeys() : keys;
    }

    /**
     * Gets telegram type. If the type is custom, automatically gets the telegram delay and sets custom to that value.
     * If {@link JTelegramType#CUSTOM} is set and {@link #telegramDelay} is null, sets default for {@link
     * JTelegramType#CUSTOM}.
     * @return telegram type, {@link JTelegramType#NONE} if null
     */
    @Nonnull
    public JTelegramType getTelegramType() {
        JTelegramType type = telegramType == null ? JTelegramType.NONE : telegramType;
        if (type == JTelegramType.CUSTOM)
            type.setWaitDuration(getTelegramDelay());

        return type;
    }

    /**
     * Returns {@link Optional} of the autostop value. Auto-stop is a feature to automatically stop polling after the
     * given duration.
     * @return auto-stop value, if null, {@code T+10 years}
     */
    public Optional<Duration> getAutoStop() {
        return Optional.ofNullable(autoStop);
    }

    /**
     * Returns telegram delay; if the delay is null, returns the default for the specified telegram type.
     * @return telegram delay, defaults if null
     */
    @Nonnull
    public Duration getTelegramDelay() {
        return telegramDelay == null ? getTelegramType().getWaitDuration() : telegramDelay;
    }
}
