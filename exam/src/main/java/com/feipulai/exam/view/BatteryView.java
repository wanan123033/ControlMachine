package com.feipulai.exam.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.exam.R;


public class BatteryView extends RelativeLayout {

    private BatteryReceiver mBatteryReceiver;
    private View batteryRl;
    private View chargeView;
    private ImageView batteryIv;
    private TextView batteryTv;
    private int padding;

    /**
     * 这里的两个构造方法必须要重写
     */
    public BatteryView(Context context, AttributeSet attrs) {
        //这里需要调用自身的构造方法，通过自身的构造方法调用父类的构造方法
        this(context, attrs, 0);
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setType(int type) {
        invalidate();
        setBackgroundResource(R.drawable.common_ic_battery_white);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        //attachToRoot=true时,this.equals(view)=true,将inflate出来的布局的根布局就设置root,并返回root
        View view = inflater.inflate(R.layout.view_battery, this, true);
        //Log.e("james",this.equals(view) + "");

        //attachToRoot=false时,this.equals(view)=false,且之后的this.findViewById(R.id.rl_battery)报错
        //(因为这个Inflate出来的View根本就不在这个这个this内--如果这里加addView(view),就可以解决这个问题了)
        //View view = inflater.inflate(R.layout.view_battery,this,false);
        //Log.e("james",this.equals(view) + "");
        //addView(view);
        //setBackgroundResource(R.mipmap.common_ic_battery_black);
//        setBackgroundResource(R.drawable.common_ic_battery_white);
        batteryRl = findViewById(R.id.rl_battery);
        padding = batteryRl.getPaddingLeft() + batteryRl.getPaddingRight();
        chargeView =/* view.*/findViewById(R.id.iv_charge);
        batteryIv = (ImageView)/*view.*/findViewById(R.id.iv_battery_state);
        batteryTv = findViewById(R.id.txt_battery);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mBatteryReceiver = new BatteryReceiver();
        getContext().registerReceiver(mBatteryReceiver, intentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (getContext() != null && mBatteryReceiver != null) {
            getContext().unregisterReceiver(mBatteryReceiver);
        }
    }

    /**
     * 更新电量进度
     *
     * @param level 电量
     */
    public void updateState(int level) {
        if (level < 0) {
            return;
        }
        ViewGroup.LayoutParams lp = batteryRl.getLayoutParams();
        int width = (lp.width - padding) * level / 100;
        int height = lp.height;
        batteryIv.setLayoutParams(new LayoutParams(width, height));
        batteryTv.setText(level+"");
    }

    /**
     * 放电状态
     *
     * @param level 电量
     */
    public void updateView(int level) {
        chargeView.setVisibility(View.GONE);
        if (level <= 20) {
            batteryIv.setBackgroundResource(R.drawable.battery_low);
        } else {
            batteryIv.setBackgroundResource(R.drawable.battery_white);
        }
        updateState(level);
    }

    /**
     * 充电状态
     *
     * @param level 电量
     */
    public void updateChargingView(int level) {
        chargeView.setVisibility(View.VISIBLE);
        batteryIv.setBackgroundResource(R.drawable.battery_charge);
        updateState(level);
    }

    /**
     * 广播接受者
     */
    private class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //判断它是否是为电量变化的Broadcast Action
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                //获取当前电量
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                //Logger.i("当前电量" + level);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 1);
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        // 正在充电
                        updateChargingView(level);
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        updateView(level);
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        // 充满
                        updateChargingView(100);
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        //Log.i("james","BATTERY_STATUS_NOT_CHARGING:" + level);
                        // 不充电
                        updateView(level);
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        // 未知状态
                        break;

                    default:
                        break;

                }
            }
        }
    }

}