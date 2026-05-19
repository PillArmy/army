package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
@SuppressWarnings("unchecked")
public abstract class BaseDomain<T extends BaseDomain<T>> extends MinBaseDomain<T> {


    @Column
    public LocalDateTime updateTime;

    @Column
    public int version;


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
