package com.feipulai.exam.activity.jump_rope.bean;

import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class TestCache {

    private Group group;
    // 正在测试的学生和设备配对信息
    private volatile List<StuDevicePair> testingPairs;
    // 所有待测考生
    private List<Student> allStudents;
    // 所有考生及对应的成绩
    private Map<Student, List<RoundResult>> results;
    /**
     * 指定学生的当前测试次数,用于保存成绩时使用
     */
    private Map<Student, Integer> testNoMap;
    /**
     * 指定学生的当前测试次数的第几轮,用于保存成绩时使用（重测指定轮次，之前按成绩按成绩条数判断已不适用）
     */
    private Map<Student, Integer> roundNoMap;

    private Map<Student, StudentItem> studentItemMap;

    private Map<Student, Integer> trackNoMap;

    private static final TestCache instance = new TestCache();

    private TestCache() {
    }

    public static TestCache getInstance() {
        return instance;
    }

    public void init() {
        if (testingPairs==null){
            testingPairs = new ArrayList<>();
            allStudents = new ArrayList<>();
            results = new IdentityHashMap<>();
            group = null;
            testNoMap = new IdentityHashMap<>();
            roundNoMap = new IdentityHashMap<>();
            studentItemMap = new IdentityHashMap<>();
            trackNoMap = new IdentityHashMap<>();
        }

    }

    public void clear() {
        testingPairs = null;
        allStudents = null;
        results = null;
        group = null;
        testNoMap = null;
        studentItemMap = null;
        trackNoMap = null;
    }

    public Map<Student, Integer> getTestNoMap() {
        if (testNoMap == null){
            testNoMap = new IdentityHashMap<>();
        }
        return testNoMap;
    }

    public Map<Student, StudentItem> getStudentItemMap() {
        if (studentItemMap == null){
            studentItemMap = new IdentityHashMap<>();
        }
        return studentItemMap;
    }

    public Map<Student, Integer> getTrackNoMap() {
        if (trackNoMap == null){
            trackNoMap = new IdentityHashMap<>();
        }
        return trackNoMap;
    }

    public void setTrackNoMap(Map<Student, Integer> trackNoMap) {
        this.trackNoMap = trackNoMap;
    }

    public void setStudentItemMap(Map<Student, StudentItem> studentItemMap) {
        this.studentItemMap = studentItemMap;
    }

    public void setTestNoMap(Map<Student, Integer> testNoMap) {
        this.testNoMap = testNoMap;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<StuDevicePair> getTestingPairs() {
        return testingPairs;
    }

    public void setTestingPairs(List<StuDevicePair> testingPairs) {
        this.testingPairs = testingPairs;
    }

    public List<Student> getAllStudents() {
        if (allStudents == null){
            allStudents = new ArrayList<>();
        }
        return allStudents;
    }

    public void setAllStudents(List<Student> allStudents) {
        this.allStudents = allStudents;
    }

    public Map<Student, List<RoundResult>> getResults() {
        if (results == null) {
            results = new IdentityHashMap<>();
        }
        return results;
    }

    public void setResults(Map<Student, List<RoundResult>> results) {
        this.results = results;
    }
}
