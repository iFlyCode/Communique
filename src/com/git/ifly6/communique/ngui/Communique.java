/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.ngui;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.CommuniqueUtils;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.data.RecipientType;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueConnector;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.communique.io.CommuniqueUpdater;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;
import com.git.ifly6.javatelegram.util.JTelegramException;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
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
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
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
	private static final Logger log = Logger.getLogger(Communique.class.getName());

	public static Path appSupport;
	static FileHandler loggerFileHandler;

	private boolean parsed = false;
	private JavaTelegram client;
	private Thread sendingThread = new Thread();

	private JFrame frame;
	private JTextField txtClientKey;
	private JTextField txtSecretKey;
	private JTextField txtTelegramId;
	private JButton btnSend;
	private JTextArea txtrCode;
	private JCheckBoxMenuItem chckbxmntmRandomiseRecipients;
	private JCheckBox chckbxRecruitment;

	private List<String> parsedRecipients;

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

	/** Launch the application. */
	public static void main(String[] args) {

		// Get us a reasonable-looking log format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");

		// Set our look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException lfE) {
			try {
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
			lfE.printStackTrace();
		}

		// Find the application support directory
		if (CommuniqueUtils.IS_OS_WINDOWS) {
			appSupport = Paths.get(System.getenv("LOCALAPPDATA"), "Communique");

		} else if (CommuniqueUtils.IS_OS_MAC) {
			appSupport = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Communique");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Communiqué " + Communique7Parser.version);

		} else {
			appSupport = Paths.get(System.getProperty("user.dir"), "config");
		}

		// Create the application support directory
		try {
			Files.createDirectories(appSupport);
		} catch (IOException e1) {
			e1.printStackTrace();
			log.warning("Cannot create directory");
		}

		// Make sure we can also log to file, apply this to the root logger
		try {
			loggerFileHandler = new FileHandler(appSupport.resolve("last-session-log.log").toString());
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
	 * @wbp.parser.entryPoint
	 */
	public Communique() {

		super();

		client = new JavaTelegram(this);
		initialise();

		// Check for update, if so, tell the user and prompt
		updater = new CommuniqueUpdater();
		boolean hasNew = updater.shouldRemind();
		if (hasNew) {
			showUpdate();
		}
		log.info("hasNewUpdate = " + hasNew);

	}

	private void showUpdate() throws HeadlessException {
		int option = JOptionPane.showConfirmDialog(frame,
				"There is a new version of Communique.\nPress YES to open the Communique downloads page.\n"
						+ "Press NO to never be reminded about this again.\n"
						+ "Press CANCEL to delay for one week.",
				"Communique Update",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.YES_OPTION) {
			try {
				Desktop.getDesktop().browse(new URI(CommuniqueUpdater.LATEST_RELEASE));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
		if (option == JOptionPane.NO_OPTION) {
			updater.setContinueReminding(false);
		}
	}

	/** Initialise the contents of the frame. */
	private void initialise() {

		frame = new JFrame();
		if (!CommuniqueUtils.IS_OS_MAC) {
			frame.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
		}

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

		JPanel controlPanel = new JPanel();
		contentPane.add(controlPanel, BorderLayout.SOUTH);

		txtClientKey = new JTextField();
		txtClientKey.setToolTipText("Client key");
		txtClientKey.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		txtClientKey.setText(CommuniqueLoader.getClientKey());

		txtSecretKey = new JTextField();
		txtSecretKey.setToolTipText("Secret key");
		txtSecretKey.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		txtSecretKey.setText("Secret Key");

		txtTelegramId = new JTextField();
		txtTelegramId.setToolTipText("Telegram ID");
		txtTelegramId.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		txtTelegramId.setText("Telegram ID");

		btnSend = new JButton("Parse");

		chckbxRecruitment = new JCheckBox("Recruit Ratelimit");
		chckbxRecruitment.addActionListener(l -> triggerParsed(false));

		GroupLayout gl_controlPanel = new GroupLayout(controlPanel);
		gl_controlPanel.setHorizontalGroup(
				gl_controlPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_controlPanel.createSequentialGroup()
								.addComponent(txtClientKey, GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
								.addComponent(txtSecretKey, GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
								.addComponent(txtTelegramId, GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
								.addComponent(chckbxRecruitment, GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
								.addGap(0)));
		gl_controlPanel.setVerticalGroup(
				gl_controlPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(txtClientKey, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtSecretKey, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtTelegramId, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_controlPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(chckbxRecruitment, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnSend)));
		controlPanel.setLayout(gl_controlPanel);

		JPanel metaDataPanel = new JPanel();
		metaDataPanel.setLayout(new BorderLayout());
		contentPane.add(metaDataPanel, BorderLayout.CENTER);

		JPanel dataPanel = new JPanel();
		metaDataPanel.add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new GridLayout(1, 0, 5, 5));

		txtrCode = new JTextArea();
		txtrCode.setText(codeHeader);
		txtrCode.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		txtrCode.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dataPanel.add(new JScrollPane(txtrCode));

		// Document listener logic
		txtrCode.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				executeInTheKingdomOfNouns();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				executeInTheKingdomOfNouns();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			// It's a joke, look here: http://steve-yegge.blogspot.com/2006/03/execution-in-kingdom-of-nouns.html
			private void executeInTheKingdomOfNouns() {
				if (parsed) {    // to avoid constant resetting of the variable
					triggerParsed(false);
				}
			}
		});

		JPanel recipientsPanel = new JPanel();
		dataPanel.add(recipientsPanel);
		recipientsPanel.setLayout(new BorderLayout(0, 0));

		JTextArea txtrRecipients = new JTextArea();
		txtrRecipients.setEditable(false);
		txtrRecipients.setText("# == Communiqué Recipients ==\n# This tab shows all recipients after parsing of the "
				+ "Code tab.\n\n");
		txtrRecipients.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		txtrRecipients.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		UndoManager undoManager = new UndoManager();
		txtrRecipients.getDocument().addUndoableEditListener(undoManager);

		recipientsPanel.add(new JScrollPane(txtrRecipients), BorderLayout.CENTER);

		JPanel progressPanel = new JPanel();
		progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		metaDataPanel.add(progressPanel, BorderLayout.SOUTH);
		GridBagLayout gbl_progressPanel = new GridBagLayout();
		gbl_progressPanel.columnWidths = new int[]{0, 0, 0};
		gbl_progressPanel.rowHeights = new int[]{0, 0};
		gbl_progressPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_progressPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		progressPanel.setLayout(gbl_progressPanel);

		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 0, 5);
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 0;
		progressPanel.add(progressBar, gbc_progressBar);

		progressLabel = new JLabel("? / ?");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.gridx = 1;
		gbc_label.gridy = 0;
		progressPanel.add(progressLabel, gbc_label);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, COMMAND_KEY));
		mntmSave.addActionListener(e -> {
			Path savePath = showFileChooser(frame, FileDialog.SAVE);

			if (savePath == null) {
				log.info("Returned path was null.");
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
				log.info("Returned path was null.");
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
		mntmShowDirectory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, COMMAND_KEY | InputEvent.SHIFT_MASK));
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

		JMenu mnData = new JMenu("Data");
		menuBar.add(mnData);

		JMenuItem mntmUndo = new JMenuItem("Undo");
		mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, COMMAND_KEY));
		mntmUndo.addActionListener(e -> {
			if (undoManager.canUndo()) undoManager.undo();
		});
		mnData.add(mntmUndo);

		JMenuItem mntmRedo = new JMenuItem("Redo");
		mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, COMMAND_KEY | InputEvent.SHIFT_MASK));
		mntmRedo.addActionListener(e -> {
			if (undoManager.canRedo()) undoManager.redo();
		});
		mnData.add(mntmRedo);

		JMenuItem mntmImportKeysFrom = new JMenuItem("Import Keys from Telegram URL");
		mntmImportKeysFrom.addActionListener(e -> {

			String rawURL = this.showTextInputDialog("Paste in keys from the URL provided by receipt by the Telegrams API",
					CommuniqueMessages.TITLE);

			// Verify that it is a valid NationStates URL
			String rawUrlStart = "https://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&";
			if (rawURL.startsWith(rawUrlStart)) {

				rawURL = rawURL.replace(rawUrlStart, "");
				rawURL = rawURL.replace("&to=NATION_NAME", "");

				String[] shards = rawURL.split("&");
				if (shards.length == 2) {
					txtTelegramId.setText(shards[0].substring(shards[0].indexOf("=") + 1, shards[0].length()));
					txtSecretKey.setText(shards[1].substring(shards[1].indexOf("=") + 1, shards[1].length()));
				}

			} else {
				String message = "Please input a properly formatted NationStates URL\nin the form displayed when a "
						+ "telegram is sent to 'tag:api'";
				this.showMessageDialog(message, CommuniqueMessages.ERROR);
			}
		});
		mnData.add(mntmImportKeysFrom);

		JMenu mnImportRecipients = new JMenu("Import Recipients");
		mnData.add(mnImportRecipients);

		JMenuItem mntmFromWaDelegate = new JMenuItem("From WA Delegate List");
		mntmFromWaDelegate.addActionListener(e -> appendCode(CommuniqueRecipient.DELEGATES));

		JMenuItem mntmAsCommaSeparated = new JMenuItem("As Comma Separated List");
		mntmAsCommaSeparated.addActionListener(e -> {
			String message = "Input a string of delegates, as found on a list of delegates\nin one of the "
					+ "NationStates World Assembly pages:";
			String input = this.showTextInputDialog(message, CommuniqueMessages.TITLE);
			if (input != null) {
				input = input.replaceAll("\\(.+?\\)", "");
				Stream.of(input.split(","))
						.map(CommuniqueRecipients::createNation)
						.map(CommuniqueRecipient::toString)
						.forEach(this::appendCode);
			}
		});
		mnImportRecipients.add(mntmAsCommaSeparated);
		mnImportRecipients.add(mntmFromWaDelegate);

		JMenuItem mntmFromAtVote = new JMenuItem("From At Vote Screen");
		mntmFromAtVote.addActionListener(e -> {

			Object[] possibilities = {"GA For", "GA Against", "SC For", "SC Against"};
			String output = (String) JOptionPane.showInputDialog(frame, "Select which chamber and side you want to address:",
					"Select Chamber and Side", JOptionPane.PLAIN_MESSAGE, null, possibilities, "GA For");

			if (!CommuniqueUtils.isEmpty(output)) {
				String[] elements = output.split(" ");
				if (elements.length == 2) {

					String chamber = elements[0].equals("GA") ? CommuniqueConnector.GA : CommuniqueConnector.SC;
					String side = elements[1].equals("For") ? CommuniqueConnector.FOR : CommuniqueConnector.AGAINST;

					try {
						CommuniqueConnector.importAtVoteDelegates(chamber, side).stream()
								.map(CommuniqueRecipient::toString)
								.forEach(this::appendCode);
					} catch (RuntimeException exc) {
						this.showMessageDialog("Cannot import data from NationStates website.", CommuniqueMessages.ERROR);
						log.warning("Cannot import data. Message:");
						exc.printStackTrace();
					}

				}
			}

		});
		mnImportRecipients.add(mntmFromAtVote);

		JMenuItem mntmFromTextFile = new JMenuItem("From Text File");
		mntmFromTextFile.addActionListener(e -> {
			Path path = showFileChooser(frame, FileDialog.LOAD);
			try {

				// load data
				List<String> fileContents = Files.lines(path)
						.filter(s -> !s.startsWith("#") || !CommuniqueUtils.isEmpty(s))
						.collect(Collectors.toList());

				// collate the data
				List<String> recipients = new ArrayList<>();
				for (String element : fileContents) {
					// If there are commas in there, split them out
					String[] elements = element.split(",");
					for (String recipient : elements) {
						recipients.add(recipient.toLowerCase().trim().replaceAll(" ", "_"));
					}
				}

				// add it to the list
				Communique7Parser.translateTokens(recipients).forEach(this::appendCode);

			} catch (IOException e1) {
				// throw an error message
				this.showMessageDialog("Cannot load file at " + path.toString(), CommuniqueMessages.ERROR);
			}
		});
		mnImportRecipients.add(mntmFromTextFile);

		mnData.addSeparator();

		// TODO Create a system to help people write their configuration files.
		// JMenuItem mntmFilter = new JMenuItem("Filter");
		// mntmFilter.setAccelerator(getOSKeyStroke(KeyEvent.VK_F));
		// mntmFilter.addActionListener(e -> {
		//
		// });
		// mnData.add(mntmFilter);
		//
		// mnData.addSeparator();

		chckbxmntmRandomiseRecipients = new JCheckBoxMenuItem("Randomise Recipients");
		mnData.add(chckbxmntmRandomiseRecipients);

		JMenu mnWindow = new JMenu("Window");
		menuBar.add(mnWindow);

		JMenuItem mntmMinimise = new JMenuItem("Minimise");
		mntmMinimise.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, COMMAND_KEY));
		mntmMinimise.addActionListener(e -> {
			if (frame.getState() == Frame.NORMAL) {
				frame.setState(Frame.ICONIFIED);
			}
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
		mntmAbout.addActionListener(e -> new CommuniqueTextDialog(frame, "About", CommuniqueMessages.acknowledgement));
		mnHelp.add(mntmAbout);

		mnHelp.addSeparator();

		JMenuItem mntmDocumentation = new JMenuItem("Documentation");
		mntmDocumentation.addActionListener(e -> {
			try {
				Desktop.getDesktop().browse(new URL("https://github.com/iFlyCode/Communique#communiqué").toURI());
			} catch (IOException | URISyntaxException e1) {
				log.warning("Cannot open Communiqué documentation.");
				e1.printStackTrace();
			}
		});
		mnHelp.add(mntmDocumentation);

		JMenuItem mntmForumThread = new JMenuItem("Forum Thread");
		mntmForumThread.addActionListener(e -> {
			try {
				Desktop.getDesktop().browse(new URI("http://forum.nationstates.net/viewtopic.php?f=15&t=352065"));
			} catch (IOException | URISyntaxException e1) {
				log.warning("Cannot open NationStates forum support thread for Communiqué.");
				e1.printStackTrace();
			}
		});
		mnHelp.add(mntmForumThread);

		JMenuItem mntmUpdate = new JMenuItem("Check for Update");
		mntmUpdate.addActionListener((ae) -> {
			if (updater.hasUpdate()) {
				showUpdate();
			} else {
				this.showMessageDialog("No new updates.", CommuniqueMessages.UPDATER);
			}
		});
		mnHelp.add(mntmUpdate);

		mnHelp.addSeparator();

		JMenuItem mntmLicence = new JMenuItem("Licence");
		mntmLicence.addActionListener(e -> new CommuniqueTextDialog(frame, "Licence", CommuniqueMessages.licence));
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
		log.info("Shutdown hook added.");

		// Parse action for above
		btnSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Communique7Parser parser = new Communique7Parser();
				List<CommuniqueRecipient> tokens = exportRecipients();

				// Check if a recruit-flag has been used.
				boolean rfPresent = tokens.stream()
						.filter(t -> t.getRecipientType() == RecipientType.FLAG)
						.anyMatch(t -> t.getName().equals("recruit"));
				if (rfPresent) {
					showRecruiter();
					return;
				}

				if (!parsed) {

					// Call and do the parsing
					log.info("Called parser");

					try {
						Communique.this.parsedRecipients = parser.apply(tokens).getRecipients();    // apply
					} catch (JTelegramException jte) {
						Communique.this.showMessageDialog(jte.getMessage(), CommuniqueMessages.ERROR);
						return;
					}

					// Estimate Time Needed
					String timeNeeded = estimateTime(parsedRecipients.size(), chckbxRecruitment.isSelected());

					// Show Recipients
					List<String> tx = new ArrayList<>();
					tx.add("# == Communiqué Recipients ==");
					tx.add("# This tab shows all " + parsedRecipients.size() + " recipients after parsing of the Code");
					tx.add("# tab. Estimated time needed is " + timeNeeded + "\n");
					parsedRecipients.forEach(tx::add);

					log.info("Recipients Parsed.");

					// Change GUI elements
					txtrRecipients.setText(tx.stream().collect(Collectors.joining("\n")));
					progressLabel.setText("0 / " + parsedRecipients.size());
					triggerParsed(true);

				} else {

					if (parsedRecipients.size() == 0) {
						Communique.this.showMessageDialog("No recipients specified, cannot send.", CommuniqueMessages.ERROR);
						return;
					}

					if (sendingThread.isAlive() && btnSend.getText().equalsIgnoreCase("Stop")) {
						sendingThread.interrupt();
						client.setKillThread(true);
					}

					send();
				}
			}

			private String estimateTime(int count, boolean isRecruitment) {
				int seconds = (int) Math.round(count * (isRecruitment ? 180.05 : 30.05));
				return CommuniqueUtilities.time(seconds);
			}
		});

		log.info("Communiqué loaded.");

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

		log.info("Autosave loaded.");

	}

	private List<CommuniqueRecipient> exportRecipients() {
		List<CommuniqueRecipient> tokens = new ArrayList<>();
		Stream.of(txtrCode.getText().split("\n"))
				.filter(s -> !(s.isEmpty() || s.trim().isEmpty()))
				.filter(s -> !s.startsWith("#"))
				.forEach(s -> Stream.of(s.split(","))      // decompose and parse
						.map(CommuniqueRecipient::parseRecipient)
						.forEach(tokens::add));
		return tokens;
	}

	private void showMessageDialog(String text, String title) {
		JOptionPane.showMessageDialog(frame, text, title, JOptionPane.PLAIN_MESSAGE, null);
	}

	private String showTextInputDialog(String text, String title) {
		return JOptionPane.showInputDialog(frame, text, title, JOptionPane.PLAIN_MESSAGE);
	}

	@Override
	public CommuniqueConfig exportState() {

		CommuniqueConfig config = new CommuniqueConfig();

		config.isRecruitment = chckbxRecruitment.isSelected();
		config.isRandomised = chckbxmntmRandomiseRecipients.isSelected();
		config.keys = new JTelegramKeys(txtClientKey.getText(), txtSecretKey.getText(), txtTelegramId.getText());

		config.setcRecipients(exportRecipients());
		// exclude update of the old String[]'s

		config.defaultVersion();

		log.info("Communiqué config exported.");
		return config;

	}

	@Override
	public void importState(CommuniqueConfig config) {

		chckbxRecruitment.setSelected(config.isRecruitment);
		chckbxmntmRandomiseRecipients.setSelected(config.isRandomised);

		txtClientKey.setText(config.keys.getClientKey());   // set keys
		txtSecretKey.setText(config.keys.getSecretKey());
		txtTelegramId.setText(config.keys.getTelegramId());

		txtrCode.setText(codeHeader + config.getcRecipientsString().stream()   // set text from cRecipients
				.collect(Collectors.joining("\n")));

		log.info("Communique info imported");
	}

	private void appendCode(Object input) {
		txtrCode.append("\n" + input.toString());
	}

	// Changes the state of the button to reflect whether it is ready to send and or parsed
	private void triggerParsed(boolean parsed) {
		this.parsed = parsed;
		btnSend.setText(parsed ? "Send" : "Parse");
	}

	/** @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String) */
	@Override
	public void log(String input) {
		log.info(input);
	}

	/** @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
	@Override
	public void sentTo(String recipient, int x, int length) {

		recipient = CommuniqueRecipients.createExcludedNation(recipient).toString();
		txtrCode.append(x == 0 ? "\n\n" + recipient : "\n" + recipient);

		if (timer != null) {
			timer.stop();                // stop current timer
			progressBar.setValue(0);    // reset progress bar
		}

		final int ups = 60;    // ups = updates per second
		int totalTime = ups * (chckbxRecruitment.isSelected() ? 180 : 30);    // est delay
		progressBar.setMaximum(totalTime);    // max = est delay

		timer = new Timer(1000 / ups, new ActionListener() {
			int elapsedSteps = 0;    // start at zero
			@Override
			public void actionPerformed(ActionEvent ae) {
				progressBar.setValue(elapsedSteps++);    // iterate through
				if (elapsedSteps >= totalTime || sendingThread.isInterrupted()) {
					timer.stop();
				}
			}
		});
		timer.start();

		// Label update
		progressLabel.setText(String.format("%d / %d", x + 1, length));

	}

	/**
	 * Shows and initialises the Communique Recruiter.
	 * @see com.git.ifly6.communique.ngui.CommuniqueRecruiter
	 * @since 6
	 */
	private void showRecruiter() {
		if (recruiter == null || !recruiter.isDisplayable()) {
			recruiter = new CommuniqueRecruiter(this);
			recruiter.setWithCConfig(this.exportState());
		} else {
			recruiter.toFront();
		}
	}

	/**
	 * Creates an file chooser (in an OS specific manner) and shows it to the user.
	 * @param parent <code>Frame</code> to show the chooser from
	 * @param type   either <code>FileDialog.SAVE</code> or <code>FileDialog.LOAD</code>
	 * @return the <code>Path</code> selected by the user
	 */
	Path showFileChooser(Frame parent, int type) {

		Path savePath;

		// Due to a problem in Windows and the AWT FileDialog, this will show a JFileChooser on Windows systems.
		if (CommuniqueUtils.IS_OS_MAC) {

			FileDialog fDialog = new FileDialog(parent, "Choose file...", type);
			if (type == FileDialog.SAVE) {
				fDialog.setTitle("Save session as...");
			}
			fDialog.setDirectory(appSupport.toFile().toString());
			fDialog.setVisible(true);

			String fileName = fDialog.getFile();
			if (fileName == null) {
				log.info("User cancelled file file dialog");
				return null;

			} else {
				savePath = Paths.get(fDialog.getDirectory() == null
						? ""
						: fDialog.getDirectory()).resolve(fDialog.getFile());
			}

		} else {

			JFileChooser fChooser = new JFileChooser(appSupport.toFile());
			fChooser.setDialogTitle("Choose file...");

			int returnVal;
			// returnVal = (type == FileDialog.SAVE) ? fChooser.showSaveDialog(parent) :
			// fChooser.showOpenDialog(parent);
			if (type == FileDialog.SAVE) {
				fChooser.setDialogTitle("Save session as...");
				returnVal = fChooser.showSaveDialog(parent);
			} else {
				returnVal = fChooser.showOpenDialog(parent);
			}
			fChooser.setVisible(true);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				savePath = fChooser.getSelectedFile().toPath();
			} else {
				return null;
			}

		}

		// Make it end in txt if saving
		if (type == FileDialog.SAVE) {
			if (!FilenameUtils.getExtension(savePath.toString()).equals("txt")) {
				log.info("Append txt to savePath");
				savePath = savePath.resolveSibling(savePath.getFileName() + ".txt");
			}
		}

		log.info("User elected to " + (type == FileDialog.SAVE ? "save" : "load") + " file at "
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

				if (chckbxmntmRandomiseRecipients.isSelected()) {
					Collections.shuffle(parsedRecipients);
				}

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
					log.severe("Exception in writing autosave or properties file.");
				}

				client.connect();
				completeSend();

			};

			sendingThread = new Thread(runner);
			sendingThread.start();

			btnSend.setText("Stop");

		} else {
			log.info("There is already a campaign running. Terminate that campaign and then retry.");
		}
	}

	/** Cleanup commands to be done when sending is complete. */
	private void completeSend() {

		log.info("Queries Complete.");
		this.showMessageDialog("Queries to " + parsedRecipients.size() + " nations complete.", CommuniqueMessages.TITLE);

		// Reset the progress bar
		progressBar.setValue(0);
		progressBar.setMaximum(0);
		timer.stop();

	}
}
