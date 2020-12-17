/*
 * Copyright (c) 2020 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.communique.ngui;

import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.marconi.Marconi;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Provides the shared {@link CommuniqueConfig} save and load functionality shared between {@link Communique} and {@link
 * Marconi}.
 * @author ifly6
 */
public abstract class AbstractCommunique {

    /** @return {@link CommuniqueConfig} representing current program state. */
    public abstract CommuniqueConfig exportState();

    /** Imports {@link CommuniqueConfig} representing overwriting program state. */
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
