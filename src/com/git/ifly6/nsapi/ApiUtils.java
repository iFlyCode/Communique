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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility functions for handling or parsing information from the NationStates API. See also <a
 * href="https://www.nationstates.net/pages/api.html">API documentation</a>.
 */
public class ApiUtils {

    private ApiUtils() {
    }

    /**
     * Changes some name into a reference name. Reference names are when all letters are lower case and all spaces are
     * substituted with underscores.
     * @param input to turn into a reference name
     * @return reference name form of input
     */
    @Nonnull
    public static String ref(String input) {
        return Objects.requireNonNull(input).trim().toLowerCase().replaceAll("\\s", "_");
    }

    /**
     * Applies {@link ApiUtils#ref(String)} to elements; removes empty (or all-white-space) elements.
     * @param list to convert to reference format
     * @return elements in reference form
     */
    public static List<String> ref(List<String> list) {
        List<String> refs = new ArrayList<>(list.size());
        for (String s : list)
            if (isNotEmpty(s))
                refs.add(ApiUtils.ref(s));

        return refs;
    }

    /**
     * Tests whether first string starts with the latter string; case insensitive.
     * @param s      to test
     * @param prefix to look for prefix
     * @return true if prefix (case-insensitive) present; false otherwise
     */
    public static boolean startsWithLowerCase(String s, String prefix) {
        return s.toLowerCase().startsWith(prefix.toLowerCase());
    }

    /**
     * Trims string and checks whether it is empty
     * @param s to trim and check
     * @return true if empty
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
     * Determines whether a string is empty after trimming. See inverse {@link #isEmpty(String)}.
     * @param s to check for empty-ness
     * @return true if not empty
     */
    public static boolean isNotEmpty(String s) {
        return !ApiUtils.isEmpty(s);
    }

    /**
     * Determines whether an array contains some value. Method does not check type.
     * @param hay    to check in
     * @param needle to check for
     * @return whether array contains needle
     */
    public static boolean contains(Object[] hay, Object needle) {
        if (isEmpty(hay)) return false;
        if (needle == null) return false;
        for (Object element : hay)
            if (element.equals(needle))
                return true;

        return false;
    }

}
