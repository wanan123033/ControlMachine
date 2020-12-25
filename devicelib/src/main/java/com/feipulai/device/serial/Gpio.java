package com.feipulai.device.serial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.IBinder;
import android.os.IGpioService;
import android.os.RemoteException;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2018/11/6.
 */
public class Gpio {

    private IGpioService mGpioService;

    public static final int KEY_CODE_0 = 275;
    public static final int KEY_CODE_1 = 276;
    public static final int KEY_CODE_2 = 277;
    public static final int KEY_CODE_3 = 278;
    public static final int KEY_CODE_4 = 279;
    public static final int KEY_CODE_5 = 280;
    public static final int KEY_CODE_6 = 281;
    public static final int KEY_CODE_7 = 282;
    public static final int KEY_CODE_8 = 283;
    public static final int KEY_CODE_9 = 284;

    @SuppressLint("WrongConstant")
    public Gpio(Context context) {
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService",
                    String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"gpio"});
            mGpioService = IGpioService.Stub.asInterface(binder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param gpio  gpio index, 0~Number
     * @param value 0:low, 1:high, other:error
     */
    public void gpioWrite(int gpio, int value) {
        if (null != mGpioService) {
            try {
                mGpioService.gpioWrite(gpio, value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param gpio gpio index, 0~Number
     * @return 0:low, 1:high; -1:unavailable, other:error
     */
    public int gpioRead(int gpio) {
        if (null != mGpioService) {
            try {
                return mGpioService.gpioRead(gpio);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * @param gpio      gpio index, 0~Number
     * @param direction 0:input, 1:output
     * @param value     0:low, 1:high, other:error
     */
    public void gpioDirection(int gpio, int direction, int value) {
        if (null != mGpioService) {
            try {
                mGpioService.gpioDirection(gpio, direction, value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * gpio index, 0~Number,when registered，the input signal will be dispatched by KeyEvent.
     *
     * @param gpio
     */
    public void gpioRegKeyEvent(int gpio) {
        if (null != mGpioService) {
            try {
                mGpioService.gpioRegKeyEvent(gpio);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * gpio index, 0~Number,when unregistered，the input signal will not be dispatched by KeyEvent.
     *
     * @param gpio
     */
    public void gpioUnregKeyEvent(int gpio) {
        if (null != mGpioService) {
            try {
                mGpioService.gpioUnregKeyEvent(gpio);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get GPIO number
     *
     * @return 0:error, other GPIO number
     */
    public int gpioGetNumber() {
        if (null != mGpioService) {
            try {
                return mGpioService.gpioGetNumber();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}