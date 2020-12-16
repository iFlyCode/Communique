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

package com.git.ifly6.communique.ngui.components;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import java.awt.Font;

import static com.git.ifly6.communique.ngui.components.CommuniqueConstants.CODE_HEADER;

public class CommuniqueFactory {

    /**
     * Creates a {@link JTextField} with tooltip text and document listener, pre-loaded with monospaced font setting.
     * @param text     to initialise with
     * @param tooltip  to give
     * @param listener to execute
     * @return constructed <code>JTextField</code>
     */
    public static JTextField createField(String text, String tooltip, DocumentListener listener) {
        JTextField field = new JTextField();
        field.setToolTipText(tooltip);
        field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        field.setText(text);
        field.getDocument().addDocumentListener(listener);
        return field;
    }

    public static JTextArea createArea(String defaultText, DocumentListener listener) {
        JTextArea area = new JTextArea();
        area.setText(CODE_HEADER);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        area.getDocument().addDocumentListener(listener);
        return area;
    }

}
