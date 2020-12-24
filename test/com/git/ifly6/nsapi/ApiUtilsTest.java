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

package com.git.ifly6.nsapi;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiUtilsTest {

    static Map<String, String> values = new HashMap<>();

    static {
        values.put("region:EuRoPe", "region:europe");
        values.put("imperium anglorum", "imperium_anglorum");
        values.put("panem Et circensus", "panem_et_circensus");
        values.put("        pax", "pax");
        values.put("IO SATURNALIA", "io_saturnalia");
        values.put("IO  SATURNALIA", "io__saturnalia");
        values.put("  IO  oPAlIA  ", "io__opalia");
    }

    @Test
    void ref() {
        assertThrows(NullPointerException.class, () -> ApiUtils.ref((String) null));
        for (Map.Entry<String, String> entry : values.entrySet())
            assertEquals(ApiUtils.ref(entry.getKey()), entry.getValue());
    }

    @Test
    void testRef() {
        assertEquals(ApiUtils.ref(new ArrayList<>(values.keySet())),
                new ArrayList<>(values.values()));
    }

    @Test
    void isNotEmpty() {
        assertTrue(ApiUtils.isNotEmpty("jdkl"));
        assertTrue(ApiUtils.isNotEmpty("  j      dkl"));
        assertFalse(ApiUtils.isNotEmpty("        "));
        assertFalse(ApiUtils.isNotEmpty(""));
    }

    @Test
    void contains() {
        assertTrue(ApiUtils.contains("So hello from the other side".split("\\s"), "hello"));
        assertFalse(ApiUtils.contains("Don't think about".split("\\s"), "elephants"));
    }
}