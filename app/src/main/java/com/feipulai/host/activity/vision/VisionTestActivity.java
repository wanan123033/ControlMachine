package com.feipulai.host.activity.vision;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.VisionManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.beans.VisionResult;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
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
import com.feipulai.host.entity.Student;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.utils.StringUtility;
import com.feipulai.host.view.OperateProgressBar;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.orhanobut.logger.Logger;

import java.util.Calendar;
import java.util.UUID;

import butterknife.BindView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

public class VisionTestActivity extends BasePersonTestActivity {
    private static final int SKIP_STUDENT = 22;
    private BlueBindBean blueBind;
    private SweetAlertDialog thermometerOpenDialog;
    private static final int CHECK_THERMOMETER = 0x1;

    @BindView(R.id.tv_device_name)
    TextView tv_device_name;

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
                OperateProgressBar.removeLoadingUiIfExist(VisionTestActivity.this);
                ToastUtils.showShort("连接成功");
                tv_device_name.setVisibility(View.VISIBLE);
                tv_device_name.setText("设备蓝牙名称:"+blueBind.getBluetoothMac());
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
                pair.getBaseDevice().setState(BaseDeviceState.STATE_ERROR);
                refreshDevice();
            }else if (status == STATUS_CONNECTED && !TextUtils.isEmpty(blueBind.getBluetoothMac())){
                pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                refreshDevice();
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
        setTestType(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        BlueToothHelper.init(MyApplication.getInstance());
        pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        refreshDevice();
        blueBind = BlueToothHelper.getBlueBind();
        if (!TextUtils.isEmpty(blueBind.getBluetoothMac())) {
            OperateProgressBar.showLoadingUi(this,"正在重连蓝牙:"+blueBind.getBluetoothMac());
            ClientManager.connectDevice(blueBind.getBluetoothMac(), bleConnectResponse);
            ClientManager.getClient().registerConnectStatusListener(blueBind.getBluetoothMac(), mConnectStatusListener);
        }
        if (mLEDManager != null)
            mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
    }


//        @Override
//    protected void addStudent(Student student) {
//        super.addStudent(student);
//        Logger.i("stu===>"+student.toString());
//        if (student != null){
//            pair.setResult(25);
//            pair.setBaseHeight(41);
//            saveResult();
//        }
//    }

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
        if (mLEDManager != null)
            mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
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

            handler.sendEmptyMessageDelayed(SKIP_STUDENT,5000);
        }else if (msg.what == SKIP_STUDENT){
            stuSkip();
        }
    }

    private void saveResult() {
        printResult(pair);
        RoundResult result = new RoundResult();
        result.setResult(pair.getResult());
        result.setItemCode(TestConfigs.sCurrentItem.getItemCode());
        result.setWeightResult(pair.getBaseHeight());
        result.setStudentCode(pair.getStudent().getStudentCode());
        result.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        result.setResultState(0);
        result.setPrintTime(DateUtil.getCurrentTime()+"");
        result.setTestTime(pair.getStartTime()+"");
        result.setTestNo(1);
        result.setRoundNo(1);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(pair.getStudent().getStudentCode());
        if (bestResult != null) {
            Logger.i("bestResult==>"+bestResult.toString());
            result.setIsLastResult(1);
            bestResult.setIsLastResult(0);
            DBManager.getInstance().updateRoundResult(bestResult);
        } else {
            Logger.i("bestResult==>null");
            // 第一次测试
            result.setIsLastResult(1);
        }
        DBManager.getInstance().insertRoundResult(result);

        if (TestConfigs.sCurrentItem.getfResultType() == 0) {
            //最好
            if (bestResult != null && bestResult.getIsLastResult() == 1)
                uploadResult(result, bestResult);
            else
                uploadResult(result, result);
        } else {
            //最后
            uploadResult(result, result);
        }
    }

    private void printResult(@NonNull BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        Student student = baseStuPair.getStudent();
        PrinterManager.getInstance().print(
                String.format(getString(R.string.host_name), TestConfigs.sCurrentItem.getItemName(), SettingHelper.getSystemSetting().getHostId()));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_code), student.getStudentCode()));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_name), student.getStudentName()));
        PrinterManager.getInstance().print("成  绩:左"+ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())+",右"+ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getBaseHeight()));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_time), TestConfigs.df.format(Calendar.getInstance().getTime())));
        PrinterManager.getInstance().print(" \n");

    }
}
