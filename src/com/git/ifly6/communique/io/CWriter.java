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
