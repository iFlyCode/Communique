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
package com.git.ifly6.commons;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;

/** This class is a collection of utility methods used inside Communique programs. */
public class CommuniqueUtilities {

    public static final boolean IS_OS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");
    public static final boolean IS_OS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    public static final Predicate<? super String> NO_COMMENTS = s -> !s.startsWith("#") && !s.startsWith("//");

    // Prevent initialisation
    private CommuniqueUtilities() {
    }

    /**
     * Converts raw seconds into days, hours, minutes, and seconds.
     * @param seconds elapsed
     * @return a string in days, hours, minutes, and seconds
     */
    public static String time(long seconds) {
        long minutes = seconds / 60;
        seconds -= minutes * 60;
        long hours = minutes / 60;
        minutes -= hours * 60;
        long days = hours / 24;
        hours -= days * 24;
        return String.format("%dd:%dh:%dm:%ds", days, hours, minutes, seconds);
    }

    /**
     * Current date and time in ISO format with 'T' -> ' '.
     * @return the date and time
     */
    public static String getDate() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME
                .format(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .replace('T', ' ');
    }

    /**
     * @return Gets a Java ISO local date time formatted with colons replaced for hyphens
     */
    public static String getWindowsSafeDate() {
        // must avoid colons in file names because windows doesn't like it apparently
        return getDate().replace(':', '-');
    }
}