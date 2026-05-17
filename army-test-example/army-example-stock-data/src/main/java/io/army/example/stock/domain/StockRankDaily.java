package io.army.example.stock.domain;

import io.army.annotation.*;
import io.army.generator.snowflake.Snowflake8Generator;

import java.math.BigDecimal;

@Table(name = "stock_rank_daily", indexes = {
        @Index(name = "stock_rank_daily_idx_stock_id", fieldList = "stockId")},
        comment = "每日排名")
public abstract class StockRankDaily extends BaseDomain<StockRankDaily> {

    @Generator(value = SNOWFLAKE8, params = @Param(name = Snowflake8Generator.START_TIME, value = "1776386333819"))
    @Column
    public long id;

    @Column(notNull = true, updatable = false, comment = "股票id")
    public Long stockId; // 不能是 primitive , 因为要使用 insert statement default value clause

    @Column(updatable = false, comment = "当日排名")
    public int rankNumber;

    @Column(notNull = true, precision = 9, scale = 3, comment = "收盘价")
    public BigDecimal close;

    @Column(notNull = true, precision = 15, scale = 3, comment = "成交量")
    public BigDecimal volume;

    @Column(precision = 7, scale = 3, comment = "涨跌幅")
    public BigDecimal chgPct;

    @Column(precision = 5, scale = 3, comment = "换手率")
    public BigDecimal turnoverRate;

    @Column(precision = 6, scale = 3, comment = "振幅")
    public BigDecimal amplitude;

    @Column(precision = 18, scale = 3, comment = "总市值(market capitalization)")
    public BigDecimal marketCap;

    @Column(comment = "随机数")
    public int rankRandom;


    public StockStatus stockStatus;


    public long getId() {
        return id;
    }

    public StockRankDaily setId(long id) {
        this.id = id;
        return this;
    }

    public Long getStockId() {
        return stockId;
    }

    public StockRankDaily setStockId(Long stockId) {
        this.stockId = stockId;
        return this;
    }

    public int getRankNumber() {
        return rankNumber;
    }

    public StockRankDaily setRankNumber(int rankNumber) {
        this.rankNumber = rankNumber;
        return this;
    }

    public BigDecimal getClose() {
        return close;
    }

    public StockRankDaily setClose(BigDecimal close) {
        this.close = close;
        return this;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public StockRankDaily setVolume(BigDecimal volume) {
        this.volume = volume;
        return this;
    }

    public BigDecimal getChgPct() {
        return chgPct;
    }

    public StockRankDaily setChgPct(BigDecimal chgPct) {
        this.chgPct = chgPct;
        return this;
    }

    public BigDecimal getTurnoverRate() {
        return turnoverRate;
    }

    public StockRankDaily setTurnoverRate(BigDecimal turnoverRate) {
        this.turnoverRate = turnoverRate;
        return this;
    }

    public BigDecimal getAmplitude() {
        return amplitude;
    }

    public StockRankDaily setAmplitude(BigDecimal amplitude) {
        this.amplitude = amplitude;
        return this;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public StockRankDaily setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
        return this;
    }

    public int getRankRandom() {
        return rankRandom;
    }

    public StockRankDaily setRankRandom(int rankRandom) {
        this.rankRandom = rankRandom;
        return this;
    }

    public StockStatus getStockStatus() {
        return stockStatus;
    }

    public StockRankDaily setStockStatus(StockStatus stockStatus) {
        this.stockStatus = stockStatus;
        return this;
    }
}
