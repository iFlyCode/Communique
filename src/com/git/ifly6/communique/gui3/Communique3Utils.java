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
import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.RoundedBalloonStyle;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
            } catch (IOException ignored) { }
        }
        return new CommuniqueConfig();
    }

    /**
     * Saves the provided {@link CommuniqueConfig} in the autosave location
     * @param config to save
     */
    public static void saveAutoSave(CommuniqueConfig config) {
        CommuniqueLoader loader = new CommuniqueLoader(AUTOSAVE_PATH);
        try {
            loader.save(config);
        } catch (IOException ignored) { }
    }

    /**
     * Gets selected element from {@link JComboBox}. This honestly should be in the standard library as an instance
     * method. I don't know why you have to roll your own.
     * @param box with item selected
     * @param <T> type of object
     * @return selected item
     */
    public static <T> T getComboBoxSelection(JComboBox<T> box) {
        int selection = Objects.requireNonNull(box).getSelectedIndex();
        if (selection != -1 && selection < box.getModel().getSize()) return box.getItemAt(selection);
        throw new IllegalArgumentException(String.format("Selection value %d is is invalid", selection));
    }

    /**
     * Sets up minimum dimensions for provided {@link Window}.
     * @param window      to set up
     * @param minimumSize of window
     * @param initialSize of window
     * @param centering   true if centered on screen
     */
    public static void setupDimensions(Window window, Dimension minimumSize, Dimension initialSize,
                                       boolean centering) {
        window.setMinimumSize(minimumSize);
        window.setPreferredSize(initialSize);
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

    /**
     * Creates balloon tip with the specified message, emanating from the provided component. Tip has auto-scheduled
     * destruction after two seconds.
     * @param component to attach to
     * @param message   to give
     */
    public static void createBalloonTip(JComponent component, String message) {
        EventQueue.invokeLater(() -> {
            // create
            BalloonTip t = new BalloonTip(component, message,
                    new RoundedBalloonStyle(5, 5,
                            UIManager.getColor("Label.background"),
                            Color.DARK_GRAY),
                    false);

            // schedule its doing away after 2 seconds
            Executors.newSingleThreadScheduledExecutor().schedule(() ->
                            EventQueue.invokeLater(t::closeBalloon),
                    2, TimeUnit.SECONDS);
        });
    }
}
