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

/**
 * Specifies different types of telegrams to be sending and their default timings and names.
 */
public enum JTelegramType {

	/**
	 * Timing for campaign telegrams with {@link #DEFAULT_TIMING} delay.
	 */
	CAMPAIGN {
		@Override
		public int getWaitTime() {
			return DEFAULT_TIMING;
		}

		@Override
		public void setDefaultTime(int i) {
			throw new UnsupportedOperationException("Cannot set time on immutable setting");
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
		@Override
		public int getWaitTime() {
			return (int) (180.05 * 1000);
		}

		@Override
		public void setDefaultTime(int i) {
			throw new UnsupportedOperationException("Cannot set time on immutable setting");
		}

		@Override
		public String toString() {
			return "Recruitment";
		}

	},

	/**
	 * Custom telegram type with custom timing. Default timing is still {@link #DEFAULT_TIMING} but can be set to any
	 * desired value.
	 */
	CUSTOM {
		private int time = DEFAULT_TIMING;

		@Override
		public int getWaitTime() {
			return time;
		}

		@Override
		public void setDefaultTime(int i) {
			time = i;
		}

		@Override
		public String toString() {
			return String.format("Custom timing (%d ms)", time);
		}
	},

	/**
	 * Timing for unmarked telegrams with {@link #DEFAULT_TIMING} delay.
	 */
	NONE {
		@Override
		public int getWaitTime() {
			return DEFAULT_TIMING;
		}

		@Override
		public void setDefaultTime(int i) {
			throw new UnsupportedOperationException("Cannot set time on immutable setting");
		}

		@Override
		public String toString() {
			return "No type";
		}

	};

	public static final int DEFAULT_TIMING = (int) (30.05 * 1000);

	/**
	 * Wait time defined by the telegram type; in milliseconds.
	 * @return wait time in milliseconds
	 */
	public abstract int getWaitTime();

	public abstract void setDefaultTime(int i);

	public abstract String toString();

}
