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

import org.junit.jupiter.api.Test;

import javax.swing.JComboBox;
import javax.swing.JTextArea;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Communique3UtilsTest {

    private static final Random random = new Random();

    @Test
    void appendLine() {
        JTextArea a = new JTextArea();
        a.setText(String.join("\n", createText(10)));
        Communique3Utils.appendLine(a, "antidisestablishmentarianism");
        assertTrue(a.getText().trim().endsWith("antidisestablishmentarianism"));
    }

    @Test
    void getComboBoxSelection() {
        int maxValues = 128;

        String[] array = createText(maxValues).toArray(new String[0]);
        int selection = random.nextInt(maxValues);

        JComboBox<String> comboBox = new JComboBox<>(array);
        comboBox.setSelectedIndex(selection);
        assertEquals(Communique3Utils.getComboBoxSelection(comboBox), array[selection]);
    }

    private static List<String> createText(int maxLines) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < maxLines; i++)
            lines.add(random.ints('a', 'a' + 26).limit(10)
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.joining("")));
        return lines;
    }
}