package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.Generator;
import io.army.annotation.MappedSuperclass;

@MappedSuperclass
public abstract class SnowFieldDomain<T extends SnowFieldDomain<T>> extends BaseDomain<T> {


    @Generator(SNOWFLAKE8)
    @Column
    public long id;


}
