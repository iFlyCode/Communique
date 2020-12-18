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

import com.git.ifly6.commons.CommApplication;
import com.git.ifly6.commons.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.ngui.AbstractCommunique;
import com.git.ifly6.nsapi.ctelegram.CommSender;
import com.git.ifly6.nsapi.ctelegram.CommSenderInterface;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommNewNationsMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommStaticMonitor;
import com.git.ifly6.nsapi.telegram.JTelegramLogger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Command line program executing {@link com.git.ifly6.communique.ngui.Communique} configuration files.
 * @since version 1.0 (build 1) */
public class Marconi extends AbstractCommunique implements JTelegramLogger, CommSenderInterface {

    private static final Logger LOGGER = Logger.getLogger(Marconi.class.getName());
    private static final Options COMMAND_LINE_OPTIONS;

    static {
        Options options = new Options();
        options.addOption("h", "help", false, "Displays this message");
        options.addOption("v", "version", false, "Prints version");
        COMMAND_LINE_OPTIONS = options;
    }

    private static Marconi instance;
    private CommuniqueConfig config;
    private CommSender client;

    private Marconi() {}

    public static Marconi getInstance() {
        if (instance == null) instance = new Marconi();
        return instance;
    }

    public static void main(String[] args) {
        String fileName = CommApplication.startUp(CommApplication.MARCONI);

        // parse command line options
        try {
            CommandLineParser cliParse = new DefaultParser();
            CommandLine commandLine = cliParse.parse(COMMAND_LINE_OPTIONS, args);
            LOGGER.info(String.format("Initialised with arguments: %s", commandLine.getArgList().toString()));

            // help option
            if (commandLine.hasOption("h") || commandLine.getArgs().length != 1) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(String.format("java -jar %s COMMUNIQUE_CONFIGURATION_FILE", fileName),
                        "Send telegrams based on Communique configuration files",
                        COMMAND_LINE_OPTIONS,
                        "Report issues to https://github.com/ifly6/Communique.",
                        true);
                System.out.println();
                System.exit(0); // terminate
            }

            // version option
            if (commandLine.hasOption("v")) {
                System.out.println("Marconi version " + Communique7Parser.BUILD + "\n"
                        + "See https://github.com/ifly6/Communique/releases.\n");
                System.exit(0); // terminate
            }

            Marconi marconi = Marconi.getInstance();
            Path configPath = Paths.get(commandLine.getArgs()[0]);
            if (Files.exists(configPath)) {
                marconi.load(configPath);
                marconi.send();

            } else throw new FileNotFoundException(String.format("File %s does not exist", configPath));

        } catch (ParseException e) {
            final String parseErrorMessage = "Cannot parse command arguments. Refer help, accessible with '-h'.";
            LOGGER.severe(parseErrorMessage);

        } catch (FileNotFoundException e) {
            LOGGER.severe(e.getMessage());

        } catch (IOException e) {
            String ioMessage = "Configuration file at %s; read error.";
            LOGGER.severe(ioMessage);
        }
    }

    /** Sets up checks, client, and sends telegrams. */
    private void send() {
        // Parser and expand recipients
        List<CommuniqueRecipient> cRecipients = config.getcRecipients();
        Communique7Parser parser = new Communique7Parser().apply(cRecipients);
        List<String> expandedRecipients = config.processingAction.apply(parser.listRecipients());

        // todo better way to handle flags
        CommMonitor monitor =
                cRecipients.contains(CommuniqueRecipients.createFlag("recruit"))
                        ? CommNewNationsMonitor.getInstance()
                        : new CommStaticMonitor(expandedRecipients);

        // Show the recipients in the order we are to send the telegrams.
        System.out.println();
        System.out.println(MarconiUtilities.twoColumn(expandedRecipients));

        System.out.println();
        System.out.printf("This should take %s to send %d telegrams%n",
                CommuniqueUtilities.time(Math.round(expandedRecipients.size()
                        * (config.telegramType.getWaitTime() / (double) 1000))),
                expandedRecipients.size());

        // allow cancel
        System.out.println("You have 3 (three) seconds to cancel.");
        try {
            Thread.sleep(Duration.ofSeconds(3).toMillis());
        } catch (InterruptedException e) {
            System.out.println("Exiting.");
            System.exit(0);
        }

        // Set the client up and go.
        client = new CommSender(config.keys, monitor, config.telegramType, this);

        // Check for file lock and send
        if (!MarconiUtilities.isFileLocked()) {
            MarconiUtilities.createFileLock(); // create file lock
            client.startSend();
        } else throw new RuntimeException("Cannot send, as another instance of Marconi is already sending.");

    }

    @Override
    @SuppressWarnings("RedundantStringFormatCall")
    public void sentTo(String recipient, int numberSent) {
        config.addcRecipient(CommuniqueRecipients.createExcludedNation(recipient));
        System.out.println(String.format("Sent telegram %s to recipient %s", numberSent, recipient));
    }

    @Override
    public void onTerminate() {
        System.out.println("Sending thread terminated");
        List<String> items = new ArrayList<>(client.getSentList());

        System.out.printf("Sent telegrams to %d nations:%n", items.size());
        System.out.println("\n" + MarconiUtilities.twoColumn(items) + "\n");

        System.out.println();
        System.out.printf("Sending thread elapsed: %s",
                CommuniqueUtilities.time(
                        Duration.between(client.getInitAt(), Instant.now()).getSeconds()));

        exportState();
    }

    /** {@inheritDoc} */
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
    @Deprecated
    public void log(String input) {
        LOGGER.info(input);
    }

    /** @see com.git.ifly6.nsapi.telegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
    @Override
    @Deprecated
    public void sentTo(String nationName, int x, int length) {
        config.addcRecipient(CommuniqueRecipients.createExcludedNation(nationName));
    }
}
