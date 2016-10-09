/* Copyright (c) 2015 ifly6
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

package com.git.ifly6.communique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.git.ifly6.javatelegram.util.JInfoFetcher;

/** This class is the central hub of the Communiqué system. It parses the <code>String</code> given to it with all the
 * recipients (and tags which stand in for multiple recipients) into a <code>String[]</code> which has every single
 * recipient, expanded, on each index. It also handles the removal of certain recipients from the list and filtering of
 * recipients as well.
 *
 * <h2>Tags</h2> There are a number of tags which stand for large defined lists of recipients.
 * <table>
 * <tr>
 * <td><code>region:[regionName]</code></td>
 * <td>this tag inserts every occupant of a region given in <code>[regionName]</code> into the recipients list.</td>
 * </tr>
 * <tr>
 * <td><code>wa:delegates</code></td>
 * <td>this tag inserts every World Assembly delegate into the recipients list.</td>
 * </tr>
 * <tr>
 * <td><code>wa:members</code></td>
 * <td>this tag inserts every World Assembly member into the recipients list. It is best to use this on a regional basis
 * with the <code>-></code> operator to filter out the World Assembly members inside a certain region or to use the
 * <code>--</code> operator to remove WA members as necessary.</td>
 * </tr>
 * <tr>
 * <td><code>world:new</code></td>
 * <td>this tag inserts 50 new nations into the recipients list.</td>
 * </tr>
 * </table>
 *
 * <h2>Grammar</h2> There are a few logical operators for this program to help you refine your list more efficiently.
 * <table>
 * <tr>
 * <td><code>/&emsp;</code></td>
 * <td>tells the parser to exclude <b>all</b> instances of the following nation from the final list, for example,
 * <code>/imperium_anglorum</code> would exclude the nation of Imperium Anglorum from the final list. It is a
 * <code>NOT</code> operator. All lines are invertable.</td>
 * </tr>
 * <tr>
 * <td><code>-></code></td>
 * <td>tells the parser to only add nations which are in both lists (the one before and after the arrow), for example,
 * <code>region:europe -> wa:members</code> would yield the list of European nations in WA members. It is a
 * <code>IN</code> operator.</td>
 * </tr>
 * <tr>
 * <td>--</td>
 * <td>tells the parser to remove any nations listed in the second tag from the first tag, for example,
 * <code>region:europe -- wa:members</code> would leave all nations of Europe who are not WA members. This tag functions
 * much like the <code>/</code> tag, but this negation is not a global negation, as the slash uses, but rather, a local
 * negation for that tag only.</td>
 * </tr>
 * </table>
*/
public class CommuniqueParser {
	
	/** This <code>int</code> determines what version of the parser is currently being used. The entire program is build
	 * around this string for extended compatibility purposes. However, due to the separation between the parser itself
	 * and the IO system, either of them can trigger a change in the version number. */
	public static final int version = 6;
	private static JInfoFetcher fetcher = JInfoFetcher.getInstance();

	/** Determine whether a <code>String</code> is a special tag or not. What strings are tags is determined in the
	 * documentation on the grammar of the Communiqué syntax.
	 *
	 * @param input
	 * @return */
	private boolean isTag(String input) {
		
		if (input.startsWith("region:")) {
			return true;

		} else if (input.equalsIgnoreCase("wa:delegates")) {
			return true;

		} else if (input.equalsIgnoreCase("wa:nations") || input.equalsIgnoreCase("wa:members")) {
			return true;

		} else if (input.equalsIgnoreCase("world:new")) { return true; }

		return false;
	}

	/** Expands a single Communique tag into a list of nations represented by that tag. For example, something like
	 * <code>region:Europe</code> would result in a <code>List&lt;String&gt;</code> of all nations in Europe. Other
	 * elements, like <code>wa:delegates</code> would yield all the delegates in the World Assembly.
	 *
	 * @param tag to be expanded
	 * @return a <code>List&lt;String&gt;</code> of nations represented */
	private List<String> expandTag(String tag) {
		
		if (tag.startsWith("region:")) {
			List<String> regionContentsArr = fetcher.getRegion(tag.replace("region:", ""));
			return regionContentsArr;

		} else if (tag.startsWith("wa:delegate")) {
			List<String> delegatesArr = fetcher.getDelegates();
			return delegatesArr;

		} else if (tag.equals("wa:nations") || tag.equals("wa:members")) {
			List<String> waNationsArr = fetcher.getWAMembers();
			return waNationsArr;

		} else if (tag.equals("world:new")) {
			List<String> newNationsArr = fetcher.getNew();
			return newNationsArr;
		}

		// If all else fails...
		return new ArrayList<String>(0);
	}

