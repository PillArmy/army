package io.army.example.stock.domain;

import io.army.annotation.Column;
import io.army.annotation.Table;

import java.time.LocalDateTime;

@Table(name = "stock_user", comment = "股票用户")
public class StockUser {

    @Column
    private long id;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;

    @Column
    private int version;

    @Column(precision = 30, comment = "用户名称")
    private String userName;


    @Column(precision = Column.DEFAULT_EXP, comment = "用户性别")
    private Gender gender;

    @Column(comment = "最后登录时间")
    private LocalDateTime lastLoginTime;


    public long getId() {
        return id;
    }

    public StockUser setId(long id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public StockUser setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public StockUser setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public StockUser setVersion(int version) {
        this.version = version;
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
