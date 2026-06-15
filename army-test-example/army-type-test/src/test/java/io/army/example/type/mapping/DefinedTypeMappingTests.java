package io.army.example.type.mapping;

import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.annotation.NewPostgreTypesId;
import io.army.example.type.domain.ManagerInfo;
import io.army.example.type.domain.PostgreTypes_;
import io.army.example.type.domain.ProductInfo;
import io.army.mapping.CompositeType;
import io.army.mapping.MappingEnv;
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

        final CompositeType type = CompositeType.from(ProductInfo.class);

        final DataType dataType;
        dataType = type.map(env.serverMeta());

        final int[] intArray = new int[]{1, 2, 3};
        final String[] textArray = new String[]{"a", null, "''\"\"army's,\\ok\\\\", "\\\"'qe,中国\\\"", null};

        ProductInfo source;
        Object bindValue, afterGetValue;

        final List<ProductInfo> list = new ArrayList<>();

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
                .setTextArray(null)
                .setManagerInfo(null); // last value must be null

        list.add(source);


        for (ProductInfo info : list) {
            bindValue = type.beforeBind(dataType, env, info);
            TestUtils.printBindValue(bindValue);
            afterGetValue = type.afterGet(dataType, env, bindValue);
            TestUtils.printBindAndGetValue(bindValue, afterGetValue);
            Assert.assertEquals(afterGetValue, info);

            TestUtils.updateAndQuery(session, id, PostgreTypes_.productInfo, info);


        }


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


}