	/** Expands the <code>List&lt;String&gt;</code> into a list of nations based on the tags, operators, etc. If you
	 * give it something like <code>region:europe</code>, then you'll get back the entire list of nations in Europe. It
	 * is provided as a list of tags, each on a list. This processes the operators.
	 *
	 * @param tagsList a <code>List&lt;String&gt;</code> of tags */
	private Set<String> expandList(List<String> tagsList) {
		List<String> expandedList = new ArrayList<String>();

		for (int x = 0; x < tagsList.size(); x++) {
			String element = tagsList.get(x).toLowerCase();

			// Operator meaning 'region:europe->wa:nations' would be 'those in Europe in (who are) WA nations'
			if (element.contains("->") || element.contains("--")) {
				
				String[] bothArr = new String[2];
				if (element.contains("->")) {
					bothArr = element.split("->");

				} else if (element.contains("--")) {
					bothArr = element.split("--");
				}

				// Remove leading and trailing underscores.
				for (int i = 0; i < bothArr.length; i++) {
					bothArr[i] = bothArr[i].trim();
					if (bothArr[i].startsWith("_")) {
						bothArr[i] = bothArr[i].substring(1, bothArr[i].length());
					}
					if (bothArr[i].endsWith("_")) {
						bothArr[i] = bothArr[i].substring(0, bothArr[i].length() - 1);
					}
				}

				// Split into the two lists
				// firsts and seconds refer to the elements on either side of the '->' or '--' operator
				Set<String> firsts = new HashSet<>(expandTag(bothArr[0]));
				Set<String> seconds = new HashSet<>(expandTag(bothArr[1]));

				List<String> both = new ArrayList<String>(0);

				// This section is for the addition and subtraction operators
				if (element.contains("->")) {
					
					// If it appears in both lists, add it. Use the new 'contains' algorithm instead of the old 'nested
					// for loops' algorithm. This gives significant speed advantages.
					for (String second : seconds) {
						if (firsts.contains(second)) {
							both.add(second);
						}
					}

				} else if (element.contains("--")) {
					
					// If an element in the first list is also contained in the second list, do not add it to the 'both'
					// list. This basically removes it from the firsts list as output is concerned.
					for (String first : firsts) {
						if (!seconds.contains(first)) {
							both.add(first);
						}
					}

				}

				expandedList.addAll(both);

			} else if (isTag(element)) {
				expandedList.addAll(expandTag(element));

			} else {
				expandedList.add(element);
			}
		}

		// Remove duplicates & return
		LinkedHashSet<String> tagsSet = new LinkedHashSet<String>();
		tagsSet.addAll(expandedList);
		return tagsSet;
	}

	/** This parses the entire contents of the recipients and allows us to actually make the tag system work through
	 * interfacing with the expansion system above. Note that this method automatically handles the removal of commented
	 * lines with <code>#</code> as the comment. This method calls all other methods to process all of the recipients.
	 *
	 * <p>
	 * This is the old version. It is based solely on the provided <code>String[]</code>. The newer method to do this is
	 * by provision of two <code>List&lt;String&gt;</code>, one of the recipients and one of the sentList. That method
	 * is the proper way to call the parsing method. However, this is kept for legacy purposes and the fact that it
	 * automatically processes this data.
	 * </p>
	 *
	 * @param input an array of the recipients, each one on an individual index, which can include commented lines
	 * @return a final array of the recipients, compatible with JavaTelegram */
	public String[] filterAndParse(String[] input) {
		
		// Filter out comments and empty lines
		input = Arrays.stream(input).filter(p -> !p.startsWith("#") && !StringUtils.isEmpty(p)).toArray(String[]::new);

		// Form a list of all the nation we want in this list.
		List<String> recipients = Arrays.stream(input).filter(s -> !s.startsWith("/")).collect(Collectors.toList());
		recipients.stream().forEach(s -> s.toLowerCase().replace(" ", "_").trim());

		// Form a list of all nations we can't have in this list.
		List<String> sentList = Arrays.stream(input).filter(s -> s.startsWith("/")).collect(Collectors.toList());
		sentList.stream().forEach(s -> s.replaceFirst("/", "").toLowerCase().trim().replace(" ", "_"));

		List<String> list = recipientsParse(recipients, sentList);
		return list.toArray(new String[list.size()]);
	}

	/** This method parses the recipients based on the list of recipients and the list of nations to which a telegram
	 * has already been sent. Recipients and sent-s are in tag-form when provided, they are automatically expanded. This
	 * method requires that they are separated individually into two lists.
	 *
	 * @param recipients
	 * @param sentList
	 * @return a <code>List</code> containing the recipients in <code>String</code> format. */
	public List<String> recipientsParse(List<String> recipients, List<String> sentList) {
		
		// Expand the lists.
		Set<String> recipientsExpanded = expandList(recipients);
		Set<String> sentlistExpanded = expandList(sentList);

		// Filter using new algorithm
		List<String> finalRecipients = new ArrayList<String>();
		for (String element : recipientsExpanded) {
			if (!sentlistExpanded.contains(element)) {
				finalRecipients.add(element);
			}
		}

		return finalRecipients;
	}

	/** Convenience method for <code>recipientsParse(String[] input)</code> if you don't feel like rewriting the code to
	 * make it an actual <code>String</code>.
	 *
	 * @param input A <code>String</code>, with each line separated by new line.
	 * @return */
	@Deprecated public String[] recipientsParse(String input) {
		return filterAndParse(input.split("\n"));
	}
}