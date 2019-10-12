package com.git.ifly6.communique.io;

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.javatelegram.JTelegramException;
import com.git.ifly6.nsapi.NSConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HappeningsParser {

	private static final String HAPPENINGS_URL = "https://www.nationstates.net/cgi-bin/api.cgi?q=happenings;filter=law+change+dispatch+rmb+embassy+admin+vote+resolution+member";

	public static List<CommuniqueRecipient> getActiveNations() throws JTelegramException {
		try {
			NSConnection connection = new NSConnection(HAPPENINGS_URL).connect();

			String data = connection.getResponse();

			Pattern pattern = Pattern.compile(Pattern.quote("@@") + "(.*?)" + Pattern.quote("@@"));
			Matcher matcher = pattern.matcher(data);

			List<String> matches = new ArrayList<>();
			while (matcher.find()) {
				String matchedText = matcher.group(1);
				matches.add(matchedText);
			}

			return matches.stream().map(CommuniqueRecipients::createNation).collect(Collectors.toList());

		} catch (IOException e) {
			throw new JTelegramException("Encountered IO exception when getting active nations", e);
		}
	}

}
