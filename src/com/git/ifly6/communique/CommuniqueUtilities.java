/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** This class is a collection of utility methods used inside Communique programs. */
public class CommuniqueUtilities {
	
	// Prevent initialisation
	private CommuniqueUtilities() {
	}
	
	/** Changes some name into a reference name.
	 * @param name to turn into a reference name
	 * @return reference name form of the input name */
	public static String ref(String name) {
		return name.trim().toLowerCase().replace(" ", "_");
	}
	
	/** Applies the {@link #ref(String)} to all elements in a <code>List</code>
	 * @param list
	 * @return a <code>List</code> with all elements having ref applied */
	public static List<String> ref(List<String> list) {
		List<String> refs = new ArrayList<>();
		list.forEach(s -> refs.add(CommuniqueUtilities.ref(s)));
		return refs;
	}
	
	/** This changes raw seconds directly into days, hours, minutes, and seconds. Very helpful for creating a system of
	 * information which humans can use.
	 * @param seconds elapsed
	 * @return a string in days, hours, minutes, and seconds */
	public static String time(int seconds) {
		Duration duration = Duration.of(seconds, ChronoUnit.SECONDS);
		return String.format("%dd:%dh:%dm:%ds", duration.toDaysPart(), duration.toHoursPart(), duration.toMinutesPart(),
				duration.toSecondsPart());
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
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getCurrentDateFormat());
		return formatter.format(LocalDateTime.now());
	}
	
	/** Returns a <code>String</code> stating the current date and time in the format defined by
	 * {@link CommuniqueUtilities#getCurrentDateAndTimeFormat()}
	 * @return the date and time */
	public static String getCurrentDateAndTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getCurrentDateAndTimeFormat());
		return formatter.format(LocalDateTime.now());
	}
	
}