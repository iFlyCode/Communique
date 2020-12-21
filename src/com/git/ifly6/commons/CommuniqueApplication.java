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

    COMMUNIQUE {
        @Override
        public String displayName() {
            return "Communiqué";
        }

        @Override
        public String ref() {
            return "communique";
        }

        @Override
        public String build() {
            return String.valueOf(Communique7Parser.BUILD);
        }

        @Override
        public String version() {
            return Communique7Parser.VERSION;
        }

    }, MARCONI {
        @Override
        public String displayName() {
            return WordUtils.capitalize(this.ref());
        }

        @Override
        public String ref() {
            return "marconi";
        }

        @Override
        public String build() {
            return COMMUNIQUE.build();
        }

        @Override
        public String version() {
            return COMMUNIQUE.version();
        }
    };

    private static final Logger LOGGER = Logger.getLogger(CommuniqueApplication.class.getName());

    public abstract String displayName();

    public abstract String ref();

    public abstract String build();

    public abstract String version();

    public static final Path APP_SUPPORT = ((Supplier<Path>) () -> { // determine where the APP_SUPPORT location is
        if (CommuniqueUtilities.IS_OS_WINDOWS)
            return Paths.get(System.getenv("LOCALAPPDATA"), "Communique");

        if (CommuniqueUtilities.IS_OS_MAC)
            return Paths.get(System.getProperty("user.home"),
                    "Library", "Application Support", "Communique");

        return Paths.get(System.getProperty("user.home"), ".communique");
    }).get();

    static {
        // try create app support directory if not existent
        try {
            Files.createDirectories(APP_SUPPORT);
        } catch (IOException e1) {
            e1.printStackTrace();
            LOGGER.warning("Cannot create directory");
        }
    }

    /** Nativises for Mac by putting name in screen menu bar. */
    public static void nativiseMac(CommuniqueApplication app) {
        if (CommuniqueUtilities.IS_OS_MAC) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                    generateName(app, false));
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
                            CommuniqueUtilities.getTime()));
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

        // get jar name
        return getJARName(app);
    }

    /** Sets system look and feel; if not available, sets Nimbus. */
    public static void setLAF() {
        // Set our look and feel
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

    /**
     * Generates display name; includes build variable if {@code includeBuild}.
     * @param app          to generate for
     * @param includeBuild whether to include build name
     * @return names like {@code Communiqué 2.5 (build 12)}
     * @since version 3.0 (build 13)
     */
    public static String generateName(CommuniqueApplication app, boolean includeBuild) {
        return includeBuild
                ? MessageFormat.format("{0} {1} (build {2})", app.displayName(), app.version(), app.build())
                : MessageFormat.format("{0} {1}", app.displayName(), app.version());
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

    /**
     * Compresses logs present in the application support {@code log} sub-folder if they are older than a day with
     * GZIP.
     */
    public static void compressLogs() {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(APP_SUPPORT.resolve("log"), "*.log");
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
                    break; // better safe than sorry
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
