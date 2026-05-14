package io.army.generator.snowflake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class SnowflakeTests {

    private static final Logger LOG = LoggerFactory.getLogger(SnowflakeTests.class);

    @Test(invocationCount = 30000, threadPoolSize = 5)
    public void dateTime() {
        final int count = 4096;
        //  final List<Long> list = new ArrayList<>(count);
        final long startNanoSecond = System.nanoTime();

        Snowflakes.defaultNext(count, _ -> {
        });
        // Snowflakes.defaultNext(count,list::add);

        final long costNano, millis, micro, nano;
        costNano = System.nanoTime() - startNanoSecond;

        millis = costNano / 1000_000L;
        micro = (costNano % 1000_000L) / 1000L;
        nano = costNano % 1000L;
        LOG.info("Take: {} ms {} micro {} nano", millis, micro, nano);
    }


}
