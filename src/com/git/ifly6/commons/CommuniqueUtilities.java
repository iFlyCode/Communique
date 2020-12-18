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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** This class is a collection of utility methods used inside Communique programs. */
public class CommuniqueUtilities {

    public static final boolean IS_OS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");
    public static final boolean IS_OS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

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
     * Returns the format for the <code>getCurrentDate</code> for a <code>SimpleDateFormat</code>.
     * @return a <code>String</code> containing <code>yyyy-MM-dd</code>
     */
    public static String dateFormat() {
        return "yyyy-MM-dd";
    }

    /**
     * Returns the format for the <code>getCurrentDateAndTimeFormat</code> for a <code>SimpleDateFormat</code>.
     * @return a <code>String</code> containing <code>yyyy-MM-dd HH:mm:ss.SSS</code>
     */
    public static String dateTimeFormat() {
        return "yyyy-MM-dd HH:mm:ss.SSS";
    }

    /**
     * Returns a <code>String</code> stating the current date and time in the format defined by {@link
     * CommuniqueUtilities#dateTimeFormat()}
     * @return the date and time
     */
    public static String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat());
        return formatter.format(LocalDateTime.now());
    }

    /**
     * @return Gets a Java ISO local date time formatted with colons replaced for hyphens
     */
    public static String getTime() {
        // must avoid colons in file names because windows doesn't like it apparently
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())
                .format(Instant.now().truncatedTo(ChronoUnit.SECONDS)) // truncate to seconds to ignore decimals
                .replace(':', '-') // hacky, whatever
                .replace('T', ' ');
    }
}