/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.git.ifly6.communique.CommuniqueFileReader;
import com.git.ifly6.communique.CommuniqueUtils;
import com.git.ifly6.communique.data.Communique7Parser;
import com.google.gson.Gson;

// Suppresses deprecation, since it is supposed to read those deprecated files
@SuppressWarnings("deprecation") class CommuniqueReader {
	
	Logger logger = Logger.getLogger(CommuniqueReader.class.getName());
	Path path;
	
	public CommuniqueReader(Path path) {
		this.path = path;
	}
	
	/** Reads data in the location specified in the path in the constructor. If necessary, it employs the methods
	 * declared in {@link Communique7Parser} to translate old Communique 6 tokens into the tokens introduced in
	 * Communique 7. It will also automatically decode old files which throw JSON errors using the deprecated
	 * {@link CommuniqueFileReader}.
	 * @return a <code>CConfig</code> holding the data specified
	 * @throws IOException if there is an issue reading the data */
	public CommuniqueConfig read() throws IOException {
		
		try {
			
			Gson gson = new Gson();
			CommuniqueConfig config = gson.fromJson(Files.newBufferedReader(path), CommuniqueConfig.class);
			
			// if necessary, translate tokens
			if (config.version < 7) {
				config.recipients = Communique7Parser
						.translateTokens(Arrays.asList(config.recipients))
						.stream().toArray(String[]::new);
				config.sentList = Communique7Parser
						.translateTokens(Arrays.asList(config.sentList))
						.stream().toArray(String[]::new);
			}
			
			return config;
			
		} catch (RuntimeException e) {
			
			// If we are reading one of the old files, which would throw some RuntimeExceptions,
			// try the old reader.
			
			logger.log(Level.INFO, "Cannot load from JSON. Attempting with old file reader.", e);
			CommuniqueFileReader reader = new CommuniqueFileReader(path.toFile());
			
			CommuniqueConfig config = new CommuniqueConfig();
			config.isDelegatePrioritised = false;	// this flag did not exist, thus, default to false.
			config.isRandomised = reader.isRandomised();
			config.isRecruitment = reader.isRecruitment();
			config.keys = reader.getKeys();
			
			List<String> recipients = new ArrayList<>(0);
			List<String> sentList = new ArrayList<>(0);
			
			for (String element : reader.getRecipients()) {
				if (!CommuniqueUtils.isEmpty(element) && !element.startsWith("#")) {
					if (element.startsWith("/")) {
						sentList.add(element);
					} else {
						recipients.add(element);
					}
				}
			}
			
			recipients = Communique7Parser.translateTokens(recipients);
			sentList = Communique7Parser.translateTokens(sentList);
			
			config.recipients = recipients.toArray(new String[recipients.size()]);
			config.sentList = sentList.toArray(new String[sentList.size()]);
			config.version = reader.getFileVersion();
			
			return config;
			
		}
	}
	
}
