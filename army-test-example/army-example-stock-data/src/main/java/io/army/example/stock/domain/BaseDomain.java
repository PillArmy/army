package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;
import io.army.generator.snowflake.Snowflake8Generator;

import java.time.LocalDateTime;

@MappedSuperclass
@SuppressWarnings("unchecked")
public abstract class BaseDomain<T extends BaseDomain<T>> {

    protected static final String SNOWFLAKE8 = "io.army.generator.snowflake.Snowflake8Generator";

    protected static final String SNOW_START_TIME = Snowflake8Generator.START_TIME;


    @Column
    public LocalDateTime createTime;

    @Column
    public LocalDateTime updateTime;

    @Column
    public int version;


    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public T setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return (T) this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public T setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return (T) this;
    }

    public int getVersion() {
        return version;
    }

    public T setVersion(int version) {
        this.version = version;
        return (T) this;
    }
}
