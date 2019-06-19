package com.baidu.tts.sample.config;

/**
 * Created by james on 2017/11/24.
 */

public class TtsConfig {
	
	//Badu TTS 的appId appKey 和 secretKey，与应用包名com.feipulai.standjump绑定
	//如果需要修改包名，需重新在百度开放平台同时修改包名
	public static final String appId = "10423495";
	public static final String appKey = "w2q2jVAdvf5Er7GVMLd78eT8";
	public static final String secretKey = "4ZNZzgRTHQIgdB4rkdN0ZkdVTNtwxDKY";
	
	public static final String TTS_OFFLINE_DIR = "/sdcard/baiduTTS/";
	public static final String TEXT_MODEL_FILENAME = TTS_OFFLINE_DIR + "bd_etts_text.dat";
	// male是男声 female女声
	public static final String MODEL_MAILE_FILENAME = TTS_OFFLINE_DIR + "bd_etts_speech_male.dat";
	// female女声
	public static final String MODEL_FEMAILE_FILENAME = TTS_OFFLINE_DIR + "bd_etts_speech_female.dat";
	
	public static final int FEMALE_VOICE = 0X1;
	public static final int MALE_VOICE = 0X2;
	
	
}
