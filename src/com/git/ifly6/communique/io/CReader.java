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
package com.git.ifly6.communique.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.git.ifly6.communique.CommuniqueFileReader;
import com.git.ifly6.communique.data.Communique7Parser;
import com.google.gson.Gson;

// Suppresses deprecation, since it is supposed to read those deprecated files
@SuppressWarnings("deprecation") class CReader {
	
	Logger logger = Logger.getLogger(CReader.class.getName());
	Path path;
	
	public CReader(Path path) {
		this.path = path;
	}
	
	/** Reads data in the location specified in the path in the constructor. If necessary, it employs the methods
	 * declared in {@link Communique7Parser} to translate old Communique 6 tokens into the tokens introduced in
	 * Communique 7. It will also automatically decode old files which throw JSON errors using the deprecated
	 * {@link CommuniqueFileReader}.
	 * @return a <code>CConfig</code> holding the data specified
	 * @throws IOException if there is an issue reading the data */
	public CConfig read() throws IOException {
		
		try {
			
			Gson gson = new Gson();
			CConfig config = gson.fromJson(Files.newBufferedReader(path), CConfig.class);
			
			// if necessary, translate tokens
			if (config.version < 7) {
				config.recipients = Communique7Parser.translateTokens(Arrays.asList(config.recipients)).stream()
						.toArray(String[]::new);
				config.sentList = Communique7Parser.translateTokens(Arrays.asList(config.sentList)).stream()
						.toArray(String[]::new);
			}
			
			return config;
			
		} catch (RuntimeException e) {
			
			// If we are reading one of the old files, which would throw some RuntimeExceptions,
			// try the old reader.
			
			logger.log(Level.INFO, "Cannot load from JSON. Attempting with old file reader.", e);
			CommuniqueFileReader reader = new CommuniqueFileReader(path.toFile());
			
			CConfig config = new CConfig();
			config.isDelegatePrioritised = false;	// this flag did not exist, thus, default to false.
			config.isRandomised = reader.isRandomised();
			config.isRecruitment = reader.isRecruitment();
			config.keys = reader.getKeys();
			
			List<String> recipients = new ArrayList<>(0);
			List<String> sentList = new ArrayList<>(0);
			
			for (String element : reader.getRecipients()) {
				if (!StringUtils.isEmpty(element) && !element.startsWith("#")) {
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
