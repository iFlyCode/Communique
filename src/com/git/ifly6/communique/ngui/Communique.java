/* Copyright (c) 2016 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.git.ifly6.communique.ngui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.git.ifly6.communique.CommuniqueParser;
import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.io.CConfig;
import com.git.ifly6.communique.io.CLoader;
import com.git.ifly6.communique.io.CNetLoader;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;

/** <code>Communiqué</code> is the main class of the Communiqué system. It handles the GUI aspect of the entire program
 * and other actions. */
public class Communique extends AbstractCommunique implements JTelegramLogger {
	
	private static final Logger log = Logger.getLogger(Communique.class.getName());
	
	private boolean parsed = false;
	private JavaTelegram client;
	private Thread sendingThread = new Thread();
	
	private JFrame frame;
	private JPanel recipientsPanel;
	
	private JTextField txtClientKey;
	private JTextField txtSecretKey;
	private JTextField txtTelegramId;
	
	private JButton btnSend;
	private JProgressBar progressBar;
	private JTextArea txtrCode;
	
	private JCheckBoxMenuItem chckbxmntmRandomiseRecipients;
	private JCheckBox chckbxRecruitment;
	
	private String[] parsedRecipients;
	
	public static Path appSupport;
	
	private static String codeHeader = "# == Communiqué Recipients Code ==\n"
			+ "# Enter recipients, one for each line or use 'region:', 'WA:',\n"
			+ "# etc tags. Use '/' to say: 'not'. Ex: 'region:europe',\n"
			+ "# '/imperium anglorum'. Use 'flag:recruit' to open the \n" + "# recruiter. \n\n";
	
	private CommuniqueRecruiter recruiter;
	
	/** Launch the application. */
	public static void main(String[] args) {
		
		try {
			
			// Set system look and feel.
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException lfE) {
			
			// If not possible, set the cross-platform look and feel.
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
			lfE.printStackTrace();
			
		}
		
		if (SystemUtils.IS_OS_WINDOWS) {
			appSupport = Paths.get(System.getenv("LOCALAPPDATA"), "Communique");
			
		} else if (SystemUtils.IS_OS_MAC) {
			appSupport = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Communique");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Communiqué " + CommuniqueParser.version);
			
		} else {
			appSupport = Paths.get(System.getProperty("user.dir"), "config");
		}
		
