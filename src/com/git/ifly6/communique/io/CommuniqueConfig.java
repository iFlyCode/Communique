/*
 * Copyright (c) 2020 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.communique.io;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <code>CommuniqueConfig</code> creates a unified object for the storage and retrieval of configuration information
 * necessary to have persistent states between Communiqué or Marconi instances.
 */
public class CommuniqueConfig implements java.io.Serializable {

	// For backwards compatibility, almost all field names cannot be changed
	private static final long serialVersionUID = Communique7Parser.BUILD;

	public static final String HEADER = "Communiqué Configuration File. Do not edit by hand. Produced at: "
			+ CommuniqueUtilities.getDate() + ". Produced by version " + Communique7Parser.BUILD;

	public int version;

	protected boolean isRecruitment;
	public CommuniqueProcessingAction processingAction;

	public JTelegramKeys keys;
	public JTelegramType telegramType;
	public String waitString;

	/**
	 * Holds all of the Communique recipients in <code>String</code>s so that it can be edited by hand and not as {@link
	 * CommuniqueRecipient}.
	 * <p>To keep this editable by hand, the configuration system uses getters and setters to translate to and from the
	 * string state representations to present to the programmer a {@link CommuniqueRecipient} API but actually store
	 * everything in strings.</p>
	 */
	private ArrayList<String> cRecipients; // must be mutable, use ArrayList

	// These should be deprecated, but are kept for backward compatibility
	public String[] recipients; // consider removing
	public String[] sentList;   // consider removing

	/**
	 * Empty constructor for {@link CommuniqueConfig}
	 */
	public CommuniqueConfig() {
		this.keys = new JTelegramKeys(); // empty keys
		this.version = defaultBuild(); // default version to current version
		this.processingAction = CommuniqueProcessingAction.NONE; // no processing action
	}

	/**
	 * Constructor for <code>{@link CommuniqueConfig}</code>s. All the
	 * <code>{@link CommuniqueRecipient}</code>s should be specified after the fact.
	 * @param t                the type of telegrams configured to be sent
	 * @param processingAction is the applicable processing action
	 * @param keys             are the keys
	 * @param s                for the wait string
	 */
	public CommuniqueConfig(JTelegramType t, CommuniqueProcessingAction processingAction,
	                        JTelegramKeys keys, String s) {
		this();
		this.telegramType = t;
		this.processingAction = processingAction;
		this.keys = keys;
		this.waitString = s;
	}

	/**
	 * Sets the default version to the version in {@link Communique7Parser}.
	 * @return the version in <code>Communique7Parser</code>
	 */
	public int defaultBuild() {
		this.version = Communique7Parser.BUILD;
		return Communique7Parser.BUILD;
	}

	/**
	 * Returns converted <code>cRecipients</code> to <code>List&lt;CommuniqueRecipient&gt;</code>
	 * @return <code>cRecipients</code> converted to <code>List&lt;CommuniqueRecipient&gt;</code>
	 */
	public List<CommuniqueRecipient> getcRecipients() {
		if (cRecipients == null) return null; // deal with null case

		// use imperative for speed
		List<CommuniqueRecipient> list = new ArrayList<>(cRecipients.size());
		for (String s : cRecipients)
			list.add(CommuniqueRecipient.parseRecipient(s));
		return list;
	}

	/**
	 * Returns raw <code>cRecipients</code>, which is <code>List&lt;String&gt;</code>
	 * @return <code>cRecipients</code>
	 */
	public List<String> getcRecipientsString() {
		return cRecipients;
	}

	/**
	 * Sets <code>cRecipients</code> with <code>List&lt;CommuniqueRecipient&gt;</code>, translates to
	 * <code>String</code> on the fly.
	 * @param crs {@link CommuniqueRecipient}s to set
	 */
	public void setcRecipients(List<CommuniqueRecipient> crs) {
		// NOTE: No setcRecipients(List<String> crs) because need for verification
		// use imperative for speed
		ArrayList<String> list = new ArrayList<>(crs.size());
		for (CommuniqueRecipient cr : crs)
			list.add(cr.toString());
		cRecipients = list;
	}

	public void addcRecipient(CommuniqueRecipient cr) {
		cRecipients.add(cr.toString());
	}

	/**
	 * Checks all the data kept in {@link CommuniqueConfig#cRecipients} and makes they are distinct and applicable to
	 * save to the program. For backward compatibility, it also applies these changes to the old <code>recipients</code>
	 * and the <code>sentList</code>. It also updates the <code>CommuniqueConfig</code> version <i>field</i>, not the
	 * one in the header, to the version of the program on which it was saved.
	 */
	void clean() {
		version = this.defaultBuild(); // updates version

		// proceeds to clean all of the fields
		cRecipients = cRecipients.stream().distinct()
				.map(CommuniqueConfig::cleanNation)
				.collect(Collectors.toCollection(ArrayList::new));

		if (Objects.nonNull(recipients) && recipients.length > 0)
			recipients = Arrays.stream(recipients)
					.distinct()
					.map(CommuniqueConfig::cleanNation)
					.toArray(String[]::new);

		if (Objects.nonNull(sentList) && sentList.length > 0)
			sentList = Arrays.stream(sentList)
					.distinct()
					.map(CommuniqueConfig::cleanNation)
					.toArray(String[]::new);
	}

	/**
	 * Cleans nation names that could have been prefixed accidentally in a previous version of Communique
	 * @param recipientString is the string-name of the nation
	 * @return the same with all the extra 'nation:'s removed.
	 */
	private static String cleanNation(String recipientString) {
		return recipientString.replace(":(nation:)*", ":");
	}

}
