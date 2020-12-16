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

package com.git.ifly6.marconi;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.ngui.AbstractCommunique;
import com.git.ifly6.nsapi.telegram.JTelegramLogger;
import com.git.ifly6.nsapi.telegram.JavaTelegram;

import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Marconi extends AbstractCommunique implements JTelegramLogger {

    private static final Logger LOGGER = Logger.getLogger(Marconi.class.getName());
    private static FileHandler handler;

    private JavaTelegram client = new JavaTelegram(this);
    private CommuniqueConfig config;

    private boolean skipChecks;
    private boolean recruiting;

    public Marconi(boolean recruiting) {
        this.recruiting = recruiting;
    }

    public void send() {

        // Process the Recipients list into a string with two columns.
        Communique7Parser parser = new Communique7Parser();
        List<String> expandedRecipients = parser.apply(config.getcRecipients()).listRecipients();

        // Apply processing action
        expandedRecipients = config.processingAction.apply(expandedRecipients);

        // Show the recipients in the order we are to send the telegrams.
        System.out.println();
        for (int x = 0; x < expandedRecipients.size(); x = x + 2)
            try {
                System.out.printf("%-30.30s  %-30.30s%n", expandedRecipients.get(x), expandedRecipients.get(x + 1));
            } catch (IndexOutOfBoundsException e) {
                System.out.print(expandedRecipients.get(x) + "\n");
            }

        System.out.println();
        //noinspection IntegerDivisionInFloatingPointContext
        System.out.printf("This will take %s to send %d telegrams%n",
                CommuniqueUtilities.time(Math.round(expandedRecipients.size() * (config.telegramType.getWaitTime() / 1000))),
                expandedRecipients.size());

        if (!skipChecks) {
            // Give a chance to check the recipients.
            String recipientsReponse = MarconiUtilities
                    .promptYN("Are you sure you want to send to these recipients? [Yes] or [No]?");
            if (recipientsReponse.startsWith("n")) System.exit(0);
        }

        // Set the client up and go.
        client.setKeys(config.keys);
        client.setTelegramType(config.telegramType);
        client.setRecipients(expandedRecipients);

        // Check for file lock
        if (!MarconiUtilities.isFileLocked()) client.connect();
        else throw new RuntimeException("Cannot send, as another instance of Marconi is already sending.");

    }

    /**
     * Note that this will not return what is loaded. It will return a sentList whose duplicates have been removed and,
     * if any elements start with a negation <code>/</code>, it will remove it.
     * @see com.git.ifly6.communique.ngui.AbstractCommunique#exportState()
     */
    @Override
    public CommuniqueConfig exportState() {
        // Remove duplicates from the sentList as part of save action
        config.setcRecipients(config.getcRecipients().stream()
                .distinct()
                .collect(Collectors.toList()));
        return config;

    }

    /** @see com.git.ifly6.communique.ngui.AbstractCommunique#importState(com.git.ifly6.communique.io.CommuniqueConfig) */
    @Override
    public void importState(CommuniqueConfig config) {
        this.config = config;
    }

    /** @see com.git.ifly6.nsapi.telegram.JTelegramLogger#log(java.lang.String) */
    @Override
    public void log(String input) {
        LOGGER.info(input);
    }

    /** @see com.git.ifly6.nsapi.telegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
    @Override
    public void sentTo(String nationName, int x, int length) {
        config.addcRecipient(CommuniqueRecipients.createExcludedNation(nationName));
    }
}
