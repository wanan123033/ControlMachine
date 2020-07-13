package com.feipulai.host.activity.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import java.io.Serializable;

/**
 * Created by James on 2018/1/3 0003.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DataRetrieveBean implements Serializable {

    private static final long serialVersionUID = -1971119630737674251L;
    private String studentCode;//学号
    private String studentName;//姓名
    private int sex;//性别 0-男  1-女
    private String portrait;
    private int testState;//成绩状态,0未测 1已测
    private String result;//成绩
    private int uploadState;
    private boolean isChecked;//是否被选中,默认不被选中

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public DataRetrieveBean() {
    }

//    public DataRetrieveBean(String studentCode, String studentName, int sex, int testState/*,int result*/) {
//        this.studentCode = studentCode;
//        this.studentName = studentName;
//        this.sex = sex;
//        this.testState = testState;
//        //this.result = result;
//    }

    public DataRetrieveBean(String studentCode, String studentName, int sex, int testState, String result) {
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.sex = sex;
        this.testState = testState;
        this.result = result;
    }

    public DataRetrieveBean(String studentCode, String studentName, int sex, int testState, String result, boolean checked) {
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.sex = sex;
        this.portrait = portrait;
        this.testState = testState;
        this.result = result;
        this.isChecked = checked;
    }

    //    public DataRetrieveBean(String studentCode, String studentName, int sex, int testState, String result, boolean checked) {
//        this.studentCode = studentCode;
//        this.studentName = studentName;
//        this.sex = sex;
//        this.testState = testState;
//        this.result = result;
//        this.isChecked = checked;
//    }
    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getTestState() {
        return testState;
    }

    public void setTestState(int testState) {
        this.testState = testState;
    }

    public String getResult() {
        if (result.equals("-1000"))
            return "";
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getUploadState() {
        return uploadState;
    }

    public void setUploadState(int uploadState) {
        this.uploadState = uploadState;
    }

    public Bitmap getBitmapPortrait() {
        if (TextUtils.isEmpty(portrait)) {
            return null;
        }
        byte[] bytes = Base64.decode(portrait, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }
}
