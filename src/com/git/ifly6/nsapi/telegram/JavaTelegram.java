package com.git.ifly6.nsapi.telegram;

import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.NSException;
import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.NSNation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * <code>JavaTelegram</code> is the heart of the library. It coordinates the monotony of handling keys, recruitment
 * flags, recipients, and sending.
 * <p>
 * <code>JavaTelegram</code> operates through a few paths. It has a dedicated input and output stream. Since
 * implementations of output change from different implementations to another, it requires a {@link JTelegramLogger} to
 * operate with the provision of output. The system accepts input by using <code>set</code> methods, setting three
 * different fields, the keys (contained in a {@link JTelegramKeys}), the <code>boolean</code> recruitment flag, and the
 * recipients contained in a <code>String[]</code>.
 * </p>
 * <p>
 * The function, due to its use inside a thread, provides a system for shutting down all threads with the volatile
 * <code>boolean killThread</code>. This allows the <code>JavaTelegram</code> to be multi-threaded whilst also allowing
 * for a safe shutdown.
 * </p>
 * <p>
 * The entire class can be overridden if a programmer feels that it implements functions incorrectly. Most sections will
 * not operate much differently if overridden, since the output of information is nearly entirely in the purview of
 * <code>JTelegramLogger</code> and the raw connection system is in <code>JTelegramConnection</code>.
 * </p>
 */
public class JavaTelegram {

	/**
	 * Time between dispatch for a normal campaign, in milliseconds
	 */
	public static final int CAMPAIGN_TIME = (int) (30.05 * 1000);

	/**
	 * Time between dispatch for recruitment telegrams, in milliseconds
	 */
	public static final int RECRUIT_TIME = (int) (180.05 * 1000);

	private static volatile boolean killThread = false;

	private int waitTime = RECRUIT_TIME;

	protected JTelegramKeys keys = new JTelegramKeys();

	private List<String> recipients = new ArrayList<>();
	private List<String> sentList = new ArrayList<>();

	private JTelegramLogger util;

	private boolean isRecruitment = true;   // Defaults to 'true' to keep on the safe side.

	/**
	 * A list of tests to run on each recipient. A <code>NSNation</code> is created for each recipient in {@link
	 * JavaTelegram#connect()} and populated before it is tested by the predicate. If any predicate returns false, the
	 * recipient will be skipped. A default predicate, which cannot be removed, is statically initialised to prevent
	 * telegrams from being sent based on this algorithm:
	 * <p>
	 * <code>if we are recruiting and nation is not recruitable -> false
	 * <br /> else (we are campaigning) and nation is not campaignable -> false</code>
	 * </p>
	 */
	private List<Predicate<NSNation>> predicates = new ArrayList<>();  // additional predicates here

	{
		predicates.add(n -> {   // default predicate for basic filtering
			// if we are recruiting and nation is not recruitable -> false
			// else (we are campaigning) and nation is not campaignable -> false
			if (isRecruitment) return n.isRecruitable();
			else return true;
		});
	}

	/**
	 * Creates a JavaTelegram function with a way of returning information and status reports as well as immediate
	 * initialisation of the keys, and the immediate setting of the isRecruitment flag.
	 * @param providedLogger is a <code>JTelegramLogger</code> which replaces the old logger for the output of
	 *                       information
	 * @param inputKeys      is a <code>JTelegramKeys</code> containing the keys to directly initialise
	 */
	public JavaTelegram(JTelegramLogger providedLogger, JTelegramKeys inputKeys, boolean isRecruitment) {
		util = providedLogger;    // to avoid creating a new method for no reason
		this.setKeys(inputKeys);
		this.setRecruitment(isRecruitment);
	}

	/**
	 * Creates a JavaTelegram function with a way of returning information and status reports. All other variables will
	 * have to be set manually later if one uses this constructor.
	 * @param logger is a <code>JTelegramLogger</code> which replaces the old logger for the output of information
	 */
	public JavaTelegram(JTelegramLogger logger) {
		util = logger;
	}

	/**
	 * Returns the amount of time which the program waits between sending telegrams. Note that in {@link
	 * JavaTelegram#connect()}, the program automatically deducts the amount of time necessary to populate
	 * <code>NSNation</code> data and check provided predicates.
	 * @return the time, in milliseconds, to wait between telegram queries
	 */
	public int getWaitTime() {
		return waitTime;
	}

	/**
	 * Sets the time between telegrams which the program is set to wait. Note that this is implemented in {@link
	 * JavaTelegram#connect()} to automatically deduct the time necessary to populate <code>NSNation</code> data and
	 * check the provided predicates.
	 * @param waitTime is the time to wait between telegrams, in milliseconds
	 */
	public void setWaitTime(int waitTime) {
		if (waitTime < NSConnection.WAIT_TIME)
			throw new JTelegramException("Telegram wait time, " + waitTime + " ms, cannot be less than " +
					NSConnection.WAIT_TIME + " milliseconds");
		if (waitTime < CAMPAIGN_TIME)
			throw new JTelegramException("Telegram wait time, " + waitTime + " ms, cannot be less than " +
					CAMPAIGN_TIME + " milliseconds");
		this.waitTime = waitTime;
	}

	/**
	 * Changes the keys which the instance will use.
	 * @param inputKeys are the keys which will be set contained in a <code>JTelegramKeys</code>
	 */
	public void setKeys(JTelegramKeys inputKeys) {
		this.keys = inputKeys;
	}

	/**
	 * Changes or sets the recipients who will be used in the connect() method.
	 * @param list is an array of all the recipients, each one for each index
	 */
	public void setRecipients(List<String> list) {
		recipients = list;
	}

	public void setRecipient(String recipient) {
		recipients = Collections.singletonList(recipient);
	}

