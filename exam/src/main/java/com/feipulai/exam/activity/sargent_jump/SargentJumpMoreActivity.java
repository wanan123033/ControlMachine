package com.feipulai.exam.activity.sargent_jump;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.DeviceListAdapter;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public abstract class SargentJumpMoreActivity extends BaseCheckActivity {

    @BindView(R.id.rv_device_list)
    RecyclerView rvDeviceList;
    private List<DeviceDetail> deviceDetails = new ArrayList<>();
    private int deviceCount;
    private int testNo;
    //成绩
    private String[] result;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sargent_jump_more;
    }

    @Override
    public void onCheckIn(Student student) {
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        testNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.get(0).getTestNo();
        //保存成绩，并测试轮次大于测试轮次次数
        if (roundResultList != null && roundResultList.size() >= setTestCount()) {
            //已测试，不重测
//            roundNo = roundResult.getRoundNo();
//            selectTestDialog(student);
            toastSpeak("该考生已测试完成");
            return;
        } else if (roundResultList != null) {
            for (RoundResult roundResult : roundResultList) {
                if (roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && isResultFullReturn(student.getSex(), roundResult.getResult())) {
                    toastSpeak("满分");
                    return;
                }

            }
        }

        //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
        if (roundResultList.size() == 0) {
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
        } else {
            testNo = roundResultList.get(0).getTestNo();
        }

        int index = 0 ;
        boolean canUseDevice = false ;
        for (int i = 0 ;i< deviceCount;i++){
            int state = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getState();
            if (state == BaseDeviceState.STATE_NOT_BEGAIN || state == BaseDeviceState.STATE_FREE){
                index = i ;
                canUseDevice = true ;
                break;
            }
        }

        if (canUseDevice){
            result = deviceDetails.get(index).getStuDevicePair().getTimeResult() ;
        }else {
            toastSpeak("当前无设备可添加学生测试");
            return;
        }

    }

    @Override
    protected void initData() {
        for (int i = 0; i < 4; i++) {
            deviceDetails.add(new DeviceDetail());
        }
        initView();

    }

    private void initView() {
        DeviceListAdapter deviceListAdapter = new DeviceListAdapter(deviceDetails);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvDeviceList.setLayoutManager(layoutManager);
        rvDeviceList.setAdapter(deviceListAdapter);


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

        return builder.setTitle(title).addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }).addRightText("项目设置", new View.OnClickListener() {
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


    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
        for (int i = 0; i < deviceCount; i++) {

        }
    }

    /**
     * 设置项目测试轮次次数
     */
    public abstract int setTestCount();

    public abstract boolean isResultFullReturn(int sex, int result);
    /**
     * 跳转项目设置页面
     */
    public abstract void gotoItemSetting();
}
