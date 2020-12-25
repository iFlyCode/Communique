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

package com.git.ifly6.nsapi.ctelegram.monitors;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Chamber;
import static com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Vote;
import static com.git.ifly6.nsapi.ctelegram.monitors.CommAssemblyMonitor.parseStrings;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommAssemblyMonitorTest {

    @Test
    void parseStringsTest() {
        Map<Pair<Chamber, Vote>, Pair<Chamber, Vote>> map = new HashMap<>();
        map.put(parseStrings("GA", "for"), new Pair<>(Chamber.GA, Vote.FOR));
        map.put(parseStrings("Ga", "against"), new Pair<>(Chamber.GA, Vote.AGAINST));
        map.put(parseStrings("sC", "for"), new Pair<>(Chamber.SC, Vote.FOR));
        map.put(parseStrings("SC", "against"), new Pair<>(Chamber.SC, Vote.AGAINST));
        for (Map.Entry<Pair<Chamber, Vote>, Pair<Chamber, Vote>> entry : map.entrySet())
            assertEquals(entry.getValue(), entry.getKey());
    }
}