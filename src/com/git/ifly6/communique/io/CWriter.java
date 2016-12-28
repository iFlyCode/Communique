/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class CWriter {
	
	private Path path;
	private CConfig config;
	
	/** Creates <code>CWriter</code> pointing to some path with some loaded configuration data.
	 * @param path on which to write
	 * @param config data */
	public CWriter(Path path, CConfig config) {
		this.path = path;
		this.config = config;
	}
	
	/** Writes the configuration data to the path specified in the constructor.
	 * @throws IOException if there is an error in writing the file */
	public void write() throws IOException {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String response = gson.toJson(config);
		
		Files.write(path, Arrays.asList(response.split("\n")));
		
	}
	
}
