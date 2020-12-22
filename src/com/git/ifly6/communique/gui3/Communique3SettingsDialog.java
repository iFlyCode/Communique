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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Communique3SettingsDialog extends JDialog {

    public static final Logger LOGGER = Logger.getLogger(Communique3SettingsDialog.class.getName());
    private static final Level[] LOGGING_LEVELS = new Level[] {
            Level.SEVERE, Level.WARNING, Level.INFO, Level.FINE, Level.FINER, Level.FINEST
    };

    private Communique3Settings finalSettings;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<Level> loggingBox;
    private Communique3DialogHandler handler;

    public Communique3SettingsDialog(JFrame parent, Communique3Settings settings) {
        super(parent);
        handler = new Communique3DialogHandler(parent, LOGGER);
        importSettings(settings);

        setContentPane(contentPane);
        setModal(false);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.pack();

        Communique3Utils.setupDimensions(this,
                new Dimension(200, 150),
                new Dimension(300,300),
                true);
        this.setVisible(true);
    }

    private void createUIComponents() {
        loggingBox = new JComboBox<>(LOGGING_LEVELS);
    }

    /** Imports settings from {@link Communique3Settings} object. */
    private void importSettings(Communique3Settings s) {
        this.loggingBox.setSelectedItem(s.loggingLevel);
    }

    /** Exporting settings to {@link Communique3Settings} object. */
    private Communique3Settings exportSettings() {
        return new Communique3Settings(loggingBox.getItemAt(loggingBox.getSelectedIndex()));
    }

    private void onOK() {
        // attempt to save
        try {
            exportSettings().save();
        } catch (IOException e) { handleException(e); }
        finalSettings = exportSettings();
        dispose();
    }

    private void onCancel() {
        finalSettings = null;
        dispose();
    }

    /**
     * Returns final settings for the settings dialog.
     * @return {@code Optional<Settings>} present if applied; absent if cancelled
     */
    public Optional<Communique3Settings> getFinalSettings() {
        return Optional.ofNullable(this.finalSettings);
    }

    /** Handles exceptions by creating error dialog. */
    private void handleException(Throwable e) {
        handler.showErrorDialog("Could not save settings!", e);
    }
}
