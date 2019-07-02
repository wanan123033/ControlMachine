package com.feipulai.common.tts;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.text.TextUtils;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.ToastUtils;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by James on 2018/7/23 0023.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class TtsManager {

    private SpeechSynthesizer mSpeechSynthesizer;//百度语音合成客户端
    private static TtsManager instance = new TtsManager();
    private boolean initedSuccess;

    private TtsManager() {
    }

    public static TtsManager getInstance() {
        return instance;
    }

    public void init(Context context, String appId, String appKey, String secretKey) {
        //把离线资源拷贝到SD卡baiduTTS目录中
        copyOfflineResources(context);
        //初始化TTS
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(context.getApplicationContext());
        //mSpeechSynthesizer.setSpeechSynthesizerListener(null);

        mSpeechSynthesizer.setAppId(appId);
        mSpeechSynthesizer.setApiKey(appKey, secretKey);
        //初始化  插入 删除  查找

        //验证权限 TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
        AuthInfo authInfo = mSpeechSynthesizer.auth(TtsMode.MIX);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名。
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Logger.e("tts 鉴权失败 errorMsg=" + errorMsg);
            ToastUtils.showShort("语音引擎打开失败,请联网获取授权");
            return;
        }
        //Logger.i("tts 验证通过，离线正式授权文件存在。");
        initedSuccess = true;
        // 文本模型文件路径(离线引擎使用)
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TtsConstants.TEXT_MODEL_FILENAME);
        // 声学模型文件路径 (离线引擎使用)，设置为女声,与在线相同
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, TtsConstants.MODEL_FEMALE_FILENAME);

        // 以下setParam参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");

        // 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "15"); // 设置合成时音频的音量，0-15 ，默认 15
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");// 设置合成的语速，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "6");// 设置合成的语调，0-15 ，默认 6
        // 该参数设置为TtsMode.MIX生效.即纯在线模式不生效.
        // MIX_MODE_DEFAULT默认,wifi状态下使用在线,非wifi离线.在线状态下,请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线,非wifi离线。在线状态下,请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK,3G 4G wifi状态下使用在线,其它状态离线。在线状态下,请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE,2G 3G 4G wifi状态下使用在线,其它状态离线。在线状态下,请求超时1.2s自动转离线
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
        mSpeechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //该接口用来设置播放器的音量，即使用speak 播放音量时生效。范围为[0.0f-1.0f]。
        mSpeechSynthesizer.setStereoVolume(1.0f, 1.0f);
        //切换离线发音
        //result = mSpeechSynthesizer.loadModel(speechModelPath,  textModelPath);
        int result = mSpeechSynthesizer.initTts(TtsMode.MIX);
        if (result != 0) {
            Logger.i("error code:" + result);
        }
        mSpeechSynthesizer.speak("菲普莱体育欢迎您");
    }

    private void copyOfflineResources(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            FileUtil.copyFromAssets(assetManager, "bd_etts_text.dat", TtsConstants.TEXT_MODEL_FILENAME);
            FileUtil.copyFromAssets(assetManager, "bd_etts_speech_male.dat", TtsConstants.MODEL_MALE_FILENAME);
            FileUtil.copyFromAssets(assetManager, "bd_etts_speech_female.dat", TtsConstants.MODEL_FEMALE_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将指定文本转换为语音输出
     *
     * @param text 文本
     */
    public void speak(String text) {
        if (!TextUtils.isEmpty(text) && initedSuccess) {
            mSpeechSynthesizer.speak(text);
        }
    }

    /**
     * 将指定文本转换为语音输出
     *
     * @param text 文本
     */
    public void batchSpeak(String text) {
        if (!TextUtils.isEmpty(text) && initedSuccess) {
            List<SpeechSynthesizeBag> bags = new ArrayList<>();

            char[] textChar = text.toCharArray();
            for (char c : textChar) {
                bags.add(getSpeechSynthesizeBag(String.valueOf(c), "0"));
            }
            mSpeechSynthesizer.batchSpeak(bags);
        }
    }

    private SpeechSynthesizeBag getSpeechSynthesizeBag(String text, String utteranceId) {
        SpeechSynthesizeBag speechSynthesizeBag = new SpeechSynthesizeBag();
        //需要合成的文本text的长度不能超过1024个GBK字节。
        speechSynthesizeBag.setText(text);
        speechSynthesizeBag.setUtteranceId(utteranceId);
        return speechSynthesizeBag;
    }
}
