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

package com.git.ifly6.communique.gui3;

import com.apple.eawt.Application;
import com.git.ifly6.commons.CommuniqueApplication;
import com.git.ifly6.commons.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Monitor;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueFilterType;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.communique.io.CommuniqueProcessingAction;
import com.git.ifly6.communique.io.CommuniqueScraper;
import com.git.ifly6.communique.ngui.CommuniqueConstants;
import com.git.ifly6.communique.ngui.CommuniqueDocumentListener;
import com.git.ifly6.communique.ngui.CommuniqueSendDialog;
import com.git.ifly6.communique.ngui.CommuniqueTextDialog;
import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.ctelegram.CommSender;
import com.git.ifly6.nsapi.ctelegram.CommSenderInterface;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly;
import com.git.ifly6.nsapi.telegram.JTelegramType;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.PlainDocument;
import javax.swing.undo.UndoManager;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.git.ifly6.commons.CommuniqueApplication.APP_SUPPORT;
import static com.git.ifly6.commons.CommuniqueUtilities.IS_OS_MAC;
import static com.git.ifly6.communique.gui3.Communique3ConfigHandler.durationAsSeconds;
import static com.git.ifly6.communique.gui3.Communique3Utils.appendLine;
import static com.git.ifly6.communique.gui3.Communique3Utils.getComboBoxSelection;
import static com.git.ifly6.communique.ngui.CommuniqueConstants.COMMAND_KEY;

/**
 * Graphical GUI to send telegrams.
 * @since version 3.0 (build 13)
 */
public class Communique3 implements CommSenderInterface {

    public static final Logger LOGGER = Logger.getLogger(Communique3.class.getName());
    public static String jarName = "Communique_" + Communique7Parser.BUILD + ".jar";
    private static Communique3Settings settings;

    private Communique3DialogHandler dialogHandler;
    private Communique3ConfigHandler configHandler;
    CommSender client;

    CommuniqueConfig config;
    JTextArea textArea;
    JTextField fieldClient;
    JTextField fieldSecret;
    JTextField fieldTelegramID;

    JTextField fieldAutoStop;
    JTextField fieldTelegramDelay;
    JComboBox<CommuniqueProcessingAction> fieldProcessingAction;
    JComboBox<JTelegramType> fieldTelegramType;

    private JFrame frame;
    private JPanel panel;

    private JButton sendButton;
    private JButton stopButton;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private OptionalLong finishCondition;

    private JProgressBar progressBar;
    private JLabel progressLabel;
    JCheckBox repeatBox;

