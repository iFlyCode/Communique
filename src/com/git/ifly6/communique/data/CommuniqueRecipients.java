/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.data;

/** Contains utility methods to make it less verbose to create a <code>CommuniqueRecipient</code>. */
public class CommuniqueRecipients {
	
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
	
}
