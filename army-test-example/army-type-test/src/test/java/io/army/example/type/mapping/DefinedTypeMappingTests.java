package io.army.example.type.mapping;

import com.google.common.collect.*;
import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.annotation.NewPostgreTypesId;
import io.army.example.type.domain.ManagerInfo;
import io.army.example.type.domain.PostgreTypes_;
import io.army.example.type.domain.ProductInfo;
import io.army.example.type.domain.ProductInfo_;
import io.army.mapping.CompositeType;
import io.army.mapping.MappingEnv;
import io.army.mapping.array.CompositeArrayType;
import io.army.mapping.guava.GuavaRangeSetType;
import io.army.mapping.guava.GuavaRangeType;
import io.army.session.SyncSession;
import io.army.sqltype.DataType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
@Transactional
@Rollback
public class DefinedTypeMappingTests {

    private static final Logger LOG = LoggerFactory.getLogger(DefinedTypeMappingTests.class);

    @Test
    public void productInfo(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {

        final CompositeType type = ProductInfo_.T;

        final DataType dataType;
        dataType = type.map(env.serverMeta());

        Object bindValue, afterGetValue;

        for (ProductInfo info : createProductInfoList()) {
            bindValue = type.beforeBind(dataType, env, info);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, info);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.productInfo, info);

        }

    }

    @Test
    public void productInfoArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final CompositeArrayType type = CompositeArrayType.from(ProductInfo[].class);

        ProductInfo[] array;
        array = createProductInfoList().toArray(new ProductInfo[0]);

        final DataType dataType;
        dataType = type.map(env.serverMeta());

        Object bindValue, afterGetValue;

        bindValue = type.beforeBind(dataType, env, array);
        TestUtils.printBindValue(bindValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);
        TestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, array);

        TestUtils.updateAndQuery(session, id, PostgreTypes_.productInfoArray, array);

    }

    @Test
    public void managerInfo(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final CompositeType type = CompositeType.from(ManagerInfo.class);

        final DataType dataType;
        dataType = type.map(env.serverMeta());

        final List<ManagerInfo> list = new ArrayList<>();

        ManagerInfo source;

        source = new ManagerInfo()
                .setId(3L);

        list.add(source);

        source = new ManagerInfo()
                .setId(null);

        list.add(source);

        Object bindValue, afterGetValue;
        for (ManagerInfo info : list) {
            bindValue = type.beforeBind(dataType, env, info);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, info);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.managerInfo, info);
        }


    }

    @Test
    public void guavaRange(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final GuavaRangeType type = GuavaRangeType.fromTypeArg(Range.class, Integer.class);

        final DataType dataType;
        dataType = type.map(env.serverMeta());

        final List<Range<Integer>> list = new ArrayList<>();

        list.add(Range.all());
        list.add(Range.singleton(0));

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


    private static List<ProductInfo> createProductInfoList() {


        final int[] intArray = new int[]{1, 2, 3};
        final String[] textArray = new String[]{"a", null, "''\"\"army's,\\ok\\\\", "\\\"'qe,中国\\\"", null};

        final List<ProductInfo> list = new ArrayList<>();

        ProductInfo source;

        source = new ProductInfo()
                .setProductId(2L)
                .setAvailable(true)
                .setProductName("water‘")
                .setPrice(new BigDecimal("1.25"))
                .setReleaseDate(LocalDate.now())
                .setIntArray(intArray)
                .setTextArray(textArray)
                .setManagerInfo(new ManagerInfo().setId(2L))
        ;

        list.add(source);

        source = new ProductInfo()
                .setProductId(4L)
                .setAvailable(true)
                .setProductName("")   // empty string
                .setPrice(new BigDecimal("1.25"))
                .setReleaseDate(LocalDate.now())
                .setIntArray(intArray)
                .setTextArray(textArray)
                .setManagerInfo(new ManagerInfo().setId(23223L))
        ;

        list.add(source);


        source = new ProductInfo()
                .setProductId(null)
                .setAvailable(null)
                .setProductName(null)
                .setPrice(null)
                .setReleaseDate(null)
                .setIntArray(null)
                .setTextArray(null)
                .setManagerInfo(null);

        list.add(source);

        source = new ProductInfo()
                .setProductId(4L)
                .setAvailable(null)
                .setProductName("\"\"tea\"\"")
                .setPrice(new BigDecimal("1.25"))
                .setReleaseDate(null)
                .setIntArray(intArray)
                .setTextArray(textArray)
                .setManagerInfo(new ManagerInfo().setId(null));

        list.add(source);

        source = new ProductInfo()
                .setProductId(5L)
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
