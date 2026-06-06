package io.army.example.type.domain;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
@SuppressWarnings("unchecked")
public abstract class TypeBaseDomain<T extends TypeBaseDomain<T>> {

    protected static final String SNOWFLAKE8 = "io.army.generator.snowflake.Snowflake8Generator";

    @Column
    public LocalDateTime createTime;

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public T setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return (T) this;
    }

}
