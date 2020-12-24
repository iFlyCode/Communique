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

import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.data.CommuniqueFilterType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Communique7ParserTranslateTest {

    // testing methods for the parser
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        System.out.print("[translate], [parser]?\t");
        String input = scan.nextLine();

        assert translate("wa:members").equals(CommuniqueRecipients.createTag(CommuniqueFilterType.NORMAL, "wa"));
        assert translate("region:Europe").equals(CommuniqueRecipients.createRegion(CommuniqueFilterType.NORMAL, "Europe"));
        assert translate("/region:Europe").equals(CommuniqueRecipients.createRegion(CommuniqueFilterType.EXCLUDE, "Europe"));
        assert translate("/wa:delegates").equals(CommuniqueRecipients.createTag(CommuniqueFilterType.EXCLUDE, "delegates"));

        if ("translate".equals(input)) {

            List<String> list = Arrays.asList("wa:members", "tag:wa", "region:Europe -> wa:members", "wa:delegates",
                    "tag:new", "world:new", "imperium anglorum", "/imperium anglorum", "region:Europe -- wa:members");
            System.out.println(String.join("\n", CommuniqueRecipient.translateTokens(list)));

            System.out.println();

            System.out.println(CommuniqueRecipient
                    .translateTokens(Collections.singletonList("region:Europe -> wa:members")));

        } else if ("parser".equals(input)) {

            Communique7Parser ps = new Communique7Parser();

            List<CommuniqueRecipient> blah = new ArrayList<>();
            blah.add(CommuniqueRecipients.createNation("hi ya"));
            blah.add(CommuniqueRecipients.createNation("hi ya"));
            blah.add(CommuniqueRecipients.createNation("hi ya"));
            blah.add(CommuniqueRecipients.createNation("hi ya"));

            ps.apply(blah);
            System.out.println(ps.listRecipients());

        }

        scan.close();

    }

    /**
     * This method is syntactical sugar for the translation process in {@link CommuniqueRecipient#translateTokens(List)}.
     * That method is applied on lists because some of the old tokens, e.g. <code>region:europe -> wa:members</code> now
     * translates to <code>region:europe, +tag:wa</code>, which is technically two statements.
     * @param s is a <code>String</code> in the old style to translate
     * @return the {@link CommuniqueRecipient} that is translated
     */
    private static CommuniqueRecipient translate(String s) {
        List<String> list = CommuniqueRecipient.translateTokens(Collections.singletonList(s));
        if (list.size() > 0)
            return CommuniqueRecipient.parseRecipient(list.get(0));

        throw new IllegalArgumentException("Problem with parsing " + s);
    }

}
