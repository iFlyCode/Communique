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

package com.git.ifly6.commons;

import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.marconi.Marconi;
import org.apache.commons.text.WordUtils;

import javax.swing.UIManager;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

/**
 * Enumerates Communique-based applications.
 * @since version 3.0 (build 13)
 */
public enum CommuniqueApplication {

    COMMUNIQUE("Communiqué", "communique"),
    MARCONI("Marconi", "marconi");

    private static final Logger LOGGER = Logger.getLogger(CommuniqueApplication.class.getName());
    private String displayName;
    private String ref;

    CommuniqueApplication(String displayName, String ref) {
        this.displayName = displayName;
        this.ref = ref;
    }

    /** @return {@link Communique7Parser#BUILD} */
    public String build() {
        return String.valueOf(Communique7Parser.BUILD);
    }

    /**
     * Generates display name; includes build variable if {@code includeBuild}.
     * @param includeBuild whether to include build name
     * @return names like {@code Communiqué 2.5 (build 12)}
     * @since version 3.0 (build 13)
     */
    public String generateName(boolean includeBuild) {
        return includeBuild
                ? MessageFormat.format("{0} {1} (build {2})", displayName(), version(), build())
                : MessageFormat.format("{0} {1}", displayName(), version());
    }

    /** @return display name */
    public String displayName() {
        return this.displayName;
    }

    /** @return Internal reference name */
    public String ref() {
        return this.ref;
    }

    /** @return {@link Communique7Parser#VERSION} string */
    public String version() {
        return Communique7Parser.VERSION;
    }

    public static final Path APP_SUPPORT = ((Supplier<Path>) () -> { // determine where the APP_SUPPORT location is
        if (CommuniqueUtilities.IS_OS_WINDOWS)
            return Paths.get(System.getenv("LOCALAPPDATA"), "Communique");

        if (CommuniqueUtilities.IS_OS_MAC)
            return Paths.get(System.getProperty("user.home"),
                    "Library", "Application Support", "Communique");

        return Paths.get(System.getProperty("user.home"), ".communique");
    }).get();

    static {
        // try get or create app support directory if not existent
        try {
            Files.createDirectories(APP_SUPPORT);
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.warning("Cannot get or create directory");
        }
    }

    /** Nativises for Mac by putting name in screen menu bar. */
    public static void nativise(CommuniqueApplication app) {
        if (CommuniqueUtilities.IS_OS_MAC) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", app.generateName(false));
        }
    }

    /**
     * Handles logging format, setting up default exception handler, and gets name of the application JAR.
     * @param app starting up
     * @return name of the application's JAR file
     * @since version 3.0 (build 13)
     */
    public static String setupLogger(CommuniqueApplication app) {
        // better log format
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");

        try {
            // Add in a logging file handler
            Path logLocation = APP_SUPPORT
                    .resolve("log")
                    .resolve(String.format("%s-session-%s.log",
                            app.ref(),
                            CommuniqueUtilities.getWindowsSafeDate()));
            Files.createDirectories(logLocation.getParent()); // make sure logging folder exists
            FileHandler handler = new FileHandler(logLocation.toString());
            handler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(handler); // empty string gets root logger

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        // set up default exception handler with root logger
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            Logger.getLogger("").log(Level.SEVERE, String.format("Exception in %s: %s", t, e.toString()), e);
            e.printStackTrace();
        });

        // log the build
        LOGGER.info(String.format("Started up %s", app.generateName(true)));

        // get jar name
        return getJARName(app);
    }

    /**
     * Sets logging level for root logger to provided level
     * @param level to set
     */
    public static void setLogLevel(Level level) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(level);
        for (Handler handler : rootLogger.getHandlers())
            handler.setLevel(level);
    }

    /** Sets system look and feel; if not available, sets Nimbus. */
    public static void setLAF() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception systemException) {
            try {
                UIManager.setLookAndFeel(
                        Arrays.stream(UIManager.getInstalledLookAndFeels())
                                .filter(laf -> laf.getName().equals("Nimbus"))
                                .findFirst()
                                .orElseThrow(ClassNotFoundException::new)
                                .getClassName()
                );
            } catch (Exception e) {
                LOGGER.severe("Cannot find basic Nimbus look and feel");
                e.printStackTrace();
                systemException.printStackTrace();
            }
        }
    }

    /** @return name of the starting executable. */
    private static String getJARName(CommuniqueApplication app) {
        try {
            return new File(Marconi.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getName();
        } catch (URISyntaxException e) {
            return String.format("%s_%d.jar",
                    WordUtils.capitalize(app.ref()),
                    Communique7Parser.BUILD); // default to standard naming format.
        }
    }

    /** Compresses logs in the application support {@code log} sub-folder if they are older than a day. */
    public static void compressLogs() {
        try {
            final Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
            DirectoryStream<Path> stream = Files.newDirectoryStream(APP_SUPPORT.resolve("log"), "*.log");
            for (Path p : stream) {
                Instant fileModified = Files.getLastModifiedTime(p).toInstant();
                boolean earlier = fileModified.compareTo(yesterday) < 0;
                if (earlier) {
                    // write new compressed file
                    Path newPath = p.resolveSibling(p.getFileName() + ".gz");
                    GZIPOutputStream zipStream = new GZIPOutputStream(Files.newOutputStream(newPath, CREATE_NEW));
                    zipStream.write(Files.readAllBytes(p));
                    zipStream.close();
                    Files.deleteIfExists(p); // delete old file if present
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Encountered exception in compression task", e);
        }
    }

}
