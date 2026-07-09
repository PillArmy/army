package io.army.example.type.local;

import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;


public class MyLocalTest {

    private static final Logger LOG = LoggerFactory.getLogger(MyLocalTest.class);

    public List<Map<String, Integer>> armyType;

    Map<String, Integer>[][] mapArray;

    List<List<Map<String, Integer>>> mapArrayList;


    @Test
    public void simpleTest() {
        OffsetDateTime now, tomorrow;
        now = OffsetDateTime.now(ZoneOffset.ofHours(8));
        tomorrow = now.plusDays(1);

        System.out.println(now.until(tomorrow, ChronoUnit.DAYS));
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
    public void range() {
        Range<?> range = Range.closedOpen(0, 0);
        System.out.println(range.hasLowerBound());
        System.out.println(range.hasUpperBound());
        System.out.println(range);
        System.out.println(range.isEmpty());

    }

    @Test
    public void pgConnect() throws Exception {
        String url = "jdbc:postgresql://localhost:5432/postgres?currentSchema=army_types,my_stock,public";

        try (Connection conn = DriverManager.getConnection(url, "army_w", "army123")) {

            try (Statement statement = conn.createStatement()) {

                //   statement.executeUpdate("SET bytea_output = 'escape'");

                String sql = "SELECT 'a fat cat sat on a mat and ate a fat rat'::tsvector";


                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    while (resultSet.next()) {
                        System.out.println(resultSet.getString(1));
                    }
                }
            }
        }
    }

}
