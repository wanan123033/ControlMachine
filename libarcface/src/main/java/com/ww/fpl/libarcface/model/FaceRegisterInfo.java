package com.ww.fpl.libarcface.model;

public class FaceRegisterInfo {
    private byte[] featureData;
    private String name;

    public FaceRegisterInfo(byte[] faceFeature, String name) {
        this.featureData = faceFeature;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getFeatureData() {
        return featureData;
    }

    public void setFeatureData(byte[] featureData) {
        this.featureData = featureData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FaceRegisterInfo) {
            FaceRegisterInfo faceRegisterInfo = (FaceRegisterInfo) obj;
            if (faceRegisterInfo.name.equals(name)) {
                return true;
            }
        }
        return super.equals(obj);
    }
}
