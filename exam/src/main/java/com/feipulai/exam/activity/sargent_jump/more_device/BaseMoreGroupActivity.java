package com.feipulai.exam.activity.sargent_jump.more_device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.AgainTestDialog;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.base.ResitDialog;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.person.PenalizeDialog;
import com.feipulai.exam.activity.sargent_jump.adapter.DeviceListAdapter;
import com.feipulai.exam.activity.sargent_jump.adapter.StuAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
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
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

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
    public List<DeviceDetail> deviceDetails = new ArrayList<>();
    private int deviceCount;
    private boolean isPenalize;
    private PenalizeDialog penalizeDialog;
    /**
     * 当前测试次数位
     */
    private int roundNo = 1;
    private List<BaseStuPair> pairList;
    private StuHandler stuHandler = new StuHandler();
    private boolean addStudent = true;
    private boolean isNextClickStart = true;

    @BindView(R.id.oneView)
    BaseGroupMoreOneView oneView;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_more;
    }

    @Override
    public void setRoundNo(Student student, int roundNo) {
        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        if (systemSetting.isResit())
            this.roundNo = roundNo;
    }

    @Override
    protected void initData() {
        super.initData();
        PrinterManager.getInstance().init();
        group = (Group) TestConfigs.baseGroupMap.get("group");
        mLEDManager = new LEDManager();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

        rvTestStu.setLayoutManager(new LinearLayoutManager(this));

        //给 界面左侧recyclerView 添加学生
        studentList = new ArrayList<>();
        pairList = new ArrayList<>();
        pairList.addAll((List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu"));
        pairList.get(0).setResit(true);
        for (BaseStuPair pair : pairList) {
            studentList.add(pair.getStudent());
        }

        if (setTestDeviceCount() == 1) {
            oneView.setVisibility(View.VISIBLE);

        } else {
            oneView.setVisibility(View.GONE);
        }
        oneView.setData(pairList);
        stuAdapter = new StuAdapter(studentList);
        rvTestStu.setAdapter(stuAdapter);

        StringBuffer sbName = new StringBuffer();
        sbName.append(group.getGroupType() == Group.MALE ? "男子" :
                (group.getGroupType() == Group.FEMALE ? "女子" : "男女混合"));
        sbName.append(group.getSortName() + String.format("第%1$d组", group.getGroupNo()));
        txtGroupName.setText(sbName);

//        initLed();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getTestStudent();
            }
        }, 1500);
        setDeviceCount(setTestDeviceCount());
        penalizeDialog = new PenalizeDialog(this, setTestCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isUse()) {
            if (deviceDetails.size() != setTestDeviceCount()) {
                setDeviceCount(setTestDeviceCount());
            }
        }

    }


    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        switch (baseEvent.getTagInt()) {
            case EventConfigs.INSTALL_RESULT:
                RoundResult iRoundResult = (RoundResult) baseEvent.getData();
                for (int i = 0; i < deviceListAdapter.getData().size(); i++) {
                    DeviceDetail deviceDetail = deviceListAdapter.getData().get(i);

                    if (TextUtils.equals(deviceDetail.getStuDevicePair().getStudent().getStudentCode(), iRoundResult.getStudentCode())) {
                        String[] timeResult = deviceDetail.getStuDevicePair().getTimeResult();
                        final BaseStuPair pair = deviceDetail.getStuDevicePair();
                        timeResult[iRoundResult.getRoundNo() - 1] = ((iRoundResult.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" :
                                ResultDisplayUtils.getStrResultForDisplay(iRoundResult.getResult()));
//                        deviceDetail.getStuDevicePair().setTimeResult(timeResult);
                        deviceListAdapter.notifyDataSetChanged();
                        if (iRoundResult.getRoundNo() < setTestCount()) {
                            deviceDetail.setRound(iRoundResult.getRoundNo());
                        }
                        deviceListAdapter.notifyDataSetChanged();
                        pair.setResult(iRoundResult.getResult());
                        pair.setResultState(iRoundResult.getResultState());
                        updateResultLed(pair, i);
                        matchStudent(pair, i);
                    }
                }
                //更新一对一数据
                for (BaseStuPair stuPair : pairList) {
                    if (TextUtils.equals(stuPair.getStudent().getStudentCode(), iRoundResult.getStudentCode())) {
                        String[] timeResult = stuPair.getTimeResult();

                        timeResult[iRoundResult.getRoundNo() - 1] = RoundResult.resultStateStr(iRoundResult.getResultState(), iRoundResult.getResult());


                        stuPair.setTimeResult(timeResult);
                        oneView.notifyDataSetChanged();
                        break;
                    }
                }
                break;
            case EventConfigs.UPDATE_RESULT:
                RoundResult roundResult = (RoundResult) baseEvent.getData();
                for (int i = 0; i < deviceListAdapter.getData().size(); i++) {
                    DeviceDetail deviceDetail = deviceListAdapter.getData().get(i);
                    BaseStuPair pair = deviceDetail.getStuDevicePair();
                    if (TextUtils.equals(deviceDetail.getStuDevicePair().getStudent().getStudentCode(), roundResult.getStudentCode())) {
                        String[] timeResult = deviceDetail.getStuDevicePair().getTimeResult();

                        timeResult[roundResult.getRoundNo() - 1] = ((roundResult.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" :
                                ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()));
//                        deviceDetail.getStuDevicePair().setTimeResult(timeResult);
                        if (roundResult.getRoundNo() == deviceDetail.getRound()) {
                            pair.setResult(roundResult.getResult());
                            pair.setResultState(roundResult.getResultState());
                            updateResultLed(pair, 0);
                        }

                    }

                }
                //更新一对一数据
                for (BaseStuPair stuPair : pairList) {
                    if (TextUtils.equals(stuPair.getStudent().getStudentCode(), roundResult.getStudentCode())) {
                        String[] timeResult = stuPair.getTimeResult();

                        timeResult[roundResult.getRoundNo() - 1] = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());

                        stuPair.setTimeResult(timeResult);
                        oneView.notifyDataSetChanged();

                        if (roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                            stuPair.setFullMark(false);
                            if (stuPair.getStudent().getSex() == 0 && TestConfigs.getFullSkip() != null && roundResult.getResult() >= TestConfigs.getFullSkip()[0]) {//男子满分跳过
                                stuPair.setFullMark(true);
                            }
                            if (stuPair.getStudent().getSex() == 1 && TestConfigs.getFullSkip() != null && roundResult.getResult() >= TestConfigs.getFullSkip()[1]) {//女子满分跳过
                                stuPair.setFullMark(true);
                            }
                        } else {
                            stuPair.setFullMark(false);
                        }

                        break;
                    }
                }
                deviceListAdapter.notifyDataSetChanged();

                break;

        }
    }


    /**
     * 是否存在使用中设备
     *
     * @return
     */
    public boolean isUse() {
        boolean isOnUse = false;
        for (DeviceDetail deviceDetail : deviceDetails) {
            if (deviceDetail.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_ONUSE) {
                return true;
            }
        }
        return isOnUse;


    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title);
    }

    @OnClick(R.id.txt_led_setting)
    public void onViewClicked() {

        if (isUse()) {
            toastSpeak("测试中,不允许修改设置");
        } else {
            IntentUtil.gotoActivity(this, LEDSettingActivity.class);
        }

    }

    public void setConfirmVisible(int index, boolean visible) {
        deviceDetails.get(index).setConfirmVisible(visible);
        deviceListAdapter.notifyItemChanged(index);
    }

    /**
     * 初始化LED
     */
    private void initLed() {
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        if (ledMode == 0) {
            for (int i = 0; i < deviceCount; i++) {
                StringBuilder data = new StringBuilder();
                data.append(i + 1).append("号机");//1号机         空闲
                for (int j = 0; j < 7; j++) {
                    data.append(" ");
                }
                data.append("空闲");
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data.toString(), 0, i, false, true);
            }
        }
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
        deviceDetails.clear();
        for (int i = 0; i < deviceCount; i++) {
            DeviceDetail detail = new DeviceDetail();
            detail.getStuDevicePair().getBaseDevice().setDeviceId(i + 1);
            detail.getStuDevicePair().setTimeResult(new String[setTestCount()]);
            detail.setDeviceOpen(true);
            if (deviceCount == 1) {
                detail.setItemType(DeviceDetail.ITEM_ONE);
                detail.getStuDevicePair().setResult(-999);
            }
            deviceDetails.add(detail);
        }
        initView();
        if (deviceDetails.size() == 1) {
            stuAdapter.setShowSelete(true);
        } else {
            stuAdapter.setShowSelete(false);
        }
    }

    /**
     * LED屏显示
     *
     * @param stuPair
     */
    private void setStuShowLed(BaseStuPair stuPair) {
        if (stuPair == null)
            return;
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), stuPair.getTrackNo() + "   " + stuPair.getStudent().getLEDStuName(), 0, 0, true, false);
        for (int i = 0; i < stuPair.getTimeResult().length; i++) {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(),
                    String.format("%-4s", String.format("第%1$d次：", i + 1)) + (TextUtils.isEmpty(stuPair.getTimeResult()[i]) ? "" : stuPair.getTimeResult()[i]),
                    0, i + 1, false, true);

