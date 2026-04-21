package io.army.example.stock.domain;

import io.army.annotation.*;
import io.army.generator.snowflake.SnowflakeGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;


@Table(name = "stock", indexes = {@Index(name = "stock_uni_ex_code", unique = true, fieldList = {"exchange", "code"})},
        comment = "股票")
@OverrideParams(params = @Param(name = SnowflakeGenerator.START_TIME, value = "1776386333818"))
public class Stock extends SnowFieldDomain {

    @Column(notNull = true, updatable = false, precision = 5, comment = "交易所代码")
    public String exchange;

    @Column(notNull = true, updatable = false, precision = 15, comment = "股票代码")
    public String code;

    @Column(notNull = true, precision = 130, comment = "股票公司名称")
    public String name;

    @Column(notNull = true, precision = 10, defaultValue = "'NORMAL'", comment = "股票在市状态")
    public StockStatus status;

    @Column(defaultValue = "''", comment = "公司全称")
    public String fullName;

    @Column(defaultValue = "DATE '1970-01-01'", comment = "股票上市日期")
    public LocalDate listingDate;

    @Column(defaultValue = "0.00", comment = "股票发行价")
    public BigDecimal offerPrice;


}
