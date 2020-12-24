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

/**
 * Splits strings and produces error if count is incorrect.
 * @since version 3.0 (build 13)
 */
public class CommuniqueSplitter {

    public static final String SPLIT_PATTERN = ";\\s+";

    private final String name;
    private final int requiredArguments;

    /**
     * Constructs splitter. Splitters are immutable.
     * @param name              of associated parsing algorithm
     * @param requiredArguments number thereof, from the split
     */
    public CommuniqueSplitter(String name, int requiredArguments) {
        this.name = name;
        this.requiredArguments = requiredArguments;
    }

    /**
     * Splits the given input on {@link #SPLIT_PATTERN}. If the resulting elements do not have the required number of
     * arguments, throws error.
     * @param input to split
     * @return input, after split; or thrown exception
     * @throws IllegalArgumentException if argument count incorrect
     */
    public String[] split(String input) {
        String[] elements = input.trim().split(SPLIT_PATTERN);
        if (elements.length != requiredArguments)
            throw new IllegalArgumentException(String.format("Parser for %s demands %d arguments",
                    name, requiredArguments));

        return elements;
    }

}
