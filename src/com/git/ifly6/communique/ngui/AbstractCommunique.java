/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
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
