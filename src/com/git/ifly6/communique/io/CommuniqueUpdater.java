/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.ngui.Communique;

/** @author ifly6 */
public class CommuniqueUpdater {
	
	private static final Logger LOGGER = Logger.getLogger(CommuniqueUpdater.class.getName());
	
	/** String for a pointing to the latest release of Communique. */
	public static final String LATEST_RELEASE = "https://github.com/iFlyCode/Communique/releases/latest";
	
	/** Path pointing to the application support folder, resolving a hidden file. */
	private static Path updatePath = Communique.appSupport.resolve(".updater-properties");
	
	/** Properties object to serialise and deserialise. */
	private CommuniqueUpdaterProperties updaterProps = new CommuniqueUpdaterProperties();
	
	/** Creates a new <code>CommuniqueUpdater</code> instance which loads the date at which it was last checked and
	 * whether it should continue checking into file. If an <code>IOException</code> occurs or there is no file, the
	 * program defaults to a last-checked date of now and defaults to continue checking. */
	public CommuniqueUpdater() {
		try {
			ObjectInputStream oiStream = new ObjectInputStream(Files.newInputStream(updatePath));
			updaterProps = (CommuniqueUpdaterProperties) oiStream.readObject();	// replace original
			
		} catch (NoSuchFileException | FileNotFoundException e) {
			LOGGER.info(String.format("Updater file not found, %s", updatePath.toString()));
		} catch (RuntimeException | IOException | ClassNotFoundException e) {
			LOGGER.info("Runtime exception in getting updater properties");
			e.printStackTrace();
			// accept default values
		}
	}
	
	/** Sets the appropriate continue checking value and then saves it with a new check date */
	public void stopReminding() {
		this.updaterProps.continueChecking = false;
		save();
	}
	
	/** Returns true if there is a major version update.
	 * @return <code>boolean</code> if there is a new major version update */
	public boolean hasUpdate() {
		
		try {
			Document doc = Jsoup.connect(LATEST_RELEASE).get(); // get Communique releases page
			Elements elements = doc.select("div.release-meta").select("span.css-truncate-target");
			String versionString = elements.first().text().trim().replace("v", "");
			
			updaterProps.lastChecked = Instant.now(); // set last updated to now
			save();
			
			int majorVersion = Integer.parseInt(versionString.substring(0,
					versionString.contains(".") ? versionString.indexOf(".") : versionString.length()));
			if (majorVersion > Communique7Parser.version)
				return true;
			
			// TODO find some way to recognise a new minor version
			
		} catch (IOException e) {
			LOGGER.info("Cannot access Github releases page, IO exception");
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
	
	/** Returns <code>boolean</code> about whether there is a new update if and only if it has not been checked within
	 * the last week and there is a new major version update.
	 * @return <code>true</code> if there is a new update and it has not been checked within the last week */
	public boolean shouldRemind() {
		return !recentCheck() && hasUpdate() && updaterProps.continueChecking;
	}
	
	/** Determines whether Communique has recently checked for an update. If it has checked within the last week, it
	 * will skip checking again.
	 * @return <code>boolean</code> about whether a check has been conducted within the last week */
	private boolean recentCheck() {
		return Instant.now().compareTo(updaterProps.lastChecked.plus(1, ChronoUnit.WEEKS)) > 0;
		// is now after the last checked date + 1 week?
	}
	
	/** Saves the check date along with the continue checking information. Note that this method uses the standard Java
	 * serialisation library. This is for two reasons: (1) Java Serialisation is faster than Gson and (2) it is harder
	 * to change Java Serialisation's data than it is JSON data.
	 * @see CommuniqueUpdaterProperties */
	private void save() {
		LOGGER.info("Saving Communique updater properties");
		try {
			FileOutputStream foStream = new FileOutputStream(updatePath.toFile());
			ObjectOutputStream ooStream = new ObjectOutputStream(foStream);
			ooStream.writeObject(updaterProps);
			ooStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

/** Basically a C-style <code>struct</code> for easily serialising information on the last time an update was checked
 * and whether Communique should continue checking. In {@link CommuniqueUpdater#updatePath}, it is saved to an invisible
 * file in the application support director. */
class CommuniqueUpdaterProperties implements Serializable {
	
	private static final long serialVersionUID = Communique7Parser.version;
	Instant lastChecked = Instant.MIN;
	boolean continueChecking = true;	// default values
	
}
