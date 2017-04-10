/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.javatelegram.JTelegramKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <code>CConfig</code> creates a unified object for the storage and retrieval of the entire state of a Communiqué
 * application. <p> <p> Because it is needed to be able to send all the Communiqué flags and relevant assorted
 * information as a single object, this object was created as an integrated system to do so. This program also contains
 * methods to access the interior components of this class using a <code>Map</code> for cross-interoperability with
 * <code>{@link com.git.ifly6.communique.io.CommuniqueLoader CLoader}</code>, <code>{@link
 * com.git.ifly6.communique.io.CommuniqueReader CReader}</code>, and <code>{@link com.git.ifly6.communique.io.CommuniqueWriter
 * CWriter}</code>, which are based on the Java properties file system. Also, the widespread use of reflection in
 * dealing with a <code>Map{@code <String, String>}</code> will allow for greater extensibility over time and
 * significantly less human error in providing methods to access such data. </p>
 */
public class CommuniqueConfig implements java.io.Serializable {

	// For reflection in CLoader to work, these MUST be the only fields
	// For backwards compatibility, these names cannot be changed

	private static final long serialVersionUID = Communique7Parser.version;

	public static final String HEADER = "Communiqué Configuration File. Do not edit by hand. Produced at: "
			+ CommuniqueUtilities.getCurrentDateAndTime() + ". Produced by version " + Communique7Parser.version;

	public int version;

	public boolean isRecruitment;
	public boolean isRandomised;
	public boolean isDelegatePrioritised;

	public JTelegramKeys keys;

	/* There is a trade-off between the ability to actually edit these files by hand, which is something that I
	happen to do, and the simple ease of saving all recipients as List<CommuniqueRecipient>. Doing so makes it
	exceptionally hard to edit by hand -- for little computing benefit, since I can just create accessor methods
	which duplicate basically all the functionality. */
	private List<String> cRecipients;

	// Deprecating the old String-based system, keeping for backward compatibility
	// TODO: 15/3/2017 get rid of these fields after a few updates
	@Deprecated
	public String[] recipients;
	@Deprecated
	public String[] sentList;

	public int defaultVersion() {
		this.version = Communique7Parser.version;
		return Communique7Parser.version;
	}

	public List<CommuniqueRecipient> getcRecipients() {
		// use explicit for speed
		ArrayList<CommuniqueRecipient> list = new ArrayList<>(cRecipients.size());
		for (String s : cRecipients)
			list.add(CommuniqueRecipient.parseRecipient(s));
		return list;
	}

	public List<String> getcRecipientsString() {
		return cRecipients;
	}

	public void setcRecipients(List<CommuniqueRecipient> crs) {
		// use explicit for speed
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
	 * save to the program. For backward compatibility, it also applies these changes to the old
	 * <code>recipients</code> and the <code>sentList</code>. It also updates the <code>CommuniqueConfig</code> version
	 * <i>field</i>, not the one in the header, to the version of the program on which it was saved.
	 */
	void checkData() {

		Function<String, String> cleanNation = s -> {
			int lastColon = s.lastIndexOf(":");
			if (lastColon > 1) {
				// nation:nation:blah
				// ^      ^------^   ^ (where '-' means it is removed)
				return s.substring(0, s.indexOf(":") + 1) + s.substring(lastColon + 1, s.length());
			}
			return s;
		};

		version = this.defaultVersion();
		cRecipients = cRecipients.stream().distinct()
				.map(cleanNation)
				.collect(Collectors.toList());

		if (recipients != null && recipients.length > 0)
			recipients = Stream.of(recipients).distinct().map(cleanNation).toArray(String[]::new);
		if (sentList != null && sentList.length > 0)
			sentList = Stream.of(sentList).distinct().map(cleanNation).toArray(String[]::new);
	}

}
