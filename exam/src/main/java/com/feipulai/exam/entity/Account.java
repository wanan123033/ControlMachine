package com.feipulai.exam.entity;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by zzs on  2021/1/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Entity
public class Account {
    @Id(autoincrement = true)
    private Long id;
    @Unique
    @NotNull
    private String account;
    @NotNull
    private String password;


    private int type;//0 超级管理员  1操作员


    private Long createTime;
    private Long updateTime;

    public Account(Long id, @NotNull String account, @NotNull String password,
                   int type) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.type = type;
    }

    @Generated(hash = 882125521)
    public Account() {
    }

    @Generated(hash = 1957355252)
    public Account(Long id, @NotNull String account, @NotNull String password,
            int type, Long createTime, Long updateTime) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.type = type;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
