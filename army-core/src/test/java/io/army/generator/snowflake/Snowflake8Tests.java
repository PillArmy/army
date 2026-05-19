package io.army.generator.snowflake;

import io.army.util._Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;

public class Snowflake8Tests {

    private static final Logger LOG = LoggerFactory.getLogger(Snowflake8Tests.class);


    @Test
    public void next() {
        for (int i = 0; i < 10; i++) {
            System.out.println(Snowflake8s.next(1779014276675L));
        }


    }

    @Test(invocationCount = 30000, threadPoolSize = 10)
    public void defaultNextBatch() {
        final int count = Snowflake8.SEQUENCE_MASK;
        final long startNanoSecond = System.nanoTime();

        Snowflake8s.defaultNext(count, _ -> {
        });

        printTimeCost(startNanoSecond);
    }

    @Test(invocationCount = 30000, threadPoolSize = 6)
    public void defaultNext() {
        final int count = Snowflake8.SEQUENCE_MASK;
        final long startNanoSecond = System.nanoTime();

        for (int i = 0; i < count; i++) {
            Snowflake8s.defaultNext();
        }

        printTimeCost(startNanoSecond);
    }

    @Test(invocationCount = 30000, threadPoolSize = 6)
    public void defaultNextBatchSet() {
        final int count = 4096;

        final Set<Long> set = _Collections.hashSetForSize(count);

        final long startNanoSecond = System.nanoTime();

        Snowflake8s.defaultNext(count, set::add);

        Assert.assertEquals(set.size(), count);

        printTimeCost(startNanoSecond);
    }

    @Test(invocationCount = 30000, threadPoolSize = 6)
    public void defaultNextSet() {
        final int count = 4096;
        final Set<Long> set = _Collections.hashSetForSize(count);
        final long startNanoSecond = System.nanoTime();

        long value;
        for (int i = 0; i < count; i++) {
            value = Snowflake8s.defaultNext();
            set.add(value);
        }

        Assert.assertEquals(set.size(), count);

        printTimeCost(startNanoSecond);
    }


    @Test(invocationCount = 30000, threadPoolSize = 6)
    public void defaultNextBatch8192() {
        final int count = 8192;
        //  final List<Long> list = new ArrayList<>(count);
        final long startNanoSecond = System.nanoTime();

        // Snowflakes.defaultNext(count,System.out::println);
        Snowflake8s.defaultNext(count, _ -> {
        });

        printTimeCost(startNanoSecond);
    }

    @Test(invocationCount = 50000, threadPoolSize = 10)
    public void defaultNextBatch8192Set() {
        final int count = 8192;
        final Set<Long> set = _Collections.hashSetForSize(count);
        final long startNanoSecond = System.nanoTime();

        Snowflake8s.defaultNext(count, set::add);

        Assert.assertEquals(set.size(), count);

        printTimeCost(startNanoSecond);
    }

    @Test(invocationCount = 30000, threadPoolSize = 6)
    public void defaultNext8192() {
        final int count = 8192;
        //  final List<Long> list = new ArrayList<>(count);
        final long startNanoSecond = System.nanoTime();

        for (int i = 0; i < count; i++) {
            Snowflake8s.defaultNext();
        }

        // Snowflakes.defaultNext(count,list::add);

        printTimeCost(startNanoSecond);
    }

    @Test(invocationCount = 30000, threadPoolSize = 6)
    public void defaultNext8192Set() {
        final int count = 8192;
        final Set<Long> set = _Collections.hashSetForSize(count);
        final long startNanoSecond = System.nanoTime();

        long value;
        for (int i = 0; i < count; i++) {

            value = Snowflake8s.defaultNext();
            //  System.out.println(value);
            set.add(value);

        }

        Assert.assertEquals(set.size(), count);

        printTimeCost(startNanoSecond);
    }


    @Test(invocationCount = 40000, threadPoolSize = 10)
    public void defaultNextWithDate() {

        final int count = 8192;
        final Set<String> set = _Collections.hashSetForSize(count);
        final long startNanoSecond = System.nanoTime();

        //  Snowflakes.defaultNextWithDate(count,"8888","999",set::add);
        Snowflake8s.defaultNextWithDate(count, "xx", "bb", null, value -> {
            Assert.assertTrue(value.startsWith("20260515"));
            //  System.out.println(value);
            set.add(value);
        });

        Assert.assertEquals(set.size(), count);

        printTimeCost(startNanoSecond);
    }


    private void printTimeCost(final long startNanoSecond) {
        final long costNano, millis, micro, nano;
        costNano = System.nanoTime() - startNanoSecond;

        millis = costNano / 1000_000L;
        micro = (costNano % 1000_000L) / 1000L;
        nano = costNano % 1000L;
        LOG.info("Take: {} ms {} micro {} nano", millis, micro, nano);
    }


}
