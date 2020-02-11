package com.feipulai.host.bean;


import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.netUtils.CommonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 上传成绩轮次成绩
 * Created by zzs on  2019/2/12
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class RoundResultBean implements Serializable {
    private Long id;//项目轮次成绩ID
    private String studentCode;
    private String itemCode;//默认为default
    private int machineCode;
    private int roundNo;//轮次
    private int testNo;//测试次数
    private int machineResult;// 获取到的机器成绩
    private int penalty;// 判罚值   判罚值有正负
    private int result;//成绩      单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"
    private int result2;//成绩      单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"
    private int resultStatus;//成绩状态 是否犯规 0:未检录 1:正常 2:犯规 3:中退 4:弃权 5:测试     体侧系统没有中退和放弃,且犯规均为机器判定的犯规
    private int resultType;//是否为最好成绩 0-不是 1-是     身高体重最后成绩即为最好成绩
    private String testTime;//测试时间  格式:yyyy-MM-dd HH:mm:ss
    private String printTime;//打印时间 格式:yyyy-MM-dd HH:mm:ss
    private int stumbleCount;// 绊绳次数
    private String msEquipment = CommonUtils.getDeviceInfo();
    private String mtEquipment;
    private String userInfo = SettingHelper.getSystemSetting().getUserName();

    public RoundResultBean(Long id, String studentCode, String itemCode, int machineCode,
                           int roundNo, int testNo, int machineResult, int result,
                           int result2, int resultStatus, int resultType, String testTime, String printTime, int stumbleCount) {
        this.id = id;
        this.studentCode = studentCode;
        this.itemCode = itemCode;
        this.machineCode = machineCode;
        this.roundNo = roundNo;
        this.testNo = testNo;
        this.machineResult = machineResult;
        this.result = result;
        this.result2 = result2;
        this.resultStatus = resultStatus;
        this.resultType = resultType;
        this.testTime = testTime;
        this.printTime = printTime;
        this.stumbleCount = stumbleCount;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }


    public static List<RoundResultBean> beanCope(List<RoundResult> roundResults) {
        if (roundResults == null) {
            return null;
        }
        List<RoundResultBean> roundResultBeans = new ArrayList<>();
        for (RoundResult roundResult : roundResults) {
            roundResultBeans.add(new RoundResultBean(roundResult.getId(), roundResult.getStudentCode(), roundResult.getItemCode(), roundResult.getMachineCode()
                    , roundResult.getRoundNo(), roundResult.getTestNo(), roundResult.getResult(),
                    roundResult.getResult(), roundResult.getWeightResult(), roundResult.getResultState()
                    , roundResult.getIsLastResult(), roundResult.getTestTime(), roundResult.getPrintTime()
                    , roundResult.getStumbleCount()));
        }
        return roundResultBeans;
    }

    public static List<RoundResultBean> beanCope2(RoundResult roundResult) {
        if (roundResult == null) {
            return null;
        }
        List<RoundResultBean> roundResultBeans = new ArrayList<>();
        roundResultBeans.add(new RoundResultBean(roundResult.getId(), roundResult.getStudentCode(), roundResult.getItemCode(), roundResult.getMachineCode()
                , roundResult.getRoundNo(), roundResult.getTestNo(), roundResult.getResult(),
                roundResult.getResult(), roundResult.getWeightResult(), roundResult.getResultState()
                , roundResult.getIsLastResult(), roundResult.getTestTime(), roundResult.getPrintTime()
                , roundResult.getStumbleCount()));
        return roundResultBeans;
    }

    public static List<RoundResult> dbCope(List<RoundResultBean> roundResultBeans) {
        List<RoundResult> roundResults = new ArrayList<>();
        for (RoundResultBean roundResult : roundResultBeans) {
            roundResults.add(new RoundResult(roundResult.getId(), roundResult.getStudentCode(), roundResult.getItemCode(), roundResult.getMachineCode()
                    , roundResult.getRoundNo(), roundResult.getResult(), roundResult.getResult2()
                    , roundResult.getStumbleCount(), roundResult.getResultStatus()
                    , roundResult.getResultType(), roundResult.getTestTime(), roundResult.getPrintTime()
                    , roundResult.getTestNo()
            ));
        }
        return roundResults;
    }

    public Long getId() {
        return id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public String getItemCode() {
        return itemCode;
    }

    public int getMachineCode() {
        return machineCode;
    }

    public int getRoundNo() {
        return roundNo;
    }

    public int getTestNo() {
        return testNo;
    }

    public int getMachineResult() {
        return machineResult;
    }

    public int getPenalty() {
        return penalty;
    }

    public int getResult() {
        return result;
    }


    public int getResultType() {
        return resultType;
    }


    public String getTestTime() {
        return testTime;
    }

    public String getPrintTime() {
        return printTime;
    }


    public int getStumbleCount() {
        return stumbleCount;
    }

    public int getResult2() {
        return result2;
    }

    public int getResultStatus() {
        return resultStatus;
    }

    public String getMsEquipment() {
        return msEquipment;
    }

    public String getMtEquipment() {
        return mtEquipment;
    }

    public String getUserInfo() {
        return userInfo;
    }
}
