package com.feipulai.testandroid.activity;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.ConverterVersion;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.testandroid.R;

public class SettingActivity extends AppCompatActivity implements RadioManager.OnRadioArrivedListener {

    private TextView version;
    private EditText frequency;
    private RadioManager radioManager;
    private String versionCode = "";
    private LEDManager mLEDManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        radioManager = RadioManager.getInstance();
        radioManager.init();
        radioManager.setOnRadioArrived(this);
        version = findViewById(R.id.tv_version);
        frequency = findViewById(R.id.tv_frequency);
        TextView content = findViewById(R.id.tv_content);

        mLEDManager = new LEDManager();
        MachineCode.machineCode = 8 ;
    }

    /**
     * 获取版本
     * @param view
     */
    public void getVersion(View view) {
	    radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.CONVERTER, SerialConfigs.CMD_GET_CONVERTER_VERSION));
    }

    /**
     * 设置频段
     * @param view
     */
    public void settingFrequency(View view) {
        String fr = frequency.getText().toString().trim();
        if (TextUtils.isEmpty(fr))
            fr = "0" ;
        int fre = Integer.parseInt(fr);
        radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(fre)));

    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what){
            case SerialConfigs.CONVERTER_VERSION_RESPONSE:
	            ConverterVersion ver = (ConverterVersion)msg.obj;
	            versionCode = ver.getVersionCode();
	            runOnUiThread(new Runnable(){
		            @Override
		            public void run(){
			            version.setText("");
			            version.setText(versionCode);
		            }
	            });
                break;
            case SerialConfigs.CONVERTER_RADIO_CHANNEL_SETTING_RESPONSE:
                final byte data = (byte) msg.obj;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingActivity.this,data== 0 ? "频道设置成功":"频道设置失败",Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    /**
     * 亮度加 此处以跳绳为例 主机号为1
     * @param view
     */
    public void lightAdd(View view) {
        mLEDManager.increaseLightness(8, 1);
    }

    /**
     * 亮度减
     * @param view
     */
    public void lightMinus(View view) {
        Log.i("view",view.getId()+"");
        mLEDManager.decreaseLightness(8, 1);
    }

    /**
     * 屏幕连接
     * @param view
     */
    public void screenConnect(View view) {
        mLEDManager.link(8, 1);
        mLEDManager.resetLEDScreen(1,"跳绳");
    }

    /**
     * 自检
     * @param view
     */
    public void screenCheck(View view) {
        mLEDManager.test(8, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        radioManager.close();
    }
}
