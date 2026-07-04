package io.army.example.type.mapping;


import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.annotation.NewPostgreTypesId;
import io.army.example.type.domain.PostgreTypes;
import io.army.example.type.domain.PostgreTypes_;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.array.BinaryArrayType;
import io.army.mapping.array.BlobArrayType;
import io.army.mapping.array.VarBinaryArrayType;
import io.army.meta.FieldMeta;
import io.army.session.SyncSession;
import io.army.sqltype.DataType;
import io.army.util.HexUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
@Transactional
@Rollback
public class BaseTypeMappingTests {

    private static final Logger LOG = LoggerFactory.getLogger(BaseTypeMappingTests.class);

    /// @see <a href="https://www.postgresql.org/docs/current/datatype-binary.html">PostgreSQL Binary Data Types</a>
    @Test
    public void binary(@Autowired MappingEnv env, @CurrentSession SyncSession session, @NewPostgreTypesId Long id) {

        final String[] textArray = new String[]{
                "a",
                "天下",
                "''\"\"army's,\\ok\\\\",
                "\\\"'qe,中国\\\"",
                "天下's  \\ \" "
        };

        final List<FieldMeta<PostgreTypes>> fieldList;
        fieldList = List.of(PostgreTypes_.bytea, PostgreTypes_.binary, PostgreTypes_.tinyBlob, PostgreTypes_.blob, PostgreTypes_.mediumBlob);

        Assert.assertTrue(PostgreTypes_.bytea.mappingType().arrayTypeOfThis() instanceof VarBinaryArrayType);
        Assert.assertTrue(PostgreTypes_.binary.mappingType().arrayTypeOfThis() instanceof BinaryArrayType);
        Assert.assertTrue(PostgreTypes_.blob.mappingType().arrayTypeOfThis() instanceof BlobArrayType);


        DataType dataType;

        Object source, bindValue, afterGetValue;
        MappingType type;
        for (String text : textArray) {
            source = text.getBytes(StandardCharsets.UTF_8);
            for (FieldMeta<PostgreTypes> field : fieldList) {

                type = field.mappingType();
                dataType = type.map(env.serverMeta());

                bindValue = type.beforeBind(dataType, env, source);
                TestUtils.printBindValue(bindValue);
                afterGetValue = type.afterGet(dataType, env, bindValue);
                TestUtils.printBindAndGetValue(bindValue, afterGetValue);
                Assert.assertEquals(afterGetValue, source);

                TestUtils.updateAndQuery(session, id, field, source);

            }

        }


        final StringBuilder builder = new StringBuilder(128);


        builder.append("\\x")
                .append(HexUtils.hexEscapesText(true, textArray[2].getBytes(StandardCharsets.UTF_8)));

        final String[] sourceArray = new String[]{
                "abc \\153\\154\\155 \\052\\251\\124",
                "\\153 \\154\\155 \\052\\251\\124",
                "\\000 \\047 \\\\ \\134 ' \\\\",
                builder.toString()
        };

        final String[] decodeArray = new String[]{
                "abc \153\154\155 \052\251\124",
                "\153 \154\155 \052\251\124",
                "\000 \047 \\ \134 ' \\",
                textArray[2]
        };


        String oct;
        for (int i = 0; i < sourceArray.length; i++) {
            oct = sourceArray[i];

            TestUtils.printBindValue(oct);
            bindValue = oct;
            for (FieldMeta<PostgreTypes> field : fieldList) {

                type = field.mappingType();
                dataType = type.map(env.serverMeta());

                afterGetValue = type.afterGet(dataType, env, bindValue);
                TestUtils.printBindAndGetValue(bindValue, afterGetValue);


                LOG.debug("value1:\n{}\nvalue2:\n{} ", afterGetValue, decodeArray[i].getBytes(StandardCharsets.ISO_8859_1));
                Assert.assertEquals(new String((byte[]) afterGetValue, StandardCharsets.ISO_8859_1), decodeArray[i]);


            }
        }


    }


}
