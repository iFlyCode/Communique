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
import com.git.ifly6.communique.data.Communique7Parser;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class CommuniqueLAF {

    private static final Logger LOGGER = Logger.getLogger(CommuniqueLAF.class.getName());
    public static Path appSupport;
    public static FileHandler loggerFileHandler = null; // Save logs to file, if null... uh stuff

    static {
        // Do this static initialisation block when LAF is called
        // Find or create the application support directory
        if (CommuniqueUtilities.IS_OS_WINDOWS) appSupport = Paths.get(System.getenv("LOCALAPPDATA"), "Communique");
        else if (CommuniqueUtilities.IS_OS_MAC) {
            appSupport = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Communique");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Communiqué " + Communique7Parser.version);

        } else appSupport = Paths.get(System.getProperty("user.dir"), "config");

        // Create the application support directory
        try {
            Files.createDirectories(appSupport);
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.warning("Cannot create directory");
        }

        // Get us a reasonable-looking log format
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");

        // Make sure we can also log to file, apply this to the root logger
        try {
            Path logFile = appSupport
                    .resolve("log")
                    .resolve(String.format("communique-session-%s.log", CommuniqueUtilities.getTime()));

            Files.createDirectories(logFile.getParent()); // make directory
            loggerFileHandler = new FileHandler(logFile.toString());
            loggerFileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(loggerFileHandler);

        } catch (SecurityException | IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void setLAF() {
        // Set our look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException lfE) {
            try {
                UIManager.setLookAndFeel(
                        Arrays.stream(UIManager.getInstalledLookAndFeels())
                                .filter(laf -> laf.getName().equals("Nimbus"))
                                .findFirst()
                                .orElseThrow(ClassNotFoundException::new)
                                .getClassName()
                );
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e) {
                LOGGER.severe("Cannot initialise? Cannot find basic Nimbus look and feel.");
                e.printStackTrace();
            }
            lfE.printStackTrace();
        }
    }

    /**
     * Compresses logs present in the application support <code>log</code> sub-folder if they are older than a day. Uses
     * G-ZIP compression. Should be called on execution.
     */
    public static void compressLogs() {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(appSupport.resolve("log"), "*.log");
            final Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
            for (Path p : stream) {
                try {
                    Instant fileModified = Files.getLastModifiedTime(p).toInstant();
                    boolean earlier = fileModified.compareTo(yesterday) < 0;
                    if (earlier) {
                        // write new compressed file
                        Path newPath = p.resolveSibling(p.getFileName() + ".gz");
                        GZIPOutputStream zipStream = new GZIPOutputStream(Files.newOutputStream(newPath, CREATE_NEW));
                        zipStream.write(Files.readAllBytes(p));
                        zipStream.close();

                        // delete old file
                        Files.deleteIfExists(p);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println(String.format("Failed to compress %s", p));
                    break; // better safe than sorry
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
