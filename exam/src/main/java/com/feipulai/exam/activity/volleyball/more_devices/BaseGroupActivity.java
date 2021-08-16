package com.feipulai.exam.activity.volleyball.more_devices;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.VolleyBallRadioManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.adapter.StuAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSettingActivity;
import com.feipulai.exam.activity.volleyball.adapter.DeviceListAdapter;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public abstract class BaseGroupActivity extends BaseCheckActivity {
    private Group group;
    private List<Student> studentList;

    private List<BaseStuPair> pairList;
    private StuAdapter stuAdapter;
    protected DeviceListAdapter deviceListAdapter;
    protected List<DeviceDetail> deviceDetails = new ArrayList<>();
    protected VolleyBallSetting setting;
    protected LEDManager mLEDManager;
    private boolean addStudent = true;


    @BindView(R.id.rv_testing_pairs)
    RecyclerView rvTestStu;
    @BindView(R.id.rv_device_list)
    RecyclerView rv_device_list;
    private Intent serverIntent;
    private int roundNo;
    private int testNo;       //测试次数
    private int currentStudentIndex;  //当前正在测试的学生下标

    @Override
    public void setRoundNo(Student student, int roundNo) {

    }
    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        builder.setTitle("排球垫球-分组模式");
        return builder.addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        });

    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_volleyball_wrieless;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        if (SettingHelper.getSystemSetting().isRtUpload()) {

            serverIntent = new Intent(this, UploadService.class);
            startService(serverIntent);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getTestStudent();
            }
        }, 3000);
    }

    private void getTestStudent() {
        if (addStudent)
            addStudent = false;
        //身份验证模式
        if (SettingHelper.getSystemSetting().isIdentityMark()) {
            stuAdapter.notifyDataSetChanged();
            return;
        }

        for (int i = 0; i < studentList.size(); i++) {
            //查询学生成绩
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                    (studentList.get(i).getStudentCode(), group.getId() + "");

            if (roundResultList.size() > 0) {
                pairList.get(i).setResultState(-99);
            }
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

        if (setTestPattern() == 0) {//连续模式
            for (int i = 0; i < pairList.size(); i++) {
                //  查询学生成绩 当有成绩则添加数据跳过测试
                pairList.get(i).getTimeResult();

                allotStudent(i);

            }
        }
    }

    @Override
    public void onCheckIn(Student student) {
        Logger.i("onCheckIn====>" + student.toString());
        if (student == null) {
            toastSpeak("该考生不存在");
            return;
        }
//        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
//        Log.e("TAG====",student.getStudentCode()+"------"+studentItem);
//        if (studentItem == null) {
//            toastSpeak("无此项目");
//            return;
//        }

        //是否开启身份验证
        if (SettingHelper.getSystemSetting().isIdentityMark() && studentList.size() > 0) {
            //考生分组测试的成绩
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
            //学生在分组中是否有进行检入
            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, student.getStudentCode());
            if ((roundResultList.size() == 0 || roundResultList.size() < setTestCount()) && groupItem != null) {
                roundNo = roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;

                gotoTest(student);
                groupItem.setIdentityMark(1);
                DBManager.getInstance().updateStudentGroupItem(groupItem);
                //TODO 保存当前测试的学生下标
                for(int i = 0 ; i < studentList.size() ; i++){
                    Student student1 = studentList.get(i);
                    if (student1.equals(student)){
                        currentStudentIndex = i;
                        break;
                    }
                }
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
                break;
            }

        }
        deviceListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initData() {

        setting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);

        rvTestStu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        group = (Group) TestConfigs.baseGroupMap.get("group");
        //给 界面左侧recyclerView 添加学生
        studentList = new ArrayList<>();
        pairList = new ArrayList<>();
        pairList.addAll((List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu"));
        for (BaseStuPair pair : pairList) {
            studentList.add(pair.getStudent());
        }
        stuAdapter = new StuAdapter(studentList);
        rvTestStu.setAdapter(stuAdapter);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        rv_device_list.setLayoutManager(layoutManager);
        ((SimpleItemAnimator) rv_device_list.getItemAnimator()).setSupportsChangeAnimations(false);

        setDeviceCount(4);
        deviceListAdapter = new DeviceListAdapter(deviceDetails);
        deviceListAdapter.setTestCount(setting.getTestNo());
        rv_device_list.setAdapter(deviceListAdapter);

        deviceListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                switch (view.getId()){
                    case R.id.txt_start:
                        sendStart(deviceDetails.get(i),i);
                        deviceDetails.get(i).setFinsh(false);
                        break;
                    case R.id.txt_end:
                        sendEnd(deviceDetails.get(i),i);
                        stuSkip(i);
                        deviceDetails.get(i).getStuDevicePair().setStudent(null);
                        refreshDevice(i);
                        int hostId = SettingHelper.getSystemSetting().getHostId();
                        int deviceId = (byte)  deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
                        VolleyBallRadioManager.getInstance().deviceFree(hostId,deviceId);
                        deviceDetails.get(i).setFinsh(true);
                        break;
                    case R.id.txt_time:
                        sendTime(deviceDetails.get(i),i);

                        break;
                    case R.id.txt_gave_up:
                        sendGaveUp(deviceDetails.get(i),i);
                        deviceDetails.get(i).setFinsh(false);
                        break;
                    case R.id.txt_confirm:
                        sendConfirm(deviceDetails.get(i),i);
                        deviceDetails.get(i).setFinsh(true);
                        break;
                    case R.id.txt_penalty:
                        sendPenalty(deviceDetails.get(i),i);
                        deviceDetails.get(i).setFinsh(true);
                        break;
                    case R.id.txt_js:
                        stopCount(deviceDetails.get(i),i);
                        deviceDetails.get(i).setFinsh(false);
                        break;
                    case R.id.txt_fq:
                        fqCount(deviceDetails.get(i),i);
                        deviceDetails.get(i).setFinsh(true);
                        break;
                }
            }
        });
    }

    protected abstract void fqCount(DeviceDetail deviceDetail, int i);

    protected abstract void stopCount(DeviceDetail deviceDetail, int i);

    protected abstract void sendPenalty(DeviceDetail deviceDetail, int i);

    protected abstract void sendConfirm(DeviceDetail deviceDetail, int i);

    protected abstract void sendGaveUp(DeviceDetail deviceDetail, int i);

    protected abstract void sendTime(DeviceDetail deviceDetail, int i);

    protected abstract void sendEnd(DeviceDetail deviceDetail, int pos);

    protected abstract void sendStart(DeviceDetail deviceDetail, int pos);

    private void init() {
        mLEDManager = new LEDManager();
//        mLEDManager.link(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        PrinterManager.getInstance().init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initLEDShow();
    }

    public void initLEDShow() {
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        int pairNum = setting.getPairNum();
        if (pairNum == 0){
            pairNum = 4;
        }
        Log.e("TAG","pairNum="+pairNum);
        if (ledMode == 0) {
            for (int i = 0; i < pairNum; i++) {
                StringBuilder data = new StringBuilder();
                data.append(i + 1).append("号机");//1号机         空闲
                for (int j = 0; j < 7; j++) {
                    data.append(" ");
                }
                data.append("空闲");
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data.toString(), 0, i, false, true);
            }
            for (int i = pairNum; i < 4; i++) {
                StringBuilder data = new StringBuilder();
                for (int j = 0; j < 16; j++) {
                    data.append(" ");
                }
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data.toString(), 0, i, false, true);
            }
        }
    }


    public void setDeviceCount(int deviceCount) {
        deviceDetails.clear();
        VolleyBallSetting setting = SharedPrefsUtil.loadFormSource(this,VolleyBallSetting.class);
        for (int i = 0; i < deviceCount; i++) {
            DeviceDetail detail = new DeviceDetail();
            detail.getStuDevicePair().getBaseDevice().setDeviceId(i + 1);
            detail.getStuDevicePair().setTimeResult(new String[setting.getTestNo()]);
            detail.setTestTime(setting.getTestTime());
            detail.setDeviceOpen(true);
            detail.setFinsh(true);
            detail.getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_ERROR);
            deviceDetails.add(detail);
        }
    }
    /**
     * 分配考生
     */
    private void allotStudent(int stuPos) {
        int index = -1;
        for (DeviceDetail detail : deviceDetails) {
            index++;
            if (detail.getStuDevicePair().isCanTest() && detail.isDeviceOpen() &&
                    detail.getStuDevicePair().getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                detail.getStuDevicePair().setCanTest(false);
                String[] timeResult = pairList.get(stuPos).getTimeResult();
                int testTimes = getRound(timeResult);
                detail.setRound(testTimes);
                detail.getStuDevicePair().setBaseHeight(0);
                detail.getStuDevicePair().setStudent(studentList.get(stuPos));
                //设置机器上学生成绩
                deviceDetails.get(index).getStuDevicePair().setTimeResult(pairList.get(stuPos).getTimeResult());
                toastSpeak(String.format(getString(R.string.test_speak_hint), studentList.get(stuPos).getStudentName(), testTimes + 1),
                        String.format(getString(R.string.test_speak_hint), studentList.get(stuPos).getStudentName(), testTimes + 1));
                rvTestStu.scrollToPosition(stuPos);
                stuAdapter.setTestPosition(stuPos);
                break;
            }
        }
        deviceListAdapter.notifyDataSetChanged();
    }

    /**
     * 获取测试次数
     */
    private int getRound(String[] timeResult) {
        int j = 0;
        for (int i = 0; i < timeResult.length; i++) {
            if (!TextUtils.isEmpty(timeResult[i]))
                j++;
        }
        return j;
    }
    protected void gotoItemSetting() {

        Intent intent = new Intent(getApplicationContext(), VolleyBallSettingActivity.class);
        intent.putExtra("deviceId",3);
        startActivity(intent);
    }
    @OnClick({R.id.txt_led_setting, R.id.tv_device_pair})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.txt_led_setting:
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;
            case R.id.tv_device_pair:
                startActivity(new Intent(this, VolleyBallPairActivity.class));
                break;
        }
    }
    /**
     * 设置项目测试次数
     */
    public abstract int setTestCount();


    /**
     * 设置项目测试次数 0 连续 1 循环
     */
    public abstract int setTestPattern();


    /**
     * 更新学生成绩
     *
     * @param baseStu
     */
    public synchronized void updateResult(@NonNull BaseStuPair baseStu) {
        int deviceId = baseStu.getBaseDevice().getDeviceId();
        Log.e("TAG","deviceId="+deviceId);
        BaseStuPair pair = null;
        int index = 0;
        for (int i = 0; i < 4; i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            Log.e("TAG","id="+id);
            if (id == deviceId) {
                pair = deviceDetails.get(i).getStuDevicePair();
                index = i;
                break;
            }
        }
        Log.e("TAG","pair="+pair);
        if (null != pair.getBaseDevice()) {
            pair.setResultState(baseStu.getResultState());
            pair.setResult(baseStu.getResult());
            pair.setFullMark(baseStu.isFullMark());
            if (pair.getLEDupdate())
                updateResultLed(baseStu, index);
            refreshDevice(index);

        }
    }
    protected void updateResultLed(BaseStuPair baseStu, int index) {
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        byte[] data = new byte[16];
        if (ledMode == 0) {
            if (baseStu.getStudent() != null) {
                try {
                    String str = baseStu.getStudent().getStudentName();
                    String result = ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
                    if (baseStu.getResult() == 0 && baseStu.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN){
                        result = "准备";
                    }
                    byte[] strData = str.getBytes("GB2312");
                    System.arraycopy(strData, 0, data, 0, strData.length);
                    byte[] resultData = result.getBytes("GB2312");
                    System.arraycopy(resultData, 0, data, data.length - resultData.length, resultData.length);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, index, false, true);
            }
        }
    }
    protected void refreshDevice(int index) {
        if (deviceDetails.get(index).getStuDevicePair().getBaseDevice() != null) {
            deviceListAdapter.notifyItemChanged(index);
        }
    }
    protected void stuSkip(int pos) {
        deviceDetails.get(pos).setTestTime(0);

//        deviceDetails.get(pos).getStuDevicePair().setStudent(null);
        deviceDetails.get(pos).getStuDevicePair().setCanTest(true);
        deviceDetails.get(pos).getStuDevicePair().setTimeResult(new String[setTestCount()]);
        deviceListAdapter.notifyItemChanged(pos);

        allotStudent(pos);
    }
    /**
     * led 显示
     *
     * @param pair
     */
    protected void setShowLed(BaseStuPair pair, int index) {
        Student student = pair.getStudent();
        if (student == null)
            return;
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        if (ledMode == 0) {
            String str = InteractUtils.getStrWithLength(student.getStudentName(), 6);

            str += "准备";
            byte[] data = new byte[0];
            try {
                data = str.getBytes("GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.e("TAGLED",str +","+index);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, index, false, true);
        }
    }
    public void saveResult(final BaseStuPair baseStuPair ,final int index) {
        Logger.i("saveResult==>" + baseStuPair.toString());
        if (baseStuPair.getStudent() == null)
            return;
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(baseStuPair.getStudent().getStudentCode());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setMachineResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        roundResult.setTestTime(baseStuPair.getTestTime());
        roundResult.setRoundNo(roundNo);
        roundResult.setTestNo(1);
        roundResult.setGroupId(group.getId());
        roundResult.setExamType(group.getExamType());
        roundResult.setScheduleNo(group.getScheduleNo());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        RoundResult bestResult = DBManager.getInstance().queryGroupBestScore(baseStuPair.getStudent().getStudentCode(), group.getId());
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStuPair.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // 这个时候就要同时修改这两个成绩了
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
                updateLastResultLed(roundResult, index);
            } else {
                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                    updateLastResultLed(roundResult, index);
                } else {
                    roundResult.setIsLastResult(0);
                    updateLastResultLed(bestResult, index);
                }
            }
        } else {
            // 第一次测试
            roundResult.setIsLastResult(1);
            updateLastResultLed(roundResult, index);
        }
        roundResult.setEndTime(System.currentTimeMillis()+"");
        DBManager.getInstance().insertRoundResult(roundResult);
        Logger.i("saveResult==>insertRoundResult->" + roundResult.toString());

        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(group.getScheduleNo()
                , TestConfigs.getCurrentItemCode(), baseStuPair.getStudent().getStudentCode()
                , "1", group , RoundResultBean.beanCope(roundResultList,group));

        uploadResult(uploadResults);
    }
    private void updateLastResultLed(RoundResult roundResult ,int index) {

    }

    /**
     * 成绩上传
     *
     * @param uploadResults 上传成绩
     */
    private void uploadResult(UploadResults uploadResults) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(UploadResults.BEAN_KEY, uploadResults);
        serverIntent.putExtras(bundle);
        startService(serverIntent);
    }
    protected void printResult(BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        Student student = baseStuPair.getStudent();
        List<RoundResult> roundResults = DBManager.getInstance().queryRountScore(student.getStudentCode());
        //是否已全部次数测试完成，非满分跳过
        if (roundResults.size() < setTestCount() && !baseStuPair.isFullMark()) {
            return;
        }
//        PrinterManager.getInstance().print("\n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机");
        PrinterManager.getInstance().print("考  号:" + student.getStudentCode());
        PrinterManager.getInstance().print("姓  名:" + student.getStudentName());
        for (int i = 0; i < roundResults.size(); i++) {
            int result = roundResults.get(i).getResult();
            if (result != 0) {
                String strResultForDisplay = ResultDisplayUtils.getStrResultForDisplay(result);
                PrinterManager.getInstance().print(String.format("第%1$d次：", i + 1) + strResultForDisplay+getPenalty(roundResults.get(i).getPenaltyNum()));
            } else {
                PrinterManager.getInstance().print(String.format("第%1$d次：", i + 1));
            }
        }
        PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
        PrinterManager.getInstance().print("\n");
    }
    private String getPenalty(int penaltyNum) {
        if (penaltyNum == 0) {
            return "(判罚:" + penaltyNum + ")";
        } else {
            return "(判罚:-" + penaltyNum + ")";
        }

    }
    protected void updateTime(int time, int pos) {
        deviceDetails.get(pos).setTestTime(time);
//        deviceListAdapter.notifyItemChanged(pos);
    }
    protected void addStudent(Student student, int index, boolean b) {
        Log.e("TAG----","student="+student+",index="+index);
        DeviceDetail deviceDetail = deviceDetails.get(index);
        deviceDetail.getStuDevicePair().setStudent(student);
        deviceDetail.getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
        deviceDetail.getStuDevicePair().setCanTest(false);
        deviceDetail.getStuDevicePair().setBaseHeight(0);
//        deviceDetail.setTestTime(setting.getTestTime());
        deviceDetail.getStuDevicePair().setRoundNo(roundNo);
        int count = deviceDetail.getRound();
        toastSpeak(String.format(getString(R.string.test_speak_hint), student.getStudentName(), count + 1)
                , String.format(getString(R.string.test_speak_hint), student.getStudentName(), count + 1));
        if (b)
            setShowLed(deviceDetail.getStuDevicePair(), index);
    }

    /**
     * 进入下一个人的测试
     */
    protected void joinNextPerson(int pos) {
        if (studentList.size() < currentStudentIndex + 2){
            toastSpeak("当前分组已测试完成");
            return;
        }
        Student student = studentList.get(currentStudentIndex+1);
        List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                (student.getStudentCode(), group.getId() + "");
        if (roundResultList.size() >= setTestCount()){     //判断是否已测试
            currentStudentIndex = currentStudentIndex+1;
            joinNextPerson(pos);
            return;
        }

        addStudent(student,pos,true);

        deviceListAdapter.notifyDataSetChanged();
    }
}
