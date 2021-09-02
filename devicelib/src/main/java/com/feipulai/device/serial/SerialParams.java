package com.feipulai.device.serial;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by pengjf on 2018/11/5.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public enum SerialParams {

    QR_CODE("/dev/ttyMT3", 9600),
    RS232("/dev/ttysWK3", 9600),
    RADIO("/dev/ttysWK0", 115200),
    IC_Card("/dev/ttysWK1", 115200),
    //	ID_CARD("/dev/ttyMT1",115200),
    ID_CARD("/dev/ttysWK2", 115200),
    PRINTER("/dev/ttyMT2", 115200);

    private String path;
    private int baudRate;
    private int type = 0;//0 串口 1 USB
    private int vid;
    private int pid;
    private int versions = 1;
    private static List<SerialParamsBean> serialList;

    SerialParams(String path, int baudRate) {
        this.baudRate = baudRate;
        this.path = path;
    }

    public static void init(Context context) {
        String model = Build.MODEL;
        Log.d("LOGGER", "手机型号：====》" + model);
        if (model.startsWith("ls")) {
            Log.d("LOGGER", "进入新机型配置");
            if (serialList == null) {
                String JsonData = getJson(context, "SerialPort_v2.json");//获取assets目录下的json文件数据
                Type type = new TypeToken<List<SerialParamsBean>>() {
                }.getType();
                serialList = new Gson().fromJson(JsonData, type);
            }

        } else if (model.startsWith("se328")) {
            if (serialList == null) {
                String JsonData = getJson(context, "SerialPort_v3.json");//获取assets目录下的json文件数据
                Type type = new TypeToken<List<SerialParamsBean>>() {
                }.getType();
                serialList = new Gson().fromJson(JsonData, type);
            }
        }
        if (serialList != null) {
            for (SerialParamsBean paramsBean : serialList) {
                if (TextUtils.equals(paramsBean.getName(), "QR_CODE")) {
                    setParams(QR_CODE, paramsBean);
                } else if (TextUtils.equals(paramsBean.getName(), "RS232")) {
                    setParams(RS232, paramsBean);
                } else if (TextUtils.equals(paramsBean.getName(), "RADIO")) {
                    setParams(RADIO, paramsBean);
                } else if (TextUtils.equals(paramsBean.getName(), "IC_Card")) {
                    setParams(IC_Card, paramsBean);
                } else if (TextUtils.equals(paramsBean.getName(), "ID_CARD")) {
                    setParams(ID_CARD, paramsBean);
                } else if (TextUtils.equals(paramsBean.getName(), "PRINTER")) {
                    setParams(PRINTER, paramsBean);
                }

            }
        }
    }

    private static void setParams(SerialParams params, SerialParamsBean paramsBean) {
        params.baudRate = paramsBean.getBaud();
        params.path = paramsBean.getSerial();
        params.versions = paramsBean.getVersions();
        if (TextUtils.equals(paramsBean.getType(), "usb")) {
            params.type = 1;
            params.pid = paramsBean.getPid();
            params.vid = paramsBean.getVid();
        }
    }

    private static String getJson(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public int getType() {
        return type;
    }

    public int getVersions() {
        return versions;
    }

    public void setVersions(int versions) {
        this.versions = versions;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

}

