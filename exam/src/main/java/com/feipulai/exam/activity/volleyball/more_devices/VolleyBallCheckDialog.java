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
import com.feipulai.device.manager.VolleyBallRadioManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.VolleyPair868Result;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.volleyball.CheckDeviceView;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;

import java.util.ArrayList;
import java.util.List;

public class VolleyBallCheckDialog extends DialogFragment implements RadioManager.OnRadioArrivedListener {
    private CheckDeviceView checkDeviceView;
    private int deviceId;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            VolleyPair868Result resul = (VolleyPair868Result) msg.obj;
//                byte[] result = resul.getCheckResult();
//                List<Integer> states = new ArrayList<>();
//                for (int j = 0; j < result.length; j++) {
//                    if (result[j] == (byte) 0xFF) {
//                        for (int i = 0; i < 10; i++) {
//                            states.add(1);
//                        }
//                    } else {
//                        for (int i = 0; i < 10; i++) {
//                            states.add(0);
//                        }
//                    }
//                }
                checkDeviceView.setData(5, resul.getPositionList());
            sendSelfCheck(deviceId);
            return false;
        }
    });
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
//        checkDeviceView.setDeviceId(deviceId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        checkDeviceView.setDeviceId(deviceId);
        sendSelfCheck(deviceId);

    }

    private void sendSelfCheck(int deviceId) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        VolleyBallRadioManager.getInstance().selfCheck(hostId,deviceId);
    }
    @Override
    public void onRadioArrived(Message msg) {
        handler.sendMessage(msg);
    }
}