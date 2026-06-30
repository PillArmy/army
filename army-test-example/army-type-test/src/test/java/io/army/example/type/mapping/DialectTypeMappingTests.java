package io.army.example.type.mapping;

import com.google.common.collect.*;
import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.annotation.NewPostgreTypesId;
import io.army.example.type.domain.PostgreTypes_;
import io.army.executor.DataAccessException;
import io.army.mapping.MappingEnv;
import io.army.mapping.guava.GuavaRangeSetType;
import io.army.mapping.guava.GuavaRangeType;
import io.army.mapping.guava.array.GuavaRangeArrayType;
import io.army.mapping.guava.array.GuavaRangeSetArrayType;
import io.army.mapping.postgre.PgHstoreType;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
@Transactional
@Rollback
public class DialectTypeMappingTests {

    private static final Logger LOG = LoggerFactory.getLogger(DialectTypeMappingTests.class);

    @Test
    public void hstore(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {

        final PgHstoreType type = PgHstoreType.INSTANCE;

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
    public void hstoreError(@Autowired MappingEnv env) {

        final PgHstoreType type = PgHstoreType.INSTANCE;

        final DataType dataType = type.map(env.serverMeta());
        Map<String, String> map;
        Object bindValue, afterGetValue;

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


    @Test
    public void guavaRange(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final GuavaRangeType type = GuavaRangeType.fromTypeArg(Range.class, Integer.class);

        final DataType dataType;
        dataType = type.map(env.serverMeta());

        final List<Range<Integer>> list = new ArrayList<>();

        list.add(Range.all());
        list.add(Range.singleton(0));

        list.add(Range.closedOpen(0, 0));

        list.add(Range.upTo(0, BoundType.CLOSED));
        list.add(Range.upTo(0, BoundType.OPEN));


        list.add(Range.downTo(0, BoundType.CLOSED));
        list.add(Range.downTo(0, BoundType.OPEN));

        list.add(Range.range(0, BoundType.OPEN, 10, BoundType.OPEN));
        list.add(Range.range(0, BoundType.CLOSED, 10, BoundType.CLOSED));

        list.add(Range.range(-1, BoundType.OPEN, 10, BoundType.CLOSED));
        list.add(Range.range(-1, BoundType.CLOSED, 10, BoundType.OPEN));

        Object bindValue, afterGetValue;


        for (Range<Integer> range : list) {
            bindValue = type.beforeBind(dataType, env, range);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, range);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.int4RangeGuava, range.canonical(DiscreteDomain.integers()));
        }

    }

    @Test
    public void guavaRangeSet(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final GuavaRangeSetType type = GuavaRangeSetType.fromTypeArg(RangeSet.class, Integer.class);

        final DataType dataType;
        dataType = type.map(env.serverMeta());

        final List<RangeSet<Integer>> list = new ArrayList<>();

        RangeSet<Integer> rangeSet;

        list.add(TreeRangeSet.create());

        rangeSet = TreeRangeSet.create();
        rangeSet.add(Range.open(1, 3));
        list.add(rangeSet);


        rangeSet = TreeRangeSet.create();
        rangeSet.add(Range.openClosed(1, 1));
        rangeSet.add(Range.all());
        list.add(rangeSet);

        rangeSet = TreeRangeSet.create();
        rangeSet.add(Range.closedOpen(1, 1));
        rangeSet.add(Range.all());
        rangeSet.add(Range.singleton(0));
        list.add(rangeSet);


        Object bindValue, afterGetValue;

        for (RangeSet<Integer> set : list) {
            bindValue = type.beforeBind(dataType, env, set);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, set);

            rangeSet = TreeRangeSet.create();
            for (Range<Integer> range : rangeSet.asRanges()) {
                rangeSet.add(range.canonical(DiscreteDomain.integers()));
            }

            TestUtils.updateAndQuery(session, id, PostgreTypes_.int4RangeSetGuava, rangeSet);
        }


    }


    @Test
    public void guavaRangeLocalDateTime(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final GuavaRangeType type = GuavaRangeType.fromTypeArg(Range.class, LocalDateTime.class);

        final DataType dataType;
        dataType = type.map(env.serverMeta());

        final List<Range<LocalDateTime>> list = new ArrayList<>();

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime zeroValue = LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIDNIGHT);

        list.add(Range.all());
        list.add(Range.singleton(now));

        list.add(Range.closedOpen(zeroValue, zeroValue)); // empty

        list.add(Range.upTo(now, BoundType.CLOSED));
        list.add(Range.upTo(now, BoundType.OPEN));


