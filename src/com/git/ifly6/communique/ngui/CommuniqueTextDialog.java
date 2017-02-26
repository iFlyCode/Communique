/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
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

import com.git.ifly6.communique.data.Communique7Parser;

/** When constructed, <code>CTextDialog</code> shows a JTextArea in the centre of the frame. It then displays some text
 * in that area and a close button. */
public class CommuniqueTextDialog extends JDialog {
	
	private static final Logger log = Logger.getLogger(CommuniqueTextDialog.class.getName());
	
	private static final long serialVersionUID = Communique7Parser.version;
	
	public CommuniqueTextDialog(JFrame parent, String title, String message) {
		
		super(parent, title);
		
		int width = 400;
		int height = 450;
		this.setSize(width, height);
		this.setMinimumSize(new Dimension(300, 350));
		
		Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
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
