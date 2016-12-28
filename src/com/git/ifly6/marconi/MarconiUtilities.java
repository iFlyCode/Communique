/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.marconi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class MarconiUtilities {
	
	private static Scanner scan = new Scanner(System.in);
	
	/** @return the current date and time in the format YYYY/MM/DD HH:MM:SS */
	public static String currentTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String finalDate = dateFormat.format(date);
		return finalDate;
	}
	
	/** Shorthand for the scanner creation, the posing of the question, and the getting of the response. This version of
	 * the prompt method will not return all responses in lower case.
	 *
	 * @param prompt The question posed to the user.
	 * @return */
	public static String prompt(String prompt) {
		
		System.out.print(prompt + "\t");
		String response = scan.nextLine();
		
		return response;
	}
	
	/** Sends data and requests that you sanitise it to avoid stupid errors. All responses will be in lower case. This
	 * is the only way the data can be effectively sanitised.
	 *
	 * @param prompt The question posed to the user.
	 * @param acceptableAnswers A list of valid responses.
	 * @return */
	public static String prompt(String prompt, List<String> acceptableAnswers) {
		String response = "";
		boolean kosher = false;
		
		while (!kosher) {
			response = prompt(prompt).toLowerCase();
			if (acceptableAnswers.contains(response)) {
				kosher = true;
				break;
			}
			
			if (!kosher) {
				System.out.println("Please provide an acceptable answer.");
			}
		}
		
		return response;
	}
	
	/** @param prompt the question posed to the user.
	 * @param trueFalse this boolean leads to the setting of valid responses to ones appropriate for a yes or no answer.
	 * @return */
	public static String promptYN(String prompt) {
		return prompt(prompt, Arrays.asList("yes", "no", "y", "n"));
	}
}
