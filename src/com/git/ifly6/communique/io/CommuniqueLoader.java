/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import com.git.ifly6.communique.CommuniqueUtils;
import com.git.ifly6.communique.ngui.Communique;
import com.git.ifly6.javatelegram.JTelegramException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * {@link CommuniqueLoader} is a class allowing the easy abstraction of access to a single point and simplifying the
 * process of reading and writing to that data. It uses the {@link CommuniqueReader} and {@link CommuniqueWriter}
 * classes to access that data. They are based on reading and writing via use of Google's {@link com.google.gson.Gson}
 * library, which then allow for reading, writing, and manipulation of a {@link com.git.ifly6.communique.io.CommuniqueConfig}
 * object.
 */
public class CommuniqueLoader {

	private Path path;

	// Force initialisation with appropriate variables
	@SuppressWarnings(value = {"unused"})
	private CommuniqueLoader() {
	}

	/**
	 * Creates the {@link CommuniqueLoader} and sets the path at which the program will do its file operations. The
	 * Communique program attempts to default this to the relevant application support folder, resolved to the
	 * Communique folder, by specifying such in the program's myriad file dialog prompts.
	 * @param path to examine
	 */
	public CommuniqueLoader(Path path) {
		this.path = path;
	}

	/**
	 * Saves a configuration file based on the provided {@link CommuniqueConfig}.
	 * @param config to save
	 * @throws IOException given IO error
	 */
	public void save(CommuniqueConfig config) throws IOException {
		CommuniqueWriter writer = new CommuniqueWriter(path, config);
		writer.write();
	}

	/**
	 * Loads a configuration file to a new CConfig.
	 * @return a {@link CommuniqueConfig} based on the loaded data from disc
	 * @throws IOException given IO error
	 */
	public CommuniqueConfig load() throws IOException {
		CommuniqueReader reader = new CommuniqueReader(path);
		return reader.read();
	}

	/**
	 * Writes the standard configuration file for the currently used client key. Properties writing here has been
	 * localised for this setup using this method.
	 */
	public static void writeProperties(String clientKey) {
		try {
			Properties prop = new Properties();
			prop.setProperty("clientKey", clientKey);
			FileOutputStream output = new FileOutputStream(Communique.appSupport.resolve("config.properties").toFile());
			prop.store(output, "Communique Properties");
			output.close();
		} catch (IOException e) {
			throw new JTelegramException("Cannot write Communique properties", e);
		}
	}

	/**
	 * Returns the last used client key from the configuration file.
	 * @return the client key from file
	 */
	public static String getClientKey() {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(Communique.appSupport.resolve("config.properties").toFile()));
			String clientKey = prop.getProperty("clientKey");
			return CommuniqueUtils.isEmpty(clientKey) ? "Client Key" : clientKey;
		} catch (IOException e) {
			return "";
		}
	}
}
