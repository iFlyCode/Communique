/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
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
import com.git.ifly6.nsapi.NSConnection;

/** @author ifly6 */
public class CommuniqueUpdater {
	
	private static CommuniqueUpdater instance;
	
	/** String for a pointing to the latest release of Communique. */
	public static final String LATEST_RELEASE = "https://github.com/iFlyCode/Communique/releases/latest";
	
	/** Path pointing to the application support folder, resolving a file called 'update-check-time'. */
	private static Path updatePath;
	
	private CommuniqueUpdater() {
		updatePath = Communique.appSupport.resolve("update-check-time");
	}
	
	/** @return */
	public static CommuniqueUpdater getInstance() {
		if (instance == null) {
			instance = new CommuniqueUpdater();
		}
		return instance;
	}
	
	public boolean forceHasNewUpdate() {
		
		try {
			
			saveChecked();
			NSConnection connection = new NSConnection(LATEST_RELEASE);
			String html = connection.getResponse();
			
			Document doc = Jsoup.parse(html);
			Elements elements = doc.select("div.release-meta").select("span.css-truncate-target");
			String versionString = elements.first().text().trim().replace("v", "");
			
			int majorVersion = Integer.parseInt(versionString.substring(0,
					versionString.indexOf(".") == -1 ? versionString.length() : versionString.indexOf(".")));
			if (majorVersion > Communique7Parser.version) { return true; }	// if higher major version, hasNewUpdate
			
			// TODO find some way to recognise a new minor version
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean hasNewUpdate() {
		// Check for recent update check
		if (isRecentlyChecked()) { return false; }
		return forceHasNewUpdate();
	}
	
	/** Determines whether Communique has recently checked for an update. If it has checked within the last week, it
	 * will skip checking again.
	 * @return <code>boolean</code> about whether a check has been conducted within the last week */
	private boolean isRecentlyChecked() {
		
		String dateTimeFormat = CommuniqueUtilities.getCurrentDateAndTimeFormat();
		SimpleDateFormat format = new SimpleDateFormat(dateTimeFormat);
		Date date = new Date();
		try {
			date = format.parse(Files.readAllLines(updatePath).stream().collect(Collectors.joining()).trim());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			return false;
		}
		
		Date now = new Date();	// 86400000 milliseconds in a day
		if (now.getTime() - date.getTime() < 86400000 * 7) { return true; }
		
		return false;
	}
	
	private void saveChecked() {
		try {
			List<String> lines = Arrays.asList(CommuniqueUtilities.getCurrentDateAndTime());
			Files.write(updatePath, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}