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

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.FilterType;
import com.git.ifly6.communique.data.RecipientType;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.communique.io.CommuniqueProcessingAction;
import com.git.ifly6.communique.ngui.components.CommuniqueConstants;
import com.git.ifly6.communique.ngui.components.CommuniqueLAF;
import com.git.ifly6.communique.ngui.components.CommuniqueNativisation;
import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramLogger;
import com.git.ifly6.nsapi.telegram.JTelegramType;
import com.git.ifly6.nsapi.telegram.JavaTelegram;
import org.apache.commons.text.WordUtils;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implements the sending functions required in {@link AbstractCommuniqueRecruiter} and the window objects and
 * interface. The class is designed around the manipulation of {@link CommuniqueConfig} objects which are then returned
 * to {@link Communique} for possible saving.
 */
public class CommuniqueRecruiter extends AbstractCommuniqueRecruiter implements JTelegramLogger {

	private static final String[] protectedRegions = new String[]{"the Pacific", "the North Pacific", "the South Pacific",
			"the East Pacific", "the West Pacific", "Lazarus", "Balder", "Osiris", "the Rejected Realms"};
	private static final Logger LOGGER = Logger.getLogger(CommuniqueRecruiter.class.getName());

	static {
		LOGGER.addHandler(CommuniqueLAF.loggerFileHandler);
	}

	private Communique communique;

	private JFrame frame;
	private JTextField clientKeyField;
	private JTextField secretKeyField;
	private JTextField telegramIdField;
	private JTextArea sentListArea;

	private Thread thread;

	// To keep track of the nations to whom we have sent a telegram
	private JLabel lblNationsCount;
	private JProgressBar progressBar;

	// To keep track of the feeders
	private JList<String> excludeList;

