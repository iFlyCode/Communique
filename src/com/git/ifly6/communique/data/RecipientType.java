/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.data;

import com.git.ifly6.communique.io.HappeningsParser;
import com.git.ifly6.javatelegram.JTelegramException;
import com.git.ifly6.javatelegram.util.JInfoFetcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Defines a number of recipient types and provides methods to decompose those types into lists of
 * <code>CommuniqueRecipient</code>.
 * @author ifly6 */
public enum RecipientType {

	/** Declares the recipient is a nation and requires no further processing in decomposition. */
	NATION {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			// return singleton list
			return Collections.singletonList(cr);
		}

		@Override public String toString() {
			return this.name().toLowerCase();
		}
	},

	/** Declares the recipient is a region, allowing for decomposition into a list of {@link CommuniqueRecipient}
	 * nations in the region. */
	REGION {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
			return newRecipients(JInfoFetcher.instance().getRegion(cr.getName()), cr.getFilterType());
		}

		@Override public String toString() {
			return this.name().toLowerCase();
		}
	},

	/** Declares the recipient is one of various tags, which can be used to get the members of the World Assembly,
	 * delegates thereof, or new nations. */
	TAG {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
			String tag = cr.getName();
			if (tag.equals("wa")) return newRecipients(JInfoFetcher.instance().getWAMembers(), cr.getFilterType());
			if (tag.equals("delegates")) return newRecipients(JInfoFetcher.instance().getDelegates(), cr.getFilterType());
			if (tag.equals("new")) return newRecipients(JInfoFetcher.instance().getNew(), cr.getFilterType());
			if (tag.equals("all")) return newRecipients(JInfoFetcher.instance().getAll(), cr.getFilterType());
			throw new JTelegramException("Invalid flag: \"" + cr.toString() + "\"");
		}

		@Override public String toString() {
			return this.name().toLowerCase();
		}
	},

	/** Declares that the recipient is an internal Commmunique flag. */
	FLAG {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			String tag = cr.getName();
			if (tag.equals("recruit")) return Collections.emptyList(); // recruit is handled by Communique logic, not here
			if (tag.equals("active")) return HappeningsParser.getActiveNations();  // active
			return Collections.emptyList();
		}

		@Override public String toString() {
			return this.name().toLowerCase();
		}
	};

	/** Allows for the recipient type to be compatible with the NationStates telegram system by providing the same tag
	 * nomenclature. */
	@Override public abstract String toString();

	/** Decomposes a tag into a list of <code>CommuniqueRecipient</code> which can then be more easily used.
	 * @param cr to be decomposed
	 * @return a list of <code>CommuniqueRecipient</code> */
	public abstract List<CommuniqueRecipient> decompose(CommuniqueRecipient cr);

	/** Translates a list of nation reference names into a list of valid <code>CommuniqueRecipient</code>s.
	 * @param list of nation reference names
	 * @param filterType from which to extract type data
	 * @return list of CommuniqueRecipients */
	private static List<CommuniqueRecipient> newRecipients(List<String> list, FilterType filterType) {
		// we use this a lot, probably better to use a loop for speed
		List<CommuniqueRecipient> result = new ArrayList<>();
		for (String s : list)
			result.add(CommuniqueRecipients.createNation(filterType, s));
		return result;
	}
}
