package com.feipulai.wireless.utils;

/**
 * Created by pengjf on 2019/3/19.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ReusableThread implements Runnable {
    //线程状态监听者接口
    public interface ThreadStateListener {
        public abstract void onRunOver(ReusableThread thread);//当用户过程执行完毕后调用的方法
    }

    public static final byte STATE_READY = 0; //线程已准备好，等待开始用户过程
    public static final byte STATE_STARTED = 1; //用户过程已启动
    public static final byte STATE_DESTROYED = 2; //线程最终销毁

    byte mState; //标示可重启线程的当前状态

    Thread mThread; //实际的主线程对象
    Runnable mProc; //用户过程的run()方法定义在mProc中
    ThreadStateListener mListener; //状态监听者，可以为null

    /**
     * Creates a new instance of ReusableThread
     */
    public ReusableThread(Runnable proc) {
        mProc = proc;
        mListener = null;
        mThread = new Thread(this);
        mState = STATE_READY;
    }

    public byte getState() {
        return mState;
    }

    public void setStateListener(ThreadStateListener listener) {
        mListener = listener;
    }

    /**
     * 可以在处于等待状态时调用该方法重设用户过程
     */
    public synchronized boolean setProcedure(Runnable proc) {
        if (mState == STATE_READY) {
            mProc = proc;
            return true;
        } else
            return false;
    }

    /**
     * 开始执行用户过程
     */
    public synchronized boolean start() {
        if (mState == STATE_READY) {
            mState = STATE_STARTED;
            if (!mThread.isAlive()) mThread.start();
            notify(); //唤醒因用户过程执行结束而进入等待中的主线程
            return true;
        } else
            return false;
    }

    /**
     * 结束整个线程，销毁主线程对象。之后将不可再次启动
     */
    public synchronized void destroy() {
        mState = STATE_DESTROYED;
        notify();
        mThread = null;
    }

    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    while (mState != STATE_STARTED) {
                        if (mState == STATE_DESTROYED) return;
                        wait();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mProc != null) mProc.run();
            if (mListener != null) mListener.onRunOver(this); //当用户过程结束后，执行监听者的onRunOver方法

            synchronized (this) {
                if (mState == STATE_DESTROYED) return;
                mState = STATE_READY;
            }
        }
    }

}


