/* Copyright (c) 2016 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.git.ifly6.communique.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.git.ifly6.communique.CommuniqueUtilities;

/**
 * @author Kevin
 *
 */
public class CNetLoader {
	
	public static final String GA = "http://www.nationstates.net/page=UN_delegate_votes/council=1";
	public static final String SC = "http://www.nationstates.net/page=UN_delegate_votes/council=2";
	public static final String FOR = "Votes For:";
	public static final String AGAINST = "Votes Against:";
	
	private static synchronized List<String> callUrl(URL url) throws IOException {
		
		try {
			Thread.sleep(610);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("User-Agent", "Communique, maintained by Imperium Anglorum, ifly6@me.com");
		List<String> output = IOUtils.readLines(new InputStreamReader(connection.getInputStream()));
		
		return output;
	}
	
	public static String[] importAtVoteDelegates(String chamber, String side) {
		
		try {
			
			Document doc = Jsoup.parse(CommuniqueUtilities.joinListWith(callUrl(new URL(chamber)), '\n'));
			Elements elements = doc.select("div.widebox td.UN p");
			
			Iterator<Element> eIter = elements.listIterator();
			while (eIter.hasNext()) {
				Element element = eIter.next();
				
				try {
					Element strong = element.select("strong").get(0);
					String text = strong.text();
					
					if (text.startsWith(side)) {
						
						String data = element.text().replace(side, "").replaceAll("\\(.+?\\)", "");
						data = data.substring(data.indexOf(":") + 1, data.indexOf("and  individual WA member nations."));
						
						String[] delegates = data.split(",");
						for (int i = 0; i < delegates.length; i++) {
							delegates[i] = delegates[i].trim().toLowerCase().replace(" ", "_");
						}
						
						return delegates;
						
					}
					
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
			}
			
		} catch (IOException e) {
			// Do nothing and return null.
		}
		
		return null;
	}
	
}
