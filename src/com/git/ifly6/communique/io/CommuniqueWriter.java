/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

class CommuniqueWriter {
	
	private Path path;
	private CommuniqueConfig config;
	
	/** Creates <code>CWriter</code> pointing to some path with some loaded configuration data.
	 * @param path on which to write
	 * @param config data */
	CommuniqueWriter(Path path, CommuniqueConfig config) {
		this.path = path;
		this.config = config;
	}
	
	/** Writes the configuration data to the path specified in the constructor.
	 * @throws IOException if there is an error in writing the file */
	void write() throws IOException {

		// Data-checking on writing
		config.checkData();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String response = gson.toJson(config);
		
		Files.write(path, Arrays.asList(response.split("\n")));
		
	}
	
}
