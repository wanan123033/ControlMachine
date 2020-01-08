package com.feipulai.exam.activity.setting;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by zzs on  2019/12/31
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MonitoringBean implements Serializable {

    private String monitoringSerial;
    private String bindTime;
    private boolean isSelect ;//列表选择使用

    public MonitoringBean(String monitoringSerial, String bindTime, boolean isSelect) {
        this.monitoringSerial = monitoringSerial;
        this.bindTime = bindTime;
        this.isSelect = isSelect;
    }

    public String getMonitoringSerial() {
        return monitoringSerial;
    }

    public void setMonitoringSerial(String monitoringSerial) {
        this.monitoringSerial = monitoringSerial;
    }

    public String getBindTime() {
        return bindTime;
    }

    public void setBindTime(String bindTime) {
        this.bindTime = bindTime;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MonitoringBean) {
            MonitoringBean monitoringBean = (MonitoringBean) obj;
            if (TextUtils.equals(monitoringSerial, monitoringBean.getMonitoringSerial())) {
                return true;
            }
        }
        return super.equals(obj);
    }
}
