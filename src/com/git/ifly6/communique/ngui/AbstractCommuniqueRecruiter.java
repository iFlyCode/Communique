/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.ngui;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.data.FilterType;
import com.git.ifly6.communique.data.RecipientType;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.javatelegram.JTelegramException;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.util.JInfoFetcher;
import com.git.ifly6.marconi.MarconiRecruiter;
import com.git.ifly6.nsapi.NSException;
import com.git.ifly6.nsapi.NSNation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Provides the outline for the recruiter classes. Also provides recipient search functionality shared between
 * {@link CommuniqueRecruiter} and {@link MarconiRecruiter}.
 * @author ifly6 */
public abstract class AbstractCommuniqueRecruiter implements JTelegramLogger {
	
	private static final JInfoFetcher fetcher = JInfoFetcher.instance();
	private static final Logger LOGGER = Logger.getLogger(AbstractCommuniqueRecruiter.class.getName());
	
	protected LinkedHashSet<CommuniqueRecipient> sentList;
	protected Set<CommuniqueRecipient> proscribedRegions;
	
	public void setWithCConfig(CommuniqueConfig config) {
		sentList = config.getcRecipients().stream()
				.filter(r -> r.getRecipientType() == RecipientType.NATION)
				.filter(r -> r.getFilterType() == FilterType.EXCLUDE)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	public abstract void send();
	
	@Override public void sentTo(String recipient, int recipientNum, int length) {
		sentList.add(CommuniqueRecipients.createExcludedNation(recipient));
	}
	
	/** Returns a recipient based on the new recipients list from the NS API, filtered by whether it is proscribed. Note
	 * that any issues or problems are dealt with my defaulting to the newest nation, ignoring the proscription filter.
	 * It also filters by whether the nation is recruitable.
	 * @return a <code>String</code> with the name of the recipient */
	public CommuniqueRecipient getRecipient() {
		
		try {
			List<String> possibleRecipients = CommuniqueUtilities.ref(fetcher.getNew());
			for (String element : possibleRecipients) {
				
				// if in sent list, next
				// if not recruitable, next
				// if proscribed, next
				// otherwise, return
				
				if (sentList.stream()   // must map to names
						.map(CommuniqueRecipient::getName)
						.anyMatch(s -> s.equalsIgnoreCase(element)))
					continue;
				
				try {
					NSNation prNation = new NSNation(element).populateData();
					if (!prNation.isRecruitable()) continue;
					if (isProscribed(prNation)) continue;
					
				} catch (NSException e) {
					// if it doesn't exist, ignore it
					continue;
				}
				
				// 2017-03-18 proscription and recruit checks are now performed by JavaTelegram#predicates
				return CommuniqueRecipients.createNation(element);
			}
			
			// If the filtering failed, then simply just return the newest nation.
			return CommuniqueRecipients.createNation(possibleRecipients.get(0));
			
		} catch (JTelegramException e) {
			LOGGER.warning("Cannot fetch new nations [" + CommuniqueUtilities.getCurrentDateAndTime() +
					"]. Retrying.");
			return getRecipient();  // retry
			
		} catch (RuntimeException e) {
			LOGGER.warning("Cannot load data for nation");
			return getRecipient();  // retry
		}
	}
	
	/** Determines whether a nation is in a region excluded by the JList <code>excludeList</code>. This method acts with
	 * two assumptions: (1) it is not all right to telegram to anyone who resides in a prescribed region and (2) if they
	 * moved out of the region since founding, it is certainly all right to do so.
	 * @param nation to check
	 * @return <code>boolean</code> on whether it is proscribed */
	private boolean isProscribed(NSNation nation) {
		
		if (!nation.hasData()) nation.populateData();
		
		// API gives region names, can only do this by converting to ref names and then comparing
		String nRegion = CommuniqueUtilities.ref(nation.getRegion());
		return proscribedRegions.stream()
				.map(CommuniqueRecipient::getName)
				.map(CommuniqueUtilities::ref)
				.anyMatch(regionName -> regionName.equals(nRegion));
		
	}
	
}