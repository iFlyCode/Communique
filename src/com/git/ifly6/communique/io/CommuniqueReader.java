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

import com.git.ifly6.communique.CommuniqueFileReader;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.data.CommuniqueFilterType;
import com.git.ifly6.nsapi.telegram.JTelegramType;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * {@link CommuniqueReader} reads Communique configuration files. It contains some backwards compatibility for the last
 * file build of the pre-JSON configuration files. The handling for this logic is done primarily in the {@link
 * CommuniqueReader#read()} file.
 * @see CommuniqueFileReader
 */
@SuppressWarnings("deprecation")
        // CommuniqueFileReader
class CommuniqueReader {

    private static final Logger LOGGER = Logger.getLogger(CommuniqueReader.class.getName());

    /**
     * {@link Path} where the class is pointed to
     */
    private Path path;

    CommuniqueReader(Path path) {
        this.path = path;
    }

    /**
     * Reads data in the location specified in the path in the constructor. If necessary, it employs the methods
     * declared in {@link Communique7Parser} to translate old Communique 6 tokens into the tokens introduced in
     * Communique 7. It will also automatically decode old files which throw JSON errors using the deprecated {@link
     * CommuniqueFileReader}.
     * @return a {@link CommuniqueConfig} holding the data specified
     * @throws IOException if there is an issue reading the data
     */
    CommuniqueConfig read() throws IOException {

        CommuniqueConfig config;

        try { // note, this will handle future builds of the class by ignoring the now-irrelevant fields
            Gson gson = new Gson();
            config = gson.fromJson(Files.newBufferedReader(path), CommuniqueConfig.class);

            // convert from randomise flag to new enums
            if (config.version == 7) {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines)
                    if (line.trim().equals("\"isRandomised\": true,")) {
                        config.processingAction = CommuniqueProcessingAction.RANDOMISE;
                        break;
                    }
            }

            // correct for introduction of recruitment enum instead of boolean flag
            if (config.version <= 11)
                for (String line : Files.readAllLines(path))
                    if (line.trim().equals("\"isRecruitment\": true")) {
                        config.telegramType = JTelegramType.RECRUIT;
                        break;
                    }

            // correct for removal of flag:recruit
            final String OLD_RECRUIT_FLAG = "flag:recruit";
            if (config.version <= 13) {
                if (config.getcRecipientsString().contains(OLD_RECRUIT_FLAG)) {
                    config.getcRecipientsString().replaceAll(s -> s.equalsIgnoreCase(OLD_RECRUIT_FLAG)
                            ? "tag:new" : s);
                    config.repeats = true;
                }
                if (config.getcRecipientsString().contains("flag:active"))
                    config.getcRecipientsString().replaceAll(s -> s.equalsIgnoreCase("flag:active")
                            ? "_happenings:active" : s);
            }

            RecipientCapture capture = new Gson().fromJson(Files.newBufferedReader(path), RecipientCapture.class);
            if (config.version <= 7)
                unifySendList(config, () -> capture);

        } catch (JsonSyntaxException | JsonIOException e) {
            // Attempting to read one of the old files would throw Json exceptions; try the old reader
            LOGGER.log(Level.INFO, "Json exception thrown. Attempting read with old file reader.", e);
            CommuniqueFileReader reader = new CommuniqueFileReader(path.toFile());

            config = new CommuniqueConfig();
            config.version = reader.getFileBuild();
            config.processingAction = reader.isRandomised() // translate old boolean flag
                    ? CommuniqueProcessingAction.RANDOMISE
                    : CommuniqueProcessingAction.NONE;
            config.telegramType = reader.isRecruitment() // translate old recruitment flag
                    ? JTelegramType.RECRUIT
                    : JTelegramType.CAMPAIGN;
            config.keys = reader.getKeys();

            List<CommuniqueRecipient> recipients = CommuniqueRecipient
                    .translateTokens(Arrays.asList(reader.getRecipients()))
                    .stream()
                    .map(CommuniqueRecipient::parseRecipient)
                    .collect(Collectors.toList());
            config.setcRecipients(recipients);
        }

        return config;
    }

    /**
     * Parses and unifies a list of recipients. The recipients must be captured as two {@code String[]} with names
     * {@code recipient} and {@code sentList}. {@link RecipientCapture} implements this.
     * <p>This exists because of an interim period before the creation of {@link CommuniqueRecipient} but after {@link
     * CommuniqueConfig} was turned into a serialised JSON file. In that period, strings in the old recipients format
     * were stored in the two (now-removed) array fields. This is here for backwards compatibility.</p>
     * <p>This acts in place.</p>
     * @param config                   to act on
     * @param recipientCaptureSupplier holding a {@link RecipientCapture}
     */
    private void unifySendList(CommuniqueConfig config, Supplier<RecipientCapture> recipientCaptureSupplier) {
        if (config.getcRecipients() == null || config.getcRecipients().isEmpty()) { // only if null is this necessary
            RecipientCapture capture = recipientCaptureSupplier.get();
            List<CommuniqueRecipient> list = new ArrayList<>();

            // if recipients contains things, lets deal with that
            if (capture.recipients != null && capture.recipients.length != 0) {
                List<String> recipients = CommuniqueRecipient.translateTokens(asList(capture.recipients));
                list.addAll(recipients.stream()    // add all recipients
                        .map(CommuniqueRecipient::parseRecipient)
                        .map(r -> CommuniqueRecipients.setFilter(r, CommuniqueFilterType.NORMAL))
                        .collect(Collectors.toList()));
            }

            // if sentList contains things, deal with that
            if (capture.sentList != null && capture.sentList.length != 0) {
                List<String> sentList = CommuniqueRecipient.translateTokens(asList(capture.sentList));
                list.addAll(sentList.stream()  // translate, then change flag as necessary
                        .map(CommuniqueRecipient::parseRecipient)
                        .map(CommuniqueRecipients::exclude) // manual cast if necessary
                        .collect(Collectors.toList()));
            }

            config.setcRecipients(list);
        }
    }

    /** Facilitates easy serialisation of two {@code String[]} with names {@code recipient} and {@code sentList}. */
    private static class RecipientCapture {
        public String[] recipients;
        public String[] sentList;
    }

}
