package com.feipulai.host.entity;

/**
 * Created by pengjf on 2018/11/23.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class Tuple {
    public int getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(int machineCode) {
        this.machineCode = machineCode;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }
    private String itemCode;
    private int machineCode;
    private String machineName;

    public Tuple(int machineCode, String machineName) {
        this.machineCode = machineCode;
        this.machineName = machineName;
    }

    public Tuple(String itemCode, String machineName) {
        this.itemCode = itemCode;
        this.machineName = machineName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }
}
