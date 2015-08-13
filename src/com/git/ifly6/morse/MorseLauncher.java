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

package com.git.ifly6.morse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.util.JTelegramException;

public class MorseLauncher {

	// TODO Rewrite this to operate based on configuration files from the GUI.

	static int version = 1;

	/**
	 * The keys are kept in the order: { client_key, secret_key, telegram_id }
	 */
	static String[] keys = { "", "", "" };
	static Scanner scan = new Scanner(System.in);

	static MorseUtilities util = new MorseUtilities();

	public static void main(String[] args) {
		// TODO Allow the input of direct files into the jar's CLI prompt to expedite things.

		System.out.println("------------------------------------------------------------------------------\n"
				+ "Welcome to NS Morse — a cross-platform telegram script for NationStates\n"
				+ "------------------------------------------------------------------------------\n");

		try {
			keys[0] = readProperties();

			String cKeyConfirm = util.prompt("Is this your client key? [Yes] or [No]? " + keys[0],
					new String[] { "yes", "no", "y", "n" });

			if (cKeyConfirm.startsWith("y")) {
				keys[1] = util.prompt("Type in your secret key. NationStates needs to verify that it is you.");
				keys[2] = util.prompt("Type in your telegram id. We need to know what to send.");
			} else {
				keys = manual_entry();
			}
		} catch (FileNotFoundException e) {
			util.log("Internal Error. Properties file does not exist. Please enter data manually.");
			keys = manual_entry();
		} catch (IOException e) {
			util.log("Internal Error. Information on that error is unknown.");
			keys = manual_entry();
		}

		// Create the client instance
		Morse client = new Morse(util);
		client.setKeys(new JTelegramKeys(keys[0], keys[1], keys[2]));

		if (args.length > 0) {
			util.log("Assuming that the file in the arguments is a recipients list (one recipient per line)");

			File location = new File(args[0]);
			ArrayList<String> contents = new ArrayList<String>(0);

			try {
				FileReader configRead = new FileReader(location.getCanonicalPath());
				Scanner scan = new Scanner(configRead);
				while (scan.hasNextLine()) {
					contents.add(scan.nextLine());
				}
				scan.close();
			} catch (IOException e) {
				util.log("Error. File read error. Does it exist?");
			}

			util.log("Attempted to Load Manual File.");
			String[] recipients = contents.toArray(new String[contents.size()]);

			client.setRecipients(recipients);

			String isRecruitment = util.prompt("Is this a recruitment telegram? [Yes] or [No]?",
					new String[] { "yes", "no", "y", "n" });
			if (isRecruitment.startsWith("y")) {
				client.setRecruitment(true);
			} else {
				client.setRecruitment(false);
			}

		} else {
			String recipient_type = util
					.prompt("To whom do you want to send these telegrams? [Delegates], [New] players, or [Manual]?",
							new String[] { "delegates", "new", "manual", "d", "n", "m" })
					.toLowerCase();

			// Expand the [d], [n], [m]
			if (recipient_type.equals("d")) {
				recipient_type = "delegates";
			} else if (recipient_type.equals("n")) {
				recipient_type = "new";
			} else if (recipient_type.equals("m")) {
				recipient_type = "manual";
			}

			// Set Recipients
			try {
				client.setRecipients(recipient_type);
			} catch (JTelegramException e) {
				util.log(
						"Internal Error. The type of elements which go into the recipients array was not specified correctly.");
			} catch (IOException e) {
				util.log(
						"Internal Error. Something went wrong in fetching the recipients. Check your internet connection.");
			}
		}

		// Send the telegrams
		client.connect();
		util.log("Queries Complete.");
	}

	private static String[] manual_entry() {
		String[] new_keys = new String[3];

		// Input information
		new_keys[0] = util.prompt("Type in your client key. NationStates needs to know who is sending.");
		new_keys[1] = util.prompt("Type in your secret key. NationStates needs to verify that it is you.");
		new_keys[2] = util.prompt("Type in your telegram id. We need to know what to send.");
		String save = util.prompt("Would you like to save your client key?", new String[] { "yes", "no", "y", "n" });

		// Do you want to save your choices?
		if (save.startsWith("y")) {
			try {
				writeProperties();
			} catch (IOException e) {
				util.log("Internal Error. Failed to write properties.");
			}
		} else {
			System.out.println(
					"Too bad. If you already have a file named 'config.properties', you don't even need to load up this prompt.");
		}

		return new_keys;
	}

	private static String readProperties() throws IOException, FileNotFoundException {
		Properties prop = new Properties();
		FileInputStream stream = new FileInputStream(new File(System.getProperty("user.dir") + "/config.properties"));

		util.log(System.getProperty("user.dir") + "/config.properties");

		prop.load(stream);

		return prop.getProperty("client_key");
	}

	private static void writeProperties() throws IOException {
		Properties prop = new Properties();
		FileOutputStream output = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");

		// Set Default Values
		prop.setProperty("client_key", keys[0]);

		// Save Properties
		prop.store(output, "== NationStates Morse Keys ==");
		output.close();

	}
}
