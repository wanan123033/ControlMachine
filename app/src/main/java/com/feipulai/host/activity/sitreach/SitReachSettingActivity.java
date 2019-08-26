package com.feipulai.host.activity.sitreach;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by zzs on  2019/8/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachSettingActivity extends BaseTitleActivity implements SerialDeviceManager.RS232ResiltListener {

    @BindView(R.id.sp_device_count)
    Spinner spDeviceCount;
    private static final int MSG_DISCONNECT = 0X101;
    private SweetAlertDialog alertDialog;
    private SerialHandler mHandler = new SerialHandler(this);
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sitreach_setting;
    }

    @Override
    protected void initData() {
        ArrayAdapter spDeviceCountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"1"});
        spDeviceCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceCount.setAdapter(spDeviceCountAdapter);
        SerialDeviceManager.getInstance().setRS232ResiltListener(this);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置").addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @OnClick(R.id.tv_device_check)
    public void onViewClicked() {
        alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        alertDialog.setTitleText("终端自检中...");
        alertDialog.setCancelable(false);
        alertDialog.show();
//        alertDialog = ProgressDialog.show(this, "", "终端自检中...", true);
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_EMPTY));
        //3秒自检
        mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
    }

    @Override
    public void onRS232Result(Message msg) {
        mHandler.sendMessage(msg);
    }

    /**
     * 回调
     */
    private static class SerialHandler extends Handler {

        private WeakReference<SitReachSettingActivity> mActivityWeakReference;

        public SerialHandler(SitReachSettingActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SitReachSettingActivity activity = mActivityWeakReference.get();
            //加载窗口是否显示
            boolean isDialogShow = true;
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT://连接失败
                        if (activity.isDisconnect) {
                            activity.toastSpeak("设备未连接");
                            //设置当前设置为不可用断开状态
                            if (activity.alertDialog.isShowing()) {
                                activity.alertDialog.dismiss();
                            }
                        }
                        break;
                    case SerialConfigs.SIT_AND_REACH_EMPTY_RESPONSE:
                        //检测设备是否连接成功
                        activity.isDisconnect = false;
                        isDialogShow = false;
                        activity.toastSpeak("设备连接成功");
                        Logger.i("空命令回复:");
                        break;
                }
                if (!isDialogShow && activity.alertDialog != null && activity.alertDialog.isShowing()) {
                    activity.alertDialog.dismiss();
                }

            }

        }
    }
}
