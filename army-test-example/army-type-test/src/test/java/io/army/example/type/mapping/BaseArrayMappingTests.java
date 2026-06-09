package io.army.example.type.mapping;

import io.army.mapping.MappingEnv;
import io.army.mapping.array.IntegerArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;

import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
public class BaseArrayMappingTests {

    private static final Logger LOG = LoggerFactory.getLogger(BaseArrayMappingTests.class);


    @Test
    public void intArray(@Autowired MappingEnv env) {
        final ServerMeta serverMeta = env.serverMeta();

        IntegerArrayType type;

        DataType dataType;
        Object sourceValue, bindValue, afterGetValue;

        sourceValue = new int[]{1, 2, 3};
        type = IntegerArrayType.PRIMITIVE_LINEAR;

        dataType = type.map(serverMeta);
        bindValue = type.beforeBind(dataType, env, sourceValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);

        LOG.debug("beforeBind :\n{}\nafterGet :\n{}", bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);


        sourceValue = new int[][]{{1, 2, 3}, {1}};
        type = IntegerArrayType.from(int[][].class);

        dataType = type.map(serverMeta);
        bindValue = type.beforeBind(dataType, env, sourceValue);
        afterGetValue = type.afterGet(dataType, env, bindValue);

        LOG.debug("beforeBind :\n{}\nafterGet :\n{}", bindValue, afterGetValue);
        Assert.assertEquals(afterGetValue, sourceValue);


    }
}
