package io.army.example.type.mapping;

import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.annotation.NewPostgreTypesId;
import io.army.example.type.domain.PostgreTypes_;
import io.army.mapping.MappingEnv;
import io.army.mapping.array.IntegerArrayType;
import io.army.mapping.array.StringArrayType;
import io.army.meta.ServerMeta;
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

import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
@Transactional
@Rollback
public class BaseArrayMappingTests {

    private static final Logger LOG = LoggerFactory.getLogger(BaseArrayMappingTests.class);


    @Test
    public void intArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final ServerMeta serverMeta = env.serverMeta();

        IntegerArrayType type;

        DataType dataType;
        Object sourceValue, bindValue, afterGetValue;

        sourceValue = new int[]{1, 2, 3};
        type = IntegerArrayType.PRIMITIVE_LINEAR;

        dataType = type.map(serverMeta);
        bindValue = type.beforeBind(dataType, env, sourceValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);

        ArrayTestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        ArrayTestUtils.updateAndQuery(session, id, PostgreTypes_.intArray, afterGetValue);


        sourceValue = new int[][]{{1, 2, 3}, {1, 8, 9}};
        type = IntegerArrayType.from(int[][].class);

        dataType = type.map(serverMeta);
        bindValue = type.beforeBind(dataType, env, sourceValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);

        ArrayTestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        ArrayTestUtils.updateAndQuery(session, id, PostgreTypes_.int2dArray, afterGetValue);

    }

    @Test
    public void integerArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final ServerMeta serverMeta = env.serverMeta();

        IntegerArrayType type;

        DataType dataType;
        Object sourceValue, bindValue, afterGetValue;

        sourceValue = new Integer[]{1, 2, 3, null};
        type = IntegerArrayType.from(Integer[].class);

        dataType = type.map(serverMeta);
        bindValue = type.beforeBind(dataType, env, sourceValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);

        ArrayTestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        ArrayTestUtils.updateAndQuery(session, id, PostgreTypes_.integerArray, afterGetValue);


        sourceValue = new Integer[][]{{null, null, 3}, {null, null, null}, {2, 8, 3}};
        type = IntegerArrayType.from(Integer[][].class);

        dataType = type.map(serverMeta);
        bindValue = type.beforeBind(dataType, env, sourceValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);

        ArrayTestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        ArrayTestUtils.updateAndQuery(session, id, PostgreTypes_.integer2dArray, afterGetValue);


    }

    @Test
    public void stringArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final ServerMeta serverMeta = env.serverMeta();

        StringArrayType type;

        DataType dataType;
        Object sourceValue, bindValue, afterGetValue;

        sourceValue = new String[]{"1", "\"2\\\"", null, "{,sdf\"\\,}", "Don't create new word", null};
        type = StringArrayType.from(String[].class);

        dataType = type.map(serverMeta);
        bindValue = type.beforeBind(dataType, env, sourceValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);

        ArrayTestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        ArrayTestUtils.updateAndQuery(session, id, PostgreTypes_.textArray, afterGetValue);


        sourceValue = new String[][]{
                {null, null, "\"2\\\"", "", "3"},
                {null, null, null, null, null},
                {"", ",", "", "{", "}"},
                {"{,sdf\"\\,}", "\\", "", "", ""},
                {"Don't create new word", "123", "", "\"", "''''"}
        };
        type = StringArrayType.from(String[][].class);

        dataType = type.map(serverMeta);
        bindValue = type.beforeBind(dataType, env, sourceValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);

        ArrayTestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        ArrayTestUtils.updateAndQuery(session, id, PostgreTypes_.text2dArray, afterGetValue);

    }


}
