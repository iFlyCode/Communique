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

package com.git.ifly6.communique.io;

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides functionality to Communique to easily scrape pertinent information from the NationStates World Assembly
 * pages in line with the script rules.
 * @author ifly6
 */
public class CommuniqueScraper {

    public static final String GA = "https://www.nationstates.net/page=UN_delegate_votes/council=1";
    public static final String SC = "https://www.nationstates.net/page=UN_delegate_votes/council=2";
    public static final String FOR = "For:";
    public static final String AGAINST = "Against:";

    private static final Logger LOGGER = Logger.getLogger(CommuniqueScraper.class.getName());

    /**
     * Makes sure that the many different instances have to compete for a single API call which is regulated to every
     * 1650 milliseconds.
     */
    private static synchronized void rateLimit() {
        try {
            Thread.sleep(1_650);
        } catch (InterruptedException e) {
            System.err.println("Rate limit was interrupted.");
        }
    }

    /**
     * Provides pertinent rate-limiting for web-scraping in line with the NationStates scripting rules.
     * @param url at which to scrape HTML
     * @return list containing the HTML
     * @throws IOException if there is an error in finding the data
     */
    private static String callUrl(URL url) throws IOException {
        LOGGER.info("Implementing scraper rate-limit");
        rateLimit();

        LOGGER.info(String.format("Calling url: %s", url));
        URLConnection connection = url.openConnection();
        connection.setRequestProperty(
                "User-Agent",
                "Communique, maintained by Imperium Anglorum, cyrilparsons.london@gmail.com"
        );

        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
        return new BufferedReader(isr).lines().collect(Collectors.joining("\n"));
    }

    /**
     * Attempts to scrape the list of delegates voting for or against some proposal.
     * @param chamber, either GA or SC
     * @param side,    either FOR or AGAINST
     * @return list of applicable delegate reference names; if failure, empty list.
     */
    public static List<CommuniqueRecipient> importAtVoteDelegates(String chamber, String side) {
        try {
            Document doc = Jsoup.parse(callUrl(new URL(chamber)));  // rate-limited call
            System.out.println("doc:\t" + doc.html());

            Element divContent = doc.select("div#content").first();
            Elements bolded = divContent.select("b");

            for (Element element : bolded) {
                String s = element.parent().text().replaceAll("\\(.+?\\)", ""); // get rid of brackets
                if (s.contains("No Resolution At Vote")) throw new NoResolutionException();
                if (s.startsWith(side)) {
                    s = s.replace(side, "");
                    System.out.println("data1:\t" + s);

                    s = s.substring(s.indexOf(":"));
                    System.out.println("data2:\t" + s);

                    s = s.substring(s.indexOf(":") + 1, s.indexOf(" , and  individual member nations."));
                    System.out.println("data3:\t" + s);

                    return Arrays.stream(s.split(",\\s*?"))
                            .map(String::trim)
                            .filter(str -> !str.isEmpty())
                            .map(CommuniqueRecipients::createNation)
                            .collect(Collectors.toList());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.warning("Got 0 recipients.");
        return Collections.emptyList();    // return empty list
    }
}