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

package com.git.ifly6.communique.data;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.git.ifly6.communique.data.CommuniqueFilterType.EXCLUDE;
import static com.git.ifly6.communique.data.CommuniqueFilterType.INCLUDE;
import static com.git.ifly6.communique.data.CommuniqueFilterType.NORMAL;
import static com.git.ifly6.communique.data.CommuniqueFilterType.REQUIRE_REGEX;
import static com.git.ifly6.communique.data.CommuniqueRecipientType.NATION;
import static com.git.ifly6.communique.data.CommuniqueRecipientType.NONE;
import static com.git.ifly6.communique.data.CommuniqueRecipientType.REGION;
import static com.git.ifly6.communique.data.CommuniqueRecipientType._VOTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommuniqueRecipientTest {

    // DO NOT RE-ORDER! testToString requires order!
    static Map<String, CommuniqueRecipient> reversible = new HashMap<>();
    static List<String> parseFails = new ArrayList<>();

    static {
        reversible.put("+nation:panem", new CommuniqueRecipient(INCLUDE, NATION, "panem"));
        reversible.put("-nation:et", new CommuniqueRecipient(EXCLUDE, NATION, "et"));
        reversible.put("+region:circenses", new CommuniqueRecipient(INCLUDE, REGION, "circenses"));
        reversible.put("+regex:[A-Z].*", new CommuniqueRecipient(REQUIRE_REGEX, NONE, "[A-Z].*"));
        reversible.put("imperium_anglorum", new CommuniqueRecipient(NORMAL, NATION, "imperium_anglorum"));
        reversible.put("_voting:ga; for", new CommuniqueRecipient(NORMAL, _VOTING, "ga; for"));

        parseFails.add("PEN:15");
        parseFails.add("hari:seldon_has");
        parseFails.add("a_great:foundational");
        parseFails.add("plan:for_the");
        parseFails.add("long_long:ages");
    }

    @Test
    void parseRecipient() {
        for (Map.Entry<String, CommuniqueRecipient> entry : reversible.entrySet())
            assertEquals(
                    entry.getValue(),
                    CommuniqueRecipient.parseRecipient(entry.getKey()));
        for (Map.Entry<String, CommuniqueRecipient> entry : reversible.entrySet()) // test if upper case
            assertEquals(
                    entry.getValue(),
                    CommuniqueRecipient.parseRecipient(entry.getKey().toUpperCase()));

        assertEquals(
                CommuniqueRecipient.parseRecipient("nation:nation:Imperium Anglorum"),
                new CommuniqueRecipient(NORMAL, NATION, "imperium anglorum")
        );

        for (String s : parseFails)
            assertThrows(IllegalArgumentException.class, () -> CommuniqueRecipient.parseRecipient(s));
    }

    @Test
    void translateTokens() {
        List<String> tokens = CommuniqueRecipient.translateTokens(Arrays.asList(
                "wa:members -- region:Europe",
                "/imperium_anglorum",
                "wa:delegates",
                "wa:nations -> region:the_north_pacific",
                "region:the_east_pacific->wa:members"//,
//                "+region:Europe", // old and new tokens won't be mixed
//                "+regex:[A-Z].*$"
        ));
        assertEquals(tokens, Arrays.asList(
                "tag:wa", "-region:europe",
                "-nation:imperium_anglorum",
                "tag:delegates",
                "tag:wa", "+region:the_north_pacific",
                "region:the_east_pacific", "+tag:wa"
        ));
    }

    @Test
    void testToString() {
        List<Map.Entry<String, CommuniqueRecipient>> entries = new ArrayList<>(reversible.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, CommuniqueRecipient> entry = entries.get(i);
            if (!entry.getKey().contains(":"))
                continue;

            assertEquals(entry.getKey(), entry.getValue().toString());
        }
    }
}