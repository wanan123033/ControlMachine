package com.feipulai.common.jump_rope.facade;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.jump_rope.task.LEDCheckDisplayTask;
import com.feipulai.common.jump_rope.task.OnGetStateWithLedListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by James on 2018/7/31 0031.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

/**
 * 该类提供检录时Led显示和获取设备状态两个任务的集合
 */
public class GetStateLedFacade {

    private ExecutorService mExecutor;
    private GetDeviceStatesTask mGetDeviceStatesTask;
    private LEDCheckDisplayTask mLEDDisplayTask;

    public GetStateLedFacade(final OnGetStateWithLedListener listener) {
        mExecutor = Executors.newFixedThreadPool(2);
        //运行两个线程,分别处理获取设备状态和LED检录显示
        mGetDeviceStatesTask = new GetDeviceStatesTask(new GetDeviceStatesTask.OnGettingDeviceStatesListener() {
            @Override
            public void onGettingState(int position) {
                listener.onGettingState(position);
            }

            @Override
            public void onStateRefreshed() {
                listener.onStateRefreshed();
            }

            @Override
            public int getDeviceCount() {
                return listener.getDeviceCount();
            }
        });

        mLEDDisplayTask = new LEDCheckDisplayTask(new LEDCheckDisplayTask.OnLedDisplayListener() {
            @Override
            public int getDeviceCount() {
                return listener.getDeviceCount();
            }

            @Override
            public String getStringToShow(int position) {
                return listener.getStringToShow(position);
            }

            @Override
            public int getHostId() {
                return listener.getHostId();
            }
        });
        // 开始之前先全部不动,等待开始
        pause();
        mExecutor.execute(mLEDDisplayTask);
        mExecutor.execute(mGetDeviceStatesTask);
    }

    public void pause() {
        mLEDDisplayTask.pause();
        mGetDeviceStatesTask.pause();
    }

    public void resume() {
        mLEDDisplayTask.resume();
        mGetDeviceStatesTask.resume();
    }

    public void letDisplayWait3Sec() {
        mLEDDisplayTask.wait3Sec();
    }

    public void finish() {
        mGetDeviceStatesTask.finish();
        mLEDDisplayTask.finish();
        mExecutor.shutdownNow();
    }

    public void setmGetDeviceStatesLoopCount(int loopCount) {
        mGetDeviceStatesTask.setLoopCount(loopCount);
    }
}
