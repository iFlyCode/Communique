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

import com.git.ifly6.commons.CommuniqueApplication;
import com.git.ifly6.communique.ngui.CommuniqueConstants;
import com.git.ifly6.nsapi.NSConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.git.ifly6.communique.ngui.CommuniqueConstants.GITHUB_RELEASES_URI;

public class Communique3Updater extends JDialog {

    public static final Logger LOGGER = Logger.getLogger(Communique3Updater.class.getName());

    private JPanel contentPane;
    private JButton buttonOK;

    private JLabel currentBuildLabel;
    private JLabel remoteBuildLabel;
    private JLabel linkLabel;

    private Communique3Updater() {
        this.setContentPane(contentPane);
        this.setModal(true);
        this.getRootPane().setDefaultButton(buttonOK);
        this.setTitle(CommuniqueConstants.UPDATER);

        buttonOK.addActionListener(e -> onOK());

        Dimension fixed = new Dimension(450, 150);
        Communique3Utils.setupDimensions(this,
                fixed, fixed, true);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void updateBuildLabel(JLabel label, String build) {
        EventQueue.invokeLater(() -> {
            String updated = label.getText().replace("BUILD", build);
            label.setText(updated);
        });
    }

    public static Communique3Updater create() {
        Communique3Updater updater = new Communique3Updater();
        updater.updateBuildLabel(updater.currentBuildLabel,
                CommuniqueApplication.COMMUNIQUE.generateName(true));
        Executors.newSingleThreadExecutor() // load asynchronously
                .submit(updater::formatCurrentVersion);

        updater.pack();
        updater.setVisible(true);
        return updater;
    }

    private void formatCurrentVersion() {
        EventQueue.invokeLater(() -> {
            linkLabel.setText(String.format("<html><a href=\"\">%s</a></html>", GITHUB_RELEASES_URI.toString()));
            linkLabel.addMouseListener(new CommuniqueMouseAdapter(e -> {
                try {
                    Desktop.getDesktop().browse(GITHUB_RELEASES_URI);
                } catch (IOException exception) { LOGGER.info("Couldn't open GitHub!"); }
            }));
        });

        String version = "UNKNOWN";
        try {
            version = getCurrentVersion();
        } catch (IOException e) { LOGGER.info("Could not get current build from GitHub!"); }
        updateBuildLabel(remoteBuildLabel, version);
    }

    private static String getCurrentVersion() throws IOException {
        Document doc = Jsoup.parse(new NSConnection(GITHUB_RELEASES_URI.toString()).getResponse());
        Element e = doc.select("span.css-truncate-target").first();
        if (e != null) {
            return e.text();

        } else throw new IOException("Could not find release tag in GitHub releases!");
    }
}
