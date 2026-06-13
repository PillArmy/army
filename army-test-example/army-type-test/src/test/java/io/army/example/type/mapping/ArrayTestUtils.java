package io.army.example.type.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ArrayTestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ArrayTestUtils.class);

    private ArrayTestUtils() {
    }


    public static void printBindAndGetValue(final Object bindValue, final Object afterGetValue) {
        LOG.debug("beforeBind :\n{}\nafterGet :\n{}", bindValue, afterGetValue);
    }

    public static void printBindValue(final Object bindValue) {
        LOG.debug("beforeBind :\n{}", bindValue);
    }


}
