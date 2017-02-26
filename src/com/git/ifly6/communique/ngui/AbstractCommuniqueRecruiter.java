/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.ngui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.javatelegram.util.JInfoFetcher;
import com.git.ifly6.javatelegram.util.JTelegramException;
import com.git.ifly6.marconi.MarconiRecruiter;
import com.git.ifly6.nsapi.NSException;
import com.git.ifly6.nsapi.NSNation;

/** Provides the outline for the recruiter classes. Also provides recipient search functionality shared between
 * {@link CommuniqueRecruiter} and {@link MarconiRecruiter}.
 * @author ifly6 */
public abstract class AbstractCommuniqueRecruiter {
	
	public static final JInfoFetcher fetcher = JInfoFetcher.instance();
	
	protected String clientKey;
	protected String secretKey;
	protected String telegramId;
	
	protected List<String> recipients;
	protected LinkedHashSet<String> sentList;
	protected Set<String> proscribedRegions;
	
	public void setWithCConfig(CommuniqueConfig config) {
		
		setClientKey(config.keys.getClientKey());
		setSecretKey(config.keys.getSecretKey());
		setTelegramId(config.keys.getTelegramId());
		
		recipients = new ArrayList<>(Arrays.asList(config.recipients));
		sentList = new LinkedHashSet<>(Arrays.asList(config.sentList));
		
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
	
	/** Returns a recipient based on the new recipients list from the NS API, filtered by whether it is proscribed. Note
	 * that any issues or problems are dealt with my defaulting to the newest nation, ignoring the proscription filter.
	 * It also filters by whether the nation is recruitable.
	 * @return a <code>String</code> with the name of the recipient */
	public String getRecipient() {
		
		try {
			recipients = fetcher.getNew();
			for (String element : recipients) {
				
				boolean match = true;
				NSNation nation = new NSNation(element);
				
				try {
					// @formatter:off
					if (sentList.contains(element)) { match = false; }
					if (isProscribed(nation)) { match = false; }
					if (!nation.isRecruitable()) { match = false; }
					// @formatter:on
				} catch (RuntimeException e) { // If there are any issues, set it to false.
					match = false;
				}
				
				// Return if match is still true
				if (match) { return element; }
				
			}
			
			// If the filtering failed, then simply just return the newest nation.
			return recipients.get(0);
			
		} catch (JTelegramException e) {
			e.printStackTrace();
			return getRecipient();
		}
	}
	
	/** Determines whether a nation is in a region excluded by the JList <code>excludeList</code>. This method acts with
	 * two assumptions: (1) it is not all right to telegram to anyone who resides in a prescribed region and (2) if they
	 * moved out of the region since founding, it is certainly all right to do so.
	 * @param nationName
	 * @return <code>boolean</code> on whether it is proscribed */
	public boolean isProscribed(NSNation nation) {
		
		if (!nation.hasData()) {
			try {
				nation.populateData();
			} catch (NSException e) {
				return false;	// if it does not exist, it is fine
			} catch (IOException e) {
				e.printStackTrace();	// print it
			}
		}
		
		String nRegion = CommuniqueUtilities.ref(nation.getRegion());
		for (String proscribedRegion : proscribedRegions) {
			if (CommuniqueUtilities.ref(proscribedRegion).equals(nRegion)) { return true; }
		}
		
		return false;
	}
	
}