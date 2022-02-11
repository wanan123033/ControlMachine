package com.feipulai.exam.activity.base;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.NewRadioGroupActivity;
import com.feipulai.exam.activity.basketball.DribbleShootGroupActivity;
import com.feipulai.exam.activity.basketball.ShootSetting;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.medicineBall.MedicineBallSetting;
import com.feipulai.exam.activity.medicineBall.more_device.BallGroupMoreActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.pushUp.PushUpGroupActivity;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.pushUp.distance.PushUpDistanceTestActivity;
import com.feipulai.exam.activity.sargent_jump.SargentSetting;
import com.feipulai.exam.activity.sargent_jump.more_device.SargentTestGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.sitreach.SitReachSetting;
import com.feipulai.exam.activity.sitreach.more_device.SitReachMoreGroupActivity;
import com.feipulai.exam.activity.situp.newSitUp.SitUpArmCheckActivity;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.activity.standjump.StandJumpSetting;
import com.feipulai.exam.activity.standjump.more.StandJumpGroupMoreActivity;
import com.feipulai.exam.activity.volleyball.VolleyBallGroupActivity;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.config.StudentCache;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentThermometer;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.utils.StringChineseUtil;
import com.feipulai.exam.view.OperateProgressBar;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class BaseGroupCheckActivity extends BaseCheckActivity{
    public static final String GROUP_INFO = "GROUP_INFO";
    private SystemSetting systemSetting;

    @BindView(R.id.iv_mode)
    ImageView iv_mode;
    @BindView(R.id.tv_camera)
    FrameLayout tv_camera;
    @BindView(R.id.et_input_text)
    EditText et_input_text;
    private Group group;
    private Student student;
    private GroupItem groupItem;


    @Override
    protected BaseToolbar.Builder setToolbar(BaseToolbar.Builder builder) {
        builder.setTitle("分组检录");
        return builder;
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_check;
    }

    @Override
    protected void initData() {

        group = getIntent().getParcelableExtra(GROUP_INFO);
        systemSetting = SettingHelper.getSystemSetting();
        if (systemSetting.getCheckTool() == 0){ //条形码二维码
            iv_mode.setImageResource(R.mipmap.check_code);
            iv_mode.setVisibility(View.VISIBLE);
            tv_camera.setVisibility(View.GONE);
        }else if (systemSetting.getCheckTool() == 1){ //身份证
            iv_mode.setImageResource(R.mipmap.check_id);
            iv_mode.setVisibility(View.VISIBLE);
            tv_camera.setVisibility(View.GONE);
        }else if (systemSetting.getCheckTool() == 2){ //IC卡
            iv_mode.setImageResource(R.mipmap.check_ic);
            iv_mode.setVisibility(View.VISIBLE);
            tv_camera.setVisibility(View.GONE);
        }else if (systemSetting.getCheckTool() == 3){ //外接扫描枪
            iv_mode.setImageResource(R.mipmap.check_gun);
            iv_mode.setVisibility(View.VISIBLE);
            tv_camera.setVisibility(View.GONE);
        }else if (systemSetting.getCheckTool() == 4){  //人脸识别
            iv_mode.setVisibility(View.GONE);
            tv_camera.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.txt_search)
    public void onClick(View view){
        final CheckStudentInfoDialog dialog = new CheckStudentInfoDialog(this);
        String studentCode = et_input_text.getText().toString().trim();
        if (TextUtils.isEmpty(studentCode)){
            ToastUtils.showLong("请输入考号！");
            return;
        }
        Student student = DBManager.getInstance().queryStudentByIDCode(studentCode);
        if (student == null){
            student = DBManager.getInstance().queryStudentByStuCode(studentCode);
        }
        if(student == null){
            InteractUtils.toastSpeak(this, "查无此人！");
            return;
        }
        List<Student> studentList = DBManager.getInstance().getStudentsByGroup(group);
        boolean isGroupStudent = false;
        for (Student groupStudent : studentList){
            if (groupStudent.getStudentCode().equals(student.getStudentCode())){
                isGroupStudent = true;
                break;
            }
        }
        if (isGroupStudent) {
            et_input_text.setText("");
            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, studentCode);
            dialog.setGroupItem(groupItem);
            dialog.setStudent(student);
            dialog.setOnCheckInListener(new CheckStudentInfoDialog.onCheckInListener() {
                @Override
                public void onCheck(Student student) {
                    checkQulification(student.getStudentCode(),0);
                    dialog.dismissDialog();
                }
            });
            dialog.show();
        }else {
            InteractUtils.toastSpeak(this, "该考生不在该分组！");
            return;
        }
    }

    @Override
    public boolean checkQulification(String code, int flag) {
        Student student = null;
        switch (flag) {
            case ID_CARD_NO:
                student = DBManager.getInstance().queryStudentByIDCode(code);
                break;
            case STUDENT_CODE:
                student = DBManager.getInstance().queryStudentByStuCode(code);
                break;
        }
        final GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, code);
//        final List<RoundResult> results = DBManager.getInstance().queryGroupRound(code,group.getId()+"");
//        if (results != null && results.size() >= TestConfigs.getMaxTestCount()) {
//            SystemSetting setting = SettingHelper.getSystemSetting();
//            if (setting.isAgainTest() && setting.isResit()) {
//                final Student finalStudent = student;
//                new SweetAlertDialog(this).setContentText("需要重测还是补考呢?")
//                        .setCancelText("重测")
//                        .setConfirmText("补考")
//                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                AgainTestDialog dialog = new AgainTestDialog();
//                                dialog.setArguments(finalStudent, results, groupItem);
//                                dialog.setOnIndividualCheckInListener(BaseGroupCheckActivity.this);
//                                dialog.show(getSupportFragmentManager(), "AgainTestDialog");
//                                sweetAlertDialog.dismissWithAnimation();
//                            }
//                        })
//                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                ResitDialog dialog = new ResitDialog();
//                                dialog.setArguments(finalStudent, results, groupItem);
//                                dialog.setOnIndividualCheckInListener(BaseGroupCheckActivity.this);
//                                dialog.show(getSupportFragmentManager(), "ResitDialog");
//                                sweetAlertDialog.dismissWithAnimation();
//                            }
//                        }).show();
//                return false;
//            }
//            if (setting.isAgainTest()) {
//                AgainTestDialog dialog = new AgainTestDialog();
//                dialog.setArguments(student, results, groupItem);
//                dialog.setOnIndividualCheckInListener(this);
//                dialog.show(getSupportFragmentManager(), "AgainTestDialog");
//                return false;
//            }
//            if (setting.isResit()) {
//                ResitDialog dialog = new ResitDialog();
//                dialog.setArguments(student, results, groupItem);
//                dialog.setOnIndividualCheckInListener(this);
//                dialog.show(getSupportFragmentManager(), "ResitDialog");
//            } else {
//                InteractUtils.toastSpeak(this, "该考生已测试");
//                return false;
//            }
//        }
        this.student = student;
        this.groupItem = groupItem;
        checkInUIThread(student, groupItem);
        return false;
    }

    private void checkInUIThread(Student student, GroupItem groupItem) {
        SystemSetting setting = SettingHelper.getSystemSetting();
        if (setting.isAutoScore()) {
            HttpSubscriber subscriber = new HttpSubscriber();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    OperateProgressBar.showLoadingUi(BaseGroupCheckActivity.this, "正在获取云端成绩...");
                }
            });
            subscriber.getRoundResult(setting.getSitCode(), groupItem.getScheduleNo(), TestConfigs.getCurrentItemCode(), student.getStudentCode(),
                    null, null, null, String.valueOf(groupItem.getExamType()), this);
        } else {
            sendCheckHandlerMessage(student);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case CHECK_IN:
                if (SettingHelper.getSystemSetting().isStartThermometer()) {
                    StudentThermometer thermometer = DBManager.getInstance().getThermometer(groupItem);
                    if (thermometer == null){
                        showThermometerDialog();
                    }else {
                        if (student != null){
                            LogUtils.operation("检入考生：" + student.toString());
                            onCheckIn(student);
                        }
                    }
                }else {
                    if (student != null) {
                        LogUtils.operation("检入考生：" + student.toString());
                        onCheckIn(student);
                    }

                }
                break;
            case CHECK_THERMOMETER:
                byte[] value = (byte[]) msg.obj;
                LogUtil.logDebugMessage("蓝牙返回数据===》" + isStartThermometer);
                if (isStartThermometer == true) {
                    LogUtil.logDebugMessage("蓝牙返回数据校验===》" + StringChineseUtil.byteToString(value));
                    if (value.length < 3) {
                        //|| value[1] + value[2] != value[3]
                        toastSpeak("体温枪异常，请再次测量体温");
                        return;
                    }

                    String getThermometer = Long.parseLong(String.format("%02X", value[1]) + String.format("%02X", value[2]), 16) + "";
                    if (getThermometer.length() < 3) {
                        toastSpeak("请规范使用体温枪重新测量");
                        return;
                    }
                    String thermometer = getThermometer.substring(0, 2) + "." + getThermometer.substring(2);
                    LogUtil.logDebugMessage("蓝牙返回数据===》" + thermometer);
                    isStartThermometer = false;
                    String contentText = student.getStudentName() + ":" + thermometer + "℃";
                    //添加体温记录
                    StudentThermometer studentThermometer = new StudentThermometer();
                    studentThermometer.setStudentCode(student.getStudentCode());
                    studentThermometer.setExamType(mStudentItem.getExamType());
                    studentThermometer.setThermometer(Double.valueOf(thermometer));
                    studentThermometer.setItemCode(TestConfigs.getCurrentItemCode());
                    studentThermometer.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                    studentThermometer.setMeasureTime(DateUtil.getCurrentTime() + "");
                    DBManager.getInstance().insterThermometer(studentThermometer);

                    thermometerDialog.showCancelButton(false)
                            .setTitleText("测量完成")
                            .setContentText(contentText).changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    toastSpeak(student.getSpeakStuName() + thermometer + "℃");
//
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            thermometerDialog.dismissWithAnimation();
                        }
                    }, 2000);

                    if (student != null) {
                        LogUtils.operation("检入考生：" + student.toString());
                        onCheckIn(student);
                    }
                }
                break;
        }

    }

    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.tv_camera;
    }

    @Override
    public void onCheckIn(Student student) {
        //TODO 跳转测试页面测试
        Log.e("BaseGroupCheckActivity","学生信息："+student.toString());
        TestConfigs.baseGroupMap.put("group", group);
        TestCache.getInstance().setGroup(group);
        List<BaseStuPair> pairs = new ArrayList<>();
        List<RoundResult> results = getResults(student.getStudentCode());
        if (results.size() >= TestConfigs.getMaxTestCount()){
            InteractUtils.toastSpeak(this,"该人已测试完成");
            return;
        }
        BaseStuPair pair = new BaseStuPair();
        pair.setCanCheck(true);
        pair.setCanTest(true);
        pair.setStudent(student);
        setStuPairsData(pair, results);
        TestConfigs.baseGroupMap.put(student, results);
        List<RoundResult> roundResultList= DBManager.getInstance().queryGroupRoundAll
                (student.getStudentCode(),group.getId() + "");
        if (roundResultList.size()>=TestConfigs.getMaxTestCount()){
            List<Integer> rounds = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                if (results.size() > 0){  //需要改变轮次
                    int roundNo = results.get(i).getRoundNo();
                    rounds.add(roundNo);
                }
            }
            for (int j = 1 ; j <= TestConfigs.getMaxTestCount() ; j++) {
                if (!rounds.contains(j)) {
                    pair.setRoundNo(j);
                }
            }
        }
        pairs.add(pair);
        TestConfigs.baseGroupMap.put("basePairStu", pairs);
        StudentCache.getStudentCaChe().clear();
        for (int i = 0; i < pairs.size(); i++) {
            StudentCache.getStudentCaChe().addStudent(pairs.get(i).getStudent());
            LogUtils.operation("分组进入测试学生:" + pairs.get(i).getStudent().toString());
        }

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_FWC) {
            PushUpSetting setting = SharedPrefsUtil.loadFormSource(this, PushUpSetting.class);
            if ((setting.getTestType() == PushUpSetting.WIRELESS_TYPE && setting.getDeviceSum() == 1)
                    || setting.getTestType() == PushUpSetting.WIRED_TYPE) {
                startActivity(new Intent(this, PushUpGroupActivity.class));
                return;
            }
            if (setting.getTestType() == 2) {
                startActivity(new Intent(this, PushUpDistanceTestActivity.class));
            }
        }
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_MG &&
                SharedPrefsUtil.loadFormSource(this, SargentSetting.class).getType() == 2) {
            startActivity(new Intent(this, SargentTestGroupActivity.class));
            return;
        }

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HWSXQ &&
                SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class).getConnectType() == 1) {
            startActivity(new Intent(this, BallGroupMoreActivity.class));
            return;
        }

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_PQ
                && SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class).getType() == 2) {

            startActivity(new Intent(this, VolleyBallGroupActivity.class));
            return;
        }
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LDTY
                && SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class).getTestType() == 1) {
            startActivity(new Intent(this, StandJumpGroupMoreActivity.class));
            return;
        }
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZWTQQ
                && SharedPrefsUtil.loadFormSource(this, SitReachSetting.class).getTestType() == 1) {
            startActivity(new Intent(this, SitReachMoreGroupActivity.class));
            return;
        }

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ
                && SharedPrefsUtil.loadFormSource(this, ShootSetting.class).getTestType() == 2) {
            startActivity(new Intent(this, DribbleShootGroupActivity.class));
            return;
        }
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZFP
                && SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class).getConnectType() == 1) {
            startActivity(new Intent(this, NewRadioGroupActivity.class));
            return;
        }
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_YWQZ
                && SharedPrefsUtil.loadFormSource(this, SitUpSetting.class).getTestType() == 1) {
            startActivity(new Intent(this, SitUpArmCheckActivity.class));
            return;
        }
        startActivity(new Intent(this, TestConfigs.groupActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
    }

    @Override
    public void setRoundNo(Student student, int roundNo) {

    }
    // 获取学生成绩列表
    private List<RoundResult> getResults(String stuCode) {
        return DBManager.getInstance().queryGroupRound(stuCode,
                group.getId() + "");
    }
    public void setStuPairsData(BaseStuPair pair, List<RoundResult> roundResultList) {

        String[] result = new String[TestConfigs.getMaxTestCount()];
        for (int j = 0; j < roundResultList.size(); j++) {
            if (j < result.length) {
                switch (roundResultList.get(j).getResultState()) {
                    case RoundResult.RESULT_STATE_FOUL:
                        result[j] = "X";
                        break;
                    case RoundResult.RESULT_STATE_WAIVE:
                        result[j] = "放弃";
                        break;
                    case RoundResult.RESULT_STATE_BACK:
                    case -2:
                        result[j] = "中退";
                        break;
                    default:
                        result[j] = ResultDisplayUtils.getStrResultForDisplay(roundResultList.get(j).getResult());
                        break;
                }
            } else {
                break;
            }
        }
        pair.setTimeResult(result);
    }
}
