package com.feipulai.host.bean;

import android.text.TextUtils;

import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;

import java.io.Serializable;
import java.util.List;

/**
 * 上传成绩实体类
 * Created by zzs on  2019/1/2
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UploadResults implements Serializable {
    private static final long serialVersionUID = 4760880676791747306L;
    private String examItemCode;//项目代码
    private String studentCode; //准考证号
    private String testNum;//测试次数
    private String hostNumber = SettingHelper.getSystemSetting().getHostId() + "";
    private String machineCode = TestConfigs.sCurrentItem.getMachineCode() + "";
    private List<RoundResultBean> roundResultList;
    private int result;
    private int result2;
    private String testTime;
    private int resultStatus;
    public final static String BEAN_KEY = "UploadResults_KEY";


    public UploadResults(String examItemCode, String studentCode, String testNum,
                         List<RoundResultBean> roundResultList) {

        this.examItemCode = examItemCode;
        this.studentCode = studentCode;
        this.testNum = testNum;
        this.roundResultList = roundResultList;
        RoundResult lastResult;
        //判断成绩类型
        if (TestConfigs.sCurrentItem.getfResultType() == 1) {
            //最后
            lastResult = DBManager.getInstance().queryLastScoreByStuCode(studentCode);

        } else {
            //最好
            lastResult = DBManager.getInstance().queryResultsByStudentCodeIsLastResult(studentCode);
        }
        if (lastResult != null) {
            this.result = lastResult.getResult();
            this.result2 = lastResult.getWeightResult();
            this.testTime = lastResult.getTestTime();
            this.resultStatus = lastResult.getResultState();
        }

    }

    public UploadResults(String examItemCode, String studentCode, String testNum, RoundResult lastResult,
                         List<RoundResultBean> roundResultList) {

        this.examItemCode = examItemCode;
        this.studentCode = studentCode;
        this.testNum = testNum;
        this.roundResultList = roundResultList;

        if (lastResult != null) {
            this.result = lastResult.getResult();
            this.result2 = lastResult.getWeightResult();
            this.testTime = lastResult.getTestTime();
            this.resultStatus = lastResult.getResultState();
        }

    }

    public String getExamItemCode() {
        return examItemCode;
    }

    public void setExamItemCode(String examItemCode) {
        this.examItemCode = examItemCode;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getTestNum() {
        return testNum;
    }

    public void setTestNum(String testNum) {
        this.testNum = testNum;
    }

    public List<RoundResultBean> getRoundResultList() {
        return roundResultList;
    }

    public void setRoundResultList(List<RoundResultBean> roundResultList) {
        this.roundResultList = roundResultList;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getTestTime() {
        return testTime;
    }

    public void setTestTime(String testTime) {
        this.testTime = testTime;
    }

    public int getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(int resultStatus) {
        this.resultStatus = resultStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UploadResults) {
            UploadResults uploadResults = (UploadResults) obj;
            if (TextUtils.equals(studentCode, uploadResults.getStudentCode()) &&
                    TextUtils.equals(testNum, uploadResults.getTestNum())) {
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "UploadResults{" +
                ", examItemCode='" + examItemCode + '\'' +
                ", studentCode='" + studentCode + '\'' +
                ", testNum='" + testNum + '\'' +
                ", hostNumber='" + hostNumber + '\'' +
                ", machineCode='" + machineCode + '\'' +
                ", roundResultList=" + roundResultList +
                '}';
    }

}
