package io.army.example.coder.utils;


import org.springframework.util.Assert;

/// String utility class extending Spring's `StringUtils` with additional truncation support.
///
/// <p>Provides the `myTruncate` method for safely truncating strings
/// to a maximum character length, commonly used for generating conversation titles.</p>
public abstract class StringUtils extends org.springframework.util.StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException();
    }

    public static String myTruncate(CharSequence charSequence, int threshold) {
        Assert.isTrue(threshold > 0,
                () -> "Truncation threshold must be a positive number: " + threshold);
        if (charSequence.length() > threshold) {
            return charSequence.subSequence(0, threshold).toString();
        }
        return charSequence.toString();
    }


}
