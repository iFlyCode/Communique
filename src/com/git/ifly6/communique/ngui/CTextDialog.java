/* Copyright (c) 2016 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.git.ifly6.communique.ngui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.git.ifly6.communique.data.CommuniqueParser;

/** When constructed, <code>CTextDialog</code> shows a JTextArea in the centre of the frame. It then displays some text
 * in that area and a close button. */
public class CTextDialog extends JDialog {

	private static final Logger log = Logger.getLogger(CTextDialog.class.getName());

	private static final long serialVersionUID = CommuniqueParser.version;

	public CTextDialog(JFrame parent, String title, String message) {

		super(parent, title);

		Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();

		int width = 300;
		int height = 350;
		this.setSize(width, height);
		this.setLocation(Math.round(sSize.width / 2 - width / 2), Math.round(sSize.height / 2 - height / 2));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.setContentPane(panel);

		// TextArea
		JTextArea textArea = new JTextArea(message);
		textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);

		panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

		// Button Panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 4));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		for (int x = 0; x < 3; x++) {
			buttonPanel.add(new JLabel());
		}

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
		// @formatter:off
		this.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) { }
			@Override public void keyReleased(KeyEvent e) { }
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					closeButton.doClick();
				}
			}
		});
		// @formatter:on

		this.setVisible(true);
		log.finer("Showing CTextDialog with: " + message);

	}
}
