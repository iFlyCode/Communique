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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommuniqueRecipientTest {

    // DO NOT RE-ORDER! testToString requires order!
    static Map<String, CommuniqueRecipient> values = ImmutableMap.of(
            "+nation:imperium_anglorum", new CommuniqueRecipient(FilterType.INCLUDE, RecipientType.NATION, "imperium_anglorum"),
            "-nation:imperium_anglorum", new CommuniqueRecipient(FilterType.EXCLUDE, RecipientType.NATION, "imperium_anglorum"),
            "+region:europe", new CommuniqueRecipient(FilterType.INCLUDE, RecipientType.REGION, "europe"),
            "+regex:[A-Z].*", new CommuniqueRecipient(FilterType.REQUIRE_REGEX, RecipientType.NONE, "[A-Z].*"),
            "imperium_anglorum", new CommuniqueRecipient(FilterType.NORMAL, RecipientType.NATION, "imperium_anglorum")
    );

    @Test
    void parseRecipient() {
        for (Map.Entry<String, CommuniqueRecipient> entry : values.entrySet())
            assertEquals(
                    entry.getValue(),
                    CommuniqueRecipient.parseRecipient(entry.getKey()));
        for (Map.Entry<String, CommuniqueRecipient> entry : values.entrySet()) // test if upper case
            assertEquals(
                    entry.getValue(),
                    CommuniqueRecipient.parseRecipient(entry.getKey().toUpperCase()));

        assertEquals(
                CommuniqueRecipient.parseRecipient("nation:nation:Imperium Anglorum"),
                new CommuniqueRecipient(FilterType.NORMAL, RecipientType.NATION, "imperium_anglorum")
        );

        String value = CommuniqueRecipient.parseRecipient("ALPHABET:15").toString();
        System.out.println(value);
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
        List<Map.Entry<String, CommuniqueRecipient>> entries = new ArrayList<>(values.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, CommuniqueRecipient> entry = entries.get(i);
            if (!entry.getKey().contains(":"))
                continue;

            assertEquals(entry.getKey(), entry.getValue().toString());
        }
    }
}