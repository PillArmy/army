package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;
import io.army.bean.FieldAccessBean;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseFieldDomain extends BaseDomain implements FieldAccessBean {


    @Column
    public LocalDateTime createTime;

    @Column
    public LocalDateTime updateTime;

    @Column
    public int version;

}
