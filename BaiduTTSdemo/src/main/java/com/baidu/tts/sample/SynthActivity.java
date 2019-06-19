/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.tts.sample;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.baidu.tts.sample.control.InitConfig;
import com.baidu.tts.sample.control.MySyntherizer;
import com.baidu.tts.sample.control.NonBlockSyntherizer;
import com.baidu.tts.sample.listener.UiMessageListener;
import com.baidu.tts.sample.util.OfflineResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合成demo。含在线和离线，没有纯离线功能。
 * 根据网络状况优先走在线，在线时访问服务器失败后转为离线。
 */
public class SynthActivity extends BaseActivity implements View.OnClickListener {

    // ================== 初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    protected String appId = "8535996";

    protected String appKey = "MxPpf3nF5QX0pndKKhS7IXcB";

    protected String secretKey = "7226e84664474aa098296da5eb2aa434";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_speech_female.data为离线男声模型；bd_etts_speech_female.data为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;

    protected String DESC = "请先看完说明。之后点击“合成并播放”按钮即可正常测试。\n" +
            "测试离线合成功能需要首次联网。\n" +
            "纯在线请修改代码里ttsMode为TtsMode.ONLINE， 没有纯离线。\n" +
            "本Demo的默认参数设置为wifi情况下在线合成, 其它网络（包括4G）使用离线合成。 在线普通女声发音，离线男声发音.\n" +
            "合成可以多次调用，SDK内部有缓存队列，会依次完成。\n\n";

    private final String TAG = "SynthActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowText.setText(DESC);

        initialButtons(); // 配置onclick
        initialTts(); // 初始化TTS引擎
    }

    /**
     * 界面上的按钮对应方法
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.speak:
                speak(); // 合成并播放
                break;
            case R.id.synthesize:
                synthesize(); // 只合成不播放
                break;
            case R.id.batchSpeak:
                batchSpeak(); //  批量合成并播放
                break;
            case R.id.loadModel:
                loadModel(); // 停止合成引擎
                break;
            case R.id.pause:
                pause(); // 播放暂停
                break;
            case R.id.resume:
                resume(); // 播放恢复
                break;
            case R.id.stop:
                stop(); // 停止合成引擎
                break;
            case R.id.help: // 启动另一个activity
                mShowText.setText(DESC);
                break;
            default:
                break;
        }
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        // 设置初始化参数
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler); // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        Map<String, String> params = getParams();
        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, offlineVoice, params, listener);
        synthesizer = new NonBlockSyntherizer(this, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0"); // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_VOLUME, "5"); // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");// 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");// 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);         // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        return params;
    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private void speak() {
        mShowText.setText("");
        String text = mInput.getText().toString();
        //需要合成的文本text的长度不能超过1024个GBK字节。
        if (TextUtils.isEmpty(mInput.getText())) {
            text = "欢迎使用百度语音合成SDK,百度语音为你提供支持。";
            mInput.setText(text);
        }
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        int result = synthesizer.speak(text);
        checkResult(result, "speak");
    }


    /**
     * 合成但是不播放，
     * 音频流保存为文件的方法可以参见SaveFileActivity及FileSaveListener
     */
    private void synthesize() {
        mShowText.setText("");
        String text = this.mInput.getText().toString();
        if (TextUtils.isEmpty(mInput.getText())) {
            text = "欢迎使用百度语音合成SDK,百度语音为你提供支持。";
            mInput.setText(text);
        }
        int result = synthesizer.synthesize(text);
        checkResult(result, "synthesize");
    }

    /**
     * 批量播放
     */
    private void batchSpeak() {
        mShowText.setText("");
        List<Pair<String, String>> texts = new ArrayList<Pair<String, String>>();
        texts.add(new Pair<String, String>("开始批量播放，", "a0"));
        texts.add(new Pair<String, String>("123456，", "a1"));
        texts.add(new Pair<String, String>("欢迎使用百度语音，，，", "a2"));
        texts.add(new Pair<String, String>("重(chong2)量这个是多音字示例", "a3"));
        int result = synthesizer.batchSpeak(texts);
        checkResult(result, "batchSpeak");
    }


    /**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     */
    private void loadModel() {
        if (offlineVoice.equals(OfflineResource.VOICE_FEMALE)) {
            offlineVoice = OfflineResource.VOICE_MALE;
        } else {
            offlineVoice = OfflineResource.VOICE_FEMALE;
        }
        int result = synthesizer.loadModel(offlineVoice);
        checkResult(result, "loadModel");
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            toPrint("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }


    /**
     * 暂停播放。仅调用speak后生效
     */
    private void pause() {
        int result = synthesizer.pause();
        checkResult(result, "pause");
    }

    /**
     * 继续播放。仅调用speak后生效，调用pause生效
     */
    private void resume() {
        int result = synthesizer.resume();
        checkResult(result, "resume");
    }

    /*
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     */
    private void stop() {
        int result = synthesizer.stop();
        checkResult(result, "stop");
    }


    @Override
    protected void onDestroy() {
        synthesizer.release();
        Log.i(TAG, "释放资源成功");
        super.onDestroy();
    }

    protected void handle(Message msg) {
        switch (msg.what) {
            case INIT_SUCCESS:
                for (Button b : buttons) {
                    b.setEnabled(true);
                }
                msg.what = PRINT;
                break;
        }
        super.handle(msg);
    }

    private void initialButtons() {
        for (Button b : buttons) {
            b.setOnClickListener(this);
            b.setEnabled(false); // 先禁用按钮，等待引擎初始化后打开。
        }
        mHelp.setOnClickListener(this);
    }

}
