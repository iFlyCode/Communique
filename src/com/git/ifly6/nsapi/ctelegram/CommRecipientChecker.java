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

import com.git.ifly6.nsapi.NSNation;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommNationCache;
import com.git.ifly6.nsapi.telegram.JTelegramType;

/** Checks whether recipient accepts telegrams based on telegram type.
 * @since version 3.0 (build 13) */
public class CommRecipientChecker {

    /**
     * Does the recipient accept our telegram? If we are recruiting and nation is not recruitable, return false. If
     * campaigning and nation is not campaign-able, return false. Otherwise, return true. Values are cached.
     * @param r recipient to check
     * @param t type to check against
     * @return true if recipient accepts telegram
     */
    public static boolean doesRecipientAccept(String r, JTelegramType t) {
        NSNation n = CommNationCache.getInstance().lookupObject(r);
        if (t == JTelegramType.RECRUIT) return n.isRecruitable();
        if (t == JTelegramType.CAMPAIGN) return n.isCampaignable();
        return true;
    }
}
