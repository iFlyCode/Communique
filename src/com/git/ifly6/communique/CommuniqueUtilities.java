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
		DateFormat dateWithTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return dateWithTime.format(new Date());
	}
}