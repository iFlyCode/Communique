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

package com.git.ifly6.nsapi.ctelegram.io;

import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;

import java.util.Arrays;

/**
 * Thrown if parsing parameter to whatever specialised object from text fails. All public non-abstract constructors for
 * {@link CommMonitor} implementors should {@link String}-only parameters.
 * @since version 3.0 (build 13)
 */
public class CommParseException extends RuntimeException {
    public CommParseException(String message) {
        super(message);
    }

    public CommParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public static <T> CommParseException make(String input, T[] validValues, Throwable cause) {
        String message = format(input, validValues);
        return new CommParseException(message, cause);
    }

    public static <T> CommParseException make(String input, Object[] validValues) {
        final String message = format(input, validValues);
        return new CommParseException(message);
    }

    private static <T> String format(String input, T[] validValues) {
        return String.format(
                "Invalid input <%s>. Valid options are %s", input,
                validValues.length > 1
                        ? Arrays.toString(Arrays.stream(validValues).map(Object::toString).toArray(String[]::new))
                        : validValues[0].toString());
    }
}
