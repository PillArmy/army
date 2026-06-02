package io.army.example.stock.domain;


import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;

import java.time.LocalDate;

/// Base domain for stock-related entities that are associated with a specific **stock** and **date**.
///
/// <p>Extends `BaseDomain` with two additional persisted fields:</p>
/// - `stockId` — foreign key referencing the `stock` table (not primitive, to support DEFAULT value clause)
/// - `date` — the trading date for the record
///
/// <p>Also provides non-persisted convenience fields (`exchange`, `code`, `name`) for
/// runtime data enrichment without additional columns in the database.</p>
///
/// @param <T> the concrete domain type for fluent setter chaining
@MappedSuperclass
@SuppressWarnings("unchecked")
public abstract class StockBaseDomain<T extends StockBaseDomain<T>> extends BaseDomain<T> {


    @Column(notNull = true, updatable = false, comment = "股票id")
    public Long stockId; // 不能是 primitive , 因为要使用 insert statement default value clause

    @Column(notNull = true, updatable = false, comment = "日期")
    public LocalDate date;


    // below non table filed

    /// non table filed
    public String exchange;

    /// non table filed
    public String code;

    /// non table filed
    public String name;


    public LocalDate getDate() {
        return date;
    }

    public T setDate(LocalDate date) {
        this.date = date;
        return (T) this;
    }

    public String getExchange() {
        return exchange;
    }

    public T setExchange(String exchange) {
        this.exchange = exchange;
        return (T) this;
    }

    public String getCode() {
        return code;
    }

    public T setCode(String code) {
        this.code = code;
        return (T) this;
    }

    public String getName() {
        return name;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }


    public Long getStockId() {
        return stockId;
    }

    public StockBaseDomain<T> setStockId(Long stockId) {
        this.stockId = stockId;
        return this;
    }
}
