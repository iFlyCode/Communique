/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.javatelegram.JTelegramKeys;

/** <code>CommuniqueConfig</code> creates a unified object for the storage and retrieval of configuration information
 * necessary to have persistent states between Communiqué or Marconi instances. */
public class CommuniqueConfig implements java.io.Serializable {
	
	// For backwards compatibility, these names cannot be changed
	
	private static final long serialVersionUID = Communique7Parser.version;
	
	public static final String HEADER = "Communiqué Configuration File. Do not edit by hand. Produced at: "
			+ CommuniqueUtilities.getCurrentDateAndTime() + ". Produced by version " + Communique7Parser.version;
	
	public int version;
	
	public boolean isRecruitment;
	public CommuniqueProcessingAction processingAction = CommuniqueProcessingAction.NONE;
	
	public JTelegramKeys keys;
	
	/** Holds all of the Communique recipients in <code>String</code>s so that it can be edited by hand and not as
	 * {@link CommuniqueRecipient}. */
	private ArrayList<String> cRecipients; // must be mutable
	// Deprecating the old String-based system, keeping for backward compatibility
	@Deprecated public String[] recipients;
	@Deprecated public String[] sentList;
	
	/** Sets the default version to the version in {@link Communique7Parser}.
	 * @return the version in <code>Communique7Parser</code> */
	public int defaultVersion() {
		this.version = Communique7Parser.version;
		return Communique7Parser.version;
	}
	
	/** Returns converted {@link cRecipients} to <code>List&lt;CommuniqueRecipient&gt;</code>
	 * @return {@link cRecipients} converted to <code>List&lt;CommuniqueRecipient&gt;</code> */
	public List<CommuniqueRecipient> getcRecipients() {
		// use imperative for speed
		ArrayList<CommuniqueRecipient> list = new ArrayList<>(cRecipients.size());
		for (String s : cRecipients)
			list.add(CommuniqueRecipient.parseRecipient(s));
		return list;
	}
	
	/** Returns raw {@link cRecipients}, which is <code>List&lt;String&gt;</code>
	 * @return {@link cRecipients} */
	public List<String> getcRecipientsString() {
		return cRecipients;
	}
	
	/** Sets {@link cRecipients} with <code>List&lt;CommuniqueRecipient&gt;</code>, translates to <code>String</code> on
	 * the fly.
	 * @param crs {@link CommuniqueRecipient}s to set */
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
	
	/** Checks all the data kept in {@link CommuniqueConfig#cRecipients} and makes they are distinct and applicable to
	 * save to the program. For backward compatibility, it also applies these changes to the old <code>recipients</code>
	 * and the <code>sentList</code>. It also updates the <code>CommuniqueConfig</code> version <i>field</i>, not the
	 * one in the header, to the version of the program on which it was saved. */
	void checkData() {
		
		Function<String, String> cleanNation = s -> {
			int lastColon = s.lastIndexOf(":");
			if (lastColon > 1)
				// nation:nation:blah
				// >>> ^ ^-------^ ^ (where '-' means it is removed)
				return s.substring(0, s.indexOf(":") + 1) + s.substring(lastColon + 1, s.length());
			return s;
		};
		
		version = this.defaultVersion();
		cRecipients = cRecipients.stream().distinct()
				.map(cleanNation)
				.collect(Collectors.toCollection(ArrayList::new));
		
		if (recipients != null && recipients.length > 0)
			recipients = Stream.of(recipients).distinct().map(cleanNation).toArray(String[]::new);
		if (sentList != null && sentList.length > 0)
			sentList = Stream.of(sentList).distinct().map(cleanNation).toArray(String[]::new);
	}
	
}
