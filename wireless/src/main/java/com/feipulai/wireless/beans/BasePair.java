package com.feipulai.wireless.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by pengjf on 2019/2/13.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class BasePair implements Parcelable {
    private int deviceId ;
    /** 1离线 2空闲 3测试 4结束*/
    private int state;
    private int power;
    private int count ;

    public int getPair() {
        return pair;
    }

    public void setPair(int pair) {
        this.pair = pair;
    }

    /**0 未配对  1配对*/
    private int pair ;
    public BasePair(int deviceId, int state, int power, int pair) {
        this.deviceId = deviceId;
        this.state = state;
        this.power = power;
        this.pair = pair;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.deviceId);
        dest.writeInt(this.state);
        dest.writeInt(this.power);
        dest.writeInt(this.count);
        dest.writeInt(this.pair);
    }

    protected BasePair(Parcel in) {
        this.deviceId = in.readInt();
        this.state = in.readInt();
        this.power = in.readInt();
        this.count = in.readInt();
        this.pair = in.readInt();
    }

    public static final Parcelable.Creator<BasePair> CREATOR = new Parcelable.Creator<BasePair>() {
        @Override
        public BasePair createFromParcel(Parcel source) {
            return new BasePair(source);
        }

        @Override
        public BasePair[] newArray(int size) {
            return new BasePair[size];
        }
    };
}
