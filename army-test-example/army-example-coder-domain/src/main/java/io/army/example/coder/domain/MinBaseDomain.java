package io.army.example.coder.domain;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;

import java.time.LocalDateTime;

/// Minimal base domain class providing the **`createTime`** managed field.
///
/// <p>All Army domain entities should extend this class (directly or transitively) to inherit
/// the `createTime` column, which is automatically managed by the framework and never updatable.</p>
///
/// <p>This class also defines the `SNOWFLAKE8` constant, a fully-qualified class name for the
/// 8-byte Snowflake ID generator used across the stock example domains.</p>
///
/// ### Inheritance Hierarchy
/// ```
/// MinBaseDomain<T>           → createTime
///   └─ BaseDomain<T>         → + updateTime, version
///       └─ StockBaseDomain<T> → + stockId, date
/// ```
///
/// @param <T> the concrete domain type for fluent setter chaining
@MappedSuperclass
@SuppressWarnings("unchecked")
public abstract class MinBaseDomain<T extends MinBaseDomain<T>> {

    protected static final String START_TIME = "startTime";

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
