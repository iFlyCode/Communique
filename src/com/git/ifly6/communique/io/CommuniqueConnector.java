/* Copyright (c) 2017 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;

/** Provides functionality to Communique to easily scrape pertinent information from the NationStates World Assembly
 * pages in line with the script rules.
 * @author ifly6 */
public class CommuniqueConnector {
	
	public static final String GA = "http://www.nationstates.net/page=UN_delegate_votes/council=1";
	public static final String SC = "http://www.nationstates.net/page=UN_delegate_votes/council=2";
	public static final String FOR = "Votes For:";
	public static final String AGAINST = "Votes Against:";
	
	/** Provides pertinent rate-limiting for web-scraping in line with the NationStates scripting rules.
	 * @param url at which to scrape HTML
	 * @return list containing the HTML
	 * @throws IOException if there is an error in finding the data */
	public static synchronized String callUrl(URL url) throws IOException {
		
		try {
			Thread.sleep(610);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("User-Agent", "Communique, maintained by Imperium Anglorum, ifly6@me.com");
		InputStreamReader isr = new InputStreamReader(connection.getInputStream());
		return new BufferedReader(isr).lines().collect(Collectors.joining("\n"));
	}
	
	/** Attempts to scrape the list of delegates voting for or against some proposal.
	 * @param chamber, either GA or SC
	 * @param side, either FOR or AGAINST
	 * @return list of applicable delegate reference names; if failure, empty list. */
	public static List<CommuniqueRecipient> importAtVoteDelegates(String chamber, String side) {
		
		try {
			
			Document doc = Jsoup.parse(callUrl(new URL(chamber)));
			Elements elements = doc.select("div.content");
			
			Iterator<Element> eIter = elements.listIterator();
			while (eIter.hasNext()) {
				Element element = eIter.next();
				try {
					Element strong = element.select("strong").get(0);
					String text = strong.text();
					if (text.startsWith(side)) {
						String data = element.text().replace(side, "").replaceAll("\\(.+?\\)", "");
						data = data.substring(data.indexOf(":") + 1, data.indexOf("and  individual WA member nations."));
						return Stream.of(data.split(","))
								.map(s -> CommuniqueRecipients.createNation(s))
								.collect(Collectors.toList());
					}
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
			}
			
		} catch (IOException e) {}
		return new ArrayList<>(0);	// return empty list
	}
	
}
