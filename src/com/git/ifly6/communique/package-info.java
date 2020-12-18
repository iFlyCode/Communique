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
 * Classes which used to be the centre of the Communique system. Much of the classes, due to the need for
 * maintainability, have now be shuffled into new packages as they have been rewritten. For example, the {@code
 * CommuniqueParser} was moved into the {@link com.git.ifly6.communique.data communique.data} package while all the
 * classes having anything to do with the user interface were moved into the {@link com.git.ifly6.communique.ngui
 * communique.ngui} package.
 * <p>The only classes here are a {@link com.git.ifly6.commons.CommuniqueUtilities CommuniqueUtilities}, a utility
 * class and the legacy {@link com.git.ifly6.communique.CommuniqueFileReader CommuniqueFileReader} classes, used by the
 * {@link com.git.ifly6.communique.io communique.io} package to read legacy files.</p>
 */
package com.git.ifly6.communique;
