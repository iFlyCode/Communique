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
		sentList.add(recipient);
	}
	
}
