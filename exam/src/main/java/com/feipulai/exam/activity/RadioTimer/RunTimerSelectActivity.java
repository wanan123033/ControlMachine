package com.feipulai.exam.activity.RadioTimer;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.MainActivity;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.NewRadioTestActivity;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.PreTestActivity;
import com.feipulai.exam.activity.SubItemsSelectActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.entity.Item;

/**
 * Created by zzs on  2019/6/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class RunTimerSelectActivity extends SubItemsSelectActivity {
    public static final String GOTO_FLAG = "goto_flag";
    private int gotoflag;
    private Context mContext;

    @Override
    protected void initData() {
        super.initData();
        gotoflag = getIntent().getIntExtra(GOTO_FLAG, 0);
        mContext = this;
        itemList.clear();
        itemList.add(new Item("V1版拦截器（有线）"));
        itemList.add(new Item("V2版拦截器（无线）"));
        adapter.notifyDataSetChanged();
        getToolbar().setTitle("红外计时模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                SettingHelper.getSystemSetting().setRadioLed(position);
                if (gotoflag == 0) {
                    RunTimerSetting setting = SharedPrefsUtil.loadFormSource(mContext, RunTimerSetting.class);
                    setting.setConnectType(position);
                    if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                        startActivity(new Intent(mContext, position == 1 ? PreTestActivity.class : RunTimerActivityTestActivity.class));
                    } else {
                        startActivity(new Intent(mContext, BaseGroupActivity.class));
                    }
                    SharedPrefsUtil.save(mContext, setting);
                } else {
                    startActivity(new Intent(RunTimerSelectActivity.this, LEDSettingActivity.class));
                }
                finish();
            }
        });
    }

    @Override
    protected void toastSpeak(String msg) {

    }


}
