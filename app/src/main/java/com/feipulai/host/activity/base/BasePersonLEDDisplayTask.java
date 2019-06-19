package com.feipulai.host.activity.base;

import android.util.Log;

import com.feipulai.device.led.LEDManager;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2018/8/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class BasePersonLEDDisplayTask implements Runnable {

    private LEDManager mLEDManager = new LEDManager();
    private OnLedDisplayListener listener;
    private boolean isFinished;
    private boolean isWait3Sec = false;
    private volatile boolean mIsLEDDisplaying;

    public BasePersonLEDDisplayTask(OnLedDisplayListener listener) {
        this.listener = listener;
        mIsLEDDisplaying = true;
    }

    public void finish() {
        isFinished = true;
    }

    public void wait3Sec() {
        isWait3Sec = true;
    }

    public void pause() {
        mIsLEDDisplaying = false;
    }

    public void resume() {
        mIsLEDDisplaying = true;
    }

    @Override
    public void run() {
        int total;
        boolean isDropToClear;
        //保存每一轮LED显示学生的学生号
        List<String> stuCode = new ArrayList<>();
        while (!isFinished) {
            Log.i("zzs","while");
            total = 0;
            isDropToClear = false;
            try {

                for (int i = 0; i < listener.getDeviceCount() && mIsLEDDisplaying; i++) {
                    Log.i("zzs","mIsLEDDisplaying");
                    // 如果刚才显示了一个具体的检录人信息,这里等3s再继续
                    if (isWait3Sec) {
                        // 在一次显示过程中(没有刷新),有学生检录,isDropToClear为true,将接下来的一些学生信息筛选掉,直到LED显示下一屏
                        Thread.sleep(4000);
                        isDropToClear = true;
                        isWait3Sec = false;
                    }
                    Student student = listener.getStuInPosition(i);

                    //只显示有学生信息配对好的手柄
                    int currentY = total % 4;
                    boolean clearScreen = total % 4 == 0;
                    boolean updateScreen = total % 4 == 3;
                    boolean isLast = i == listener.getDeviceCount() - 1;

                    if (student != null) {
                        if (isDropToClear) {
                            if (clearScreen) {
                                isDropToClear = false;
                            } else {
                                continue;
                            }
                        }
                        String strToShow = listener.getStringToShow(i);
                        //保证最后会睡眠
                        updateScreen = isLast ? true : updateScreen;
                        mLEDManager.showString(TestConfigs.sCurrentItem.getMachineCode(),
                                listener.getHostId(),
                                strToShow,
                                0,
                                currentY,
                                clearScreen,
                                updateScreen);
                        total++;
                        //添加已显示学生
                        stuCode.add(student.getStudentCode());
                    } else if (isLast && !clearScreen) {
                        if (!isDropToClear) {
                            //	isDropToClear = false;
                            //	continue;
                            //}else{
                            //最后一个元素没有学生,且不需要新开一个屏幕,最后一行就显示为空行,更新屏幕,将之前屏幕缓存的内容显示出来
                            mLEDManager.showString(TestConfigs.sCurrentItem.getMachineCode(),
                                    listener.getHostId(),
                                    "                ",
                                    0,
                                    currentY,
                                    false,
                                    true);
                            updateScreen = true;
                        }
                    } else {
                        //跳过,保证不会出现不正常的睡眠
                        continue;
                    }


                    //显示完成本界面后停留4s,再刷新整个界面
                    if (updateScreen) {
                        Thread.sleep(4000);
                    }
                }
                listener.endLED(stuCode);
                stuCode.clear();
                //当前没有人检录,等待,避免总是循环,这台机器受不了
                if (total == 0) {
                    Thread.sleep(4000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public interface OnLedDisplayListener {
        /**
         * 这里返回设备具体数量,用于获取设备信息时指定
         *
         * @return 设备具体数量
         */
        int getDeviceCount();

        /**
         * 获取第position个位置的学生信息,没有就返回null
         *
         * @param position 位置
         * @return 第position个位置的学生信息, 没有就返回null
         */
        Student getStuInPosition(int position);

        /**
         * 对于第position个位置,LED屏幕应显示的内容
         *
         * @param position 位置
         * @return LED屏幕应显示的内容
         */
        String getStringToShow(int position);

        /**
         * 第一次数据轮播结束
         */
        void endLED(List<String> stuCode);

        /**
         * 返回主机号
         *
         * @return 返回主机号
         */
        int getHostId();
    }

}
