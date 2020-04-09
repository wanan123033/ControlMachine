package com.feipulai.device.spputils.beans;

import android.util.Log;

import java.io.UnsupportedEncodingException;

public class RangerResult {
    private int id; //数据识别符 3F
    private double slantRange; //斜距
    private String unit; //距离单位
    private double vertical; //垂直角
    private double level; //水平角
    private String jsUnit; //角度单位
    private double result; //水平距离
    private int backlight; //回光强度
    private int frame; //菱镜常数

    private int type; //type=1: 测距结果数据

    public RangerResult(byte[] result){
        try {
            String string = new String(result,"ASCII");
            String[] split = string.split("\\+");
            if (split.length >= 5) {
                id = split[0].getBytes()[0];
                String[] ms = split[1].split("m");
                slantRange = Double.parseDouble(ms[0]) / 1000.0;
                unit = "m";
                vertical = Double.parseDouble(ms[1]) / 10000.0;
                level = Double.parseDouble(split[2].replace("d", "")) / 10000.0;
                String[] ss = split[3].split("t");
                this.result = Double.parseDouble(ss[0]) / 1000.0;
                Log.e("TAG----","result="+this.result+"米");
                backlight = Integer.parseInt(ss[1]);
                frame = Integer.parseInt(split[5].substring(0, 2));
                Log.e("TAG----", string);
                type = 1;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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

    public double getResult() {
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
}
