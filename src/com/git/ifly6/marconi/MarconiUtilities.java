/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.marconi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MarconiUtilities {
	
	private static final Logger LOGGER = Logger.getLogger(MarconiUtilities.class.getName());
	
	static Path lockFile = Paths.get(System.getProperty("user.dir"), "marconi.lock");
	
	private static Scanner scan = new Scanner(System.in);
	
	/** Determines whether there is another instance of Marconi which is already sending.
	 * @return */
	static boolean isFileLocked() {
		// determine whether the program is already running
		try {
			if (!Files.exists(lockFile)) {
				Files.write(lockFile, Collections.singletonList(MarconiUtilities.currentTime()));
				return false;
			} else return true;
		} catch (IOException exc) {
			LOGGER.log(Level.INFO, "Cannot determine if program is already running from lock file.", exc);
			return false;
		}
	}
	
	/** @return the current date and time in the format YYYY/MM/DD HH:MM:SS */
	public static String currentTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	/** Shorthand for the scanner creation, the posing of the question, and the getting of the response. This version of
	 * the prompt method will not return all responses in lower case.
	 * @param prompt The question posed to the user.
	 * @return */
	static String prompt(String prompt) {
		System.out.print(prompt + "\t");
		return scan.nextLine();
	}
	
	/** Sends data and requests that you sanitise it to avoid stupid errors. All responses will be in lower case. This
	 * is the only way the data can be effectively sanitised.
	 * @param prompt The question posed to the user.
	 * @param acceptableAnswers A list of valid responses.
	 * @return */
	static String prompt(String prompt, List<String> acceptableAnswers) {
		String response;
		while (true) {
			response = prompt(prompt).toLowerCase();
			if (acceptableAnswers.contains(response)) break;
			else System.out.println("Please provide an acceptable answer.");
		}
		return response;
	}
	
	/** @param prompt the question posed to the user.
	 * @return answer */
	static String promptYN(String prompt) {
		return prompt(prompt, Arrays.asList("yes", "no", "y", "n"));
	}
}
