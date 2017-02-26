/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.javatelegram.JTelegramKeys;

/** <code>CConfig</code> creates a unified object for the storage and retrieval of the entire state of a Communiqué
 * application.
 *
 * <p>
 * Because it is needed to be able to send all the Communiqué flags and relevant assorted information as a single
 * object, this object was created as an integrated system to do so. This program also contains methods to access the
 * interior components of this class using a <code>Map</code> for cross-interoperability with
 * <code>{@link com.git.ifly6.communique.io.CommuniqueLoader CLoader}</code>,
 * <code>{@link com.git.ifly6.communique.io.CommuniqueReader CReader}</code>, and
 * <code>{@link com.git.ifly6.communique.io.CommuniqueWriter CWriter}</code>, which are based on the Java properties file system.
 * Also, the widespread use of reflection in dealing with a <code>Map{@code <String, String>}</code> will allow for
 * greater extensibility over time and significantly less human error in providing methods to access such data.
 * </p>
*/
public class CommuniqueConfig implements java.io.Serializable {
	
	// For reflection in CLoader to work, these MUST be the only fields
	// For backwards compatibility, these names cannot be changed
	
	private static final long serialVersionUID = Communique7Parser.version;
	
	public final String header = "Communiqué Configuration File. Do not edit by hand. Produced at: "
			+ CommuniqueUtilities.getCurrentDateAndTime() + ". Produced by version " + Communique7Parser.version;
	
	public int version;
	
	public boolean isRecruitment;
	public boolean isRandomised;
	public boolean isDelegatePrioritised;
	
	public JTelegramKeys keys;
	
	public String[] recipients;
	public String[] sentList;
	
	public int defaultVersion() {
		this.version = Communique7Parser.version;
		return Communique7Parser.version;
	}
}
