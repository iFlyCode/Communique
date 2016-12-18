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
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
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
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.git.ifly6.communique.data.CommuniqueParser;
import com.git.ifly6.communique.io.CConfig;
import com.git.ifly6.communique.io.CLoader;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;

/** @author ifly6 */
public class CommuniqueRecruiter extends AbstractCommuniqueRecruiter implements JTelegramLogger {

	private static final Logger log = Logger.getLogger(CommuniqueRecruiter.class.getName());
	public static final String[] regionList = new String[] { "the Pacific", "the North Pacific", "the South Pacific",
			"the East Pacific", "the West Pacific", "Lazarus", "Balder", "Osiris", "the Rejected Realms" };
	
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
	
	/** Create the application, if necessary. */
	public CommuniqueRecruiter(Communique comm) {
		initialize();
		frame.setVisible(true);
		this.communique = comm;
	}
	
	/** Initialise the contents of the frame. */
	private void initialize() {
		
		frame = new JFrame("Communiqué Recruiter " + CommuniqueParser.version);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		{
			Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
			double sWidth = screenDimensions.getWidth();
			double sHeight = screenDimensions.getHeight();
			int windowWidth = 800;
			int windowHeight = 500;
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
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		leftPanel.setLayout(gbl_panel_1);
		
		JLabel lblClientKey = new JLabel("Client Key");
		GridBagConstraints gbc_lblClientKey = new GridBagConstraints();
		gbc_lblClientKey.anchor = GridBagConstraints.EAST;
		gbc_lblClientKey.insets = new Insets(0, 0, 5, 5);
		gbc_lblClientKey.gridx = 0;
		gbc_lblClientKey.gridy = 0;
		leftPanel.add(lblClientKey, gbc_lblClientKey);
		
		clientKeyField = new JTextField();
		clientKeyField.setFont(new Font(Font.MONOSPACED, 0, 11));
		clientKeyField.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				((JTextField) e.getComponent()).setText("");
			}
		});
		GridBagConstraints gbc_clientKeyField = new GridBagConstraints();
		gbc_clientKeyField.gridwidth = 2;
		gbc_clientKeyField.insets = new Insets(0, 0, 5, 0);
		gbc_clientKeyField.fill = GridBagConstraints.HORIZONTAL;
		gbc_clientKeyField.gridx = 1;
		gbc_clientKeyField.gridy = 0;
		leftPanel.add(clientKeyField, gbc_clientKeyField);
		clientKeyField.setColumns(10);
		
		JLabel lblSecretKey = new JLabel("Secret Key");
		GridBagConstraints gbc_lblSecretKey = new GridBagConstraints();
		gbc_lblSecretKey.anchor = GridBagConstraints.EAST;
		gbc_lblSecretKey.insets = new Insets(0, 0, 5, 5);
		gbc_lblSecretKey.gridx = 0;
		gbc_lblSecretKey.gridy = 1;
		leftPanel.add(lblSecretKey, gbc_lblSecretKey);
		
		secretKeyField = new JTextField();
		secretKeyField.setFont(new Font(Font.MONOSPACED, 0, 11));
		secretKeyField.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				((JTextField) e.getComponent()).setText("");
			}
		});
		GridBagConstraints gbc_secretKeyField = new GridBagConstraints();
		gbc_secretKeyField.gridwidth = 2;
		gbc_secretKeyField.insets = new Insets(0, 0, 5, 0);
		gbc_secretKeyField.fill = GridBagConstraints.HORIZONTAL;
		gbc_secretKeyField.gridx = 1;
		gbc_secretKeyField.gridy = 1;
		leftPanel.add(secretKeyField, gbc_secretKeyField);
		secretKeyField.setColumns(10);
		
		JLabel lblTelegramId = new JLabel("Telegram ID");
		GridBagConstraints gbc_lblTelegramId = new GridBagConstraints();
		gbc_lblTelegramId.anchor = GridBagConstraints.EAST;
		gbc_lblTelegramId.insets = new Insets(0, 0, 5, 5);
		gbc_lblTelegramId.gridx = 0;
		gbc_lblTelegramId.gridy = 2;
		leftPanel.add(lblTelegramId, gbc_lblTelegramId);
		
		telegramIdField = new JTextField();
		telegramIdField.setFont(new Font(Font.MONOSPACED, 0, 11));
		telegramIdField.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				((JTextField) e.getComponent()).setText("");
			}
		});
		GridBagConstraints gbc_telegramIdField = new GridBagConstraints();
		gbc_telegramIdField.gridwidth = 2;
		gbc_telegramIdField.insets = new Insets(0, 0, 5, 0);
		gbc_telegramIdField.fill = GridBagConstraints.HORIZONTAL;
		gbc_telegramIdField.gridx = 1;
		gbc_telegramIdField.gridy = 2;
		leftPanel.add(telegramIdField, gbc_telegramIdField);
		telegramIdField.setColumns(10);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 3;
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 3;
		leftPanel.add(separator, gbc_separator);
		
		JLabel lblExclude = new JLabel("Exclude:");
		GridBagConstraints gbc_lblExclude = new GridBagConstraints();
		gbc_lblExclude.anchor = GridBagConstraints.EAST;
		gbc_lblExclude.insets = new Insets(0, 0, 5, 5);
		gbc_lblExclude.gridx = 0;
		gbc_lblExclude.gridy = 4;
		leftPanel.add(lblExclude, gbc_lblExclude);
		
		JButton btnSendButton = new JButton("Send");
		btnSendButton.addActionListener(e -> {
			
			if (btnSendButton.getText().equals("Send")) {		// STARTING UP
				
				btnSendButton.setText("Stop");
				send();
				
			} else {	// SHUTTING DOWN
				
				thread.interrupt();
				Path savePath = communique.showFileChooser(frame, FileDialog.SAVE);
				
				// Cancel saving if null
				if (savePath == null) { return; }
				save(savePath);
				
				// Dispose the components
				frame.setVisible(false);
				frame.dispose();
			}
		});
		
		DefaultListModel<String> exListModel = new DefaultListModel<>();
		Arrays.stream(regionList).forEach(exListModel::addElement);
		
		excludeList = new JList<>(exListModel);
		excludeList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		excludeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		GridBagConstraints gbc_excludeList = new GridBagConstraints();
		gbc_excludeList.gridwidth = 2;
		gbc_excludeList.gridheight = 7;
		gbc_excludeList.insets = new Insets(0, 0, 5, 0);
		gbc_excludeList.fill = GridBagConstraints.BOTH;
		gbc_excludeList.gridx = 1;
		gbc_excludeList.gridy = 4;
		JScrollPane scrollPane = new JScrollPane(excludeList);
		leftPanel.add(scrollPane, gbc_excludeList);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(al -> {
			String rName = (String) JOptionPane.showInputDialog(frame, "Input the name of the region you want to exclude.",
					"Exclude region", JOptionPane.PLAIN_MESSAGE, null, null, "");
			if (!StringUtils.isEmpty(rName)) {
				exListModel.addElement(rName);
			}
		});
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 5;
		leftPanel.add(btnAdd, gbc_btnAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(al -> {
			int[] selectedIndices = excludeList.getSelectedIndices();
			for (int i = selectedIndices.length - 1; i >= 0; i--) {
				if (!ArrayUtils.contains(regionList, exListModel.get(i))) {
					exListModel.removeElementAt(selectedIndices[i]);
				}
			}
		});
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemove.insets = new Insets(0, 0, 5, 5);
		gbc_btnRemove.gridx = 0;
		gbc_btnRemove.gridy = 6;
		leftPanel.add(btnRemove, gbc_btnRemove);
		
		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(al -> {
			excludeList.clearSelection();
		});
		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClear.insets = new Insets(0, 0, 5, 5);
		gbc_btnClear.gridx = 0;
		gbc_btnClear.gridy = 7;
		leftPanel.add(btnClear, gbc_btnClear);
		
		JLabel lblSentTo = new JLabel("Sent to");
		GridBagConstraints gbc_lblSentTo = new GridBagConstraints();
		gbc_lblSentTo.anchor = GridBagConstraints.EAST;
		gbc_lblSentTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblSentTo.gridx = 0;
		gbc_lblSentTo.gridy = 11;
		leftPanel.add(lblSentTo, gbc_lblSentTo);
		
		lblNationsCount = new JLabel("0 nations");
		lblNationsCount.setText("0 nations");
		GridBagConstraints gbc_lblNationscount = new GridBagConstraints();
		gbc_lblNationscount.anchor = GridBagConstraints.WEST;
		gbc_lblNationscount.insets = new Insets(0, 0, 5, 5);
		gbc_lblNationscount.gridx = 1;
		gbc_lblNationscount.gridy = 11;
		leftPanel.add(lblNationsCount, gbc_lblNationscount);
		
		progressBar = new JProgressBar();
		progressBar.setMaximum(180);
		if (SystemUtils.IS_OS_MAC) {
			// Mac, make progress bar around the same length as the button
			progressBar.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
		} else if (SystemUtils.IS_OS_WINDOWS) {
			progressBar.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		}
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridwidth = 3;
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 12;
		leftPanel.add(progressBar, gbc_progressBar);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridwidth = 3;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 13;
		leftPanel.add(btnSendButton, gbc_btnNewButton);
		
		JPanel rightPanel = new JPanel();
		panel.add(rightPanel);
		rightPanel.setLayout(new BorderLayout(0, 0));
		
		sentListArea = new JTextArea("");
		sentListArea.setEditable(false);
		sentListArea.setLineWrap(true);
		sentListArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		sentListArea.setFont(new Font(Font.MONOSPACED, 0, 11));
		rightPanel.add(new JScrollPane(sentListArea));
		
		JLabel lblListOfNations = new JLabel(
				"<html>List of nations to which a recruitment telegram has been sent in the current session.</html>");
		lblListOfNations.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
		rightPanel.add(lblListOfNations, BorderLayout.NORTH);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnWindow = new JMenu("Window");
		menuBar.add(mnWindow);
		
		JMenuItem mntmMinimise = new JMenuItem("Minimise");
		Communique.getOSKeyStroke(KeyEvent.VK_M);
		mntmMinimise.addActionListener(e -> {
			if (frame.getState() == Frame.NORMAL) {
				frame.setState(Frame.ICONIFIED);
			}
		});
		mnWindow.add(mntmMinimise);
	}
	
	@Override public void log(String input) {
		// Filter out the stuff we don't care about
		if (!input.equals("API Queries Complete.")) {
			System.err.println(input);
		}
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
	@Override public void sentTo(String recipient, int x, int length) {

		sentList.add(recipient);
		lblNationsCount.setText(sentList.size() + (sentList.size() == 1 ? " nation" : " nations"));
		
		if (!StringUtils.isEmpty(sentListArea.getText())) {
			sentListArea.append("\n-nation:" + recipient);
		} else {
			sentListArea.setText("-nation:" + recipient);
		}
	}
	
	private HashSet<String> listProscribedRegions() {
		
		return IntStream.of(excludeList.getSelectedIndices())
				.mapToObj(x -> excludeList.getModel().getElementAt(x).toString())
				.collect(Collectors.toCollection(HashSet::new));
		
		// HashSet<String> hashSet = new HashSet<>();
		// int[] sIndices = excludeList.getSelectedIndices();
		// for (int x : sIndices) {
		// hashSet.add(excludeList.getModel().getElementAt(x).toString());
		// }
		//
		// return hashSet;
	}
	
	/** @param file */
	private void save(Path savePath) {

		log.info("User elected to save file at " + savePath.toAbsolutePath().toString());
		
		// If it does not end in txt, make it end in txt
		if (!savePath.toAbsolutePath().toString().endsWith(".txt")) {
			savePath = Paths.get(savePath.toAbsolutePath().toString() + ".txt");
		}
		
		// Prepare to save by:
		// * Creating a configuration file up to specifications
		// * Importing that configuration into Communique
		// * Have Communique save that file
		// ===================
		CConfig config = new CConfig();
		
		// Set the many flags that need to be set
		config.isDelegatePrioritised = false;
		config.isRandomised = false;
		config.isRecruitment = true;
		config.defaultVersion();
		
		config.keys = new JTelegramKeys(clientKeyField.getText(), secretKeyField.getText(), telegramIdField.getText());
		
		// Create and set recipients and sent-lists
		List<String> recipients = new ArrayList<>(0);
		recipients.add("flag:recruit");
		for (String element : listProscribedRegions()) {
			recipients.add("flag:recruit -- region:" + element);
		}
		
		config.recipients = recipients.toArray(new String[recipients.size()]);
		config.sentList = sentList.toArray(new String[sentList.size()]);
		
		// Sync up with Communique
		communique.importState(config);
		
		// Save
		try {
			CLoader loader = new CLoader(savePath);
			loader.save(communique.exportState());
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public boolean isDisplayable() {
		return frame.isDisplayable();
	}
	
	public void toFront() {
		frame.setVisible(true);
		frame.toFront();
	}
	
	/** @see com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter#setClientKey(java.lang.String) */
	@Override public void setClientKey(String key) {
		super.setClientKey(key);
		clientKeyField.setText(key);
	}
	
	/** @see com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter#setSecretKey(java.lang.String) */
	@Override public void setSecretKey(String key) {
		super.setSecretKey(key);
		secretKeyField.setText(key);
	}
	
	/** @see com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter#setTelegramId(java.lang.String) */
	@Override public void setTelegramId(String id) {
		super.setTelegramId(id);
		telegramIdField.setText(id);
	}
	
	@Override public void setWithCConfig(CConfig config) {
		super.setWithCConfig(config);
		
		// Update graphical component
		lblNationsCount.setText(sentList.size() + (sentList.size() == 1 ? " nation" : " nations"));
		
		// Update list
		excludeList.clearSelection();
		DefaultListModel<String> model = (DefaultListModel<String>) excludeList.getModel();
		List<String> mapRecipients = recipients.stream().filter(s -> s.startsWith("flag:recruit -- region:"))
				.map(x -> x.replace("flag:recruit -- region:", "")).collect(Collectors.toList());
		for (String element : mapRecipients) {
			boolean found = false;
			for (int i = 0; i < model.getSize(); i++) {
				// search in the list, if it is already there, select it
				if (model.getElementAt(i).toString().toLowerCase().replace(" ", "_")
						.equalsIgnoreCase(element.toLowerCase().replace(" ", "_"))) {
					excludeList.addSelectionInterval(i, i);
					found = true;
					break;
				}
			}
			if (!found) {
				// add to the list
				model.addElement(element);
				excludeList.addSelectionInterval(model.size(), model.size());
				// TODO fix bug here where the first element added is not selected for some reason
			}
		}
	}
	
	/** @see com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter#send() */
	@Override public void send() {
		Runnable runner = () -> {

			boolean isSending = true;
			while (isSending) {

				proscribedRegions = listProscribedRegions();
				
				// Otherwise, start sending.
				JavaTelegram client = new JavaTelegram(CommuniqueRecruiter.this);
				client.setKeys(
						new JTelegramKeys(clientKeyField.getText(), secretKeyField.getText(), telegramIdField.getText()));
				client.setRecipient(getRecipient());
				client.connect();
				
				for (int x = 0; x < 180; x++) {
					try {
						progressBar.setValue(x);
						Thread.sleep(1000);	// 1-second intervals, wake to update the progressBar
					} catch (InterruptedException e) {
						isSending = false;
						return;
					}
				}
			}
		};
		
		thread = new Thread(runner);
		thread.start();
	}
}
