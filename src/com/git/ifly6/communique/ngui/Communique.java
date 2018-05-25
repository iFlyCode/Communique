/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.ngui;

import com.git.ifly6.communique.CommuniqueUtils;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.data.FilterType;
import com.git.ifly6.communique.data.RecipientType;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.communique.io.CommuniqueProcessingAction;
import com.git.ifly6.communique.io.CommuniqueScraper;
import com.git.ifly6.communique.io.CommuniqueUpdater;
import com.git.ifly6.communique.io.NoResolutionException;
import com.git.ifly6.javatelegram.JTelegramException;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;
import org.apache.commons.io.FilenameUtils;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <code>Communiqué</code> is the main class of the Communiqué system. It handles the GUI aspect of the entire program
 * and other actions.
 */
public class Communique extends AbstractCommunique implements JTelegramLogger {

	static final int COMMAND_KEY = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	private static final Logger LOGGER = Logger.getLogger(Communique.class.getName());

	public static Path appSupport; // Hit open, what directory?
	static FileHandler loggerFileHandler; // Save logs to file

	private CommuniqueConfig config = new CommuniqueConfig();
	private JavaTelegram client; // Sending client
	private Thread sendingThread = new Thread(); // The one sending thread

	private JFrame frame;
	private JTextArea txtrCode;
	private JTextField txtClientKey;
	private JTextField txtSecretKey;
	private JTextField txtTelegramId;
	private JCheckBox chckbxRecruitment;
	private JComboBox<CommuniqueProcessingAction> specialAction;
	private JButton btnParse;

	private List<String> parsedRecipients;
	private Map<String, Boolean> rSuccessTracker;

	private static final String codeHeader =
			"# == Communiqué Recipients Syntax ==\n"
					+ "# Enter recipients, separated by comma or new lines. Please\n"
					+ "# read the readme at \n"
					+ "# [ https://github.com/iflycode/communique#readme ]\n\n";

	private CommuniqueUpdater updater;
	private CommuniqueRecruiter recruiter;

	private JProgressBar progressBar;
	private JLabel progressLabel;
	private Timer timer;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		// Get us a reasonable-looking log format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");

		// Set our look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException lfE) {
			try {
				UIManager.setLookAndFeel(Stream.of(UIManager.getInstalledLookAndFeels())
						.filter(laf -> laf.getName().equals("Nimbus"))
						.findFirst().orElseThrow(ClassNotFoundException::new).getClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				LOGGER.severe("Cannot initialise? Cannot find basic Nimbus look and feel.");
				e.printStackTrace();
			}
			lfE.printStackTrace();
		}

