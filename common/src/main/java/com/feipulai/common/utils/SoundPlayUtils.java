package com.feipulai.common.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.feipulai.common.R;
import com.feipulai.common.voice.VoiceSetting;


/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2018/1/12.
 */
@SuppressWarnings("deprecation")
public class SoundPlayUtils {

    // SoundPool对象
    private static SoundPool mSoundPlayer = new SoundPool(12,
            AudioManager.STREAM_MUSIC, 5);
    private static SoundPlayUtils soundPlayUtils;
    // 上下文
    private static Context mContext;
    public static VoiceSetting voiceSetting;

    public static SoundPlayUtils init(Context context) {
        if (soundPlayUtils == null) {
            soundPlayUtils = new SoundPlayUtils();
        }
        if (mSoundPlayer == null) {
            mSoundPlayer = new SoundPool(12,
                    AudioManager.STREAM_MUSIC, 5);
        }
//        else {
//            mSoundPlayer.release();
//        }
        // 初始化声音
        mContext = context;
        voiceSetting = SharedPrefsUtil.loadFormSource(mContext, VoiceSetting.class);
        if (voiceSetting == null) {
            voiceSetting = new VoiceSetting();
        }

        if (voiceSetting.getVoiceType() == 0) {//女
            //注意：最多加载256个音频资源
            mSoundPlayer.load(mContext, R.raw.sound1, 1);// "1"             --------1
            mSoundPlayer.load(mContext, R.raw.sound2, 1);// "2"             --------2
            mSoundPlayer.load(mContext, R.raw.sound3, 1);// "3"             --------3
            mSoundPlayer.load(mContext, R.raw.sound4, 1);// "4"             --------4
            mSoundPlayer.load(mContext, R.raw.sound5, 1);// "5"             --------5
            mSoundPlayer.load(mContext, R.raw.sound6, 1);// "5"             --------6
            mSoundPlayer.load(mContext, R.raw.sound7, 1);// "5"             --------7
            mSoundPlayer.load(mContext, R.raw.sound8, 1);// "5"             --------8
            mSoundPlayer.load(mContext, R.raw.sound9, 1);// "5"             --------9
            mSoundPlayer.load(mContext, R.raw.sound10, 1);// "5"             --------10
            mSoundPlayer.load(mContext, R.raw.soundstart, 1);// "开始"       --------11
            mSoundPlayer.load(mContext, R.raw.soundend, 1);// "结束"         --------12
        } else {
            //注意：最多加载256个音频资源
            mSoundPlayer.load(mContext, R.raw.b_sound1, 1);// "1"             --------1
            mSoundPlayer.load(mContext, R.raw.b_sound2, 1);// "2"             --------2
            mSoundPlayer.load(mContext, R.raw.b_sound3, 1);// "3"             --------3
            mSoundPlayer.load(mContext, R.raw.b_sound4, 1);// "4"             --------4
            mSoundPlayer.load(mContext, R.raw.b_sound5, 1);// "5"             --------5
            mSoundPlayer.load(mContext, R.raw.sound6, 1);// "5"             --------6
            mSoundPlayer.load(mContext, R.raw.sound7, 1);// "5"             --------7
            mSoundPlayer.load(mContext, R.raw.sound8, 1);// "5"             --------8
            mSoundPlayer.load(mContext, R.raw.sound9, 1);// "5"             --------9
            mSoundPlayer.load(mContext, R.raw.sound10, 1);// "5"             --------10
            mSoundPlayer.load(mContext, R.raw.b_soundstart, 1);// "开始"       --------11
            mSoundPlayer.load(mContext, R.raw.b_soundend, 1);// "结束"         --------12
        }
        mSoundPlayer.load(mContext, R.raw.pre_ready, 1);// "各就各位"         --------13
        mSoundPlayer.load(mContext, R.raw.ready, 1);// "预备"         --------14
        mSoundPlayer.load(mContext, R.raw.beep, 1);// "枪声"         --------15
        mSoundPlayer.load(mContext, R.raw.look_camera, 1);// "正对摄像头"         --------16
        mSoundPlayer.load(mContext, R.raw.siren, 1);// "汽笛"         --------17
        if (voiceSetting.getVoiceType() == 0) {//女
            mSoundPlayer.load(mContext, R.raw.g_10, 1);// "20"         --------18
            mSoundPlayer.load(mContext, R.raw.g_20, 1);// "20"         --------19
            mSoundPlayer.load(mContext, R.raw.g_30, 1);// "30"         --------20
            mSoundPlayer.load(mContext, R.raw.g_ready, 1);// "预备"         --------21
        } else {
            mSoundPlayer.load(mContext, R.raw.b_10, 1);// "20"         --------18
            mSoundPlayer.load(mContext, R.raw.b_20, 1);// "20"         --------19
            mSoundPlayer.load(mContext, R.raw.b_30, 1);// "30"         --------20
            mSoundPlayer.load(mContext, R.raw.b_ready, 1);// "预备"         --------21
        }

        return soundPlayUtils;
    }

    /**
     * 释放资源
     */
    public static void releaseSoundPool() {
        if (mSoundPlayer != null) {
            mSoundPlayer.autoPause();
            mSoundPlayer.release();
            mSoundPlayer = null;
            soundPlayUtils = null;
        }
    }

    /**
     * 播放声音
     */
    public static void play(int soundID) {
        mSoundPlayer.play(soundID, 1, 1, 0, 0, 1);
    }

}
