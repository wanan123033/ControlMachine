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
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.adapter.DeviceListAdapter;
import com.feipulai.exam.activity.sargent_jump.adapter.StuAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
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
    /**
     * 当前测试次数位
     */
    private int roundNo = 1;
    private List<BaseStuPair> pairList;
    private StuHandler stuHandler = new StuHandler();
    private boolean addStudent = true;
    private boolean isNextClickStart = true;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_more;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrinterManager.getInstance().init();
        group = (Group) TestConfigs.baseGroupMap.get("group");

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

        initLed();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getTestStudent();
            }
        }, 3000);
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
        boolean isOnUse = false;
        for (DeviceDetail deviceDetail : deviceDetails) {
            if (deviceDetail.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_ONUSE) {
                isOnUse = true;
            }
        }
        if (isOnUse) {
            toastSpeak("测试中,不允许修改设置");
        } else {
            IntentUtil.gotoActivity(this, LEDSettingActivity.class);
        }

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
            deviceDetails.add(detail);
        }
        initView();
    }

    /***
     * 刷新
     * @param index
     */
    public void refreshDevice(int index) {
        if (deviceDetails.get(index).getStuDevicePair().getBaseDevice() != null) {
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
    }

    private void initView() {
        deviceListAdapter = new DeviceListAdapter(deviceDetails);
        deviceListAdapter.setTestCount(setTestCount());

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        rvDeviceList.setLayoutManager(layoutManager);
        rvDeviceList.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int pos) {
                BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
                switch (view.getId()) {
                    case R.id.txt_skip://跳过
                        Student student = pair.getStudent();
                        if (student != null)
                            stuSkipDialog(student, pos);
                        break;
                    case R.id.txt_start://开始
                        if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR)
                            toStart(pos);
                        break;
                    case R.id.txt_confirm:
                        if (pair.getStudent() != null) {
                            confirmResult(pos);
                        }
                        break;
                }
            }
        });
    }

    public void setFaultEnable(boolean isPenalize) {
        this.isPenalize = isPenalize;
    }

    public void setNextClickStart(boolean nextClickStart) {
        isNextClickStart = nextClickStart;
    }

    protected void stuSkipDialog(final Student student, final int index) {
        new AlertDialog.Builder(this).setMessage("是否跳过" + student.getStudentName() + "考生测试")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.i("stuSkip:" + student.toString());
                        //测试结束学生清除 ，设备设置空闲状态
                        toSkip(index);
                        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
                    }
                }).setNegativeButton("取消", null).show();
    }


    protected void toSkip(int index) {
        deviceDetails.get(index).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        deviceDetails.get(index).getStuDevicePair().setCanTest(true);

        if (deviceDetails.get(index).getStuDevicePair().getStudent() != null) {
            Logger.i("studentSkip=>跳过考生：" + deviceDetails.get(index).getStuDevicePair().getStudent().getStudentName());
            deviceDetails.get(index).getStuDevicePair().setStudent(null);
            deviceDetails.get(index).getStuDevicePair().setTimeResult(new String[setTestCount()]);
            deviceListAdapter.notifyItemChanged(index);
        }
        //跳过成绩保存
        if (studentList == null || studentList.size() == 0 || stuAdapter.getTestPosition() == -1) {
            Logger.i("TestPosition" + stuAdapter.getTestPosition());
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
                toastSpeak("当前小组所有人员都测试完");
            } else {
                for (int i = 0; i < studentList.size(); i++) {
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
                        stuAdapter.notifyDataSetChanged();
                        roundNo++;
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
                stuAdapter.notifyDataSetChanged();
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


    private void allTestComplete() {
        //全部次数测试完，
        toastSpeak("分组考生全部测试完成，请选择下一组");
        stuAdapter.setTestPosition(-1);
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
                                int testTimes = getRound(pairList.get(j).getTimeResult());
                                roundNo = testTimes;
                                toastSpeak(String.format(getString(R.string.test_speak_hint), studentList.get(j).getStudentName(), testTimes + 1),
                                        String.format(getString(R.string.test_speak_hint), studentList.get(j).getStudentName(), testTimes + 1));
                                deviceListAdapter.notifyDataSetChanged();
                                rvTestStu.scrollToPosition(j);
                                stuAdapter.setTestPosition(j);
                                deviceListAdapter.notifyItemChanged(index);
                                break;
                            }
                        }
                    }
                }
            }

        }

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
                break;
            }

        }
        deviceListAdapter.notifyDataSetChanged();
    }

    public void updateDevice(BaseDeviceState deviceState) {
        int deviceId = deviceState.getDeviceId();
        BaseStuPair pair = null;
        int deviceIndex = 0;
        for (int i = 0; i < deviceCount; i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            if (id == deviceId) {
                pair = deviceDetails.get(i).getStuDevicePair();
                deviceIndex = i;
                break;
            }

        }

        pair.getBaseDevice().setState(deviceState.getState());
        //状态为测试已结束
        if (deviceState.getState() == BaseDeviceState.STATE_END) {
            if (pair.getStudent() != null) {
                Logger.i("考生" + pair.getStudent().toString());
            }
            Logger.i("设备成绩信息STATE_END==>" + deviceState.toString());
            pair.setCanTest(true);
            pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
            if (isPenalize && pair.getResultState() != RoundResult.RESULT_STATE_FOUL) {
                deviceDetails.get(deviceIndex).setConfirmVisible(true);
                deviceListAdapter.notifyItemChanged(deviceIndex);
            } else {
                doResult(pair, deviceIndex);
            }

        }

        if (deviceDetails.get(deviceIndex).getStuDevicePair().getBaseDevice() != null) {
            deviceListAdapter.notifyItemChanged(deviceIndex);
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
        for (int i = 0; i < timeResult.length; i++) {
            if (TextUtils.isEmpty(timeResult[i])) {
                timeResult[i] = (baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" :
                        ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
                break;
            }
        }
        deviceDetails.get(index).getStuDevicePair().setTimeResult(timeResult);
        deviceListAdapter.notifyItemChanged(index);

        //设置学生成绩
        Student student = deviceDetails.get(index).getStuDevicePair().getStudent();
        int stuIndex = studentList.indexOf(student);
        pairList.get(stuIndex).setFullMark(baseStu.isFullMark());
        pairList.get(stuIndex).setResultState(baseStu.getResultState());
        pairList.get(stuIndex).setTimeResult(timeResult);


        //TODO LED显示
    }

    //处理结果
    private void doResult(BaseStuPair pair, int deviceIndex) {
        Logger.i("考生" + pair.getStudent().toString());
        //保存成绩
        if (setTestPattern() == 0) {
            saveResult(pair, getRound(pair.getTimeResult()), deviceIndex);
        } else {
            saveResult(pair, roundNo, deviceIndex);
        }

        printResult(pair);
        //分配考生到机器中
        matchStudent(pair, deviceIndex);
    }

    /**
     * 分派考生到机器中
     *
     * @param pair
     * @param deviceIndex
     */
    private void matchStudent(BaseStuPair pair, int deviceIndex) {
        //非身份验证模式
        if (!SettingHelper.getSystemSetting().isIdentityMark()) {
            //连续测试
            if (setTestPattern() == 0) {
                int testTimes = deviceDetails.get(deviceIndex).getRound();
                testTimes++;
                deviceDetails.get(deviceIndex).setRound(testTimes);
                if (testTimes < setTestCount()) {
                    if (pair.isFullMark() && pair.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                        //测试下一个
                        continuousTestNext(deviceIndex);
                    } else {//继续测试同一个
                        toastSpeak(String.format(getString(R.string.test_speak_hint),
                                pair.getStudent().getStudentName(), testTimes + 1),
                                String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), testTimes + 1));
                        group.setIsTestComplete(2);
                        DBManager.getInstance().updateGroup(group);
                        if (!isNextClickStart) {
                            toStart(deviceIndex);
                        }
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
                stuAdapter.setTestPosition(stuAdapter.getTestPosition() + 1);
                loopTestNext(deviceIndex);
            }
        } else {
            //身份验证下一轮
            identityMarkTest();
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
                    roundNo = getRound(pairList.get(j).getTimeResult());
                    stuAdapter.setTestPosition(j);

                    if (hasStudentInDevice(studentList.get(j).getStudentCode()))
                        continue;
                    stuPos = j;
                    stuHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toastSpeak(String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getSpeakStuName(),
                                    roundNo + 1),
                                    String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getStudentName(),
                                            roundNo + 1));

                            deviceDetails.get(index).getStuDevicePair().setTimeResult(pairList.get(stuPos).getTimeResult());
                            deviceDetails.get(index).getStuDevicePair().setStudent(studentList.get(stuPos));
                            deviceListAdapter.notifyItemChanged(index);
                            rvTestStu.scrollToPosition(stuAdapter.getTestPosition());
                        }
                    }, 3000);

                    deviceDetails.get(index).getStuDevicePair().setBaseHeight(0);
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
                int testTimes = getRound(pairList.get(i).getTimeResult());
                stuAdapter.setTestPosition(i);
                if (hasStudentInDevice(studentList.get(i).getStudentCode()))
                    continue;

                stuPos = i;
                stuHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deviceDetails.get(deviceIndex).getStuDevicePair().setTimeResult(pairList.get(stuPos).getTimeResult());
                        deviceDetails.get(deviceIndex).getStuDevicePair().setStudent(studentList.get(stuPos));
                        deviceListAdapter.notifyItemChanged(deviceIndex);
                        rvTestStu.scrollToPosition(stuAdapter.getTestPosition());
                    }
                }, 3000);

                deviceDetails.get(deviceIndex).getStuDevicePair().setBaseHeight(0);
                deviceDetails.get(deviceIndex).setRound(testTimes);

                Logger.i("addStudent:" + studentList.get(i).toString());
                Logger.i("addStudent:当前考生进行第" + 1 + "次的第" + testTimes + "轮测试");
                toastSpeak(String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getSpeakStuName(), testTimes + 1),
                        String.format(getString(R.string.test_speak_hint), studentList.get(stuAdapter.getTestPosition()).getStudentName(), testTimes + 1));
                return;
            }
        }

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


    private void saveResult(BaseStuPair baseStuPair, int roundNo, int index) {
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setMachineResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
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

    private void updateLastResultLed(RoundResult roundResult, int index) {
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        String result = ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult());
        if (ledMode == 0) {
            int x = ResultDisplayUtils.getStringLength(result);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), result,16- x, index, false, true);
        }else {
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
        }

    }

    public abstract void toStart(int pos);

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
