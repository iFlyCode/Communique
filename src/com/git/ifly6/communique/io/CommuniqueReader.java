/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.git.ifly6.communique.CommuniqueFileReader;
import com.git.ifly6.communique.CommuniqueUtils;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

// Suppresses deprecation, since it is supposed to read those deprecated files
@SuppressWarnings("deprecation") class CommuniqueReader {
	
	private Logger logger = Logger.getLogger(CommuniqueReader.class.getName());
	private Path path;
	
	CommuniqueReader(Path path) {
		this.path = path;
	}
	
	/** Reads data in the location specified in the path in the constructor. If necessary, it employs the methods
	 * declared in {@link Communique7Parser} to translate old Communique 6 tokens into the tokens introduced in
	 * Communique 7. It will also automatically decode old files which throw JSON errors using the deprecated
	 * {@link CommuniqueFileReader}.
	 * @return a {@link CommuniqueConfig} holding the data specified
	 * @throws IOException if there is an issue reading the data */
	CommuniqueConfig read() throws IOException {
		
		CommuniqueConfig config;
		
		try { // note, this will handle future version of the class by ignoring the now-irrelevant fields
			Gson gson = new Gson();
			config = gson.fromJson(Files.newBufferedReader(path), CommuniqueConfig.class);
			
			if (config.version == 7) { // convert from randomise flag to new enums
				List<String> lines = Files.readAllLines(path).stream()
						.map(String::trim)
						.collect(Collectors.toList());
				for (String line : lines)
					if (line.equals("\"isRandomised\": true,"))
						config.processingAction = CommuniqueProcessingAction.RANDOMISE;
			}
			
		} catch (JsonSyntaxException | JsonIOException e) {
			
			// If we are reading one of the old files, which would throw some RuntimeExceptions,
			// try the old reader.
			
			logger.log(Level.INFO, "Cannot load from JSON. Attempting with old file reader.", e);
			CommuniqueFileReader reader = new CommuniqueFileReader(path.toFile());
			
			config = new CommuniqueConfig();
			config.processingAction = reader.isRandomised()
					? CommuniqueProcessingAction.RANDOMISE
					: CommuniqueProcessingAction.NONE;
			config.isRecruitment = reader.isRecruitment();
			config.keys = reader.getKeys();
			
			List<String> recipients = new ArrayList<>(0);
			List<String> sentList = new ArrayList<>(0);
			
			for (String element : reader.getRecipients())
				if (!CommuniqueUtils.isEmpty(element) && !element.startsWith("#"))
					if (element.startsWith("/")) sentList.add(element);
					else recipients.add(element);
				
			recipients = Communique7Parser.translateTokens(recipients);
			sentList = Communique7Parser.translateTokens(sentList);
			
			config.version = reader.getFileVersion();
			
			config.recipients = recipients.toArray(new String[recipients.size()]);
			config.sentList = sentList.toArray(new String[sentList.size()]);
			config.setcRecipients(unifySendList(config));
			
		}
		
		// if necessary, translate tokens
		if (config.version < 7) {
			config.recipients = Communique7Parser
					.translateTokens(Arrays.asList(config.recipients))
					.stream().toArray(String[]::new);
			config.sentList = Communique7Parser
					.translateTokens(Arrays.asList(config.sentList))
					.stream().toArray(String[]::new);
		}
		
		// if necessary, generate cRecipients from String[]
		if (config.version < 7 || config.getcRecipientsString() == null || config.getcRecipientsString().isEmpty())
			config.setcRecipients(unifySendList(config));
		
		return config;
		
	}
	
	/** Parses and unifies {@link CommuniqueConfig#recipients} and {@link CommuniqueConfig#sentList} into a
	 * <code>List&lt;CommuniqueRecipient&gt;</code>
	 * @param config holding {@link CommuniqueConfig#recipients} and {@link CommuniqueConfig#sentList}
	 * @return <code>List&lt;CommuniqueRecipient&gt;</code> of their union */
	private List<CommuniqueRecipient> unifySendList(CommuniqueConfig config) {
		List<CommuniqueRecipient> list = new ArrayList<>(); // deal with null case, create new
		if (config.recipients != null && config.recipients.length != 0)
			list.addAll(Stream.of(config.recipients)    // add all recipients
					.map(CommuniqueRecipient::parseRecipient)
					.collect(Collectors.toList()));
		if (config.sentList != null && config.sentList.length != 0)
			list.addAll(Stream.of(config.sentList)  // translate, then change flag as necessary
					.map(CommuniqueRecipient::parseRecipient)
					.map(CommuniqueRecipients::exclude)
					.collect(Collectors.toList()));
		return list;
	}
	
}
