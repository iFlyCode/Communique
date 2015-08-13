/*
 * Copyright (c) 2015 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * This class here is designed to be an instance of an object which can be called upon for sending.
 * It is not static, since having it as static would likely mean an annoying amount of code. Hence,
 * with instances, it can be called from anywhere, and thus, have multiple front-ends. The purpose
 * of this was so multiple front-ends could be built -- a GUI or CLI -- and thus, solve problems
 * much easier than if they were hardcoded for one another.
 */

package com.git.ifly6.communique;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.git.ifly6.javatelegram.JTelegramLogger;

import javafx.scene.control.TextArea;

/**
 * This class interfaces with {@link com.git.ifly6.javatelegram.JTelegramLogger JTelegramLogger} to provide a way of
 * reporting on the output of the <code>JavaTelegram</code> client.
 *
 * <p>
 * Note that this class doubles as a easy way for <code>Communiqué</code> to log to itself important information. The
 * methods inside it are effectively just text manipulation methods on the part of both Communiqué and JavaTelegram.
 * </p>
 *
 * @see Communiqué
 */
public class CommuniquéLogger implements JTelegramLogger {

	private TextArea logPane;
	private TextArea codePane;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public CommuniquéLogger(TextArea inputLog, TextArea inputCode) {
		logPane = inputLog;
		codePane = inputCode;
	}

	/**
	 * Localises the appropriate output for our Communiqué interface.
	 *
	 * @param input is the <code>String</code> to be input into <code>logPane</code>
	 */
	@Override public void log(String input) {
		logPane.appendText("[" + currentTime() + "] " + input + "\n");
		logPane.positionCaret(logPane.getLength());
	}

	/**
	 * Convenience method so that the <code>Communiqué</code> interface can easily (and without errors) print to its
	 * <code>codePane</code>.
	 *
	 * @param input is the string to be appended to <code>codePane</code>
	 */
	public void codePrintln(String input) {
		codePane.appendText(input + "\n");
	}

	private String currentTime() {
		Date date = new Date();
		return dateFormat.format(date);
	}
}
