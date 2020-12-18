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

import com.git.ifly6.communique.data.Communique7Parser;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * <code>CommuniqueTextDialog</code> shows a JTextArea in the centre of the frame. It then displays some text in that
 * area and a close button.
 */
public class CommuniqueTextDialog extends JDialog {

    private static final long serialVersionUID = Communique7Parser.BUILD;
    private static final Logger log = Logger.getLogger(CommuniqueTextDialog.class.getName());

    public static void createDialog(JFrame parent, String title, String message) {
        new CommuniqueTextDialog(parent, title, message, new Font(Font.SANS_SERIF, Font.PLAIN, 11), true);
    }

    public static void createMonospacedDialog(JFrame parent, String title, String message, boolean lineWrap) {
        new CommuniqueTextDialog(parent, title, message, new Font(Font.MONOSPACED, Font.PLAIN, 11), lineWrap);
    }

    private CommuniqueTextDialog(JFrame parent, String title, String message, Font font, boolean lineWrap) {

        super(parent, true); // true for modal
        setTitle(title);

        int width = lineWrap
                ? 400
                : 10 + Arrays.stream(message.split("\n")).mapToInt(String::length).max().orElse(50) * 8;
        int height = 450;
        this.setSize(width, height);
        this.setMinimumSize(new Dimension(300, 350));

        Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(
                Math.round((float) sSize.width / 2 - (float) width / 2),
                Math.round((float) sSize.height / 2 - (float) height / 2));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.setContentPane(panel);

        // textArea
        JTextArea textArea = new JTextArea(message);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textArea.setFont(font);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(lineWrap);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));
        panel.add(buttonPanel, BorderLayout.SOUTH);

        for (int x = 0; x < 3; x++)
            buttonPanel.add(new JLabel());

        // Button
        JButton closeButton = new JButton("Ok");
        this.getRootPane().setDefaultButton(closeButton);
        closeButton.addActionListener(e -> {
            log.finer("Closed CTextDialog");
            setVisible(false);
            dispose();
        });
        buttonPanel.add(closeButton);

        // Make pressing the enter key the same as hitting the button.
        this.addKeyListener(new KeyListener() {
            // @formatter:off
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) closeButton.doClick();
            }
            // @formatter:on
        });

        this.setVisible(true);
        log.finer("Showing CTextDialog with: " + message);

    }
}
