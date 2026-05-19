package io.army.example.stock.dao.impl;

import io.army.dao.ArmySyncBaseDao;
import io.army.example.stock.dao.StockBaseDao;
import io.army.session.SyncSessionContext;
import org.springframework.stereotype.Repository;

@Repository("armyStockBaseDao")
public class ArmyStockBaseDao extends ArmySyncBaseDao implements StockBaseDao {

    public ArmyStockBaseDao(SyncSessionContext sessionContext) {
        super(sessionContext);
    }
}