	/**
	 * Create the application, if necessary.
	 */
	CommuniqueRecruiter(Communique comm) {
		initialize();
		frame.setVisible(true);
		this.communique = comm;
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame("Communiqué Recruiter " + Communique7Parser.BUILD);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		{
			Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
			double sWidth = screenDimensions.getWidth();
			double sHeight = screenDimensions.getHeight();
			int windowWidth = 600;
			int windowHeight = 400;
			frame.setBounds((int) (sWidth / 2 - windowWidth / 2), (int) (sHeight / 2 - windowHeight / 2), windowWidth,
					windowHeight);
			frame.setMinimumSize(new Dimension(600, 400));
		}

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		frame.setContentPane(panel);
		panel.setLayout(new GridLayout(0, 2, 5, 5));

		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		panel.add(leftPanel);

		JLabel lblClientKey = new JLabel("Client Key");

		clientKeyField = new JTextField();
		clientKeyField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		clientKeyField.setColumns(10);

		JLabel lblSecretKey = new JLabel("Secret Key");

		secretKeyField = new JTextField();
		secretKeyField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		secretKeyField.setColumns(10);

		JLabel lblTelegramId = new JLabel("Telegram ID");

		telegramIdField = new JTextField();
		telegramIdField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		telegramIdField.setColumns(10);

		JLabel lblExclude = new JLabel("Exclude:");

		JButton btnSendButton = new JButton("Send");
		btnSendButton.addActionListener(e -> {
			if (btnSendButton.getText().equals("Send")) { // STARTING UP
				btnSendButton.setText("Stop");
				send();

			} else { // SHUTTING DOWN
				thread.interrupt();
				Path savePath = CommuniqueNativisation.showFileChooser(frame, FileDialog.SAVE);

				// Cancel saving if null
				if (savePath == null) return;
				save(savePath);

				// Dispose the components
				frame.setVisible(false);
				frame.dispose();
			}
		});

		DefaultListModel<String> exListModel = new DefaultListModel<>();
		Arrays.stream(protectedRegions).forEach(exListModel::addElement);

		excludeList = new JList<>(exListModel);
		excludeList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		excludeList.setSelectionModel(new DefaultListSelectionModel() {
			private static final long serialVersionUID = 1L;
			boolean gestureStarted = false;

			@Override
			public void setSelectionInterval(int index0, int index1) {
				if (!gestureStarted) if (isSelectedIndex(index0)) super.removeSelectionInterval(index0, index1);
				else super.addSelectionInterval(index0, index1);
				gestureStarted = true;
			}

			@Override
			public void setValueIsAdjusting(boolean isAdjusting) {
				if (!isAdjusting) gestureStarted = false;
			}
		});
		JScrollPane scrollPane = new JScrollPane(excludeList);

		JPanel buttonsPane = new JPanel();
		buttonsPane.setLayout(new GridLayout(1, 3, 0, 0));
		buttonsPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

		JLabel lblSentTo = new JLabel("Sent to");
		lblNationsCount = new JLabel("0 nations");

		progressBar = new JProgressBar();
		progressBar.setMaximum(180);
		if (CommuniqueUtilities.IS_OS_MAC) // Mac, make progress bar around the same length as the button
			progressBar.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
		else if (CommuniqueUtilities.IS_OS_WINDOWS) progressBar.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

		JButton btnAdd = new JButton("+");
		btnAdd.setPreferredSize(new Dimension(25, 20));
		btnAdd.addActionListener(al -> {
			String rName =
					(String) JOptionPane.showInputDialog(frame, "Input the name of the region you want to exclude.",
							"Exclude region", JOptionPane.PLAIN_MESSAGE, null, null, "");
			if (!ApiUtils.isEmpty(rName)) exListModel.addElement(rName);
		});

		JButton btnRemove = new JButton("—");
		btnRemove.setPreferredSize(new Dimension(25, 20));
		btnRemove.addActionListener(al -> {
			int[] selectedIndices = excludeList.getSelectedIndices();
			for (int i = selectedIndices.length - 1; i >= 0; i--)
				if (!ApiUtils.contains(protectedRegions, exListModel.get(selectedIndices[i])))
					exListModel.remove(selectedIndices[i]);
			this.sync();
		});

		GroupLayout gl_leftPanel = new GroupLayout(leftPanel);
		gl_leftPanel.setHorizontalGroup(
				gl_leftPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_leftPanel.createSequentialGroup()
								.addGroup(gl_leftPanel.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_leftPanel.createSequentialGroup()
												.addContainerGap()
												.addComponent(lblSentTo)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(lblNationsCount))
										.addGroup(gl_leftPanel.createSequentialGroup()
												.addGroup(gl_leftPanel.createParallelGroup(Alignment.LEADING)
														.addComponent(lblClientKey)
														.addComponent(lblSecretKey)
														.addComponent(lblTelegramId))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_leftPanel.createParallelGroup(Alignment.LEADING)
														.addComponent(secretKeyField, GroupLayout.DEFAULT_SIZE, 300,
																Short.MAX_VALUE)
														.addComponent(clientKeyField, Alignment.TRAILING,
																GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
														.addComponent(telegramIdField, Alignment.TRAILING,
																GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)))
										.addGroup(gl_leftPanel.createSequentialGroup()
												.addComponent(lblExclude, GroupLayout.PREFERRED_SIZE, 59,
														GroupLayout.PREFERRED_SIZE)
												.addGap(18)
												.addGroup(gl_leftPanel.createParallelGroup(Alignment.LEADING)
														.addGroup(gl_leftPanel.createSequentialGroup()
																.addComponent(btnAdd, GroupLayout.PREFERRED_SIZE,
																		GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(btnRemove, GroupLayout.PREFERRED_SIZE,
																		GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																.addGap(30)
																.addComponent(buttonsPane, GroupLayout.PREFERRED_SIZE,
																		GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(ComponentPlacement.RELATED, 219,
																		Short.MAX_VALUE))
														.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 305,
																Short.MAX_VALUE)))
										.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
										.addComponent(btnSendButton, GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
								.addGap(0)));
		gl_leftPanel.setVerticalGroup(
				gl_leftPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_leftPanel.createSequentialGroup()
								.addGroup(gl_leftPanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(clientKeyField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lblClientKey))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_leftPanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(secretKeyField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lblSecretKey))
								.addGap(5)
								.addGroup(gl_leftPanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(telegramIdField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lblTelegramId))
								.addGap(8)
								.addGroup(gl_leftPanel.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_leftPanel.createSequentialGroup()
												.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
												.addGap(5)
												.addGroup(gl_leftPanel.createParallelGroup(Alignment.LEADING)
														.addGroup(gl_leftPanel.createParallelGroup(Alignment.BASELINE)
																.addComponent(btnAdd, GroupLayout.PREFERRED_SIZE,
																		GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																.addComponent(btnRemove, GroupLayout.PREFERRED_SIZE,
																		GroupLayout.DEFAULT_SIZE,
																		GroupLayout.PREFERRED_SIZE))
														.addComponent(buttonsPane, GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addGap(5))
										.addGroup(gl_leftPanel.createSequentialGroup()
												.addComponent(lblExclude)
												.addPreferredGap(ComponentPlacement.RELATED)))
								.addGroup(gl_leftPanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblSentTo)
										.addComponent(lblNationsCount))
								.addGap(5)
								.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(5)
								.addComponent(btnSendButton)
								.addGap(4)));
		leftPanel.setLayout(gl_leftPanel);

		JPanel rightPanel = new JPanel();
		panel.add(rightPanel);
		rightPanel.setLayout(new BorderLayout(0, 0));

		sentListArea = new JTextArea("");
		sentListArea.setEditable(false);
		sentListArea.setLineWrap(true);
		sentListArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		sentListArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		rightPanel.add(new JScrollPane(sentListArea));

		JLabel lblListOfNations = new JLabel(
				"<html>List of nations to which a recruitment telegram has been sent in the current session.</html>");
		lblListOfNations.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
		rightPanel.add(lblListOfNations, BorderLayout.NORTH);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnWindow = new JMenu("Window");
		menuBar.add(mnWindow);

		JMenuItem mntmClose = new JMenuItem("Close");
		mntmClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, CommuniqueConstants.COMMAND_KEY));
		mntmClose.addActionListener(e -> {
			if (thread != null) thread.interrupt();
			frame.setVisible(false);
			frame.dispose();
		});
		mnWindow.add(mntmClose);

