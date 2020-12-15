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
 * Classes in this package manage the loading of information for Communique. This falls into two general categories,
 * loading information from disc and from the Internet. Most of the classes have to do with loading information from
 * disc, the most important of which is the {@link com.git.ifly6.communique.io.CommuniqueLoader} class, which should be
 * the only way to load or save Communique files, kept in the form defined by {@link
 * com.git.ifly6.communique.io.CommuniqueConfig}. Some of the other classes provide functionality to scrape data from
 * Internet pages.
 * <p>Note that the majority of data retrieval is done via the API, which is accessed throgh the {@link
 * com.git.ifly6.nsapi.telegram.util.JInfoCache} class and the classes contained in {@link com.git.ifly6.nsapi}
 * package.</p>
 * @see com.git.ifly6.nsapi
 */
package com.git.ifly6.communique.io;