package com.feipulai.testandroid.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.feipulai.testandroid.R;
import com.feipulai.testandroid.view.PaintView;

public class ScreenActivity extends AppCompatActivity {


    private PaintView paintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_screen);
        initView();
    }

    private void initView() {
        paintView = findViewById(R.id.btn_paint);
        paintView.setBackgroundColor(getResources().getColor(R.color.color_red));
        mHandler.sendEmptyMessageDelayed(1,3000);
        mHandler.sendEmptyMessageDelayed(2,6000);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    paintView.setBackgroundColor(getResources().getColor(R.color.color_green));
                    break;
                case 2:
                    paintView.setBackgroundColor(getResources().getColor(R.color.color_blue));
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
