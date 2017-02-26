/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.ngui.Communique;
import com.sun.istack.internal.logging.Logger;

/** @author ifly6 */
public class CommuniqueUpdater {
	
	/** String for a pointing to the latest release of Communique. */
	public static final String LATEST_RELEASE = "https://github.com/iFlyCode/Communique/releases/latest";
	
	/** Path pointing to the application support folder, resolving a hidden file. */
	private static Path updatePath;
	
	/** Properties object to serialise and deserialise. */
	private CommuniqueUpdaterProperties updaterProps;
	
	/** Creates a new <code>CommuniqueUpdater</code> instance which loads the date at which it was last checked and
	 * whether it should continue checking into file. If an <code>IOException</code> occurs or there is no file, the
	 * program defaults to a last-checked date of now and defaults to continue checking. */
	public CommuniqueUpdater() {
		updatePath = Communique.appSupport.resolve(".updater-properties");
		updaterProps = new CommuniqueUpdaterProperties();	// create default values
		try {
			ObjectInputStream oiStream = new ObjectInputStream(Files.newInputStream(updatePath));
			updaterProps = (CommuniqueUpdaterProperties) oiStream.readObject();
		} catch (RuntimeException | IOException | ClassNotFoundException e) {
			Logger.getLogger(this.getClass()).info("Could not get updater properties.");
			// accept default values
		}
	}
	
	public CommuniqueUpdater(boolean continueChecking) {
		this();
		this.updaterProps.continueChecking = continueChecking;
	}
	
	/** Returns true if there is a major version update.
	 * @return <code>boolean</code> on whether there is a new major version update */
	public boolean checkForUpdate() {
		
		try {
			
			save();
			URLConnection connection = new URL(LATEST_RELEASE).openConnection();	// no need to rate-limit
			connection.connect();
			
			String html;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				html = reader.lines().collect(Collectors.joining("\n"));
			}
			
			Document doc = Jsoup.parse(html);
			Elements elements = doc.select("div.release-meta").select("span.css-truncate-target");
			String versionString = elements.first().text().trim().replace("v", "");
			
			int majorVersion = Integer.parseInt(versionString.substring(0,
					versionString.indexOf(".") == -1 ? versionString.length() : versionString.indexOf(".")));
			if (majorVersion > Communique7Parser.version) { return true; }	// if higher major version, return for
																				// update
			
			// TODO find some way to recognise a new minor version
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
	
	/** Returns <code>boolean</code> about whether there is a new update if and only if it has not been checked within
	 * the last week and there is a new major version update.
	 * @return <code>true</code> if there is a new update and it has not been checked within the last week */
	public boolean shouldRemind() {
		if (canCheck()) { return false; }
		return checkForUpdate();
	}
	
	/** Determines whether Communique has recently checked for an update. If it has checked within the last week, it
	 * will skip checking again.
	 * @return <code>boolean</code> about whether a check has been conducted within the last week */
	private boolean canCheck() {
		boolean notWithinWeek = new Date().getTime() - updaterProps.lastChecked.getTime() < 86400000 * 7;
		if (updaterProps.continueChecking && notWithinWeek) { return true; }
		return false;
	}
	
	/** Saves the check date along with the continue checking information. Note that this method uses the standard Java
	 * serialisation library. This is for two reasons: (1) Java Serialisation is faster than Gson and (2) it is harder
	 * to change Java Serialisation's data than it is JSON data.
	 * @see CommuniqueUpdaterProperties */
	private void save() {
		try {
			FileOutputStream foStream = new FileOutputStream(updatePath.toFile());
			ObjectOutputStream ooStream = new ObjectOutputStream(foStream);
			ooStream.writeObject(updaterProps);
			ooStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Sets the appropriate continue checking value and then saves it with a new check date
	public void setContinueReminding(boolean continueChecking) {
		this.updaterProps.continueChecking = continueChecking;
		save();
	}
	
}

class CommuniqueUpdaterProperties implements Serializable {
	
	private static final long serialVersionUID = Communique7Parser.version;
	Date lastChecked = new Date();
	boolean continueChecking = true;	// default values
	
}
