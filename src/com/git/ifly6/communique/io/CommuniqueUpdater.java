/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.ngui.Communique;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An object which deals with updating the program. It handles when to check, how to persist the data on checking, and
 * also whether to remind the user of any updates. Currently, it doesn't actually update the program itself, but simply
 * informs users that there is one.
 * @author ifly6
 */
public class CommuniqueUpdater {

	private static final Logger LOGGER = Logger.getLogger(CommuniqueUpdater.class.getName());

	/** String for a pointing to the latest release of Communique. */
	public static final String LATEST_RELEASE = "https://github.com/iFlyCode/Communique/releases/latest";

	/** Path pointing to the application support folder, resolving a hidden file. */
	private static Path updatePath = Communique.appSupport.resolve(".updater-properties");

	/** Properties object to serialise and deserialise. */
	private CommuniqueUpdaterProperties updaterProps = new CommuniqueUpdaterProperties();

	/**
	 * Creates a new <code>CommuniqueUpdater</code> instance which loads the date at which it was last checked and
	 * whether it should continue checking into file. If some exception occurs or there is no file, the program defaults
	 * to a last-checked date of now and defaults to continue checking.
	 */
	public CommuniqueUpdater() {
		try {
			updaterProps = new Gson().fromJson(Files.newBufferedReader(updatePath), CommuniqueUpdaterProperties.class);

		} catch (NoSuchFileException | FileNotFoundException e) {
			LOGGER.info(String.format("Updater file not found, %s", updatePath.toString()));

		} catch (IOException e) {
			LOGGER.log(Level.INFO, "IOException in reading updater properties", e);


		} catch (RuntimeException e) {
			LOGGER.log(Level.INFO, "Runtime exception in getting updater properties", e);
		}

		// if exception, re-assignment doesn't happen.
		// thus, keep default properties
	}

	/** Sets program to remind of updates */
	public void remindMe() {
		this.updaterProps.continueChecking = true;
		save();
	}

	/** Sets program not to remind of updates */
	public void stopReminding() {
		this.updaterProps.continueChecking = false;
		save();
	}

	/**
	 * Checks whether there is a major version update.
	 * @return <code>true</code> if there is a new major version update
	 */
	public boolean hasUpdate() {

		try {
			LOGGER.info("Checking for new major version");

			Document doc = Jsoup.connect(LATEST_RELEASE).get(); // get Communique releases page
			Elements elements = doc.select("div.release-meta").select("span.css-truncate-target");
			String versionString = elements.first().text().trim().replace("v", "");

			LOGGER.info("Loaded and parsed. Updating lastChecked variable");
			updaterProps.lastChecked = Instant.now(); // set last updated to now
			save();

			int majorVersion = Integer.parseInt(versionString.substring(0,
					versionString.contains(".") ? versionString.indexOf(".") : versionString.length()));
			if (majorVersion > Communique7Parser.version) {
				LOGGER.info("Found new major version");
				return true;
			}

			// TODO find some way to recognise a new minor version

		} catch (IOException e) {
			LOGGER.info("Cannot access Github releases page, IO exception");
			e.printStackTrace();
			return false;
		}

		return false;
	}

	/**
	 * Checks whether the program should remind the user of a new update based on three criteria: (1) there has not been
	 * a recent check, (2) there is in fact a new update, and (3) the user has not turned off checking.
	 * @return if all those criteria are true
	 * @see CommuniqueUpdater#recentCheck()
	 */
	public boolean shouldRemind() {
		return !recentCheck() && hasUpdate() && updaterProps.continueChecking;
	}

	/**
	 * Determines whether Communique has recently checked for an update. If it has checked within the last three days,
	 * it returns <code>true</code>.
	 * @return <code>true</code> if a check has been conducted in the last three days
	 */
	private boolean recentCheck() {
		return Instant.now().compareTo(updaterProps.lastChecked.plus(3, ChronoUnit.DAYS)) > 0;
		// is now after the last checked date + 3 days?
		// max supported ChronoUnit size is DAYS
	}

	/**
	 * Saves the check date along with the continue checking information.
	 * <p>In former versions, this used Java's standard serialisation. This was changed to use standard JSON on
	 * 2018-05-26.</p>
	 * @see CommuniqueLoader
	 * @see CommuniqueUpdaterProperties
	 */
	private void save() {
		LOGGER.info("Saving Communique updater properties");
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			Files.write(updatePath, Arrays.asList(gson.toJson(updaterProps).split("\n")));
		} catch (IOException e) {
			LOGGER.warning("Could not have Communique updater properties");
			e.printStackTrace();
		}
	}

}

/**
 * Basically a C-style <code>struct</code> for easily serialising information on the last time an update was checked and
 * whether Communique should continue checking. In {@link CommuniqueUpdater#updatePath}, it is saved to an invisible
 * file in the application support director.
 */
class CommuniqueUpdaterProperties implements Serializable {
	private static final long serialVersionUID = Communique7Parser.version;
	Instant lastChecked = Instant.MIN; // default value
	boolean continueChecking = true; // default value
}
