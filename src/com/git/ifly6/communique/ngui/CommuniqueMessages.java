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
package com.git.ifly6.communique.ngui;

import java.io.InputStream;
import java.util.Scanner;

/**
 * <code>CommuniqueMessages</code> holds <code>String</code>s for various Communique messages.
 */
public class CommuniqueMessages {

    public static final String TITLE = "Communiqué";
    public static final String ERROR = "Communiqué Error";
    public static final String UPDATER = "Communiqué Updater";
    public static final String RECRUITER = "Communiqué Recruiter";

    private static String licence;

    // Prevent initialisation
    private CommuniqueMessages() {
    }

    public static final String acknowledgement =
            "Developed by ifly6, contributing to the repository at "
                    + "[github.com/iflycode/communique], also known as the nation Imperium Anglorum on "
                    + "NationStates.\n\nMy thanks to bug-reporters Tinfect, Krypton Nova, Separatist Peoples, "
                    + "Wallenburg, and Tinhampton.";

    /**
     * Gives the licence information that is saved in the file 'licences' in this source directory.
     * @return licence information
     */
    public static String getLicence() {
        if (licence == null) {
            InputStream resourceInputStream = CommuniqueMessages.class.getResourceAsStream("licence");
            try (Scanner s = new Scanner(resourceInputStream)) {
                licence = s.useDelimiter("\\A").hasNext() ? s.next() : "Could not load licence.";
            }
        }
        return licence;
    }
}
