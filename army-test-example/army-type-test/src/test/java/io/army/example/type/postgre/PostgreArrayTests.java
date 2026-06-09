package io.army.example.type.postgre;

import io.army.criteria.Select;
import io.army.criteria.impl.SQLs;
import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.domain.PostgreTypes;
import io.army.example.type.domain.PostgreTypes_;
import io.army.session.SyncSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static io.army.criteria.impl.SQLs.AS;
import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
@Transactional
@Rollback
public class PostgreArrayTests {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreArrayTests.class);

    @Test
    public void integerArray(@CurrentSession SyncSession session) {
        Integer[] array, result;
        array = new Integer[]{1, 2, 3};
        PostgreTypes postgre;
        postgre = new PostgreTypes();
        postgre.integerArray = array;
        session.save(postgre);

        final Select stmt;
        stmt = SQLs.query()
                .select(PostgreTypes_.integerArray)
                .from(PostgreTypes_.T, AS, "t")
                .where(PostgreTypes_.id.equal(postgre.id))
                .asQuery();

        result = session.queryOne(stmt, Integer[].class);
        LOG.info("result : {}", Arrays.toString(result));
        Assertions.assertArrayEquals(result, array);
    }


}
