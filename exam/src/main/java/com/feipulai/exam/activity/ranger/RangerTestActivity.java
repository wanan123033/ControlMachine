package com.feipulai.exam.activity.ranger;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.serial.beans.RangerCommand;
import com.feipulai.device.spputils.OnDataReceivedListener;
import com.feipulai.device.spputils.SppUtils;
import com.feipulai.device.spputils.beans.RangerResult;
import com.feipulai.exam.activity.base.PenalizeDialog;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.ranger.bluetooth.BluetoothManager;
import com.feipulai.exam.activity.ranger.usb.RangerUsbSerialUtils;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.utils.Toast;
import com.feipulai.exam.view.OperateProgressBar;
import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

public class RangerTestActivity extends BaseTestActivity  implements PenalizeDialog.PenalizeListener{
    private static final int GET_BLUETOOTH = 222;
    private static final int TEST = 333;
    private static final int SKIP_STU = 444;
    private static final int GET_USB = 555;
    SppUtils utils;
    RangerSetting setting;
    private RangerUsbSerialUtils usbSerialUtils;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (setting != null && setting.getConnectType() == 0) {
                if (msg.what == GET_BLUETOOTH) {
                    boolean connected = utils.isConnected();
                    if (!connected) {
                        pair.getBaseDevice().setState(BaseDeviceState.STATE_ERROR);
                    } else {
                        pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                    }
                    refreshDevice();
                    handler.sendEmptyMessageDelayed(GET_BLUETOOTH, 500);
                } else if (msg.what == TEST) {
                    BaseStuPair pair = (BaseStuPair) msg.obj;
                    if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
                        sendTestCommand(pair);
                        pair.setTestTime(System.currentTimeMillis() + "");
                    }
                } else if (msg.what == SKIP_STU) {
                    roundNo = 1;
                    stuSkip();
                }
            }else {
                if (msg.what == GET_USB && usbSerialUtils != null) {
                    boolean connected = usbSerialUtils.isConnected();
                    if (!connected) {
                        pair.getBaseDevice().setState(BaseDeviceState.STATE_ERROR);
                    } else {
                        pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                    }
                    refreshDevice();
                }
                handler.sendEmptyMessageDelayed(GET_USB, 500);
            }
        }
    };

    @Override
    protected void initData() {
        super.initData();
        setting = SharedPrefsUtil.loadFormSource(getApplicationContext(),RangerSetting.class);
        if (setting.getConnectType() == 0) {
            utils = BluetoothManager.getSpp(this);
            if (utils.isBluetoothEnabled()) {
                utils.setupService();
                utils.startService();
            } else {
                utils.enable();
                utils.setupService();
                utils.startService();
            }
        }else {
            usbSerialUtils = RangerUsbSerialUtils.getInstance(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (setting.getConnectType()== 0) {
            utils.setOnDataReceivedListener(new OnDataReceivedListener() {
                @Override
                protected void onResult(byte[] datas) {
                    onResults(datas);
                }
            });
        }else {
            if (usbSerialUtils != null){
                usbSerialUtils.setOnBufferListenner(new RangerUsbSerialUtils.OnBufferListenner(){
                    @Override
                    public void onBuffer(byte[] buffer) {
                        onResults(buffer);
                    }
                });
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (setting.getConnectType() == 0) {
            if (!utils.isBluetoothEnabled()) {
                //????????????
                utils.enable();
            } else {
                //????????????
                if (!utils.isServiceAvailable()) {
                    utils.setupService();
                    utils.startService();
                }
            }
            if (setting.getBluetoothName() != null && setting.getBluetoothMac() != null && !utils.isConnected()) {
                OperateProgressBar.showLoadingUi(this, "??????????????????:" + setting.getBluetoothName());
                utils.connect(setting.getBluetoothMac());
                utils.setBluetoothConnectionListener(new SppUtils.BluetoothConnectionListener() {
                    @Override
                    public void onDeviceConnected(String name, String address) {
                        OperateProgressBar.removeLoadingUiIfExist(RangerTestActivity.this);
                        Toast.showToast(getApplicationContext(), "????????? " + name, Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onDeviceDisconnected() {
                        OperateProgressBar.removeLoadingUiIfExist(RangerTestActivity.this);
                        Toast.showToast(getApplicationContext(), "????????????", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onDeviceConnectionFailed() {
                        OperateProgressBar.removeLoadingUiIfExist(RangerTestActivity.this);
                        Toast.showToast(getApplicationContext(), "????????????,???????????????", Toast.LENGTH_LONG);
                    }
                });
            }
            //????????????????????????
            handler.sendEmptyMessageDelayed(GET_BLUETOOTH, 500);
        }else {
            handler.sendEmptyMessageDelayed(GET_USB, 500);
        }
    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        Log.e("TAG-----","roundNo = "+roundNo);
        if (roundNo > setting.getTestNo()){
            ToastUtils.showLong("???????????????");
            return;
        }

        if (setting.getConnectType() == 0) {          //??????????????????
            Log.e("TAG----",pair.getStudent()+"--"+utils.isConnected());
            if (baseStuPair.getStudent() != null) {
                if (utils.isConnected()) {

                    utils.send(RangerCommand.MODE_UPDATE, false);
                    utils.send(RangerCommand.RANGER_COMMAND, false);
                    if (roundNo < setting.getTestNo() && setting.getAutoTestTime() > 0) {  //??????????????????
                        Message msg = Message.obtain();
                        msg.obj = baseStuPair;
                        msg.what = TEST;
                        handler.sendMessageDelayed(msg, setting.getAutoTestTime() * 1000);
                        return;
                    }
                    updateTestBtnState();
                } else {
                    ToastUtils.showLong("???????????????????????????");
                }
            } else {
                resultList.clear();
                rvTestResult.getAdapter().notifyDataSetChanged();
                ToastUtils.showLong("?????????????????????");
            }
        }else {
            if (usbSerialUtils.isConnected()){
                usbSerialUtils.sendCommand(RangerCommand.MODE_UPDATE);
                usbSerialUtils.sendCommand(RangerCommand.RANGER_COMMAND);
                updateTestBtnState();
            }else {
                ToastUtils.showShort("USB?????????");
            }
        }
    }

    @Override
    public int setTestCount() {
        return setting.getTestNo();
    }

    @Override
    public void gotoItemSetting() {
        if (pair.getStudent() == null) {
            startActivity(new Intent(this, RangerSettingActivity.class));
        }else {
            ToastUtils.showLong("?????????????????????????????????????????????");
        }
    }

    @Override
    public void stuSkip() {
        pair.setStudent(null);
        pair.setTimeResult(new String[setTestCount()]);
        refreshTxtStu(null);
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        return false;
    }

    @Override
    protected void confrim() {
        saveResult(pair);
        updateInitBtnState();
        result[roundNo - 1] = ResultDisplayUtils.getStrResultForDisplay(pair.getResult());
        adapter.setNewData(Arrays.asList(result));
        roundNo++;
        if (roundNo > setting.getTestNo()){
            ToastUtils.showLong("???????????????");
            handler.removeMessages(SKIP_STU);
            handler.sendEmptyMessageDelayed(SKIP_STU,3000);
            return;
        }
    }

    private void onResults(byte[] datas) {
        RangerResult result = new RangerResult(datas);
        LogUtils.operation("??????????????????:"+result.toString());
        if (result.getType() == 1){
            setScore(result);
        }

    }
    public void onDestroy() {
        super.onDestroy();
        //??????????????????
        if (setting.getConnectType() == 0) {
            if (utils.isConnected()) {
                utils.disconnect();
            }
            //????????????
            utils.unregisterRecvier();
            utils.stopService();
            handler.removeMessages(GET_BLUETOOTH);
            utils = null;
            BluetoothManager.spp = null;
        }
    }

    @Override
    public void showPenalize() {
        if (setting.isPenglize()) {
            PenalizeDialog dialog = new PenalizeDialog(this);
            dialog.setPenalizeListener(this);
            dialog.setMinMaxValue(-1, 1);
            dialog.show();
        }
    }

    @Override
    public RangerSetting getRangerSetting() {
        return setting;
    }

    @Override
    public void penalize(int value) {
        pair.setResult(pair.getResult()+value*1000);
        updateResult(pair);
    }

    @Override
    public void dismisson(DialogInterface dialog) {
        dialog.dismiss();
    }

    @Override
    public boolean getPenalize() {
        return true;
    }
}
