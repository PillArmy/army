package io.army.example.type.local;

import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;


public class MyLocalTest {

    public Range<Integer> int4RangeGuava;

    public Integer integer;

    @Test
    public void simple() {
        String text = "\"\"";

        System.out.println(text.substring(1, 1));
    }

    @Test
    public void range() throws Exception {
        Range<?> range = Range.singleton(0);
        System.out.println(range.hasLowerBound());
        System.out.println(range.hasUpperBound());
        System.out.println(range);
        System.out.println(range.isEmpty());

    }
}
