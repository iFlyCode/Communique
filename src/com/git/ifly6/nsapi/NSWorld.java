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

package com.git.ifly6.nsapi;

import com.git.ifly6.nsapi.telegram.JTelegramException;
import com.jcabi.xml.XMLDocument;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/* There is only one World, so this is going to be static. */
public class NSWorld {

    private NSWorld() {
    }

    /**
     * Queries the NationStates API for a listing of 50 new nations.
     * @return {@code List<String>} with the recipients inside, in {@code ref} form
     * @throws JTelegramException in case the NationStates API is unreachable for some reason
     */
    public static List<String> getNew() throws JTelegramException {
        try {
            final NSConnection connection = new NSConnection(NSConnection.API_PREFIX + "q=newnations");
            final String response = connection.connect().getResponse();
            final String newNations = new XMLDocument(response).xpath("/WORLD/NEWNATIONS/text()").get(0);
            return arrayRef(newNations.split(","));
        } catch (IOException e) {
            throw new JTelegramException("Failed to get new nations", e);
        }
    }

    /**
     * Queries the NationStates API for a listing of all nations in the game.
     * @return {@code List<String>} of every NS nation in {@code ref} form
     * @throws IOException from {@link java.net.URLConnection}
     */
    public static List<String> getAllNations() throws IOException {
        String x = new NSConnection(NSConnection.API_PREFIX + "q=nations").getResponse();
        return arrayRef(new XMLDocument(x).xpath("/WORLD/NATIONS/text()").get(0).split(","));
    }

    /**
     * Queries the NationStates API for a listing of every single World Assembly member.
     * @return {@code List<String>} with the reference name of every World Assembly member
     * @throws IOException from {@link java.net.URLConnection}
     */
    public static List<String> getWAMembers() throws IOException {
        String x = new NSConnection(NSConnection.API_PREFIX + "wa=1&q=members").getResponse();
        return arrayRef(new XMLDocument(x).xpath("/WA/MEMBERS/text()").get(0).split(","));
    }

    /**
     * Queries the NationStates API for a listing of all World Assembly delegates.
     * @return {@code List<String>} with the reference name of every delegate
     * @throws IOException from {@link java.net.URLConnection}
     */
    public static List<String> getDelegates() throws IOException {
        String x = new NSConnection(NSConnection.API_PREFIX + "wa=1&q=delegates").getResponse();
        return arrayRef(new XMLDocument(x).xpath("/WA/DELEGATES/text()").get(0).split(","));
    }

    /**
     * Queries the NS API for list of regions declaring the provided tag.
     * @param regionTag to query
     * @return {@code List<String>} of regions by names
     * @throws IOException          from {@link java.net.URLConnection}
     * @throws NSNoSuchTagException if tag specified does not exist
     */
    public static List<String> getRegionTag(String regionTag) throws IOException, NSNoSuchTagException {
        // https://www.nationstates.net/cgi-bin/api.cgi?q=regionsbytag;tags=-medium,class,-minuscule
        try {
            String content = new NSConnection(
                    NSConnection.API_PREFIX
                            + "q=regionsbytag;tags="
                            + regionTag.trim()
            ).getResponse();
            return arrayRef(new XMLDocument(content).xpath("/WORLD/REGIONS/text()").get(0).split(","));

        } catch (IndexOutOfBoundsException e) {
            throw new NSNoSuchTagException(String.format("tag <%s> does not exist", regionTag), e);
        }
    }

    /**
     * Wraps {@link ApiUtils#ref} for {@code String[]}.
     */
    private static List<String> arrayRef(String[] input) {
        return ApiUtils.ref(Arrays.asList(input));
    }

    /** Thrown if the specified region tag does not exist.
     * @since version 3.0 (build 13)*/
    public static class NSNoSuchTagException extends NSException {
        public NSNoSuchTagException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}