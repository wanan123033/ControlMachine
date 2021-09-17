package com.feipulai.exam.activity.basketball.motion;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.BasketBallSettingActivity;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.orhanobut.logger.utils.LogUtils;

import java.util.List;

public class BasketBallMotionGroupActivity extends BaseTitleActivity {
    protected volatile int state = WAIT_FREE;
    private static final int WAIT_FREE = 0x0;
    private static final int WAIT_CHECK_IN = 0x1;
    private static final int WAIT_BEGIN = 0x2;
    private static final int TESTING = 0x3;
    private static final int WAIT_STOP = 0x4;
    private static final int WAIT_CONFIRM = 0x5;
    private BasketBallSetting setting;
    private List<BaseStuPair> stuPairs;
    private List<StuDevicePair> pairs;

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        return builder.setTitle("篮球运球"+hostId+"号机").addRightText("项目设置", new View.OnClickListener() {
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
    private void gotoItemSetting() {
        if (!isConfigurableNow()) {
            LogUtils.operation("跳转至篮球项目设置界面");
            IntentUtil.gotoActivityForResult(this, BasketBallSettingActivity.class, 1);
        } else {
            toastSpeak("测试中,不允许修改设置");
        }
    }
    private boolean isConfigurableNow() {
        boolean flag = !(state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_BEGIN);
        return flag;
    }
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_basketball;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, BasketBallSetting.class);
        if (setting == null)
            setting = new BasketBallSetting();
        TestCache.getInstance().init();

        stuPairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");
        pairs = CheckUtils.newPairs(stuPairs.size(),stuPairs);
        CheckUtils.groupCheck(pairs);
    }
}
