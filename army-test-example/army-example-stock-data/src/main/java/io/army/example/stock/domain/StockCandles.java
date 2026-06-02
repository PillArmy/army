package io.army.example.stock.domain;


import io.army.annotation.*;
import io.army.generator.snowflake.Snowflake8Generator;

import java.math.BigDecimal;

/// Domain entity representing **stock K-line (candlestick) data**.
///
/// <p>Maps to the `stock_candles` table with a unique composite index on `(stockId, date)`.
/// Each record captures OHLCV (Open-High-Low-Close-Volume) candlestick data
/// used for technical analysis and charting.</p>
///
/// @see StockBaseDomain
/// @see Stock
@Table(name = "stock_candles",
        indexes = @Index(name = "${DEFAULT}", unique = true, fieldList = {"stockId", "date"}),
        comment = "股票k线数据")
public class StockCandles extends StockBaseDomain<StockCandles> {

    @Generator(value = SNOWFLAKE8, params = @Param(name = Snowflake8Generator.START_TIME, value = "1779111209547"))
    @Column
    public long id;

    @Column(precision = 9, scale = 3, comment = "开盘价")
    public BigDecimal open;

    @Column(precision = 9, scale = 3, comment = "最高价")
    public BigDecimal high;

    @Column(precision = 9, scale = 3, comment = "最低价")
    public BigDecimal low;

    @Column(precision = 9, scale = 3, comment = "收盘价")
    public BigDecimal close;

    @Column(precision = 9, scale = 3, comment = "昨日收盘价")
    public BigDecimal prevClose;

    @Column(precision = 15, scale = 3, comment = "成交量,单位万手")
    public BigDecimal volume;

    @Column(precision = 8, scale = 3, comment = "涨跌额")
    public BigDecimal change;

    @Column(precision = 9, scale = 3, comment = "涨跌幅")
    public BigDecimal chgPct;

    @Column(precision = 8, scale = 3, comment = "换手率")
    public BigDecimal turnoverRate;


    public long getId() {
        return id;
    }

    public StockCandles setId(long id) {
        this.id = id;
        return this;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public StockCandles setOpen(BigDecimal open) {
        this.open = open;
        return this;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public StockCandles setHigh(BigDecimal high) {
        this.high = high;
        return this;
    }

    public BigDecimal getLow() {
        return low;
    }

    public StockCandles setLow(BigDecimal low) {
        this.low = low;
        return this;
    }

    public BigDecimal getClose() {
        return close;
    }

    public StockCandles setClose(BigDecimal close) {
        this.close = close;
        return this;
    }

    public BigDecimal getPrevClose() {
        return prevClose;
    }

    public StockCandles setPrevClose(BigDecimal prevClose) {
        this.prevClose = prevClose;
        return this;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public StockCandles setVolume(BigDecimal volume) {
        this.volume = volume;
        return this;
    }

    public BigDecimal getChange() {
        return change;
    }

    public StockCandles setChange(BigDecimal change) {
        this.change = change;
        return this;
    }

    public BigDecimal getChgPct() {
        return chgPct;
    }

    public StockCandles setChgPct(BigDecimal chgPct) {
        this.chgPct = chgPct;
        return this;
    }

    public BigDecimal getTurnoverRate() {
        return turnoverRate;
    }

    public StockCandles setTurnoverRate(BigDecimal turnoverRate) {
        this.turnoverRate = turnoverRate;
        return this;
    }
}
