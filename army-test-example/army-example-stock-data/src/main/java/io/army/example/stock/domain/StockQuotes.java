package io.army.example.stock.domain;


import io.army.annotation.*;
import io.army.generator.snowflake.Snowflake8Generator;

import java.math.BigDecimal;

/// Domain entity representing **daily stock quotes** (market snapshot data).
///
/// <p>Maps to the `stock_quotes` table with a unique composite index on `(stockId, date)`.
/// Contains comprehensive daily trading metrics including OHLC prices, volume,
/// valuation ratios (P/E, P/B, P/S), market capitalization, and trading flags.</p>
///
/// ### Key Financial Metrics
/// | Field           | Description                          |
/// |-----------------|--------------------------------------|
/// | `open/high/low` | Intraday OHLC prices                 |
/// | `prevClose`     | Previous day's closing price         |
/// | `volume`        | Trading volume (in lots)             |
/// | `peTtm/peLyr`   | P/E ratio (TTM / Last Year)          |
/// | `marketCap`     | Total market capitalization           |
/// | `turnoverRate`  | Daily turnover rate (%)              |
///
/// @see StockBaseDomain
/// @see Stock
@Table(name = "stock_quotes",
        indexes = @Index(name = "${DEFAULT}", unique = true, fieldList = {"stockId", "date"}),
        comment = "股票每日行情数据")
public class StockQuotes extends StockBaseDomain<StockQuotes> {

    @Generator(value = SNOWFLAKE8, params = @Param(name = Snowflake8Generator.START_TIME, value = "1779111192831"))
    @Column
    public long id;

    @Column(updatable = false, precision = 9, scale = 3, comment = "开盘价")
    public BigDecimal open;

    @Column(updatable = false, precision = 9, scale = 3, comment = "最高价")
    public BigDecimal high;

    @Column(updatable = false, precision = 9, scale = 3, comment = "最低价")
    public BigDecimal low;

    @Column(updatable = false, precision = 9, scale = 3, comment = "昨日收盘价")
    public BigDecimal prevClose;

    @Column(updatable = false, precision = 15, scale = 3, comment = "成交量")
    public BigDecimal volume;

    @Column(precision = 7, scale = 3, updatable = false, comment = "换手率")
    public BigDecimal turnoverRate;

    @Column(precision = 9, scale = 3, comment = "涨跌幅")
    public BigDecimal chgPct;

    @Column(precision = 9, scale = 3, comment = "滚动市盈率")
    public BigDecimal peTtm;

    @Column(precision = 9, scale = 3, comment = "表态市盈率")
    public BigDecimal peLyr;

    @Column(precision = 18, scale = 3, comment = "总市值(market capitalization)")
    public BigDecimal marketCap;

    @Column(precision = 6, scale = 3, comment = "量比")
    public BigDecimal volumeRatio;

    @Column(comment = "总股本")
    public Long totalShares;

    @Column(comment = "流通股本")
    public Long floatShares;

    @Column(precision = 6, scale = 3, comment = "委比")
    public BigDecimal orderRatio;

    @Column(precision = 18, scale = 3, comment = "流通值(Circulating Market Capitalization)")
    public BigDecimal circMarketCap;

    @Column(updatable = false, precision = 9, scale = 3, comment = "涨停价")
    public BigDecimal limitUp;

    @Column(updatable = false, precision = 9, scale = 3, comment = "跌停价")
    public BigDecimal limitDown;

    @Column(precision = 6, scale = 3, comment = "振幅")
    public BigDecimal amplitude;

    @Column(updatable = false, precision = 15, scale = 3, comment = "内盘")
    public BigDecimal sellVolume;

    @Column(updatable = false, precision = 15, scale = 3, comment = "外盘")
    public BigDecimal buyVolume;

    @Column(precision = 15, scale = 3, comment = "市销率")
    public BigDecimal psRatio;

    @Column(precision = 10, scale = 3, comment = "市净率")
    public BigDecimal pbRatio;

    @Column(comment = "是否盈利")
    public Boolean profitable;

    @Column(comment = "是否同股同权")
    public Boolean sameRights;

    @Column(comment = "是否注册制")
    public Boolean registrationBased;


    public String dayCandlesUrl;


    public StockStatus stockStatus;


    public long getId() {
        return id;
    }

