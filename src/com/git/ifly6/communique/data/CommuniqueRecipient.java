/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.data;

import java.util.List;

import com.git.ifly6.communique.CommuniqueUtilities;

/** An object in which to store information about a recipient. It is based on three characteristics, a
 * <code>FilterType</code>, a <code>RecipientType</code>, and the name. The filter type can be used to exclude, include,
 * or simply add. The recipient type can be used to specify multiple recipients, like in a region or in the set of World
 * Assembly delegates. The name specifies exactly what is being queried for.
 * @author ifly6 */
public class CommuniqueRecipient {
	
	public static final CommuniqueRecipient DELEGATES =
			new CommuniqueRecipient(FilterType.NORMAL, RecipientType.TAG, "delegates");
	public static final CommuniqueRecipient WA_MEMBERS =
			new CommuniqueRecipient(FilterType.NORMAL, RecipientType.TAG, "wa");
	
	private RecipientType recipientType;
	private String name;
	private FilterType filterType;
	
	/** Creates a <code>CommuniqueRecipient</code> with certain characteristics. */
	public CommuniqueRecipient(FilterType filterType, RecipientType recipientType, String name) {
		this.filterType = filterType;
		this.recipientType = recipientType;
		this.name = CommuniqueUtilities.ref(name);	// convert to reference name
	}
	
	/** Returns the name, which, for all elements, will be the reference name format.
	 * @return the specific thing which is being requested */
	public String getName() {
		return name;
	}
	
	/** Returns the type of the filter or token, defined in {@link com.git.ifly6.communique.data.FilterType FilterType}.
	 * @return the type of filter or token */
	public FilterType getFilterType() {
		return filterType;
	}
	
	/** Returns the type of the recipient, defined in {@link com.git.ifly6.communique.data.RecipientType RecipientType}.
	 * @return the type of recipient */
	public RecipientType getRecipientType() {
		return recipientType;
	}
	
	/** Returns a string representation of the recipient, in the same form which is used by the NationStates telegram
	 * system to specify large numbers of nations. For example, <code>tag:wa</code> or
	 * <code>nation:imperium_anglorum</code>. */
	@Override public String toString() {
		return getFilterType().toString() + getRecipientType().toString() + ":" + this.getName();
	}
	
	/** Decomposes a tag to its constituent nations. All decompositions are done in
	 * {@link com.git.ifly6.communique.data.RecipientType RecipientType} class.
	 * @return a list of <code>CommuniqueRecipient</code>s */
	public List<CommuniqueRecipient> decompose() {
		return getRecipientType().decompose(this);
	}
	
	/** Parses a <code>CommuniqueRecipient</code> of the same form defined in the
	 * {@link com.git.ifly6.communique.data.CommuniqueRecipient#toString toString()} method. Allows for fast and simple
	 * access between <code>String</code> representations of a recipient and the computer's conception of the object.
	 * <p>
	 * If a reference name is provided without an accompanying recipient-type declaration, in the form
	 * <code>imperium_anglorum</code>, it is assumed that this is a <code>FilterType.NORMAL</code> nation with that
	 * name.
	 * </p>
	 * @param <code>s</code>, a <code>String</code> to be parsed
	 * @return a <code>CommuniqueRecipient</code> representing that string */
	public static CommuniqueRecipient parseRecipient(String s) {
		
		s = s.trim();
		
		FilterType fType = FilterType.NORMAL;
		for (FilterType type : FilterType.values()) {
			if (s.startsWith(type.toString())) {
				fType = type;
				s = s.substring(type.toString().length());
				break;
			}
		}
		
		RecipientType rType = RecipientType.NATION;
		for (RecipientType type : RecipientType.values()) {
			if (s.startsWith(type.toString())) {
				rType = type;
				s = s.substring(type.toString().length());
				break;
			}
		}
		
		return new CommuniqueRecipient(fType, rType, s.substring(s.indexOf(":") + 1));
	}
	
	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (filterType == null ? 0 : filterType.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (recipientType == null ? 0 : recipientType.hashCode());
		return result;
	}
	
	@Override public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		CommuniqueRecipient other = (CommuniqueRecipient) obj;
		if (filterType != other.filterType) { return false; }
		if (name == null) {
			if (other.name != null) { return false; }
		} else if (!name.equals(other.name)) { return false; }
		if (recipientType != other.recipientType) { return false; }
		return true;
	}
}
