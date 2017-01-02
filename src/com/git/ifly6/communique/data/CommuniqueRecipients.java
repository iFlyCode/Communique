/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.data;

/** Contains utility methods to make it less verbose to create a <code>CommuniqueRecipient</code>. */
public class CommuniqueRecipients {
	
	// Prevent initialisation
	private CommuniqueRecipients() {
	}
	
	public static CommuniqueRecipient createNation(FilterType filter, String nationName) {
		return new CommuniqueRecipient(filter, RecipientType.NATION, nationName);
	}
	
	public static CommuniqueRecipient createNation(String nationName) {
		return createNation(FilterType.NORMAL, nationName);
	}
	
	public static CommuniqueRecipient createExcludedNation(String nationName) {
		return createNation(FilterType.EXCLUDE, nationName);
	}
	
	public static CommuniqueRecipient createRegion(FilterType filterType, String regionName) {
		return new CommuniqueRecipient(filterType, RecipientType.REGION, regionName);
	}
	
	public static CommuniqueRecipient createTag(FilterType filterType, String tag) {
		return new CommuniqueRecipient(filterType, RecipientType.TAG, tag);
	}
	
	public static CommuniqueRecipient createFlag(String flag) {
		return new CommuniqueRecipient(FilterType.NORMAL, RecipientType.FLAG, flag);
	}
	
}
