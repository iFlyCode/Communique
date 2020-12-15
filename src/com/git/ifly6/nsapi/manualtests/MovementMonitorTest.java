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

import com.git.ifly6.nsapi.ctelegram.monitors.CommMovementDirection;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMovementMonitor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MovementMonitorTest {

    public static void main(String[] args) throws InterruptedException {
        List<String> regions = Arrays.asList("Europe");
        CommMovementMonitor movementMonitor = new CommMovementMonitor(
                regions, CommMovementDirection.EXIT, false);
        movementMonitor.setUpdateInterval(Duration.ofSeconds(60));
        movementMonitor.start();

        List<String> totalRecipients = new ArrayList<>();
        while (totalRecipients.size() < 10) {
            System.out.println("Waiting for data to come in...");
            Thread.sleep((long) (movementMonitor.getUpdateInterval().toMillis() * 1.2));

            System.out.println("Recipients:");
            List<String> recipients = movementMonitor.getRecipients();
            totalRecipients.addAll(recipients);

            System.out.println(recipients);
        }

        movementMonitor.stop();
        System.out.println("Stopped");
    }
}