    public Communique3() {
        // $$$setupUI$$$(); // setup UI starts here
        frame = new JFrame(CommuniqueApplication.COMMUNIQUE.generateName(false));
        dialogHandler = new Communique3DialogHandler(frame, LOGGER);
        configHandler = new Communique3ConfigHandler(this);

        Communique3Utils.setupDimensions(frame,
                new Dimension(900, 500),
                new Dimension(600, 400),
                false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(this.panel);
        frame.setJMenuBar(initialiseMenuBar());

        frame.pack();
        frame.setVisible(true);

        config = Communique3Utils.loadAutoSave(); // load autosave
        initialiseModelComponents();
        initialiseButtons();
        initialiseAutoSaves();
    }

    private void createUIComponents() {
        // combo boxes
        fieldProcessingAction = new JComboBox<>(CommuniqueProcessingAction.values());
        fieldTelegramType = new JComboBox<>(JTelegramType.values());
    }

    private void initialiseModelComponents() {
        // text fields and areas document listeners
        textArea.getDocument().addDocumentListener(
                new CommuniqueDocumentListener(e -> {
                    List<CommuniqueRecipient> recipients =
                            Arrays.stream(textArea.getText().split("\n"))
                                    .filter(ApiUtils::isNotEmpty)
                                    .filter(CommuniqueUtilities.NO_COMMENTS)
                                    .map(CommuniqueRecipient::parseRecipient)
                                    .collect(Collectors.toList());
                    config.setcRecipients(recipients);
                }));
        fieldClient.getDocument().addDocumentListener(
                new CommuniqueDocumentListener(e -> config.keys.setClientKey(fieldClient.getText())));
        fieldSecret.getDocument().addDocumentListener(
                new CommuniqueDocumentListener(e -> config.keys.setSecretKey(fieldSecret.getText())));
        fieldTelegramID.getDocument().addDocumentListener(
                new CommuniqueDocumentListener(e -> config.keys.setTelegramId(fieldTelegramID.getText())));

        // autostop and telegram delay fields' listeners
        repeatBox.addActionListener(e -> config.repeats = repeatBox.isSelected());
        fieldAutoStop.getDocument().addDocumentListener(new CommuniqueDocumentListener(
                e -> {
                    Duration duration = null;
                    try {
                        duration = Duration.ofMinutes(Long.parseLong(fieldAutoStop.getText()));
                    } catch (NumberFormatException ignored) { }
                    config.autoStop = duration;
                }));
        fieldTelegramDelay.getDocument().addDocumentListener(new CommuniqueDocumentListener(
                e -> {
                    Duration duration = null;
                    try {
                        double seconds = Double.parseDouble(fieldAutoStop.getText());
                        long millis = Math.round(seconds * 1000);
                        duration = Duration.ofMillis(millis);
                    } catch (NumberFormatException ignored) { }
                    config.telegramDelay = duration;
                }));

        // mouse listeners to explain these fields
        fieldAutoStop.addMouseListener(new CommuniqueMouseAdapter(e -> {
            Communique3Utils.createBalloonTip((JComponent) e.getComponent(),
                    "Automatically stops sending after specified minutes");
        }));
        fieldTelegramDelay.addMouseListener(new CommuniqueMouseAdapter(e -> {
            if (!e.getComponent().isEnabled()) {
                Communique3Utils.createBalloonTip((JComponent) e.getComponent(),
                        "Custom telegram delays require the custom telegram type");
            }
        }));

        // implement numeric filters for these fields
        for (JTextField field : new JTextField[] {fieldAutoStop, fieldTelegramDelay}) {
            PlainDocument pDoc = (PlainDocument) field.getDocument();
            pDoc.setDocumentFilter(new Communique3NumericDocumentFilter(field));
        }

        // update configuration enums
        fieldProcessingAction.addActionListener(e -> {
            config.processingAction = getComboBoxSelection(fieldProcessingAction);
        });
        fieldTelegramType.addActionListener(e -> {
            config.telegramType = getComboBoxSelection(fieldTelegramType);
            fieldTelegramDelay.setEnabled(config.telegramType == JTelegramType.CUSTOM);
            fieldTelegramDelay.setText(durationAsSeconds(config.getTelegramDelay())); // tg delay gets custom setting
        });

        // load everything
        configHandler.setConfig(this.config);
    }

    private void initialiseButtons() {
        // starting status
        stopButton.setEnabled(false);
        sendButton.addActionListener(e -> initialiseClient());
        stopButton.addActionListener(e -> {
            if (client != null) {
                LOGGER.info("Stopping client");
                client.stopSend();
            }

            sendButton.setEnabled(true);
            stopButton.setEnabled(false);
        });
    }

    private void initialiseClient() {
        try {
            // Parser and expand recipients
            Communique7Monitor communique7Monitor = new Communique7Monitor(config);
            List<String> parsedRecipients = communique7Monitor.preview();

            CommuniqueSendDialog sendDialog = new CommuniqueSendDialog(frame, parsedRecipients, config.getTelegramDelay());
            LOGGER.info("CommuniqueSendDialog " + (sendDialog.getValue() == 0
                    ? "cancelled"
                    : "accepted with " + sendDialog.getValue()));

            if (sendDialog.getValue() == CommuniqueSendDialog.SEND) {
                client = new CommSender(config.keys, communique7Monitor, config.getTelegramType(), this);
                client.startSend();

                final Optional<Duration> autoStop = config.getAutoStop();
                autoStop.ifPresent(
                        duration -> Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                            client.stopSend();
                            dialogHandler.showMessageDialog(
                                    String.format("Communiqué stopped automatically after %s.",
                                            CommuniqueUtilities.time(duration.getSeconds())), // time auto-formats
                                    CommuniqueConstants.TITLE);

                        }, duration.toMillis(), TimeUnit.MILLISECONDS));

                stopButton.setEnabled(true);
                sendButton.setEnabled(false);
            }

        } catch (Throwable e) {
            dialogHandler.showErrorDialog("Encountered error during initialisation!", e);
        }
    }

    /**
     * Intialises a {@link Runtime#addShutdownHook(Thread)} which saves {@link Communique3Settings} and {@link
     * Communique3Utils#saveAutoSave(CommuniqueConfig)}.
     */
    private void initialiseAutoSaves() {
        settings = Communique3Settings.load(config);
        CommuniqueApplication.setLogLevel(settings.loggingLevel);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                settings.save();
            } catch (IOException ignored) { }
            Communique3Utils.saveAutoSave(this.config);
        }));
    }

    private JMenuBar initialiseMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmSave = new JMenuItem("Save");
        mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, COMMAND_KEY));
        mntmSave.addActionListener(e -> {
            Optional<Path> choice = dialogHandler.showFileChooser(
                    Communique3DialogHandler.ChooserMode.SAVE,
                    "Save Communique config file as...");
            if (choice.isPresent()) {
                try {
                    CommuniqueLoader loader = new CommuniqueLoader(choice.get());
                    loader.save(this.config);
                } catch (IOException exception) {
                    dialogHandler.showErrorDialog(String.format("Cannot save file at %s", choice), exception);
                }
            }
        });
        mnFile.add(mntmSave);

        JMenuItem mntmOpen = new JMenuItem("Open");
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, COMMAND_KEY));
        mntmOpen.addActionListener(e -> {
            Optional<Path> choice = dialogHandler.showFileChooser(
                    Communique3DialogHandler.ChooserMode.OPEN,
                    "Open Communique config file...");
            if (choice.isPresent()) {
                try {
                    CommuniqueLoader loader = new CommuniqueLoader(choice.get());
                    this.configHandler.setConfig(loader.load());
                } catch (IOException exception) {
                    dialogHandler.showErrorDialog(String.format("Cannot load file %s", choice), exception);
                }
            }
        });
        mnFile.add(mntmOpen);

        mnFile.addSeparator();

        JMenuItem mntmClose = new JMenuItem("Close");
        mntmClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, COMMAND_KEY));
        mntmClose.addActionListener(e -> {
            frame.setVisible(false);
            frame.dispose();
            System.exit(0);
        });
        mnFile.add(mntmClose);

        mnFile.addSeparator();

        JMenuItem mntmShowDirectory = new JMenuItem("Open Application Support");
        mntmShowDirectory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                COMMAND_KEY | InputEvent.SHIFT_DOWN_MASK));
        mntmShowDirectory.addActionListener(event -> {
            try {
                Desktop.getDesktop().open(APP_SUPPORT.toFile());
            } catch (IOException e) { LOGGER.log(Level.SEVERE, "Could not open app support!", e); }
        });
        mnFile.add(mntmShowDirectory);

        // Only add the Quit menu item if the OS is not Mac
        if (!IS_OS_MAC) {
            mnFile.addSeparator();
            JMenuItem mntmExit = new JMenuItem("Exit");
            mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, COMMAND_KEY));
            mntmExit.addActionListener(e -> mntmClose.getActionListeners()[0].actionPerformed(e));
            mnFile.add(mntmExit);
        }

        JMenu mnEdit = new JMenu("Edit");
        menuBar.add(mnEdit);

        // Create undo manager to get that dope functionality
        UndoManager undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);

        JMenuItem mntmUndo = new JMenuItem("Undo");
        mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, COMMAND_KEY));
        mntmUndo.addActionListener(e -> {
            if (undoManager.canUndo()) undoManager.undo();
        });
        mnEdit.add(mntmUndo);

        JMenuItem mntmRedo = new JMenuItem("Redo");
        mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                COMMAND_KEY | InputEvent.SHIFT_DOWN_MASK));
        mntmRedo.addActionListener(e -> {
            if (undoManager.canRedo()) undoManager.redo();
        });
        mnEdit.add(mntmRedo);

        mnEdit.addSeparator();

        JMenuItem mntmImportKeysFrom = new JMenuItem("Import Keys from Telegram URL");
        mntmImportKeysFrom.addActionListener(e -> {
            Optional<String> response = dialogHandler.showTextInputDialog(
                    "Paste in keys from the URL provided by receipt by the telegrams API");
            if (response.isPresent())
                try {
                    String rawURL = response.get();
                    if (!rawURL.matches("http(s)?://www\\.nationstates\\.net/cgi-bin/api\\.cgi.*$")
                            || !rawURL.contains("sendTG"))
                        throw new IllegalArgumentException(String.format("Cannot parse non-API call %s", rawURL));

                    Matcher m = Pattern.compile("(?<=(tgid=)|(key=))(.+?)(?=&|$)").matcher(rawURL);
                    List<String> matches = new ArrayList<>();
                    while (m.find())
                        matches.add(m.group());

                    fieldTelegramID.setText(matches.get(0)); // first match is always telegram ID
                    fieldSecret.setText(matches.get(1));

                } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
                    dialogHandler.showErrorDialog(
                            "Input a properly formatted NationStates URL in the form displayed "
                                    + "when a telegram is sent to 'tag:api'.", exception);
                }
        });
        mnEdit.add(mntmImportKeysFrom);

        JMenu mnImportRecipients = new JMenu("Import Recipients");
        mnEdit.add(mnImportRecipients);

        JMenuItem mntmFromWaDelegate = new JMenuItem("From WA Delegate List");
        mntmFromWaDelegate.addActionListener(e ->
                appendLine(textArea, CommuniqueRecipient.DELEGATES));
        mnImportRecipients.add(mntmFromWaDelegate);

        JMenuItem mntmAsCommaSeparated = new JMenuItem("As Comma Separated List");
        mntmAsCommaSeparated.addActionListener(e -> {
            String message = "Input a string of recipients, eg that found on the list of delegates on World " +
                    "Assembly vote pages";
            Optional<String> input = dialogHandler.showTextInputDialog(message);
            if (input.isPresent()) {
                String inputString = input.get();
                inputString = inputString.replaceAll("\\(.+?\\)", ""); // get rid of brackets and anything in them
                Arrays.stream(inputString.split(",\\s*?"))
                        .map(CommuniqueRecipients::createNation) // createNation auto-trims
                        .map(CommuniqueRecipient::toString)
                        .forEach(s -> appendLine(textArea, s));
            }
        });
        mnImportRecipients.add(mntmAsCommaSeparated);

        JMenuItem mntmFromAtVote = new JMenuItem("From At Vote Screen");
        mntmFromAtVote.addActionListener(e -> {
            Object[] possibilities = {"GA For", "GA Against", "SC For", "SC Against"};
            Optional<Object> response = dialogHandler.showChoiceSelector(
                    "Select chamber and side", CommuniqueConstants.TITLE,
                    possibilities, possibilities[0]);
            if (response.isPresent()) {
                String selection = (String) response.get();

                if (!ApiUtils.isEmpty(selection)) {
                    LOGGER.info("Starting scrape of NS WA voting page: " + selection);
                    String[] elements = selection.toLowerCase().split("\\s+?");
                    try {
                        final String chamber = elements[0].trim().equals("ga") ? CommuniqueScraper.GA : CommuniqueScraper.SC;
                        final String side = elements[1].trim().equals("for") ? CommuniqueScraper.FOR : CommuniqueScraper.AGAINST;
                        CommuniqueScraper.importAtVoteDelegates(chamber, side).stream()
                                .map(CommuniqueRecipient::toString)
                                .forEach(s -> appendLine(textArea, s));

                    } catch (CommWorldAssembly.NoSuchProposalException nre) {
                        dialogHandler.showMessageDialog("No resolution is at vote in that chamber, cannot import data",
                                CommuniqueConstants.ERROR);

                    } catch (NSIOException exc) {
                        LOGGER.log(Level.WARNING, "Cannot import data.", exc);
                        dialogHandler.showMessageDialog("Cannot import data from NationStates website",
                                CommuniqueConstants.ERROR);
                        exc.printStackTrace();
                    }
                }
            }
        });
        mnImportRecipients.add(mntmFromAtVote);

        JMenuItem mntmFromTextFile = new JMenuItem("From Text File");
        mntmFromAtVote.setToolTipText("Reads line in file and appends them to the text screen");
        mntmFromTextFile.addActionListener(event -> {
            Optional<Path> response = dialogHandler.showFileChooser(
                    Communique3DialogHandler.ChooserMode.OPEN,
                    "Choose text file...",
                    new FileNameExtensionFilter("Text files", "txt"));
            if (response.isPresent()) {
                Path path = response.get();
                try {
                    Files.lines(path) // attempt load data
                            .filter(CommuniqueUtilities.NO_COMMENTS)
                            .filter(ApiUtils::isNotEmpty)
                            .map(ApiUtils::ref) // process
                            .forEach(s -> appendLine(textArea, s)); // append to text area
                } catch (IOException exception) {
                    dialogHandler.showErrorDialog(String.format("Could not read file %s.", path), exception);
                }
            }
        });
        mnImportRecipients.add(mntmFromTextFile);

        mnEdit.addSeparator();

        JMenuItem mntmAddExcludedNations = new JMenuItem("Add Excluded Nations");
        mntmAddExcludedNations.setToolTipText("Inputs comma-separated values as excluded");
        mntmAddExcludedNations.addActionListener(e -> {
            String message = "Input nations to exclude as comma-separated list (Do not include trailing 'and'.)";
            Optional<String> response = dialogHandler.showTextInputDialog(message);
            if (response.isPresent()) {
                String input = response.get();
                input = input.replaceAll("\\(.+?\\)", ""); // get rid of brackets and anything in them
                Arrays.stream(input.split(","))
                        .map(n -> CommuniqueRecipients.createNation(CommuniqueFilterType.EXCLUDE, n)) // method auto-formats
                        .map(CommuniqueRecipient::toString)
                        .forEach(s -> appendLine(textArea, s));
            }
        });
        mnEdit.add(mntmAddExcludedNations);

        JMenu mnWindow = new JMenu("Window");
        menuBar.add(mnWindow);

        JMenuItem mntmMinimise = new JMenuItem("Minimise");
        mntmMinimise.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, COMMAND_KEY));
        mntmMinimise.addActionListener(e -> {
            if (frame.getState() == Frame.NORMAL)
                frame.setState(Frame.ICONIFIED);
        });
        mnWindow.add(mntmMinimise);

        mnWindow.addSeparator();

        JMenu mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);

        Runnable aboutRunnable = () -> CommuniqueTextDialog.createMonospacedDialog(frame, "About",
                CommuniqueConstants.acknowledgement, true);
        if (!IS_OS_MAC) {
            JMenuItem mntmAbout = new JMenuItem("About");
            mntmAbout.addActionListener(e -> aboutRunnable.run());
            mnHelp.add(mntmAbout);
            mnHelp.addSeparator();
        }

        JMenuItem mntmDocumentation = new JMenuItem("Documentation");
        mntmDocumentation.addActionListener(event -> {
            try {
                Desktop.getDesktop().browse(CommuniqueConstants.GITHUB_URI);
            } catch (IOException e) {
                dialogHandler.showErrorDialog("Cannot open Github page.", e);
            }
        });
        mnHelp.add(mntmDocumentation);

        JMenuItem mntmForumThread = new JMenuItem("Forum Thread");
        mntmForumThread.addActionListener(event -> {
            try {
                Desktop.getDesktop().browse(CommuniqueConstants.FORUM_THREAD);
            } catch (IOException e) {
                dialogHandler.showErrorDialog("Cannot open Communiqué NationStates forum thread", e);
            }
        });
        mnHelp.add(mntmForumThread);

        JMenuItem mntmUpdate = new JMenuItem("Check for updates...");
        mntmUpdate.addActionListener((e) -> Communique3Updater.create());
        mnHelp.add(mntmUpdate);

        mnHelp.addSeparator();

        JMenuItem mntmLicence = new JMenuItem("Licence");
        mntmLicence.addActionListener(e ->
                CommuniqueTextDialog.createMonospacedDialog(frame, "Licence",
                        CommuniqueConstants.getLicence(), false));
        mnHelp.add(mntmLicence);

        { // handle preferences, also the object in the correct native location
            // everything is handled by this dialog, just run it
            Runnable prefrencesRunnable = () -> {
                Optional<Communique3Settings> s = new Communique3SettingsDialog(this.frame, settings).getFinalSettings();
                settings = s.orElse(settings);
            };
            if (IS_OS_MAC) {
                Application app = Application.getApplication();
                app.setPreferencesHandler(event -> prefrencesRunnable.run());
                app.setQuitHandler((event, response) -> System.exit(0));
                app.setAboutHandler(event -> aboutRunnable.run());

            } else {
                JMenuItem preferences = new JMenuItem("Preferences...");
                preferences.addActionListener(e -> prefrencesRunnable.run());
                mnFile.add(preferences);
            }
        }

        return menuBar;
    }

    public static void main(String[] args) {
        CommuniqueApplication.nativise(CommuniqueApplication.COMMUNIQUE); // first!

        // todo what else needs to be done in initialisation?
        jarName = CommuniqueApplication.setupLogger(CommuniqueApplication.COMMUNIQUE);
        CommuniqueApplication.setLAF();

        Executors.newSingleThreadExecutor().submit(() -> {
            LOGGER.info("Starting log compression task");
            CommuniqueApplication.compressLogs();
        });
        EventQueue.invokeLater(Communique3::new);
    }

    @Override
    public void processed(String recipient, int numberProcessed, CommSender.SendingAction action) {
        EventQueue.invokeLater(() -> {
            // update text interfaces
            finishCondition = client.getMonitor().recipientsCountIfKnown();
            appendLine(textArea, CommuniqueRecipients.createExcludedNation(recipient));
            if (finishCondition.isPresent()) {
                final String text = String.format("%d of %d", numberProcessed, finishCondition.getAsLong())
                                + (config.repeats ? " (est)" : "");
                progressLabel.setText(text);
            } else
                progressLabel.setText(String.format("%d sent", numberProcessed));

            // draw timer changes
            Duration duration = Duration.between(Instant.now(), client.nextAt());
            progressBar.setMaximum((int) duration.toMillis());
            Timer timer = new Timer((int) Duration.ofMillis(15).toMillis(), e -> {
                int timeElapsed = progressBar.getMaximum()
                        - (int) Duration.between(Instant.now(), client.nextAt()).toMillis();
                progressBar.setValue(timeElapsed);
            });
            timer.start();
        });
    }

    @Override
    public void onError(String m, Throwable e) {
        // do not call onTerminate; onTerminate already called by the sending thread!
        dialogHandler.showErrorDialog(m, e);
        stopButton.doClick();
    }

    @Override
    public void onTerminate() {
        LOGGER.info("Termination passed to Communique");
        client.stopSend();

        Set<String> sentTo = client.getSentList();
        Set<String> skipped = client.getSkipList();

        List<String> messages = new ArrayList<>();
        messages.add(String.format("Successful queries to %d nations.\n", sentTo.size()));

        if (skipped.size() != 0) { // if there was a failure to connect,
            messages.add("Failure to dispatch to the following nations, not auto-excluded:\n");  // add formatting,
            skipped.forEach(s -> messages.add("- " + s));
        }

        if (sentTo.size() == 0)   // if does not contain any trues, i.e. all false
            messages.add("\nNo successful queries. Check the log file for errors and report to author as necessary.");

        CommuniqueTextDialog.createMonospacedDialog(frame, "Results",
                String.join("\n", messages), true);
        stopButton.doClick();
    }

}
