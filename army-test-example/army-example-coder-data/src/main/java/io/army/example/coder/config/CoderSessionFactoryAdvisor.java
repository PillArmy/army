package io.army.example.coder.config;

import io.army.advice.FactoryAdvice;
import io.army.criteria.Insert;
import io.army.criteria.LiteralMode;
import io.army.criteria.impl.Postgres;
import io.army.example.coder.domain.Gender;
import io.army.example.coder.domain.User;
import io.army.example.coder.domain.User_;
import io.army.session.SessionFactory;
import io.army.session.SyncSession;
import io.army.session.SyncSessionFactory;

import java.time.LocalDateTime;

/// `FactoryAdvice` implementation that performs **post-initialization data seeding**
/// and cleanup when the Army session factory is created.
///
/// <p>Executed during `afterInitialize` callback:</p>
/// 1. **Seeds a default user** (`zoro`) using PostgreSQL's `ON CONFLICT DO NOTHING`
///    to ensure idempotent insertion
/// 2. **Deletes expired uploaded files** older than 1 day that haven't been
///    soft-deleted yet, cleaning up both filesystem and database records
final class CoderSessionFactoryAdvisor implements FactoryAdvice {



    @Override
    public void afterInitialize(SessionFactory sessionFactory) {
        final SyncSessionFactory factory = (SyncSessionFactory) sessionFactory;

        try (SyncSession session = factory.localSession("initCoderData", false)) {
            saveDefaultUser(session);
        }
    }


    private static void saveDefaultUser(SyncSession session) {
        final LocalDateTime now = LocalDateTime.now();

        final User u = new User()
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
                .insertInto(User_.T)
                .value(u)
                .onConflict().parens(s -> s.space(User_.id))
                .doNothing()
                .asInsert();

        session.update(stmt);
    }


}
