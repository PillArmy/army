package io.army.example.type.dao.impl;

import io.army.criteria.Insert;
import io.army.criteria.Select;
import io.army.criteria.Update;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.example.type.dao.PostgreDao;
import io.army.example.type.domain.Postgre;
import io.army.example.type.domain.Postgre_;
import io.army.session.SyncSessionContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static io.army.criteria.impl.SQLs.AS;

@Repository("postgreDao")
public class PostgreDaoImpl extends ArmyTypeBaseDao implements PostgreDao {

    public PostgreDaoImpl(SyncSessionContext sessionContext) {
        super(sessionContext);
    }

    @Override
    public Postgre insert(Postgre postgre) {
        final Insert stmt = Postgres.singleInsert()
                .insertInto(Postgre_.T)
                .values(postgre)
                .asInsert();

        final int count = sessionContext.currentSession().update(stmt);
        assert count == 1;
        return postgre;
    }

    @Override
    public Postgre update(Postgre postgre) {
        final Update stmt = Postgres.singleUpdate()
                .update(Postgre_.T, AS, "p")
                .set(postgre)
                .where(Postgre_.id.equal(postgre.id))
                .asUpdate();

        final int count = sessionContext.currentSession().update(stmt);
        assert count == 1;
        return postgre;
    }

    @Override
    public Postgre findById(Long id) {
        final Select stmt = Postgres.singleQuery()
                .select("p")
                .from(Postgre_.T, AS, "p")
                .where(Postgre_.id.equal(id))
                .asQuery();

        return sessionContext.currentSession().queryOne(stmt, Postgre::new);
    }

    @Override
    public Postgre findFirst() {
        final Select stmt = Postgres.singleQuery()
                .select("p")
                .from(Postgre_.T, AS, "p")
                .orderBy(Postgre_.id)
                .limit(1)
                .asQuery();

        return sessionContext.currentSession().queryOne(stmt, Postgre::new);
    }

}
