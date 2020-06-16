package com.feipulai.host.activity.vision;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.VisionManager;
import com.feipulai.device.serial.beans.VisionResult;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.person.BasePersonTestActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.vision.bluetooth.BlueBindBean;
import com.feipulai.host.activity.vision.bluetooth.BlueToothHelper;
import com.feipulai.host.activity.vision.bluetooth.BlueToothListActivity;
import com.feipulai.host.activity.vision.bluetooth.ClientManager;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.utils.StringUtility;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.orhanobut.logger.Logger;

import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

public class VisionTestActivity extends BasePersonTestActivity {
    private static final int SKIP_STUDENT = 22;
    private BlueBindBean blueBind;
    private SweetAlertDialog thermometerOpenDialog;
    private static final int CHECK_THERMOMETER = 0x1;

    private MyHandler handler = new MyHandler(this){
        @Override
        public void handleMessage(Message msg) {
            handleMessage1(msg);
        }
    };

    private final BleConnectResponse bleConnectResponse = new BleConnectResponse() {
        @Override
        public void onResponse(int code, BleGattProfile bleGattProfile) {
            if (code == REQUEST_SUCCESS) {
                //设置读取
                ClientManager.getGattProfile(bleGattProfile);
                openBlueThermometerRead();
                if (thermometerOpenDialog != null && thermometerOpenDialog.isShowing()) {
                    thermometerOpenDialog.dismissWithAnimation();
                }
            } else {
                LogUtil.logDebugMessage("蓝牙连接断开");
                //提示打开蓝牙连接设备
                showThermometerOpenDialog();
            }
        }
    };
    private BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            Logger.e("接收到数据:---"+ StringUtility.bytesToHexString(value));
            if (value[3] != 0x39) {  //判断是不是心跳包
                Message msg = Message.obtain();
                msg.what = CHECK_THERMOMETER;
                msg.obj = value;
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                LogUtil.logDebugMessage("蓝牙读取连接成功");
                //"success");
            } else {
                LogUtil.logDebugMessage("蓝牙读取连接失败");
                //"failed");
            }
        }
    };
    //蓝牙连接状态
    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status != STATUS_CONNECTED && !TextUtils.isEmpty(blueBind.getBluetoothMac())) {
                LogUtil.logDebugMessage("蓝牙连接状态断开");
                ClientManager.connectDevice(blueBind.getBluetoothMac(), bleConnectResponse);
            }

        }
    };

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId());
        } else {
            title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId())
                    + "-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title).addRightText("蓝牙设置", new View.OnClickListener() {
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
    @Override
    protected void initData() {
        super.initData();
        txtStuSkip.setVisibility(View.VISIBLE);
        setTestType(1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        BlueToothHelper.init(MyApplication.getInstance());

        blueBind = BlueToothHelper.getBlueBind();
        if (!TextUtils.isEmpty(blueBind.getBluetoothMac())) {
            ClientManager.connectDevice(blueBind.getBluetoothMac(), bleConnectResponse);
            ClientManager.getClient().registerConnectStatusListener(blueBind.getBluetoothMac(), mConnectStatusListener);
        }
    }

    private void openBlueThermometerRead() {

        ClientManager.getClient().notify(blueBind.getBluetoothMac(), UUID.fromString(blueBind.getServerUUID())
                , UUID.fromString(blueBind.getCharacterUUID()), mNotifyRsp);
    }
    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        //0xF0 0x3F 0x06 0x32 ID1 ID2 ID3 ID4 CHECK  01100111
        toastSpeak("请"+baseStuPair.getStudent().getStudentName()+"准备测试");
        ClientManager.writeCharacter(VisionManager.START_TEST);
        pair.setStartTime(DateUtil.getCurrentTime());
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(getApplicationContext(),BlueToothListActivity.class));
    }

    @Override
    public void stuSkip() {
        pair.setStudent(null);
        refreshTxtStu(null);

    }

    private void showThermometerOpenDialog() {
        if (thermometerOpenDialog == null) {
            thermometerOpenDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("温馨提示")
                    .setContentText("请启动蓝牙或未进行蓝牙连接")

                    .setConfirmText("去连接").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            IntentUtil.gotoActivity(VisionTestActivity.this, BlueToothListActivity.class);
                        }
                    }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
        }
        if (!thermometerOpenDialog.isShowing() && !isFinishing()) {
            thermometerOpenDialog.show();
        }
        thermometerOpenDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                thermometerOpenDialog = null;
            }
        });
    }

    protected void handleMessage1(Message msg) {
        if (msg.what == CHECK_THERMOMETER && pair.getStudent() != null){
            byte[] bytearr = (byte[]) msg.obj;
            LogUtil.logDebugMessage("-------163"+StringUtility.bytesToHexString(bytearr));
            VisionResult result = new VisionResult(bytearr);
            pair.setResult(result.getLeftVision());
            pair.setBaseHeight(result.getRightVision());
            updateVision(pair);
            saveResult();

            handler.sendEmptyMessageDelayed(SKIP_STUDENT,2000);
        }else if (msg.what == SKIP_STUDENT){
            stuSkip();
        }
    }

    private void saveResult() {
        RoundResult result = new RoundResult();
        result.setResult(pair.getResult());
        result.setItemCode(TestConfigs.sCurrentItem.getItemCode());
        result.setIsLastResult(1);
        result.setWeightResult(pair.getBaseHeight());
        result.setStudentCode(pair.getStudent().getStudentCode());
        result.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        result.setResultState(0);
        result.setPrintTime(DateUtil.getCurrentTime()+"");
        result.setTestTime(pair.getStartTime()+"");
        result.setTestNo(1);
        result.setRoundNo(1);
        DBManager.getInstance().insertRoundResult(result);
    }
}
