/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.marconi;

import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.RecipientType;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Kevin */
public class MarconiLauncher {
	
	private static final Logger LOGGER = Logger.getLogger(Marconi.class.getName());
	
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

		// Get us a reasonable-looking log format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");

		if (args.length == 0) {
			LOGGER.severe("Runtime Error. Please provide a single valid Communiqué configuration file of version "
					+ Communique7Parser.version + ".");
			System.exit(0);
		}
		
		Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, "Exception in " + t + ": " + e.toString(), e);
		});
		
		CommandLineParser cliParse = new DefaultParser();
		
		try {
			
			CommandLine commandLine = cliParse.parse(COMMAND_LINE_OPTIONS, args);
			
			// Deal with options
			if (commandLine.hasOption("h")) {
				
				HelpFormatter formatter = new HelpFormatter();
				
				String fileName;
				try {
					URI u = MarconiLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI();
					fileName = new File(u).getName();
				} catch (Exception e1) {
					// Catch any and all exceptions as default to standard naming format.
					fileName = "Marconi_" + Communique7Parser.version;
				}
				
				String header = "Send telegrams on NationStates from the command line";
				String footer = "Please report issues to the NationStates nation Imperium Anglorum via telegram or to "
						+ "http://forum.nationstates.net/viewtopic.php?f=15&t=352065.";
				formatter.printHelp("java -jar " + fileName, header, COMMAND_LINE_OPTIONS, footer, true);
				System.out.println();
				return;
			}
			if (commandLine.hasOption("S")) skipChecks = true;
			if (commandLine.hasOption("R")) recruiting = true;
			if (commandLine.hasOption("v")) {
				System.out.println("Marconi version " + Communique7Parser.version + "\n"
						+ "Please visit https://github.com/iFlyCode/Communique/releases.\n");
				return; // exit
			}
			
			// Deal with the remaining arguments
			String[] fileList = commandLine.getArgs();
			if (fileList.length != 1) {

				LOGGER.severe("Please provide only one file argument to the program.\n");
				System.exit(0);
				
			} else {
				
				Path configPath = Paths.get(fileList[0]);
				initSend(configPath);
				
			}
			
		} catch (ParseException e) {
			LOGGER.severe("Please refer to the help, accessible using '-h'\n");
			e.printStackTrace();
			
		} catch (IOException e) {
			LOGGER.severe("Please provide a valid or existing file argument to the program.\n");
			e.printStackTrace();
		}
	}
	
	private static void initSend(Path configPath) throws IOException {
		
		Marconi marconi = new Marconi(skipChecks, recruiting);
		marconi.load(configPath);

		// If there is a recruit flag, set it to true
		boolean recruiting = marconi.exportState().getcRecipients().stream()
				.anyMatch(r -> r.getRecipientType() == RecipientType.FLAG && r.getName().equals("recruit"));
		
		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				LOGGER.info("Attempting to save to:" + configPath.toAbsolutePath().toString());	// save config
				marconi.save(configPath);
				if (Files.deleteIfExists(MarconiUtilities.lockFile))
					LOGGER.info("Removed file lock");	// remove file lock, if it exists
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
