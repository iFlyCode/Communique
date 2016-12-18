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
package com.git.ifly6.communique.data;

import java.util.InputMismatchException;
import java.util.List;

/** An object in which to store information about a recipient. It is based on three characteristics, a
 * <code>FilterType</code>, a <code>RecipientType</code>, and the name. The filter type can be used to exclude, include,
 * or simply add. The recipient type can be used to specify multiple recipients, like in a region or in the set of World
 * Assembly delegates. The name specifies exactly what is being queried for.
 * @author ifly6 */
public class CommuniqueRecipient {

	private RecipientType recipientType;
	private String name;
	private FilterType filterType;
	
	/** Creates a <code>CommuniqueRecipient</code> with certain characteristics. */
	public CommuniqueRecipient(FilterType filterType, RecipientType recipientType, String name) {
		this.recipientType = recipientType;
		this.name = name;
		this.filterType = filterType;
	}
	
	/** Creates a {@link CommuniqueRecipient} from a reference name with a default {@link FilterType.NORMAL}.
	 * @param recipientType of some NationStates object to be added
	 * @param name of the object, assuming a normal filter type */
	public CommuniqueRecipient(RecipientType recipientType, String name) {
		this(FilterType.NORMAL, recipientType, name);
	}

	/** Creates a {@link CommuniqueRecipient} from a nation reference name. This basic constructor assumes that the
	 * reference name refers to a nation and that the filter type is normal.
	 * @see RecipientType#NATION
	 * @see FilterType#NORMAL
	 * @param referenceName of a nation, assuming a normal filter type */
	public CommuniqueRecipient(String referenceName) {
		this(RecipientType.NATION, referenceName);
	}

	/** Returns the name, which, for all elements, will be the reference name format.
	 * @return the specific thing which is being requested */
	public String getName() {
		return name.trim().toLowerCase().replace(" ", "_");
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
		return filterType.toString() + recipientType.toString() + ":" + this.getName();
	}

	/** Decomposes a tag to its constituent nations. All decompositions are done in
	 * {@link com.git.ifly6.communique.data.RecipientType RecipientType} class.
	 * @return a list of <code>CommuniqueRecipient</code>s */
	public List<CommuniqueRecipient> decompose() {
		return recipientType.decompose(this);
	}
	
	/** Parses a <code>CommuniqueRecipient</code> of the same form defined in the
	 * {@link com.git.ifly6.communique.data.CommuniqueRecipient#toString toString()} method. Allows for fast and simple
	 * access between <code>String</code> representations of a recipient and the computer's conception of the object.
	 * @param <code>s</code>, a <code>String</code> to be parsed
	 * @throws <code>InputMismatchException</code> if the string does not match a predefined <code>RecipientType</code>.
	 * @return a <code>CommuniqueRecipient</code> representing that string */
	public static CommuniqueRecipient parseRecipient(String s) {
		
		s = s.trim();

		boolean match = false;
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
				match = true;
				break;
			}
		}
		
		if (!match) { throw new InputMismatchException("Cannot parse recipient on: " + s); }
		return new CommuniqueRecipient(fType, rType, s.substring(s.indexOf(":") + 1, s.length()));
	}
}
