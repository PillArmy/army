package io.army.session.sync.postgre;

import io.army.criteria.Select;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.example.bank.domain.user.ChinaRegion_;
import io.army.session.SyncSession;
import org.testng.annotations.Test;

import static io.army.criteria.impl.SQLs.AS;

@Test(dataProvider = "localSessionProvider")
public class OperatorTests extends SessionTestSupport {

    @Test//(invocationCount = 3)
    public void similarTo(SyncSession session) {
        Select stmt;
        stmt = Postgres.query()
                .select(ChinaRegion_.id)
                .from(ChinaRegion_.T, AS, "c")
                .where(ChinaRegion_.name.similarTo(SQLs::constant, "%(b|d)%"))
                .and(ChinaRegion_.name.notSimilarTo(SQLs::literal, "%(b|d)%"))
                .and(ChinaRegion_.name.similarTo(SQLs::constant, "%(b|d)%", SQLs.ESCAPE, '|'))
                .and(ChinaRegion_.name.notSimilarTo(SQLs::literal, "Hong Kong", SQLs.ESCAPE, '|'))
                .and(ChinaRegion_.name.similarTo(SQLs::param, "%(b|d)%", SQLs.ESCAPE, '|'))
                .and(ChinaRegion_.name.notSimilarTo(SQLs::param, "Hong Kong", SQLs.ESCAPE, '|'))
                .and(ChinaRegion_.id.equal(1))
                .asQuery();

         session.queryOne(stmt,Long.class);


    }
}
