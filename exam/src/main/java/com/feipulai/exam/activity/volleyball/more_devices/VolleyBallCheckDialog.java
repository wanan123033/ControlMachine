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
    private VolleyBallSetting setting;
    private Runnable runable = new Runnable() {
        @Override
        public void run() {
            sendSelfCheck(deviceId);
        }
    };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            VolleyPair868Result resul = (VolleyPair868Result) msg.obj;
            checkDeviceView.setData(setting.getTestPattern() == 0 ? VolleyBallSetting.ANTIAIRCRAFT_POLE : VolleyBallSetting.WALL_POLE, resul.getPositionList());

            return false;
        }
    });
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setting = SharedPrefsUtil.loadFormSource(getActivity().getApplicationContext(),VolleyBallSetting.class);
        RadioManager.getInstance().setOnRadioArrived(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        checkDeviceView = new CheckDeviceView(getActivity());
        checkDeviceView.setUnunitedData(setting.getTestPattern() == 0 ? VolleyBallSetting.ANTIAIRCRAFT_POLE : VolleyBallSetting.WALL_POLE);
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

    private void sendSelfCheck(final int deviceId) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        VolleyBallRadioManager.getInstance().selfCheck(hostId,deviceId);

        handler.postDelayed(runable,3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runable);
    }

    @Override
    public void onRadioArrived(Message msg) {
        handler.sendMessage(msg);
    }
}
