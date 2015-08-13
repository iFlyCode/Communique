/*
 * Copyright (c) 2015 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.communique;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Properties;

import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JavaTelegram;
import com.git.ifly6.javatelegram.util.JTelegramException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;

public class CommuniquéController {

	// TODO Live updating recipients list
	// TODO Interface with NS Happenings

	private int version = CommuniquéParser.version;

	@FXML private MenuBar menuBar;
	@FXML private TabPane tabPane;
	@FXML private TextArea logPane;
	@FXML private TextArea codePane;
	@FXML private TextArea recipientsPane;
	@FXML private TextField clientField;
	@FXML private TextField secretField;
	@FXML private TextField telegramField;
	@FXML private CheckBox recruitmentCheckBox;
	@FXML private CheckMenuItem disableSendingCheckBox;

	private JavaTelegram client;
	private CommuniquéLogger util;
	private CommuniquéParser parser;

	private Thread sendingThread = new Thread();

	@FXML private void initialize() {

		// If relevant, set to true.
		menuBar.setUseSystemMenuBar(true);

		logPane.setText("== Communiqué " + version + " ==\nEnter information or load file to proceed.\n");
		codePane.setText("# == Communiqué Recipients Code ==\n" + "# Enter recipients, one for each line or use 'region:', 'WA:', etc tags.\n"
				+ "# Use '/' to say: 'not'. Ex: 'region:europe, /imperium anglorum'.\n");
		recipientsPane.setText("# == Communiqué Recipients ==\n" + "# This tab shows all recipients after parsing of the Code tab.\n\n");

		try {
			clientField.setText(readProperties()); // Attempt to fetch client key.
		} catch (IOException e) {
			clientField.setText("Client Key");
		}

		util = new CommuniquéLogger(logPane, codePane);
		client = new JavaTelegram(util);
		parser = new CommuniquéParser(util);
	}

	@FXML protected void about(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION);

		alert.setTitle("About");
		alert.setHeaderText("Communique");
		alert.setContentText(
				"Version " + version + "\n\n" + "IC: Developed by His Grace, Cyril Parsons, the Duke of Geneva and the staff of the Democratic "
						+ "Empire of Imperium Anglorum's Delegation to the World Assembly.\n\n" + "OOC: Created by ifly6.");

		alert.showAndWait();
	}

	@FXML protected void exportLog(ActionEvent event) {
		try {

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Resource File");
			File file = fileChooser.showSaveDialog(null);

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			String header = "# Communiqué Log Export File. Produced by Communiqué version " + version;

			writer.println(header + "\n");
			writer.println("client_key=" + clientField.getText());
			writer.println("secret_key=" + secretField.getText());

			writer.println("telegram_id=" + telegramField.getText());
			writer.println("isRecruitment=" + recruitmentCheckBox.isSelected() + "\n");

			writer.println(logPane.getText());
			writer.println(codePane.getText());
			writer.println(recipientsPane.getText());

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			util.log("Internal Error. Could not exporting the log.");
		}
	}

	@FXML protected void importDelegates(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog("List of Delegates");
		dialog.setTitle("Input");
		dialog.setHeaderText("Paste in the Delegates listed on the relevant page.");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {

			String input = result.get();
			input = input.replaceAll("\\(.+?\\)", "");
			String[] list = input.split(",");

			for (String element : list) {
				util.codePrintln(element.toLowerCase().replace(" ", "_"));
			}
		}
	}

	@FXML protected void importURLKeys(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog("Telegram URL");
		dialog.setTitle("Input");
		dialog.setHeaderText("Paste in keys from the URL provided by receipt by the Telegrams API");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			String rawURL = result.get();

			// Verify that it is a valid NationStates URL
			if (rawURL.contains("http://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&")) {

				rawURL = rawURL.replace("http://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&", "");
				rawURL = rawURL.replace("&to=NATION_NAME", "");

				String[] tags = rawURL.split("&");
				telegramField.setText(tags[0].replace("tgid=", ""));
				secretField.setText(tags[1].replace("key=", ""));

			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Communiqué Error");
				alert.setHeaderText("Please input a correct URL.");
				alert.showAndWait();
			}
		}
	}

	@FXML protected void killSendingThread(ActionEvent event) {
		client.setKillThread(true);
		util.log("Kill signal sent. By next sending loop, it should be dead.");
	}

	@FXML protected void openConfiguration(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Open Resource File");
		File file = fileChooser.showOpenDialog(null);

		if (file != null) {
			try {
				CommuniquéFileReader fileReader = new CommuniquéFileReader(file);

				// Set Keys
				JTelegramKeys keys = fileReader.getKeys();
				clientField.setText(keys.getClientKey());
				secretField.setText(keys.getSecretKey());
				telegramField.setText(keys.getTelegramId());

				// Set Recruitment Flag
				recruitmentCheckBox.setSelected(fileReader.getRecruitmentFlag());

				// Set Recipients
				String[] recipients = fileReader.getRecipients();
				for (String element : recipients) {
					util.codePrintln(element);
				}

				util.log("Configuration loaded.");

			} catch (JTelegramException e) {
				util.log("Version of the provided file is not compatible.");
			} catch (FileNotFoundException e) {
				util.log("Cannot find file provided.");
			}
		}
	}

	@FXML protected void openWorkingDirectory(ActionEvent event) {
		try {
			Desktop.getDesktop().open(new File(System.getProperty("user.dir")));
		} catch (IOException e) {
			util.log("Cannot open working directory.");
		}
	}

	@FXML protected String[] parse(ActionEvent event) {
		String[] recipients = parser.recipientsParse(codePane.getText());	// Get recipients

		// Estimate Time Needed
		double numRecipients = recipients.length;
		int seconds = (int) Math.round(numRecipients * ((recruitmentCheckBox.isSelected()) ? 180.05 : 30.05));

		int minutes = seconds / 60;
		seconds -= minutes * 60;
		int hours = minutes / 60;
		minutes -= hours * 60;
		int days = hours / 24;
		hours -= days * 24;

		String timeNeeded = days + "d:" + hours + "h:" + minutes + "m:" + seconds + "s";

		// Show Recipients
		String recipient = "# == Communiqué Recipients ==\n" + "# This tab shows all " + recipients.length
				+ " recipients after parsing of the Code tab.\n# Estimated time needed is " + timeNeeded + "\n\n";
		for (String element : recipients) {
			recipient = recipient + element + "\n";
		}

		util.log("Recipients Parsed.");

		recipientsPane.setText(recipient);
		tabPane.getSelectionModel().select(2);

		return recipients;
	}

	@FXML protected void quit(ActionEvent event) {
		System.exit(0);
	}

	@FXML protected void saveConfiguration(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Open Resource File");
		File saveFile = fileChooser.showSaveDialog(null);

		// Make sure it ends in .txt
		if (!saveFile.toPath().endsWith(".txt")) {
			saveFile = new File(saveFile.toString() + ".txt");
		}

		if (saveFile != null) {	// In case they pressed cancel.
			try {

				CommuniquéFileWriter fileWriter = new CommuniquéFileWriter(saveFile);

				updateCode();

				fileWriter.setKeys(clientField.getText(), secretField.getText(), telegramField.getText());
				fileWriter.setBody(codePane.getText());
				fileWriter.setRecuitment(recruitmentCheckBox.isSelected());
				fileWriter.write();

				util.log("Configuration saved.");
			} catch (FileNotFoundException e1) {
				util.log("Cannot find the location of the selected document.");
			} catch (UnsupportedEncodingException e) {
				util.log("Encoding of selected document is not supported. Create a new savefile.");
			} catch (RuntimeException e) {
				util.log("Runtime exception occurred. Cannot save configuration file.");
			}
		}
	}

	@FXML protected void send(ActionEvent event) {
		if (!sendingThread.isAlive()) {
			client.setKillThread(false);

			Runnable runner = new Runnable() {
				@Override public void run() {
					String[] recipients = parse(event);	// Get recipients
					client.setRecipients(recipients);									// Set recipients

					client.setKeys(new JTelegramKeys(clientField.getText(), secretField.getText(), telegramField.getText()));

					// Set Recruitment Status
					client.setRecruitment(recruitmentCheckBox.isSelected());

					// Save client key
					try {
						writeProperties();
					} catch (IOException e) {
					}

					// In case you need a dry run, it will do everything but send.
					if (!disableSendingCheckBox.isSelected()) {
						client.connect();
						util.log("Queries Complete.");
					} else {
						util.log("Sending is disabled. Enable sending to send telegrams.");
					}

					// Update recipients pane.
					updateCode();
				}
			};

			sendingThread = new Thread(runner);
			sendingThread.start();
			tabPane.getSelectionModel().select(0);
		} else {
			util.log("There is already a campaign running. Terminate that campaign and then retry.");
		}
	}

	private void updateCode() {
		String[] sentList = client.getSentList();

		for (String element : sentList) {
			util.codePrintln("/" + element);
		}
	}

	/**
	 * Writes the standard configuration file for the currently used client key. Properties writing here has been
	 * localised for this setup using this method.
	 *
	 * @throws IOException
	 */
	private void writeProperties() throws IOException {
		Properties prop = new Properties();
		FileOutputStream output = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
		prop.setProperty("client_key", clientField.getText());
		prop.store(output, "");
		output.close();
	}

	/**
	 * Reads the standard configuration file for the last used client key. The method returns the client key from the
	 * configuration file.
	 *
	 * @return the client key from file
	 * @throws IOException if there was a problem in reading or finding the configuration file
	 */
	private String readProperties() throws IOException {
		Properties prop = new Properties();
		FileInputStream stream = new FileInputStream(new File(System.getProperty("user.dir") + "/config.properties"));
		prop.load(stream);
		String clientKey = prop.getProperty("client_key");
		if (clientKey != null && !clientKey.isEmpty()) {
			return clientKey;
		} else {
			return "Client Key";
		}
	}
}
