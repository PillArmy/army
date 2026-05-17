package io.army.example.stock;

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


}
