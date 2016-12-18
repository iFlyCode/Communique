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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/** Defines a number of filter types which can be used in {@link Communique7Parser} to effect the recipients list. All
 * of the exact definitions of what occurs are kept here. @author ifly6 */
public enum FilterType {

	// Note that the NORMAL type, because it does not have a prefix, must be kept last in order for parsing.
	/** Provides equivalent functionality to the <code>+</code> command used in NationStates and the <code>-></code>
	 * command used in past versions of Communique. Basically, it filter the recipients list to be an intersection of
	 * the list and the token provided. */
	INCLUDE {
		@Override public List<CommuniqueRecipient> apply(List<CommuniqueRecipient> recipients,
				CommuniqueRecipient provided) {
			// match by names, not by recipient type
			HashSet<String> set = provided.decompose().stream().map(CommuniqueRecipient::getName)
					.collect(Collectors.toCollection(HashSet::new));
			return recipients.stream().map(CommuniqueRecipient::getName).filter(r -> set.contains(r))
					.map(CommuniqueRecipient::parseRecipient).collect(Collectors.toList());
		}

		@Override public String toString() {
			return "+";
		}
	},

	/** Excludes nations from the recipients list based on the token provided. Provides equivalent functionality as the
	 * NationStates <code>-</code> command in telegram queries. */
	EXCLUDE {
		@Override public List<CommuniqueRecipient> apply(List<CommuniqueRecipient> recipients,
				CommuniqueRecipient provided) {
			// match by names, not by recipient type
			HashSet<String> set = provided.decompose().stream().map(CommuniqueRecipient::getName)
					.collect(Collectors.toCollection(HashSet::new));
			return recipients.stream().map(CommuniqueRecipient::getName).filter(r -> !set.contains(r))
					.map(CommuniqueRecipient::parseRecipient).collect(Collectors.toList());
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
		@Override public List<CommuniqueRecipient> apply(List<CommuniqueRecipient> recipients,
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
	public List<CommuniqueRecipient> apply(List<CommuniqueRecipient> recipients, CommuniqueRecipient provided) {
		return FilterType.NORMAL.apply(recipients, provided);
	}

	/** Helps to translate a {@link CommuniqueRecipient} in a compatible fashion with the NationStates telegram system
	 * syntax. The default <code>toString</code> method defaults to {@link FilterType.NORMAL.toString()}, which returns
	 * an empty string. */
	@Override public String toString() {
		return NORMAL.toString();
	}

}