		try {
			Files.createDirectories(appSupport);
		} catch (IOException e1) {
			e1.printStackTrace();
			log.warning("Cannot create directory");
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
	
	/** Create the application.
	 *
	 * @wbp.parser.entryPoint */
	public Communique() {
		
		client = new JavaTelegram(this);
		initialise();
		
	}
	
	/** Initialise the contents of the frame. */
	private void initialise() {
		
		frame = new JFrame();
		if (!SystemUtils.IS_OS_MAC) {
			frame.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
		}
		
		Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
		double sWidth = screenDimensions.getWidth();
		double sHeight = screenDimensions.getHeight();
		
		frame.setTitle("Communiqué " + CommuniqueParser.version);
		frame.setBounds(100, 100, (int) Math.round(2 * sWidth / 3), (int) Math.round(2 * sHeight / 3));
		frame.setMinimumSize(new Dimension(600, 400));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		frame.setContentPane(contentPane);
		
		contentPane.setLayout(new BorderLayout(5, 5));
		
		JPanel controlPanel = new JPanel();
		contentPane.add(controlPanel, BorderLayout.SOUTH);
		controlPanel.setLayout(new GridLayout(1, 0, 0, 0));
		
		txtClientKey = new JTextField();
		txtClientKey.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtClientKey.setText(CLoader.readProperties());
		txtClientKey.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				((JTextField) e.getComponent()).setText("");
				triggerParsed(false);
			}
		});
		controlPanel.add(txtClientKey);
		txtClientKey.setColumns(10);
		
		txtSecretKey = new JTextField();
		txtSecretKey.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtSecretKey.setText("Secret Key");
		txtSecretKey.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				((JTextField) e.getComponent()).setText("");
				triggerParsed(false);
			}
		});
		controlPanel.add(txtSecretKey);
		txtSecretKey.setColumns(10);
		
		txtTelegramId = new JTextField();
		txtTelegramId.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtTelegramId.setText("Telegram ID");
		txtTelegramId.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				((JTextField) e.getComponent()).setText("");
				triggerParsed(false);
			}
		});
		controlPanel.add(txtTelegramId);
		txtTelegramId.setColumns(10);
		
		btnSend = new JButton("Parse");
		
		chckbxRecruitment = new JCheckBox("Recruit Ratelimit");
		chckbxRecruitment.addActionListener(l -> {
			triggerParsed(false);
		});
		controlPanel.add(chckbxRecruitment);
		controlPanel.add(btnSend);
		
		JPanel metaDataPanel = new JPanel();
		metaDataPanel.setLayout(new BorderLayout());
		contentPane.add(metaDataPanel, BorderLayout.CENTER);
		
		JPanel dataPanel = new JPanel();
		metaDataPanel.add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new GridLayout(1, 0, 5, 5));
		
		txtrCode = new JTextArea();
		txtrCode.setText(codeHeader);
		txtrCode.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtrCode.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dataPanel.add(new JScrollPane(txtrCode));
		
		// Document listener logic
		txtrCode.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override public void removeUpdate(DocumentEvent e) {
				executeInTheKingdomOfNouns();
			}
			
			@Override public void insertUpdate(DocumentEvent e) {
				executeInTheKingdomOfNouns();
			}
			
			@Override public void changedUpdate(DocumentEvent e) {
			}
			
			// It's a joke, look here: http://steve-yegge.blogspot.com/2006/03/execution-in-kingdom-of-nouns.html
			public void executeInTheKingdomOfNouns() {
				if (parsed) {	// to avoid constant resetting of the variable
					triggerParsed(false);
				}
			}
		});
		
		recipientsPanel = new JPanel();
		dataPanel.add(recipientsPanel);
		recipientsPanel.setLayout(new BorderLayout(0, 0));
		
		JTextArea txtrRecipients = new JTextArea();
		txtrRecipients.setText(
				"# == Communiqué Recipients ==\n# This tab shows all recipients after parsing of the Code tab.\n\n");
		txtrRecipients.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtrRecipients.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		recipientsPanel.add(new JScrollPane(txtrRecipients), BorderLayout.CENTER);
		
		progressBar = new JProgressBar();
		progressBar.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		metaDataPanel.add(progressBar, BorderLayout.SOUTH);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.setAccelerator(getOSKeyStroke(KeyEvent.VK_S));
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
		mntmOpen.setAccelerator(getOSKeyStroke(KeyEvent.VK_O));
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
		mntmClose.setAccelerator(getOSKeyStroke(KeyEvent.VK_W));
		mntmClose.addActionListener(e -> {
			frame.setVisible(false);
			frame.dispose();
			System.exit(0);
		});
		mnFile.add(mntmClose);
		
		mnFile.addSeparator();
		
		JMenuItem mntmShowDirectory = new JMenuItem("Show Directory");
		mntmShowDirectory.setAccelerator(getOSKeyStroke(KeyEvent.VK_O, true));
		mntmShowDirectory.addActionListener(e -> {
			try {
				Desktop.getDesktop().open(appSupport.toFile());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		mnFile.add(mntmShowDirectory);
		
		// Only add the Quit menu item if the OS is not Mac
		if (!SystemUtils.IS_OS_MAC) {
			mnFile.addSeparator();
			JMenuItem mntmExit = new JMenuItem("Exit");
			mntmExit.setAccelerator(getOSKeyStroke(KeyEvent.VK_Q));
			mntmExit.addActionListener(e -> System.exit(0));
			mnFile.add(mntmExit);
		}
		
		JMenu mnData = new JMenu("Data");
		menuBar.add(mnData);
		
		JMenuItem mntmImportKeysFrom = new JMenuItem("Import Keys from Telegram URL");
		mntmImportKeysFrom.addActionListener(e -> {
			
			String rawURL = JOptionPane
					.showInputDialog("Paste in keys from the URL provided by receipt by the Telegrams API");
			
			// Verify that it is a valid NationStates URL
			String rawUrlStart = "http://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&";
			if (rawURL.startsWith(rawUrlStart)) {
				
				rawURL = rawURL.replace(rawUrlStart, "");
				rawURL = rawURL.replace("&to=NATION_NAME", "");
				
				String[] tags = rawURL.split("&");
				if (tags.length == 2) {
					txtTelegramId.setText(tags[0].substring(tags[0].indexOf("=") + 1, tags[0].length()));
					txtSecretKey.setText(tags[1].substring(tags[1].indexOf("=") + 1, tags[1].length()));
				}
				
			} else {
				String message = "<html>Please input a properly formatted NationStates URL in the form displayed when a "
						+ "telegram is sent to 'tag:api'</html>";
				JOptionPane.showMessageDialog(frame, new JLabel(message));
			}
		});
		mnData.add(mntmImportKeysFrom);
		
		JMenu mnImportRecipients = new JMenu("Import Recipients");
		mnData.add(mnImportRecipients);
		
		JMenuItem mntmFromWaDelegate = new JMenuItem("From WA Delegate List");
		mntmFromWaDelegate.addActionListener(e -> appendCode("wa:delegates"));
		
		JMenuItem mntmAsCommaSeparated = new JMenuItem("As Comma Separated List");
		mntmAsCommaSeparated.addActionListener(e -> {
			String message = "Input a string of delegates, as found on a list of delegates\nin one of the "
					+ "NationStates World Assembly pages:";
			String input = JOptionPane.showInputDialog(message);
			if (input != null) {
				
				input = input.replaceAll("\\(.+?\\)", "");
				String[] list = input.split(",");
				
				for (String element : list) {
					appendCode(element.toLowerCase().trim().replace(" ", "_"));
				}
			}
		});
		mnImportRecipients.add(mntmAsCommaSeparated);
		mnImportRecipients.add(mntmFromWaDelegate);
		
		JMenuItem mntmFromAtVote = new JMenuItem("From At Vote Screen");
		mntmFromAtVote.addActionListener(e -> {
			
			Object[] possibilities = { "GA For", "GA Against", "SC For", "SC Against" };
			String output = (String) JOptionPane.showInputDialog(frame, "Select which chamber and side you want to address:",
					"Select Chamber and Side", JOptionPane.PLAIN_MESSAGE, null, possibilities, "GA For");
			
			if (!StringUtils.isEmpty(output)) {
				String[] elements = output.split(" ");
				if (elements.length == 2) {
					
					String chamber = elements[0].equals("GA") ? CNetLoader.GA : CNetLoader.SC;
					String side = elements[1].equals("For") ? CNetLoader.FOR : CNetLoader.AGAINST;
					
					try {
						txtrCode.append(CommuniqueUtilities
								.joinListWith(Arrays.asList(CNetLoader.importAtVoteDelegates(chamber, side)), '\n'));
					} catch (NullPointerException exc) {
						JOptionPane.showMessageDialog(frame, "Cannot import data from NationStates website.");
						log.warning("Cannot import data.");
					}
					
				}
			}
			
		});
		mnImportRecipients.add(mntmFromAtVote);
		
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
		mntmMinimise.setAccelerator(getOSKeyStroke(KeyEvent.VK_M));
		mntmMinimise.addActionListener(e -> {
			if (frame.getState() == Frame.NORMAL) {
				frame.setState(Frame.ICONIFIED);
			}
		});
		mnWindow.add(mntmMinimise);
		
		mnWindow.addSeparator();
		
		JMenuItem mntmRecruiter = new JMenuItem("Recruiter...");
		mntmRecruiter.setAccelerator(getOSKeyStroke(KeyEvent.VK_R));
		mntmRecruiter.addActionListener(e -> showRecruiter());
		mnWindow.add(mntmRecruiter);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(e -> new CTextDialog(frame, "About", CommuniqueMessages.acknowledgement));
		mnHelp.add(mntmAbout);
		
		mnHelp.addSeparator();
		
		JMenuItem mntmDocumentation = new JMenuItem("Documentation");
		mntmDocumentation.addActionListener(e -> {
			try {
				Desktop.getDesktop().browse(new URL("https://github.com/iFlyCode/Communique").toURI());
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
		
		mnHelp.addSeparator();
		
		JMenuItem mntmLicence = new JMenuItem("Licence");
		mntmLicence.addActionListener(e -> new CTextDialog(frame, "Licence", CommuniqueMessages.licence));
		mnHelp.add(mntmLicence);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				try {
					
					// Save it to application support
					CLoader loader = new CLoader(appSupport.resolve("autosave.txt"));
					loader.save(exportState());
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		log.info("Shutdown hook added.");
		
		// Parse action for above
		btnSend.addActionListener(new ActionListener() {
			
			@Override public void actionPerformed(ActionEvent e) {
				
				CommuniqueParser parser = new CommuniqueParser();
				
				// Check if a recruit-flag has been used.
				String[] lines = txtrCode.getText().split("\n");
				for (String element : lines) {
					if (element.startsWith("flag:recruit")) {
						showRecruiter();
						return;
					}
				}
				
				if (!parsed) {
					
					// Call and do the parsing
					log.info("Called parser");
					Communique.this.parsedRecipients = parser.filterAndParse(txtrCode.getText().split("\n"));
					
					// Estimate Time Needed
					String timeNeeded = estimateTime(parsedRecipients.length, chckbxRecruitment.isSelected());
					
					// Show Recipients
					StringBuilder builder = new StringBuilder();
					builder.append("# == Communiqué Recipients ==\n" + "# This tab shows all " + parsedRecipients.length
							+ " recipients after parsing of the Code tab.\n# Estimated time needed is " + timeNeeded
							+ "\n\n");
					for (String element : parsedRecipients) {
						builder.append(element + "\n");
					}
					
					log.info("Recipients Parsed.");
					
					// Change GUI elements
					txtrRecipients.setText(builder.toString());
					triggerParsed(true);
					
				} else {
					send();
				}
			}
			
			private String estimateTime(int count, boolean isRecruitment) {
				int numRecipients = count;
				int seconds = (int) Math.round(numRecipients * (isRecruitment ? 180.05 : 30.05));
				return CommuniqueUtilities.time(seconds);
			}
		});
		
		log.info("Communiqué loaded.");
		
		// If there is an auto-save, load it
		if (Files.exists(appSupport.resolve("autosave.txt"))) {
			CLoader loader = new CLoader(appSupport.resolve("autosave.txt"));
			try {
				this.importState(loader.load());
			} catch (IOException e1) {
				// do nothing if it fails to load
			}
		}
		
	}
	
	@Override public CConfig exportState() {
		
		CConfig config = new CConfig();
		
		config.isRecruitment = chckbxRecruitment.isSelected();
		config.isRandomised = chckbxmntmRandomiseRecipients.isSelected();
		config.keys = new JTelegramKeys(txtClientKey.getText(), txtSecretKey.getText(), txtTelegramId.getText());
		
		String[][] recipientsAndSents = filterSents(txtrCode.getText().split("\n"));
		
		config.recipients = recipientsAndSents[0];
		config.sentList = recipientsAndSents[1];
		
		config.defaultVersion();
		
		log.info("Communiqué config exported.");
		return config;
		
	}
	
	@Override public void importState(CConfig config) {
		
		chckbxRecruitment.setSelected(config.isRecruitment);
		chckbxmntmRandomiseRecipients.setSelected(config.isRandomised);
		
		txtClientKey.setText(config.keys.getClientKey());
		txtSecretKey.setText(config.keys.getSecretKey());
		txtTelegramId.setText(config.keys.getTelegramId());
		
		// Format the standard recipients.
		if (!ArrayUtils.isEmpty(config.recipients)) {
			txtrCode.setText(codeHeader + CommuniqueUtilities.joinListWith(Arrays.asList(config.recipients), '\n'));
		}
		
		// Format the universal negations.
		if (!ArrayUtils.isEmpty(config.sentList)) {
			for (int x = 0; x < config.sentList.length; x++) {
				config.sentList[x] = "/" + config.sentList[x];
			}
			txtrCode.append("\n\n" + CommuniqueUtilities.joinListWith(Arrays.asList(config.sentList), '\n'));
		}
		
		log.info("Communique info imported");
	}
	
	private void appendCode(String input) {
		txtrCode.append("\n" + input);
	}
	
	private String[][] filterSents(String[] input) {
		
		List<String> inputList = Arrays.asList(input);
		
		List<String> recipients = new ArrayList<>();
		List<String> sents = new ArrayList<>();
		
		for (int x = 0; x < inputList.size(); x++) {
			
			if (!StringUtils.isEmpty(inputList.get(x)) && !inputList.get(x).startsWith("#")) {
				if (!inputList.get(x).startsWith("/")) {
					recipients.add(inputList.get(x));
					
				} else {
					sents.add(inputList.get(x).replaceFirst("/", ""));
				}
			}
		}
		
		return new String[][] { recipients.toArray(new String[recipients.size()]), sents.toArray(new String[sents.size()]) };
	}
	
	// Changes the state of the button to reflect whether it is ready to send and or parsed
	void triggerParsed(boolean parsed) {
		this.parsed = parsed;
		btnSend.setText(parsed ? "Send" : "Parse");
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String) */
	@Override public void log(String input) {
		log.info(input);
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
	@Override public void sentTo(String recipient, int x, int length) {
		
		if (!(progressBar.getMaximum() == length - 1)) {
			progressBar.setMaximum(length - 1);
		}
		
		txtrCode.append(x == 0 ? "\n\n/" + recipient : "\n/" + recipient);
		progressBar.setValue(x);
	}
	
	/** Shows and initialises the Communique Recruiter.
	 *
	 * @since 6
	 * @see com.git.ifly6.communique.ngui.CommuniqueRecruiter */
	private void showRecruiter() {
		if (recruiter == null || !recruiter.isDisplayable()) {
			recruiter = new CommuniqueRecruiter(this);
			recruiter.setWithCConfig(this.exportState());
		} else {
			recruiter.toFront();
		}
	}
	
	/** Returns a <code>KeyStroke</code> which is automatically adapted for
	 *
	 * @param keyEvent
	 * @param shiftMask
	 * @return */
	public static KeyStroke getOSKeyStroke(int keyEvent, boolean shiftMask) {
		if (shiftMask) {
			if (SystemUtils.IS_OS_MAC) {
				return KeyStroke.getKeyStroke(keyEvent, Event.META_MASK | Event.SHIFT_MASK);
			} else {
				return KeyStroke.getKeyStroke(keyEvent, Event.CTRL_MASK | Event.SHIFT_MASK);
			}
		} else {
			return KeyStroke.getKeyStroke(keyEvent, SystemUtils.IS_OS_MAC ? Event.META_MASK : Event.CTRL_MASK);
		}
	}
	
	public static KeyStroke getOSKeyStroke(int keyEvent) {
		return getOSKeyStroke(keyEvent, false);
	}
	
	/** @param type */
	public Path showFileChooser(Frame parent, int type) {
		
		Path savePath;
		
		// Due to a problem in Windows and the AWT FileDialog, this will show a JFileChooser on Windows systems.
		if (!SystemUtils.IS_OS_MAC) {
			
			JFileChooser fChooser = new JFileChooser(appSupport.toFile());
			fChooser.setDialogTitle("Choose file...");
			
			int returnVal = JFileChooser.CANCEL_OPTION;
			if (type == FileDialog.SAVE) {
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
			
		} else {
			
			FileDialog fDialog = new FileDialog(parent, "Choose file...", type);
			fDialog.setDirectory(appSupport.toFile().toString());
			fDialog.setVisible(true);
			
			String fileName = fDialog.getFile();
			if (fileName == null) {
				log.info("User cancelled file file dialog");
				return null;
				
			} else {
				savePath = Paths.get(fDialog.getDirectory() == null ? "" : fDialog.getDirectory())
						.resolve(fDialog.getFile());
			}
			
		}
		
		// Make it end in txt if saving
		if (type == FileDialog.SAVE) {
			if (!FilenameUtils.getExtension(savePath.toString()).equals("txt")) {
				log.info("Append txt to savePath");
				savePath = savePath.resolveSibling(savePath.getFileName() + ".txt");
			}
		}
		
		String action = type == FileDialog.SAVE ? "save" : "load";
		log.info("User elected to " + action + " file at " + savePath.toAbsolutePath().toString());
		return savePath;
	}
	
	/**
	 *
	 */
	private void send() {
		
		// sending logic
		if (!sendingThread.isAlive()) {
			client.setKillThread(false);
			
			Runnable runner = () -> {
				
				if (chckbxmntmRandomiseRecipients.isSelected()) {
					CommuniqueUtilities.randomiseArray(parsedRecipients);
				}
				
				client.setRecipients(parsedRecipients);	// Set recipients
				client.setKeys(new JTelegramKeys(txtClientKey.getText(), txtSecretKey.getText(), txtTelegramId.getText()));
				
				// Set Recruitment Status, JavaTelegram defaults to true
				client.setRecruitment(chckbxRecruitment.isSelected());
				
				// Save client key
				try {
					CLoader.writeProperties(txtClientKey.getText());
					CLoader loader = new CLoader(appSupport.resolve("autosave.txt"));
					loader.save(exportState());
					
				} catch (IOException e) {
					System.err.println("Exception in writing autosave or properties file.");
				}
				
				client.connect();
				log.info("Queries Complete.");
				JOptionPane.showMessageDialog(Communique.this.frame,
						"Queries to " + (progressBar.getValue() + 1) + " nations complete.");
				
				// Reset the progress bar
				progressBar.setValue(0);
				progressBar.setMaximum(0);
				
				// Update code has been removed because of a change to the JTelegramLogger which allows
				// for direct event dispatch.
			};
			
			sendingThread = new Thread(runner);
			sendingThread.start();
			
		} else {
			log.info("There is already a campaign running. Terminate that campaign and then retry.");
		}
	}
}
