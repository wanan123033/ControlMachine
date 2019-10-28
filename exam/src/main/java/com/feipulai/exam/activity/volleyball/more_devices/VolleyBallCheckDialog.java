package com.feipulai.exam.activity.volleyball.more_devices;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.VolleyPairResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.volleyball.CheckDeviceView;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;

import java.util.ArrayList;
import java.util.List;

public class VolleyBallCheckDialog extends DialogFragment implements RadioManager.OnRadioArrivedListener {
    private CheckDeviceView checkDeviceView;
    private VolleyBallSetting setting;
    private int deviceId;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            VolleyPairResult resul = (VolleyPairResult) msg.obj;
            byte[] dataArr = resul.getDataArr();
            byte[] result = new byte[]{dataArr[14],dataArr[15],dataArr[16],dataArr[17],dataArr[18]};
            List<Integer> states = new ArrayList<>();
            for (int j = 0 ; j < result.length ; j++){
                if (result[j] == (byte)0xFF){
                    for (int i = 0 ; i < 10 ; i++){
                        states.add(1);
                    }
                }else {
                    for (int i = 0 ; i < 10 ; i++){
                        states.add(0);
                    }
                }
            }
            checkDeviceView.setData(5,states);
            return false;
        }
    });
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setting = SharedPrefsUtil.loadFormSource(getActivity(), VolleyBallSetting.class);
        RadioManager.getInstance().setOnRadioArrived(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        checkDeviceView = new CheckDeviceView(getActivity());
        checkDeviceView.setUnunitedData(5);
        checkDeviceView.setWiress(true);
        return checkDeviceView;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        deviceId = args.getInt("deviceId");
        checkDeviceView.setDeviceId(deviceId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sendSelfCheck(deviceId);

    }

    private void sendSelfCheck(int deviceId) {
        byte[] cmd = {(byte) 0xAA,0x0E,0x0A,0x03,0x01,0x00,0x00, (byte) 0xC7,0x00,0x00,0x00,0x00,0x00,0x0d};
        cmd[5] = (byte) SettingHelper.getSystemSetting().getHostId();
        cmd[6] = (byte) deviceId;
        cmd[12] = (byte) sum(cmd,12);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,cmd));
    }

    private int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    @Override
    public void onRadioArrived(Message msg) {
        handler.sendMessage(msg);
    }
}
