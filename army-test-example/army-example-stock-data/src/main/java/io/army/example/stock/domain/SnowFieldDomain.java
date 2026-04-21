package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.Generator;
import io.army.annotation.MappedSuperclass;

@MappedSuperclass
public abstract class SnowFieldDomain extends BaseFieldDomain {

    @Generator(SNOWFLAKE)
    @Column
    public long id;


}
