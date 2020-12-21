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

package com.git.ifly6.nsapi.telegram;

import com.git.ifly6.nsapi.NSConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.CLIENT_NOT_REGISTERED;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.NO_SUCH_TELEGRAM;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.QUEUED;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.RATE_LIMIT_EXCEEDED;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.REGION_MISMATCH;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.SECRET_KEY_MISMATCH;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.UNKNOWN_ERROR;

/**
 * {@code JTelegramConnection} connects to NationStates telegram API. This is done by creating a {@link
 * HttpURLConnection} to the appropriately generated URL with provided keys. On connection, also parses response to
 * appropriate response codes.
 */
public class JTelegramConnection {

    private static final Logger LOGGER = Logger.getLogger(JTelegramConnection.class.getName());

//    public static final int QUEUED = 0;
//    public static final int UNKNOWN_ERROR = 1;
//    public static final int REGION_MISMATCH = 2;
//    public static final int CLIENT_NOT_REGISTERED = 3;
//    public static final int RATE_LIMIT_EXCEEDED = 4;
//    public static final int SECRET_KEY_MISMATCH = 5;
//    public static final int NO_SUCH_TELEGRAM = 6;

    private HttpURLConnection apiConnection;

    /**
     * Creates a telegram connection. If {@code doNothing} is true, does nothing.
     * @param keys      to use to send
     * @param recipient to send to
     * @param doNothing if true, does nothing
     * @throws IOException if {@link MalformedURLException} or if cannot connect to Internet
     */
    public JTelegramConnection(JTelegramKeys keys, String recipient, boolean doNothing) throws IOException {
        String urlString = MessageFormat.format("{0}a=sendTG&client={1}&key={2}&tgid={3}&to={4}",
                NSConnection.API_PREFIX,
                keys.getClientKey(),
                keys.getSecretKey(),
                keys.getTelegramId(),
                recipient);
        URL tgURL = new URL(urlString);
        apiConnection = (HttpURLConnection) tgURL.openConnection(); // always should be true
        apiConnection.setRequestProperty("User-Agent",
                MessageFormat.format(
                        "NationStates JavaTelegram (maintained by Imperium Anglorum, used by {0})",
                        keys.getClientKey()));

        // if doing nothing, do nothing; log everything
        if (!doNothing) {
            apiConnection.connect();
            LOGGER.fine(String.format("Attempting to send TG <%s> to <%s>",
                    keys.getTelegramId(), recipient));

        } else
            LOGGER.fine(String.format("Dry run; did nothing for TG <%s> to <%s>",
                    keys.getTelegramId(), recipient));
    }

    /**
     * Creates and establishes a telegram connection. Automatically connects when instantiated.
     * @param keys      {@link JTelegramKeys} containing all of the relevant keys
     * @param recipient is a <code>String</code> which contains name of the recipient in NationStates back-end
     * @throws IOException if there is a problem in connecting to the API
     */
    public JTelegramConnection(JTelegramKeys keys, String recipient) throws IOException {
        this(keys, recipient, false);
    }

    /**
     * Verifies whether the telegram was queued or not.
     * <p>
     * There are error codes. If the error code is 0, there is no problem. If the code is 1, then it failed to queue for
     * an unknown reason. If the code is 2, it failed to code due to a region mismatch between the telegram and the
     * client key.
     * </p>
     * @return an <code>int</code> which contains an error code.
     * @throws IOException if error in queuing the telegram
     */
    public ResponseCode verify() throws IOException {
        BufferedReader webReader = apiConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST
                ? new BufferedReader(new InputStreamReader(apiConnection.getInputStream()))
                : new BufferedReader(new InputStreamReader(apiConnection.getErrorStream()));
        String response = webReader.lines().collect(Collectors.joining("\n"));
        webReader.close();

        if (response.startsWith("queued")) return QUEUED;
        if (response.contains("ratelimit exceeded")) return RATE_LIMIT_EXCEEDED;
        if (response.contains("Region mismatch between Telegram and Client API Key")) return REGION_MISMATCH;
        if (response.contains("Client Not Registered For API")) return CLIENT_NOT_REGISTERED;
        if (response.contains("Incorrect Secret Key")) return SECRET_KEY_MISMATCH;
        if (response.contains("No Such API Telegram Template")) return NO_SUCH_TELEGRAM;

        // else, print and return
        LOGGER.warning(String.format("Unknown error at code (%d):\n%s", apiConnection.getResponseCode(), response));
        return UNKNOWN_ERROR;
    }

    public enum ResponseCode {
        QUEUED, UNKNOWN_ERROR, REGION_MISMATCH, CLIENT_NOT_REGISTERED, RATE_LIMIT_EXCEEDED,
        SECRET_KEY_MISMATCH, NO_SUCH_TELEGRAM
    }
}
