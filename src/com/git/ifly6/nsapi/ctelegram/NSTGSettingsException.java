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

package com.git.ifly6.nsapi.ctelegram;

import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.telegram.JTelegramConnection;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;

public class NSTGSettingsException extends NSIOException {
    public NSTGSettingsException(String message) {
        super(message);
    }

    public static NSTGSettingsException createException(JTelegramKeys keys, int errorCode) {
        switch (errorCode) {
            case JTelegramConnection.REGION_MISMATCH:
                return new NSTGSettingsException("Region key mismatch.");

            case JTelegramConnection.RATE_LIMIT_EXCEEDED:
                return new NSTGSettingsException("Client exceeded rate limit. "
                        + "Check form multiple recruiter instances");

            case JTelegramConnection.CLIENT_NOT_REGISTERED:
                return new NSTGSettingsException("Client key not registered with API. Client key typo?");

            case JTelegramConnection.SECRET_KEY_MISMATCH:
                return new NSTGSettingsException("Secret key mismatch. "
                        + "Verify secret key is valid for specified telegram ID.");

            case JTelegramConnection.NO_SUCH_TELEGRAM:
                return new NSTGSettingsException(String.format("Telegram ID <%s> does not exist",
                        keys.getTelegramId()));

            case JTelegramConnection.UNKNOWN_ERROR:
                return new NSTGSettingsException("Unknown error.");

            default:
                throw new IllegalStateException("unexpected value " + errorCode);
        }
    }

}
