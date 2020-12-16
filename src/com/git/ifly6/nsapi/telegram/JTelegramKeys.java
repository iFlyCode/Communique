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

import java.util.Arrays;
import java.util.Objects;

/**
 * <code>JTelegramKeys</code> creates a unified object for the storage and retrieval of the client, secret, and
 * telegram keys.
 * <p>
 * Due to the problems of hardcoding a direct <code>String[]</code> as the system for holding the keys as well as not
 * being modular in any way, this system was created to organise them and simplify usage. It gives multiple methods
 * which are able to access and get the keys. It also gives ways to translate those keys into a <code>String[]</code>
 * and a <code>String</code>.
 * </p>
 * <p>
 * All <code>setX</code> methods trim their inputs.
 * </p>
 */
public class JTelegramKeys {

	private String clientKey;
	private String secretKey;
	private String telegramId;

	/**
	 * Creates a <code>JTelegramKeys</code> which holds the client, secret, and telegram keys provided in the
	 * constructor.
	 * @param client a <code>String</code> which contains the <code>clientKey</code>
	 * @param secret a <code>String</code> which contains the <code>secretKey</code>
	 * @param tgId   a <code>String</code> which contains the <code>telegramId</code>
	 */
	public JTelegramKeys(String client, String secret, String tgId) {
		clientKey = client;
		secretKey = secret;
		telegramId = tgId;
	}

	/**
	 * Creates a purposely invalid "empty" <code>JTelegramKeys</code>.
	 */
	public JTelegramKeys() {
		// cannot be removed; IO code relies on this 'empty' constructor
		clientKey = "CLIENT_KEY";
		secretKey = "SECRET_KEY";
		telegramId = "TELEGRAM_ID";
	}

	@Override
	public String toString() {
		return String.join(", ", Arrays.asList(clientKey, secretKey, telegramId));
	}

	public String getClientKey() {
		return clientKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public String getTelegramId() {
		return telegramId;
	}

	public void setClientKey(String input) {
		clientKey = input.trim();
	}

	public void setSecretKey(String input) {
		secretKey = input.trim();
	}

	public void setTelegramId(String input) {
		telegramId = input.trim();
	}

	/** @return {@code String[]} with elements {@code { clientKey, secretKey, telegramId }}. */
	public String[] toArray() {
		return new String[]{clientKey, secretKey, telegramId};
	}

	/** @return true if any key is empty or null */
	public boolean anyEmpty() {
		if (Objects.isNull(clientKey) || clientKey.isEmpty()) return true;
		if (Objects.isNull(secretKey) || secretKey.isEmpty()) return true;

		//noinspection RedundantIfStatement
		if (Objects.isNull(telegramId) || telegramId.isEmpty()) return true;
		// ^ IntelliJ says 'simplify!', this is more clear

		return false;
	}

}
