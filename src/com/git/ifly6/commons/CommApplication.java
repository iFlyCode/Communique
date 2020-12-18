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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Enumerates Communique-based applications.
 * @since version 3.0 (build 13)
 */
public enum CommApplication {

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

    public abstract String displayName();

    public abstract String ref();

    public abstract String build();

    public abstract String version();

    /**
     * Generates display name; includes build variable if {@code includeBuild}.
     * @param app          to generate for
     * @param includeBuild whether to include build name
     * @return names like {@code Communiqué 2.5 (build 12)}
     * @since version 3.0 (build 13)
     */
    public static String generateName(CommApplication app, boolean includeBuild) {
        return includeBuild
                ? MessageFormat.format("{0} {1} (build {2})", app.displayName(), app.version(), app.build())
                : MessageFormat.format("{0} {1}", app.displayName(), app.version());
    }

    /**
     * Handles logging format, setting up default exception handler, and gets name of the application JAR.
     * @param app starting up
     * @return name of the application's JAR file
     * @since version 3.0 (build 13)
     */
    public static String startUp(CommApplication app) {
        // better log format
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
        try {
            // Add in a logging file handler
            Path logLocation = Paths.get(String.format("%s-session-%s.log",
                    app.ref(),
                    CommuniqueUtilities.getTime()));
            FileHandler handler = new FileHandler(logLocation.toString());
            handler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(handler); // empty string gets root logger

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        // set up default exception handler
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            Logger.getGlobal().log(Level.SEVERE,
                    String.format("Exception in %s: %s", t, e.toString()), e);
            e.printStackTrace();
        });

        // get jar name
        return getJARName(app);
    }

    /** @return name of the starting executable. */
    private static String getJARName(CommApplication app) {
        try {
            return new File(Marconi.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getName();
        } catch (URISyntaxException e) {
            return String.format("%s_%d.jar",
                    WordUtils.capitalize(app.ref()),
                    Communique7Parser.BUILD); // default to standard naming format.
        }
    }

}
