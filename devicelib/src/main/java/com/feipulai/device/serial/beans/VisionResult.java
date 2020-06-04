package com.feipulai.device.serial.beans;

public class VisionResult {
    private int leftVision;
    private int rightVision;
    public VisionResult(byte[] arr){
        if (arr[3] == 0x34){  //视力结果
            leftVision = arr[5];
            rightVision = arr[4];
        }
    }

    public int getLeftVision() {
        return leftVision;
    }

    public int getRightVision() {
        return rightVision;
    }
}
