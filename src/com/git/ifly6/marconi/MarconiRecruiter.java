/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.marconi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;

/** @author Kevin */
public class MarconiRecruiter extends AbstractCommuniqueRecruiter implements JTelegramLogger {
	
	private Marconi marconi;
	private Thread thread;
	
	/** @param marconi */
	public MarconiRecruiter(Marconi marconi) {
		this.marconi = marconi;
	}
	
	/** @see com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter#send() */
	@Override public void send() {
		
		Runnable runner = () -> {
			
			boolean isSending = true;
			while (isSending) {
				
				proscribedRegions = populateProscribedRegions();
				String recipient = getRecipient();
				
				// Otherwise, start sending.
				JavaTelegram client = new JavaTelegram(this);
				client.setKeys(marconi.exportState().keys);
				client.setRecipient(recipient);
				client.connect();
				
				// Report information
				marconi.log("Sent recruitment telegram " + marconi.exportState().sentList.length + " to " + recipient);
				
				Calendar now = Calendar.getInstance();
				now.add(Calendar.SECOND, 180);
				String nextTelegramTime = new SimpleDateFormat("HH:mm:ss").format(now.getTime());
				marconi.log("Next recruitment telegram in 180 seconds at " + nextTelegramTime);
				
				try {
					Thread.sleep(180 * 1000);
				} catch (InterruptedException e) {
					// nothing, since it cannot be interrupted.
				}
				
			}
		};
		
		thread = new Thread(runner);
		thread.start();
		
	}
	
	private Set<String> populateProscribedRegions() {
		
		if (proscribedRegions == null) {
			
			String[] recipients = marconi.exportState().recipients;
			proscribedRegions = new HashSet<>();
			for (String element : recipients) {
				if (element.startsWith("flags:recruit -- region:")) {
					proscribedRegions.add(element);
				}
			}
			
		}
		
		return proscribedRegions;
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String) */
	@Override public void log(String input) {
		marconi.log(input);
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
	@Override public void sentTo(String recipient, int x, int length) {
		marconi.sentTo(recipient, x, length);
	}
	
}
