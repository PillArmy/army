package io.army.example.stock;

import org.jspecify.annotations.Nullable;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;

public abstract class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException();
    }

    public static String truncate(CharSequence charSequence, int threshold) {
        Assert.isTrue(threshold > 0,
                () -> "Truncation threshold must be a positive number: " + threshold);
        if (charSequence.length() > threshold) {
            return charSequence.subSequence(0, threshold).toString();
        }
        return charSequence.toString();
    }

    @Contract("null -> false")
    public static boolean hasText(@Nullable CharSequence str) {
        if (str == null) {
            return false;
        }

        int strLen = str.length();
        if (strLen == 0) {
            return false;
        }

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

}
