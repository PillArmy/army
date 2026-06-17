package io.army.example.type.mapping;

import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.annotation.NewPostgreTypesId;
import io.army.example.type.domain.PostgreTypes_;
import io.army.executor.DataAccessException;
import io.army.mapping.MappingEnv;
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


}
