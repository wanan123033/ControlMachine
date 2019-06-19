package com.feipulai.common.utils;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by zzs on  2019/5/5
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AudioUtil {
    private AudioManager mAudioManager;
    private static AudioUtil mInstance;

    private AudioUtil(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public synchronized static AudioUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AudioUtil(context);
        }
        return mInstance;
    }

    //获取多媒体最大音量
    public int getMediaMaxVolume() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    //获取多媒体音量
    public int getMediaVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    //获取系统音量最大值
    public int getSystemMaxVolume() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
    }

    //获取系统音量
    public int getSystemVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
    }

    /**
     * 设置多媒体音量
     * 这里我只写了多媒体和通话的音量调节，其他的只是参数不同，大家可仿照
     */
    public void setMediaVolume(int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                volume, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
    }

}
