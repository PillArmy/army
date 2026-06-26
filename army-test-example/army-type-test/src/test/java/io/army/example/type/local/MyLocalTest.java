package io.army.example.type.local;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import io.army.mapping.MappingType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


public class MyLocalTest {


    public List<Map<String, Integer>> armyType;

    Map<String, Integer>[][] mapArray;

    List<List<Map<String, Integer>>> mapArrayList;


    @Test
    public void simpleTest() {
        System.out.println(MappingType.SqlJson.class.getName());
    }


    @Test
    public void simple() throws Exception {
        Field field;
        field = getClass().getDeclaredField("mapArrayList");

        Type type = field.getGenericType();
        if (!(type instanceof ParameterizedType pt)) {
            throw new IllegalArgumentException();
        }

        System.out.println(pt.getRawType() instanceof Class<?>);
        System.out.println(pt.getRawType().getTypeName());

//        Type[] typeArray;
//        typeArray =   pt.getActualTypeArguments();
//
//        GenericArrayType arrayType;
//        arrayType =  (GenericArrayType)typeArray[0];
//        System.out.println( arrayType.getTypeName());

    }

    @Test
    public void range() throws Exception {
        Range<?> range = Range.downTo(0, BoundType.CLOSED);
        System.out.println(range.hasLowerBound());
        System.out.println(range.hasUpperBound());
        System.out.println(range);
        System.out.println(range.isEmpty());

    }
}
