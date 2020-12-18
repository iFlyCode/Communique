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

package com.git.ifly6.marconi;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class MarconiUtilities {

    private static final Logger LOGGER = Logger.getLogger(MarconiUtilities.class.getName());

    static Path lockFile = Paths.get(System.getProperty("user.dir"), "marconi.lock");

    /**
     * Creates Marconi lock file.
     * @since version 3.0 (build 13)
     */
    static void createFileLock() {
        try {
            if (!Files.exists(lockFile)) {
                Files.write(lockFile, Collections.singletonList(
                        String.format("marconi. started %s.", CommuniqueUtilities.getDate())));
                lockFile.toFile().deleteOnExit(); // delete lock file when marconi closes!
            }
        } catch (IOException e) {
            LOGGER.severe("Cannot create lock file!");
            e.printStackTrace();
        }
    }

    /**
     * Determines whether there is another instance of Marconi which is already sending.
     * @return boolean, whether lock file already exists
     */
    static boolean isFileLocked() {
        return Files.exists(lockFile);
    }

    /** @return name of the jar in which Marconi is located. */
    static String getJARName() {
        try {
            return new File(Marconi.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getName();
        } catch (URISyntaxException e) {
            return String.format("Marconi_%d.jar", Communique7Parser.BUILD); // default to standard naming format.
        }
    }

    /** Creates two column string.
     * @since version 3.0 (build 13) */
    static String twoColumn(List<String> items) {
        List<String> lines = new ArrayList<>();
        for (int x = 0; x < items.size(); x = x + 2)
            try {
                lines.add(String.format("%-30.30s  %-30.30s", items.get(x), items.get(x + 1)));
            } catch (IndexOutOfBoundsException e) {
                lines.add(items.get(x)); // odd number of entries
            }
        return String.join("\n", lines);
    }
}
