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

package com.git.ifly6.communique.ngui;

import com.git.ifly6.commons.CommuniqueApplication;
import com.git.ifly6.commons.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.data.FilterType;
import com.git.ifly6.communique.data.RecipientType;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.communique.io.CommuniqueProcessingAction;
import com.git.ifly6.communique.io.CommuniqueScraper;
import com.git.ifly6.communique.io.NoResolutionException;
import com.git.ifly6.communique.ngui.components.CommuniqueFactory;
import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.telegram.JTelegramException;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramLogger;
import com.git.ifly6.nsapi.telegram.JTelegramType;
import com.git.ifly6.nsapi.telegram.JavaTelegram;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.undo.UndoManager;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static com.git.ifly6.commons.CommuniqueApplication.APP_SUPPORT;
import static com.git.ifly6.communique.ngui.CommuniqueConstants.CODE_HEADER;
import static com.git.ifly6.communique.ngui.CommuniqueConstants.COMMAND_KEY;
import static com.git.ifly6.communique.ngui.components.CommuniqueNativisation.showFileChooser;

/**
 * <code>Communiqué</code> is the main class of the Communiqué system. It handles the GUI aspect of the entire program
 * and other actions.
 */
@Deprecated
public class Communique extends AbstractCommunique implements JTelegramLogger {

    private static final Logger LOGGER = Logger.getLogger(Communique.class.getName());

    private CommuniqueConfig config = new CommuniqueConfig();
    private JavaTelegram client; // Sending client
    private Thread sendingThread = new Thread(); // The one sending thread

    private JFrame frame;
    private JTextArea txtrCode;
    private JTextField txtClientKey;
    private JTextField txtSecretKey;
    private JTextField txtTelegramId;
    private JTextField txtWaitTime;
    private JComboBox<CommuniqueProcessingAction> specialAction;
    private JComboBox<JTelegramType> telegramType;
    private JButton btnParse;

    private List<String> parsedRecipients;
    private Map<String, Boolean> rSuccessTracker;

    private CommuniqueRecruiter recruiter;

    private JProgressBar progressBar;
    private JLabel progressLabel;
    private Timer timer;

