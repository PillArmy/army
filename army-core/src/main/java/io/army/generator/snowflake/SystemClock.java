/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.generator.snowflake;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


final class SystemClock {


    public static long now() {
        return Holder.INSTANCE.nowMills;
    }

    private volatile long nowMills;

    private volatile DatePair datePair;

    private SystemClock() {
        this.nowMills = System.currentTimeMillis();
    }

    private synchronized void updateNow() {
        this.nowMills = System.currentTimeMillis();
    }

    public static LocalDate nowDate() {
        DatePair pair;
        pair = Holder.INSTANCE.datePair;
        if (pair == null || now() > pair.boundaryTime) {
            pair = updateDatePair();
        }
        return pair.date;
    }

    private static DatePair updateDatePair() {

        final LocalDate now;
        now = LocalDate.now();

        final long boundaryTime;
        boundaryTime = now.plusDays(1)
                .atStartOfDay(ZoneOffset.systemDefault())
                .toInstant()
                .toEpochMilli();

        final DatePair pair;
        pair = new DatePair(boundaryTime, now);

        synchronized (SystemClock.class) {
            Holder.INSTANCE.datePair = pair;
        }
        return pair;
    }

    private static final class DatePair {

        private final long boundaryTime;

        private final LocalDate date;

        private DatePair(long boundaryTime, LocalDate date) {
            this.boundaryTime = boundaryTime;
            this.date = date;
        }
    }


    private static final class Holder {

        private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, SystemClock.class.getName());
            thread.setDaemon(true);
            return thread;
        });

        static final SystemClock INSTANCE;

        static {
            final SystemClock clock = new SystemClock();
            INSTANCE = clock;
            SCHEDULER.scheduleAtFixedRate(clock::updateNow, 1L, 1L, TimeUnit.MILLISECONDS);
        }

    }


}
