package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;

import java.time.LocalDateTime;

/// Base domain class extending `MinBaseDomain` with **`updateTime`** and **`version`** managed fields.
///
/// <p>Provides optimistic locking support via the `version` field, which is automatically
/// incremented by the Army framework on each UPDATE operation.</p>
///
/// ### Managed Fields
/// | Field        | Type             | Behavior                              |
/// |--------------|------------------|---------------------------------------|
/// | `createTime` | `LocalDateTime`  | Inherited; auto-set on INSERT         |
/// | `updateTime` | `LocalDateTime`  | Auto-set on INSERT and every UPDATE   |
/// | `version`    | `int`            | Auto-incremented on UPDATE (optimistic lock) |
///
/// @param <T> the concrete domain type for fluent setter chaining
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
