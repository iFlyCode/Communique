/* Copyright (c) 2018 Kevin Wong. All Rights Reserved. */

/** Classes which used to be the centre of the Communique system. Much of the classes, due to the need for
 * maintainability, have now be shuffled into new packages as they have been rewritten. For example, the
 * {@link com.git.ifly6.communique.data.CommuniqueParser CommuniqueParser} was moved into the
 * {@link com.git.ifly6.communique.data communique.data} package while all the classes having anything to do with the
 * user interface were moved into the {@link com.git.ifly6.communique.ngui communique.ngui} package.
 * <p>
 * Marconi classes have been moved to their own package.
 * </p>
 * <p>
 * The only classes here are a {@link com.git.ifly6.communique.CommuniqueUtilities CommuniqueUtilities}, a utility class
 * and the legacy {@link com.git.ifly6.communique.CommuniqueFileReader CommuniqueFileReader} and
 * {@link com.git.ifly6.communique.CommuniqueFileWriter CommuniqueFileWriter} classes, used by the
 * {@link com.git.ifly6.communique.io communique.io} package to read legacy files.
 * </p>
 */
package com.git.ifly6.communique;
