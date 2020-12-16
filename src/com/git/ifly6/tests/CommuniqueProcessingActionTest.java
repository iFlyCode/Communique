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
import com.git.ifly6.communique.io.CommuniqueProcessingAction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.git.ifly6.communique.data.CommuniqueRecipients.createNation;

public class CommuniqueProcessingActionTest {


    public static void main(String[] args) {
        List<CommuniqueRecipient> original = new ArrayList<>();
        original.add(createNation("alpha"));
        original.add(createNation("bravo"));
        original.add(createNation("charlie"));
        original.add(createNation("delta"));
        original.add(createNation("echo"));
        original.add(createNation("foxtrot"));
        original.add(createNation("golf"));

        List<String> input = original.stream().map(CommuniqueRecipient::toString).collect(Collectors.toList());
        System.out.println(CommuniqueProcessingAction.REVERSE.apply(input));
        System.out.println(CommuniqueProcessingAction.RANDOMISE.apply(input));
        System.out.println(CommuniqueProcessingAction.NONE.apply(input));
    }
}
