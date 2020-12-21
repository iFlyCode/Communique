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

package com.git.ifly6.nsapi.telegram;

import org.apache.commons.text.WordUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.git.ifly6.nsapi.telegram.JTelegramConstants.DEFAULT_DURATION;
import static com.git.ifly6.nsapi.telegram.JTelegramConstants.RECRUIT_DURATION;

/**
 * Specifies different types of telegrams to be sending and their default timings and names.
 */
public enum JTelegramType {


    /**
     * Timing for recruitment telegrams with {@link JTelegramConstants#RECRUIT_DURATION} delay.
     */
    RECRUIT(RECRUIT_DURATION) {},

    /**
     * Timing for campaign telegrams with {@link JTelegramConstants#DEFAULT_DURATION} delay.
     */
    CAMPAIGN(DEFAULT_DURATION) {},

    /**
     * Custom telegram type with custom timing. Default timing is still {@link JTelegramConstants#DEFAULT_DURATION} but
     * can be set to any desired value. Note that when the internal value is changed, it is changed for all references
     * to it because there is only one instance of the enum value.
     */
    CUSTOM(DEFAULT_DURATION) {
        @Override
        public String toString() {
            return String.format("Custom (%d ms)", waitDuration.toMillis());
        }
    },

    /**
     * Timing for unmarked telegrams with {@link JTelegramConstants#DEFAULT_DURATION} delay.
     */
    NONE(DEFAULT_DURATION) {};

    protected Duration waitDuration;

    JTelegramType(Duration waitDuration) {
        this.waitDuration = waitDuration;
    }

    public Duration getWaitDuration() {
        return waitDuration;
    }

    public void setWaitDuration(Duration waitDuration) {
        if (this != CUSTOM) throw new UnsupportedOperationException(
                String.format("Cannot change internals of constant %s", this.toString()));
        this.waitDuration = waitDuration;
    }

    /**
     * Wait time defined by the telegram type; in milliseconds.
     * @return wait time in milliseconds
     */
    public int getWaitTime() {
        return (int) waitDuration.toMillis();
    }

    @Override
    public String toString() {
        return WordUtils.capitalize(super.toString());
    }

    /** Returns presets */
    public static JTelegramType[] getPresets() {
        return Arrays.stream(JTelegramType.values())
                .filter(t -> t != JTelegramType.CUSTOM)
                .toArray(JTelegramType[]::new);
    }

    /**
     * Parses telegram type.
     * @return telegram type
     * @see JTelegramType#CUSTOM
     */
    public static JTelegramType parseType(String input) {
        try {
            return JTelegramType.valueOf(input);
        } catch (IllegalArgumentException ignored) { }

        Matcher m = Pattern.compile("(?<=Custom \\()\\d+(?= ms\\))").matcher(input);
        if (m.find()) { // if it matches this pattern
            int ms = Integer.parseInt(m.group());
            JTelegramType type = JTelegramType.CUSTOM;
            type.setWaitDuration(Duration.ofMillis(ms));
            return type;
        }

        throw new IllegalArgumentException(String.format("Cannot parse input %s", input));
    }

}
