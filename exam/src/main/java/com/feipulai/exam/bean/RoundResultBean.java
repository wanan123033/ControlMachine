package com.feipulai.exam.bean;

import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.netUtils.CommonUtils;

import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 上传成绩轮次成绩
 * Created by zzs on  2019/2/12
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class RoundResultBean implements Serializable {

    private static final long serialVersionUID = 4270898793752206729L;
    private Long id;//项目轮次成绩ID
    private String studentCode;
    private String itemCode;//默认为default
    private int machineCode;
    private int roundNo;//轮次
    private int testNo;//测试次数
    private int machineResult;// 获取到的机器成绩
    private int penalty;// 判罚值   判罚值有正负
    private int result;//成绩      单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"
    private int isFoul;//成绩状态 是否犯规 0:未检录 1:正常 2:犯规 3:中退 4:弃权 5:测试     体侧系统没有中退和放弃,且犯规均为机器判定的犯规
    private int resultStatus;//成绩状态 是否犯规 0:未检录 1:正常 2:犯规 3:中退 4:弃权 5:测试     体侧系统没有中退和放弃,且犯规均为机器判定的犯规
    private int resultType;//是否为最好成绩 0-不是 1-是     身高体重最后成绩即为最好成绩
    private int examState;//考试类型 0.正常 1.补考，(2.缓考,现没有这功能)
    private String testTime;//测试时间  格式:yyyy-MM-dd HH:mm:ss
    private String printTime;//打印时间 格式:yyyy-MM-dd HH:mm:ss
    private int updateState;//上传状态 0未上传 1上传
    private Long groupId = RoundResult.DEAFULT_GROUP_ID;//分组id
    private String scheduleNo;  //日程编号
    private int stumbleCount;// 绊绳次数
    private int trackNo;//道次
    private byte[] cycleResult;//中长跑每一圈成绩
    private String msEquipment = CommonUtils.getDeviceInfo();
    private String mtEquipment;
    private String userInfo = SettingHelper.getSystemSetting().getUserName();

    private String remark1;
    private String remark2;
    private String remark3;

    public int getResultStatus() {
        return resultStatus;
    }

    public int getTrackNo() {
        return trackNo;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public RoundResultBean(Long id, String studentCode, String itemCode, int machineCode, int roundNo,
                           int testNo, int machineResult, int penalty, int result, int isFoul, int resultType,
                           int examState, String testTime, String printTime, int stumbleCount, int updateState, Long groupId,
                           String scheduleNo, int trackNo, String mtEquipment, String remark1, String remark2, String remark3, byte[] cResult) {
        this.id = id;
        this.studentCode = studentCode;
        this.itemCode = itemCode;
        this.machineCode = machineCode;
        this.roundNo = roundNo;
        this.testNo = testNo;
        this.machineResult = machineResult;
        this.penalty = penalty;
        this.result = result;
        this.isFoul = isFoul;
        this.resultStatus = isFoul;
        this.resultType = resultType;
        this.examState = examState;
        this.testTime = testTime;
        this.printTime = printTime;
        this.updateState = updateState;
        this.groupId = groupId;
        this.scheduleNo = scheduleNo;
        if (trackNo != 0) {

            this.trackNo = trackNo;
        }
        this.stumbleCount = stumbleCount;
        this.mtEquipment = mtEquipment;
        this.remark1 = remark1;
        this.remark2 = remark2;
        this.remark3 = remark3;
        this.cycleResult = cResult;
    }

    public static List<RoundResultBean> beanCope(List<RoundResult> roundResults, Group group) {
        if (roundResults == null) {
            return null;
        }
        List<RoundResultBean> roundResultBeans = new ArrayList<>();
        GroupItem groupItem = null;
        if (group != null) {
            groupItem = DBManager.getInstance().getItemStuGroupItem(group, roundResults.get(0).getStudentCode());
        }

        for (RoundResult roundResult : roundResults) {
            if (roundResult.isDelete()){
                continue;
            }
            roundResultBeans.add(new RoundResultBean(roundResult.getId(), roundResult.getStudentCode(), roundResult.getItemCode(), roundResult.getMachineCode()
                    , roundResult.getRoundNo(), roundResult.getTestNo(), roundResult.getMachineResult(),
                    roundResult.getPenaltyNum(), roundResult.getResult(), roundResult.getResultState()
                    , roundResult.getIsLastResult(), roundResult.getExamType(), roundResult.getTestTime(), roundResult.getEndTime()
                    , roundResult.getStumbleCount(), roundResult.getUpdateState(), roundResult.getGroupId(), roundResult.getScheduleNo(), groupItem == null ? 0 : groupItem.getTrackNo(), roundResult.getMtEquipment(),
                    roundResult.getRemark1(), roundResult.getRemark2(), roundResult.getRemark3(), roundResult.getCycleResult()));
        }
        return roundResultBeans;
    }

    public static List<RoundResultBean> beanCope(List<RoundResult> roundResults) {
        if (roundResults == null) {
            return null;
        }
        List<RoundResultBean> roundResultBeans = new ArrayList<>();
        for (RoundResult roundResult : roundResults) {
            if (roundResult.isDelete()){
                continue;
            }
            roundResultBeans.add(new RoundResultBean(roundResult.getId(), roundResult.getStudentCode(), roundResult.getItemCode(), roundResult.getMachineCode()
                    , roundResult.getRoundNo(), roundResult.getTestNo(), roundResult.getMachineResult(),
                    roundResult.getPenaltyNum(), roundResult.getResult(), roundResult.getResultState()
                    , roundResult.getIsLastResult(), roundResult.getExamType(), roundResult.getTestTime(), roundResult.getEndTime()
                    , roundResult.getStumbleCount(), roundResult.getUpdateState(), roundResult.getGroupId(), roundResult.getScheduleNo(), 0, roundResult.getMtEquipment(),
                    roundResult.getRemark1(), roundResult.getRemark2(), roundResult.getRemark3(), roundResult.getCycleResult()));
        }
        return roundResultBeans;
    }

    public static List<RoundResultBean> beanCope2(RoundResult roundResult, Group group) {
        if (roundResult == null) {
            return null;
        }
        if (roundResult.isDelete()){
            return null;
        }
        List<RoundResultBean> roundResultBeans = new ArrayList<>();
        GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, roundResult.getStudentCode());
        roundResultBeans.add(new RoundResultBean(roundResult.getId(), roundResult.getStudentCode(), roundResult.getItemCode(), roundResult.getMachineCode()
                , roundResult.getRoundNo(), roundResult.getTestNo(), roundResult.getMachineResult(),
                roundResult.getPenaltyNum(), roundResult.getResult(), roundResult.getResultState()
                , roundResult.getIsLastResult(), roundResult.getExamType(), roundResult.getTestTime(), roundResult.getPrintTime()
                , roundResult.getStumbleCount(), roundResult.getUpdateState(), roundResult.getGroupId(), roundResult.getScheduleNo(), groupItem.getTrackNo(), roundResult.getMtEquipment(),
                roundResult.getRemark1(), roundResult.getRemark2(), roundResult.getRemark3(), roundResult.getCycleResult()));
        return roundResultBeans;
    }

    public static List<RoundResultBean> beanCope2(RoundResult roundResult) {
        if (roundResult == null) {
            return null;
        }
        if (roundResult.isDelete()){
            return null;
        }
        List<RoundResultBean> roundResultBeans = new ArrayList<>();
        roundResultBeans.add(new RoundResultBean(roundResult.getId(), roundResult.getStudentCode(), roundResult.getItemCode(), roundResult.getMachineCode()
                , roundResult.getRoundNo(), roundResult.getTestNo(), roundResult.getMachineResult(),
                roundResult.getPenaltyNum(), roundResult.getResult(), roundResult.getResultState()
                , roundResult.getIsLastResult(), roundResult.getExamType(), roundResult.getTestTime(), roundResult.getPrintTime()
                , roundResult.getStumbleCount(), roundResult.getUpdateState(), roundResult.getGroupId(), roundResult.getScheduleNo(), 0, roundResult.getMtEquipment(),
                roundResult.getRemark1(), roundResult.getRemark2(), roundResult.getRemark3(), roundResult.getCycleResult()));
        return roundResultBeans;
    }

    public static List<RoundResult> dbCope(List<RoundResultBean> roundResultBeans) {
        List<RoundResult> roundResults = new ArrayList<>();
        for (RoundResultBean roundResult : roundResultBeans) {
            roundResults.add(new RoundResult(roundResult.getId(), roundResult.getStudentCode(), roundResult.getItemCode(), roundResult.getMachineCode()
                    , roundResult.getRoundNo(), roundResult.getTestNo(), roundResult.getMachineResult(),
                    roundResult.getPenalty(), roundResult.getResult(), roundResult.getIsFoul()
                    , roundResult.getResultType(), roundResult.getExamState(), roundResult.getTestTime(), roundResult.getPrintTime(), roundResult.getPrintTime()
                    , roundResult.getStumbleCount(), roundResult.getUpdateState(), roundResult.getCycleResult(), roundResult.getGroupId(), roundResult.getScheduleNo(), roundResult.getMtEquipment(),
                    roundResult.getRemark1(), roundResult.getRemark2(), roundResult.getRemark3()));
        }
        return roundResults;
    }

    public byte[] getCycleResult() {
        return cycleResult;
    }

    public void setCycleResult(byte[] cycleResult) {
        this.cycleResult = cycleResult;
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

    public int getIsFoul() {
        return isFoul;
    }

    public int getResultType() {
        return resultType;
    }

    public int getExamState() {
        return examState;
    }

    public String getTestTime() {
        return testTime;
    }

    public String getPrintTime() {
        return printTime;
    }

    public int getUpdateState() {
        return updateState;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getScheduleNo() {
        return scheduleNo;
    }

    public int getStumbleCount() {
        return stumbleCount;
    }

    public String getRemark1() {
        return remark1;
    }

    public String getRemark2() {
        return remark2;
    }

    public String getRemark3() {
        return remark3;
    }

    public String getMtEquipment() {
        return mtEquipment;
    }

    public void setMtEquipment(String mtEquipment) {
        this.mtEquipment = mtEquipment;
    }

    @Override
    public String toString() {
        return "RoundResultBean{" +
                "id=" + id +
                ", studentCode='" + studentCode + '\'' +
                ", itemCode='" + itemCode + '\'' +
                ", machineCode=" + machineCode +
                ", roundNo=" + roundNo +
                ", testNo=" + testNo +
                ", machineResult=" + machineResult +
                ", penalty=" + penalty +
                ", result=" + result +
                ", isFoul=" + isFoul +
                ", resultType=" + resultType +
                ", examState=" + examState +
                ", testTime='" + testTime + '\'' +
                ", printTime='" + printTime + '\'' +
                ", updateState=" + updateState +
                ", groupId=" + groupId +
                ", scheduleNo='" + scheduleNo + '\'' +
                ", stumbleCount=" + stumbleCount +
                ", msEquipment='" + msEquipment + '\'' +
                ", mtEquipment='" + mtEquipment + '\'' +
                ", userInfo='" + userInfo + '\'' +
                ", remark1='" + remark1 + '\'' +
                ", remark2='" + remark2 + '\'' +
                ", remark3='" + remark3 + '\'' +
                '}';
    }

}
