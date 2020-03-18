package com.feipulai.common.jump_rope.facade;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.jump_rope.task.GetReadyCountDownTimer;
import com.feipulai.common.jump_rope.task.LEDContentGenerator;
import com.feipulai.common.jump_rope.task.LEDResultDisplayTask;
import com.feipulai.common.jump_rope.task.LEDResultDisplayer;
import com.feipulai.common.jump_rope.task.TestingCountDownTimer;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.device.led.LEDManager;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by James on 2018/8/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class CountTimingTestFacade {

    private ExecutorService mExecutor;
    private GetDeviceStatesTask mGetDeviceStatesTask;
    private GetReadyCountDownTimer mGetReadyCountDownTimer;
    private TestingCountDownTimer mTestingCountDownTimer;
    private LEDResultDisplayer mLEDResultDisplayer;
    private LEDResultDisplayTask mLEDResultDisplayTask;
    private LEDManager mLEDManager = new LEDManager();
    private Listener mListener;
    private Builder builder;

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public CountTimingTestFacade(Builder builder) {
        this.builder = builder;
        mExecutor = Executors.newCachedThreadPool();
        // 只有在真正退出测试时,才会停止获取设备状态
        mGetDeviceStatesTask = new GetDeviceStatesTask(builder.mOnGettingDeviceStatesListener);
        // 成绩显示工具
        mLEDResultDisplayer = new LEDResultDisplayer(builder.size, builder.hostId, builder.mGenerator);
        mExecutor.execute(mGetDeviceStatesTask);
    }

    public void resetLed() {
        mLEDManager.showString(builder.hostId, "测试准备", 3, 0, false, true);
    }

    // 重置任务
    private void reset() {
        //Log.i("builder",builder.toString());
        // 成绩显示的任务,在测试完成时,开始运行
        mLEDResultDisplayTask = new LEDResultDisplayTask(mLEDResultDisplayer);

        mTestingCountDownTimer = new TestingCountDownTimer(
                builder.testTime * 1000,
                1000,
                builder.countStartTime * 1000,
                builder.countFinishTime,
                new TestingCountDownTimer.OnTestingCountDownListener() {
                    //private long lastTime;

                    @Override
                    public void onTick(long tick) {
                        mGetDeviceStatesTask.pause();
                        if (tick == 0) {
                            mListener.onTestingTimerFinish();
                            //结束
                            mLEDManager.showString(builder.hostId, "测试结束", 3, 0, false, true);
                            mLEDResultDisplayer.showDetails();

                            mExecutor.execute(mLEDResultDisplayTask);
                        } else {
                            mListener.onTestingTimerTick(tick);
                            // 倒计时中	每3s进行一次成绩更新
                            boolean needUpdateScore = (builder.testTime - tick) % 3 == 0;
                            byte[] data = new byte[16];
                            try {
                                byte[] resultData = DateUtil.formatTime(tick * 1000, "mm:ss").getBytes("GB2312");
                                int srcX = mLEDManager.getX(DateUtil.formatTime(tick * 1000, "mm:ss"));
                                System.arraycopy(resultData, 0, data, srcX - 1, resultData.length);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            mLEDManager.showString(builder.hostId, data,
                                    0, 0, tick == builder.testTime/*最开始要清一下屏*/, true);
                            if (needUpdateScore) {
                                mLEDResultDisplayer.showDetails();
                            }
                        }
                        mGetDeviceStatesTask.resume();
                    }
                });

        mGetReadyCountDownTimer = new GetReadyCountDownTimer(builder.countStartTime * 1000,
                1000,
                builder.hostId,
                new GetReadyCountDownTimer.onGetReadyTimerListener() {

                    @Override
                    public void beforeTick(long tick) {
                        mGetDeviceStatesTask.pause();
                        mListener.onGetReadyTimerTick(tick);
                    }

                    @Override
                    public void afterTick(long tick) {
                        // mGetDeviceStatesTask.resume();
                    }

                    @Override
                    public void onFinish() {
                        mListener.onGetReadyTimerFinish();
                        mExecutor.execute(mTestingCountDownTimer);
                        mGetDeviceStatesTask.resume();
                    }
                });
    }

    // 开始测试 可以用作 重新开始
    public void start() {
        stop();
        reset();
        mExecutor.execute(mGetReadyCountDownTimer);
    }

    // 结束设备状态获取,只有在测试真正结束时才调用
    private void stopGetDeivceState() {
        mGetDeviceStatesTask.finish();
        // 任务已经完成,可以退出历史舞台了
        mExecutor.shutdownNow();
    }

    public void stop() {
        if (mTestingCountDownTimer != null
                && mLEDResultDisplayTask != null
                && mGetReadyCountDownTimer != null) {
            mTestingCountDownTimer.cancel();
            mLEDResultDisplayTask.cancel();
            mGetReadyCountDownTimer.cancel();
        }
    }

    public void stopTotally() {
        stop();
        stopGetDeivceState();
    }

    public void pauseGettingState() {
        mGetDeviceStatesTask.pause();
    }

    public void resumeGettingState() {
        mGetDeviceStatesTask.resume();
    }


    public interface Listener {
        void onGetReadyTimerTick(long tick);

        void onGetReadyTimerFinish();

        void onTestingTimerTick(long tick);

        void onTestingTimerFinish();

    }

    public static class Builder {

        private int hostId;
        private int testTime;
        private int size;
        private LEDContentGenerator mGenerator;
        private GetDeviceStatesTask.OnGettingDeviceStatesListener mOnGettingDeviceStatesListener;
        private int countStartTime;
        private int countFinishTime;

        public Builder hostId(int hostId) {
            this.hostId = hostId;
            return this;
        }

        public Builder countStartTime(int countStartTime) {
            this.countStartTime = countStartTime;
            return this;
        }

        public Builder countFinishTime(int countFinishTime) {
            this.countFinishTime = countFinishTime;
            return this;
        }

        public Builder testTime(int testTime) {
            this.testTime = testTime;
            return this;
        }

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public Builder setLEDContentGenerator(LEDContentGenerator generator) {
            mGenerator = generator;
            return this;
        }

        public Builder setOnGettingDeviceStatesListener(GetDeviceStatesTask.OnGettingDeviceStatesListener onGettingDeviceStatesListener) {
            mOnGettingDeviceStatesListener = onGettingDeviceStatesListener;
            return this;
        }

        public CountTimingTestFacade build() {
            return new CountTimingTestFacade(this);
        }

    }

}
