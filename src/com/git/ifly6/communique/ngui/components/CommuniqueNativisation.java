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

package com.git.ifly6.communique.ngui.components;

import com.git.ifly6.communique.CommuniqueUtilities;

import javax.swing.JFileChooser;
import java.awt.FileDialog;
import java.awt.Frame;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static com.git.ifly6.communique.ngui.components.CommuniqueLAF.appSupport;

public class CommuniqueNativisation {

    private static final java.util.logging.Logger LOGGER = Logger.getLogger(CommuniqueNativisation.class.getName());

    /**
     * Creates an file chooser (in an OS specific manner) and shows it to the user.
     * @param parent <code>Frame</code> to show the chooser from
     * @param type   either <code>FileDialog.SAVE</code> or <code>FileDialog.LOAD</code>
     * @return the <code>Path</code> selected by the user
     */
    public static Path showFileChooser(Frame parent, int type) {

        Path savePath;

        // Due to a problem in Windows and the AWT FileDialog, this will show a JFileChooser on Windows systems.
        if (CommuniqueUtilities.IS_OS_MAC) {

            FileDialog fDialog = new FileDialog(parent, "Choose file...", type);
            if (type == FileDialog.SAVE) fDialog.setTitle("Save session as...");
            fDialog.setDirectory(appSupport.toFile().toString());
            fDialog.setVisible(true);

            String fileName = fDialog.getFile();
            if (fileName == null) {
                LOGGER.info("User cancelled file file dialog");
                return null;

            } else savePath = Paths.get(fDialog.getDirectory() == null
                    ? ""
                    : fDialog.getDirectory()).resolve(fDialog.getFile());

        } else {

            JFileChooser fChooser = new JFileChooser(appSupport.toFile());
            fChooser.setDialogTitle("Choose file...");

            int returnVal;
            // returnVal = (type == FileDialog.SAVE) ? fChooser.showSaveDialog(parent) :
            // fChooser.showOpenDialog(parent);
            if (type == FileDialog.SAVE) {
                fChooser.setDialogTitle("Save session as...");
                returnVal = fChooser.showSaveDialog(parent);
            } else returnVal = fChooser.showOpenDialog(parent);
            fChooser.setVisible(true);

            if (returnVal == JFileChooser.APPROVE_OPTION) savePath = fChooser.getSelectedFile().toPath();
            else return null;

        }

        // Make it end in txt if saving
        if (type == FileDialog.SAVE && !savePath.toString().endsWith("txt")) {
            LOGGER.info("Append txt to savePath");
            savePath = savePath.resolveSibling(savePath.getFileName() + ".txt");
        }

        LOGGER.info(String.format("%s file at %s", type == FileDialog.SAVE ? "Saved" : "Loaded",
                savePath.toAbsolutePath().toString()));
        return savePath;
    }
}
