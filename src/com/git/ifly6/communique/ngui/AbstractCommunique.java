/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.ngui;

import java.io.IOException;
import java.nio.file.Path;

import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.marconi.Marconi;

/** Provides the outline of the Communique and Marconi programs. Also provides the shared {@link CommuniqueConfig} save and load
 * functionality shared between {@link Communique} and {@link Marconi}.
 * @author ifly6 */
public abstract class AbstractCommunique {
	
	/** Returns a <code>CConfig</code> object which represents the state of the program as it is here.
	 * @return a <code>CConfig</code> representing the program */
	public abstract CommuniqueConfig exportState();
	
	public abstract void importState(CommuniqueConfig config);
	
	public void save(Path savePath) throws IOException {
		// System.out.println("exportState().sentList\t" + Arrays.toString(exportState().sentList));
		CommuniqueLoader loader = new CommuniqueLoader(savePath);
		loader.save(exportState());
	}
	
	public void load(Path savePath) throws IOException {
		CommuniqueLoader loader = new CommuniqueLoader(savePath);
		this.importState(loader.load());
	}
}
