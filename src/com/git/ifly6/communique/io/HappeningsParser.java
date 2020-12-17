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

package com.git.ifly6.communique.io;

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.telegram.JTelegramException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HappeningsParser {
    // todo make monitor

    private static final String HAPPENINGS_URL = "https://www.nationstates.net/cgi-bin/api.cgi?q=happenings;filter=law+change+dispatch+rmb+embassy+admin+vote+resolution+member";

    public static List<CommuniqueRecipient> getActiveNations() throws JTelegramException {
        try {
            NSConnection connection = new NSConnection(HAPPENINGS_URL).connect();

            String data = connection.getResponse();

            Pattern pattern = Pattern.compile(Pattern.quote("@@") + "(.*?)" + Pattern.quote("@@"));
            Matcher matcher = pattern.matcher(data);

            List<String> matches = new ArrayList<>();
            while (matcher.find()) {
                String matchedText = matcher.group(1);
                matches.add(matchedText);
            }

            return matches.stream().map(CommuniqueRecipients::createNation).collect(Collectors.toList());

        } catch (IOException e) {
            throw new JTelegramException("Encountered IO exception when getting active nations", e);
        }
    }

}
