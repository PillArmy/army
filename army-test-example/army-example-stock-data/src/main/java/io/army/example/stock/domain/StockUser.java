package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.Generator;
import io.army.annotation.Param;
import io.army.annotation.Table;
import io.army.generator.snowflake.Snowflake8Generator;

import java.time.LocalDateTime;


/// Domain entity representing a **stock application user**.
///
/// <p>Maps to the `stock_user` table. Each user has a name, gender, and last login timestamp.
/// A default user is seeded during application startup via `StockSessionFactoryAdvisor`.</p>
///
/// @see Gender
/// @see BaseDomain
@Table(name = "stock_user", comment = "股票用户")
public class StockUser extends BaseDomain<StockUser> {

    @Generator(value = SNOWFLAKE8, params = @Param(name = Snowflake8Generator.START_TIME, value = "1779014276675"))
    @Column
    private long id;


    @Column(notNull = true, precision = 30, comment = "用户名称")
    private String userName;


    @Column(notNull = true, defaultValue = "'UNKNOWN'", precision = Column.DEFAULT_EXP, comment = "用户性别")
    private Gender gender;

    @Column(notNull = true, defaultValue = "'1970-01-01 00:00:00'", comment = "最后登录时间")
    private LocalDateTime lastLoginTime;

    public long getId() {
        return id;
    }

    public StockUser setId(long id) {
        this.id = id;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public StockUser setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public StockUser setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public StockUser setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
        return this;
    }
}
