package com.feipulai.exam.bean;

import android.text.TextUtils;

import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;

import java.io.Serializable;
import java.util.List;

/**
 * 上传成绩实体类
 * Created by zzs on  2019/1/2
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UploadResults implements Serializable {
    private static final long serialVersionUID = 4760880676791747306L;
    private String siteScheduleNo;//日程编号
    private String examItemCode;//项目代码
    private String studentCode; //准考证号
    private String testNum;//测试次数
    private String groupNo;  //组号
    private String hostNumber = SettingHelper.getSystemSetting().getHostId() + "";
    private String machineCode = TestConfigs.sCurrentItem.getMachineCode() + "";
    private List<RoundResultBean> roundResultList;
    public final static String BEAN_KEY = "UploadResults_KEY";
    //打印成绩使用
    private long groupId;
    private int result;
    private String testTime;
    private int resultStatus;

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public UploadResults(String siteScheduleNo, String itemCode, String studentCode, String testNum, String groupNo, List<RoundResultBean> roundResultList) {
        this.siteScheduleNo = siteScheduleNo;
        this.examItemCode = itemCode;
        this.studentCode = studentCode;
        this.testNum = testNum;
        this.groupNo = groupNo;
        this.roundResultList = roundResultList;
        RoundResult lastResult;
        if (TestConfigs.sCurrentItem.getLastResultMode() == 1) {
            //最后
            lastResult = DBManager.getInstance().queryBestFinallyScore(studentCode, Integer.valueOf(testNum));
        } else {
            //最好的
            lastResult = DBManager.getInstance().queryBestScore(studentCode, Integer.valueOf(testNum));
        }
        if (lastResult != null) {
            this.result = lastResult.getResult();
            this.resultStatus = lastResult.getResultState();
            this.testTime = lastResult.getTestTime();
        }
    }

    public String getSiteScheduleNo() {
        return siteScheduleNo;
    }

    public void setSiteScheduleNo(String siteScheduleNo) {
        this.siteScheduleNo = siteScheduleNo;
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

    public String getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public List<RoundResultBean> getRoundResultList() {
        return roundResultList;
    }

    public void setRoundResultList(List<RoundResultBean> roundResultList) {
        this.roundResultList = roundResultList;
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
                "siteScheduleNo='" + siteScheduleNo + '\'' +
                ", examItemCode='" + examItemCode + '\'' +
                ", studentCode='" + studentCode + '\'' +
                ", testNum='" + testNum + '\'' +
                ", groupNo='" + groupNo + '\'' +
                ", hostNumber='" + hostNumber + '\'' +
                ", machineCode='" + machineCode + '\'' +
                ", roundResultList=" + roundResultList +
                '}';
    }

}
