package io.army.spring.ai.vectorstore;

import io.army.dialect._Constant;
import io.army.mapping.DualGenericsMapping;
import io.army.mapping.MappingType;
import io.army.meta.FieldMeta;
import io.army.modelgen._MetaBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SpringAiVectorStoreTests {

    private static final Logger LOG = LoggerFactory.getLogger(SpringAiVectorStoreTests.class);

    @Test
    public void fieldColumnName() {
        final StringBuilder tempBuilder = new StringBuilder(20);
        final StringBuilder logBuilder = new StringBuilder(1024);
        for (FieldMeta<SpringAiVectorStore> field : SpringAiVectorStore_.T.fieldList()) {
            logBuilder.append(field.fieldName())
                    .append(_Constant.SPACE)
                    .append(':')
                    .append(_Constant.SPACE)
                    .append(field.columnName())
                    .append('\n');
            Assert.assertEquals(field.columnName(), _MetaBridge.camelToLowerCase(field.fieldName(), tempBuilder));
        }

        LOG.info(logBuilder.toString());
    }

    @Test
    public void metadataFieldMeta() {
        final MappingType type;
        type = SpringAiVectorStore_.metadata.mappingType();
        if (type instanceof DualGenericsMapping t) {
            Assert.assertEquals(t.firstGenericsType(), String.class);
            Assert.assertEquals(t.secondGenericsType(), Object.class);
        } else {
            Assert.fail(String.format("type is not %s", DualGenericsMapping.class.getName()));
        }

    }


}
