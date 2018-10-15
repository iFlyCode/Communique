/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.data;

/**
 * Contains utility methods to make it less verbose to create a {@link CommuniqueRecipient}. Also provides functionality
 * to help with the fact that {@link CommuniqueRecipient}s are immutable.
 */
public class CommuniqueRecipients {

	// Prevent initialisation
	private CommuniqueRecipients() {
	}

	/**
	 * Creates recipient of type {@link RecipientType#NATION} from given string and with the given {@link FilterType}
	 * @param filter     to apply to construction
	 * @param nationName to target
	 * @return new recipient
	 */
	public static CommuniqueRecipient createNation(FilterType filter, String nationName) {
		return new CommuniqueRecipient(filter, RecipientType.NATION, nationName);
	}

	/**
	 * Creates a nation recipient with the given name with a normal filter type
	 * @param nationName to target
	 * @return new recipient
	 */
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

	/**
	 * Creates new recipient with copied {@link RecipientType} and name, but with {@link FilterType#EXCLUDE}
	 * @param recipient holding name and type
	 * @return the recipient, excluded
	 */
	public static CommuniqueRecipient exclude(CommuniqueRecipient recipient) {
		return new CommuniqueRecipient(FilterType.EXCLUDE, recipient.getRecipientType(), recipient.getName());
	}

	public static CommuniqueRecipient setFilter(CommuniqueRecipient recipient, FilterType filterType) {
		return new CommuniqueRecipient(filterType, recipient.getRecipientType(), recipient.getName());
	}

}
