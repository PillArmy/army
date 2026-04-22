package io.army.example.stock.domain;

import io.army.annotation.*;
import io.army.generator.snowflake.SnowflakeGenerator;

import java.math.BigDecimal;

@Table(name = "stock_rank_daily", indexes = {
        @Index(name = "stock_rank_daily_idx_stock_id", fieldList = "stockId")},
        comment = "每日排名")
@OverrideParams(fields = {@FieldParam(name = "id", params = @Param(name = SnowflakeGenerator.START_TIME, value = "1776386333819"))})
public abstract class StockRankDaily extends SnowFieldDomain {

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

}
