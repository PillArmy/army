package io.army.example.stock.config;

import io.army.advice.FactoryAdvice;
import io.army.criteria.Insert;
import io.army.criteria.LiteralMode;
import io.army.criteria.Select;
import io.army.criteria.Update;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.example.stock.domain.*;
import io.army.session.SessionFactory;
import io.army.session.SyncSession;
import io.army.session.SyncSessionFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static io.army.criteria.impl.SQLs.*;

final class StockSessionFactoryAdvisor implements FactoryAdvice {

    @Override
    public int order() {
        return 0;
    }


    @Override
    public void afterInitialize(SessionFactory sessionFactory) {
        final SyncSessionFactory factory = (SyncSessionFactory) sessionFactory;

        try (SyncSession session = factory.localSession("initStockData", false)) {
            saveDefaultUser(session);
            deleteExpiredUploadedFiles(session);
        }
    }


    private static void saveDefaultUser(SyncSession session) {
        final LocalDateTime now = LocalDateTime.now();

        final StockUser u = new StockUser()
                .setId(47540383744L)
                .setCreateTime(now)
                .setUpdateTime(now)
                .setVersion(0)
                .setUserName("zoro")
                .setGender(Gender.MALE);

        final Insert stmt;
        stmt = Postgres.singleInsert()
                .migration()
                .literalMode(LiteralMode.LITERAL)
                .insertInto(StockUser_.T)
                .value(u)
                .onConflict().parens(s -> s.space(StockUser_.id))
                .doNothing()
                .asInsert();

        session.update(stmt);
    }


    private static void deleteExpiredUploadedFiles(SyncSession session) {
        final Select stmt;
        stmt = SQLs.query()
                .select(UploadRecord_.id, UploadRecord_.filePath)
                .from(UploadRecord_.T, AS, "t")
                .where(UploadRecord_.deleted.equal(FALSE))
                .and(UploadRecord_.createTime.less(LocalDateTime.now().minusDays(1)))
                .orderBy(UploadRecord_.id)
                .asQuery();

        final List<UploadRecord> list;
        list = session.queryObjectList(stmt, UploadRecord::new);
        for (UploadRecord o : list) {
            deleteIfExists(o, session);
        }

    }


    private static void deleteIfExists(UploadRecord o, SyncSession session) {
        try {
            Files.deleteIfExists(Path.of(o.filePath));

            final Update stmt;
            stmt = Postgres.singleUpdate()
                    .update(UploadRecord_.T, AS, "t")
                    .set(UploadRecord_.deleted, TRUE)
                    .where(UploadRecord_.id.equal(o.id))
                    .and(UploadRecord_.deleted.equal(FALSE))
                    .asUpdate();

            session.update(stmt);
        } catch (Exception e) {
            // ignore
        }

    }


}
