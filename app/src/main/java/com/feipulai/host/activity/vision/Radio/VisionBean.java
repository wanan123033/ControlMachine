package com.feipulai.host.activity.vision.Radio;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zzs on  2020/9/15
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VisionBean implements Serializable {

    private double distance;//距离长度 5 ，2.5 ，1
    private List<VisionData> visions;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public List<VisionData> getVisions() {
        return visions;
    }

    public void setVisions(List<VisionData> visions) {
        this.visions = visions;
    }

    @Override
    public String toString() {
        return "VisionBean{" +
                "distance=" + distance +
                ", visions=" + visions +
                '}';
    }

    public static class VisionData implements Serializable {

        @SerializedName("LogMAR_5")
        private double logMAR_5;
        @SerializedName("E_DP")
        private int eDP;
        @SerializedName("LogMAR_Decimals")
        private double logMAR_Decimals;
        @SerializedName("Error_Count")
        private int errorCount;

        public double getLogMAR_5() {
            return logMAR_5;
        }

        public void setLogMAR_5(double logMAR_5) {
            this.logMAR_5 = logMAR_5;
        }

        public int geteDP() {
            return eDP;
        }

        public void seteDP(int eDP) {
            this.eDP = eDP;
        }

        public double getLogMAR_Decimals() {
            return logMAR_Decimals;
        }

        public void setLogMAR_Decimals(double logMAR_Decimals) {
            this.logMAR_Decimals = logMAR_Decimals;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(int errorCount) {
            this.errorCount = errorCount;
        }

        @Override
        public String toString() {
            return "VisionData{" +
                    "logMAR_5=" + logMAR_5 +
                    ", eDP=" + eDP +
                    ", logMAR_Decimals=" + logMAR_Decimals +
                    ", errorCount=" + errorCount +
                    '}';
        }
    }

}
