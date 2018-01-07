/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.marconi;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.ngui.AbstractCommunique;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;

public class Marconi extends AbstractCommunique implements JTelegramLogger {
	
	private static final Logger LOGGER = Logger.getLogger(Marconi.class.getName());
	private static FileHandler handler;
	
	private JavaTelegram client = new JavaTelegram(this);
	private CommuniqueConfig config;
	
	private boolean skipChecks = false;
	private boolean recruiting = false;
	
	public Marconi(boolean skipChecks, boolean recruiting) {
		this.skipChecks = skipChecks;
		this.recruiting = recruiting;
		
		try {
			handler = new FileHandler(Paths.get("marconi-last-session.log").toString()); // save: default directory
			handler.setFormatter(new SimpleFormatter());
			Logger.getGlobal().addHandler(handler);
			
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void send() {
		
		// Process the Recipients list into a string with two columns.
		Communique7Parser parser = new Communique7Parser();
		List<String> expandedRecipients = parser.apply(config.getcRecipients()).getRecipients();
		
		// Apply processing action
		expandedRecipients = config.processingAction.apply(expandedRecipients);
		
		// Show the recipients in the order we are to send the telegrams.
		System.out.println();
		for (int x = 0; x < expandedRecipients.size(); x = x + 2)
			try {
				System.out.printf("%-30.30s  %-30.30s%n", expandedRecipients.get(x), expandedRecipients.get(x + 1));
			} catch (IndexOutOfBoundsException e) {
				System.out.printf(expandedRecipients.get(x) + "\n");
			}
		
		System.out.println();
		System.out.println(String.format("This will take %s to send %d telegrams",
				CommuniqueUtilities.time(Math.round(expandedRecipients.size()
						* (config.isRecruitment ? JavaTelegram.RECRUIT_TIME / 1000 : JavaTelegram.CAMPAIGN_TIME / 1000))),
				expandedRecipients.size()));
		
		if (!skipChecks) {
			// Give a chance to check the recipients.
			String recipientsReponse = MarconiUtilities
					.promptYN("Are you sure you want to send to these recipients? [Yes] or [No]?");
			if (recipientsReponse.startsWith("n")) System.exit(0);
		}
		
		// Set the client up and go.
		client.setKeys(config.keys);
		client.setRecruitment(config.isRecruitment);
		client.setRecipients(expandedRecipients);
		
		// Check for file lock
		if (!MarconiUtilities.isFileLocked()) client.connect();
		else throw new RuntimeException("Cannot send, as another instance of Marconi is already sending.");
	}
	
	/** Should the problem be prompted to manually check all flags, this method does so, retrieving the flags and asking
	 * for the user to reconfirm them. */
	public void manualFlagCheck() {
		
		if (!skipChecks) {
			// Give a chance to check the keys.
			String keysResponse = MarconiUtilities.promptYN(String
					.format("Are these keys correct? %s, %s, %s [Yes] or [No]", config.keys.getClientKey(),
							config.keys.getSecretKey(), config.keys.getTelegramId()));
			if (!keysResponse.startsWith("y")) return;
			
			if (!recruiting) {
				// Confirm the recruitment flag.
				while (true) {
					String recruitResponse = MarconiUtilities.promptYN(String
							.format("Is the recruitment flag (%s) set correctly? [Yes] or [No]?",
									String.valueOf(config.isRecruitment)));
					if (recruitResponse.startsWith("n")) config.isRecruitment = !config.isRecruitment;
					else if (recruitResponse.startsWith("y")) break;
				}
				
				// Confirm the randomisation flag.
				while (true) {
					String randomResponse = MarconiUtilities.promptYN(String
							.format("Do you want to apply the %s processing action?",
									String.valueOf(config.processingAction)));
					if (randomResponse.startsWith("n")) return;
					else if (randomResponse.startsWith("y")) break;
				}
			}
			
		}
	}
	
	/** Note that this will not return what is loaded. It will return a sentList whose duplicates have been removed and,
	 * if any elements start with a negation <code>/</code>, it will remove it.
	 * @see com.git.ifly6.communique.ngui.AbstractCommunique#exportState() */
	@Override public CommuniqueConfig exportState() {
		// Remove duplicates from the sentList as part of save action
		config.setcRecipients(config.getcRecipients().stream().distinct().collect(Collectors.toList()));
		return config;
		
	}
	
	/** @see com.git.ifly6.communique.ngui.AbstractCommunique#importState(com.git.ifly6.communique.io.CommuniqueConfig) */
	@Override public void importState(CommuniqueConfig config) {
		this.config = config;
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String) */
	@Override public void log(String input) {
		LOGGER.info(input);
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
	@Override public void sentTo(String nationName, int x, int length) {
		config.addcRecipient(CommuniqueRecipients.createExcludedNation(nationName));
	}
}
