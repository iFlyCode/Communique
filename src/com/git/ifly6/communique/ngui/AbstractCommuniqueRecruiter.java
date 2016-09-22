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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.git.ifly6.communique.io.CConfig;
import com.git.ifly6.javatelegram.util.JInfoFetcher;
import com.git.ifly6.javatelegram.util.JTelegramException;
import com.git.ifly6.nsapi.NSNation;

/**
 * @author Kevin
 *
 */
public abstract class AbstractCommuniqueRecruiter {
	
	public static final JInfoFetcher fetcher = new JInfoFetcher();
	
	protected String clientKey;
	protected String secretKey;
	protected String telegramId;
	
	protected List<String> recipients;
	protected List<String> sentList;
	protected Set<String> proscribedRegions;
	
	public void setWithCConfig(CConfig config) {
		
		setClientKey(config.keys.getClientKey());
		setSecretKey(config.keys.getSecretKey());
		setTelegramId(config.keys.getTelegramId());
		
		recipients = new ArrayList<>(Arrays.asList(config.recipients));
		sentList = new ArrayList<>(Arrays.asList(config.sentList));
		
	}
	
	public void setClientKey(String key) {
		this.clientKey = key;
	}
	
	public void setSecretKey(String key) {
		this.secretKey = key;
	}
	
	public void setTelegramId(String id) {
		this.telegramId = id;
	}
	
	public abstract void send();
	
	/**
	 * Returns a recipient based on the new recipients list from the NS API, filtered by whether it is proscribed. Note
	 * that any issues or problems are dealt with my defaulting to the newest nation, ignoring the proscription filter.
	 *
	 * @return a <code>String</code> with the recipient
	 */
	public String getRecipient() {
		
		try {
			
			recipients = fetcher.getNew();

			for (String element : recipients) {
				if (!sentList.contains(element) && !isProscribed(element, proscribedRegions)) { return element; }
			}

			// If the filtering failed, then simply just return the newest nation.
			return recipients.get(0);

		} catch (JTelegramException e) {
			e.printStackTrace();
			System.err.println("Error. Retrying recipients list.");
			
			// If recipients cannot be got, try again.
			return getRecipient();
		}
	}
	
	/**
	 * Determines whether a nation is in a region excluded by the JList <code>excludeList</code>. This method acts with
	 * two assumptions: (1) it is not all right to telegram to anyone who resides in a prescribed region and (2) if they
	 * moved out of the region since founding, it is certainly all right to do so.
	 *
	 * @param nationName
	 * @return <code>boolean</code> on whether it is proscribed
	 */
	public boolean isProscribed(String element, Set<String> proscribedRegions) {
		
		NSNation nation = new NSNation(element);
		try {
			nation.populateData();
		} catch (IOException e) {
			// Failure to fetch information means false
			return false;
		}
		
		String nRegion = nation.getRegion().replaceAll(" ", "_");
		for (String proscribedRegion : proscribedRegions) {
			if (proscribedRegion.replaceAll(" ", "_").equalsIgnoreCase(nRegion)) { return true; }
		}
		
		return false;
	}
	
}