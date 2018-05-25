/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */

/**
 * Classes in this package manage the loading of information for Communique. This falls into two general categories,
 * loading information from disc and from the Internet. Most of the classes have to do with loading information from
 * disc, the most important of which is the {@link com.git.ifly6.communique.io.CommuniqueLoader} class, which should be the only way to load or save
 * Communique files, kept in the form defined by {@link com.git.ifly6.communique.io.CommuniqueConfig}. Some of the other classes provide
 * functionality to scrape data from Internet pages.
 * <p>
 * Note that the majority of data retrieval is done via the API, which is accessed throgh the
 * {@link com.git.ifly6.javatelegram.util.JInfoFetcher} class and the classes contained in
 * {@link com.git.ifly6.nsapi} package.
 * </p>
 *
 * @see com.git.ifly6.nsapi
 */
package com.git.ifly6.communique.io;