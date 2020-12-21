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

import com.git.ifly6.communique.gui3.Communique3MonitorReflections.CommReflectException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.javatuples.Pair;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.git.ifly6.commons.CommuniqueApplication.APP_SUPPORT;

public class Communique3SettingsDialog extends JDialog {

    public static final Logger LOGGER = Logger.getLogger(Communique3SettingsDialog.class.getName());
    private static final Level[] LOGGING_LEVELS = new Level[] {
            Level.SEVERE, Level.WARNING, Level.INFO, Level.FINE, Level.FINER, Level.FINEST
    };

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<Level> loggingBox;
    private JCheckBox clientKeySaveBox;
    private Communique3DialogHandler handler;

    public Communique3SettingsDialog(JFrame parent) {
        super(parent);
        handler = new Communique3DialogHandler(parent, LOGGER);

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

        importSettings();
        this.pack();
        this.setVisible(true);
    }

    private void createUIComponents() {
        loggingBox = new JComboBox<>(LOGGING_LEVELS);
    }

    /** Imports settings from {@link Settings} object. */
    private void importSettings() {
        try {
            Settings s = Settings.load();
            this.loggingBox.setSelectedItem(s.loggingLevel);
            this.clientKeySaveBox.setSelected(s.saveClientKey);
        } catch (IOException e) {
            handleException(e);
        }
    }

    /** Exporting settings to {@link Settings} object. */
    private Settings exportSettings() {
        return new Settings(loggingBox.getItemAt(loggingBox.getSelectedIndex()),
                clientKeySaveBox.isSelected());
    }

    private void onOK() {
        // attempt to save
        try {
            exportSettings().save();
        } catch (IOException e) {
            handleException(e);
        }
        dispose();
    }

    private void onCancel() {
        // do nothing
        dispose();
    }

    /** Handles exceptions by creating error dialog. */
    private void handleException(Throwable e) {
        handler.showErrorDialog("Could not save settings!", e);
    }

    @SuppressWarnings("unused")
    public static class Settings {
        private static final Path SETTINGS_PATH = APP_SUPPORT.resolve("client_settings.properties");

        public Level loggingLevel;
        public boolean saveClientKey;

        public Settings(Level loggingLevel, boolean saveClientKey) {
            this.loggingLevel = loggingLevel;
            this.saveClientKey = saveClientKey;
        }

        public void save() throws IOException {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Pair<Boolean, String> returnedInfo = this.anyNull();

            boolean haveNulls = returnedInfo.getValue0();
            String nullName = returnedInfo.getValue1();

            if (haveNulls) {
                String response = gson.toJson(this);
                Files.write(SETTINGS_PATH, Arrays.asList(response.split("\n")));

            } else throw new Communique3SettingsException(String.format("Value %s is null!",
                    nullName));
        }

        public static Settings load() throws IOException {
            Gson gson = new Gson();
            return gson.fromJson(Files.newBufferedReader(SETTINGS_PATH), Settings.class);
        }

        /**
         * @return tuple of {@code false} with the name of the field; otherwise if no nulls true and null
         */
        private Pair<Boolean, String> anyNull() {
            Field[] fields = this.getClass().getFields();
            try {
                for (Field f : fields) {
                    if (Modifier.isStatic(f.getModifiers())
                            || !Modifier.isPublic(f.getModifiers()))
                        continue;
                    if (f.get(this) == null)
                        return new Pair<>(false, f.getName());
                }
            } catch (IllegalAccessException e) {
                throw new CommReflectException("Cannot check for null fields in settings!");
            }

            return new Pair<>(true, null);
        }
    }

    /** Thrown if there is a validity issue with Communique3's settings. */
    public static class Communique3SettingsException extends RuntimeException {
        public Communique3SettingsException(String message) { super(message); }
    }

}
