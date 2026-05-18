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

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.LongConsumer;


/// Snowflake algorithm with 8-bit machine ID
public final class Snowflake8 {


    public static Snowflake8 getInstance(final long startTime) {
        if (startTime < 0) {
            throw new IllegalArgumentException("startTime must great than or equals 0");
        }
        return INSTANCE_MAP.computeIfAbsent(startTime, Snowflake8::new);
    }


    private static final ConcurrentMap<Long, Snowflake8> INSTANCE_MAP = new ConcurrentHashMap<>();


    /// 8-bit machine ID
    public static final byte WORKER_BIT_SIZE = 8;

    /// bit number of sequence id
    public static final byte SEQUENCE_BITS = 12;

    /// bit number that timestamp left shift
    public static final byte TIMESTAMP_LEFT_SHIFT = WORKER_BIT_SIZE + SEQUENCE_BITS;

    public static final long TIMESTAMP_MASK = ~(-1L << (63 - TIMESTAMP_LEFT_SHIFT));

    /// max value of sequence
    public static final int SEQUENCE_MASK = ~(-1 << SEQUENCE_BITS);

    public static final int WORKER_ID_MASK = ~(-1 << WORKER_BIT_SIZE);

    public static final int MAX_ACCEPT_BACKWARD_MS;

    static {
        final Properties properties;
        properties = _ResourceUtils.loadArmyProperties(Snowflake8.class.getSimpleName());
        final String key, value;
        key = Snowflake8.class.getName() + '.' + "max_accept_backward_ms";
        value = properties.getProperty(key, "20");

        try {
            MAX_ACCEPT_BACKWARD_MS = Integer.parseInt(value);

            if (MAX_ACCEPT_BACKWARD_MS < 0 || MAX_ACCEPT_BACKWARD_MS > 1000) {
                String m = String.format("%s config error", key);
                throw new RuntimeException(m);
            }
        } catch (NumberFormatException e) {
            String m = String.format("%s config error", key);
            throw new RuntimeException(m, e);
        }
    }


    public final long startTime;

    /// (0~4095)
    private long sequence = -1L;

    private long lastTimestamp;


    private Snowflake8(final long startTime) {
        this.startTime = startTime;
        this.lastTimestamp = System.currentTimeMillis(); // don't use  SystemClock.now();
    }

    /// @param count    {@code >} 0, Efficient for count ≤ 4096; split into 4096 chunks if larger.
    /// @param consumer when count {@code >} 0, consumer can't be null
    public long next(final long workerId, final int count, final @Nullable LongConsumer consumer) {
        validateArgs(workerId, count, consumer);

        final long[] array;
        if (consumer == null) {
            array = null;
        } else {
            array = new long[count];
        }

        long lastId = 0;

        synchronized (this) {
            long lastTimestamp, timestamp, sequence, timeBits;
            lastTimestamp = this.lastTimestamp;
            sequence = this.sequence;
            timestamp = nowTimestamp(lastTimestamp);

            if (timestamp != lastTimestamp) {
                sequence = -1L;
            }

            for (int i = 0; i < count; i++) {
                sequence++;
                if ((sequence & SEQUENCE_MASK) == 0) {
                    sequence = 0L;
                    timestamp = nowTimestamp(timestamp); // use timestamp
                    if (timestamp == lastTimestamp) {
                        timestamp = waitClock(lastTimestamp, timestamp);
                    }
                    lastTimestamp = timestamp;
                }

                timeBits = ((timestamp - this.startTime) & TIMESTAMP_MASK) << TIMESTAMP_LEFT_SHIFT;

                lastId = timeBits
                        | (workerId << SEQUENCE_BITS)
                        | sequence;

                if (array != null) {
                    array[i] = lastId;
                }

            } // loop

            this.lastTimestamp = timestamp;
            this.sequence = sequence;

        } // synchronized


        if (consumer != null) {
            for (int i = 0; i < count; i++) {
                consumer.accept(array[i]);
            }
        }
        return lastId;
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.startTime);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof Snowflake8 s) {
            match = s.startTime == this.startTime;
        } else {
            match = false;
        }
        return match;
    }


    @Override
    public String toString() {
        return String.format("[%s startTime:%s]", Snowflake8.class.getName(), this.startTime);
    }


    private long nowTimestamp(final long lastTimestamp) {
        long timestamp;
        timestamp = SystemClock.now();
        if (timestamp >= lastTimestamp) {
            return timestamp;
        }
        // Avoid misjudging clock rollback
        // SystemClock.now() reduces, rather than eliminates, calls to System.currentTimeMillis().
        timestamp = System.currentTimeMillis();
        if (timestamp >= lastTimestamp) {
            return timestamp;
        }
        return waitClock(lastTimestamp, timestamp);
    }

    private long waitClock(final long lastTimestamp, long timestamp) {
        long diff = lastTimestamp - timestamp;
        if (diff > MAX_ACCEPT_BACKWARD_MS) {
            throw clockRollback(diff);
        }

        while (true) {
            timestamp = System.currentTimeMillis();
            if (timestamp > lastTimestamp) {
                break;
            }
            if (timestamp < lastTimestamp) {
                diff = lastTimestamp - timestamp;
                if (diff > MAX_ACCEPT_BACKWARD_MS) {
                    throw clockRollback(diff);
                }
            }

            Thread.onSpinWait();
        } // loop
        return timestamp;
    }


    private static void validateArgs(final long workerId, final int count, final @Nullable LongConsumer consumer) {

        if (workerId < 0 || workerId > WORKER_ID_MASK) {
            String m = String.format("Worker id[%s] couldn't be greater than %s or less than 0", workerId, WORKER_ID_MASK);
            throw new IllegalArgumentException(m);
        }
        if (count < 1) {
            throw new IllegalArgumentException(String.format("%s[%s] error", "count", count));
        } else if (count > 1 && consumer == null) {
            throw new IllegalArgumentException(String.format("%s[%s] and %s not match", "count", count, "consumer"));
        }
    }


    private static IllegalStateException clockRollback(long diff) {
        String m = "Clock rollback too severe, ID generation rejected! Rollback milliseconds: " + diff;
        return new IllegalStateException(m);
    }


}
