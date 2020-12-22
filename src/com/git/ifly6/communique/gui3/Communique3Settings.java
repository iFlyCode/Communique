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

package com.git.ifly6.communique.gui3;

import com.git.ifly6.communique.io.CommuniqueConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.javatuples.Pair;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;

import static com.git.ifly6.commons.CommuniqueApplication.APP_SUPPORT;

/**
 * Holds settings information for Communique3. This is an initialisation class.
 * @since version 3.0 (build 13)
 */
@SuppressWarnings("unused")
public class Communique3Settings {
    private static final Path SETTINGS_PATH = APP_SUPPORT.resolve("client_settings.properties");

    public Level loggingLevel; // these are technically unused; but used by reflection

    public Communique3Settings(Level loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    public void save() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Pair<Boolean, String> returnedInfo = this.noNulls();

        boolean noNulls = returnedInfo.getValue0();
        String nullName = returnedInfo.getValue1();

        if (noNulls) { // if have no nulls!
            String response = gson.toJson(this);
            Files.write(SETTINGS_PATH, Arrays.asList(response.split("\n")));

        } else throw new Communique3SettingsException(String.format("Value %s is null!",
                nullName));
    }

    /** Loads settings from file if present. Puts defaults if not. */
    public static Communique3Settings load() {
        return load(null);
    }

    /**
     * Loads settings from file if present, puts defaults if not, but substitutes client key from {@link
     * CommuniqueConfig}.
     * @param config to load from
     * @return settings
     */
    public static Communique3Settings load(CommuniqueConfig config) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(Files.newBufferedReader(SETTINGS_PATH), Communique3Settings.class);
        } catch (IOException e) {
            return new Communique3Settings(Level.INFO);
        }
    }

    /**
     * @return tuple of {@code false} with the name of the field; otherwise if no nulls true and null
     */
    private Pair<Boolean, String> noNulls() {
        Field[] fields = this.getClass().getFields();
        try {
            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers())
                        || !Modifier.isPublic(f.getModifiers()))
                    continue;
                if (f.get(this) == null)
                    return new Pair<>(false, f.getName());
            }
        } catch (IllegalAccessException e) {
            throw new Communique3MonitorReflections.CommReflectException("Cannot check for null fields in settings!");
        }

        return new Pair<>(true, null);
    }

    /** Thrown if there is a validity issue with Communique3's settings. */
    public static class Communique3SettingsException extends RuntimeException {
        public Communique3SettingsException(String message) { super(message); }
    }
}
