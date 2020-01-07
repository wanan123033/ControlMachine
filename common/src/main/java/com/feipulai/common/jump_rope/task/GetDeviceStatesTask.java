package com.feipulai.common.jump_rope.task;

/**
 * Created by James on 2018/8/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

/**
 * 开启线程轮询获取设备状态,三次得不到设备状态判断为中断状态
 * 界面每3轮刷新显示状态信息
 */
public class GetDeviceStatesTask implements Runnable {

    private volatile boolean mIsFinished;
    private OnGettingDeviceStatesListener listener;
    private volatile boolean mIsGettingHandState;
    private int loopCount = 5;

    public void setLoopCount(int loopCount) {
        this.loopCount = loopCount;
    }

    public GetDeviceStatesTask(OnGettingDeviceStatesListener listener) {
        this.listener = listener;
    }

    public void finish() {
        mIsFinished = true;
    }

    public void pause() {
        mIsGettingHandState = false;
    }

    public void resume() {
        mIsGettingHandState = true;
    }

    @Override
    public void run() {
        if (listener == null) {
            throw new NullPointerException("必须设置OnGettingDeviceStatesListener");
        }
        int i, j;
        int deviceCount;
        try {
            while (!mIsFinished) {
                for (i = 0; i < loopCount; i++) {
                    deviceCount = listener.getDeviceCount();
                    for (j = 0; j < deviceCount; j++) {

                        while (!mIsGettingHandState) {
                            if (mIsFinished) {
                                return;
                            }
                            Thread.sleep(100);
                        }
                        // 对每一个获取状态
                        listener.onGettingState(j);
                        Thread.sleep(10);
                    }
                }
                // 没有获取状态时,不更新界面配对情况
                if (mIsGettingHandState) {
                    listener.onStateRefreshed();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public interface OnGettingDeviceStatesListener {
        /**
         * 发送获取状态命令
         *
         * @param position 表示本次需要给第position个设备发送获取状态命令
         */
        void onGettingState(int position);

        /**
         * 连续发送3轮获取状态消息后,回调该函数,这里面进行连接是否OK等业务逻辑处理
         */
        void onStateRefreshed();

        /**
         * 这里返回设备具体数量,用于获取设备信息时指定
         *
         * @return 设备具体数量
         */
        int getDeviceCount();
    }

}
