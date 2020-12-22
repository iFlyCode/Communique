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

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.ngui.CommuniqueConstants;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;

import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles GUI interface with configurations.
 * @since version 3.0 (build 13)
 */
public class Communique3ConfigHandler {

    private static final Logger LOGGER = Logger.getLogger(Communique3ConfigHandler.class.getName());
    private Communique3 comm;

    Communique3ConfigHandler(Communique3 comm) {
        this.comm = comm;
    }

    public void setConfig(CommuniqueConfig config) {
        // this section here cannot operate directly on text area or it will delete a provided self-config's contents
        String output = config.getcRecipients().stream()
                .map(CommuniqueRecipient::toString)
                .collect(Collectors.joining("\n"));
        comm.textArea.setText(CommuniqueConstants.CODE_HEADER + output);

        JTelegramKeys keys = config.keys;
        comm.fieldClient.setText(keys.getClientKey());
        comm.fieldSecret.setText(keys.getSecretKey());
        comm.fieldTelegramID.setText(keys.getTelegramId());

        comm.fieldTelegramType.setSelectedItem(config.telegramType);
        comm.fieldProcessingAction.setSelectedItem(config.processingAction);
        comm.fieldAutoStop.setText(config.autoStop != null
                ? String.valueOf(config.autoStop.toMinutes())
                : "");
        comm.fieldTelegramDelay.setText(config.telegramDelay != null
                ? String.valueOf(config.telegramDelay.getSeconds())
                : "");

        comm.config = config;
        LOGGER.info("Imported configuration data");
    }
}
