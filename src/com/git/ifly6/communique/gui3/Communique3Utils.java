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

package com.git.ifly6.communique.gui3;

import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;

import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static com.git.ifly6.commons.CommuniqueApplication.APP_SUPPORT;

/**
 * Graphical utilities for Communique 3.
 * @since version 3.0 (build 13)
 */
public class Communique3Utils {

    private static final Logger LOGGER = Logger.getLogger(Communique3Utils.class.getName());
    private static final Path AUTOSAVE_PATH = APP_SUPPORT.resolve("autosave.txt");

    private static final Dimension MINIMUM_SIZE = new Dimension(600, 400);
    private static final Dimension SCREEN_DIMENSIONS = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * Appends line to {@link JTextArea}. Automatically moves the caret to the bottom.
     * @param area   to append to
     * @param object string representation thereof to append
     */
    public static void appendLine(JTextArea area, Object object) {
        area.append("\n" + object.toString());
        area.setCaretPosition(area.getDocument().getLength());
    }

    /**
     * Loads {@link CommuniqueConfig} from autosave location.
     * @return autosaved configuration file
     */
    public static CommuniqueConfig loadAutoSave() {
        if (Files.exists(APP_SUPPORT.resolve("autosave.txt"))) {
            CommuniqueLoader loader = new CommuniqueLoader(AUTOSAVE_PATH);
            try {
                LOGGER.info("Loading auto-save");
                return loader.load();
            } catch (IOException ignored) {

            }
        }
        return new CommuniqueConfig();
    }

    public static void saveAutoSave(CommuniqueConfig config) {
        CommuniqueLoader loader = new CommuniqueLoader(AUTOSAVE_PATH);
        try {
            loader.save(config);
        } catch (IOException ignored) { }
    }

    /**
     * Sets up dimensions for provided {@link Window}.
     * @param window to set up
     */
    public static void setupDimensions(Window window) {
        setupDimensions(window, MINIMUM_SIZE, MINIMUM_SIZE, false);
    }

    /**
     * Sets up minimum dimensions for provided {@link Window}.
     * @param window      to set up
     * @param minimumSize for window
     * @param centering   true if centered on screen
     */
    public static void setupDimensions(Window window, Dimension minimumSize, Dimension initialSize,
                                       boolean centering) {
        window.setMinimumSize(minimumSize);
        window.setSize(initialSize);
        if (centering)
            window.setBounds(
                    (int) (SCREEN_DIMENSIONS.getWidth() / 2 - initialSize.getWidth() / 2),
                    (int) (SCREEN_DIMENSIONS.getHeight() / 2 - initialSize.getHeight() / 2),
                    (int) initialSize.getWidth(),
                    (int) initialSize.getHeight());
        else
            window.setBounds(100, 100,
                    (int) Math.round(SCREEN_DIMENSIONS.getWidth() / 2),
                    (int) Math.round(SCREEN_DIMENSIONS.getHeight() / 2));
    }
}
