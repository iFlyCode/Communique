/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.marconi;

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.FilterType;
import com.git.ifly6.communique.data.RecipientType;
import com.git.ifly6.communique.ngui.AbstractCommuniqueRecruiter;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** @author Kevin */
public class MarconiRecruiter extends AbstractCommuniqueRecruiter implements JTelegramLogger {

	private static final Logger LOGGER = Logger.getLogger(MarconiRecruiter.class.getName());
	private Marconi marconi;

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
					marconi.log(String.format("Attempted dispatch of telegram %d to %s", sentList.size(),
							nextRecipient.get()));

					Calendar now = Calendar.getInstance();
					now.add(Calendar.SECOND, 180);
					String nextTelegramTime = new SimpleDateFormat("HH:mm:ss").format(now.getTime());
					marconi.log("Next recruitment telegram probably in 180 seconds at " + nextTelegramTime);
					setX = 1;

				} catch (RuntimeException e) {    // Catch, if error between recipient retrieval and telegram dispatch
					// this catch block allows for that extra bit of fault tolerance

					marconi.log("Failed to dispatch telegram to " + nextRecipient.get());
					marconi.log(e.toString());

					Calendar now = Calendar.getInstance();
					now.add(Calendar.SECOND, 10);
					String nextTelegramTime = new SimpleDateFormat("HH:mm:ss").format(now.getTime());
					marconi.log("Next recruitment telegram probably in 10 seconds at " + nextTelegramTime);
					setX = 170;

				}

				// new 2017-03-25
				for (AtomicInteger x = new AtomicInteger(setX); ; x.getAndIncrement()) {

					try {
						Thread.sleep(1000);    // 1-second intervals, wake to update the progressBar
						LOGGER.finest("Interval " + x.get());
					} catch (InterruptedException e) {
						return; // also breaks
					}

					if (x.get() == 170) {
						Runnable runnable1 = () -> {
							nextRecipient.set(getRecipient().getName());
							LOGGER.fine("Found next recipient, " + nextRecipient.get() + ", at " + x.get());
							foundNext.set(true);
						};
						LOGGER.fine("Running runnable to find next recipient at " + x.get());
						new Thread(runnable1).start();
					}

					if (x.get() >= 180 && foundNext.get()) {    // delay until recipient is found by runnable1's thread
						LOGGER.info(String.format("Starting next loop, delay of %d s for telegram %d", 180 - x.get(),
								sentList.size()));
						break;  // break this loop, and dispatch the next telegram
					}

				}

			}
		};

		Thread thread = new Thread(runner);
		thread.start();

	}

	/**
	 * @return the regions currently specified as excluded in the recipients code
	 */
	private Set<CommuniqueRecipient> getProscribedRegions() {
		if (proscribedRegions == null)
			return marconi.exportState().getcRecipients().stream()
					.filter(r -> r.getFilterType() == FilterType.EXCLUDE)
					.filter(r -> r.getRecipientType() == RecipientType.REGION)
					.collect(Collectors.toSet());
		return proscribedRegions;
	}

	/** @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String) */
	@Override
	public void log(String input) {
		// Get rid of useless messages
		if (input.equals("API Queries Complete.")) return;
		marconi.log(input);
	}

	/** @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
	@Override
	public void sentTo(String r, int x, int i) {
		super.sentTo(r, x, i);
		marconi.sentTo(r, x, i);
	}

}
