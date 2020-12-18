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

/**
 * This package manages parsing for the Communique system. Each recipient is contained in a {@link
 * com.git.ifly6.communique.data.CommuniqueRecipient CommuniqueRecipient} object, which can then be applied via the
 * {@link com.git.ifly6.communique.data.Communique7Parser parser} to determine whatever recipients are meant. This
 * package also contains a number of <code>enum</code>s which are used to hold methods that make those recipients work.
 * <p>A lot of the data which is queried in the parser is provided via {@link com.git.ifly6.nsapi.telegram.util.JInfoCache}
 * and other classes like {@link com.git.ifly6.nsapi.NSNation} and {@link com.git.ifly6.nsapi.NSRegion}.</p>
 * @see com.git.ifly6.nsapi
 * @since version 2.0 (build 7)
 */
package com.git.ifly6.communique.data;