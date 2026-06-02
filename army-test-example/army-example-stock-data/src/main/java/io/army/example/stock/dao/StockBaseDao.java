package io.army.example.stock.dao;

import io.army.dao.SyncBaseDao;

/// Base DAO interface for the stock example module.
///
/// <p>Extends `SyncBaseDao` to inherit common synchronous data access operations
/// (save, batch save, update, delete, query) provided by the Army framework.</p>
///
/// <p>All domain-specific DAO interfaces in this module extend this interface.</p>
public interface StockBaseDao extends SyncBaseDao {


}
