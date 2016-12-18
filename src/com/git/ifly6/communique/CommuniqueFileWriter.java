/* Copyright (c) 2015 ifly6
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

package com.git.ifly6.communique;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;

import com.git.ifly6.communique.data.CommuniqueParser;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.util.JTelegramException;

/**
 * Convenience class for correctly writing Communiqué configuration files. It is directly based on
 * <code>PrintWriter</code>. It utilises the encoding <code>UTF-8</code>.
 *
 * <p>
 * Note that this class will not automatically load and process any documents you give it when it is created. If you
 * want that behaviour to change, extend the class and write a new constructor to directly call the <code>write()</code>
 * method.
 * </p>
 *
 * @see CommuniqueFileWriter
 * @see CommuniqueParser
 */
@Deprecated public class CommuniqueFileWriter {
	
	// Requirements to Write
	PrintWriter writer;
	JTelegramKeys keys = new JTelegramKeys();
	String[] recipients = {};
	
	// Flags
	private boolean isRecruitment = true;
	private boolean randomSort = false;
	private boolean preexisting = false;
	
	// Preservation information
	private String[] originalHeader = {};
	private String[] originalFooter = {};
	
	/**
	 * This is the basic constructor, which initialises an empty CommuniquéFileWriter. After creating a
	 * CommuniquéFileWriter in this fashion, provide the keys, the state of the recruitment flag, and a
	 * <code>String[]</code> of recipients.
	 *
	 * @param file to which a Communiqué configuration file will be written
	 * @throws FileNotFoundException if there is no file there or the file cannot be written to
	 * @throws UnsupportedEncodingException if your computer does not support UTF-8 as a valid encoding
	 * @throws JTelegramException
	 */
	public CommuniqueFileWriter(File file) throws FileNotFoundException, UnsupportedEncodingException, JTelegramException {
		
		// Get file before destroying it
		if (file.exists()) {
			preexisting = true;
			CommuniqueFileReader reader = new CommuniqueFileReader(file);
			originalHeader = reader.getHeader();
			originalFooter = reader.getFooter();
		}
		
		writer = new PrintWriter(file, "UTF-8");
	}
	
	/**
	 * This is a more advanced constructor which initialises the keys, recruitment flag, and recipients list directly.
	 *
	 * @param file to which a Communiqué configuration file will be written
	 * @param providedKeys given for writing directly to configuration
	 * @param isRecruitment flag which will be written to the configuration
	 * @param bodyString the list of recipients in a <code>String</code> delimited by <code>\n</code>
	 * @throws FileNotFoundException if the FileWriter cannot write to the file
	 * @throws UnsupportedEncodingException if the FileWriter cannot write in UTF-8
	 */
	public CommuniqueFileWriter(File file, JTelegramKeys providedKeys, boolean isRecruitment, String[] bodyString)
			throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(file, "UTF-8");
		this.setKeys(providedKeys);
		this.setRecuitment(isRecruitment);
		this.setBody(bodyString);
	}
	
	/**
	 * Sets the keys inside a <code>JTelegramKeys</code> object which will then be written to disc.
	 *
	 * @param clientKey is the client key used when sending telegrams
	 * @param secretKey is the secret key used when sending telegrams
	 * @param telegramId is the key of the telegram sent to recipients
	 */
	public void setKeys(String clientKey, String secretKey, String telegramId) {
		keys.setClientKey(clientKey);
		keys.setSecretKey(secretKey);
		keys.setTelegramId(telegramId);
	}
	
	/**
	 * This is an old method to set the keys inside the new <code>JTelegramKeys</code> object which will then be written
	 * to disc. It was written to keep compatibility with API version 1.
	 *
	 * @param inputKeys a String array containing the keys in this order:
	 *            <code>{ clientKey, secretKey, telegramId }</code>
	 */
	@Deprecated public void setKeys(String[] inputKeys) {
		keys.setKeys(inputKeys);
	}
	
	/**
	 * Sets the keys inside a <code>JTelegramKeys</code> object which will then be written to disc.
	 *
	 * @param inputKeys is a <code>JTelegramKeys</code> object
	 */
	public void setKeys(JTelegramKeys inputKeys) {
		keys = inputKeys;
	}
	
	/**
	 * Sets the contents of the recipients.
	 *
	 * @param codeContents a <code>String[]</code> containing all of the recipients delimited by index.
	 */
	public void setBody(String[] codeContents) {
		recipients = codeContents;
	}
	
	/**
	 * Sets the contents of the recipients.
	 *
	 * @param codeContents a <code>String</code> containing all of the recipients delimited by <code>\n</code>
	 */
	public void setBody(String codeContents) {
		String[] contents = codeContents.split("\n");
		setBody(contents);
	}
	
	/**
	 * Sets the <code>isRecruitment</code> flag inside the object. This determines what delay timer the program is to
	 * use.
	 *
	 * @param recuitment is the flag sent to the writer
	 */
	public void setRecuitment(boolean recuitment) {
		isRecruitment = recuitment;
	}
	
	/**
	 * Sets the <code>randomSort</code> flag inside the object. This determines whether the program is to sort the list
	 * of recipients randomly.
	 *
	 * @param random is the flag sent to the writer
	 */
	public void setRandom(boolean random) {
		randomSort = random;
	}
	
	/**
	 * Instructs the instance of the <code>CommuniquéFileWriter</code> to write the given information to file. The
	 * instance automatically closes the created <code>PrintWriter</code>.
	 */
	public void write() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		if (preexisting) {
			for (String element : originalHeader) {
				writer.println(element);
			}
		} else {
			writer.println("# Communiqué Configuration File. Do not edit by hand.");
			writer.println("# Produced at: " + dateFormat.format(date));
			writer.println("# Produced by version " + CommuniqueParser.version);
		}
		
		writer.println();
		writer.println("version=" + CommuniqueParser.version);
		writer.println("client_key=" + keys.getClientKey());
		writer.println("secret_key=" + keys.getSecretKey());
		writer.println("telegram_id=" + keys.getTelegramId());
		writer.println("isRecruitment=" + isRecruitment);
		writer.println("randomSort=" + randomSort);
		writer.println();
		writer.println("# Recipients");
		
		// Ignore commented and empty lines.
		ArrayList<String> contentList = new ArrayList<String>(0);
		for (String element : recipients) {
			if (!element.startsWith("#") && !element.isEmpty() && !element.contains("=")) {
				contentList.add(element);
			}
		}
		
		// Sort out recipients from the sent and get rid of duplicates.
		LinkedHashSet<String> recpList = new LinkedHashSet<String>(0);
		LinkedHashSet<String> nopeList = new LinkedHashSet<String>(0);
		for (String element : contentList) {
			if (element.startsWith("/")) {
				nopeList.add(element);
			} else {
				recpList.add(element);
			}
		}
		
		// Print in the recipients
		for (String element : recpList) {
			writer.println(element);
		}
		
		writer.println("");
		
		// Print in the nopeList
		for (String element : nopeList) {
			writer.println(element);
		}
		
		if (preexisting) {
			writer.println();
			
			for (String element : originalFooter) {
				writer.println(element);
			}
			writer.println("# Changed at " + dateFormat.format(date) + " by version " + CommuniqueParser.version);
		}
		
		writer.close();
	}
}
