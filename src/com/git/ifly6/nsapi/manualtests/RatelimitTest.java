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

package com.git.ifly6.nsapi.manualtests;

import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.NSIOException;

import java.io.IOException;

public class RatelimitTest {

    public static void main(String[] args) throws IOException {

        /* This section here purposefully creates an API rate limit error. For some reason, you have to read the data
         * or it doesn't actually connect. I guess that's Java trying to save you network calls without explicit
         * instructions. */
//		for (int i = 0; i < 51; i++) {
//			URL url = new URL(NSConnection.API_PREFIX + "q=newnations");
//			HttpURLConnection apiConnection = (HttpURLConnection) url.openConnection();
//			apiConnection.connect();
//
//			BufferedReader reader = new BufferedReader(new InputStreamReader(apiConnection.getInputStream()));
//			String xml_raw = reader.lines().collect(Collectors.joining("\n"));
//			reader.close();
//
//			System.out.println(xml_raw);
//			System.out.println("Called API " + i + " of 51.");
//		}
//
//		System.out.println(NSConnection.API_PREFIX + "q=newnations");

        /*
         * This section logs the start time and the time delta between connections. For the JInfoFetcher equivalent
         * code, it should operate around every 700 milliseconds, which is above the 610 ms API cut off.
         */
        final int MAX = 100;
        long startTime = System.currentTimeMillis();
        long lastTime = startTime;

        for (int i = 0; i < MAX; i++) {

            try {
                NSConnection connection = new NSConnection(NSConnection.API_PREFIX + "q=newnations");
                connection.connect();
            } catch (NSIOException e) {
                // pass
            }

            System.out.printf("Tried to connect %d of %d \t time %d, %d delta%n", i, MAX,
                    System.currentTimeMillis() - startTime, System.currentTimeMillis() - lastTime);
            lastTime = System.currentTimeMillis();
        }
    }

}
