/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** This class is a collection of utility methods used inside Communique programs. */
public class CommuniqueUtilities {
	
	// Prevent initialisation
	private CommuniqueUtilities() {
	}
	
	/** Randomises an array's sequence of contents.
	 * @param inputArray which is to be shuffled
	 * @return a copy of the inputArray which is shuffled randomly */
	public static Object[] randomiseArray(Object[] inputArray) {
		Random rnd = ThreadLocalRandom.current();
		for (int i = inputArray.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			Object a = inputArray[index];
			inputArray[index] = inputArray[i];
			inputArray[i] = a;
		}
		return inputArray;
	}
	
	/** Randomises an array's sequence of contents with a given seed, which allows for a consistent randomisation order.
	 * Obviously, this shouldn't be used for anything really all that important.
	 * @param inputArray which is to be shuffled
	 * @param seed which is used in the shuffling
	 * @return a copy of the inputArray which is shuffled randomly */
	public static Object[] randomiseArray(Object[] inputArray, long seed) {
		Random rnd = new Random(seed);
		for (int i = inputArray.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			Object a = inputArray[index];
			inputArray[index] = inputArray[i];
			inputArray[i] = a;
		}
		return inputArray;
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
	
	public static String getCurrentDate() {
		DateFormat dateDays = new SimpleDateFormat("yyyy-MM-dd");
		return dateDays.format(new Date());
	}
	
	public static String getCurrentDateAndTime() {
		DateFormat dateWithTime = new SimpleDateFormat(getCurrentDateAndTimeFormat());
		return dateWithTime.format(new Date());
	}
	
	public static String getCurrentDateAndTimeFormat() {
		return "yyyy-MM-dd HH:mm:ss.SSS";
	}
}