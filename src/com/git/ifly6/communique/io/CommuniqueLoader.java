/*
 * Copyright (c) 2020 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.communique.io;

import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.telegram.JTelegramException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static com.git.ifly6.communique.ngui.components.CommuniqueLAF.appSupport;

/**
 * {@link CommuniqueLoader} is a class allowing the easy abstraction of access to a single point and simplifying the
 * process of reading and writing to that data. It uses the {@link CommuniqueReader} and {@link CommuniqueWriter}
 * classes to access that data. They are based on reading and writing via use of Google's {@link com.google.gson.Gson}
 * library, which then allow for reading, writing, and manipulation of a {@link com.git.ifly6.communique.io.CommuniqueConfig}
 * object.
 */
public class CommuniqueLoader {

    private Path path;

    /** @see CommuniqueLoader(Path) */
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
            FileOutputStream output = new FileOutputStream(appSupport.resolve("config.properties").toFile());
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
            prop.load(new FileInputStream(appSupport.resolve("config.properties").toFile()));
            String clientKey = prop.getProperty("clientKey");
            return ApiUtils.isEmpty(clientKey) ? "Client Key" : clientKey;
        } catch (IOException e) {
            return "";
        }
    }
}
