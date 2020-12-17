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

package com.git.ifly6.marconi;

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.FilterType;
import com.git.ifly6.communique.data.RecipientType;
import com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter;
import com.git.ifly6.nsapi.telegram.JTelegramLogger;
import com.git.ifly6.nsapi.telegram.JavaTelegram;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** @author ifly6 */
@Deprecated
public class MarconiRecruiter extends AbstractCommuniqueRecruiter implements JTelegramLogger {

    private static final Logger LOGGER = Logger.getLogger(MarconiRecruiter.class.getName());
    private Marconi marconi;

    private static final int RECRUITMENT_DELAY = 180;
    private static final int FIND_NEXT_TIME = 10;

    /** @param marconi framework to piggy-back upon to send data */
    MarconiRecruiter(Marconi marconi) {
        this.marconi = marconi;
    }

    /** @see com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter#send() */
    @Override
    public void send() {

        if (MarconiUtilities.isFileLocked())
            throw new RuntimeException("Another instance of Marconi is already running. Cannot send.");

        Runnable runner = () -> {

            proscribedRegions = getProscribedRegions();

            // init with first recipient so we can immediately start sending
            AtomicReference<String> nextRecipient = new AtomicReference<>(getRecipient().getName());
            AtomicBoolean foundNext = new AtomicBoolean(false);

            while (true) {

                int setX;

                // Otherwise, start sending.
                try {
                    JavaTelegram client = new JavaTelegram(this);
                    client.setKeys(marconi.exportState().keys);
                    client.setRecipient(nextRecipient.get());
                    client.connect();
                    foundNext.set(false);

                    // Report information
                    marconi.log(String.format("Attempted dispatch of telegram %d to %s", sentList.size() + 1,
                            nextRecipient.get()));

                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, 180);
                    String nextTelegramTime = new SimpleDateFormat("HH:mm:ss").format(now.getTime());
                    marconi.log("Next recruitment telegram probably in 180 seconds at " + nextTelegramTime);
                    setX = 0;

                } catch (RuntimeException e) {    // Catch, if error between recipient retrieval and telegram dispatch
                    // this catch block allows for that extra bit of fault tolerance
                    marconi.log("Failed to dispatch telegram to " + nextRecipient.get());
                    marconi.log(e.toString());

                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, 10);
                    String nextTelegramTime = new SimpleDateFormat("HH:mm:ss").format(now.getTime());
                    marconi.log("Next recruitment telegram probably in 10 seconds at " + nextTelegramTime);
                    setX = RECRUITMENT_DELAY - FIND_NEXT_TIME;

                }

                // new 2017-03-25
                for (AtomicInteger x = new AtomicInteger(setX); ; x.getAndIncrement()) {

                    try {
                        Thread.sleep(1000); // 1-second intervals, wake to update the progressBar
                        LOGGER.finest("Interval " + x.get());
                    } catch (InterruptedException e) {
                        return; // break out of it all
                    }

                    if (x.get() == RECRUITMENT_DELAY - FIND_NEXT_TIME) {
                        Runnable runnable1 = () -> {
                            nextRecipient.set(getRecipient().getName());
                            LOGGER.fine("Found next recipient, " + nextRecipient.get() + ", at " + x.get());
                            foundNext.set(true);
                        };
                        LOGGER.fine("Running runnable to find next recipient at " + x.get());
                        new Thread(runnable1).start();
                        // note: this runnable will continue until it finds the next recipient
                    }

                    if (x.get() >= RECRUITMENT_DELAY && foundNext.get()) { // delay until recipient is found by runnable1's thread
                        // note: x.get() -> time; time - expected = delay; time - 180 = delay
                        LOGGER.info(String.format("Starting next loop, delay of %d s for telegram %d", x.get() - 180,
                                sentList.size()));
                        break; // break this loop, and dispatch the next telegram
                    }

                }

            }
        };

        Thread thread = new Thread(runner);
        thread.start();

    }

    /** @return the regions currently specified as excluded in the recipients code */
    private Set<CommuniqueRecipient> getProscribedRegions() {
        if (proscribedRegions == null)
            return marconi.exportState().getcRecipients().stream()
                    .filter(r -> r.getRecipientType() == RecipientType.REGION)
                    .filter(r -> r.getFilterType() == FilterType.EXCLUDE)
                    .collect(Collectors.toCollection(HashSet::new));
        return proscribedRegions;
    }

    /** @see com.git.ifly6.nsapi.telegram.JTelegramLogger#log(java.lang.String) */
    @Override
    public void log(String input) {
        // Get rid of useless messages
        if (input.equals("API Queries Complete.")) return;
        marconi.log(input);
    }

    /** @see com.git.ifly6.nsapi.telegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
    @Override
    public void sentTo(String recipient, int recipientNum, int length) {
        super.sentTo(recipient, recipientNum, length);
        marconi.sentTo(recipient, recipientNum, length);
    }

}
