package com.feipulai.exam.activity.basketball;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.setting.SettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.orhanobut.logger.examlogger.LogUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseShootActivity extends BaseTitleActivity
        implements IndividualCheckFragment.OnIndividualCheckInListener {

    protected IndividualCheckFragment individualCheckFragment;
    private static final int WAIT_FREE = 0x0;
    private static final int WAIT_CHECK_IN = 0x1;
    private static final int WAIT_BEGIN = 0x2;
    private static final int TESTING = 0x3;
    private static final int WAIT_STOP = 0x4;
    private static final int WAIT_CONFIRM = 0x5;
    protected volatile int state = WAIT_FREE;
    private List<StuDevicePair> pairs = new ArrayList<>();

    @Override
    protected void initData() {
        individualCheckFragment = new IndividualCheckFragment();
        individualCheckFragment.setOnIndividualCheckInListener(this);

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (individualCheckFragment.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results) {
        if (student != null)
            LogUtils.operation("篮球检入到学生:" + student.toString());
        if (studentItem != null)
            LogUtils.operation("篮球检入到学生StudentItem:" + studentItem.toString());
        if (results != null)
            LogUtils.operation("篮球检入到学生成绩:" + results.size() + "----" + results.toString());
        if (state == WAIT_FREE || state == WAIT_CHECK_IN) {

            pairs.get(0).setStudent(student);

            for (RoundResult result : results) {
                if (isFullSkip(result.getResult(), result.getResultState())) {
                    toastSpeak("满分");
                    pairs.get(0).setStudent(null);
                    LogUtils.operation("篮球该学生已满分跳过测试:" + result.getStudentCode());
                    return;
                }
            }
            updateStudent(student);
        }


    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title).addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjectSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjectSetting();
            }
        });
    }

    /**
     * 跳转项目设置
     */
    private void startProjectSetting() {
        if (!isConfigurableNow()) {
            LogUtils.operation("跳转至篮球项目设置界面");
            IntentUtil.gotoActivityForResult(this, ShootSettingActivity.class, 1);
        } else {
            toastSpeak("测试中,不允许修改设置");
        }
    }

    /**
     * 是否是使用中
     */
    private boolean isConfigurableNow() {
        boolean flag = !(state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_BEGIN);
        LogUtils.operation("篮球isConfigurableNow(是否是使用中) = " + flag);
        return flag;
    }

    protected abstract void updateStudent(Student student);

    private boolean isFullSkip(int result, int resultState) {

        return false;
    }


}
