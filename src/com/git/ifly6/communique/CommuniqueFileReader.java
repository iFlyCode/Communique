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

package com.git.ifly6.communique;

import com.git.ifly6.communique.data.CommuniqueParser;
import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.telegram.JTelegramException;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class has been deprecated. Please see {@link com.git.ifly6.communique.io.CommuniqueLoader CLoader}. Note that
 * this is still in line with Communique 4 and 5's configuration files, and therefore, because it is still used to read
 * those configuration files when necessary, should not be changed.
 * <p>
 * <strike>Convenience class for correctly loading, deciphering, and verifying Communiqué configuration files. It is
 * directly based on <code>FileReader</code> so it assumes the default encoding and byte-buffer sizes. Note that this
 * class will automatically load and process any documents you give it when it is created. If you want that behaviour to
 * change, extend the class and write a new constructor.</strike>
 * </p>
 * @see CommuniqueParser
 * @see com.git.ifly6.communique.io.CommuniqueLoader CLoader
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class CommuniqueFileReader {

    List<String> fileContents = new ArrayList<>(0);

    private boolean recruitment;
    private boolean randomised;

    Object[] information = {new JTelegramKeys(), new String[] {}};

    /**
     * Constructs a FileReader tailored to the correct file and loads the entire file into an ArrayList. From there, it
     * calls <code>parseConfig()</code> to load all the processed information into an accessible object.
     * @param file of the Communiqué configuration file
     * @throws FileNotFoundException if the Communiqué configuration file is non-existent or unwritable
     * @throws JTelegramException    if the version is incorrect
     */
    public CommuniqueFileReader(File file) throws FileNotFoundException, JTelegramException {
        // Immediately load the file into memory.
        Scanner scan = new Scanner(new FileReader(file));
        while (scan.hasNextLine())
            fileContents.add(scan.nextLine().trim());

        scan.close();

        if (isCompatible()) {
            information = parseConfig();
        } else {
            throw new JTelegramException("Communiqué file version mismatch");
        }
    }

    /**
     * Gets the keys from <code>information</code> which is returned in a JTelegramKeys object containing the keys which
     * were written onto the configuration file.
     * @return <code>JTelegramKeys</code> containing the keys
     */
    public JTelegramKeys getKeys() {
        return (JTelegramKeys) information[0];
    }

    /**
     * Gets the list of recipients from <code>information</code> which was written on the configuration file.
     * <p>
     * The file structure is pretty simple. It uses comments and tags to store all information which are not the list of
     * recipients, as the list of recipients is everything but those two tag types. The tags used here are like the
     * property tags, <code>isRecruitment</code>, for example. Anything which starts with a <code>#</code> character is
     * also ignored. Everything else, as long as it is not a new line, is returned. All of this is implemented in the
     * configuration parser in this class.
     * </p>
     * @return <code>String[]</code> containing every entry of the configuration file except the commented or empty
     * lines
     * @see #parseConfig
     */
    public String[] getRecipients() {
        return (String[]) information[1];
    }

    /**
     * Gets the flag <code>isRecruitment</code> which was loaded off the provided configuration file.
     * @return <code>boolean</code> containing the contents of <code>isRecruitment</code>
     */
    public boolean isRecruitment() {
        return recruitment;
    }

    /**
     * Gets the flag <code>randomSort</code> which was loaded off the provided configuration file.
     * @return <code>boolean</code> containing the contents of <code>randomSort</code>
     */
    public boolean isRandomised() {
        return randomised;
    }

    /**
     * Parses the entire configuration file by searching out the <code>client_key</code> and other such keys, ignores
     * lines which start with <code>#</code> and then returns everything else as the recipients list.
     * @return <code>Object[]</code> which replaces the default initialisers of <code>information</code> which is then
     * referenced by other methods in this class.
     */
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
                recipientsList.add(ApiUtils.ref(element));
            }
        }

        return new Object[] {keys, recipientsList.toArray(new String[0])};
    }

    /**
     * Queries the file for an integer version to determine whether it is compatible with this parser. If so, it returns
     * true. Otherwise, it will return false. This operation also effectively makes sure that there is a file which can
     * be read.
     * @return <code>boolean</code> containing true or false on whether the configuration file is compatible.
     */
    public boolean isCompatible() {
        return getFileVersion() < 7;    // changed from original
    }

    /**
     * Finds the file version declarer by finding the line which states "# Produced by version" or the version tag. The
     * following is an integer which determines which version of the program this file was made by. Returns its
     * contents.
     * @return <code>String</code> containing the ending of the commented version line
     */
    public int getFileVersion() {
        // Look for version tag first
        for (String element : fileContents)
            if (element.startsWith("version"))
                return Integer.parseInt(element.replace("version=", ""));

        // If the version tag does not yet exist, look for header version tag
        for (String element : fileContents)
            if (element.startsWith("# Produced by version "))
                return Integer.parseInt(element.replace("# Produced by version ", ""));

        return 0;
    }

}
