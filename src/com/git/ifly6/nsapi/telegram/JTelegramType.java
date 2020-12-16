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

import java.time.Duration;

import static com.git.ifly6.nsapi.telegram.JTelegramConstants.DEFAULT_MILLIS;

/**
 * Specifies different types of telegrams to be sending and their default timings and names.
 */
public enum JTelegramType {

    /**
     * Timing for campaign telegrams with {@link JTelegramConstants#DEFAULT_MILLIS} delay.
     */
    CAMPAIGN {
        @Override
        public int getWaitTime() {
            return (int) JTelegramConstants.DEFAULT_DURATION.toMillis();
        }

        @Override
        public Duration getWaitDuration() {
            return JTelegramConstants.DEFAULT_DURATION;
        }

        @Override
        public String toString() {
            return "Campaign";
        }
    },

    /**
     * Timing for recruitment telegrams with wait timing at 180.05 seconds.
     */
    RECRUIT {
        private final Duration RECRUIT_DURATION = Duration.ofMillis(Math.round(180.05 * 1000L));

        @Override
        public int getWaitTime() {
            return (int) RECRUIT_DURATION.toMillis();
        }

        @Override
        public Duration getWaitDuration() {
            return RECRUIT_DURATION;
        }

        @Override
        public String toString() {
            return "Recruitment";
        }
    },

    /**
     * Custom telegram type with custom timing. Default timing is still {@link JTelegramConstants#DEFAULT_MILLIS} but
     * can be set to any desired value.
     */
    CUSTOM {
        private Duration time = JTelegramConstants.DEFAULT_DURATION;

        @Override
        public int getWaitTime() {
            return (int) time.toMillis();
        }

        @Override
        public Duration getWaitDuration() {
            return null;
        }

        public void setWaitDuration(Duration d) {
            time = d;
        }

        @Override
        public String toString() {
            return String.format("Custom timing at %d ms", time.toMillis());
        }
    },

    /**
     * Timing for unmarked telegrams with {@link JTelegramConstants#DEFAULT_MILLIS} delay.
     */
    NONE {
        @Override
        public int getWaitTime() {
            return DEFAULT_MILLIS;
        }

        @Override
        public Duration getWaitDuration() {
            return null;
        }

        @Override
        public String toString() {
            return "No type";
        }
    };

    public abstract Duration getWaitDuration();

    /**
     * Wait time defined by the telegram type; in milliseconds.
     * @return wait time in milliseconds
     */
    public abstract int getWaitTime();

    public abstract String toString();

}
