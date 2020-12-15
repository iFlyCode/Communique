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
package com.git.ifly6.communique.io;

import com.git.ifly6.nsapi.telegram.util.JInfoCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This implements post-processing actions on the recipient list as a whole for Communique and Marconi clients. It has
 * various functions, like randomisation, reversing the order of recipients, and prioritisation of delegates.
 */
public enum CommuniqueProcessingAction {

	/** Randomises the order of recipients */
	RANDOMISE {
		@Override
		public List<String> apply(List<String> input) {
			Collections.shuffle(input);
			return input; // do nothing
		}

		@Override
		public String toString() {
			return "Randomise order";
		}
	},

	/** Reverses the initial order of recipients */
	REVERSE {
		@Override
		public List<String> apply(List<String> input) {
			for (int i = 0; i < input.size() / 2; i++) { // algorithm to quickly reverse a list
				String original = input.get(i);
				int other = input.size() - 1 - i;
				input.set(i, input.get(other));
				input.set(other, original);
			}
			return input; // do nothing
		}

		@Override
		public String toString() {
			return "Reverse order";
		}
	},

	/**
	 * Moves all delegates in the list of recipients to the front, randomises its order, then places all other nations
	 * in a random order after those delegates.
	 */
	DELEGATE_PRIORITISE {
		@Override
		public List<String> apply(List<String> input) {
			Set<String> delegates = new HashSet<>(JInfoCache.getInstance().getDelegates());

			List<String> listDelegates = new ArrayList<>(); // delegates we have
			List<String> nonDelegate = new ArrayList<>(); // the rest
			for (String s : input)
				if (delegates.contains(s)) listDelegates.add(s);
				else nonDelegate.add(s);

			Collections.shuffle(listDelegates);
			Collections.shuffle(nonDelegate);

			listDelegates.addAll(nonDelegate);
			return listDelegates;
		}

		@Override
		public String toString() {
			return "Prioritise delegates";
		}
	},

	NONE {
		@Override
		public List<String> apply(List<String> input) {
			return input;
		}

		@Override
		public String toString() {
			return "None";
		}
	};

	/** Applies the processing action to the provided list, which should be of raw NationStates reference names */
	public abstract List<String> apply(List<String> input);

	/** Should always return the name of the <code>CommuiniqueProcessingAction</code> in the code */
	@Override
	public abstract String toString();
}