//            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1,
//                    String.format("%-4s", String.format("第%1$d次：", i + 1)) + (TextUtils.isEmpty(stuPair.getTimeResult()[i]) ? "" : stuPair.getTimeResult()[i]),
//                    0, i + 1, false, true);
        }

    }

    /***
     * 刷新
     * @param index
     */
    public void refreshDevice(int index) {
        if (deviceDetails.get(index).getStuDevicePair().getBaseDevice() != null) {
            if (setTestDeviceCount() == 1) {
                pairList.get(0).getBaseDevice().setState(deviceDetails.get(index).getStuDevicePair().getBaseDevice().getState());
                oneView.notifyDataSetChanged();
            }
            deviceListAdapter.notifyItemChanged(index);
        }
    }

    /**
     * 更新测试次数
     */
    public void updateAdapterTestCount() {

        deviceListAdapter.setTestCount(setTestCount());
        for (DeviceDetail deviceDetail : deviceDetails) {
            deviceDetail.getStuDevicePair().setTimeResult(new String[setTestCount()]);
        }
        deviceListAdapter.notifyDataSetChanged();
        if (deviceDetails.size() == 1) {
            stuAdapter.setShowSelete(true);
        } else {
            stuAdapter.setShowSelete(false);
        }
    }

    private void initView() {
        deviceListAdapter = new DeviceListAdapter(deviceDetails, true);
        deviceListAdapter.setTestCount(setTestCount());

        GridLayoutManager layoutManager = new GridLayoutManager(this, setTestDeviceCount());
        rvDeviceList.setLayoutManager(layoutManager);
        rvDeviceList.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int pos) {
                BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
                switch (view.getId()) {
                    case R.id.txt_skip://跳过
                        Student student = pair.getStudent();
                        if (student != null) {
                            LogUtils.operation("点击了跳过:考生考号=" + student.getStudentCode());
                            stuSkipDialog(student, pos);
                        }
                        break;
                    case R.id.txt_start://开始
                        LogUtils.operation("点击了开始...");
                        if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                            if (deviceDetails.get(pos).getStuDevicePair().getStudent() == null) {
                                toastSpeak("当前无设备可用");
                                return;
                            }
                            if (isAllTest()) {
                                toastSpeak("当前分组已测试完成，请选下一组");
                            } else {
                                deviceDetails.get(pos).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
                                toStart(pos);
                                updateLastResultLed("", pos);
                                deviceListAdapter.setPenalize(false);
                            }
                        } else {
                            toastSpeak("当前设备异常");
                        }

                        break;
                    case R.id.txt_confirm:
                        if (pair.getStudent() != null) {
                            LogUtils.operation("点击了确认成绩:" + pair.getStudent().toString());
//                            confirmResult(pos);
                            updateTestResult(pair);
                            doResult(pair, pos);
                            deviceDetails.get(pos).setConfirmVisible(false);
                            deviceListAdapter.notifyItemChanged(pos);
                            if (isPenalize) {
                                setConfirmVisible(pos, false);
                            }
                        }
                        break;
                    case R.id.txt_punish:
                        if (pair.getStudent() != null) {
                            penalizeDialog.setGroupId(group.getId());
                            penalizeDialog.setData(1, pair.getStudent(),
                                    pair.getTimeResult(), lastStu, lastResult);
                            penalizeDialog.showDialog(0);
                        }
                        break;
                    case R.id.txt_get_data:
                        if (pair.getStudent() != null) {
                            showGetData(pos);
                        }
                        break;
                }
            }
        });

        oneView.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BaseStuPair pair = pairList.get(oneView.getSaveLayoutSeletePosition());
                Student student = pair.getStudent();
                switch (view.getId()) {

                    case R.id.txt_stu_skip://跳过

                        if (student != null) {
                            LogUtils.operation("点击了跳过:考生考号=" + student.getStudentCode());
                            stuSkipDialog(student, 0);
                        }
                        break;
                    case R.id.txt_start_test://开始
                        LogUtils.operation("点击了开始...");
                        if (deviceDetails.get(0).getStuDevicePair().getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                            if (deviceDetails.get(0).getStuDevicePair().getStudent() == null) {
                                toastSpeak("当前无设备可用");
                                return;
                            }
                            if (isAllTest()) {
                                toastSpeak("当前分组已测试完成，请选下一组");
                            } else {
                                deviceDetails.get(0).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
                                toStart(0);
                                updateLastResultLed("", 0);
                                deviceListAdapter.setPenalize(false);
                            }
                        } else {
                            toastSpeak("当前设备异常");
                        }

                        break;
                    case R.id.txt_confirm:
                        if (pair.getStudent() != null) {
                            LogUtils.operation("点击了确认成绩:" + pair.getStudent().toString());
//                            confirmResult(pos);
                            updateTestResult(pair);
                            doResult(pair, 0);
                            deviceDetails.get(0).setConfirmVisible(false);
                            deviceListAdapter.notifyItemChanged(0);
                            if (isPenalize) {
                                setConfirmVisible(0, false);
                            }
                        } else {
                            toastSpeak("无设备添加考生测试");
                        }
                        break;

                    case R.id.tv_get_data:
                        if (pair.getStudent() != null) {
                            showGetData(0);
                        }
                        break;
                    case R.id.tv_foul:
                        if (pair.getStudent() != null) {
                            penalizeDialog.setGroupId(group.getId());
                            penalizeDialog.setData(1, pair.getStudent(),
                                    pair.getTimeResult(), lastStu, lastResult);
                            penalizeDialog.showDialog(0);
                        } else {
                            toastSpeak("无设备添加考生测试");
                        }
                        break;
                    case R.id.tv_inBack:
                        if (pair.getStudent() != null) {
                            penalizeDialog.setGroupId(group.getId());
                            penalizeDialog.setData(1, pair.getStudent(),
                                    pair.getTimeResult(), lastStu, lastResult);
                            penalizeDialog.showDialog(1);
                        } else {
                            toastSpeak("无设备添加考生测试");
                        }
                        break;
                    case R.id.tv_abandon:
                        if (pair.getStudent() != null) {
                            penalizeDialog.setGroupId(group.getId());
                            penalizeDialog.setData(1, pair.getStudent(),
                                    pair.getTimeResult(), lastStu, lastResult);
                            penalizeDialog.showDialog(2);
                        } else {
                            toastSpeak("无设备添加考生测试");
                        }
                        break;
                    case R.id.tv_normal:
                        if (pair.getStudent() != null) {
                            penalizeDialog.setGroupId(group.getId());
                            penalizeDialog.setData(2, pair.getStudent(),
                                    pair.getTimeResult(), lastStu, lastResult);
                            penalizeDialog.showDialog(3);
                        } else {
                            toastSpeak("无设备添加考生测试");
                        }
                        break;
                    case R.id.tv_resurvey:
                        String resultString = pairList.get(oneView.stuAdapter.getSaveLayoutSeletePosition())
                                .getSelectResultList().get(oneView.stuAdapter.getSaveSeletePosition()).getResult();
                        if (TextUtils.isEmpty(resultString)) {
                            toastSpeak("当前轮次无成绩，请进行测试");
                            return;
                        }
                        AgainTestDialog dialog = new AgainTestDialog();

                        RoundResult roundResult = DBManager.getInstance().queryGroupRoundNoResult(student.getStudentCode(), group.getId().toString(), oneView.stuAdapter.getSaveSeletePosition() + 1);
                        if (roundResult == null) {
                            toastSpeak("当前轮次无成绩，请进行测试");
                            return;
                        }
                        GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, student.getStudentCode());
                        List<RoundResult> results = new ArrayList<>();
                        results.add(roundResult);
                        dialog.setArguments(student, results, groupItem);
                        dialog.setOnIndividualCheckInListener(new ResitDialog.onClickQuitListener() {
                            @Override
                            public void onCancel() {

                            }

                            @Override
                            public void onCommitPattern(Student student, StudentItem studentItem, List<RoundResult> results, int updateRoundNo) {

                            }

                            @Override
                            public void onCommitGroup(Student student, GroupItem groupItem, List<RoundResult> results, int updateRoundNo) {
                                LogUtils.operation(student.getStudentCode() + "重测第" + updateRoundNo + "轮成绩");
                                BaseStuPair pair = pairList.get(oneView.stuAdapter.getSaveLayoutSeletePosition());

                                pair.getTimeResult()[updateRoundNo - 1] = "";
                                deviceDetails.get(0).getStuDevicePair().setStudent(pair.getStudent());
                                deviceDetails.get(0).getStuDevicePair().setTimeResult(pair.getTimeResult());
                                deviceDetails.get(0).setRound(updateRoundNo);
                                pair.setTimeResult(pair.getTimeResult());
                                //设置测试轮次
                                pair.setRoundNo(updateRoundNo);
                                roundNo = updateRoundNo;
                                stuAdapter.setTestPosition(oneView.stuAdapter.getSaveLayoutSeletePosition());
                                oneView.setTestPosition(oneView.stuAdapter.getSaveLayoutSeletePosition());
                                oneView.indexStuTestResult(oneView.stuAdapter.getSaveLayoutSeletePosition(), roundNo - 1);
                                stuAdapter.notifyDataSetChanged();
                                toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), roundNo)
                                        , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), roundNo));

