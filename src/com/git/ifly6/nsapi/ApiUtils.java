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
package com.git.ifly6.nsapi;

import java.util.ArrayList;
import java.util.List;

public class ApiUtils {

	private ApiUtils() {
	}

	/**
	 * Changes some name into a reference name.
	 * @param input to turn into a reference name
	 * @return reference name form of the input name
	 */
	public static String ref(String input) {
		if (input == null)
			throw new NullPointerException("Cannot convert null string to reference format");
		return input.trim().toLowerCase().replace("\\s", "_");
	}

	/**
	 * Applies the {@link ApiUtils#ref(String)}; removes empty (or all-white-space) elements.
	 * @param list to convert to reference format
	 * @return elements in reference form
	 */
	public static List<String> ref(List<String> list) {
		List<String> refs = new ArrayList<>(list.size());
		for (String s : list)
			if (!s.trim().isEmpty())
				refs.add(ApiUtils.ref(s));

		return refs;
	}

	/**
	 * Trims string and checks whether it is empty
	 * @param s to trim and check
	 * @return empty if nothing but input contains nothing but whitespace
	 */
	public static boolean isEmpty(String s) {
		if (s == null)
			return true;
		return s.trim().isEmpty(); // isEmpty : value.length == 0
	}

	/**
	 * Determines whether a given array is empty.
	 * @param a array
	 * @return true if empty
	 */
	public static boolean isEmpty(Object[] a) {
		return a == null || a.length == 0;
	}

	/**
	 * @param s to check for empty-ness
	 * @return inverted {@link #isEmpty(String)}
	 */
	public static boolean isNotEmpty(String s) {
		return !ApiUtils.isEmpty(s);
	}

	/**
	 * Determines whether an array contains some value, utilising the standard <code>Object</code> equals method. This
	 * method does not do type-checking.
	 * @param array  to check in
	 * @param needle to check for
	 * @return whether array contains needle
	 */
	public static boolean contains(Object[] array, Object needle) {
		if (isEmpty(array)) return false;
		if (needle == null) return false;
		for (Object element : array)
			if (element.equals(needle))
				return true;

		return false;
	}

}
