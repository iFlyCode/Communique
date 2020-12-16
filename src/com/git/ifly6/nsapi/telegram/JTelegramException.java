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

package com.git.ifly6.nsapi.telegram;

/**
 * <code>JTelegramException</code> indicates conditions that an application might want to catch when related to issues
 * which can only be created by a failure to sanitise data which is recognised by <code>JavaTelegram</code>.
 * <p>
 * Proper use of this class is to catch an <code>Exception</code> which would arise, but can only arise due to a failure
 * to handle data correctly. The purpose of this class is to replace the use of <code>catch (Exception e)</code> in code
 * for the purpose of being more accurate in cases where an <code>IOException</code> could also occur.
 */
public class JTelegramException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JTelegramException() {
        super();
    }

    public JTelegramException(String message) {
        super(message);
    }

    public JTelegramException(String message, Throwable e) {
        super(message, e);
    }
}
