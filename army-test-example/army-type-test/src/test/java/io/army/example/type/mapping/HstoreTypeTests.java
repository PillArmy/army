package io.army.example.type.mapping;

import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.annotation.NewPostgreTypesId;
import io.army.example.type.domain.HstorePojo;
import io.army.example.type.domain.ManagerInfo;
import io.army.example.type.domain.PostgreTypes_;
import io.army.executor.DataAccessException;
import io.army.mapping.MappingEnv;
import io.army.mapping.postgre.PgHstoreType;
import io.army.mapping.postgre.array.PgHstoreArrayType;
import io.army.session.SyncSession;
import io.army.sqltype.DataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
@Transactional
@Rollback
public class HstoreTypeTests {

    private static final Logger LOG = LoggerFactory.getLogger(HstoreTypeTests.class);

    @Test
    public void hstore(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {

        final PgHstoreType type = (PgHstoreType) PostgreTypes_.hstore.mappingType();

        final DataType dataType = type.map(env.serverMeta());
        Map<String, String> map;
        Object bindValue, afterGetValue;

        final String text = "\"\"\"army's zoro\\\\\"\"";

        final List<Map<String, String>> list = new ArrayList<>();

        list.add(Map.of());

        list.add(Map.of(text, text));

        map = Map.of(
                "a", "null",
                "b", "a",
                "c", "",
                "army", text,
                text, text
        );
        list.add(map);

        map = new HashMap<>();
        map.put("a", null);
        list.add(map);

        map = new HashMap<>();
        map.put("a", text);
        map.put("army", null);
        map.put(text, null);
        list.add(map);


        for (Map<String, String> source : list) {
            bindValue = type.beforeBind(dataType, env, source);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, source);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.hstore, source);

        }

    }


    @Test
    public void hstoreInt(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {

        final PgHstoreType type = (PgHstoreType) PostgreTypes_.hstoreInt.mappingType();

        final DataType dataType = type.map(env.serverMeta());
        Map<String, Integer> map;
        Object bindValue, afterGetValue;

        final String text = "\"\"\"army's zoro\\\\\"\"";

        final List<Map<String, Integer>> list = new ArrayList<>();

        list.add(Map.of());

        list.add(Map.of(text, 0));

        map = Map.of(
                "a", 1,
                "b", 0,
                "c", Integer.MAX_VALUE,
                "army", Integer.MIN_VALUE,
                text, -1
        );
        list.add(map);

        map = new HashMap<>();
        map.put("a", null);
        list.add(map);

        map = new HashMap<>();
        map.put("a", 0);
        map.put("army", null);
        map.put(text, null);
        list.add(map);


        for (Map<String, Integer> source : list) {
            bindValue = type.beforeBind(dataType, env, source);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, source);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.hstoreInt, source);

        }

    }

    @Test
    public void hstoreEnumMap(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {

        final PgHstoreType type = (PgHstoreType) PostgreTypes_.dayOfWeekStringEnumMap.mappingType();

        final DataType dataType = type.map(env.serverMeta());
        EnumMap<DayOfWeek, String> map;
        Object bindValue, afterGetValue;

        final String text = "\"\"\"army's zoro\\\\\"\"";

        final List<EnumMap<DayOfWeek, String>> list = new ArrayList<>();

        list.add(new EnumMap<>(DayOfWeek.class));

        map = new EnumMap<>(DayOfWeek.class);
        map.put(DayOfWeek.MONDAY, text);
        map.put(DayOfWeek.FRIDAY, DayOfWeek.FRIDAY.name());


        list.add(map);

        map = new EnumMap<>(DayOfWeek.class);
        map.put(DayOfWeek.FRIDAY, null);

        list.add(map);

        map = new EnumMap<>(DayOfWeek.class);
        map.put(DayOfWeek.FRIDAY, text);

        list.add(map);


        for (EnumMap<DayOfWeek, String> source : list) {
            bindValue = type.beforeBind(dataType, env, source);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, source);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.dayOfWeekStringEnumMap, source);

        }
    }

    @Test
    public void hstorePojo(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final PgHstoreType type = (PgHstoreType) PostgreTypes_.hstorePojo.mappingType();
        final DataType dataType = type.map(env.serverMeta());

        Object bindValue, afterGetValue;
        for (HstorePojo source : createHstorePojoList()) {
            bindValue = type.beforeBind(dataType, env, source);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, source);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.hstorePojo, source);

        }
    }


    @SuppressWarnings("unchecked")
    @Test
    public void hstoreArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {

        final PgHstoreArrayType type = (PgHstoreArrayType) PostgreTypes_.hstoreArray.mappingType();

        final DataType dataType = type.map(env.serverMeta());
        Map<String, String> map;
        Object bindValue, afterGetValue;

        final String text = "\"\"\"army's zoro\\\\\"\"";

        final List<Map<String, String>[]> list = new ArrayList<>();

        list.add(new Map[]{Map.of()});

        list.add(new Map[]{Map.of(text, text)});

        map = Map.of(
                "a", "null",
                "b", "a",
                "c", "",
                "army", text,
                text, text
        );
        list.add(new Map[]{map});

        map = new HashMap<>();
        map.put("a", null);
        list.add(new Map[]{map});

        map = new HashMap<>();
        map.put("a", text);
        map.put("army", null);
        map.put(text, null);
        list.add(new Map[]{map, Map.of(text, text)});


        for (Map<String, String>[] source : list) {
            bindValue = type.beforeBind(dataType, env, source);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, source);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.hstoreArray, source);

        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void hstoreEnumMapArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {

        final PgHstoreArrayType type = (PgHstoreArrayType) PostgreTypes_.dayOfWeekStringEnumMapArray.mappingType();

        final DataType dataType = type.map(env.serverMeta());
        EnumMap<DayOfWeek, String> map, map2;
        Object bindValue, afterGetValue;

        final String text = "\"\"\"army's zoro\\\\\"\"";

        final List<EnumMap<DayOfWeek, String>[]> list = new ArrayList<>();

        list.add(new EnumMap[]{new EnumMap<>(DayOfWeek.class)});

        map2 = map = new EnumMap<>(DayOfWeek.class);
        map.put(DayOfWeek.MONDAY, text);
        map.put(DayOfWeek.FRIDAY, DayOfWeek.FRIDAY.name());


        list.add(new EnumMap[]{map});

        map = new EnumMap<>(DayOfWeek.class);
        map.put(DayOfWeek.FRIDAY, null);

        list.add(new EnumMap[]{map});

        map = new EnumMap<>(DayOfWeek.class);
        map.put(DayOfWeek.FRIDAY, text);

        list.add(new EnumMap[]{map, map2});


        for (EnumMap<DayOfWeek, String>[] source : list) {
            bindValue = type.beforeBind(dataType, env, source);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, source);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.dayOfWeekStringEnumMapArray, source);

        }
    }


    @Test
    public void hstorePojoArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final PgHstoreArrayType type = (PgHstoreArrayType) PostgreTypes_.hstorePojoArray.mappingType();
        final DataType dataType = type.map(env.serverMeta());

        final List<HstorePojo> pojoList = createHstorePojoList();

        final List<HstorePojo[]> list = new ArrayList<>();

        list.add(new HstorePojo[0]);

        list.add(new HstorePojo[]{pojoList.getFirst()});

        list.add(new HstorePojo[]{pojoList.getLast()});

        list.add(pojoList.toArray(new HstorePojo[0]));


        Object bindValue, afterGetValue;
        for (HstorePojo[] source : list) {
            bindValue = type.beforeBind(dataType, env, source);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, source);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.hstorePojoArray, source);

        }
    }

    @Test
    public void hstoreError(@Autowired MappingEnv env) {

        final PgHstoreType type = (PgHstoreType) PostgreTypes_.hstore.mappingType();

        final DataType dataType = type.map(env.serverMeta());

        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, Map.of(1, ""));
        });

        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, Map.of("a", LocalDate.now()));
        });


        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, "null=>1");
        });


        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, ",");
        });


        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, "a");
        });

        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, "\"a\"");
        });

        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, "\"a\"=>7 b=>8");
        });

        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, "\"a\"=>7 =>");
        });

        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, "\"a\"=>7,b=>8=>");
        });

        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, "=>");
        });

        Assertions.assertThrowsExactly(DataAccessException.class, () -> {
            type.afterGet(dataType, env, "\"a\"=>7,b=>8,");
        });


    }


    private static List<HstorePojo> createHstorePojoList() {


        final int[] intArray = new int[]{1, 2, 3};
        final String[] textArray = new String[]{"a", null, "''\"\"army's,\\ok\\\\", "\\\"'qe,中国\\\"", null};

        final List<HstorePojo> list = new ArrayList<>();

        HstorePojo source;

        source = new HstorePojo();
        source.setProductId(2L)
                .setAvailable(true)
                .setProductName("water‘")
                .setPrice(new BigDecimal("1.25"))
                .setReleaseDate(LocalDate.now())
                .setIntArray(intArray)
                .setTextArray(textArray)
                .setManagerInfo(new ManagerInfo().setId(2L))
        ;

        list.add(source);

        source = new HstorePojo();
        source.setProductId(4L)
                .setAvailable(true)
                .setProductName("")   // empty string
                .setPrice(new BigDecimal("1.25"))
                .setReleaseDate(LocalDate.now())
                .setIntArray(intArray)
                .setTextArray(textArray)
                .setManagerInfo(new ManagerInfo().setId(23223L))
        ;

        list.add(source);


        source = new HstorePojo();
        source.setProductId(null)
                .setAvailable(null)
                .setProductName(null)
                .setPrice(null)
                .setReleaseDate(null)
                .setIntArray(null)
                .setTextArray(null)
                .setManagerInfo(null);

        list.add(source);

        source = new HstorePojo();
        source.setProductId(4L)
                .setAvailable(null)
                .setProductName("\"\"tea\"\"")
                .setPrice(new BigDecimal("1.25"))
                .setReleaseDate(null)
                .setIntArray(intArray)
                .setTextArray(textArray)
                .setManagerInfo(new ManagerInfo().setId(null));

        list.add(source);

        source = new HstorePojo();
        source.setProductId(5L)
                .setAvailable(Boolean.TRUE)
                .setProductName("tea")
                .setPrice(new BigDecimal("1.25"))
                .setReleaseDate(LocalDate.now())
                .setIntArray(intArray)
                .setTextArray(textArray)
                .setManagerInfo(null); // last value must be null

        list.add(source);
        return list;
    }


}
