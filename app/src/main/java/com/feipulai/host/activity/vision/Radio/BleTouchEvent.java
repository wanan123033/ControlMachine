package com.feipulai.host.activity.vision.Radio;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.feipulai.common.view.baseToolbar.DisplayUtil;

/**
 * Created by zzs on  2020/12/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BleTouchEvent {
    public final static int EVENT_TOP = 1;
    public final static int EVENT_BOTTOM = 2;
    public final static int EVENT_LEFT = 3;
    public final static int EVENT_RIGHT = 4;
    public final static int EVENT_CANCEL = 5;
    public final static int EVENT_CONFIRM = 6;

    public BleTouchEvent(Context context) {
        this.context = context;
    }

    private Context context;
    private int x = 0;
    private int y = 0;
    private int moveX = 0;
    private int moveY = 0;

    public int onTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://按下
                Log.i("onTouchEvent", "ACTION_DOWN----> x: " + event.getX() + "y: " + event.getY());
                x = (int) event.getX();
                y = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("onTouchEvent", "ACTION_MOVE----> x: " + event.getX() + "y: " + event.getY());
                moveX = (int) event.getX();
                moveY = (int) event.getY();

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.i("onTouchEvent", "ACTION_UP-ACTION_CANCEL---> x: " + event.getX() + "y: " + event.getY());
                if (x == (int) event.getX() && y == (int) event.getY()) {
                    if (y < DisplayUtil.getScreenWidthPx(context) / 2) {
                        Log.i("onTouchEvent", "确定");
                        return EVENT_CONFIRM;
                        //确定
                    } else {
                        Log.i("onTouchEvent", "取消");
                        //取消
                        return EVENT_CANCEL;
                    }
                } else {
                    if (x == moveX) {
                        if (y > moveY) {
                            //下
                            Log.i("onTouchEvent", "下");
                            return EVENT_BOTTOM;
                        } else {
                            //上
                            Log.i("onTouchEvent", "上");
                            return EVENT_TOP;
                        }
                    } else if (y == moveY) {
                        if (x > moveX) {
                            //右
                            Log.i("onTouchEvent", "右");
                            return EVENT_RIGHT;
                        } else {
                            //左
                            Log.i("onTouchEvent", "左    ");
                            return EVENT_LEFT;
                        }
                    }
                }
                break;
        }
        return -1;
    }
}
