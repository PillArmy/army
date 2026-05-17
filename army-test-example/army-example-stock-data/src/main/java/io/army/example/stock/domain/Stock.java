package io.army.example.stock.domain;

import io.army.annotation.*;
import io.army.generator.snowflake.Snowflake8Generator;

import java.math.BigDecimal;
import java.time.LocalDate;


@Table(name = "stock", indexes = {@Index(name = "${DEFAULT}", unique = true, fieldList = {"exchange", "code"})},
        comment = "股票")
public class Stock extends BaseDomain<Stock> {

    @Generator(value = SNOWFLAKE8, params = @Param(name = Snowflake8Generator.START_TIME, value = "1779012232202"))
    @Column
    public long id;

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

    @Column(precision = 10, scale = 2, defaultValue = "0.00", comment = "股票发行价")
    public BigDecimal offerPrice;

    public long getId() {
        return id;
    }

    public Stock setId(long id) {
        this.id = id;
        return this;
    }

    public String getExchange() {
        return exchange;
    }

    public Stock setExchange(String exchange) {
        this.exchange = exchange;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Stock setCode(String code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public Stock setName(String name) {
        this.name = name;
        return this;
    }

    public StockStatus getStatus() {
        return status;
    }

    public Stock setStatus(StockStatus status) {
        this.status = status;
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public Stock setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public LocalDate getListingDate() {
        return listingDate;
    }

    public Stock setListingDate(LocalDate listingDate) {
        this.listingDate = listingDate;
        return this;
    }

    public BigDecimal getOfferPrice() {
        return offerPrice;
    }

    public Stock setOfferPrice(BigDecimal offerPrice) {
        this.offerPrice = offerPrice;
        return this;
    }
}
