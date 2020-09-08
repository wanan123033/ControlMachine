package com.feipulai.host.bean;

public class UserPhoto {
    public double getFaceSimilar() {
        return faceSimilar;
    }

    public void setFaceSimilar(double faceSimilar) {
        this.faceSimilar = faceSimilar;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    private double faceSimilar;
    private String studentCode;
}
