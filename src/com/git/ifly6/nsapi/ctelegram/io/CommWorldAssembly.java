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

import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.NSIOException;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

/** Gets information about the World Assembly. */
public class CommWorldAssembly {

    private CommWorldAssembly() {
    }

    /** Formats URL for NS chamber vote. */
    private static String formatURL(Chamber c, Vote v) {
        return NSConnection.API_PREFIX
                + MessageFormat.format("wa={0}&q=resolution+voters", c.getCouncilCode());
    }

    /**
     * Gets voters in a chamber who are voting a certain way
     * @param chamber to look in
     * @param voting direction to look for
     * @return voters who are voting in specified chamber with specified vote
     */
    public static List<String> getVoters(Chamber chamber, Vote voting) {
        try {
            NSConnection apiConnect = new NSConnection(formatURL(chamber, voting));
            XML xml = new XMLDocument(apiConnect.getResponse());
            List<String> voters = xml.xpath(
                    MessageFormat.format("/WA/RESOLUTION/{0}/N/text()", // load all these values
                            voting.getXMLTag())); // get elements voting this direction
            return ApiUtils.ref(voters); // ref

        } catch (IOException e) {
            throw new NSIOException("Could not connect to NationStates API", e);
        }
    }

    public enum Vote {
        FOR {
            @Override
            public String getXMLTag() {
                return "VOTES_FOR";
            }
        }, AGAINST {
            @Override
            public String getXMLTag() {
                return "VOTES_AGAINST";
            }
        };

        public abstract String getXMLTag();
    }

    public enum Chamber {
        GA {
            @Override
            public int getCouncilCode() {
                return 1;
            }

            @Override
            public String properName() {
                return "General Assembly";
            }
        }, SC {
            @Override
            public int getCouncilCode() {
                return 2;
            }

            @Override
            public String properName() {
                return "Security Council";
            }
        };

        public abstract int getCouncilCode();

        public abstract String properName();
    }

}
