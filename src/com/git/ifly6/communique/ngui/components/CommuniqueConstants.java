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

package com.git.ifly6.communique.ngui.components;

import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class CommuniqueConstants {
    public static final int COMMAND_KEY = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    public static final String CODE_HEADER =
            "# == Communiqué Recipients Syntax ==\n"
                    + "# Enter recipients, separated by comma or new lines. Please\n"
                    + "# read the readme at \n"
                    + "# [ https://github.com/iflycode/communique#readme ]\n\n";

    public static final String INTERNET_ERROR = "NationStates appears down from your location.\n" +
            "To send any telegrams, we must be able to connect to NationStates.";

    public static URI GITHUB_URI;
    public static URI FORUM_THREAD;

    static {
        try {
            FORUM_THREAD = new URI("https://forum.nationstates.net/viewtopic.php?f=15&t=352065");
            GITHUB_URI = new URL("https://github.com/iFlyCode/Communique").toURI();
        } catch (URISyntaxException | MalformedURLException ignored) {
        }
    }
}
