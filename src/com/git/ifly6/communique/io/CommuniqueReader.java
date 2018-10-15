/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import com.git.ifly6.communique.CommuniqueFileReader;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.data.FilterType;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * {@link CommuniqueReader} reads Communique configuration files. It contains some backwards compatibility for the last
 * version of the pre-JSON configuration files. The handling for this logic is done primarily in the {@link
 * CommuniqueReader#read()} file.
 * @see CommuniqueFileReader
 * @see com.git.ifly6.communique.CommuniqueFileWriter
 */
@SuppressWarnings("deprecation")
		// Suppress deprecation as we use dep. classes
class CommuniqueReader {

	private Logger logger = Logger.getLogger(CommuniqueReader.class.getName());

	/**
	 * {@link Path} where the class is pointed to
	 */
	private Path path;

	CommuniqueReader(Path path) {
		this.path = path;
	}

	/**
	 * Reads data in the location specified in the path in the constructor. If necessary, it employs the methods
	 * declared in {@link Communique7Parser} to translate old Communique 6 tokens into the tokens introduced in
	 * Communique 7. It will also automatically decode old files which throw JSON errors using the deprecated {@link
	 * CommuniqueFileReader}.
	 * @return a {@link CommuniqueConfig} holding the data specified
	 * @throws IOException if there is an issue reading the data
	 */
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

			logger.log(Level.INFO, "Json exception thrown. Attempting read with old file reader.", e);
			CommuniqueFileReader reader = new CommuniqueFileReader(path.toFile());

			config = new CommuniqueConfig();
			config.version = reader.getFileVersion();
			config.processingAction = reader.isRandomised() // translate old boolean flag
					? CommuniqueProcessingAction.RANDOMISE
					: CommuniqueProcessingAction.NONE;
			config.isRecruitment = reader.isRecruitment();
			config.keys = reader.getKeys();

			List<CommuniqueRecipient> recipients = CommuniqueRecipient.translateTokens(asList(reader.getRecipients())).stream()
					.map(CommuniqueRecipient::parseRecipient)
					.collect(Collectors.toList());
			config.setcRecipients(recipients);

		}

		// There can be old files in Json which predate the introduction of CommuniqueRecipient in version 7
		// Those files need to be translated too, which is problematic due to the creation of an explicit difference
		// in those version between the standard recipients list and the sent-list.
		if (config.version < 7)
			config = unifySendList(config);

		return config;
	}

	/**
	 * Parses and unifies {@link CommuniqueConfig#recipients} and {@link CommuniqueConfig#sentList} into a
	 * <code>List&lt;CommuniqueRecipient&gt;</code>
	 * <p>This exists because of an interim period before the creation of {@link CommuniqueRecipient} but after {@link
	 * CommuniqueConfig} was turned into a serialised JSON file. In that period, strings in the old recipients format
	 * were stored in the two (now-deprecated) <code>recipients</code> and <code>sentList</code> fields. Since it is
	 * necessary to turn those all into the standard <code>CommuniqueRecipient</code>, keep this method.</p>
	 * @param config holding {@link CommuniqueConfig#recipients} and {@link CommuniqueConfig#sentList}
	 * @return a translated configuration with a unified list from the two old lists
	 */
	private CommuniqueConfig unifySendList(CommuniqueConfig config) {
		if (config.getcRecipients() == null) { // only if null is this necessary
			List<CommuniqueRecipient> list = new ArrayList<>();

			// if recipients contains things, lets deal with that
			if (config.recipients != null && config.recipients.length != 0) {
				List<String> recipients = CommuniqueRecipient.translateTokens(asList(config.recipients));
				list.addAll(recipients.stream()    // add all recipients
						.map(CommuniqueRecipient::parseRecipient)
						.map(r -> CommuniqueRecipients.setFilter(r, FilterType.NORMAL))
						.collect(Collectors.toList()));
			}

			// if sentList contains things, deal with that
			if (config.sentList != null && config.sentList.length != 0) {
				List<String> sentList = CommuniqueRecipient.translateTokens(asList(config.sentList));
				list.addAll(sentList.stream()  // translate, then change flag as necessary
						.map(CommuniqueRecipient::parseRecipient)
						.map(CommuniqueRecipients::exclude) // manual cast if necessary
						.collect(Collectors.toList()));
			}

			config.setcRecipients(list);
		}

		return config;
	}

}
