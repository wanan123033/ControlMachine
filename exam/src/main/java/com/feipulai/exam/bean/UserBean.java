package com.feipulai.exam.bean;

import java.io.Serializable;

/**
 * 用户信息
 * Created by zzs on  2018/12/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UserBean implements Serializable {
    private String token;
    private String examName;//考试名称

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "token='" + token + '\'' +
                ", examName='" + examName + '\'' +
                '}';
    }
}
