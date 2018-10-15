/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */

/**
 * This package manages parsing for the Communique system. Each recipient is contained in a {@link
 * com.git.ifly6.communique.data.CommuniqueRecipient CommuniqueRecipient} object, which can then be applied via the
 * {@link com.git.ifly6.communique.data.Communique7Parser parser} to determine whatever recipients are meant. This
 * package also contains a number of <code>enum</code>s which are used to hold methods that make those recipients work.
 * <p>A lot of the data which is queried in the parser is provided via {@link com.git.ifly6.javatelegram.util.JInfoFetcher}
 * and other classes like {@link com.git.ifly6.nsapi.NSNation} and {@link com.git.ifly6.nsapi.NSRegion}.</p>
 * @see com.git.ifly6.nsapi
 */
package com.git.ifly6.communique.data;