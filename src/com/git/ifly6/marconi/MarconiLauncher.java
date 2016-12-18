/* Copyright (c) 2016 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.git.ifly6.marconi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.git.ifly6.communique.data.CommuniqueParser;

/** @author Kevin */
public class MarconiLauncher {

	private static final Logger log = Logger.getLogger(Marconi.class.getName());

	// Deal with command line options
	public static final Options COMMAND_LINE_OPTIONS;

	private static boolean skipChecks = false;
	private static boolean recruiting = false;

	static {
		Options options = new Options();
		options.addOption("h", "help", false, "Displays this message");
		options.addOption("S", "skip", false,
				"Skips all checks for confirmation such that the configuration immediately executes");
		options.addOption("R", false, "Uses input data as configuration to call a recruiter that sends infinitely");
		options.addOption("v", "version", false, "Prints version");

		COMMAND_LINE_OPTIONS = options;
	}

	public static void main(String[] args) {

		if (args.length == 0) {
			System.err.println("Runtime Error. Please provide a single valid Communiqué configuration file of version "
					+ CommuniqueParser.version + ".");
			System.exit(0);
		}

		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			e.printStackTrace();
			log.log(Level.SEVERE, "Exception in " + t + ": " + e.toString(), e);
		});

		CommandLineParser cliParse = new DefaultParser();

		try {

			CommandLine commandLine = cliParse.parse(COMMAND_LINE_OPTIONS, args);

			// Deal with options
			if (commandLine.hasOption("h")) {

				HelpFormatter formatter = new HelpFormatter();

				String fileName;
				try {
					fileName = new File(
							MarconiLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
									.getName();
				} catch (Exception e1) {
					// Catch any and all exceptions as default to standard naming format.
					fileName = "Marconi_" + CommuniqueParser.version;
				}

				String header = "Send telegrams on NationStates from the command line";
				String footer = "Please report issues to the NationStates nation Imperium Anglorum via telegram or to "
						+ "http://forum.nationstates.net/viewtopic.php?f=15&t=352065.";
				formatter.printHelp("java -jar " + fileName, header, COMMAND_LINE_OPTIONS, footer, true);
				System.out.println();
				return;
			}
			if (commandLine.hasOption("S")) {
				skipChecks = true;
			}
			if (commandLine.hasOption("R")) {
				recruiting = true;
			}
			if (commandLine.hasOption("v")) {
				System.out.println("Marconi version " + CommuniqueParser.version + "\n"
						+ "Please visit https://github.com/iFlyCode/Communique/releases.\n");
				return;
			}

			// Deal with the remaining arguments
			String[] fileList = commandLine.getArgs();
			if (fileList.length != 1) {

				System.err.println("Please provide only one file argument to the program.\n");
				System.exit(0);

			} else {

				Path configPath = Paths.get(fileList[0]);
				initSend(configPath);

			}

		} catch (ParseException e) {
			System.err.println("Please refer to the help, accessible using '-h'\n");
			e.printStackTrace();

		} catch (IOException e) {
			System.err.println("Please provide a valid or existing file argument to the program.\n");
			e.printStackTrace();
		}
	}

	private static void initSend(Path configPath) throws IOException {

		Marconi marconi = new Marconi(skipChecks, recruiting);
		marconi.load(configPath);

		// Accept text commands to recruit...
		String[] recipients = marconi.exportState().recipients;
		for (String element : recipients) {
			if (element.startsWith("flag:recruit")) {
				recruiting = true;
				break;
			}
		}

		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				System.err.println("Attempting to save to:" + configPath.toAbsolutePath().toString());
				marconi.save(configPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));

		if (recruiting) {

			MarconiRecruiter recruiter = new MarconiRecruiter(marconi);
			recruiter.setWithCConfig(marconi.exportState());
			recruiter.send();

			// Indefinite ending point, so use ShutdownHook to save

		} else {

			marconi.send();
			marconi.save(configPath);

		}
	}
}