		JMenuItem mntmMinimise = new JMenuItem("Minimise");
		mntmClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, CommuniqueConstants.COMMAND_KEY));
		mntmMinimise.addActionListener(e -> {
			if (frame.getState() == Frame.NORMAL) frame.setState(Frame.ICONIFIED);
		});
		mnWindow.add(mntmMinimise);
	}

	@Override
	public void log(String input) {
		// Filter out the stuff we don't care about
		if (!input.equals("API Queries Complete.")) LOGGER.info(input);
	}

	/**
	 * @see com.git.ifly6.nsapi.telegram.JTelegramLogger#sentTo(java.lang.String, int, int)
	 */
	@Override
	public void sentTo(String recipient, int recipientNum, int length) {
		super.sentTo(recipient, recipientNum, length);
		lblNationsCount.setText(sentList.size() + (sentList.size() == 1 ? " nation" : " nations"));
		if (!ApiUtils.isEmpty(sentListArea.getText())) sentListArea.append("\n" + recipient);
		else sentListArea.setText(recipient);
	}

	/**
	 * @return returns the proscribed regions as defined by the regions selected in the <code>excludeList</code>, mapped
	 * to <code>CommuniqueRecipient</code>
	 */
	private Set<CommuniqueRecipient> listProscribedRegions() {
		return Arrays.stream(excludeList.getSelectedIndices())
				.mapToObj(x -> excludeList.getModel().getElementAt(x))
				.map(s -> new CommuniqueRecipient(FilterType.EXCLUDE, RecipientType.REGION, s))
				.collect(Collectors.toCollection(HashSet::new));
	}

	/**
	 * @param savePath is the path on which to save
	 */
	private void save(Path savePath) {

		// Prepare to save by:
		// * Creating a configuration file up to specifications
		// * Importing that configuration into Communique
		// * Have Communique save that file
		sync();

		// Save
		try {
			if (!savePath.toAbsolutePath().toString().endsWith(".txt"))
				savePath = Paths.get(savePath.toAbsolutePath().toString() + ".txt");
			CommuniqueLoader loader = new CommuniqueLoader(savePath);
			loader.save(communique.exportState());

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void sync() {

		JTelegramKeys keys = new JTelegramKeys(
				clientKeyField.getText(),
				secretKeyField.getText(),
				telegramIdField.getText()
		);
		CommuniqueConfig config = new CommuniqueConfig(JTelegramType.NONE, CommuniqueProcessingAction.NONE, keys, "");

		// Create and set recipients and sent-lists
		List<CommuniqueRecipient> rList = new ArrayList<>();

		rList.add(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.FLAG, "recruit"));
		rList.addAll(filterList); // add filtered list
		rList.addAll(listProscribedRegions()); // add proscribed regions
		rList.addAll(sentList); // add sent list

		// Sync up with Communique
		config.setcRecipients(rList);
		communique.importState(config);

	}

	boolean isDisplayable() {
		return frame.isDisplayable();
	}

	void toFront() {
		frame.setVisible(true);
		frame.toFront();
	}

	@Override
	public void setConfig(CommuniqueConfig config) {
		super.setConfig(config);
		LOGGER.info("Imported configuration from Communique");

		// Client, Secret, and Telegram Keys
		clientKeyField.setText(config.keys.getClientKey());
		secretKeyField.setText(config.keys.getSecretKey());
		telegramIdField.setText(config.keys.getTelegramId());

		// Update graphical component
		lblNationsCount.setText(sentList.size() + (sentList.size() == 1 ? " nation" : " nations"));

		// Update list
		excludeList.clearSelection();
		DefaultListModel<String> model = (DefaultListModel<String>) excludeList.getModel();
		List<String> excludeRegions = config.getcRecipients().stream()
				.filter(r -> r.getRecipientType() == RecipientType.REGION)
				.filter(r -> r.getFilterType() == FilterType.EXCLUDE)
				.map(CommuniqueRecipient::getName)
				.collect(Collectors.toList());

		for (String element : excludeRegions) {
			final String elementRef = ApiUtils.ref(element);
			boolean found = false;
			for (int i = 0; i < model.getSize(); i++) {
				if (ApiUtils.ref(model.getElementAt(i)).equals(elementRef)) {
					excludeList.addSelectionInterval(i, i);
					found = true;
					break;
				}
			}
			if (!found) {
				model.addElement(WordUtils.capitalize(element)); // add to the list
				excludeList.addSelectionInterval(model.size() - 1, model.size() - 1);
			}
		}
	}

	/**
	 * @see com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter#send()
	 */
	@Override
	public void send() {

		proscribedRegions = listProscribedRegions();
		// note that filter list is set in cconfig

		Runnable runner = () -> {
			AtomicReference<String> nextRecipient = new AtomicReference<>(this.getRecipient().getName());
			AtomicBoolean foundNext = new AtomicBoolean(false);

			while (true) {

				JavaTelegram client = new JavaTelegram(CommuniqueRecruiter.this);
				client.setKeys(new JTelegramKeys(
						clientKeyField.getText().trim(),
						secretKeyField.getText().trim(),
						telegramIdField.getText().trim()
				));
				client.setRecipient(nextRecipient.get());
				client.connect();

				LOGGER.info(String.format("Attempted dispatch of telegram %d to %s", sentList.size() + 1,
						nextRecipient.get()));

				Calendar now = Calendar.getInstance();
				now.add(Calendar.SECOND, 180);
				String nextTelegramTime = new SimpleDateFormat("HH:mm:ss").format(now.getTime());
				LOGGER.info("Next recruitment telegram probably in 180 seconds at " + nextTelegramTime);

				foundNext.set(false);

				for (int x = 0; ; x++) {

					try {
						progressBar.setValue(x);
						Thread.sleep(1000);    // 1-second intervals, wake to update the progressBar
					} catch (InterruptedException e) {
						return; // break all the things
					}

					if (x == 170) {
						Runnable runnable1 = () -> {
							nextRecipient.set(getRecipient().getName());
							LOGGER.info("Got next recipient " + nextRecipient.get());
							foundNext.set(true);
						};
						new Thread(runnable1).start();
					}

					if (x > 180 && foundNext.get()) {
						LOGGER.info(String.format("Starting next loop at %d s for telegram %d", x, sentList.size()));
						break;  // break and send the next telegram
					}

				}
			}
		};

		thread = new Thread(runner);
		thread.start();    // thread#run does not work
	}
}
