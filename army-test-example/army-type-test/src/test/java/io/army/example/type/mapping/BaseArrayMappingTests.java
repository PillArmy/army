package io.army.example.type.mapping;

import io.army.criteria.CriteriaException;
import io.army.criteria.Select;
import io.army.criteria.dialect.DmlCommand;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.annotation.NewPostgreTypesId;
import io.army.example.type.domain.PostgreTypes;
import io.army.example.type.domain.PostgreTypes_;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.StringType;
import io.army.mapping.array.BinaryArrayType;
import io.army.mapping.array.IntegerArrayType;
import io.army.mapping.array.StringArrayType;
import io.army.meta.FieldMeta;
import io.army.meta.ServerMeta;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

        TestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        TestUtils.updateAndQuery(session, id, PostgreTypes_.intArray, sourceValue);


        sourceValue = new int[][]{
                {1, 2, 3},
                {1, 8, 9678}
        };
        type = IntegerArrayType.from(int[][].class);

        dataType = type.map(serverMeta);
        bindValue = type.beforeBind(dataType, env, sourceValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);

        TestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        TestUtils.updateAndQuery(session, id, PostgreTypes_.int2dArray, sourceValue);

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

        TestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        TestUtils.updateAndQuery(session, id, PostgreTypes_.integerArray, sourceValue);


        sourceValue = new Integer[][]{{null, null, 3}, {null, null, null}, {2, 8, 3}};
        type = IntegerArrayType.from(Integer[][].class);

        dataType = type.map(serverMeta);
        bindValue = type.beforeBind(dataType, env, sourceValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);

        TestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        TestUtils.updateAndQuery(session, id, PostgreTypes_.integer2dArray, sourceValue);


    }

    @Test
    public void stringArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {
        final ServerMeta serverMeta = env.serverMeta();

        StringArrayType type;

        DataType dataType;
        Object sourceValue, bindValue, afterGetValue;

        String[] textArray;
        final List<String[]> list = new ArrayList<>();

        textArray = new String[]{"1", "\"2\\\"", null, "{,sdf\"\\,}", "Don't create new word", null};
        list.add(textArray);

        textArray = new String[]{"\"\"a", null, "''\"\"army's,\\ok\\\\", "\\\"'qe,中国\\\"", null};
        list.add(textArray);

        for (String[] array : list) {

            type = StringArrayType.from(String[].class);

            dataType = type.map(serverMeta);
            bindValue = type.beforeBind(dataType, env, array);
            afterGetValue = type.afterGet(dataType, env, bindValue);

            TestUtils.printBindAndGetValue(array, afterGetValue);
            Assert.assertEquals(afterGetValue, array);
            TestUtils.updateAndQuery(session, id, PostgreTypes_.textArray, array);
        }


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

        TestUtils.printBindAndGetValue(bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);
        TestUtils.updateAndQuery(session, id, PostgreTypes_.text2dArray, sourceValue);

    }


    @Test
    public void binaryArray(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {

        final String[] textArray = new String[]{
                "a",
                "天下",
                "''\"\"army's,\\ok\\\\",
                "\\\"'qe,中国\\\"",
                "天下's  \\ \" "
        };

        final List<FieldMeta<PostgreTypes>> fieldList;
        fieldList = List.of(PostgreTypes_.byteaArray, PostgreTypes_.binaryArray, PostgreTypes_.blobArray);

        final List<byte[][]> arrayList = new ArrayList<>();


        arrayList.add(new byte[][]{textArray[2].getBytes(StandardCharsets.UTF_8)});

        final byte[][] byteArray = new byte[textArray.length][];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = textArray[i].getBytes(StandardCharsets.UTF_8);
        }

        arrayList.add(byteArray);

        MappingType type;
        DataType dataType;

        Object bindValue, afterGetValue;


        for (int i = 0; i < 2; i++) {


            for (byte[][] array : arrayList) {

                for (FieldMeta<PostgreTypes> field : fieldList) {

                    type = field.mappingType();
                    dataType = type.map(env.serverMeta());

                    bindValue = type.beforeBind(dataType, env, array);
                    TestUtils.printBindValue(bindValue);
                    afterGetValue = type.afterGet(dataType, env, bindValue);
                    TestUtils.printBindAndGetValue(bindValue, afterGetValue);
                    Assert.assertEquals(afterGetValue, array);

                    TestUtils.updateAndQuery(session, id, field, array);

                }

            }


            if (i == 0) {
                final DmlCommand command;
                command = Postgres.setStmt()
                        .set(SQLs.LOCAL, "bytea_output", SQLs.EQUAL, "escape")
                        .asCommand();
                session.update(command);
            }


        }

        final String sourceTextValue = "abc \153\154\155 \052\251\124";

        final String textValue = "{\"abc \\\\153\\\\154\\\\155 \\\\052\\\\251\\\\124\"}";
        final Select stmt;
        stmt = SQLs.query()
                .select(s -> s.space(SQLs.constant(StringType.INSTANCE, textValue).castToArray(BinaryArrayType.LINEAR).as("r")))
                .asQuery();

        final Object result, expectedValue;
        result = session.queryOne(stmt, Object.class);

        expectedValue = new byte[][]{sourceTextValue.getBytes(StandardCharsets.ISO_8859_1)};
        Assert.assertEquals(result, expectedValue);


    }


    @Test
    public void malformedArrayError(@Autowired MappingEnv env) {
        final ServerMeta serverMeta = env.serverMeta();

        IntegerArrayType type;

        DataType dataType;
        Object sourceValue;

        sourceValue = new Integer[][]{{null, null, 3}, {null, null}, {3}};
        type = IntegerArrayType.from(Integer[][].class);

        dataType = type.map(serverMeta);


        Assertions.assertThrows(CriteriaException.class, () -> {
            type.beforeBind(dataType, env, sourceValue);
        });


    }


}
