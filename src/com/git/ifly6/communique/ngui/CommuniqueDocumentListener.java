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

package com.git.ifly6.communique.ngui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

/**
 * Listens to document; on update, executes provided consumer.
 * @since version 2.1 (build 8) (2018-01-17)
 */
public class CommuniqueDocumentListener implements DocumentListener {

    private Consumer<DocumentEvent> consumer;

    public CommuniqueDocumentListener(Consumer<DocumentEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        consumer.accept(event);
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        consumer.accept(event);
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        consumer.accept(event);
    }
}
