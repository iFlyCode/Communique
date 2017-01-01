/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/** This class is a collection of utility methods used inside Communique programs. */
public class CommuniqueUtilities {
	
	// Prevent initialisation
	private CommuniqueUtilities() {
	}
	
	/** Changes some name into a reference name.
	 * @param name to turn into a reference name
	 * @return reference name form of the input name */
	public static String ref(String name) {
		return name.trim().toLowerCase().replace("\\s", "_");
	}
	
	/** This changes raw seconds directly into days, hours, minutes, and seconds. Very helpful for creating a system of
	 * information which humans can use.
	 * @param seconds elapsed
	 * @return a string in days, hours, minutes, and seconds */
	public static String time(int seconds) {
		int minutes = seconds / 60;
		seconds -= minutes * 60;
		int hours = minutes / 60;
		minutes -= hours * 60;
		int days = hours / 24;
		hours -= days * 24;
		return days + "d:" + hours + "h:" + minutes + "m:" + seconds + "s";
	}
	
	/** Returns the format for the <code>getCurrentDate</code> for a <code>SimpleDateFormat</code>.
	 * @return a <code>String</code> containing <code>yyyy-MM-dd</code> */
	public static String getCurrentDateFormat() {
		return "yyyy-MM-dd";
	}
	
	/** Returns the format for the <code>getCurrentDateAndTimeFormat</code> for a <code>SimpleDateFormat</code>.
	 * @return a <code>String</code> containing <code>yyyy-MM-dd HH:mm:ss.SSS</code> */
	public static String getCurrentDateAndTimeFormat() {
		return "yyyy-MM-dd HH:mm:ss.SSS";
	}
	
	/** Returns a <code>String</code> stating the current date in the format defined by
	 * {@link CommuniqueUtilities#getCurrentDateFormat()}
	 * @return the date */
	public static String getCurrentDate() {
		DateFormat dateDays = new SimpleDateFormat(getCurrentDateFormat());
		return dateDays.format(new Date());
	}
	
	/** Returns a <code>String</code> stating the current date and time in the format defined by
	 * {@link CommuniqueUtilities#getCurrentDateAndTimeFormat()}
	 * @return the date and time */
	public static String getCurrentDateAndTime() {
		DateFormat dateWithTime = new SimpleDateFormat(getCurrentDateAndTimeFormat());
		return dateWithTime.format(new Date());
	}
	
}