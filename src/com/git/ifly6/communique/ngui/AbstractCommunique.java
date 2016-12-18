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
package com.git.ifly6.communique.ngui;

import java.io.IOException;
import java.nio.file.Path;

import com.git.ifly6.communique.io.CConfig;
import com.git.ifly6.communique.io.CLoader;
import com.git.ifly6.marconi.Marconi;

/** Provides the outline of the Communique and Marconi programs. Also provides the shared {@link CConfig} save and load
 * functionality shared between {@link Communique} and {@link Marconi}.
 * @author ifly6 */
public abstract class AbstractCommunique {
	
	/** Returns a <code>CConfig</code> object which represents the state of the program as it is here.
	 * @return a <code>CConfig</code> representing the program */
	public abstract CConfig exportState();
	
	public abstract void importState(CConfig config);
	
	public void save(Path savePath) throws IOException {
		// System.out.println("exportState().sentList\t" + Arrays.toString(exportState().sentList));
		CLoader loader = new CLoader(savePath);
		loader.save(exportState());
	}
	
	public void load(Path savePath) throws IOException {
		CLoader loader = new CLoader(savePath);
		this.importState(loader.load());
	}
}
