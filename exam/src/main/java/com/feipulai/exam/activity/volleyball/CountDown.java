package com.feipulai.exam.activity.volleyball;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Chronometer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * created by ww on 2019/11/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class CountDown extends Chronometer {

    private long time;
    private long nextTime;
    private OnTimeCompleteListener clistener;
    private SimpleDateFormat format;

    public CountDown(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        format = new SimpleDateFormat("mm:ss");
        this.setOnChronometerTickListener(listener);
    }

    /*
     *
     * 设置时间格式
     */
    public void setTimeFormat(String pattern) {
        format = new SimpleDateFormat(pattern);
    }

    public void setOnTimeCompleteListener(OnTimeCompleteListener l) {
        clistener = l;
    }

    OnChronometerTickListener listener = new OnChronometerTickListener() {

        @Override
        public void onChronometerTick(Chronometer chronometer) {
            // TODO Auto-generated method stub
            if (nextTime <= 0) {
                if (nextTime == 0) {
                    CountDown.this.stop();
                    if (null != clistener) {
                        clistener.onTimeComplete();
                    }
                }
                nextTime = 0;
                updateTimeNext();
                return;
            }
            nextTime--;
            updateTimeNext();
        }
    };

    /*
     * 初始化时间
     */
    public void initTime(long _time_s) {
        time = nextTime = _time_s;
        updateTimeNext();
    }

    private void updateTimeNext() {
        // TODO Auto-generated method stub
        this.setText(format.format(new Date(nextTime * 1000)));
    }

    /*
     * 重启
     */
    public void reStart(long _time_s) {
        if (_time_s == -1) {
            nextTime = time;
        } else {
            time = nextTime = _time_s;
        }
        this.start();
    }

    public void reStart() {
        reStart(-1);
    }

    /*
     * 继续计时
     */
    public void onResume() {
        this.start();
    }

    /*
     * 暂停计时
     */
    public void onPause() {
        this.stop();
    }

    interface OnTimeCompleteListener {
        void onTimeComplete();
    }
}
