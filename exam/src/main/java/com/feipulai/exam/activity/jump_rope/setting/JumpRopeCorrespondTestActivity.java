package com.feipulai.exam.activity.jump_rope.setting;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.JumpRopeManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.JumpRopeResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.adapter.CorrespondAdapter;
import com.feipulai.exam.activity.jump_rope.bean.CorrespondBean;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.view.DividerItemDecoration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;

public class JumpRopeCorrespondTestActivity extends BaseTitleActivity implements GetDeviceStatesTask.OnGettingDeviceStatesListener, RadioManager.OnRadioArrivedListener {
    private JumpRopeSetting setting;
    private SystemSetting systemSetting;
    @BindView(R.id.rv_devices)
    RecyclerView rv_devices;
    @BindView(R.id.btn_clear)
    Button btn_clear;

    private GetDeviceStatesTask statesTask;
    private JumpRopeManager mManager;
    private ExecutorService service;
    private List<CorrespondBean> correspondBeans;
    private CorrespondAdapter adapter;
    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("跳绳-通信质量测试");
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
        setting = SharedPrefsUtil.loadFormSource(this,JumpRopeSetting.class);
        rv_devices.setLayoutManager(new GridLayoutManager(this,5));

        correspondBeans = new ArrayList<>();
        int deviceSum = setting.getDeviceSum();
        for (int i = 1 ; i <= deviceSum ; i++){
            CorrespondBean bean = new CorrespondBean();
            bean.deviceId = i;
            correspondBeans.add(bean);
        }
        statesTask = new GetDeviceStatesTask(this);
        rv_devices.addItemDecoration(dividerItemDecoration);
        adapter = new CorrespondAdapter(this,correspondBeans);
        rv_devices.setAdapter(adapter);
        RadioManager.getInstance().setOnRadioArrived(this);

    }

    @OnClick({R.id.btn_start,R.id.btn_stop,R.id.btn_clear})
    public void onClick(View view){
        switch (view.getId()){
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
                for (int i = 0 ; i < correspondBeans.size() ; i++){
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
        mManager.getJumpRopeState(systemSetting.getHostId(),position + 1,setting.getDeviceGroup()+1);
        for (int i = 0 ; i < correspondBeans.size() ; i++){
            CorrespondBean bean = correspondBeans.get(i);
            if (bean.deviceId == position + 1){
                bean.sendNum++;
                bean.quality = String.format("%.1f",(((double)bean.sendNum - (double)bean.receiverNum) / (double)bean.sendNum *100)) + "%";
            }
        }
        runOnUiThread(runnable);
    }

    @Override
    public void onStateRefreshed() {

    }

    @Override
    public int getDeviceCount() {
        return setting.getDeviceSum();
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.JUMPROPE_RESPONSE:
                JumpRopeResult result = (JumpRopeResult) msg.obj;
                if (result != null || result.getState() != 0){
                    for (int i = 0 ; i < correspondBeans.size() ; i++){
                        CorrespondBean bean = correspondBeans.get(i);
                        if (result.getHostId() == systemSetting.getHostId() && result.getHandId() == bean.deviceId){
                            bean.receiverNum++;
                            bean.quality = String.format("%.1f",(((double)bean.sendNum - (double)bean.receiverNum) / (double)bean.sendNum *100)) + "%";
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