//                                Message msg = new Message();
//                                msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
//                                ledHandler.removeCallbacksAndMessages(null);
//                                ledHandler.sendMessageDelayed(msg, 1000);
                            }
                        });
                        dialog.show(getSupportFragmentManager(), "AgainTestDialog");

                        break;

                }
            }
        });
    }

    private String getItemCode() {
        return TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
    }

    public void getData(int pos) {

    }

    /**
     * 展示手动获取成绩
     */
    private void showGetData(final int index) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText("是否手动获取成绩");
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                LogUtils.operation("点击了获取成绩:");
                sweetAlertDialog.dismissWithAnimation();
                getData(index);
            }
        }).setCancelText("否").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();

    }

    public void setFaultEnable(boolean isPenalize) {
        this.isPenalize = isPenalize;
        deviceListAdapter.setPenalize(isPenalize);

        if (deviceDetails.size() == 1) {
            deviceDetails.get(0).setPunish(true);
        }
    }

    public void setNextClickStart(boolean nextClickStart) {
        isNextClickStart = nextClickStart;
        deviceListAdapter.setNextClickStart(nextClickStart);
    }

    public void setTxtEnable(int deviceId, boolean enable) {
        deviceListAdapter.setTxtStartEnable(deviceId, enable);
    }

    public void setShowGetData(int deviceId, boolean enable) {
        deviceListAdapter.setShowGetData(deviceId, enable);
        if (setTestDeviceCount() == 1) {
            oneView.setShowGetData(enable);
        }
    }

    protected void stuSkipDialog(final Student student, final int index) {
        new AlertDialog.Builder(this).setMessage("是否跳过" + student.getStudentName() + "考生测试")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.i("stuSkip:" + student.toString());
                        //测试结束学生清除 ，设备设置空闲状态
                        toSkip(index);
//                        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
                    }
                }).setNegativeButton("取消", null).show();
    }


    protected void toSkip(int index) {
        deviceDetails.get(index).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        deviceDetails.get(index).getStuDevicePair().setCanTest(true);

        if (deviceDetails.get(index).getStuDevicePair().getStudent() != null) {
            LogUtils.operation("跳过考生：" + deviceDetails.get(index).getStuDevicePair().getStudent().getStudentName());
            deviceDetails.get(index).getStuDevicePair().setStudent(null);
            deviceDetails.get(index).getStuDevicePair().setResult(-999);
            deviceDetails.get(index).getStuDevicePair().setTimeResult(new String[setTestCount()]);
            deviceListAdapter.notifyItemChanged(index);
        }
        //跳过成绩保存
        if (studentList == null || studentList.size() == 0 || stuAdapter.getTestPosition() == -1) {
            return;
        }
        if (deviceDetails.get(index).getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_ERROR)
            return;
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
            if (setTestPattern() == 0) {
                if (isAllTest()) {
                    toastSpeak("当前小组所有人员都测试完");
                } else {
                    getTestStudent();
                    return;
                }


            } else {
                for (int i = 0; i < studentList.size(); i++) {
                    List<RoundResult> roundResults = DBManager.getInstance().queryGroupRound(studentList.get(i).getStudentCode(), group.getId() + "");
                    if (roundResults.size() >= setTestCount())
                        continue;
                    if (hasStudentInDevice(studentList.get(i).getStudentCode()))
                        continue;
                    if (deviceDetails.get(index).isDeviceOpen() && deviceDetails.get(index).getStuDevicePair().getStudent() == null) {
                        deviceDetails.get(index).getStuDevicePair().setStudent(studentList.get(i));
                        deviceDetails.get(index).getStuDevicePair().setTimeResult(pairList.get(i).getTimeResult());
                        deviceListAdapter.notifyItemChanged(index);
                        stuAdapter.setTestPosition(i);
                        oneView.setTestPosition(i);
                        oneView.indexStuTestResult(i, roundNo);
                        stuAdapter.notifyDataSetChanged();
                        roundNo++;

                        if (!isNextClickStart) {
                            if (deviceCount == 1) {
                                setStuShowLed(stuAdapter.getTestPosition() != -1 ? pairList.get(stuAdapter.getTestPosition()) : null);
                            }
                            deviceDetails.get(index).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
                            toStart(index);
                        }
                    }
                    return;
                }
            }

        }

        for (int i = stuAdapter.getTestPosition() + 1; i < studentList.size(); i++) {
            List<RoundResult> roundResults = DBManager.getInstance().queryGroupRound(studentList.get(i).getStudentCode(), group.getId() + "");
            if (roundResults.size() >= setTestCount())
                continue;
            if (hasStudentInDevice(studentList.get(i).getStudentCode()))
                continue;
            if (deviceDetails.get(index).isDeviceOpen()) {
                deviceDetails.get(index).getStuDevicePair().setStudent(studentList.get(i));
                deviceDetails.get(index).getStuDevicePair().setTimeResult(pairList.get(i).getTimeResult());
                deviceListAdapter.notifyItemChanged(index);
                stuAdapter.setTestPosition(i);
                oneView.setTestPosition(i);
                int testTimes = getRoundNo(pairList.get(i).getTimeResult());
                roundNo = pairList.get(i).getRoundNo() != 0 ? pairList.get(i).getRoundNo() : testTimes;
                deviceDetails.get(index).setRound(roundNo);
                oneView.indexStuTestResult(i, roundNo - 1);
                stuAdapter.notifyDataSetChanged();
                if (!isNextClickStart) {
                    if (deviceCount == 1) {
                        setStuShowLed(stuAdapter.getTestPosition() != -1 ? pairList.get(stuAdapter.getTestPosition()) : null);
                    }
                    deviceDetails.get(index).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
                    toStart(index);
                }
            }

            return;
        }

    }

    //确认成绩判罚
    protected void confirmResult(int pos) {
        showPenalize(pos);
    }

    /**
     * 展示判罚
     */
    private void showPenalize(final int index) {
        final BaseStuPair pair = deviceDetails.get(index).getStuDevicePair();
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText(getString(R.string.confirm_result));
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                updateTestResult(pair);
                doResult(pair, index);
            }
        }).setCancelText(getString(R.string.foul)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                pair.setResultState(RoundResult.RESULT_STATE_FOUL);
                updateTestResult(pair);
                doResult(pair, index);
            }
        }).show();
        deviceDetails.get(index).setConfirmVisible(false);
        deviceListAdapter.notifyItemChanged(index);
    }

    /**
     * 展示判罚
     */
    private void penalize(final int index) {
        final BaseStuPair pair = deviceDetails.get(index).getStuDevicePair();
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText("确认判罚?");
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                pair.setResultState(RoundResult.RESULT_STATE_FOUL);
                updateTestResult(pair);
                doResult(pair, index);
                deviceDetails.get(index).setConfirmVisible(false);
                deviceListAdapter.notifyItemChanged(index);
            }
        }).setCancelText("取消").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();

            }
        }).show();

    }

    private void broadResult(BaseStuPair baseStuPair) {
        if (deviceCount > 1)
            return;
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak(baseStuPair.getStudent().getSpeakStuName() + "犯规");
            } else {

                TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), baseStuPair.getStudent().getSpeakStuName(), ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
            }


        }
    }

    private boolean isAllTest() {
        for (BaseStuPair stuPair : pairList) {
            if (!stuPair.isFullMark()) {
                for (String s : stuPair.getTimeResult()) {
                    if (TextUtils.isEmpty(s)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void allTestComplete() {
        //全部次数测试完，
        if (stuAdapter.getTestPosition() != -1 &&
                SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN &&
                (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4 || SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_CUSTOM_APP) &&
                SettingHelper.getSystemSetting().isAutoPrint()) {
            InteractUtils.printA4Result(this, group);
        }
        toastSpeak("分组考生全部测试完成，请选择下一组");
        stuAdapter.setTestPosition(-1);
        oneView.setTestPosition(-1);
        stuAdapter.notifyDataSetChanged();
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);

    }

    /**
     * 获取考生
     */
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
                RoundResult roundResult = roundResultList.get(j);
                if (roundResultList.get(j).getRoundNo() - 1 < setTestCount()) {
                    result[roundResult.getRoundNo() - 1] = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());
//                    switch (roundResultList.get(j).getResultState()) {
//                        case RoundResult.RESULT_STATE_FOUL:
//                            result[roundResultList.get(j).getRoundNo() - 1] = "X";
//                            break;
//                        case RoundResult.RESULT_STATE_BACK:
//                            result[roundResultList.get(j).getRoundNo() - 1] = "中退";
//                            break;
//                        case RoundResult.RESULT_STATE_WAIVE:
//                            result[roundResultList.get(j).getRoundNo() - 1] = "放弃";
//                            break;
//                        default:
//                            result[roundResultList.get(j).getRoundNo() - 1] = ResultDisplayUtils.getStrResultForDisplay(roundResultList.get(j).getResult());
//                            break;
//                    }
                }
            }

            pairList.get(i).setTimeResult(result);
        }

        if (setTestPattern() == 0) {//连续模式
            for (int i = 0; i < pairList.size(); i++) {
                //  查询学生成绩 当有成绩则添加数据跳过测试

                allotStudent(i);

            }
        } else {//循环测试
            for (int i = roundNo; i <= setTestCount(); i++) {
                for (int j = 0; j < pairList.size(); j++) {
                    if (TextUtils.isEmpty(pairList.get(j).getTimeResult()[i - 1])) {
                        int index = -1;
                        for (DeviceDetail detail : deviceDetails) {
                            index++;
                            if (detail.getStuDevicePair().isCanTest() && detail.isDeviceOpen() &&
                                    detail.getStuDevicePair().getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                                detail.getStuDevicePair().setCanTest(false);
                                detail.getStuDevicePair().setBaseHeight(0);
                                detail.getStuDevicePair().setStudent(studentList.get(j));
                                deviceDetails.get(index).getStuDevicePair().setTimeResult(pairList.get(j).getTimeResult());
                                int testTimes = getRoundNo(pairList.get(j).getTimeResult());
                                roundNo = pairList.get(j).getRoundNo() != 0 ? pairList.get(j).getRoundNo() : testTimes;
                                toastSpeak(String.format(getString(R.string.test_speak_hint), studentList.get(j).getStudentName(), roundNo),
                                        String.format(getString(R.string.test_speak_hint), studentList.get(j).getStudentName(), roundNo));

                                LogUtils.operation((index + 1) + "号机：" + studentList.get(j).getStudentName());
                                LogUtils.operation(String.format(getString(R.string.test_speak_hint), studentList.get(j).getStudentName(), roundNo));


                                deviceDetails.get(index).setRound(roundNo);
                                deviceListAdapter.notifyDataSetChanged();
                                rvTestStu.scrollToPosition(j);
                                stuAdapter.setTestPosition(j);
                                stuAdapter.notifyDataSetChanged();
                                oneView.setTestPosition(j);
                                oneView.indexStuTestResult(j, roundNo - 1);
                                deviceListAdapter.notifyItemChanged(index);
                                setShowLed(pairList.get(j), index);
                                if (!isNextClickStart) {
                                    deviceDetails.get(index).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
                                    toStart(index);
                                    if (deviceCount > 1)
                                        updateLastResultLed("", index);
                                }
                                break;
                            }
                        }
                    }
                }
            }

        }
        if (deviceCount == 1) {
            setStuShowLed(stuAdapter.getTestPosition() != -1 ? pairList.get(stuAdapter.getTestPosition()) : null);
        }
    }

