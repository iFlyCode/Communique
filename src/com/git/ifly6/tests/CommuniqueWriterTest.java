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

package com.git.ifly6.tests;

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.communique.io.CommuniqueProcessingAction;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class CommuniqueWriterTest {

    public static void main(String[] args) throws IOException {
        CommuniqueConfig config = new CommuniqueConfig(
                new JTelegramKeys("client-key", "secret-key", "telegram-id"),
                JTelegramType.RECRUIT, CommuniqueProcessingAction.NONE, null, null);

        CommuniqueRecipient[] recipients = new CommuniqueRecipient[] {
                CommuniqueRecipients.createFlag("recruit"),
                CommuniqueRecipients.createExcludedNation("excluded1"),
                CommuniqueRecipients.createExcludedNation("excluded2"),
                CommuniqueRecipients.createExcludedNation("excluded3"),
                CommuniqueRecipients.createExcludedNation("excluded4"),
                CommuniqueRecipients.createExcludedNation("excluded5"),
                CommuniqueRecipients.createExcludedNation("excluded6"),
                CommuniqueRecipients.createExcludedNation("excluded7"),
                CommuniqueRecipients.createExcludedNation("excluded8"),
                CommuniqueRecipients.createExcludedNation("excluded9")
        };
        config.setcRecipients(Arrays.asList(recipients));

        Path path = Paths.get("test-output.json");
        CommuniqueLoader loader = new CommuniqueLoader(path);
        loader.save(config);

        assert Files.exists(path); // make sure file is in fact output
        assert config.getcRecipientsString().size() == recipients.length; // make sure lengths preserved
        assert config.getcRecipients().equals(Arrays.asList(recipients)); // make sure saving is correct

    }

}
