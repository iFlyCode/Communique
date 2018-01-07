/* Copyright (c) 2018 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.git.ifly6.communique.ngui;

import java.util.function.Consumer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** This is a document listener which is constructed around a single <code>Consumer</code> which implements
 * <code>DocumentListener</code> with all methods having the same effect. It means that specific anonymous
 * implementations of <code>DocumentListener</code> are not necessary except so far as a consumer is written, which is
 * easy.
 * @author ifly6 */
public class CommuniqueDocumentListener implements DocumentListener {
	
	private Consumer<DocumentEvent> consumer;
	
	public CommuniqueDocumentListener(Consumer<DocumentEvent> consumer) {
		this.consumer = consumer;
	}
	
	@Override public void changedUpdate(DocumentEvent event) {
		consumer.accept(event);
	}
	
	@Override public void insertUpdate(DocumentEvent event) {
		consumer.accept(event);
	}
	
	@Override public void removeUpdate(DocumentEvent event) {
		consumer.accept(event);
	}
	
}