package com.feipulai.host.bean;

public class UserPhoto {
    public double getFaceSimilar() {
        return faceSimilar;
    }

    public void setFaceSimilar(double faceSimilar) {
        this.faceSimilar = faceSimilar;
    }

    public String getStudentcode() {
        return studentcode;
    }

    public void setStudentcode(String studentCode) {
        this.studentcode = studentCode;
    }

    private double faceSimilar;
    private String studentcode;
}