        list.add(Range.downTo(now, BoundType.CLOSED));
        list.add(Range.downTo(now, BoundType.OPEN));

        list.add(Range.range(now, BoundType.OPEN, now.plusDays(10), BoundType.OPEN));
        list.add(Range.range(now, BoundType.CLOSED, now.plusDays(10), BoundType.CLOSED));

        list.add(Range.range(now.minusDays(1), BoundType.OPEN, now.plusDays(10), BoundType.CLOSED));
        list.add(Range.range(now.minusDays(1), BoundType.CLOSED, now.plusDays(10), BoundType.OPEN));

        Object bindValue, afterGetValue;


        for (Range<LocalDateTime> range : list) {
            bindValue = type.beforeBind(dataType, env, range);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, range);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.tsrange, range);
        }

    }


    @SuppressWarnings("unchecked")
    @Test
    public void tsrangeArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final GuavaRangeArrayType type = (GuavaRangeArrayType) PostgreTypes_.tsrangeArray.mappingType();

        final DataType dataType;
        dataType = type.map(env.serverMeta());

        final List<Range<LocalDateTime>[]> list = new ArrayList<>();

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime zeroValue = LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIDNIGHT);

        list.add(new Range[0]);

        list.add(new Range[]{Range.all()});
        list.add(new Range[]{Range.singleton(now)});

        list.add(new Range[]{Range.closedOpen(zeroValue, zeroValue)}); // empty

        list.add(new Range[]{Range.upTo(now, BoundType.CLOSED)});
        list.add(new Range[]{Range.upTo(now, BoundType.OPEN)});


        list.add(new Range[]{Range.downTo(now, BoundType.CLOSED)});
        list.add(new Range[]{Range.downTo(now, BoundType.OPEN)});

        list.add(new Range[]{Range.range(now, BoundType.OPEN, now.plusDays(10), BoundType.OPEN)});
        list.add(new Range[]{Range.range(now, BoundType.CLOSED, now.plusDays(10), BoundType.CLOSED)});

        list.add(new Range[]{Range.range(now.minusDays(1), BoundType.OPEN, now.plusDays(10), BoundType.CLOSED)});
        list.add(new Range[]{Range.range(now.minusDays(1), BoundType.CLOSED, now.plusDays(10), BoundType.OPEN)});

        final Range<LocalDateTime>[] array = new Range[list.size()];
        Range[] r;
        for (int i = 0; i < array.length; i++) {
            r = list.get(i);
            if (r.length > 0) {
                array[i] = r[0];
            }
        }

        list.add(array);


        Object bindValue, afterGetValue;


        for (Range<LocalDateTime>[] rangeArray : list) {
            bindValue = type.beforeBind(dataType, env, rangeArray);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, rangeArray);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.tsrangeArray, rangeArray);
        }

    }


    @SuppressWarnings("unchecked")
    @Test
    public void tsmultirangeArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final GuavaRangeSetArrayType type = (GuavaRangeSetArrayType) PostgreTypes_.tsmultirangeArray.mappingType();

        final DataType dataType;
        dataType = type.map(env.serverMeta());

        final List<RangeSet<LocalDateTime>[]> list = new ArrayList<>();

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime zeroValue = LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIDNIGHT);


        list.add(new RangeSet[0]);

        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.all())});
        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.singleton(now))});

        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.closedOpen(zeroValue, zeroValue))}); // empty

        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.upTo(now, BoundType.CLOSED))});
        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.upTo(now, BoundType.OPEN))});


        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.downTo(now, BoundType.CLOSED))});
        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.downTo(now, BoundType.OPEN))});

        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.range(now, BoundType.OPEN, now.plusDays(10), BoundType.OPEN))});
        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.range(now, BoundType.CLOSED, now.plusDays(10), BoundType.CLOSED))});

        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.range(now.minusDays(1), BoundType.OPEN, now.plusDays(10), BoundType.CLOSED))});
        list.add(new RangeSet[]{ImmutableRangeSet.of(Range.range(now.minusDays(1), BoundType.CLOSED, now.plusDays(10), BoundType.OPEN))});

        final RangeSet<LocalDateTime>[] array = new RangeSet[list.size()];
        RangeSet[] r;
        for (int i = 0; i < array.length; i++) {
            r = list.get(i);
            if (r.length == 0) {
                continue;
            }
            array[i] = r[0];

        }

        list.add(array);


        Object bindValue, afterGetValue;


        for (RangeSet<LocalDateTime>[] rangeArray : list) {
            bindValue = type.beforeBind(dataType, env, rangeArray);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, rangeArray);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.tsmultirangeArray, rangeArray);
        }

    }


}
