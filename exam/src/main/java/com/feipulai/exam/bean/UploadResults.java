package com.feipulai.exam.bean;

import android.text.TextUtils;

import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.RoundResult;

import org.greenrobot.greendao.annotation.NotNull;

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
    private String groupNo = "";  //组号
    private int groupType;//分组性别（0.男子 1.女子 2.混合）
    private String sortName;//组别
    private String hostNumber = SettingHelper.getSystemSetting().getHostId() + "";
    private String machineCode = TestConfigs.sCurrentItem.getMachineCode() + "";
    private List<RoundResultBean> roundResultList;
    public final static String BEAN_KEY = "UploadResults_KEY";
    //打印成绩使用
    private long groupId;
    private int result;//最终成绩
    private String testTime;//最终成绩测试时间
    private int resultStatus;//最终成绩状态
    private int examState;//

    public int getExamState() {
        return examState;
    }

    public int getResult() {
        return result;
    }

    public int getResultStatus() {
        return resultStatus;
    }

    public String getTestTime() {
        return testTime;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public UploadResults(String siteScheduleNo, String itemCode, String studentCode, String testNum, Group group, List<RoundResultBean> roundResultList) {
        this.siteScheduleNo = siteScheduleNo;
        this.examItemCode = itemCode;
        this.studentCode = studentCode;
        this.testNum = testNum;
        if (group != null) {
            this.groupNo = group.getGroupNo() + "";
            this.groupType = group.getGroupType();
            this.sortName = group.getSortName();
        }

        this.roundResultList = roundResultList;
        RoundResult lastResult;
        Item item=DBManager.getInstance().queryItemByCode(itemCode);
        if (item != null){
            if (item.getLastResultMode() == 1) {
                //最后
                lastResult = DBManager.getInstance().queryBestFinallyScore(item,studentCode, Integer.valueOf(testNum));
            } else {
                //最好的
                lastResult = DBManager.getInstance().queryBestScore(item,studentCode, Integer.valueOf(testNum));
            }
            if (lastResult != null) {
                this.result = lastResult.getResult();
                this.resultStatus = lastResult.getResultState();
                this.testTime = lastResult.getTestTime();
                this.examState=lastResult.getExamType();
            }
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

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public String getHostNumber() {
        return hostNumber;
    }

    public void setHostNumber(String hostNumber) {
        this.hostNumber = hostNumber;
    }

    public String getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(String machineCode) {
        this.machineCode = machineCode;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public void setTestTime(String testTime) {
        this.testTime = testTime;
    }

    public void setResultStatus(int resultStatus) {
        this.resultStatus = resultStatus;
    }

    public void setExamState(int examState) {
        this.examState = examState;
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
                ", groupType=" + groupType +
                ", sortName='" + sortName + '\'' +
                ", hostNumber='" + hostNumber + '\'' +
                ", machineCode='" + machineCode + '\'' +
                ", roundResultList=" + roundResultList +
                ", groupId=" + groupId +
                ", result=" + result +
                ", testTime='" + testTime + '\'' +
                ", resultStatus=" + resultStatus +
                ", examState=" + examState +
                '}';
    }

}