		// Find the application support directory
		if (CommuniqueUtils.IS_OS_WINDOWS) appSupport = Paths.get(System.getenv("LOCALAPPDATA"), "Communique");
		else if (CommuniqueUtils.IS_OS_MAC) {
			appSupport = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Communique");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Communiqué " + Communique7Parser.version);

		} else appSupport = Paths.get(System.getProperty("user.dir"), "config");

		// Create the application support directory
		try {
			Files.createDirectories(appSupport);
		} catch (IOException e1) {
			e1.printStackTrace();
			LOGGER.warning("Cannot create directory");
		}

		// Make sure we can also log to file, apply this to the root logger
		try {
			Path logFile = appSupport.resolve("log").resolve("communique-last-session.log");
			Files.createDirectories(logFile.getParent()); // make directory
			loggerFileHandler = new FileHandler(logFile.toString());
			loggerFileHandler.setFormatter(new SimpleFormatter());
			Logger.getLogger("").addHandler(loggerFileHandler);

		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}

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
	 *
	 * @wbp.parser.entryPoint
	 */
	public Communique() {

		super();

		client = new JavaTelegram(this);
		initialise();

		// Make sure user is connected to the Internet
		try {
			URL nsUrl = new URL("http://www.nationstates.net");
			nsUrl.openConnection().connect();
		} catch (IOException e) {
			this.showMessageDialog("You are not connected to the Internet.\nTo send any telegrams, "
					+ "you must be connected to the Internet.", CommuniqueMessages.ERROR);
		}

		// Check for update, if so, tell the user and prompt
		updater = new CommuniqueUpdater();
		boolean hasNew = updater.shouldRemind();
		if (hasNew) showUpdate();
		LOGGER.info("hasNewUpdate = " + hasNew);

	}

	private void showUpdate() {
		int option = JOptionPane.showConfirmDialog(frame,
				"There is a new version of Communique.\nPress YES to open the Communique downloads page.\n"
						+ "Press NO to never be reminded about this again.\n"
						+ "Press CANCEL to delay for one week.",
				"Communique Update",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.YES_OPTION) try {
			Desktop.getDesktop().browse(new URI(CommuniqueUpdater.LATEST_RELEASE));
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		if (option == JOptionPane.NO_OPTION) updater.stopReminding();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialise() {

		frame = new JFrame();
		if (!CommuniqueUtils.IS_OS_MAC)
			frame.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());

		Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
		double sWidth = screenDimensions.getWidth();
		double sHeight = screenDimensions.getHeight();

		frame.setTitle("Communiqué " + Communique7Parser.version);
		frame.setBounds(100, 100, (int) Math.round(2 * sWidth / 3), (int) Math.round(2 * sHeight / 3));
		frame.setMinimumSize(new Dimension(600, 400));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		frame.setContentPane(contentPane);

		contentPane.setLayout(new BorderLayout(5, 5));

		JPanel dataPanel = new JPanel();
		contentPane.add(dataPanel, BorderLayout.CENTER);

		txtrCode = new JTextArea();
		txtrCode.setText(codeHeader);
		txtrCode.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		txtrCode.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		txtrCode.getDocument().addDocumentListener(new CommuniqueDocumentListener(e -> {
			Communique.this.config.setcRecipients(exportRecipients()); // dynamic update config
		}));
		JScrollPane scrollPane = new JScrollPane(txtrCode);

		progressBar = new JProgressBar();
		progressBar.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		progressLabel = new JLabel("? / ?");

		txtClientKey = new JTextField();
		txtClientKey.setToolTipText("Put your client key here");
		txtClientKey.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		txtClientKey.setText(CommuniqueLoader.getClientKey());
		txtClientKey.getDocument().addDocumentListener(new CommuniqueDocumentListener(event -> {
			config.keys.setClientKey(txtClientKey.getText()); // dynamic update config
		}));

		txtSecretKey = new JTextField();
		txtSecretKey.setToolTipText("Put your telegram's secret key here");
		txtSecretKey.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		txtSecretKey.getDocument().addDocumentListener(new CommuniqueDocumentListener(event -> {
			config.keys.setSecretKey(txtSecretKey.getText()); // dynamic update config
		}));

		txtTelegramId = new JTextField();
		txtTelegramId.setToolTipText("Put your telegram's ID, a long multidigit integer, here");
		txtTelegramId.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		txtTelegramId.getDocument().addDocumentListener(new CommuniqueDocumentListener(event -> {
			config.keys.setTelegramId(txtTelegramId.getText()); // dynamic update config
		}));

		btnParse = new JButton("Parse");
		btnParse.addActionListener(ae -> {

			// Process in the case that the button currently says stop
			if (sendingThread.isAlive() && btnParse.getText().equalsIgnoreCase("Stop")) {
				// kill the thread
				sendingThread.interrupt();
				client.setKillThread(true);
				return;
			}

			Communique7Parser parser = new Communique7Parser();
			List<CommuniqueRecipient> tokens = config.getcRecipients();

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
			try {
				parsedRecipients = parser.apply(tokens).getRecipients();
				if (!Arrays.asList(CommuniqueProcessingAction.values()).contains(config.processingAction)) {
					// deal with invalid processing action
					this.showMessageDialog("Invalid processing action.\n"
							+ "Select a valid processing action", CommuniqueMessages.ERROR);
					return;
				}
				parsedRecipients = config.processingAction.apply(parsedRecipients);

			} catch (JTelegramException jte) {
				LOGGER.log(Level.SEVERE, "Exception in parsing recipients. Displaying to user", jte);
				this.showMessageDialog(jte.getMessage(), CommuniqueMessages.ERROR);
				return;
			}
			LOGGER.info("Recipients Parsed");

			// Change GUI elements
			progressLabel.setText("0 / " + parsedRecipients.size());

			// Run checks
			if (parsedRecipients.size() == 0) {
				Communique.this.showMessageDialog("No recipients specified, cannot send", CommuniqueMessages.ERROR);
				return;
			}

			// Ask for confirmation
			CommuniqueSendDialog sendDialog = new CommuniqueSendDialog(frame, parsedRecipients,
					config.isRecruitment);
			LOGGER.info("Displayed CommuniqueSendDialog");
			LOGGER.info("CommuniqueSendDialog " + (sendDialog.getValue() == 0
					? "cancelled"
					: "accepted with " + sendDialog.getValue()));
			if (sendDialog.getValue() == CommuniqueSendDialog.SEND) send();

		});

		JLabel lblClientKey = new JLabel("Client key");
		JLabel lblSecretKey = new JLabel("Secret key");
		JLabel lblTelegramId = new JLabel("Telegram ID");

		chckbxRecruitment = new JCheckBox("Recruitment ratelimit");
		chckbxRecruitment.setToolTipText("The recruitment ratelimit is 180 seconds per telegram. The ratelimit "
				+ "for all other telegrams is 30 seconds. Communiqué puts in an extra 0.05 seconds as a buffer "
				+ "against latency.");
		chckbxRecruitment.addActionListener(evt -> {
			config.isRecruitment = chckbxRecruitment.isSelected();
			LOGGER.info(String.format("Set config recruitment to %s",
					String.valueOf(chckbxRecruitment.isSelected())));
		});

		specialAction = new JComboBox<>();
		for (CommuniqueProcessingAction action : CommuniqueProcessingAction.values()) // populate enum selector
			specialAction.addItem(action);
		specialAction.setToolTipText("Processing actions can be applied to the list of recipients after they "
				+ "are parsed. Select a processing action here");
		specialAction.addActionListener(evt -> {
			config.processingAction = specialAction.getItemAt(specialAction.getSelectedIndex());
			LOGGER.info(String.format("Set config processing action to %s",
					specialAction.getItemAt(specialAction.getSelectedIndex())));
		});

		JLabel lblProcessingAction = new JLabel("Processing action");

		GroupLayout gl_dataPanel = new GroupLayout(dataPanel);
		gl_dataPanel.setHorizontalGroup(
				gl_dataPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_dataPanel.createSequentialGroup()
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE)
								.addGroup(gl_dataPanel.createParallelGroup(Alignment.TRAILING)
										.addGroup(Alignment.LEADING, gl_dataPanel.createSequentialGroup()
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_dataPanel.createParallelGroup(Alignment.TRAILING)
														.addGroup(gl_dataPanel.createSequentialGroup()
																.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 224,
																		Short.MAX_VALUE)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(progressLabel)
																.addGap(6))
														.addGroup(gl_dataPanel.createSequentialGroup()
																.addGroup(gl_dataPanel.createParallelGroup(Alignment.LEADING)
																		.addComponent(lblSecretKey)
																		.addComponent(lblTelegramId)
																		.addComponent(lblClientKey))
																.addGap(12)
																.addGroup(gl_dataPanel.createParallelGroup(Alignment.LEADING)
																		.addComponent(txtClientKey, GroupLayout.DEFAULT_SIZE,
																				173, Short.MAX_VALUE)
																		.addComponent(txtSecretKey, Alignment.TRAILING,
																				GroupLayout.DEFAULT_SIZE, 173,
																				Short.MAX_VALUE)
																		.addComponent(txtTelegramId,
																				GroupLayout.DEFAULT_SIZE, 173,
																				Short.MAX_VALUE)))
														.addComponent(btnParse, GroupLayout.DEFAULT_SIZE, 261,
																Short.MAX_VALUE)
														.addComponent(chckbxRecruitment, GroupLayout.DEFAULT_SIZE, 261,
																Short.MAX_VALUE)))
										.addGroup(Alignment.LEADING, gl_dataPanel.createSequentialGroup()
												.addGap(14)
												.addComponent(lblProcessingAction)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(specialAction, 0, 161, Short.MAX_VALUE)))
								.addContainerGap()));
		gl_dataPanel.setVerticalGroup(
				gl_dataPanel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_dataPanel.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_dataPanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(txtClientKey, GroupLayout.PREFERRED_SIZE, 29,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lblClientKey))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_dataPanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblSecretKey)
										.addComponent(txtSecretKey, GroupLayout.PREFERRED_SIZE, 29,
												GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_dataPanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblTelegramId)
										.addComponent(txtTelegramId, GroupLayout.PREFERRED_SIZE, 29,
												GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED, 313, Short.MAX_VALUE)
								.addComponent(chckbxRecruitment, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_dataPanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(specialAction, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lblProcessingAction))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnParse)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_dataPanel.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
										.addComponent(progressLabel, GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE))
								.addContainerGap())
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE));
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
				Desktop.getDesktop().open(appSupport.toFile());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		mnFile.add(mntmShowDirectory);

		// Only add the Quit menu item if the OS is not Mac
		if (!CommuniqueUtils.IS_OS_MAC) {
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
					+ "Telegrams API", CommuniqueMessages.TITLE);

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
					+ "when a telegram is sent to 'tag:api'", CommuniqueMessages.ERROR);
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
			String input = this.showTextInputDialog(message, CommuniqueMessages.TITLE);
			if (input != null) {
				input = input.replaceAll("\\(.+?\\)", ""); // get rid of brackets and anything in them
				Stream.of(input.split(","))
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

			if (!CommuniqueUtils.isEmpty(selection)) {
				LOGGER.info("Starting scrape of NS WA voting page, " + selection);
				String[] elements = selection.split(" ");
				String chamber = elements[0].equals("GA") ? CommuniqueScraper.GA : CommuniqueScraper.SC;
				String side = elements[1].equals("For") ? CommuniqueScraper.FOR : CommuniqueScraper.AGAINST;

				try {
					CommuniqueScraper.importAtVoteDelegates(chamber, side).stream()
							.map(CommuniqueRecipient::toString)
							.forEach(this::appendCode);

				} catch (NoResolutionException nre) {
					this.showMessageDialog("No resolution is at vote in that chamber, cannot import data",
							CommuniqueMessages.ERROR);

				} catch (RuntimeException exc) {
					LOGGER.log(Level.WARNING, "Cannot import data.", exc);
					this.showMessageDialog("Cannot import data from NationStates website", CommuniqueMessages.ERROR);
					exc.printStackTrace();
				}
			}

		});
		mnImportRecipients.add(mntmFromAtVote);

		JMenuItem mntmFromTextFile = new JMenuItem("From Text File");
		mntmFromAtVote.setToolTipText("This file should be of the same syntax as that used by the NationStates "
				+ "telegram API or Communique");
		mntmFromTextFile.addActionListener(e -> {
			Path path = showFileChooser(frame, FileDialog.LOAD);
			try {
				Files.lines(path) // attempt load data
						.filter(s -> !s.startsWith("#") || !CommuniqueUtils.isEmpty(s))
						.map(s -> s.split(",")) // split
						.flatMap(Arrays::stream) // map to single stream
						.map(s -> s.toLowerCase().trim().replaceAll(" ", "_")) // process
						.forEach(this::appendCode); // append to text area
			} catch (IOException e1) {
				LOGGER.log(Level.WARNING, "Cannot read file, IOException", e1);
				this.showMessageDialog("Cannot read file at " + path.toString(), CommuniqueMessages.ERROR);
			}
		});
		mnImportRecipients.add(mntmFromTextFile);

		mnEdit.addSeparator();

		JMenuItem mntmAddExcludedNations = new JMenuItem("Add excluded nations");
		mntmAddExcludedNations.setToolTipText("Input comma-separated values");
		mntmAddExcludedNations.addActionListener(e -> {
			String message = "Input nations to exclude as comma-separated list (Do not include trailing 'and'.)";
			String input = this.showTextInputDialog(message, CommuniqueMessages.TITLE);
			if (input != null) {
				input = input.replaceAll("\\(.+?\\)", ""); // get rid of brackets and anything in them
				Stream.of(input.split(","))
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
				e -> CommuniqueTextDialog.createDialog(frame, "About", CommuniqueMessages.acknowledgement));
		mnHelp.add(mntmAbout);

		mnHelp.addSeparator();

		JMenuItem mntmDocumentation = new JMenuItem("Documentation");
		mntmDocumentation.addActionListener(e -> {
			try {
				Desktop.getDesktop().browse(new URL("https://github.com/iFlyCode/Communique").toURI());
			} catch (IOException | URISyntaxException e1) {
				LOGGER.warning("Cannot open Communiqué documentation");
				e1.printStackTrace();
			}
		});
		mnHelp.add(mntmDocumentation);

		JMenuItem mntmForumThread = new JMenuItem("Forum Thread");
		mntmForumThread.addActionListener(e -> {
			try {
				Desktop.getDesktop().browse(new URI("http://forum.nationstates.net/viewtopic.php?f=15&t=352065"));
			} catch (IOException | URISyntaxException e1) {
				LOGGER.warning("Cannot open NationStates forum support thread for Communiqué");
				e1.printStackTrace();
			}
		});
		mnHelp.add(mntmForumThread);

		JMenuItem mntmUpdate = new JMenuItem("Check for Update");
		mntmUpdate.addActionListener((ae) -> {
			if (updater.hasUpdate()) showUpdate();
			else this.showMessageDialog("No new updates", CommuniqueMessages.UPDATER);
		});
		mnHelp.add(mntmUpdate);

		mnHelp.addSeparator();

		JMenuItem mntmLicence = new JMenuItem("Licence");
		mntmLicence.addActionListener(e -> CommuniqueTextDialog.createMonospacedDialog(frame, "Licence",
				CommuniqueMessages.getLicence(), false));
		mnHelp.add(mntmLicence);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				// Save it to application support
				CommuniqueLoader loader = new CommuniqueLoader(appSupport.resolve("autosave.txt"));
				loader.save(exportState());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
		LOGGER.info("Shutdown hook added");
		LOGGER.info("Communiqué loaded");

		// If there is an auto-save, load it
		if (Files.exists(appSupport.resolve("autosave.txt"))) {
			CommuniqueLoader loader = new CommuniqueLoader(appSupport.resolve("autosave.txt"));
			try {
				CommuniqueConfig configuration = loader.load();
				this.importState(configuration);
			} catch (IOException e1) {
				// do nothing if it fails to load
			}
		}
		LOGGER.info("Autosave loaded");

	}

	private List<CommuniqueRecipient> exportRecipients() {
		return Stream.of(txtrCode.getText().split("\n"))
				.filter(s -> !(s.isEmpty() || s.trim().isEmpty()))
				.filter(s -> !s.startsWith("#"))
				.flatMap(s -> Stream.of(s.split(",")))    // flat map the splits
				.map(CommuniqueRecipient::parseRecipient)
				.collect(Collectors.toList());
	}

	private void showMessageDialog(String text, String title) {
		JOptionPane.showMessageDialog(frame, text, title, JOptionPane.PLAIN_MESSAGE, null);
	}

	private String showTextInputDialog(String text, String title) {
		return JOptionPane.showInputDialog(frame, text, title, JOptionPane.PLAIN_MESSAGE);
	}

	@Override
	public CommuniqueConfig exportState() {

		config.defaultVersion();

		LOGGER.info("Communiqué config exported");
		return this.config;

	}

	@Override
	public void importState(CommuniqueConfig config) {

		this.config = config;   // import to state config object

		// manually sync them up
		chckbxRecruitment.setSelected(config.isRecruitment);
		specialAction.setSelectedItem(config.processingAction);

		txtClientKey.setText(config.keys.getClientKey());   // set keys
		txtSecretKey.setText(config.keys.getSecretKey());
		txtTelegramId.setText(config.keys.getTelegramId());

		txtrCode.setText(codeHeader + config.getcRecipientsString().stream()   // set text from cRecipients
				.collect(Collectors.joining("\n")));

		LOGGER.info("Communique info imported");

	}

	private void appendCode(Object input) {
		txtrCode.append("\n" + input.toString());
	}

	/**
	 * Shows and initialises the Communique Recruiter.
	 *
	 * @see com.git.ifly6.communique.ngui.CommuniqueRecruiter
	 * @since 6
	 */
	private void showRecruiter() {
		if (recruiter == null || !recruiter.isDisplayable()) {
			recruiter = new CommuniqueRecruiter(this);
			recruiter.setWithCConfig(this.exportState());
		} else recruiter.toFront();
	}

	/**
	 * Creates an file chooser (in an OS specific manner) and shows it to the user.
	 *
	 * @param parent <code>Frame</code> to show the chooser from
	 * @param type   either <code>FileDialog.SAVE</code> or <code>FileDialog.LOAD</code>
	 * @return the <code>Path</code> selected by the user
	 */
	Path showFileChooser(Frame parent, int type) {

		Path savePath;

		// Due to a problem in Windows and the AWT FileDialog, this will show a JFileChooser on Windows systems.
		if (CommuniqueUtils.IS_OS_MAC) {

			FileDialog fDialog = new FileDialog(parent, "Choose file...", type);
			if (type == FileDialog.SAVE) fDialog.setTitle("Save session as...");
			fDialog.setDirectory(appSupport.toFile().toString());
			fDialog.setVisible(true);

			String fileName = fDialog.getFile();
			if (fileName == null) {
				LOGGER.info("User cancelled file file dialog");
				return null;

			} else savePath = Paths.get(fDialog.getDirectory() == null
					? ""
					: fDialog.getDirectory()).resolve(fDialog.getFile());

		} else {

			JFileChooser fChooser = new JFileChooser(appSupport.toFile());
			fChooser.setDialogTitle("Choose file...");

			int returnVal;
			// returnVal = (type == FileDialog.SAVE) ? fChooser.showSaveDialog(parent) :
			// fChooser.showOpenDialog(parent);
			if (type == FileDialog.SAVE) {
				fChooser.setDialogTitle("Save session as...");
				returnVal = fChooser.showSaveDialog(parent);
			} else returnVal = fChooser.showOpenDialog(parent);
			fChooser.setVisible(true);

			if (returnVal == JFileChooser.APPROVE_OPTION) savePath = fChooser.getSelectedFile().toPath();
			else return null;

		}

		// Make it end in txt if saving
		if (type == FileDialog.SAVE) if (!FilenameUtils.getExtension(savePath.toString()).equals("txt")) {
			LOGGER.info("Append txt to savePath");
			savePath = savePath.resolveSibling(savePath.getFileName() + ".txt");
		}

		LOGGER.info("User elected to " + (type == FileDialog.SAVE ? "save" : "load") + " file at "
				+ savePath.toAbsolutePath().toString());
		return savePath;
	}

	/**
	 * Sending thread. It executes all of these commands in the <code>Runner</code> thread and then calls the
	 * completion method.
	 */
	private void send() {

		// sending logic
		if (!sendingThread.isAlive()) {
			client.setKillThread(false);
			Runnable runner = () -> {

				client.setRecipients(parsedRecipients);    // Set recipients
				client.setKeys(new JTelegramKeys(txtClientKey.getText(), txtSecretKey.getText(), txtTelegramId.getText()));

				// Set recruitment Status, JavaTelegram defaults to true
				client.setRecruitment(chckbxRecruitment.isSelected());

				// Save client key
				try {
					CommuniqueLoader.writeProperties(txtClientKey.getText());
					CommuniqueLoader loader = new CommuniqueLoader(appSupport.resolve("autosave.txt"));
					loader.save(exportState());

				} catch (IOException e) {
					LOGGER.severe("Exception in writing autosave or properties file");
					e.printStackTrace();
				}

				// Create tracker, initialise success tracking HashMap
				rSuccessTracker = new LinkedHashMap<>();
				parsedRecipients.forEach(r -> rSuccessTracker.put(r, false));
				if (rSuccessTracker == null) LOGGER.severe("Success tracker is null");

				try {
					client.connect();
				} catch (JTelegramException jte) {  // JTE occurring during send?
					LOGGER.log(Level.SEVERE, "JTelegramException in send", jte);
					Communique.this.showMessageDialog(jte.getMessage(), CommuniqueMessages.ERROR);
					return;
				}

				completeSend();
			};

			sendingThread = new Thread(runner);
			sendingThread.start();

			btnParse.setText("Stop");
		}
	}

	/**
	 * @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String)
	 */
	@Override
	public void log(String input) {
		LOGGER.info(input);
	}

	/**
	 * @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int)
	 */
	@Override
	public void sentTo(String recipient, int x, int length) {

		recipient = CommuniqueRecipients.createExcludedNation(recipient).toString();
		txtrCode.append(x == 0 ? "\n\n" + recipient : "\n" + recipient);

		// Progress bar reset code
		if (timer != null) {
			timer.stop();               // stop current timer
			progressBar.setValue(0);    // reset progress bar
		}

		final int ups = 20;    // ups = updates per second
		int totalTime = ups * (chckbxRecruitment.isSelected() ? 180 : 30);    // est delay
		progressBar.setMaximum(totalTime);    // max = est delay

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
		rSuccessTracker.put(recipient, true);

	}

	/**
	 * Cleanup commands to be done when sending is complete.
	 */
	private void completeSend() {
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
				messages.stream().collect(Collectors.joining("\n")), true);

		// Graphical reset
		EventQueue.invokeLater(() -> {
			progressBar.setValue(0); // reset the progress bar
			progressBar.setMaximum(0); // reset progress bar max
			progressLabel.setText("? / ?"); // reset progress label
			if (timer != null) timer.stop(); // null check for timer, stop if stopped
			btnParse.setText("Parse"); // reset parse button
		});
	}
}

class CommuniqueDocumentListener implements DocumentListener {

	private Consumer<DocumentEvent> consumer;

	CommuniqueDocumentListener(Consumer<DocumentEvent> consumer) {
		this.consumer = consumer;
	}

	@Override
	public void changedUpdate(DocumentEvent event) {
		consumer.accept(event);
	}

	@Override
	public void insertUpdate(DocumentEvent event) {
		consumer.accept(event);
	}

	@Override
	public void removeUpdate(DocumentEvent event) {
		consumer.accept(event);
	}

}