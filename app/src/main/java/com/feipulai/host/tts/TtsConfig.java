package com.feipulai.host.tts;

import com.feipulai.host.MyApplication;
import com.feipulai.host.R;

/**
 * Created by james on 2017/11/24.
 */

public class TtsConfig{

	//Badu TTS 的appId APP_KEY 和 SECRET_KEY，与应用包名com.feipulai.host绑定
	//如果需要修改包名，需重新在百度开放平台同时修改包名
//	public static final String APP_ID = "10443933";
//	public static final String APP_KEY = "7dZtiofCHPtzvd5C5anMAdP7";
//	public static final String SECRET_KEY = "S7KqUUnUQeQPOUaMe3dWGWsGEy6d6sKn";

    public static final String APP_ID = MyApplication.getInstance().getString(R.string.tts_app_id);
    public static final String APP_KEY = MyApplication.getInstance().getString(R.string.tts_app_key);
    public static final String SECRET_KEY = MyApplication.getInstance().getString(R.string.tts_secret_key);
}
