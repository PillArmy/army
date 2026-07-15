package io.army.example.coder.domain;

import io.army.annotation.Column;
import io.army.annotation.Generator;
import io.army.annotation.Param;
import io.army.annotation.Table;

import java.time.LocalDateTime;

@Table(name = "user", comment = "用户")
public class User extends BaseDomain<User> {

    @Generator(value = SNOWFLAKE8, params = @Param(name = START_TIME, value = "1779014276675"))
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

    public User setId(long id) {
        this.id = id;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public User setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public User setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public User setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
        return this;
    }
}
