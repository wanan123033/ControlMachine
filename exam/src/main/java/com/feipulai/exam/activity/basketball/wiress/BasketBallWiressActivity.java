package com.feipulai.exam.activity.basketball.wiress;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;

import butterknife.OnClick;

public class BasketBallWiressActivity extends BaseTitleActivity {

    private static final int GET_STATE = 1;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case GET_STATE:
                    getState(1);
                    break;
            }
            return false;
        }
    });

    private BasketBallResultJump resultJump = new BasketBallResultJump(){

    };

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_basketball_wireless;
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ){
            builder.setTitle("篮球运球");
        }else {
            builder.setTitle("足球运球");
        }
        return builder;
    }

    @Override
    protected void initData() {
        RadioManager.getInstance().setOnRadioArrived(resultJump);
        getState(1);
    }

    private void getState(int deviceId) {
        byte[] cmd = new byte[]{(byte) 0xAA,0x14,0x0D,0x03,0x01,0x00,0x00,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0d};
        cmd[5] = (byte) SettingHelper.getSystemSetting().getHostId();
        cmd[6] = (byte) deviceId;
        cmd[18] = (byte) sum(cmd,18);

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,cmd));
        mHandler.sendEmptyMessageDelayed(GET_STATE,1000 * 3);
    }

    @OnClick({R.id.tv_pair,R.id.txt_waiting,R.id.txt_illegal_return})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_pair:  //设备配对
                startActivity(new Intent(this,BasketBallPairActivity.class));
                break;
            case R.id.txt_waiting:  //等待发令
                break;
            case R.id.txt_stop_timing:  //停止计时
                break;
            case R.id.txt_illegal_return: //违规返回
                break;
        }
    }

    private int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

}