    public StockQuotes setId(long id) {
        this.id = id;
        return this;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public StockQuotes setOpen(BigDecimal open) {
        this.open = open;
        return this;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public StockQuotes setHigh(BigDecimal high) {
        this.high = high;
        return this;
    }

    public BigDecimal getLow() {
        return low;
    }

    public StockQuotes setLow(BigDecimal low) {
        this.low = low;
        return this;
    }

    public BigDecimal getPrevClose() {
        return prevClose;
    }

    public StockQuotes setPrevClose(BigDecimal prevClose) {
        this.prevClose = prevClose;
        return this;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public StockQuotes setVolume(BigDecimal volume) {
        this.volume = volume;
        return this;
    }

    public BigDecimal getTurnoverRate() {
        return turnoverRate;
    }

    public StockQuotes setTurnoverRate(BigDecimal turnoverRate) {
        this.turnoverRate = turnoverRate;
        return this;
    }

    public BigDecimal getChgPct() {
        return chgPct;
    }

    public StockQuotes setChgPct(BigDecimal chgPct) {
        this.chgPct = chgPct;
        return this;
    }

    public BigDecimal getPeTtm() {
        return peTtm;
    }

    public StockQuotes setPeTtm(BigDecimal peTtm) {
        this.peTtm = peTtm;
        return this;
    }

    public BigDecimal getPeLyr() {
        return peLyr;
    }

    public StockQuotes setPeLyr(BigDecimal peLyr) {
        this.peLyr = peLyr;
        return this;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public StockQuotes setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
        return this;
    }

    public BigDecimal getVolumeRatio() {
        return volumeRatio;
    }

    public StockQuotes setVolumeRatio(BigDecimal volumeRatio) {
        this.volumeRatio = volumeRatio;
        return this;
    }

    public Long getTotalShares() {
        return totalShares;
    }

    public StockQuotes setTotalShares(Long totalShares) {
        this.totalShares = totalShares;
        return this;
    }

    public Long getFloatShares() {
        return floatShares;
    }

    public StockQuotes setFloatShares(Long floatShares) {
        this.floatShares = floatShares;
        return this;
    }

    public BigDecimal getOrderRatio() {
        return orderRatio;
    }

    public StockQuotes setOrderRatio(BigDecimal orderRatio) {
        this.orderRatio = orderRatio;
        return this;
    }

    public BigDecimal getCircMarketCap() {
        return circMarketCap;
    }

    public StockQuotes setCircMarketCap(BigDecimal circMarketCap) {
        this.circMarketCap = circMarketCap;
        return this;
    }

    public BigDecimal getLimitUp() {
        return limitUp;
    }

    public StockQuotes setLimitUp(BigDecimal limitUp) {
        this.limitUp = limitUp;
        return this;
    }

    public BigDecimal getLimitDown() {
        return limitDown;
    }

    public StockQuotes setLimitDown(BigDecimal limitDown) {
        this.limitDown = limitDown;
        return this;
    }

    public BigDecimal getAmplitude() {
        return amplitude;
    }

    public StockQuotes setAmplitude(BigDecimal amplitude) {
        this.amplitude = amplitude;
        return this;
    }

    public BigDecimal getSellVolume() {
        return sellVolume;
    }

    public StockQuotes setSellVolume(BigDecimal sellVolume) {
        this.sellVolume = sellVolume;
        return this;
    }

    public BigDecimal getBuyVolume() {
        return buyVolume;
    }

    public StockQuotes setBuyVolume(BigDecimal buyVolume) {
        this.buyVolume = buyVolume;
        return this;
    }

    public BigDecimal getPsRatio() {
        return psRatio;
    }

    public StockQuotes setPsRatio(BigDecimal psRatio) {
        this.psRatio = psRatio;
        return this;
    }

    public BigDecimal getPbRatio() {
        return pbRatio;
    }

    public StockQuotes setPbRatio(BigDecimal pbRatio) {
        this.pbRatio = pbRatio;
        return this;
    }

    public Boolean getProfitable() {
        return profitable;
    }

    public StockQuotes setProfitable(Boolean profitable) {
        this.profitable = profitable;
        return this;
    }

    public Boolean getSameRights() {
        return sameRights;
    }

    public StockQuotes setSameRights(Boolean sameRights) {
        this.sameRights = sameRights;
        return this;
    }

    public Boolean getRegistrationBased() {
        return registrationBased;
    }

    public StockQuotes setRegistrationBased(Boolean registrationBased) {
        this.registrationBased = registrationBased;
        return this;
    }

    public String getDayCandlesUrl() {
        return dayCandlesUrl;
    }

    public StockQuotes setDayCandlesUrl(String dayCandlesUrl) {
        this.dayCandlesUrl = dayCandlesUrl;
        return this;
    }

    public StockStatus getStockStatus() {
        return stockStatus;
    }

    public StockQuotes setStockStatus(StockStatus stockStatus) {
        this.stockStatus = stockStatus;
        return this;
    }
}
