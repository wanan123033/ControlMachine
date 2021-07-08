package com.feipulai.exam.activity.setting;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.JumpRopeManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.JumpRopeResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.adapter.CorrespondAdapter;
import com.feipulai.exam.activity.jump_rope.bean.CorrespondBean;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.activity.sport_timer.bean.SportTimerSetting;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;

public class CorrespondTestActivity extends BaseTitleActivity implements GetDeviceStatesTask.OnGettingDeviceStatesListener, RadioManager.OnRadioArrivedListener {
    private JumpRopeSetting setting;
    private SystemSetting systemSetting;
    @BindView(R.id.rv_devices)
    RecyclerView rv_devices;
    @BindView(R.id.btn_clear)
    Button btn_clear;

    private GetDeviceStatesTask statesTask;
    private JumpRopeManager mManager;
    private SportTimerManger sportTimerManger;
    private ExecutorService service;
    private List<CorrespondBean> correspondBeans;
    private CorrespondAdapter adapter;
    private int deviceCount;
    private SitPushUpManager sitPushUpManager;

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title = TestConfigs.machineNameMap.get(machineCode);
        return builder.setTitle(title + "通信质量测试");
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_jump_rope_correspond;
    }

    @Override
    protected void initData() {
        service = Executors.newSingleThreadExecutor();
        mManager = new JumpRopeManager();
        systemSetting = SettingHelper.getSystemSetting();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        setting = SharedPrefsUtil.loadFormSource(this, JumpRopeSetting.class);
        rv_devices.setLayoutManager(new GridLayoutManager(this, 5));
        deviceCount = 0;
        switch (machineCode) {
            case ItemDefault.CODE_ZFP:
                RunTimerSetting runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
                if (runTimerSetting.getInterceptPoint() == 3) {
                    deviceCount = Integer.parseInt(runTimerSetting.getRunNum()) * 2;
                } else {
                    deviceCount = Integer.parseInt(runTimerSetting.getRunNum());
                }
                break;
            case ItemDefault.CODE_TS:
                deviceCount = setting.getDeviceSum();
                break;
            case ItemDefault.CODE_SPORT_TIMER:
                deviceCount = SharedPrefsUtil.loadFormSource(this, SportTimerSetting.class).getDeviceCount();
                break;
            case ItemDefault.CODE_YWQZ:
                deviceCount = SharedPrefsUtil.loadFormSource(this, SitUpSetting.class).getDeviceSum();
                sitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP, 1);
                break;
        }

        correspondBeans = new ArrayList<>();

        for (int i = 1; i <= deviceCount; i++) {
            CorrespondBean bean = new CorrespondBean();
            bean.deviceId = i;
            correspondBeans.add(bean);
        }
        statesTask = new GetDeviceStatesTask(this);
        rv_devices.addItemDecoration(dividerItemDecoration);
        adapter = new CorrespondAdapter(this, correspondBeans);
        rv_devices.setAdapter(adapter);
        RadioManager.getInstance().setOnRadioArrived(this);
        sportTimerManger = new SportTimerManger();
    }

    @OnClick({R.id.btn_start, R.id.btn_stop, R.id.btn_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                btn_clear.setEnabled(false);
                statesTask.start();
                statesTask.resume();
                service.execute(statesTask);
                break;
            case R.id.btn_stop:
                btn_clear.setEnabled(true);
                statesTask.pause();
                statesTask.finish();
                break;
            case R.id.btn_clear:
                for (int i = 0; i < correspondBeans.size(); i++) {
                    CorrespondBean bean = correspondBeans.get(i);
                    bean.quality = null;
                    bean.receiverNum = 0;
                    bean.sendNum = 0;
                }
                adapter.notifyDataSetChanged();
                break;
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onGettingState(int position) {
        switch (machineCode) {
            case ItemDefault.CODE_ZFP:
            case ItemDefault.CODE_SPORT_TIMER:
                sportTimerManger.connect(position + 1, systemSetting.getHostId());
                break;
            case ItemDefault.CODE_TS:
                mManager.getJumpRopeState(systemSetting.getHostId(), position + 1, setting.getDeviceGroup() + 1);
                break;
            case ItemDefault.CODE_YWQZ:
                sitPushUpManager.getState(position + 1);
                break;
        }

        for (int i = 0; i < correspondBeans.size(); i++) {
            CorrespondBean bean = correspondBeans.get(i);
            if (bean.deviceId == position + 1) {
                bean.sendNum++;
                bean.quality = String.format("%.1f", (((double) bean.sendNum - (double) bean.receiverNum) / (double) bean.sendNum * 100)) + "%";
            }
        }
        runOnUiThread(runnable);
    }

    @Override
    public void onStateRefreshed() {

    }

    @Override
    public int getDeviceCount() {
        return deviceCount;
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.JUMPROPE_RESPONSE:
                JumpRopeResult result = (JumpRopeResult) msg.obj;
                if (result != null || result.getState() != 0) {
                    for (int i = 0; i < correspondBeans.size(); i++) {
                        CorrespondBean bean = correspondBeans.get(i);
                        if (result.getHostId() == systemSetting.getHostId() && result.getHandId() == bean.deviceId) {
                            bean.receiverNum++;
                            bean.quality = String.format("%.1f", (((double) bean.sendNum - (double) bean.receiverNum) / (double) bean.sendNum * 100)) + "%";
//                            bean.quality = (((double)bean.sendNum - (double)bean.receiverNum) / (double)bean.sendNum *100) + "%";
                        }
                    }
                    runOnUiThread(runnable);
                }

                break;
            case SerialConfigs.SPORT_TIMER_CONNECT:
                if (msg.obj instanceof SportResult) {
                    SportResult sportResult = (SportResult) msg.obj;
                    for (int i = 0; i < correspondBeans.size(); i++) {
                        CorrespondBean bean = correspondBeans.get(i);
                        if (sportResult.getHostId() == systemSetting.getHostId() && sportResult.getDeviceId() == bean.deviceId) {
                            bean.receiverNum++;
                            bean.quality = String.format("%.1f", (((double) bean.sendNum - (double) bean.receiverNum) / (double) bean.sendNum * 100)) + "%";
//                            bean.quality = (((double)bean.sendNum - (double)bean.receiverNum) / (double)bean.sendNum *100) + "%";
                        }
                    }
                    runOnUiThread(runnable);
                }

                break;
            case SerialConfigs.SIT_UP_GET_STATE:
                if (msg.obj instanceof SitPushUpStateResult) {
                    SitPushUpStateResult sitResult = (SitPushUpStateResult) msg.obj;
                    for (int i = 0; i < correspondBeans.size(); i++) {
                        CorrespondBean bean = correspondBeans.get(i);
                        if (sitResult.getDeviceId() == bean.deviceId) {
                            bean.receiverNum++;
                            bean.quality = String.format("%.1f", (((double) bean.sendNum - (double) bean.receiverNum) / (double) bean.sendNum * 100)) + "%";
//                            bean.quality = (((double)bean.sendNum - (double)bean.receiverNum) / (double)bean.sendNum *100) + "%";
                        }
                    }
                    runOnUiThread(runnable);
                }
                break;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statesTask.finish();
        service.shutdownNow();
    }
}
