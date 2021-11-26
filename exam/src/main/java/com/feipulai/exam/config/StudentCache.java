package com.feipulai.exam.config;

import com.feipulai.exam.entity.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentCache {
    private static StudentCache cache = new StudentCache();
    private List<Student> students;
    private StudentCache(){
        students = new ArrayList<>();
    }
    public static synchronized StudentCache getStudentCaChe(){
        return cache;
    }
    public void addStudent(Student student){
        if (!students.contains(student)){
            students.add(student);
        }
    }
    public void clear(){
        if (students != null){
            students.clear();
        }
    }
    public List<Student> getAllStudent(){
        return cache.students;
    }
}
