/* Copyright (c) 2015 ifly6
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

package com.git.ifly6.marconi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.git.ifly6.communique.CommuniqueParser;
import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.io.CConfig;
import com.git.ifly6.communique.ngui.AbstractCommunique;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;

public class Marconi extends AbstractCommunique implements JTelegramLogger {
	
	private JavaTelegram client = new JavaTelegram(this);
	private CConfig config;
	
	private boolean skipChecks = false;
	private boolean recruiting = false;
	
	public Marconi(boolean skipChecks, boolean recruiting) {
		this.skipChecks = skipChecks;
		this.recruiting = recruiting;
	}
	
	public void send() {
		
		// Process the Recipients list into a string with two columns.
		CommuniqueParser parser = new CommuniqueParser();
		List<String> expandedRecipients = parser.recipientsParse(Arrays.asList(config.recipients),
				Arrays.asList(config.sentList));
		
		// If it needs to be randomised, do so.
		if (config.isRandomised) {
			Collections.shuffle(expandedRecipients);
		}
		
		// Show the recipients in the order we are to send the telegrams.
		System.out.println();
		for (int x = 0; x < expandedRecipients.size(); x = x + 2) {
			try {
				System.out.printf("%-30.30s  %-30.30s%n", expandedRecipients.get(x), expandedRecipients.get(x + 1));
			} catch (IndexOutOfBoundsException e) {
				System.out.printf(expandedRecipients.get(x) + "\n");
			}
		}
		
		System.out.println();
		System.out
				.println(
						"This will take "
								+ CommuniqueUtilities.time((int) Math
										.round(expandedRecipients.size() * (config.isRecruitment ? 180.05 : 30.05)))
								+ " to send " + expandedRecipients.size() + " telegrams.");
		
		if (!skipChecks) {
			// Give a chance to check the recipients.
			String recipientsReponse = MarconiUtilities
					.promptYN("Are you sure you want to send to these recipients? [Yes] or [No]?");
			if (recipientsReponse.startsWith("n")) {
				System.exit(0);
			}
		}
		
		// Set the client up and go.
		client.setKeys(config.keys);
		client.setRecruitment(config.isRecruitment);
		client.setRecipients(expandedRecipients);
		
		client.connect();
	}
	
	/** Should the problem be prompted to manually check all flags, this method does so, retrieving the flags and asking
	 * for the user to reconfirm them. */
	public void manualFlagCheck() {
		
		if (!skipChecks) {
			
			// Give a chance to check the keys.
			String keysResponse = MarconiUtilities.promptYN("Are these keys correct? " + config.keys.getClientKey() + ", "
					+ config.keys.getSecretKey() + ", " + config.keys.getTelegramId() + " [Yes] or [No]?");
			if (!keysResponse.startsWith("y")) { return; }
			
			if (!recruiting) {
				// Confirm the recruitment flag.
				while (true) {
					String recruitResponse = MarconiUtilities.promptYN(
							"Is the recruitment flag (" + config.isRecruitment + ") set correctly? [Yes] or [No]?");
					if (recruitResponse.startsWith("n")) {
						config.isRecruitment = !config.isRecruitment;
					} else if (recruitResponse.startsWith("y")) {
						break;
					}
				}
				
				// Confirm the randomisation flag.
				while (true) {
					String randomResponse = MarconiUtilities.promptYN(
							"Is the randomisation flag (" + config.isRandomised + ") set correctly? [Yes] or [No]?");
					if (randomResponse.startsWith("n")) {
						config.isRandomised = !config.isRandomised;
					} else if (randomResponse.startsWith("y")) {
						break;
					}
				}
			}
		}
	}
	
	/** Note that this will not return what is loaded. It will return a sentList whose duplicates have been removed and,
	 * if any elements start with a negation <code>/</code>, it will remove it.
	 * @see com.git.ifly6.communique.ngui.AbstractCommunique#exportState() */
	@Override public CConfig exportState() {
		
		// Remove duplicates from the sentList
		config.sentList = Stream.of(config.sentList).distinct().map(s -> s.startsWith("/") ? s.substring(1) : s)
				.toArray(String[]::new);
		
		return config;
		
	}
	
	/** @see com.git.ifly6.communique.ngui.AbstractCommunique#importState(com.git.ifly6.communique.io.CConfig) */
	@Override public void importState(CConfig config) {
		this.config = config;
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String) */
	@Override public void log(String input) {
		
		// If we are recruiting, suppress the API Queries message
		if (recruiting) {
			if (input.equals("API Queries Complete.")) { return; }
		}
		
		System.out.println("[" + MarconiUtilities.currentTime() + "] " + input);
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
	@Override public void sentTo(String recipient, int x, int length) {
		config.sentList = ArrayUtils.add(config.sentList, recipient);
	}
}
