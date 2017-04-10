/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique;

import com.git.ifly6.communique.data.CommuniqueParser;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.util.JTelegramException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/** This class has been deprecated. Please see {@link com.git.ifly6.communique.io.CommuniqueLoader CLoader}. Note that this is
 * still in line with Communique 4 and 5's configuration files, and therefore, because it is still used to read those
 * configuration files when necessary, should not be changed.
 * <p>
 * <strike>Convenience class for correctly loading, deciphering, and verifying Communiqué configuration files. It is
 * directly based on <code>FileReader</code> so it assumes the default encoding and byte-buffer sizes. Note that this
 * class will automatically load and process any documents you give it when it is created. If you want that behaviour to
 * change, extend the class and write a new constructor.</strike>
 * </p>
 * @see CommuniqueFileWriter
 * @see CommuniqueParser
 * @see com.git.ifly6.communique.io.CommuniqueLoader CLoader */
@Deprecated public class CommuniqueFileReader {
	
	List<String> fileContents = new ArrayList<>(0);
	
	private boolean recruitment;
	private boolean randomised;
	
	/** <code>information</code> encapsulates the returning information of the JTelegramKeys and the recipients. */
	Object[] information = { new JTelegramKeys(), new String[] {} };
	
	/** Constructs a FileReader tailored to the correct file and loads the entire file into an ArrayList. From there, it
	 * calls <code>parseConfig()</code> to load all the processed information into an accessible object.
	 * @param file of the Communiqué configuration file
	 * @throws FileNotFoundException if the Communiqué configuration file is non-existent or unwritable
	 * @throws JTelegramException if the version is incorrect */
	public CommuniqueFileReader(File file) throws FileNotFoundException, JTelegramException {
		
		// Immediately load the file into memory.
		FileReader configRead = new FileReader(file);
		Scanner scan = new Scanner(configRead);
		
		while (scan.hasNextLine()) {
			fileContents.add(scan.nextLine().trim());
		}
		scan.close();
		
		if (isCompatible()) {
			information = parseConfig();
		} else {
			throw new JTelegramException("Communiqué file version mismatch");
		}
	}
	
	/** Gets the keys from <code>information</code> which is returned in a JTelegramKeys object containing the keys
	 * which were written onto the configuration file.
	 * @return <code>JTelegramKeys</code> containing the keys */
	public JTelegramKeys getKeys() {
		return (JTelegramKeys) information[0];
	}
	
	/** Gets the list of recipients from <code>information</code> which was written on the configuration file.
	 * <p>
	 * The file structure is pretty simple. It uses comments and tags to store all information which are not the list of
	 * recipients, as the list of recipients is everything but those two tag types. The tags used here are like the
	 * property tags, <code>isRecruitment</code>, for example. Anything which starts with a <code>#</code> character is
	 * also ignored. Everything else, as long as it is not a new line, is returned. All of this is implemented in the
	 * configuration parser in this class.
	 * </p>
	 * @return <code>String[]</code> containing every entry of the configuration file except the commented or empty
	 *         lines
	 * @see #parseConfig */
	public String[] getRecipients() {
		return (String[]) information[1];
	}
	
	/** Gets the flag <code>isRecruitment</code> which was loaded off the provided configuration file.
	 * @return <code>boolean</code> containing the contents of <code>isRecruitment</code> */
	public boolean isRecruitment() {
		return recruitment;
	}
	
	/** Gets the flag <code>randomSort</code> which was loaded off the provided configuration file.
	 * @return <code>boolean</code> containing the contents of <code>randomSort</code> */
	public boolean isRandomised() {
		return randomised;
	}
	
	/** Parses the entire configuration file by searching out the <code>client_key</code> and other such keys, ignores
	 * lines which start with <code>#</code> and then returns everything else as the recipients list.
	 * @return <code>Object[]</code> which replaces the default initialisers of <code>information</code> which is then
	 *         referenced by other methods in this class. */
	private Object[] parseConfig() {
		JTelegramKeys keys = new JTelegramKeys();
		List<String> recipientsList = new ArrayList<>(0);
		
		for (String element : fileContents) {
			element = element.trim();
			
			if (element.startsWith("client_key=")) {
				keys.setClientKey(element.replace("client_key=", ""));
				
			} else if (element.startsWith("secret_key=")) {
				keys.setSecretKey(element.replace("secret_key=", ""));
				
			} else if (element.startsWith("telegram_id=")) {
				keys.setTelegramId(element.replace("telegram_id=", ""));
				
			} else if (element.startsWith("isRecruitment=")) {
				recruitment = Boolean.parseBoolean(element.replace("isRecruitment=", ""));
				
			} else if (element.startsWith("randomSort=")) {
				randomised = Boolean.parseBoolean(element.replace("randomSort=", ""));
				
			} else if (!element.startsWith("#") && !element.isEmpty() && !element.contains("=")) {
				recipientsList.add(element.toLowerCase().trim().replace(" ", "_"));
			}
		}
		
		return new Object[] { keys, recipientsList.toArray(new String[recipientsList.size()]) };
	}
	
	/** Queries the file for an integer version to determine whether it is compatible with this parser. If so, it
	 * returns true. Otherwise, it will return false. This operation also effectively makes sure that there is a file
	 * which can be read.
	 * @return <code>boolean</code> containing true or false on whether the configuration file is compatible. */
	public boolean isCompatible() {
		return getFileVersion() < 7;    // changed
	}
	
	/** Finds the file version declarer by finding the line which states "# Produced by version" or the version tag. The
	 * following is an integer which determines which version of the program this file was made by. Returns its
	 * contents.
	 * @return <code>String</code> containing the ending of the commented version line */
	public int getFileVersion() {
		
		// Look for version tag first
		for (String element : fileContents) {
			if (element.startsWith("version")) { return Integer.parseInt(element.replace("version=", "")); }
		}
		
		// If the version tag does not yet exist, look for header version tag
		for (String element : fileContents) {
			if (element.startsWith(
					"# Produced by version ")) { return Integer.parseInt(element.replace("# Produced by version ", "")); }
		}
		
		return 0;
	}
	
	/** Gets the header of the entire file (that is, all comments before the first real entry) and returns it in a
	 * String array.
	 * @return the header of the file in <code>String[]</code> format */
	public String[] getHeader() {
		ArrayList<String> header = new ArrayList<>();
		String[] filteredContents = fileContents.stream().filter(s -> s.trim().length() != 0).toArray(String[]::new);

		for (String filteredContent : filteredContents) {
			if (!filteredContent.startsWith("#")) {
				// When comments end, break.
				break;
			} else {
				header.add(filteredContent);
			}
		}
		
		return header.toArray(new String[header.size()]);
	}
	
	/** Gets the footer sections of the file (that is, all comments after the last real entry) and returns it in a
	 * String array.
	 * @return the footer of the file in <code>String[]</code> format */
	public String[] getFooter() {
		ArrayList<String> header = new ArrayList<>();
		String[] tempContents = fileContents.toArray(new String[fileContents.size()]);
		String[] filteredContents = Stream.of(tempContents)
				.filter(s -> s.trim().length() != 0)
				.toArray(String[]::new);
		
		for (int i = filteredContents.length - 1; i >= 0; i--) {
			// Start from the bottom and read commented lines.
			
			if (!filteredContents[i].startsWith("#")) {
				// When those commented lines terminate, break.
				break;
			} else {
				header.add(filteredContents[i]);
			}
		}
		
		Collections.reverse(header);
		
		return header.toArray(new String[header.size()]);
	}
}
