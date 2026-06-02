package io.army.example.stock.dao.impl;

import io.army.dao.ArmySyncBaseDao;
import io.army.example.stock.dao.StockBaseDao;
import io.army.session.SyncSessionContext;
import org.springframework.stereotype.Repository;

/// Base DAO implementation for the stock module, extending `ArmySyncBaseDao`
/// with synchronous session-based data access capabilities.
///
/// <p>Serves as the superclass for all concrete DAO implementations in this module.
/// Injected with `SyncSessionContext` for obtaining the current Army session.</p>
@Repository("armyStockBaseDao")
public class ArmyStockBaseDao extends ArmySyncBaseDao implements StockBaseDao {

    public ArmyStockBaseDao(SyncSessionContext sessionContext) {
        super(sessionContext);
    }
}
