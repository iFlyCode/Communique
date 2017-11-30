/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.git.ifly6.javatelegram.util.JInfoFetcher;
import com.git.ifly6.javatelegram.util.JTelegramException;

/** Defines a number of recipient types and provides methods to decompose those types into lists of
 * <code>CommuniqueRecipient</code>.
 * @author ifly6 */
public enum RecipientType {
	
	/** Declares the recipient is a nation and requires no further processing in decomposition. */
	NATION {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			// return singleton list
			return new ArrayList<>(Arrays.asList(cr));
		}
	},
	
	/** Declares the recipient is a region, allowing for decomposition into a list of {@link CommuniqueRecipient}
	 * nations in the region. */
	REGION {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
			return newRecipients(JInfoFetcher.instance().getRegion(cr.getName()), cr);
		}
	},
	
	/** Declares the recipient is one of various tags, which can be used to get the members of the World Assembly,
	 * delegates thereof, or new nations. */
	TAG {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
			String tag = cr.getName();    // TODO create tag enums for this
			if (tag.equals("wa")) return newRecipients(JInfoFetcher.instance().getWAMembers(), cr);
			if (tag.equals("delegates")) return newRecipients(JInfoFetcher.instance().getDelegates(), cr);
			if (tag.equals("new")) return newRecipients(JInfoFetcher.instance().getNew(), cr);
			if (tag.equals("all")) return newRecipients(JInfoFetcher.instance().getAll(), cr);
			throw new JTelegramException("Invalid flag: \"" + cr.toString() + "\"");
		}
	},
	
	/** Declares that the recipient is an internal Commmunique flag. It does not return any real recipients when calling
	 * its decompose method. */
	FLAG {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			return new ArrayList<>();
		}
	};
	
	/** Allows for the recipient type to be compatible with the NationStates telegram system by providing the same tag
	 * nomenclature. */
	@Override public String toString() {
		return super.toString().toLowerCase();
	}
	
	/** Decomposes a tag into a list of <code>CommuniqueRecipient</code> which can then be more easily used.
	 * @param cr to be decomposed
	 * @return a list of <code>CommuniqueRecipient</code> */
	public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
		// this is the default kind
		return NATION.decompose(cr);
	}
	
	/** Translates a list of nation reference names into a list of valid <code>CommuniqueRecipient</code>s.
	 * @param list of nation reference names
	 * @param cr from which to extract type data
	 * @return list of CommuniqueRecipients */
	private static List<CommuniqueRecipient> newRecipients(List<String> list, CommuniqueRecipient cr) {
		return list.stream()
				.map(s -> CommuniqueRecipients.createNation(cr.getFilterType(), s))
				.collect(Collectors.toList());
	}
}
