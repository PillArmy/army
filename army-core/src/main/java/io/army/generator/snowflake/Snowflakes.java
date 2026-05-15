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

import io.army.lang.Nullable;
import io.army.util._ResourceUtils;

import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public abstract class Snowflakes {

    private Snowflakes() {
        throw new UnsupportedOperationException();
    }

    public static final long START_TIME;

    private static final Snowflake DEFAULT_SNOWFLAKE;

    static {
        final Properties properties;
        properties = _ResourceUtils.loadArmyProperties(Snowflake.class.getSimpleName());
        final String key, startTime;
        key = Snowflakes.class.getName() + '.' + SnowflakeGenerator.START_TIME;
        startTime = properties.getProperty(key, "1776386333818");
        try {
            START_TIME = Long.parseLong(startTime);
        } catch (NumberFormatException e) {
            String m = String.format("%s config error", key);
            throw new RuntimeException(m, e);
        }
        DEFAULT_SNOWFLAKE = Snowflake.getInstance(START_TIME);
    }

    private static volatile Worker worker = Worker.ZERO;


    public static long next(final long startTime) {
        return Snowflake.getInstance(startTime)
                .next(worker, 1, null);
    }

    /// @param count    {@code >} 0, Efficient for count ≤ 4096; split into 4096 chunks if larger.
    /// @param consumer when count {@code >} 0, consumer can't be null
    public static void next(final long startTime, int count, @Nullable LongConsumer consumer) {
        Snowflake.getInstance(startTime)
                .next(worker, count, consumer);
    }


    public static String nextWithDate(final long startTime) {
        final long suffix;
        suffix = Snowflake.getInstance(startTime)
                .next(worker, 1, null);
        return SnowflakeGenerator.FORMATTER.format(SystemClock.nowDate()) + suffix;
    }

    /// @param count    {@code >} 0, Efficient for count ≤ 4096; split into 4096 chunks if larger.
    /// @param consumer non-null
    public static void nextWithDate(final long startTime, int count, Consumer<String> consumer) {
        Snowflake.getInstance(startTime)
                .nextWithDate(worker, count, null, null, consumer);
    }

    /// @param count    {@code >} 0, Efficient for count ≤ 4096; split into 4096 chunks if larger.
    /// @param consumer non-null
    public static void nextWithDate(final long startTime, int count, @Nullable String prefix,
                                    @Nullable String suffix, Consumer<String> consumer) {
        Snowflake.getInstance(startTime)
                .nextWithDate(worker, count, prefix, suffix, consumer);
    }


    public static long defaultNext() {
        return DEFAULT_SNOWFLAKE.next(worker, 1, null);
    }

    /// @param count    {@code >} 0, Efficient for count ≤ 4096; split into 4096 chunks if larger.
    /// @param consumer when count {@code >} 0, consumer can't be null
    public static long defaultNext(int count, @Nullable LongConsumer consumer) {
        return DEFAULT_SNOWFLAKE.next(worker, count, consumer);
    }

    public static String defaultNextWithDate() {
        final long suffix;
        suffix = DEFAULT_SNOWFLAKE.next(worker, 1, null);
        return SnowflakeGenerator.FORMATTER.format(SystemClock.nowDate()) + suffix;
    }

    /// @param count    {@code >} 0, Efficient for count ≤ 4096; split into 4096 chunks if larger.
    /// @param consumer non-null
    public static void defaultNextWithDate(int count, Consumer<String> consumer) {
        DEFAULT_SNOWFLAKE.nextWithDate(worker, count, null, null, consumer);
    }

    public static void defaultNextWithDate(int count, @Nullable String prefix, @Nullable String suffix, Consumer<String> consumer) {
        DEFAULT_SNOWFLAKE.nextWithDate(worker, count, prefix, suffix, consumer);
    }


    static synchronized void updateWorker(final Worker worker) {
        Snowflakes.worker = worker;
    }



}
