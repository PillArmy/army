package io.army.example.type.local;

import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
        Range<Integer> range = Range.openClosed(1, 1);
        System.out.println(range.isEmpty());

        Field field;
        field = getClass().getField("integer");

        Type type;
        type = field.getGenericType();
        System.out.println(type instanceof ParameterizedType);

    }
}
