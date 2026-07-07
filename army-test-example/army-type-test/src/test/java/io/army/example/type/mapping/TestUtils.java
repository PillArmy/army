package io.army.example.type.mapping;

import io.army.criteria.Select;
import io.army.criteria.Update;
import io.army.criteria.impl.SQLs;
import io.army.example.type.domain.PostgreTypes;
import io.army.example.type.domain.PostgreTypes_;
import io.army.meta.FieldMeta;
import io.army.session.SyncSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import static io.army.criteria.impl.SQLs.AS;

public abstract class TestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    private TestUtils() {
    }


    public static void printBindAndGetValue(final Object bindValue, final Object afterGetValue) {
        LOG.debug("beforeBind :\n{}\nafterGet :\n{}", bindValue, afterGetValue);
    }

    public static void printBindValue(final Object bindValue) {
        LOG.debug("beforeBind :\n{}", bindValue);
    }


    public static void updateAndQuery(SyncSession session, Long id, FieldMeta<PostgreTypes> field, final Object value) {
        Update updateStmt;
        updateStmt = SQLs.singleUpdate()
                .update(PostgreTypes_.T, AS, "t")
                .set(field, SQLs::literal, value)
                .where(PostgreTypes_.id.equal(SQLs::literal, id))
                .asUpdate();

        session.update(updateStmt);


        Object result;

        final Select selectStmt;
        selectStmt = SQLs.query()
                .select(field)
                .from(PostgreTypes_.T, AS, "t")
                .where(PostgreTypes_.id.equal(SQLs::literal, id))
                .asQuery();


        result = session.queryOne(selectStmt, field.javaType());
        LOG.debug("source:\n{}\nresult:\n{}", value, result);
        Assert.assertEquals(result, value);


        updateStmt = SQLs.singleUpdate()
                .update(PostgreTypes_.T, AS, "t")
                .set(field, SQLs::param, value)
                .where(PostgreTypes_.id.equal(SQLs::literal, id))
                .asUpdate();

        session.update(updateStmt);

        result = session.queryOne(selectStmt, field.javaType());
        Assert.assertEquals(result, value);
    }


}
