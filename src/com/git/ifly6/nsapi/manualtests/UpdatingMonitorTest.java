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

import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UpdatingMonitorTest extends CommUpdatingMonitor {

    private static final Logger LOGGER = Logger.getLogger(UpdatingMonitorTest.class.getName());

    private Random random = new Random();
    private List<Integer> integers = new ArrayList<>();

    @Override
    public List<String> getRecipients() {
        final List<String> stringList = integers.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        LOGGER.info(String.format("Called integers %s", stringList.toString()));
        return stringList;
    }

    @Override
    protected void updateAction() {
        // generate 15 random integers
        integers = IntStream
                .generate(() -> random.nextInt(100))
                .limit(5)
                .boxed()
                .collect(Collectors.toList());
        LOGGER.info("new random integers");
    }

    public static String prettyPrint(Map<Instant, ? extends Collection<String>> map) {
        return map.entrySet().stream()
                .map(pair -> pair.getKey().toString() + " : " + pair.getValue().toString())
                .collect(Collectors.joining("\n", "{", "}"));
    }

    public static void main(String[] args) throws InterruptedException {
        final String action = "change delay interval";

        System.out.println("Start update test");
        UpdatingMonitorTest mct = new UpdatingMonitorTest();
        mct.setUpdateInterval(Duration.ofSeconds(1));
        mct.start();

        System.out.println("Delay for start");
        Thread.sleep(10); // give time for instantiation...

        Map<Instant, List<String>> results = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            results.put(Instant.now(), mct.getRecipients());
            Thread.sleep(1005);
        }
        System.out.println(prettyPrint(results));

//        // test restart
//        System.out.println("Stopping for 5 seconds");
//        mct.stop();
//        Thread.sleep(5000);
//
//        System.out.println("Attempting restart");
//        mct.start();

        // test change update interval by restart
        mct.setUpdateInterval(Duration.of(15, ChronoUnit.SECONDS));

        Map<Instant, List<String>> restartResults = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            restartResults.put(Instant.now(), mct.getRecipients());
            Thread.sleep(1005);
        }
        System.out.println(prettyPrint(restartResults));
        mct.stop();

        System.out.println("Test successful");
    }
}
