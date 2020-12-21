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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import static com.git.ifly6.communique.ngui.CommuniqueConstants.GITHUB_URI;

public class Communique3Updater extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;

    private JLabel currentVersionLabel;
    private JLabel remoteVersionLabel;
    private JLabel linkLabel;

    public Communique3Updater() {
        this.setContentPane(contentPane);
        this.setModal(true);
        this.getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {onOK();}
        });

        updateVersionLabel(currentVersionLabel, CommuniqueApplication.COMMUNIQUE.generateName(true));
        updateVersionLabel(remoteVersionLabel, getCurrentVersion());
        linkLabel.setText(MessageFormat.format("<html><a href=\"{0}\">{0}</a></html>",
                GITHUB_URI.toString()));

        this.pack();
        this.setVisible(true);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void updateVersionLabel(JLabel label, String version) {
        String updated = label.getText().replace("VERSION", version);
        label.setText(updated);
    }

    private String getCurrentVersion() {
        // GITHUB_URI;
        return null;
    }
}
