package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;
import io.army.pojo.FieldAccessPojo;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseFieldDomain extends BaseDomain implements FieldAccessPojo {


    @Column
    public LocalDateTime createTime;

    @Column
    public LocalDateTime updateTime;

    @Column
    public int version;

}
