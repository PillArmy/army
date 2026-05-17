package io.army.example.stock.config;

import io.army.advice.FactoryAdvice;
import io.army.criteria.Insert;
import io.army.criteria.LiteralMode;
import io.army.criteria.impl.Postgres;
import io.army.example.stock.domain.Gender;
import io.army.example.stock.domain.StockUser;
import io.army.example.stock.domain.StockUser_;
import io.army.session.SessionFactory;
import io.army.session.SyncSession;
import io.army.session.SyncSessionFactory;

import java.time.LocalDateTime;

final class StockSessionFactoryAdvisor implements FactoryAdvice {

    @Override
    public int order() {
        return 0;
    }


    @Override
    public void afterInitialize(SessionFactory sessionFactory) {
        final SyncSessionFactory factory = (SyncSessionFactory) sessionFactory;

        try (SyncSession session = factory.localSession("initUseId", false)) {
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
    }


}
