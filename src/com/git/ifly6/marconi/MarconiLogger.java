/*
 * Copyright (c) 2015 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.marconi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.git.ifly6.javatelegram.JTelegramLogger;

public class MarconiLogger implements JTelegramLogger {

	Scanner scan = new Scanner(System.in);

	/**
	 * @param output is the <code>String</code> to be logged
	 */
	@Override public void log(String output) {
		System.out.println("[" + currentTime() + "] " + output);
	}

	/**
	 * @return the current date and time in the format YYYY/MM/DD HH:MM:SS
	 */
	private String currentTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String finalDate = dateFormat.format(date);
		return finalDate;
	}

	/**
	 * Shorthand for the scanner creation, the posing of the question, and the getting of the response. This version of
	 * the prompt method will not return all responses in lower case.
	 *
	 * @param prompt The question posed to the user.
	 * @return
	 */
	public String prompt(String prompt) {

		System.out.print(prompt + "\t");
		String response = scan.nextLine();

		return response;
	}

	/**
	 * Sends data and requests that you sanitise it to avoid stupid errors. All responses will be in lower case. This is
	 * the only way the data can be effectively sanitised.
	 *
	 * @param prompt The question posed to the user.
	 * @param conditions A list of valid responses.
	 * @return
	 */
	public String prompt(String prompt, String[] conditions) {
		String response = "";
		boolean fine = false;

		while (!fine) {
			response = this.prompt(prompt).toLowerCase();

			for (String element : conditions) {
				if (element.equals(response)) {
					fine = true;
				}
			}

			if (!fine) {
				System.out.println("Please provide an acceptable answer.");
			}
		}

		return response;
	}

	/**
	 * @param prompt the question posed to the user.
	 * @param trueFalse this boolean leads to the setting of valid responses to ones appropriate for a yes or no answer.
	 * @return
	 */
	public String prompt(String prompt, boolean trueFalse) {
		return prompt(prompt, new String[] { "yes", "no", "y", "n" });
	}
}
