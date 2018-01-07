/* Copyright (c) 2018 ifly6
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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.javatelegram.JavaTelegram;

/** <code>CommuniqueSendDialog</code> shows the user the results of the parsing of the recipients and an estimate of how
 * much time it will take to send. It then returns a Send or Cancel option from the user. */
public class CommuniqueSendDialog extends JDialog {
	
	private static final long serialVersionUID = Communique7Parser.version;
	
	public static final int SEND = 1;
	public static final int CANCEL = 0;
	
	private final JPanel contentPanel = new JPanel();
	private int returnValue = 0;
	
	private JButton sendButton;
	private JButton cancelButton;
	private JLabel lblThisWillTake;
	private JLabel lblConfirmSendTo;
	
	public CommuniqueSendDialog(JFrame parent, List<String> parsedRecipients, boolean isRecruitment) {
		super(parent, true);
		
		{
			Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
			double sWidth = screenDimensions.getWidth();
			double sHeight = screenDimensions.getHeight();
			int windowWidth = 500;
			int windowHeight = 500;
			setBounds((int) (sWidth / 2 - windowWidth / 2), (int) (sHeight / 2 - windowHeight / 2), windowWidth,
					windowHeight);
			setMinimumSize(new Dimension(windowWidth, windowHeight));
		}
		
		setTitle("Confirm Send");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JTextPane textPane = new JTextPane();
			contentPanel.add(new JScrollPane(textPane), BorderLayout.CENTER);
			textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
			textPane.setEditable(false);
			textPane.setText(parsedRecipients.stream().collect(Collectors.joining("\n")));
		}
		{
			lblConfirmSendTo = new JLabel(String.format("Confirm send to %d recipients?",
					parsedRecipients.size()));
			lblConfirmSendTo.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
			contentPanel.add(lblConfirmSendTo, BorderLayout.NORTH);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new EmptyBorder(0, 5, 5, 5));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			GridBagLayout gbl_buttonPane = new GridBagLayout();
			gbl_buttonPane.columnWidths = new int[] { 229, 75, 86, 0 };
			gbl_buttonPane.rowHeights = new int[] { 29, 0 };
			gbl_buttonPane.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
			gbl_buttonPane.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
			buttonPane.setLayout(gbl_buttonPane);
			lblThisWillTake = new JLabel(String.format("Estimated sending time: %s",
					estimateTime(parsedRecipients.size(), isRecruitment)));
			lblThisWillTake.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			GridBagConstraints gbc_lblThisWillTake = new GridBagConstraints();
			gbc_lblThisWillTake.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblThisWillTake.insets = new Insets(0, 0, 0, 5);
			gbc_lblThisWillTake.gridx = 0;
			gbc_lblThisWillTake.gridy = 0;
			buttonPane.add(lblThisWillTake, gbc_lblThisWillTake);
			{
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener((ae) -> {
					// no need to set, default is cancel
					closeDialog();
				});
			}
			{
				sendButton = new JButton("Send");
				sendButton.addActionListener((ae) -> {
					System.out.println("note, sendButton called.");
					returnValue = CommuniqueSendDialog.SEND;
					closeDialog();
				});
				getRootPane().setDefaultButton(sendButton);
			}
			GridBagConstraints gbc_sendButton = new GridBagConstraints();
			gbc_sendButton.anchor = GridBagConstraints.NORTHEAST;
			gbc_sendButton.insets = new Insets(0, 0, 0, 5);
			gbc_sendButton.gridx = 1;
			gbc_sendButton.gridy = 0;
			buttonPane.add(sendButton, gbc_sendButton);
			GridBagConstraints gbc_cancelButton = new GridBagConstraints();
			gbc_cancelButton.anchor = GridBagConstraints.NORTHEAST;
			gbc_cancelButton.gridx = 2;
			gbc_cancelButton.gridy = 0;
			buttonPane.add(cancelButton, gbc_cancelButton);
		}
		
		contentPanel.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					sendButton.doClick();
			}
			
			@Override public void keyReleased(KeyEvent e) {
			}
			
			@Override public void keyPressed(KeyEvent e) {
			}
		});
		
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
	
	private void closeDialog() {
		this.setVisible(false);
	}
	
	public int getValue() {
		return returnValue;
	}
	
	private String estimateTime(int count, boolean isRecruitment) {
		int seconds = Math.round(count *
				(isRecruitment
						? JavaTelegram.RECRUIT_TIME / 1000
						: JavaTelegram.CAMPAIGN_TIME / 1000));
		return CommuniqueUtilities.time(seconds);
	}
	
}
