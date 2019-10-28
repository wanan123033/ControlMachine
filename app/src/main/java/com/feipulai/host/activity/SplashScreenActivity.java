package com.feipulai.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.LogUtils;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.BuildConfig;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.main.MainActivity;
import com.feipulai.host.tts.TtsConfig;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 现在这完全是一个程序欢迎界面了
 */
public class SplashScreenActivity extends BaseActivity {

    public static final String MACHINE_CODE = "machine_code";
    @BindView(R.id.ll_image)
    LinearLayout llImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);


//        List<String> fileArray = FileUtil.getFilesAllName(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/跳绳说明文档");
//        Collections.sort(fileArray, fileNameComparator);
//        for (String fileName : fileArray) {
//            File lhsFile = new File(fileName);
//
//            Log.i("zzs", "111===>" + lhsFile.getName().substring(0, lhsFile.getName().indexOf(".")));
//            ImageView imageView = new ImageView(this);
//            imageView.setAdjustViewBounds(true);
////            Glide.with(this).load(fileName).into(imageView);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            llImage.addView(imageView, layoutParams);
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 2;
//            Bitmap bm = BitmapFactory.decodeFile(fileName, options);
//            imageView.setImageBitmap(bm);
//        }


    }

//    private Comparator<String> fileNameComparator = Collections.reverseOrder(new Comparator<String>() {
//        @Override
//        public int compare(String lhs, String rhs) {
//            File lhsFile = new File(lhs);
//            File rhsFile = new File(rhs);
//            return Integer.valueOf(rhsFile.getName().substring(0, rhsFile.getName().indexOf("."))).
//                    compareTo(Integer.valueOf(lhsFile.getName().substring(0, lhsFile.getName().indexOf("."))));
//        }
//    });

    @Override
    protected void onResume() {
        super.onResume();
//         这里是否还需要延时需要再测试后再修改
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }
        }, 500);


    }
//
//    /**
//     * 加载本地图片
//     * http://bbs.3gstdy.com
//     *
//     * @param url
//     * @return
//     */
//    public static Bitmap getLoacalBitmap(String url) {
//        try {
//            FileInputStream fis = new FileInputStream(url);
//            return BitmapFactory.decodeStream(fis);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    private void init() {
//		SoundPlayUtils.init(MyApplication.getInstance());
//		LogUtils.initLogger(BuildConfig.DEBUG,BuildConfig.DEBUG);
        ToastUtils.init(getApplicationContext());
        //这里初始化时间很长,大约需要3s左右
        TtsManager.getInstance().init(this, TtsConfig.APP_ID, TtsConfig.APP_KEY, TtsConfig.SECRET_KEY);


        new Thread(new Runnable() {
            @Override
            public void run() {
                SoundPlayUtils.init(MyApplication.getInstance());
                LogUtils.initLogger(BuildConfig.DEBUG, BuildConfig.DEBUG);
                //这里初始化时间很长,大约需要3s左右
//                TtsManager.getInstance().init(SplashScreenActivity.this,TtsConfig.APP_ID,TtsConfig.APP_KEY,TtsConfig.SECRET_KEY);
            }
        }).start();
    }

}
