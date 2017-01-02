/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.data;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Defines a number of filter types which can be used in {@link Communique7Parser} to effect the recipients list. All
 * of the exact definitions of what occurs are kept here. @author ifly6 */
public enum FilterType {
	
	// Note that the NORMAL type, because it does not have a prefix, must be kept last in order for parsing.
	/** Provides equivalent functionality to the <code>+</code> command used in NationStates and the <code>-></code>
	 * command used in past versions of Communique. Basically, it filter the recipients list to be an intersection of
	 * the list and the token provided. */
	INCLUDE {
		@Override public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
				CommuniqueRecipient provided) {
			// match by names, not by recipient type
			HashSet<String> set = provided.decompose().stream()
					.map(CommuniqueRecipient::getName)
					.collect(Collectors.toCollection(HashSet::new));
			return recipients.stream()
					.filter(r -> set.contains(r.getName()))
					.collect(Collectors.toCollection(LinkedHashSet::new));
		}
		
		@Override public String toString() {
			return "+";
		}
	},
	
	/** Excludes nations from the recipients list based on the token provided. Provides equivalent functionality as the
	 * NationStates <code>-</code> command in telegram queries. */
	EXCLUDE {
		@Override public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
				CommuniqueRecipient provided) {
			HashSet<String> set = provided.decompose().stream()
					.map(CommuniqueRecipient::getName)
					.collect(Collectors.toCollection(HashSet::new));
			return recipients.stream()
					.filter(r -> !set.contains(r.getName()))
					.collect(Collectors.toCollection(LinkedHashSet::new));
		}
		
		@Override public String toString() {
			return "-";
		}
	},
	
	/** Adds the provided <code>CommuniqueRecipient</code> to the end of the recipients list. This is the default action
	 * for <code>CommuniqueRecipient</code> tokens, unless they are declared otherwise.
	 * <p>
	 * Please note that this portion of the <code>enum</code> should be kept at the bottom of the class, or otherwise,
	 * {@link CommuniqueRecipient#parseRecipient} will break.
	 * </p>
	*/
	NORMAL {
		@Override public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients,
				CommuniqueRecipient provided) {
			recipients.addAll(provided.decompose());
			return recipients;
		}
		
		@Override public String toString() {
			return "";
		}
	};
	
	/** Applies the provided <code>CommuniqueRecipient</code> to the provided recipients list. Without a provided
	 * <code>enum</code> state, this defaults to {@link FilterType#NORMAL}.
	 * @param recipients upon which the token is to be applied
	 * @param provided token
	 * @return recipients after the token is applied */
	public Set<CommuniqueRecipient> apply(Set<CommuniqueRecipient> recipients, CommuniqueRecipient provided) {
		return FilterType.NORMAL.apply(recipients, provided);
	}
	
	/** Helps to translate a {@link CommuniqueRecipient} in a compatible fashion with the NationStates telegram system
	 * syntax. The default <code>toString</code> method defaults to {@link FilterType.NORMAL.toString()}, which returns
	 * an empty string. */
	@Override public String toString() {
		return NORMAL.toString();
	}
	
}
