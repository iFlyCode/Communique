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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.git.ifly6.communique.ngui.Communique;

/** <code>CLoader</code> is a class allowing the easy abstraction of access to a single point and simplifying the
 * process of reading and writing to that data. It uses the <code>{@link CReader}</code> and
 * <code>{@link CWriter}</code> classes to access that data. They are based on reading and writing via use of Google's
 * Gson library, which then allow for reading, writing, and manipulation of a {@link com.git.ifly6.communique.io.CConfig
 * CConfig} object. */
public class CLoader {
	
	Path path;
	
	// Force initialisation with appropriate variables
	@SuppressWarnings(value = { "unused" }) private CLoader() {
	}
	
	/** Creates the <code>CLoader</code> and sets the path at which the program will do its file operations. The
	 * Communique program attempts to default this to the relevant application support folder, resolved to the
	 * Communique folder, by specifying such in the program's myriad file dialog prompts.
	 *
	 * @param path */
	public CLoader(Path path) {
		this.path = path;
	}
	
	/** Saves a configuration file based on the provided <code>CCoNfig</code>.
	 *
	 * @param config
	 * @throws IOException */
	public void save(CConfig config) throws IOException {
		CWriter writer = new CWriter(path, config);
		writer.write();
	}
	
	/** Loads a configuration file to a new CConfig.
	 *
	 * @return a <code>CConfig</code> based on the loaded data from disc
	 * @throws IOException */
	public CConfig load() throws IOException {
		CReader reader = new CReader(path);
		return reader.read();
	}
	
	/** Writes the standard configuration file for the currently used client key. Properties writing here has been
	 * localised for this setup using this method.
	 *
	 * @throws IOException */
	public static void writeProperties(String clientKey) {
		
		try {
			
			Properties prop = new Properties();
			FileOutputStream output = new FileOutputStream(Communique.appSupport + "/config.properties");
			prop.setProperty("clientKey", clientKey);
			prop.store(output, "");
			output.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Reads the standard configuration file for the last used client key. The method returns the client key from the
	 * configuration file.
	 *
	 * @return the client key from file
	 * @throws IOException if there was a problem in reading or finding the configuration file */
	public static String readProperties() {
		
		Properties prop = new Properties();
		
		try {
			FileInputStream stream = new FileInputStream(Communique.appSupport + "/config.properties");
			prop.load(stream);
			
		} catch (IOException e) {
			return "Client Key";
		}
		
		String clientKey = prop.getProperty("clientKey");
		return StringUtils.isEmpty(clientKey) ? "Client Key" : clientKey;
	}
}
