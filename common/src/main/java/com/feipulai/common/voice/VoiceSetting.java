package com.feipulai.common.voice;

/**
 * Created by zzs on  2021/2/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VoiceSetting {

    private int voiceType = 0; //0女 1 男

    private int voiceMode = 0;//0数字  1 嘟嘟.


    private boolean isTimeBroadcast = false; //是否进行时间播报


    public int getVoiceType() {
        return voiceType;
    }

    public void setVoiceType(int voiceType) {
        this.voiceType = voiceType;
    }

    public int getVoiceMode() {
        return voiceMode;
    }

    public void setVoiceMode(int voiceMode) {
        this.voiceMode = voiceMode;
    }

    public boolean isTimeBroadcast() {
        return isTimeBroadcast;
    }

    public void setTimeBroadcast(boolean timeBroadcast) {
        isTimeBroadcast = timeBroadcast;
    }
}
