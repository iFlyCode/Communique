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

import com.google.common.util.concurrent.RateLimiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class regulates how the program connects to the NationStates API.
 * <p>
 * It only operates for one program at a time. If you run multiple instances, it will go over the API rate limit and
 * will result in your computer getting locked out from the API for 15 minutes. The program uses a
 * <code>synchronized</code> block to force that only one connection exists at one time.
 * </p>
 */
public class NSConnection {

    private static final Logger LOGGER = Logger.getLogger(NSConnection.class.getName());

    private static final double PERMITS_PER_SECOND = 50 / (double) 30; // 50 requests per 30 seconds
    private static final RateLimiter limiter = RateLimiter.create(PERMITS_PER_SECOND);

    /** API delay in milliseconds. */
    public final static int WAIT_TIME = 610;

    /** NationStates API call prefix, {@code https://www.nationstates.net/cgi-bin/api.cgi?}. */
    public static final String API_PREFIX = "https://www.nationstates.net/cgi-bin/api.cgi?";

    /** NationStates API query prefix, {@code &q=}. */
    public static final String QUERY_PREFIX = "&q=";

    private URL url;
    private String xml_raw;
    private boolean hasConnected;

    private Map<String, String> entries;

    /**
     * Creates an unconnected {@code NSConnection} to query the specified URL.
     * @param urlString to query
     * @throws NSIOException if {@code urlString} is invalid URL
     */
    public NSConnection(String urlString) {
        try {
            this.url = new URL(urlString);
            LOGGER.finest(String.format("Connecting to %s", urlString));
        } catch (MalformedURLException e) {
            throw new NSIOException(String.format("Input URL <%s> malformed", urlString), e);
        }
    }

    /**
     * Connects instantiated {@code NSConnection}.
     * @return this
     * @throws IOException   if connection fails
     * @throws NSIOException if rate limit exceeded
     * @throws NSException   if queried element does not exist or no data can be got
     */
    public NSConnection connect() throws IOException {
        // Implement the rate limit
        double secondsWaited = limiter.acquire();
        LOGGER.finest(String.format("NSConnection rate limit -> waited %.3f seconds", secondsWaited));

        // Create connection, add request properties
        HttpURLConnection apiConnection = (HttpURLConnection) url.openConnection();
        apiConnection.addRequestProperty("User-Agent",
                "NS API request; maintained by Imperium Anglorum, email: cyrilparsons.london@gmail.com; see IP");
        if (entries != null && !entries.isEmpty())
            for (Map.Entry<String, String> entry : entries.entrySet())
                apiConnection.addRequestProperty(entry.getKey(), entry.getValue());

        apiConnection.connect(); // do connection
        hasConnected = true; // update API

        if (apiConnection.getResponseCode() == 200) { // if normal
            BufferedReader reader = new BufferedReader(new InputStreamReader(apiConnection.getInputStream()));
            xml_raw = reader.lines().collect(Collectors.joining("\n"));
            reader.close();

        } else { // otherwise, read error stream
            BufferedReader reader = new BufferedReader(new InputStreamReader(apiConnection.getErrorStream()));
            xml_raw = reader.lines().collect(Collectors.joining("\n"));
            reader.close();

            if (apiConnection.getResponseCode() == 429)
                throw new NSIOException("Api ratelimit exceeded");

            if (xml_raw.contains("Unknown nation")
                    || apiConnection.getResponseCode() == 404)
                throw new NSException(String.format("No results for url %s", url.toString()));

            throw new NSException(String.format("Cannot get data from the API at url %s"
                            + "\nHTTP response code %d",
                    url.toString(),
                    apiConnection.getResponseCode()));
        }

        return this;
    }

    /**
     * Sets HTML POST headers
     * @param entries of keys and values to pass
     * @return self, after setting
     */
    public NSConnection setHeaders(Map<String, String> entries) {
        this.entries = entries;
        return this;
    }

    /**
     * Delivers response as {@code String}. If {@code NSConnection} has already connected, ie is spent, returns result
     * thereof. If not yet connected, invokes {@link #connect()} automatically.
     * @return response
     * @throws IOException if connection fails
     */
    public String getResponse() throws IOException {
        // if it has connected, get response. otherwise, connect and return response
        return hasConnected ? xml_raw : connect().xml_raw;
    }
}