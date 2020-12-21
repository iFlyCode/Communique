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

import com.git.ifly6.communique.io.CommuniqueVersionException;
import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class has been deprecated. Reads old Communique configuration files.
 * <p>See {@link com.git.ifly6.communique.io.CommuniqueLoader}. Note that this is still in line with Communique 4 and
 * 5's configuration files, and therefore, because it is still used to read those configuration files when necessary,
 * should not be changed.</p>
 * @see com.git.ifly6.communique.io.CommuniqueLoader
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class CommuniqueFileReader {

    private List<String> fileContents;

    private boolean recruitment;
    private boolean randomised;

    private Map.Entry<JTelegramKeys, List<String>> keysAndList;

    /**
     * Reads the file to {@code List<String>}. Then parses information and makes accessible by get methods.
     * @param file location of  configuration file
     * @throws FileNotFoundException      if thrown by {@link FileReader}
     * @throws CommuniqueVersionException if the build is incorrect
     */
    public CommuniqueFileReader(File file) throws FileNotFoundException, CommuniqueVersionException {
        // load the file into memory
        BufferedReader br = new BufferedReader(new FileReader(file));
        fileContents = br.lines().collect(Collectors.toList());
        if (isCompatible()) keysAndList = parseConfig();
        else throw new CommuniqueVersionException("Communiqué file build mismatch");
    }


    /**
     * Gets the client, secret, and telegram id keys.
     * @return {@link JTelegramKeys} containing the keys
     */
    public JTelegramKeys getKeys() {
        return keysAndList.getKey();
    }

    /**
     * Gets the list of recipient in configuration file.
     * <p>
     * The file structure is pretty simple. It uses comments and tags to store all information which are not the list of
     * recipients, as the list of recipients is everything but those two tag types. The tags used here are like the
     * property tags, {@code isRecruitment}, for example. Anything which starts with a {@code #} character is ignored.
     * Everything else, as long as it is not a new line, is returned.
     * </p>
     * @return array of recipients
     * @see #parseConfig
     */
    public String[] getRecipients() {
        return keysAndList.getValue().toArray(new String[0]);
    }

    /**
     * Gets flag {@code isRecruitment} from configuration file.
     * @return true if recruiting
     */
    public boolean isRecruitment() {
        return recruitment;
    }

    /**
     * Gets flag {@code randomSort} loaded from the provided configuration file.
     * @return true if preference is randomised sending order
     */
    public boolean isRandomised() {
        return randomised;
    }

    /**
     * Parses the entire configuration file by searching out the {@code client_key} and other such keys, ignores lines
     * which start with {@code #} and then returns everything else as the recipients list.
     * @return tuple with {@link JTelegramKeys} and {@code List<String>}
     */
    private Map.Entry<JTelegramKeys, List<String>> parseConfig() {
        // ignores # implicitly in last element of switch
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

        return new AbstractMap.SimpleEntry<>(keys, recipientsList);
    }

    /**
     * Queries the file for build number to determine whether it is compatible with this parser.
     * @return true if compatible
     */
    public boolean isCompatible() {
        return getFileBuild() < 7;    // changed from original
    }

    /**
     * Checks the version tag. Otherwise, finds file build by parsing text for line
     * <pre># Produced by version INT</pre>
     * @return build number
     */
    public long getFileBuild() {
        // Look for version tag first
        for (String element : fileContents)
            if (element.startsWith("version"))
                return Long.parseLong(element.replace("version=", "").trim());

        // If the version tag does not yet exist, look for header version tag
        for (String element : fileContents)
            if (element.startsWith("# Produced by version "))
                return Long.parseLong(element.replace("# Produced by version ", "").trim());

        return 0;
    }

}
