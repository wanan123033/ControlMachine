package com.feipulai.device.spputils.beans;

import android.util.Log;

import com.feipulai.device.serial.beans.StringUtility;
import com.orhanobut.logger.utils.LogUtils;

import java.io.UnsupportedEncodingException;

public class RangerResult {
    private int id; //数据识别符 3F
    private double slantRange; //斜距
    private String unit; //距离单位
    private double vertical; //垂直角
    private double level; //水平角
    private String jsUnit; //角度单位
    private int result; //水平距离
    private int backlight; //回光强度
    private int frame; //菱镜常数

    private int type; //type=1: 测距结果数据

    private int level_d;  // 水平角 度
    private int level_g;  // 水平角 分
    private int level_m;  // 水平角 秒

    public RangerResult(byte[] result){
        try {
            String string = new String(result,"ASCII");
            String[] split = string.split("\\+");
            if (split.length >= 5) {
                Log.e("TAG----",string);
                id = split[0].getBytes()[0];
                String[] ms = split[1].split("m");
                slantRange = Double.parseDouble(ms[0]) / 1000.0;
                unit = "cm";
                vertical = Double.parseDouble(ms[1]) / 10000.0;
                level = Double.parseDouble(split[2].replace("d", "")) / 10000.0;
                String[] ss = split[3].split("t");
                this.result = Integer.parseInt(ss[0]);
                Log.e("TAG----","result="+this.result+"mm");
                backlight = Integer.parseInt(ss[1]);
                frame = Integer.parseInt(split[5].substring(0, 2));
                Log.e("TAG----", string);
                type = 1;
                level_d = (int) level;
                level_g = Integer.parseInt(split[2].replace("d", "").substring(3,5));
                level_m = Integer.parseInt(split[2].replace("d", "").substring(5,7));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        LogUtils.normal("测距返回数据(解析前):"+result.length+"---"+ StringUtility.bytesToHexString(result)+"---\n(解析后):"+toString());

    }

    public int getId() {
        return id;
    }

    public double getSlantRange() {
        return slantRange;
    }

    public String getUnit() {
        return unit;
    }

    public double getVertical() {
        return vertical;
    }

    public double getLevel() {
        return level;
    }

    public String getJsUnit() {
        return jsUnit;
    }

    public int getResult() {
        return result;
    }

    public int getBacklight() {
        return backlight;
    }

    public int getFrame() {
        return frame;
    }

    public int getType() {
        return type;
    }

    public int getLevel_d() {
        return level_d;
    }

    public int getLevel_g() {
        return level_g;
    }

    public int getLevel_m() {
        return level_m;
    }

    @Override
    public String toString() {
        return "RangerResult{" +
                "id=" + id +
                ", slantRange=" + slantRange +
                ", unit='" + unit + '\'' +
                ", vertical=" + vertical +
                ", level=" + level +
                ", jsUnit='" + jsUnit + '\'' +
                ", result=" + result +
                ", backlight=" + backlight +
                ", frame=" + frame +
                ", type=" + type +
                ", level_d=" + level_d +
                ", level_g=" + level_g +
                ", level_m=" + level_m +
                '}';
    }
}