    public static void main(String[] args) {
        CommuniqueApplication.setupLogger(CommuniqueApplication.COMMUNIQUE);
        CommuniqueApplication.nativiseMac(CommuniqueApplication.COMMUNIQUE);
        CommuniqueApplication.setLAF(); // note that this line will also set up the static initialisation for appSupport etc
        CommuniqueApplication.compressLogs(); // compresses logs one day older than this initialisation

        EventQueue.invokeLater(() -> {
            try {
                Communique window = new Communique();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the application.
     * @wbp.parser.entryPoint
     */
    public Communique() {
        super();

        client = new JavaTelegram(this);
        initialise();

        // Make sure user is connected to the Internet
        try {
            new URL("https://www.nationstates.net").openConnection().connect();
        } catch (IOException e) {
            this.showMessageDialog(CommuniqueConstants.INTERNET_ERROR, CommuniqueConstants.ERROR);
        }
    }

    /** Initialise the contents of the frame. */
    private void initialise() {

        frame = new JFrame();
        if (!CommuniqueUtilities.IS_OS_MAC)
            frame.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());

        Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
        double sWidth = screenDimensions.getWidth();
        double sHeight = screenDimensions.getHeight();

        frame.setTitle("Communiqué " + Communique7Parser.BUILD);
        frame.setBounds(100, 100, (int) Math.round(2 * sWidth / 3), (int) Math.round(2 * sHeight / 3));
        frame.setMinimumSize(new Dimension(600, 400));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        frame.setContentPane(contentPane);

        contentPane.setLayout(new BorderLayout(5, 5));

        JPanel dataPanel = new JPanel();
        contentPane.add(dataPanel, BorderLayout.CENTER);

        txtrCode = CommuniqueFactory.createArea(CODE_HEADER, new CommuniqueDocumentListener(e -> {
            Communique.this.config.setcRecipients(exportRecipients()); // dynamic update config
        }));
        JScrollPane scrollPane = new JScrollPane(txtrCode);

        // create key fields
        JLabel lblClientKey = new JLabel("Client key", SwingConstants.RIGHT);
        JLabel lblSecretKey = new JLabel("Secret key", SwingConstants.RIGHT);
        JLabel lblTelegramId = new JLabel("Telegram ID", SwingConstants.RIGHT);
        txtClientKey = CommuniqueFactory.createField(
                CommuniqueLoader.getClientKey(), "Client key",
                new CommuniqueDocumentListener(e -> {
                    config.keys.setClientKey(txtClientKey.getText().trim()); // dynamic update config
                })
        );
        txtSecretKey = CommuniqueFactory.createField(
                "",
                "Secret key",
                new CommuniqueDocumentListener(e -> {
                    config.keys.setSecretKey(txtSecretKey.getText().trim()); // dynamic update config
                })
        );
        txtTelegramId = CommuniqueFactory.createField(
                "",
                "Telegram ID",
                new CommuniqueDocumentListener(e -> {
                    config.keys.setTelegramId(txtTelegramId.getText().trim()); // dynamic update config
                })
        );

        progressBar = new JProgressBar();
        progressBar.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        progressLabel = new JLabel("? / ?");

        btnParse = new JButton("Parse");
        btnParse.addActionListener(ae -> setupSend());

        specialAction = new JComboBox<>();
        Arrays.stream(CommuniqueProcessingAction.values()).forEach(specialAction::addItem); // populate selector
        specialAction.setToolTipText("Processing actions can be applied to the list of recipients after they "
                + "are parsed. Select a processing action here");
        specialAction.addActionListener(evt -> {
            config.processingAction = specialAction.getItemAt(specialAction.getSelectedIndex());
            LOGGER.info(String.format("Set config processing action to %s",
                    specialAction.getItemAt(specialAction.getSelectedIndex())));
        });

        JLabel lblProcessingAction = new JLabel("Processing action");

        JLabel lblTelegramType = new JLabel("Telegram type");

        telegramType = new JComboBox<>();
        Arrays.stream(JTelegramType.values()).forEach(telegramType::addItem);
        telegramType.setSelectedItem(JTelegramType.RECRUIT); // default to recruitment
        telegramType.setToolTipText("Telegram types are declared in the telegram itself");
        telegramType.addActionListener(evt -> {
            config.telegramType = currentTelegramType();
            LOGGER.info(String.format("Set telegram type to %s", currentTelegramType()));
        });

        JLabel lblWaitTime = new JLabel("Telegram delay");
        lblWaitTime.setToolTipText("Delay must be in milliseconds");

        txtWaitTime = CommuniqueFactory.createField(
                "",
                "Leave as blank, 'default', or '-' to accept defaults. Must be in milliseconds.",
                new CommuniqueDocumentListener(e -> {
                    config.waitString = txtWaitTime.getText().trim(); // dynamic update config
                })
        );
        txtWaitTime.setColumns(10);

        GroupLayout gl_dataPanel = new GroupLayout(dataPanel);
        gl_dataPanel.setHorizontalGroup(
                gl_dataPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_dataPanel.createSequentialGroup()
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 725, Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.TRAILING)
                                        .addGroup(Alignment.LEADING, gl_dataPanel.createSequentialGroup()
                                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.TRAILING)
                                                        .addComponent(lblWaitTime)
                                                        .addComponent(lblTelegramType)
                                                        .addComponent(lblProcessingAction))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.LEADING)
                                                        .addComponent(telegramType, Alignment.TRAILING, 0, 222, Short.MAX_VALUE)
                                                        .addComponent(specialAction, 0, 222, Short.MAX_VALUE)
                                                        .addComponent(txtWaitTime, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)))
                                        .addGroup(gl_dataPanel.createSequentialGroup()
                                                .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(progressLabel)
                                                .addGap(6))
                                        .addGroup(gl_dataPanel.createSequentialGroup()
                                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.TRAILING)
                                                        .addComponent(lblTelegramId)
                                                        .addComponent(lblSecretKey)
                                                        .addComponent(lblClientKey))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.LEADING)
                                                        .addComponent(txtClientKey, GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                                                        .addComponent(txtSecretKey, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                                                        .addComponent(txtTelegramId, GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)))
                                        .addComponent(btnParse, GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE))
                                .addContainerGap())
        );
        gl_dataPanel.setVerticalGroup(
                gl_dataPanel.createParallelGroup(Alignment.TRAILING)
                        .addGroup(gl_dataPanel.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(txtClientKey, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblClientKey))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(txtSecretKey, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblSecretKey))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(txtTelegramId, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblTelegramId))
                                .addPreferredGap(ComponentPlacement.RELATED, 379, Short.MAX_VALUE)
                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(txtWaitTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblWaitTime))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(telegramType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblTelegramType))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(specialAction, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblProcessingAction))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(btnParse)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_dataPanel.createParallelGroup(Alignment.TRAILING, false)
                                        .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                                        .addComponent(progressLabel, GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE))
                                .addContainerGap())
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE)
        );
        dataPanel.setLayout(gl_dataPanel);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmSave = new JMenuItem("Save");
        mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, COMMAND_KEY));
        mntmSave.addActionListener(e -> {
            Path savePath = showFileChooser(frame, FileDialog.SAVE);
            if (savePath == null) {
                LOGGER.warning("Returned path was null");
                return;
            }
            try {
                Communique.this.save(savePath);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        mnFile.add(mntmSave);

        JMenuItem mntmOpen = new JMenuItem("Open");
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, COMMAND_KEY));
        mntmOpen.addActionListener(e -> {
            Path savePath = showFileChooser(frame, FileDialog.LOAD);

            if (savePath == null) {
                LOGGER.info("Returned path was null");
                return;
            }

            try {
                Communique.this.load(savePath);
            } catch (IOException e1) {
                e1.printStackTrace();
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

        JMenuItem mntmShowDirectory = new JMenuItem("Show Directory");
        mntmShowDirectory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, COMMAND_KEY | InputEvent.SHIFT_DOWN_MASK));
        mntmShowDirectory.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(APP_SUPPORT.toFile());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        mnFile.add(mntmShowDirectory);

        // Only add the Quit menu item if the OS is not Mac
        if (!CommuniqueUtilities.IS_OS_MAC) {
            mnFile.addSeparator();
            JMenuItem mntmExit = new JMenuItem("Exit");
            mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, COMMAND_KEY));
            mntmExit.addActionListener(e -> System.exit(0));
            mnFile.add(mntmExit);
        }

        JMenu mnEdit = new JMenu("Edit");
        menuBar.add(mnEdit);

        // Create undo manager to get that dope functionality
        UndoManager undoManager = new UndoManager();

        JMenuItem mntmUndo = new JMenuItem("Undo");
        mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, COMMAND_KEY));
        mntmUndo.addActionListener(e -> {
            if (undoManager.canUndo()) undoManager.undo();
        });
        mnEdit.add(mntmUndo);

        JMenuItem mntmRedo = new JMenuItem("Redo");
        mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, COMMAND_KEY | InputEvent.SHIFT_DOWN_MASK));
        mntmRedo.addActionListener(e -> {
            if (undoManager.canRedo()) undoManager.redo();
        });
        mnEdit.add(mntmRedo);

        mnEdit.addSeparator();

        JMenuItem mntmImportKeysFrom = new JMenuItem("Import Keys from Telegram URL");
        mntmImportKeysFrom.addActionListener(e -> {

            String rawURL = this.showTextInputDialog("Paste in keys from the URL provided by receipt by the "
                    + "Telegrams API", CommuniqueConstants.TITLE);

            // Verify that it is a valid NationStates URL
            String raw1 = "https://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&";
            String raw2 = "http://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&";
            if (rawURL.startsWith(raw1) || rawURL.startsWith(raw2)) {

                rawURL = rawURL.substring(rawURL.indexOf("a=sendTG&client=YOUR_API_CLIENT_KEY&") +
                        "a=sendTG&client=YOUR_API_CLIENT_KEY&".length()); // use substring
                rawURL = rawURL.replace("&to=NATION_NAME", "");

                String[] shards = rawURL.split("&");
                if (shards.length == 2) {
                    txtTelegramId.setText(shards[0].substring(shards[0].indexOf("=") + "=".length()));
                    txtSecretKey.setText(shards[1].substring(shards[1].indexOf("=") + "=".length()));
                }

            } else this.showMessageDialog("Input a properly formatted NationStates URL in the form displayed "
                    + "when a telegram is sent to 'tag:api'", CommuniqueConstants.ERROR);
        });
        mnEdit.add(mntmImportKeysFrom);

        JMenu mnImportRecipients = new JMenu("Import Recipients");
        mnEdit.add(mnImportRecipients);

        JMenuItem mntmFromWaDelegate = new JMenuItem("From WA Delegate List");
        mntmFromWaDelegate.addActionListener(e -> appendCode(CommuniqueRecipient.DELEGATES));
        mnImportRecipients.add(mntmFromWaDelegate);

        JMenuItem mntmAsCommaSeparated = new JMenuItem("As Comma Separated List");
        mntmAsCommaSeparated.addActionListener(e -> {
            String message = "Input a string of delegates, as found on a list of delegates\nin one of the "
                    + "NationStates World Assembly pages:";
            String input = this.showTextInputDialog(message, CommuniqueConstants.TITLE);
            if (input != null) {
                input = input.replaceAll("\\(.+?\\)", ""); // get rid of brackets and anything in them
                Arrays.stream(input.split(",\\s*?"))
                        .map(CommuniqueRecipients::createNation) // createNation auto-trims
                        .map(CommuniqueRecipient::toString)
                        .forEach(this::appendCode);
            }
        });
        mnImportRecipients.add(mntmAsCommaSeparated);

        JMenuItem mntmFromAtVote = new JMenuItem("From At Vote Screen");
        mntmFromAtVote.addActionListener(e -> {

            Object[] possibilities = {"GA For", "GA Against", "SC For", "SC Against"};
            String selection =
                    (String) JOptionPane.showInputDialog(frame, "Select which chamber and side you want to address:",
                            "Select Chamber and Side", JOptionPane.PLAIN_MESSAGE, null, possibilities, "GA For");

            if (!ApiUtils.isEmpty(selection)) {
                LOGGER.info("Starting scrape of NS WA voting page, " + selection);
                String[] elements = selection.split("\\s*?");
                String chamber = elements[0].equals("GA") ? CommuniqueScraper.GA : CommuniqueScraper.SC;
                String side = elements[1].equals("For") ? CommuniqueScraper.FOR : CommuniqueScraper.AGAINST;

                try {
                    CommuniqueScraper.importAtVoteDelegates(chamber, side).stream()
                            .map(CommuniqueRecipient::toString)
                            .forEach(this::appendCode);

                } catch (NoResolutionException nre) {
                    this.showMessageDialog("No resolution is at vote in that chamber, cannot import data",
                            CommuniqueConstants.ERROR);

                } catch (RuntimeException exc) {
                    LOGGER.log(Level.WARNING, "Cannot import data.", exc);
                    this.showMessageDialog("Cannot import data from NationStates website", CommuniqueConstants.ERROR);
                    exc.printStackTrace();
                }
            }

        });
        mnImportRecipients.add(mntmFromAtVote);

        JMenuItem mntmFromTextFile = new JMenuItem("From Text File");
        mntmFromAtVote.setToolTipText("This file should be of the same syntax as that used by the NationStates "
                + "telegram API or Communique");
        mntmFromTextFile.addActionListener(event -> {
            Path path = showFileChooser(frame, FileDialog.LOAD);
            if (path != null) {
                try {
                    Files.lines(path) // attempt load data
                            .filter(s -> !s.startsWith("#"))
                            .filter(ApiUtils::isNotEmpty)
                            .map(ApiUtils::ref) // process
                            .forEach(this::appendCode); // append to text area
                } catch (IOException e1) {
                    LOGGER.log(Level.WARNING, "Cannot read file, IOException", e1);
                    this.showMessageDialog("Cannot read file at " + path.toString(), CommuniqueConstants.ERROR);
                }
            }
        });
        mnImportRecipients.add(mntmFromTextFile);

        mnEdit.addSeparator();

        JMenuItem mntmAddExcludedNations = new JMenuItem("Add excluded nations");
        mntmAddExcludedNations.setToolTipText("Input comma-separated values");
        mntmAddExcludedNations.addActionListener(e -> {
            String message = "Input nations to exclude as comma-separated list (Do not include trailing 'and'.)";
            String input = this.showTextInputDialog(message, CommuniqueConstants.TITLE);
            if (input != null) {
                input = input.replaceAll("\\(.+?\\)", ""); // get rid of brackets and anything in them
                Arrays.stream(input.split(","))
                        .map(n -> CommuniqueRecipients.createNation(FilterType.EXCLUDE, n)) // method auto-formats
                        .map(CommuniqueRecipient::toString)
                        .forEach(this::appendCode);
            }
        });
        mnEdit.add(mntmAddExcludedNations);

        JMenu mnWindow = new JMenu("Window");
        menuBar.add(mnWindow);

        JMenuItem mntmMinimise = new JMenuItem("Minimise");
        mntmMinimise.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, COMMAND_KEY));
        mntmMinimise.addActionListener(e -> {
            if (frame.getState() == Frame.NORMAL) frame.setState(Frame.ICONIFIED);
        });
        mnWindow.add(mntmMinimise);

        mnWindow.addSeparator();

        JMenuItem mntmRecruiter = new JMenuItem("Recruiter...");
        mntmRecruiter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, COMMAND_KEY));
        mntmRecruiter.addActionListener(e -> showRecruiter());
        mnWindow.add(mntmRecruiter);

        JMenu mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);

        JMenuItem mntmAbout = new JMenuItem("About");
        mntmAbout.addActionListener(
                e -> CommuniqueTextDialog.createMonospacedDialog(
                        frame,
                        "About",
                        CommuniqueConstants.acknowledgement,
                        true));
        mnHelp.add(mntmAbout);

        mnHelp.addSeparator();

        JMenuItem mntmDocumentation = new JMenuItem("Documentation");
        mntmDocumentation.addActionListener(event -> {
            try {
                Desktop.getDesktop().browse(CommuniqueConstants.GITHUB_URI);
            } catch (IOException e) {
                LOGGER.warning("Cannot open Communiqué GitHub page");
                e.printStackTrace();
            }
        });
        mnHelp.add(mntmDocumentation);

        JMenuItem mntmForumThread = new JMenuItem("Forum Thread");
        mntmForumThread.addActionListener(event -> {
            try {
                Desktop.getDesktop().browse(CommuniqueConstants.FORUM_THREAD);
            } catch (IOException e) {
                LOGGER.warning("Cannot open NationStates forum support thread for Communiqué");
                e.printStackTrace();
            }
        });
        mnHelp.add(mntmForumThread);

        mnHelp.addSeparator();

        JMenuItem mntmLicence = new JMenuItem("Licence");
        mntmLicence.addActionListener(e -> CommuniqueTextDialog.createMonospacedDialog(frame, "Licence",
                CommuniqueConstants.getLicence(), false));
        mnHelp.add(mntmLicence);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Save it to application support
                CommuniqueLoader loader = new CommuniqueLoader(APP_SUPPORT.resolve("autosave.txt"));
                loader.save(exportState());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        LOGGER.info("Shutdown hook added");
        LOGGER.info("Communiqué loaded");

        // If there is an auto-save, load it
        if (Files.exists(APP_SUPPORT.resolve("autosave.txt"))) {
            CommuniqueLoader loader = new CommuniqueLoader(APP_SUPPORT.resolve("autosave.txt"));
            try {
                CommuniqueConfig configuration = loader.load();
                this.importState(configuration);
            } catch (IOException e1) {
                // do nothing if it fails to load
            }
        }
        LOGGER.info("Auto-save loaded");

    }

    /**
     * This massive method parses the data and does internal Communique checks; it then passes on to the send method to
     * start execution of the sending process.
     */
    private void setupSend() {

        // Process in the case that the button currently says stop
        if (sendingThread.isAlive() && btnParse.getText().equalsIgnoreCase("Stop")) {
            // kill the thread
            sendingThread.interrupt();
            client.setKillThread(true);
            return;
        }

        List<CommuniqueRecipient> tokens = exportRecipients();

        // Check if a recruit-flag has been used.
        boolean rfPresent = tokens.stream()
                .filter(t -> t.getRecipientType() == RecipientType.FLAG)
                .anyMatch(t -> t.getName().equals("recruit"));
        if (rfPresent) {
            showRecruiter();
            return;
        }

        // Call and do the parsing
        LOGGER.info("Called parser");
        Communique7Parser parser = new Communique7Parser();
        try {
            parsedRecipients = parser.apply(tokens).listRecipients();
            if (!ApiUtils.contains(CommuniqueProcessingAction.values(), config.processingAction)) {
                // if config.processingAction not in CommuniqueProcessingAction.values
                // deal with invalid processing action
                this.showMessageDialog("Invalid processing action.\n"
                        + "Select a valid processing action", CommuniqueConstants.ERROR);
                return;
            }
            parsedRecipients = config.processingAction.apply(parsedRecipients);

        } catch (PatternSyntaxException pse) {
            // note 2020-01-27: better that regex errors are shown in monospaced font
            JLabel label = new JLabel(
                    String.format("<html>Regex pattern syntax error. <br /><pre>%s</pre></html>",
                            pse.getMessage().replace("\n", "<br />"))
            );
            // pass to message dialog
            LOGGER.log(Level.SEVERE, "Exception in parsing recipients. Displaying to user", pse);
            this.showMessageDialog(label, CommuniqueConstants.ERROR);
            return;

        } catch (JTelegramException | IllegalArgumentException jte) {
            LOGGER.log(Level.SEVERE, "Exception in parsing recipients. Displaying to user", jte);
            this.showMessageDialog(jte.getMessage(), CommuniqueConstants.ERROR);
            return;
        }
        LOGGER.info("Recipients Parsed");

        // Change GUI elements
        progressLabel.setText("0 / " + parsedRecipients.size());

        // Check that there are in fact recipients
        if (parsedRecipients.size() == 0) {
            Communique.this.showMessageDialog("No recipients specified.", CommuniqueConstants.ERROR);
            return;
        }

        // Ask for confirmation
        CommuniqueSendDialog sendDialog = new CommuniqueSendDialog(frame, parsedRecipients, currentWaitTime());
        LOGGER.info("Displayed CommuniqueSendDialog");
        LOGGER.info("CommuniqueSendDialog " + (sendDialog.getValue() == 0
                ? "cancelled"
                : "accepted with " + sendDialog.getValue()));
        if (sendDialog.getValue() == CommuniqueSendDialog.SEND) send();
    }

    /**
     * Sending thread. It executes all of these commands in the <code>Runner</code> thread and then calls the completion
     * method.
     */
    private void send() {

        // sending logic
        if (!sendingThread.isAlive()) {
            client.setKillThread(false);
            Runnable runner = () -> {

                client.setRecipients(parsedRecipients);    // Set recipients
                client.setKeys(new JTelegramKeys(
                        txtClientKey.getText().trim(),
                        txtSecretKey.getText().trim(),
                        txtTelegramId.getText().trim()
                ));

                // Set recruitment status, JavaTelegram defaults to true
                client.setTelegramType(this.currentTelegramType());

                // Save client key
                try {
                    CommuniqueLoader.writeProperties(txtClientKey.getText());
                    CommuniqueLoader loader = new CommuniqueLoader(APP_SUPPORT.resolve("autosave.txt"));
                    loader.save(exportState());

                } catch (IOException e) {
                    LOGGER.severe("Exception in writing autosave or properties file");
                    e.printStackTrace();
                }

                // set wait time
                client.setWaitTime(this.currentWaitTime());

                // Create tracker, initialise success tracking HashMap
                rSuccessTracker = new LinkedHashMap<>();
                parsedRecipients.forEach(r -> rSuccessTracker.put(r, false));
                if (rSuccessTracker == null) LOGGER.severe("Success tracker is null");

                try {
                    client.connect();
                } catch (JTelegramException jte) {  // JTE occurring during send?
                    LOGGER.log(Level.SEVERE, "JTelegramException in send", jte);
                    Communique.this.showMessageDialog(jte.getMessage(), CommuniqueConstants.ERROR);
                    return;
                }

                cleanupSend();
            };

            sendingThread = new Thread(runner);
            sendingThread.start();

            btnParse.setText("Stop");
        }
    }

    /**
     * Cleanup commands to be done when sending is complete.
     */
    private void cleanupSend() {
        LOGGER.info("Queries complete");

        List<String> messages = new ArrayList<>();
        messages.add(String.format("Successful queries to %d of %d nations.\n",
                rSuccessTracker.entrySet().stream().filter(e -> e.getValue() == Boolean.TRUE).count(),  // # successes
                parsedRecipients.size()));

        if (rSuccessTracker.containsValue(Boolean.FALSE)) { // if there was a failure to connect,
            messages.add("Failure to dispatch to the following nations, not auto-excluded:\n");  // add formatting,
            rSuccessTracker.entrySet().stream() // and then list the relevant nations to which there was a failure
                    .filter(e -> e.getValue() == Boolean.FALSE)
                    .forEach(e -> messages.add("- " + e.getKey()));
        }

        if (!rSuccessTracker.containsValue(Boolean.TRUE))   // if does not contain any trues, i.e. all false
            messages.add("\nNo successful queries. Check the log file for errors and report to author as necessary.");

        // display that data in a CommuniqueTextDialog
        CommuniqueTextDialog.createMonospacedDialog(frame, "Results",
                String.join("\n", messages), true);

        // Graphical reset
        EventQueue.invokeLater(() -> {
            progressBar.setValue(0); // reset the progress bar
            progressBar.setMaximum(0); // reset progress bar max
            progressLabel.setText("? / ?"); // reset progress label
            if (timer != null) timer.stop(); // null check for timer, stop if stopped
            btnParse.setText("Parse"); // reset parse button
        });
    }

    @Override
    public void importState(CommuniqueConfig config) {
        this.config = config;   // import to state config object

        // manually sync them up
        telegramType.setSelectedItem(config.telegramType);
        specialAction.setSelectedItem(config.processingAction);

        txtClientKey.setText(config.keys.getClientKey());   // set keys
        txtSecretKey.setText(config.keys.getSecretKey());
        txtTelegramId.setText(config.keys.getTelegramId());

        txtWaitTime.setText(config.waitString); // get wait string

        // set text from cRecipients
        txtrCode.setText(CODE_HEADER + String.join("\n", config.getcRecipientsString()));

        LOGGER.info("Communique info imported");
    }

    @Override
    public CommuniqueConfig exportState() {
        LOGGER.info("Communiqué config exported");
        return this.config;
    }

    private void appendCode(Object input) {
        txtrCode.append("\n" + input.toString());
    }

    /**
     * @return currently selected telegram mode
     */
    private JTelegramType currentTelegramType() {
        return telegramType.getItemAt(telegramType.getSelectedIndex());
    }

    /**
     * @return currently selected wait time if present, otherwise, defualt wait time
     */
    private int currentWaitTime() {
        if (txtWaitTime.getText().trim().isEmpty()
                || txtWaitTime.getText().equals("-")
                || txtWaitTime.getText().equalsIgnoreCase("default")
                || txtWaitTime.getText().equalsIgnoreCase("defaults"))
            return currentTelegramType().getWaitTime();

        try {
            return Integer.parseInt(txtWaitTime.getText());
        } catch (NumberFormatException e) {
            String message = String.format("Invalid integer %s", txtWaitTime.getText());
            Communique.this.showMessageDialog(message, CommuniqueConstants.ERROR);
            throw new JTelegramException(message, e);
        }
    }

    /**
     * Shows and initialises the Communique Recruiter.
     * @see com.git.ifly6.communique.ngui.CommuniqueRecruiter
     * @since 6
     */
    private void showRecruiter() {
        if (recruiter == null || !recruiter.isDisplayable()) {
            recruiter = new CommuniqueRecruiter(this);
            recruiter.setConfig(this.exportState());
        } else recruiter.toFront();
    }

    /** {@inheritDoc} */
    @Override
    public void log(String input) {
        LOGGER.info(input);
    }

    /** {@inheritDoc} */
    @Override
    public void sentTo(String recipientName, int x, int length) {

        String recipient = CommuniqueRecipients.createExcludedNation(recipientName).toString();
        txtrCode.append(x == 0 ? "\n\n" + recipient : "\n" + recipient);

        // Progress bar reset code
        if (timer != null) {
            timer.stop();               // stop current timer
            progressBar.setValue(0);    // reset progress bar
        }

        final int ups = 20; // ups = updates per second
        int totalTime = ups * currentWaitTime(); // est delay
        progressBar.setMaximum(totalTime); // max = est delay

        timer = new Timer(1000 / ups, new ActionListener() {
            int elapsedSteps = 0;    // start at zero

            @Override
            public void actionPerformed(ActionEvent ae) {
                progressBar.setValue(elapsedSteps++);    // iterate through
                if (elapsedSteps >= totalTime || sendingThread.isInterrupted()) timer.stop();
            }
        });
        timer.start();

        // Update the label and log successes as relevant
        progressLabel.setText(String.format("%d / %d", x + 1, length));
        rSuccessTracker.put(recipientName, true);

    }

    private List<CommuniqueRecipient> exportRecipients() {
        return Arrays.stream(txtrCode.getText().split("\n"))
                .filter(ApiUtils::isNotEmpty)
                .filter(CommuniqueUtilities.NO_COMMENTS)
                .map(CommuniqueRecipient::parseRecipient)
                .collect(Collectors.toList());
    }

    private void showMessageDialog(String text, String title) {
        JOptionPane.showMessageDialog(frame, text, title, JOptionPane.PLAIN_MESSAGE, null);
    }

    private void showMessageDialog(JLabel label, String title) {
        // new 2020-01-27
        JOptionPane.showMessageDialog(frame, label, title, JOptionPane.PLAIN_MESSAGE, null);
    }

    private String showTextInputDialog(String text, String title) {
        return JOptionPane.showInputDialog(frame, text, title, JOptionPane.PLAIN_MESSAGE);
    }
}

