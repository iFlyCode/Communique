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

import com.git.ifly6.commons.CommuniqueUtilities;
import com.git.ifly6.communique.ngui.CommuniqueConstants;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles dialogs for the constructing class.
 * @since version 3.0 (build 13)
 */
@SuppressWarnings("ClassWithMultipleLoggers")
public class Communique3DialogHandler {

    private static final Logger LOGGER = Logger.getLogger(Communique3DialogHandler.class.getName());
    private static int MAX_DIALOG_WIDTH = (int) Math.round(
            Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 4);

    private Frame frame;

    @SuppressWarnings("NonConstantLogger")
    private final Logger frameLogger;

    /**
     * Construct dialog handler
     * @param frame  is root frame for modality
     * @param logger logger for the root frame
     */
    public Communique3DialogHandler(Frame frame, Logger logger) {
        this.frame = frame;
        frameLogger = logger;
    }

    /**
     * Shows provided message.
     * @param message to show
     * @param title   of dialog
     */
    public void showMessageDialog(String message, String title) {
        JOptionPane.showMessageDialog(frame, formatMessage(message), title, JOptionPane.PLAIN_MESSAGE, null);
    }

    /**
     * Shows error message as dialog and prints to provided logger.
     * @param message to show
     * @param causal  exception
     */
    public void showErrorDialog(String message, Throwable causal) {
//        JLabel label = new JLabel(
//                String.format("<html>Regex pattern syntax error. <br /><pre>%s</pre></html>",
//                        StringEscapeUtils.escapeHtml4(causal.getMessage()).replace("\n", "<br />"))
//        );
        JLabel messageLabel = formatMessage(message + "\n\n"
                + "<pre>" + causal.getClass().getSimpleName() + "\n\n"
                + causal.getMessage() + "</pre>");
        JOptionPane.showMessageDialog(frame, messageLabel, CommuniqueConstants.ERROR,
                JOptionPane.PLAIN_MESSAGE, null);
        frameLogger.log(Level.SEVERE, message, causal);
    }

    /**
     * Shows text input dialog with provided text and title.
     * @param message to show
     * @param title   of dialogue
     * @return input text
     */
    public Optional<String> showTextInputDialog(String message, String title) {
        return Optional.ofNullable(
                JOptionPane.showInputDialog(frame, formatMessage(message), title, JOptionPane.PLAIN_MESSAGE));
    }

    /**
     * Shows dialog to input text; title is {@value CommuniqueConstants#TITLE}.
     * @returns input text
     */
    public Optional<String> showTextInputDialog(String message) {
        // message already formatted
        return showTextInputDialog(message, CommuniqueConstants.TITLE);
    }

    /** Shows dialog to choose from enumerated choices. */
    public Optional<Object> showChoiceSelector(String message, String title, Object[] possibilities, Object initial) {
        return Optional.ofNullable(JOptionPane.showInputDialog(frame,
                formatMessage(message), title,
                JOptionPane.PLAIN_MESSAGE, null,
                possibilities, initial));
    }

    /**
     * Shows nativised file chooser pointing to user home director.
     * @param type  of file chooser {@link ChooserMode}
     * @param title to display
     * @return null if cancelled, path of choice otherwise
     * @see #showFileChooser(ChooserMode, String, FileFilter)
     * @see #showFileChooser(ChooserMode, String, Path, FileFilter)
     */
    public Optional<Path> showFileChooser(ChooserMode type, String title) {
        return showFileChooser(type, title, null);
    }

    /**
     * Shows nativised file chooser pointing to user home director.
     * @param type  of file chooser {@link ChooserMode}
     * @param title to display
     * @return null if cancelled, path of choice otherwise
     */
    public Optional<Path> showFileChooser(ChooserMode type, String title, FileFilter filter) {
        return showFileChooser(type, title, Paths.get(System.getProperty("user.home")), filter);
    }

    /**
     * Shows nativised file chooser.
     * @param type     of file chooser {@link ChooserMode}
     * @param title    to display
     * @param initPath initial location
     * @return null if cancelled, path of choice otherwise
     */
    public Optional<Path> showFileChooser(ChooserMode type, String title, Path initPath,
                                          FileFilter filter) {
        Path choicePath;

        // Due to a problem in Windows and their AWT FileDialog, this will show a JFileChooser on Windows systems.
        if (CommuniqueUtilities.IS_OS_MAC) {
            FileDialog fDialog = new FileDialog(this.frame, "Choose file...", type.map());
            fDialog.setTitle(title);
            fDialog.setDirectory(initPath.toFile().toString());
            if (Objects.nonNull(filter))
                fDialog.setFilenameFilter(new TranslatedFilter(filter));

            fDialog.setVisible(true);

            String fileName = fDialog.getFile();
            if (fileName == null) return Optional.empty();
            else choicePath = Paths.get(fDialog.getDirectory() == null ? "" : fDialog.getDirectory())
                    .resolve(fDialog.getFile());

        } else {
            JFileChooser fChooser = new JFileChooser(initPath.toFile());
            fChooser.setDialogTitle(title);
            fChooser.setVisible(true);
            if (Objects.nonNull(filter))
                fChooser.setFileFilter(filter);

            int returnVal = type.jFileChooser(fChooser, this.frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) choicePath = fChooser.getSelectedFile().toPath();
            else return Optional.empty();
        }

        // Append txt if saving
        if (type == ChooserMode.SAVE && !choicePath.toString().endsWith("txt")) {
            LOGGER.info("Append txt to savePath");
            choicePath = choicePath.resolveSibling(choicePath.getFileName() + ".txt");
        }

        LOGGER.info(String.format("%s file at %s",
                type == ChooserMode.SAVE ? "Saved" : "Opened",
                choicePath.toAbsolutePath().toString()));
        return Optional.of(choicePath);
    }


    /**
     * Formats message with maximum width.
     * @param message to format
     * @return formatted message
     */
    private static JLabel formatMessage(String message) {
        message = message.replaceAll("\n", "<br/>");

        JLabel label = new JLabel(message);
        String newMessage;
        if (label.getPreferredSize().width > MAX_DIALOG_WIDTH)
            newMessage = MessageFormat.format("<html><div style=\"width:{0} px;\">{1}</div></html>",
                    MAX_DIALOG_WIDTH, message);

        else newMessage = MessageFormat.format("<html><div>{0}</div></html>",
                message);

        JLabel resLabel = new JLabel(newMessage);
        resLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return resLabel;
    }

    /** Enumerates file modes for {@link JFileChooser}. */
    public enum ChooserMode {
        SAVE {
            @Override
            public int map() { return FileDialog.SAVE; }

            @Override
            public int jFileChooser(JFileChooser chooser, Window parentWindow) {
                return chooser.showSaveDialog(parentWindow);
            }
        }, OPEN {
            @Override
            public int map() { return FileDialog.LOAD; }

            @Override
            public int jFileChooser(JFileChooser chooser, Window parentWindow) {
                return chooser.showOpenDialog(parentWindow);
            }
        };

        public abstract int map();

        public abstract int jFileChooser(JFileChooser chooser, Window parentWindow);
    }

    /** Translated {@link java.io.FilenameFilter} to {@link javax.swing.filechooser.FileFilter}. */
    private static class TranslatedFilter implements FilenameFilter {
        private final FileFilter filter;

        public TranslatedFilter(FileFilter filter) {this.filter = filter;}

        @Override
        public boolean accept(File dir, String name) {
            return filter.accept(new File(dir, name));
        }
    }
}
