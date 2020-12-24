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

package com.git.ifly6.nsapi.ctelegram.io;

import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.nsapi.NSIOException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommHappeningsTest {

    @Test
    void getActiveNations() {
        try {
            List<String> activeNations = CommHappenings.getActiveNations();
            assertTrue(activeNations.size() > 0);
            assertFalse(activeNations.get(0).contains("@@"));
            assertDoesNotThrow(
                    () -> {
                        for (String s : activeNations)
                            CommuniqueRecipients.createNation(s);
                    }
            );

        } catch (NSIOException e) {
            System.err.println("Cannot test for happenings, is internet broken?");
        }
    }
}