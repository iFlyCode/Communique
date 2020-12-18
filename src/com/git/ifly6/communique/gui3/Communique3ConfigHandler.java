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

/**
 * Handles GUI interface with configurations.
 * @since version 3.0 (build 13)
 */
public class Communique3ConfigHandler {

    private Communique3 comm;

    Communique3ConfigHandler(Communique3 comm) {
        this.comm = comm;
    }

    /**
     * Appends {@link CommuniqueConfig#getcRecipients()#toString()} to area with new lines.
     */
    public void importRecipients() {
        comm.textArea.setText(CommuniqueConstants.CODE_HEADER);
        for (CommuniqueRecipient r : comm.config.getcRecipients())
            Communique3Utils.appendLine(comm.textArea, r.toString());
    }

    public void importKeys() {
        JTelegramKeys keys = comm.config.keys;
        comm.fieldClient.setText(keys.getClientKey());
        comm.fieldSecret.setText(keys.getSecretKey());
        comm.fieldTelegramID.setText(keys.getTelegramId());
    }

}
