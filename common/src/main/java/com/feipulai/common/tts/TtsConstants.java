package com.feipulai.common.tts;

import android.os.Environment;

/**
 * Created by james on 2017/11/24.
 */

public class TtsConstants{
	
	public static final String TTS_OFFLINE_DIR = Environment.getExternalStorageDirectory() + "/baiduTTS/";
	public static final String TEXT_MODEL_FILENAME = TTS_OFFLINE_DIR + "bd_etts_text.dat";
	// male是男声 female女声
	public static final String MODEL_MALE_FILENAME = TTS_OFFLINE_DIR + "bd_etts_speech_male.dat";
	public static final String MODEL_FEMALE_FILENAME = TTS_OFFLINE_DIR + "bd_etts_speech_female.dat";
	
	public static final int FEMALE_VOICE = 0X1;
	public static final int MALE_VOICE = 0X2;
	

	
	public static final int MALE = 0;//男
	public static final int FEMALE = 1;//女
	
}
