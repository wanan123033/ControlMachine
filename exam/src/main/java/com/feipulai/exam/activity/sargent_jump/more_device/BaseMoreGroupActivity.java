package com.feipulai.exam.activity.sargent_jump.more_device;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.adapter.StuAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.DeviceListAdapter;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.service.UploadService;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseMoreGroupActivity extends BaseCheckActivity {
    @BindView(R.id.txt_group_name)
    TextView txtGroupName;
    @BindView(R.id.rv_device_list)
    RecyclerView rvDeviceList;
    @BindView(R.id.rv_test_stu)
    RecyclerView rvTestStu;
    private LEDManager mLEDManager;
    private List<Student> studentList;
    private Group group;
    private DeviceListAdapter deviceListAdapter;
    private StuAdapter stuAdapter;
    private int MAX_COUNT = 4;
    public List<DeviceDetail> deviceDetails = new ArrayList<>();
    private int deviceCount;
    /**
     * 是否停止测试
     */
    private boolean isStop = true;
    /**
     * 当前测试次数位
     */
    private int roundNo = 1;
    private List<BaseStuPair> pairList;
    private StuHandler stuHandler = new StuHandler();

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_more;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        PrinterManager.getInstance().init();
        group = (Group) TestConfigs.baseGroupMap.get("group");

        initData();
        mLEDManager = new LEDManager();
        mLEDManager.link(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

        rvTestStu.setLayoutManager(new LinearLayoutManager(this));

        //给 界面左侧recyclerView 添加学生
        studentList = new ArrayList<>();
        pairList = new ArrayList<>();
        pairList.addAll((List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu"));
        for (BaseStuPair pair : pairList) {
            studentList.add(pair.getStudent());
        }
        stuAdapter = new StuAdapter(studentList);
        rvTestStu.setAdapter(stuAdapter);

        StringBuffer sbName = new StringBuffer();
        sbName.append(group.getGroupType() == Group.MALE ? "男子" :
                (group.getGroupType() == Group.FEMALE ? "女子" : "男女混合"));
        sbName.append(group.getSortName() + String.format("第%1$d组", group.getGroupNo()));
        txtGroupName.setText(sbName);

        getTestStudent(group);
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
        deviceDetails.clear();
        for (int i = 0; i < deviceCount; i++) {
            DeviceDetail detail = new DeviceDetail();
            detail.getStuDevicePair().getBaseDevice().setDeviceId(i + 1);
            detail.setDeviceOpen(true);
            deviceDetails.add(detail);
        }
        initView();
    }

    private void initView() {
        deviceListAdapter = new DeviceListAdapter(deviceDetails);
        deviceListAdapter.setTestCount(3);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        rvDeviceList.setLayoutManager(layoutManager);
        rvDeviceList.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int pos) {
                switch (view.getId()) {
                    case R.id.txt_skip://跳过
                        toSkip(pos);
                        break;
                    case R.id.txt_start://开始
                        if (deviceDetails.get(pos).getStuDevicePair().getBaseDevice().getState() != BaseDeviceState.STATE_ERROR)
                            toStart(pos);
                        break;
                }
            }
        });
    }

    public abstract void toStart(int pos);

    protected void toSkip(int pos) {
        deviceDetails.get(pos).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        deviceDetails.get(pos).getStuDevicePair().setCanTest(true);
        if (deviceDetails.get(pos).getStuDevicePair().getStudent() != null) {
            Logger.i("studentSkip=>跳过考生：" + deviceDetails.get(pos).getStuDevicePair().getStudent().getStudentName());
            deviceDetails.get(pos).getStuDevicePair().setStudent(null);
            deviceListAdapter.notifyItemChanged(pos);
        }
        //跳过成绩保存
        if (studentList == null || studentList.size() == 0 || stuAdapter.getTestPosition() == -1) {
            return;
        }
        //是否开启身份验证
        if (SettingHelper.getSystemSetting().isIdentityMark()) {
            //学生在分组中是否有进行检入
            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group
                    , studentList.get(stuAdapter.getTestPosition()).getStudentCode());
            if (groupItem.getIdentityMark() == 0) {
                return;
            }
        }

        if (stuAdapter.getTestPosition() == studentList.size() - 1 && roundNo >= setTestCount()) {
            //全部次数测试完，
            toastSpeak("当前小组所有人员都测试完");
            return;
        }

        if (stuAdapter.getTestPosition() == studentList.size() - 1) {//进行下一轮测试

            for (int i = 0; i < studentList.size(); i++) {
                //todo 判断是否可被测试 其他机器是否包含学员
                if (deviceDetails.get(pos).isDeviceOpen()) {
                    deviceDetails.get(pos).getStuDevicePair().setStudent(studentList.get(i));
                    deviceListAdapter.notifyItemChanged(pos);
                    stuAdapter.setTestPosition(i);
                    stuAdapter.notifyDataSetChanged();
                    if (setTestPattern() == 0){

                    }
                    roundNo++;
                }
                return;
            }
        }

        for (int i = stuAdapter.getTestPosition() + 1; i < studentList.size(); i++) {
            if (deviceDetails.get(pos).isDeviceOpen()) {
                deviceDetails.get(pos).getStuDevicePair().setStudent(studentList.get(i));
                deviceListAdapter.notifyItemChanged(pos);
                stuAdapter.setTestPosition(i);
                stuAdapter.notifyDataSetChanged();
            }

            return;
        }

    }

    private void allTestComplete() {
        //全部次数测试完，
        toastSpeak("分组考生全部测试完成，请选择下一组");
        roundNo = 1;
        stuAdapter.setTestPosition(-1);
        stuAdapter.notifyDataSetChanged();
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
    }

    /**
     * 获取考生
     *
     * @param group
     */
    private void getTestStudent(Group group) {
        stuAdapter.setTestPosition(-1);
        //身份验证模式
        if (SettingHelper.getSystemSetting().isIdentityMark()) {
            stuAdapter.notifyDataSetChanged();
            return;
        }

        if (setTestPattern() == 0) {//连续模式
            for (int i = 0; i < studentList.size(); i++) {
                //  查询学生成绩 当有成绩则添加数据跳过测试
                List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                        (studentList.get(i).getStudentCode(), group.getId() + "");

                if (roundResultList == null || roundResultList.size() == 0 || roundResultList.size() < setTestCount()) {

                    roundNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;
                    allotStudent(i, roundResultList);

                }
            }
        } else {//循环测试

            for (int i = 0; i < studentList.size(); i++) {
                //查询学生成绩
                List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                        (studentList.get(i).getStudentCode(), group.getId() + "");

                if (roundResultList== null || roundResultList.size() ==0 ){
                    allotStudent(i, roundResultList);
                }

                if (roundResultList.size()>0){
                    pairList.get(i).setResultState(-99);
                    String[] result = new String[setTestCount()];
                    for (int j = 0; j < roundResultList.size(); j++) {
                        switch (roundResultList.get(j).getResultState()) {
                            case RoundResult.RESULT_STATE_FOUL:
                                result[j] = "X";
                                break;
                            case -2:
                                result[j] = "中退";
                                break;
                            default:
                                result[j] = ResultDisplayUtils.getStrResultForDisplay(roundResultList.get(j).getResult());
                                break;
                        }

                    }
                    pairList.get(i).setTimeResult(result);
                }


            }

            if (stuAdapter.getTestPosition() == -1){
                for (int i = roundNo; i <= setTestCount(); i++) {
                    for (int j = 0; j < pairList.size(); j++) {
                        if (TextUtils.isEmpty(pairList.get(j).getTimeResult()[i - 1])) {
                            roundNo = i;
                            int index = -1;
                            for (DeviceDetail detail : deviceDetails) {
                                index++;
                                if (detail.getStuDevicePair().isCanTest() && detail.isDeviceOpen()) {
                                    detail.getStuDevicePair().setCanTest(false);
                                    detail.getStuDevicePair().setStudent(studentList.get(j));
                                    detail.getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                                    deviceDetails.get(index).getStuDevicePair().setTimeResult(pairList.get(j).getTimeResult());
                                    deviceListAdapter.notifyDataSetChanged();
                                    rvTestStu.scrollToPosition(j);
                                    stuAdapter.setTestPosition(j);
                                    deviceListAdapter.notifyItemChanged(index);
                                }
                            }

                        }
                    }
                }
            }

        }

    }

    /**
     * 分配考生
     */
    private void allotStudent(int stuPos, List<RoundResult> roundResultList) {
        int index = -1;
        for (DeviceDetail detail : deviceDetails) {
            index++;
            if (detail.getStuDevicePair().isCanTest() && detail.isDeviceOpen()) {
                detail.getStuDevicePair().setCanTest(false);
                detail.getStuDevicePair().setStudent(studentList.get(stuPos));
                detail.getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                setStuPairsData(index, roundResultList);
                rvTestStu.scrollToPosition(stuPos);
                stuAdapter.setTestPosition(stuPos);
                deviceListAdapter.notifyItemChanged(index);
                break;
            }
        }
    }

    /**
     * 设置位置考生已测成绩
     *
     * @param index
     * @param roundResultList
     */
    public void setStuPairsData(int index, List<RoundResult> roundResultList) {
        String[] result = new String[setTestCount()];

        for (int j = 0; j < roundResultList.size(); j++) {
            switch (roundResultList.get(j).getResultState()) {
                case RoundResult.RESULT_STATE_FOUL:
                    result[j] = "X";
                    break;
                case -2:
                    result[j] = "中退";
                    break;
                default:
                    result[j] = ResultDisplayUtils.getStrResultForDisplay(roundResultList.get(j).getResult());
                    break;
            }
        }
        deviceDetails.get(index).getStuDevicePair().setTimeResult(result);
        deviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCheckIn(Student student) {
        Logger.i("onCheckIn====>" + student.toString());
        if (student == null) {
            toastSpeak("该考生不存在");
            return;
        }
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            toastSpeak("无此项目");
            return;
        }

        //是否开启身份验证
        if (SettingHelper.getSystemSetting().isIdentityMark() && studentList.size() > 0) {
            //考生分组测试的成绩
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
            //学生在分组中是否有进行检入
            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, student.getStudentCode());
            if ((roundResultList.size() == 0 || roundResultList.size() < setTestCount()) && groupItem != null) {
                isStop = false;
                roundNo = roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;
                gotoTest(student);
                groupItem.setIdentityMark(1);
                DBManager.getInstance().updateStudentGroupItem(groupItem);
            } else if (groupItem == null) {//没报名
                toastSpeak(student.getSpeakStuName() + "考生没有在选择的分组内，无法测试",
                        student.getStudentName() + "考生没有在选择的分组内，无法测试");
            } else if (roundResultList.size() > 0) {
                toastSpeak(student.getSpeakStuName() + "考生已测试完成",
                        student.getStudentName() + "考生已测试完成");
            }
        }
    }

    /**
     * 定位考生测试
     *
     * @param student
     */
    private void gotoTest(Student student) {
        for (int i = 0; i < deviceDetails.size(); i++) {
            BaseStuPair pair = deviceDetails.get(i).getStuDevicePair();
            if (pair.isCanTest()
                    && pair.getStudent() == null) {
                pair.setCanTest(false);
                pair.setStudent(student);
            }

            break;
        }

    }

    public void updateDevice(BaseDeviceState deviceState) {
        int deviceId = deviceState.getDeviceId();
        BaseStuPair pair = null;
        int index = 0;
        for (int i = 0; i < deviceCount; i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            if (id == deviceId) {
                pair = deviceDetails.get(i).getStuDevicePair();
                index = i;
                break;
            }

        }

        if (pair.getBaseDevice() != null) {
            pair.getBaseDevice().setState(deviceState.getState());
            //状态为测试已结束
            if (deviceState.getState() == BaseDeviceState.STATE_END) {
                if (pair.getStudent() != null) {
                    Logger.i("考生" + pair.getStudent().toString());
                }
                Logger.i("设备成绩信息STATE_END==>" + deviceState.toString());
                pair.setCanTest(true);
                doResult(pair, index);
            }
        }

        if (deviceDetails.get(index).getStuDevicePair().getBaseDevice() != null) {
            deviceListAdapter.notifyItemChanged(index);
        }
    }


    public synchronized void updateTestResult(@NonNull BaseStuPair baseStu) {
        int deviceId = baseStu.getBaseDevice().getDeviceId();
        int index = 0;
        for (int i = 0; i < deviceCount; i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            if (id == deviceId) {
                index = i;
                break;
            }

        }
        //更新成绩
        String[] timeResult = deviceDetails.get(index).getStuDevicePair().getTimeResult();
        for (int i = 0;i<timeResult.length;i++){
            if (TextUtils.isEmpty(timeResult[i])){
                timeResult[i] = (baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" :
                        ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
                break;
            }
        }

        //满分跳过
        Student student = deviceDetails.get(index).getStuDevicePair().getStudent();
        int stuIndex = studentList.indexOf(student);
        pairList.get(stuIndex).setFullMark(baseStu.isFullMark());
        pairList.get(stuIndex).setResultState(baseStu.getResultState());
        pairList.get(stuIndex).setTimeResult(timeResult);
        deviceDetails.get(index).getStuDevicePair().setTimeResult(timeResult);
        deviceListAdapter.notifyItemChanged(index);

        //TODO LED显示
    }
    //处理结果
    private void doResult(BaseStuPair pair, int index) {
        Logger.i("考生" + pair.getStudent().toString());
        //保存成绩
        saveResult(pair);
        printResult(pair);
        //分配考生到机器中
        matchStudent(pair,index);
    }

    /**
     * 分派考生到机器中
     * @param pair
     * @param index
     */
    private void matchStudent(BaseStuPair pair,int index) {

        //非身份验证模式
        if (!SettingHelper.getSystemSetting().isIdentityMark()) {
            //连续测试
            if (setTestPattern() == 0) {
                if (roundNo < setTestCount()) {
                    if (pair.isFullMark() && pair.getResultState() == RoundResult.RESULT_STATE_NORMAL){
                        //测试下一个
                        continuousTestNext(index);
                    }else {//继续测试同一个
                        roundNo++;
                        toastSpeak(String.format(getString(R.string.test_speak_hint),
                                studentList.get(index).getSpeakStuName(), roundNo),
                                String.format(getString(R.string.test_speak_hint), studentList.get(index).getStudentName(), roundNo));
                        group.setIsTestComplete(2);
                        DBManager.getInstance().updateGroup(group);
                    }
                }else {//测试下一个
                    continuousTestNext(index);
                }
                pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                pair.setCanTest(false);

            } else {
                //循环是否测试到最后一位
                if (stuAdapter.getTestPosition() == studentList.size() - 1) {
                    //是否为最后一次测试，开启新的测试
                    if (setTestCount() > roundNo) {
                        roundNo++;
                        //设置测试学生，当学生有满分跳过则寻找需要测试学生
                        stuAdapter.setTestPosition(0);
                        loopTestNext(index);
                        return;
                    } else {
                        //全部次数测试完，
                        allTestComplete();
                        return;
                    }
                }
                //设置测试学生，当学生有满分跳过则寻找需要测试学生
                stuAdapter.setTestPosition(stuAdapter.getTestPosition() + 1);
                loopTestNext(index);
            }
        } else {
            //身份验证下一轮
            identityMarkTest();
        }
    }

    /**
     * 循环测试下一个
     */
    private void loopTestNext(int index) {
        for (int i = roundNo; i <= setTestCount(); i++) {
            for (int j = stuAdapter.getTestPosition(); j < studentList.size(); j++) {
                if (TextUtils.isEmpty(pairList.get(j).getTimeResult()[i - 1])) {
                    if (pairList.get(j).isFullMark() && pairList.get(j).getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                        continue;
                    }
                    roundNo = i;
                    stuAdapter.setTestPosition(j);
                    toastSpeak(String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getSpeakStuName(), roundNo),
                            String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getStudentName(), roundNo));

                    deviceDetails.get(index).getStuDevicePair().setTimeResult(pairList.get(j).getTimeResult());
                    deviceDetails.get(index).getStuDevicePair().setStudent(studentList.get(j));
                    Message msg = new Message();
                    msg.obj = studentList.get(stuAdapter.getTestPosition());
                    stuHandler.sendMessageDelayed(msg, 3000);
                    Logger.i("下一位测试考生：" + studentList.get(stuAdapter.getTestPosition()));
                    group.setIsTestComplete(2);
                    DBManager.getInstance().updateGroup(group);
                    return;
                }
            }
        }
    }

    /**
     * 连续测试测下一个
     */
    private void continuousTestNext(int index) {
        if (stuAdapter.getTestPosition() == studentList.size() - 1) {
            //全部次数测试完，
            allTestComplete();
            return;
        }

        for (int i = (stuAdapter.getTestPosition() + 1); i < studentList.size(); i++) {
            //  查询学生成绩 当有成绩则添加数据跳过测试
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                    (studentList.get(i).getStudentCode(), group.getId() + "");
            if (roundResultList == null || roundResultList.size() == 0 || roundResultList.size() < setTestCount()) {
                roundNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;
                stuAdapter.setTestPosition(i);
                deviceDetails.get(index).getStuDevicePair().setTimeResult(pairList.get(i).getTimeResult());
                deviceDetails.get(index).getStuDevicePair().setStudent(studentList.get(i));
                Message msg = new Message();
                msg.obj = studentList.get(stuAdapter.getTestPosition());
                stuHandler.sendMessageDelayed(msg, 3000);
                Logger.i("addStudent:" + studentList.get(i).toString());
                Logger.i("addStudent:当前考生进行第" + 1 + "次的第" + roundNo + "轮测试");
                group.setIsTestComplete(2);
                DBManager.getInstance().updateGroup(group);
                return;
            }
        }

    }

    private void identityMarkTest() {
        //TODO
    }

    private void printResult(BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        //是否已全部次数测试完成，非满分跳过
        if (roundNo < setTestCount() && !baseStuPair.isFullMark()) {
            return;
        }
        print(baseStuPair);
    }

    private void print(BaseStuPair baseStuPair) {
        Student student = baseStuPair.getStudent();
//        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机  " + group.getGroupNo() + "组");
        PrinterManager.getInstance().print("序  号:" + baseStuPair.getTrackNo() + "");
        PrinterManager.getInstance().print("考  号:" + student.getStudentCode() + "");
        PrinterManager.getInstance().print("姓  名:" + student.getStudentName() + "");
        for (int i = 0; i < baseStuPair.getTimeResult().length; i++) {
            if (!TextUtils.isEmpty(baseStuPair.getTimeResult()[i])) {
                PrinterManager.getInstance().print(String.format("第%1$d次：", i + 1) + baseStuPair.getTimeResult()[i] + "");
            } else {
                PrinterManager.getInstance().print(String.format("第%1$d次：", i + 1) + "");
            }
        }
        PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()) + "");
        PrinterManager.getInstance().print(" \n");
    }


    private void saveResult(BaseStuPair baseStuPair) {
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setMachineResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
//        roundResult.setTestTime(TestConfigs.df.format(Calendar.getInstance().getTime()));
        roundResult.setTestTime(System.currentTimeMillis() + "");
        roundResult.setRoundNo(roundNo);
        roundResult.setTestNo(1);
        roundResult.setGroupId(group.getId());
        roundResult.setExamType(group.getExamType());
        roundResult.setScheduleNo(group.getScheduleNo());
        roundResult.setUpdateState(0);
        RoundResult bestResult = DBManager.getInstance().queryGroupBestScore(baseStuPair.getStudent().getStudentCode(), group.getId());
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStuPair.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // 这个时候就要同时修改这两个成绩了
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
                updateLastResultLed(roundResult);
            } else {
                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                    updateLastResultLed(roundResult);
                } else {
                    roundResult.setIsLastResult(0);
                    updateLastResultLed(bestResult);
                }
            }
        } else {
            // 第一次测试
            roundResult.setIsLastResult(1);
            updateLastResultLed(roundResult);
        }

        DBManager.getInstance().insertRoundResult(roundResult);
        Logger.i("saveResult==>insertRoundResult->" + roundResult.toString());

        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(group.getScheduleNo()
                , TestConfigs.getCurrentItemCode(), baseStuPair.getStudent().getStudentCode()
                , "1", group.getGroupNo() + "", RoundResultBean.beanCope(roundResultList));

        uploadResult(uploadResults);
    }

    private void uploadResult(UploadResults uploadResults) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            Intent serverIntent = new Intent(this, UploadService.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(UploadResults.BEAN_KEY, uploadResults);
            serverIntent.putExtras(bundle);
            startService(serverIntent);
        }
    }

    private void updateLastResultLed(RoundResult roundResult) {
        //TODO
    }


    /**
     * 设置项目测试次数
     */
    public abstract int setTestCount();

    public abstract void initData();

    /**
     * 设置项目测试次数 0 连续 1 循环
     */
    public abstract int setTestPattern();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
        Intent serverIntent = new Intent(this, UploadService.class);
        stopService(serverIntent);
    }

    class StuHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            deviceListAdapter.notifyDataSetChanged();
            rvTestStu.scrollToPosition(stuAdapter.getTestPosition());
            stuAdapter.notifyDataSetChanged();
        }
    }
}
