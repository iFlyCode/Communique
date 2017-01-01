/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.ngui.Communique;

/** @author ifly6 */
public class CommuniqueUpdater {
	
	private Date lastChecked;
	private boolean continueChecking;
	
	/** String for a pointing to the latest release of Communique. */
	public static final String LATEST_RELEASE = "https://github.com/iFlyCode/Communique/releases/latest";
	
	/** Path pointing to the application support folder, resolving a file called 'update-check-time'. */
	private static Path updatePath;
	
	/** Creates a new <code>CommuniqueUpdater</code> instance which loads the date at which it was last checked and
	 * whether it should continue checking into file. If an <code>IOException</code> occurs or there is no file, the
	 * program defaults to a last-checked date of now and defaults to continue checking. */
	public CommuniqueUpdater() {
		updatePath = Communique.appSupport.resolve("updater-log");
		
		// Load data from files, do in constructor because no need for remote
		lastChecked = new Date();
		continueChecking = true;
		try {
			if (Files.exists(updatePath)) {
				List<String> lines = Files.readAllLines(updatePath);
				lastChecked = new SimpleDateFormat(CommuniqueUtilities.getCurrentDateAndTimeFormat()).parse(lines.get(0));
				continueChecking = Boolean.parseBoolean(lines.get(1));
			}
		} catch (Exception e) {	// all exceptions
			// do nothing, accept default values
		}
	}
	
	public CommuniqueUpdater(boolean continueChecking) {
		this();
		this.continueChecking = continueChecking;
	}
	
	/** Returns true if there is a major version update.
	 * @return <code>boolean</code> on whether there is a new major version update */
	public boolean checkForUpdate() {
		
		try {
			
			saveCheckDate();
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
		if (continueChecking && new Date().getTime() - lastChecked.getTime() < 86400000 * 7) { return true; }
		return false;
	}
	
	// Saves the check date along with the continue checking information
	private void saveCheckDate() {
		try {
			String currentDateAndTime = CommuniqueUtilities.getCurrentDateAndTime();
			List<String> lines = Arrays.asList(currentDateAndTime, String.valueOf(continueChecking));
			Files.write(updatePath, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Sets the appropriate continue checking value and then saves it with a new check date
	public void setContinueReminding(boolean continueChecking) {
		this.continueChecking = continueChecking;
		saveCheckDate();
	}
	
}