//    /**
//     * 获取测试次数
//     */
//    private int getRound(String[] timeResult) {
//        int j = 0;
//        for (int i = 0; i < timeResult.length; i++) {
//            if (!TextUtils.isEmpty(timeResult[i]))
//                j++;
//        }
//        return j;
//    }

    /**
     * 获取测试次数
     */
    private int getRoundNo(String[] timeResult) {
        int j = 0;
        for (int i = 0; i < timeResult.length; i++) {
            if (TextUtils.isEmpty(timeResult[i])) {
                return ++i;
            }
            j++;
        }
        return ++j;
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
                int testTimes = getRoundNo(timeResult);
                roundNo = pairList.get(stuPos).getRoundNo() != 0 ? pairList.get(stuPos).getRoundNo() : testTimes;
                detail.setRound(roundNo);
                detail.getStuDevicePair().setBaseHeight(0);
                detail.getStuDevicePair().setStudent(studentList.get(stuPos));
                //设置机器上学生成绩
                deviceDetails.get(index).getStuDevicePair().setTimeResult(pairList.get(stuPos).getTimeResult());
                toastSpeak(String.format(getString(R.string.test_speak_hint), studentList.get(stuPos).getStudentName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), studentList.get(stuPos).getStudentName(), roundNo));
                LogUtils.operation((index + 1) + "号机：" + studentList.get(stuPos).getStudentName());
                LogUtils.operation(String.format(getString(R.string.test_speak_hint), studentList.get(stuPos).getStudentName(), roundNo));
                setShowLed(pairList.get(stuPos), index);
                rvTestStu.scrollToPosition(stuPos);
                stuAdapter.setTestPosition(stuPos);
                oneView.setTestPosition(stuPos);
                oneView.indexStuTestResult(stuPos, roundNo - 1);
                stuAdapter.notifyDataSetChanged();
                if (!isNextClickStart) {
                    deviceDetails.get(stuPos).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
                    toStart(stuPos);
                    if (deviceCount > 1)
                        updateLastResultLed("", stuPos);
                }
                break;
            }
        }
        deviceListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onCheckIn(Student student) {
        if (student == null) {
            toastSpeak("该考生不存在");
            return;
        }
        LogUtils.all("检入学生" + student.toString());
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            toastSpeak("无此项目");
            return;
        }
        LogUtils.all("检入学生StudentItem" + studentItem.toString());

        //是否开启身份验证
        if (SettingHelper.getSystemSetting().isIdentityMark() && studentList.size() > 0) {
            //考生分组测试的成绩
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
            LogUtils.all("检入到学生成绩:" + roundResultList.toString());
            //学生在分组中是否有进行检入
            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, student.getStudentCode());
            if ((roundResultList.size() == 0 || roundResultList.size() < setTestCount()) && groupItem != null) {
                roundNo = roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;
                LogUtils.operation("检入到学生轮次: = " + roundNo);
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
            LogUtils.all("检入到学生成绩:" + roundResultList.toString());
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
                if (student != null)
                    LogUtils.operation("添加考生信息:" + student.toString());
                break;
            }

        }
        deviceListAdapter.notifyDataSetChanged();
    }

    public void updateDevice(BaseDeviceState deviceState) {
        int deviceId = deviceState.getDeviceId();
        LogUtils.all("更新摸高设备状态:deviceId=" + deviceId + ",deviceState=" + deviceState.toString());
        BaseStuPair pair = null;
        int deviceIndex = 0;
        for (int i = 0; i < deviceDetails.size(); i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            if (id == deviceId) {
                pair = deviceDetails.get(i).getStuDevicePair();
                deviceIndex = i;
                break;
            }

        }
        if (deviceDetails.size() == 1) {
            pairList.get(0).getBaseDevice().setState(deviceState.getState());
            oneView.notifyDataSetChanged();
        }
        pair.getBaseDevice().setState(deviceState.getState());
        //状态为测试已结束
        if (deviceState.getState() == BaseDeviceState.STATE_END) {
            if (pair.getStudent() != null) {
                Logger.i("考生" + pair.getStudent().toString());
            } else {
                return;
            }
//            pair.setCanTest(true);
//            pair.getBaseDevice().setResultState(BaseDeviceState.STATE_FREE);
//            if (isPenalize && pair.getResultState() != RoundResult.RESULT_STATE_FOUL) {
//
//                if (setTestDeviceCount() == 1) {
//                    showPenalize(deviceIndex);
//                } else {
//                    deviceDetails.get(deviceIndex).setConfirmVisible(true);
//                    deviceListAdapter.notifyItemChanged(deviceIndex);
//                }
//            } else {
//                doResult(pair, deviceIndex);
//            }

//            deviceListAdapter.setPenalize(isPenalize);
//            deviceDetails.get(deviceIndex).setConfirmVisible(true);
//            deviceListAdapter.notifyDataSetChanged();
            doResult(pair, deviceIndex);
        }

        if (deviceDetails.get(deviceIndex).getStuDevicePair().getBaseDevice() != null) {
            deviceListAdapter.notifyItemChanged(deviceIndex);
        }
    }

    /**
     * led 显示
     *
     * @param pair
     */
    private void setShowLed(BaseStuPair pair, int index) {
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
            Log.e("TAGLED", str + "," + index);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, index, false, true);
        } else {
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, student.getLEDStuName() + "   第" + deviceDetails.get(index).getRound() + "次", 0, 0, true, false);
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, "当前：", 0, 1, false, true);

            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), student.getLEDStuName() + "   第" + deviceDetails.get(index).getRound() + "次", 0, 0, true, false);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "当前：", 0, 1, false, true);


            RoundResult bestResult = DBManager.getInstance().queryBestScore(student.getStudentCode(), 1);
            if (bestResult != null) {
                byte[] data = new byte[16];
                String str = "最好：";
                String result = bestResult.getResultState() == RoundResult.RESULT_STATE_FOUL ? "X" : ResultDisplayUtils.getStrResultForDisplay(bestResult.getResult());
                try {
                    byte[] strData = str.getBytes("GB2312");
                    System.arraycopy(strData, 0, data, 0, strData.length);
                    byte[] resultData = result.getBytes("GB2312");
                    System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, data, 0, 2, false, true);
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 2, false, true);
            } else {
                mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, "最好：", 0, 2, false, true);
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "最好：", 0, 2, false, true);

            }
        }

    }

    private void updateResultLed(BaseStuPair baseStu, int index) {
        //todo led模式
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        String result = baseStu.getResultState() != RoundResult.RESULT_STATE_NORMAL ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
        if (deviceDetails.size() > 1) {
            int x = ResultDisplayUtils.getStringLength(result);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), result, 16 - x, index, false, true);
        } else {
            byte[] data = new byte[16];
            String str = "当前：";
            try {
                byte[] strData = str.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                byte[] resultData = result.getBytes("GB2312");
                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 1, false, true);
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1, data, 0, 1, false, true);
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
//        //更新成绩
//        String[] timeResult = deviceDetails.get(index).getStuDevicePair().getTimeResult();
//        for (int i = 0; i < timeResult.length; i++) {
//            if (TextUtils.isEmpty(timeResult[i])) {
//                timeResult[i] = (baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" :
//                        ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
//                break;
//            }
//        }
//        //设置学生成绩
//        Student student = deviceDetails.get(index).getStuDevicePair().getStudent();
//        int stuIndex = studentList.indexOf(student);
//        pairList.get(stuIndex).setFullMark(baseStu.isFullMark());
//        pairList.get(stuIndex).setResultState(baseStu.getResultState());
//        pairList.get(stuIndex).setTimeResult(timeResult);


        updateResultLed(baseStu, index);
    }

    private Student lastStu;
    private String[] lastResult;

    //处理结果
    private void doResult(BaseStuPair pair, int deviceIndex) {
        broadResult(pair);
        lastStu = pair.getStudent();
        lastResult = pair.getTimeResult();
        Logger.i("考生" + pair.getStudent().toString());
        //更新成绩
        String[] timeResult = deviceDetails.get(deviceIndex).getStuDevicePair().getTimeResult();
        for (int i = 0; i < timeResult.length; i++) {
            if (TextUtils.isEmpty(timeResult[i])) {
                timeResult[i] = RoundResult.resultStateStr(pair.getResultState(), pair.getResult());
                break;
            }
        }
        deviceDetails.get(deviceIndex).getStuDevicePair().setTimeResult(timeResult);
        deviceListAdapter.notifyItemChanged(deviceIndex);

        //设置学生成绩
        Student student = deviceDetails.get(deviceIndex).getStuDevicePair().getStudent();
        int stuIndex = studentList.indexOf(student);
        pairList.get(stuIndex).setFullMark(pair.isFullMark());
        pairList.get(stuIndex).setResultState(pair.getResultState());
        pairList.get(stuIndex).setTimeResult(timeResult);
        oneView.notifyDataSetChanged();
        //保存成绩
        if (setTestPattern() == 0) {
            saveResult(pair, deviceDetails.get(deviceIndex).getRound(), deviceIndex);
        } else {
            saveResult(pair, deviceDetails.get(deviceIndex).getRound(), deviceIndex);
        }

        printResult(pair);
        if (!isPenalize) {
            //分配考生到机器中
            matchStudent(pair, deviceIndex);
        }

    }

    /**
     * 分派考生到机器中
     *
     * @param pair
     * @param deviceIndex
     */
    private void matchStudent(final BaseStuPair pair, final int deviceIndex) {
        //非身份验证模式
        if (!SettingHelper.getSystemSetting().isIdentityMark()) {
            //连续测试
            if (setTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
                int testTimes = deviceDetails.get(deviceIndex).getRound();
                roundNo = pair.getRoundNo() != 0 ? pair.getRoundNo() : testTimes + 1;
                deviceDetails.get(deviceIndex).setRound(roundNo);
                if (roundNo - 1 < setTestCount()) {
                    if (pair.isFullMark() && pair.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                        //测试下一个
                        continuousTestNext(deviceIndex);
                    } else {//继续测试同一个
                        //是否为重测，重测直接下一位
                        boolean isAllTest = true;
                        for (String s : pair.getTimeResult()) {
                            if (TextUtils.isEmpty(s)) {
                                isAllTest = false;
                            }
                        }
                        if (isAllTest) {
                            //测试下一个
                            continuousTestNext(deviceIndex);
                            return;
                        }
                        final int ts = roundNo;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                testNextRound(pair, deviceIndex, ts);
                                oneView.indexStuTestResult(oneView.getTestPosition(), ts - 1);
                            }
                        }, 3000);

                    }
                } else {//测试下一个
                    continuousTestNext(deviceIndex);
                }

            } else {
                //循环是否测试到最后一位
                if (stuAdapter.getTestPosition() == studentList.size() - 1) {
                    //是否为最后一次测试，开启新的测试
                    if (setTestCount() > roundNo) {
                        roundNo++;
                        //设置测试学生，当学生有满分跳过则寻找需要测试学生
                        stuAdapter.setTestPosition(0);
                        oneView.setTestPosition(0);
                        loopTestNext(deviceIndex);
                        return;
                    } else {
                        //全部次数测试完，
                        if (deviceIndex == deviceDetails.size() - 1)
                            allTestComplete();
                        return;
                    }
                }
                //设置测试学生，当学生有满分跳过则寻找需要测试学生
//                stuAdapter.setTestPosition(stuAdapter.getTestPosition() + 1);
//                oneView.setTestPosition(oneView.getTestPosition() + 1);
                loopTestNext(deviceIndex);
            }
        } else {
            //身份验证下一轮
            identityMarkTest();
        }
    }

    /**
     * 连续测试测试下一轮
     *
     * @param pair
     * @param deviceIndex
     * @param testTimes
     */
    private void testNextRound(BaseStuPair pair, int deviceIndex, int testTimes) {
        toastSpeak(String.format(getString(R.string.test_speak_hint),
                pair.getStudent().getStudentName(), testTimes),
                String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), testTimes));
        LogUtils.operation((deviceIndex + 1) + "号机：" + pair.getStudent().getStudentName());
        LogUtils.operation(String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), testTimes));
        group.setIsTestComplete(2);
        DBManager.getInstance().updateGroup(group);
        if (!isNextClickStart) {
            deviceDetails.get(deviceIndex).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
            toStart(deviceIndex);
            updateLastResultLed("", deviceIndex);
        }
    }

    /**
     * 循环测试下一个
     */
    private void loopTestNext(final int index) {
        final int stuPos;
        if (roundNo == 0)
            roundNo = 1;
        for (int i = roundNo; i <= setTestCount(); i++) {
            for (int j = stuAdapter.getTestPosition(); j < studentList.size(); j++) {
                if (TextUtils.isEmpty(pairList.get(j).getTimeResult()[i - 1])) {
                    if (pairList.get(j).isFullMark() && pairList.get(j).getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                        continue;
                    }
                    roundNo = getRoundNo(pairList.get(j).getTimeResult());

                    stuAdapter.setTestPosition(j);
                    oneView.setTestPosition(j);
                    if (hasStudentInDevice(studentList.get(j).getStudentCode()))
                        continue;
                    stuPos = j;
                    roundNo = pairList.get(stuPos).getRoundNo() != 0 ? pairList.get(stuPos).getRoundNo() : roundNo;
                    loopConfirmStudent(index, stuPos);

                    deviceDetails.get(index).setRound(roundNo);
                    deviceDetails.get(index).getStuDevicePair().setBaseHeight(0);
                    group.setIsTestComplete(2);
                    DBManager.getInstance().updateGroup(group);
                    return;
                }
            }
            //再次验证当前轮次是否存在未测试考生
            stuAdapter.setTestPosition(0);
//            for (int j = stuAdapter.getTestPosition(); j < studentList.size(); j++) {
//                if (pairList.get(j).isFullMark() && pairList.get(j).getResultState() == RoundResult.RESULT_STATE_NORMAL) {
//                    continue;
//                }
//                roundNo = getRoundNo(pairList.get(j).getTimeResult());
//
//                stuAdapter.setTestPosition(j);
//                oneView.setTestPosition(j);
//                if (hasStudentInDevice(studentList.get(j).getStudentCode()))
//                    continue;
//                stuPos = j;
//                roundNo = pairList.get(stuPos).getRoundNo() != 0 ? pairList.get(stuPos).getRoundNo() : roundNo;
//                loopConfirmStudent(index, stuPos);
//
//                deviceDetails.get(index).setRound(roundNo);
//                deviceDetails.get(index).getStuDevicePair().setBaseHeight(0);
//                group.setIsTestComplete(2);
//                DBManager.getInstance().updateGroup(group);
//                return;
//            }
        }

        if (!isAllTest()) {
            roundNo = 1;
            stuAdapter.setTestPosition(0);
            oneView.setTestPosition(0);
            loopTestNext(index);
            return;
        }
        //全部次数测试完，
        allTestComplete();
    }

    /**
     * 循环确定选中考生
     *
     * @param index
     * @param stuPos
     */
    private void loopConfirmStudent(final int index, final int stuPos) {
        stuHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toastSpeak(String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getSpeakStuName(),
                        roundNo),
                        String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getStudentName(),
                                roundNo));
                LogUtils.operation((index + 1) + "号机：" + studentList.get(stuAdapter.getTestPosition()).getStudentName());
                LogUtils.operation(String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getStudentName(), roundNo));
                deviceDetails.get(index).getStuDevicePair().setTimeResult(pairList.get(stuPos).getTimeResult());
                deviceDetails.get(index).getStuDevicePair().setStudent(studentList.get(stuPos));
                deviceDetails.get(index).getStuDevicePair().setResult(-999);
                deviceListAdapter.notifyItemChanged(index);
                rvTestStu.scrollToPosition(stuAdapter.getTestPosition());
                oneView.indexStuTestResult(stuAdapter.getTestPosition(), roundNo - 1);
                setShowLed(pairList.get(stuPos), index);
                updateLastResultLed("", index);
                if (!isNextClickStart) {
                    deviceDetails.get(index).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
                    toStart(index);
                }
                stuAdapter.notifyDataSetChanged();
            }
        }, 3000);
    }

    /**
     * 连续测试测下一个
     */
    private void continuousTestNext(final int deviceIndex) {
        group.setIsTestComplete(2);
        DBManager.getInstance().updateGroup(group);

        if (stuAdapter.getTestPosition() == studentList.size() - 1) {
            //全部次数测试完，
            if (deviceIndex == deviceDetails.size() - 1)
                allTestComplete();
            return;
        }
        final int stuPos;
        for (int i = (stuAdapter.getTestPosition() + 1); i < studentList.size(); i++) {
            //  查询学生成绩 当有成绩则添加数据跳过测试

            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                    (studentList.get(i).getStudentCode(), group.getId() + "");
            if (roundResultList == null || roundResultList.size() == 0 || roundResultList.size() < setTestCount()) {
                int testTimes = getRoundNo(pairList.get(i).getTimeResult());
                stuAdapter.setTestPosition(i);
                oneView.setTestPosition(i);
                if (hasStudentInDevice(studentList.get(i).getStudentCode()))
                    continue;

                stuPos = i;
                roundNo = pairList.get(stuPos).getRoundNo() != 0 ? pairList.get(stuPos).getRoundNo() : getRoundNo(pairList.get(i).getTimeResult());
                stuHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deviceDetails.get(deviceIndex).getStuDevicePair().setTimeResult(pairList.get(stuPos).getTimeResult());
                        deviceDetails.get(deviceIndex).getStuDevicePair().setStudent(studentList.get(stuPos));
                        deviceDetails.get(deviceIndex).getStuDevicePair().setResult(-999);
                        deviceListAdapter.notifyItemChanged(deviceIndex);
                        rvTestStu.scrollToPosition(stuAdapter.getTestPosition());
                        oneView.indexStuTestResult(oneView.getTestPosition(), roundNo - 1);
                        setShowLed(pairList.get(stuPos), deviceIndex);
                        updateLastResultLed("", deviceIndex);
                        if (!isNextClickStart) {
                            deviceDetails.get(deviceIndex).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
                            toStart(deviceIndex);
                        }
                        stuAdapter.notifyDataSetChanged();
                    }
                }, 3000);

                deviceDetails.get(deviceIndex).getStuDevicePair().setBaseHeight(0);

                deviceDetails.get(deviceIndex).setRound(roundNo);

                toastSpeak(String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getStudentName(), roundNo));

                LogUtils.operation((deviceIndex + 1) + "号机：" + studentList.get(stuAdapter.getTestPosition()).getStudentName());
                LogUtils.operation(String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getStudentName(), testTimes + 1));
                return;
            }
        }

        if (!isAllTest()) {
            roundNo = 1;
            stuAdapter.setTestPosition(0);
            oneView.setTestPosition(0);
            continuousTestNext(deviceIndex);
            return;
        }
        //全部次数测试完，
        allTestComplete();

    }

    /**
     * 判断有当前学生是否已经在测试
     *
     * @param studentCode
     * @return
     */
    private boolean hasStudentInDevice(String studentCode) {
        for (DeviceDetail detail : deviceDetails) {
            if (detail.getStuDevicePair().getStudent() != null && studentCode.equals(detail.getStuDevicePair().getStudent().getStudentCode())) {
                return true;
            }
        }
        return false;
    }


    private void identityMarkTest() {

    }

    private void printResult(BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        //是否已全部次数测试完成，非满分跳过
        if (getRoundNo(baseStuPair.getTimeResult()) < setTestCount() && !baseStuPair.isFullMark()) {
            return;
        }
        print(baseStuPair);
    }

    private void print(BaseStuPair baseStuPair) {
        Student student = baseStuPair.getStudent();
//        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机  " + group.getGroupNo() + "组");
        GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, baseStuPair.getStudent().getStudentCode());
        PrinterManager.getInstance().print("序  号:" + groupItem.getTrackNo() + "");
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


    private void saveResult(BaseStuPair baseStuPair, int roundNo, int index) {
        LogUtils.all("保存成绩:baseStuPair=" + baseStuPair.toString() + "---roundNo=" + roundNo + "---index=" + index);
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setMachineResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        roundResult.setTestTime(baseStuPair.getTestTime());
        roundResult.setEndTime(DateUtil.getCurrentTime() + "");
        if (baseStuPair.getRoundNo() != 0) {
            roundResult.setRoundNo(baseStuPair.getRoundNo());
            baseStuPair.setRoundNo(0);
            roundResult.setResultTestState(1);
        } else {
            roundResult.setRoundNo(roundNo);
            roundResult.setResultTestState(0);
        }
        roundResult.setTestNo(1);
        roundResult.setGroupId(group.getId());
        GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, baseStuPair.getStudent().getStudentCode());
        if (group.getExamType() == StudentItem.EXAM_MAKE) {
            roundResult.setExamType(group.getExamType());
        } else {
            roundResult.setExamType(groupItem.getExamType());
        }
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
            } else {
                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                } else {
                    roundResult.setIsLastResult(0);
                }
            }
        } else {
            // 第一次测试
            roundResult.setIsLastResult(1);

        }
        if (roundResult.getTestTime() == null) {
            roundResult.setTestTime(System.currentTimeMillis() + "");
        }
        DBManager.getInstance().insertRoundResult(roundResult);
        LogUtils.operation("保存成绩:" + roundResult.toString());
        updateLastResultLed(roundResult.getResultState() != RoundResult.RESULT_STATE_NORMAL ? "X" : ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()), index);
        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(group.getScheduleNo()
                , TestConfigs.getCurrentItemCode(), baseStuPair.getStudent().getStudentCode()
                , "1", group, RoundResultBean.beanCope(roundResultList, group));

        uploadResult(uploadResults);

        if (groupItem != null && groupItem.getExamType() == StudentItem.EXAM_MAKE) {
            toSkip(stuAdapter.getTestPosition());
        }
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

    private void updateLastResultLed(String result, int index) {
        //TODO 模式一对多 和  一对一 选择模式都是一样的，无法区分
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
//        String result = ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult());
//        if (ledMode == 0) {
        //todo 无法区分模式 使用设备数量来区分一对多与一对一模式 ，一对一只对1号设备操作
        if (deviceDetails.size() > 1) {
            int x = ResultDisplayUtils.getStringLength(result) == 0 ? 1 : ResultDisplayUtils.getStringLength(result);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), result, 16 - x, index, false, true);
        } else {
            try {
                int testRound = getRoundNo(deviceDetails.get(index).getStuDevicePair().getTimeResult()) - 1;
                if (TextUtils.isEmpty(result)) {
                    testRound += 1;
                }

                byte[] data = new byte[16];
                String ledName = deviceDetails.get(index).getStuDevicePair().getStudent().getLEDStuName() + "   第" +
                        testRound + "次";
                byte[] strData = ledName.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                //todo
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 0, false, true);
                data = new byte[16];
                String str = "当前：";
                strData = str.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                byte[] resultData = result.getBytes("GB2312");
                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
                //todo
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 1, false, true);
//            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1, data, 0, 1, false, true);
                RoundResult bestResult = DBManager.getInstance().queryGroupBestScore(deviceDetails.get(index).getStuDevicePair().getStudent().getStudentCode(), group.getId());
                if (bestResult != null && bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                    data = new byte[16];
                    str = "最好：";
                    strData = str.getBytes("GB2312");
                    System.arraycopy(strData, 0, data, 0, strData.length);
                    resultData = ResultDisplayUtils.getStrResultForDisplay(bestResult.getResult()).getBytes("GB2312");
                    System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
                    //todo
                    mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 2, false, true);
                } else {
                    //todo
                    data = new byte[16];
                    str = "最好：";
                    strData = str.getBytes("GB2312");
                    System.arraycopy(strData, 0, data, 0, strData.length);
                    mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 2, false, true);

                }
                if (!SettingHelper.getSystemSetting().isIdentityMark()) {
                    //todo
                    data = new byte[16];
                    ledName = "下一位：" + getNextName();
                    strData = ledName.getBytes("GB2312");
                    System.arraycopy(strData, 0, data, 0, strData.length);
                    mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 3, false, true);

//                    mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "下一位：" + getNextName(), 0, 3, false, true);
//                mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, "下一位：" + getNextName(), 0, 3, false, true);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


    }

    private String getNextName() {
        if (pairList.size() == stuAdapter.getTestPosition() + 1) {
            if (setTestPattern() == 0) {//连续
                return "";
            } else {
                if (roundNo == setTestCount()) {
                    return "";
                } else {
                    return pairList.get(0).getStudent().getLEDStuName();
                }
            }
        } else {
            return pairList.get(stuAdapter.getTestPosition() + 1).getStudent().getLEDStuName();
        }
    }

    public abstract void toStart(int pos);

    public abstract int setTestDeviceCount();

    /**
     * 设置项目测试次数
     */
    public abstract int setTestCount();

    /**
     * 设置项目测试次数 0 连续 1 循环
     */
    public abstract int setTestPattern();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT
        ));
        Intent serverIntent = new Intent(this, UploadService.class);
        stopService(serverIntent);
    }


    class StuHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            deviceListAdapter.notifyDataSetChanged();
            rvTestStu.scrollToPosition(stuAdapter.getTestPosition());
            stuAdapter.notifyDataSetChanged();
        }

    }
}