	/**
	 * Sets the <code>isRecruitment</code> flag inside the client. This flag defaults to <code>true</code>. When it is
	 * set, it overwrites {@link JavaTelegram#waitTime} to the default constants for recruitment and campaign delays.
	 * @param isRecruitment tells the client if we are recruiting
	 */
	public void setRecruitment(boolean isRecruitment) {
		this.isRecruitment = isRecruitment;
		this.waitTime = isRecruitment ? RECRUIT_TIME : CAMPAIGN_TIME;
	}

	/**
	 * Finds out the current status of the <code>killThread</code> boolean.
	 * @return boolean killThread
	 */
	public boolean isKillThread() {
		return killThread;
	}

	/**
	 * Shuts down the connect method, if <code>killThread</code> is set to <code>true</code>. The client, if running,
	 * should terminate by the next cycle.
	 * @param killNow is the <code>boolean</code> to which <code>killThread</code> will be set
	 */
	public void setKillThread(boolean killNow) {
		killThread = killNow;
		if (killNow) Thread.currentThread().interrupt();
	}

	public void addFilter(Predicate<NSNation> p) {
		this.predicates.add(p);
	}

	/**
	 * Connects to the NationStates API and starts sending telegrams to the provided recipients with the provided keys.
	 * Note that checks are made in this method ({@link JavaTelegram#predicates}) to make sure that telegrams are sent
	 * to nations which do not opt-out of those telegrams. All output is logged using {@link JTelegramLogger}.
	 * @see JTelegramConnection
	 * @see com.git.ifly6.nsapi.telegram.util.JInfoFetcher JInfoFetcher
	 */
	public void connect() {

		// Do some null-checks to make sure we can actually send things
		if (keys.anyEmpty()) {
			util.log("Check your keys, one of them is null or empty");
			return;
		}

		if (recipients == null || recipients.isEmpty()) {
			util.log("Error, no recipients.");
			return;
		}

		// Make sure we can actually run a cycle
		killThread = false;
		int totalTelegrams = recipients.size();
		for (int i = 0; i < recipients.size(); i++) { // No iterator due to need for indexing

			String recipient = recipients.get(i);

			// Verify the defaultPredicate
			boolean passedChecks = true;
			NSNation nation = new NSNation(recipient);
			try {
				nation.populateData();
				for (Predicate<NSNation> predicate : predicates) {
					if (predicate == null) continue; // skip null predicates
					if (!predicate.test(nation)) {
						passedChecks = false;
						break;
					}
				}

			} catch (NSException e) {
				util.log(String.format("Nation %s does not exist. Skipping nation.", recipient));
				continue;

			} catch (NSIOException e) {
				util.log(String.format("Cannot query for data on %s, assuming check passed, continuing", recipient));
				e.printStackTrace();

			}

			if (!passedChecks) {
				util.log("Failed predicate check, skipping " + recipient);
				continue;
			}

			try {

				// Connect to the API
				JTelegramConnection connection = new JTelegramConnection(keys, recipient);
				int errorCode = connection.verify();

				// Verify Status, then deal with all the possible error codes...
				if (errorCode == JTelegramConnection.QUEUED) {
					util.sentTo(recipient, i, totalTelegrams);
					sentList.add(recipient);

				} else if (errorCode == JTelegramConnection.REGION_MISMATCH)
					util.log(formatError("Region key mismatch.", recipient, i + 1, totalTelegrams));

				else if (errorCode == JTelegramConnection.RATE_LIMIT_EXCEEDED)
					util.log(formatError("Client exceeded rate limit. Check for multiple recruiter instances", recipient,
							i + 1, totalTelegrams));

				else if (errorCode == JTelegramConnection.CLIENT_NOT_REGISTERED)
					util.log(formatError("Client key not registered with API, verify client key", recipient,
							i + 1, totalTelegrams));

				else if (errorCode == JTelegramConnection.SECRET_KEY_MISMATCH)
					util.log(formatError("Secret key incorrect, verify secret key", recipient,
							i + 1, totalTelegrams));

				else if (errorCode == JTelegramConnection.NO_SUCH_TELEGRAM)
					util.log(formatError("No such telegram by id: " + keys.getTelegramId(), recipient,
							i + 1, totalTelegrams));

				else if (errorCode == JTelegramConnection.UNKNOWN_ERROR)
					util.log(formatError("Unknown connection error", recipient, i + 1, totalTelegrams));

				else util.log(formatError("Unknown internal error", recipient, i + 1, totalTelegrams));
				// above should literally never happen

			} catch (IOException e) {
				util.log(formatError("Error in queuing. Check your Internet connection", recipient, i + 1,
						totalTelegrams));
				e.printStackTrace();
			}

			// Implement the rate limit, is skipped if campaign not possible
			try {

				if (killThread) { // terminate if requested
					util.log("Sending thread terminated quietly.");
					break;

				} else if (i + 1 == totalTelegrams) util.log("API Queries Complete.");
				else {
					util.log(String.format("[%d of %d] Queried for %s, next delivery in %.2f seconds",
							i + 1, totalTelegrams, recipient, (double) waitTime / 1000));
					Thread.sleep(waitTime - NSConnection.WAIT_TIME);
				}

			} catch (InterruptedException e) {
				util.log("Sending thread was forced to terminate.");    // Report.
				killThread = true;
				break;
			}
		}
	}

	/**
	 * Generates an error message in form: <code>Failed to queue delivery to: $rName, $i of $ofI. $message</code>
	 */
	private String formatError(String message, String rName, int i, int ofI) {
		if (i == 1 && ofI == 1) return String.format("Failed to queue delivery to: %s. %s", rName, message);
		return String.format("Failed to queue delivery to: %s, %d of %d. %s",
				rName, i, ofI, message);
	}
}
