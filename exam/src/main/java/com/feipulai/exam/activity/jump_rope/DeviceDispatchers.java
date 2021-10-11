package com.feipulai.exam.activity.jump_rope;

import android.util.Log;

import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.StudentCache;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;

import java.util.List;

public class DeviceDispatchers {
    private int testNo;
    private List<Student> students;
    private int index;

    public DeviceDispatchers(int testNo){
        this.testNo = testNo;
        students = StudentCache.getStudentCaChe().getAllStudent();
    }

    public boolean dispatchDevice(List<StuDevicePair> pairs, int groupMode){
        // 清空成绩和状态
        for(StuDevicePair pair : pairs){
            pair.setDeviceResult(null);
            if(pair.getBaseDevice().getState() == BaseDeviceState.STATE_STOP_USE){
                pair.getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
            }
        }

        int testPattern = SettingHelper.getSystemSetting().getTestPattern();
        if(testPattern == SystemSetting.PERSON_PATTERN){
            return dispatchDevicePersonal(pairs);
        }
        if (pairs.size() >= 1)
            index = students.indexOf(pairs.get(pairs.size() - 1).getStudent());
        else
            index = -1;
        if (index == students.size() - 1){
            index = -1;
        }
        boolean isFlag = false;
        if (groupMode == TestConfigs.GROUP_PATTERN_LOOP){  //循环模式
            isFlag =  dispatchDeviceLoop(pairs);
        }else {
            isFlag = dispatchDeviceSuccesive(pairs);
        }
        checkStuRoundNoInfo(pairs);
        return isFlag;
    }

    /**
     * 检查学生的轮次信息是否一致
     * @param pairs
     */
    private void checkStuRoundNoInfo(List<StuDevicePair> pairs) {
        for (int i = 0 ; i < pairs.size() ; i++){
            Student student = pairs.get(i).getStudent();
            if (student == null){
                for (int j = i ; j < pairs.size() ; j++){
                    pairs.get(j).setStudent(null);
                }
                if (i >= 1) {
                    index = students.indexOf(pairs.get(i - 1).getStudent());
                    if (index == students.size() - 1) {
                        index = -1;
                    }
                }
                break;
            }
        }

    }

    private boolean dispatchDeviceLoop(List<StuDevicePair> pairs) {
        for (int i = 0 ; i < pairs.size() ; i++){
            StuDevicePair pair = pairs.get(i);
            if (index == students.size()){
                index = -1;
            }
            Student student = nextStudent();
            pair.setStudent(student);
            if (student == null) {
                continue;
            }
        }
        for (int i = 0 ; i < pairs.size() ; i++){
            if (pairs.get(i).getStudent() != null){
                return true;
            }
        }
        return false;
    }

    private Student nextStudent() {
        index++;
        if (index >= students.size()) {
            return null;
        }

        Student student = students.get(index);
        Log.e("TAG---79",student.toString());
        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),student.getStudentCode());

        if (studentItem != null && studentItem.getExamType() == 2){
            List<RoundResult> results = TestCache.getInstance().getResults().get(student);
            if (results == null || results.isEmpty()){
                return student;
            }else {
                return nextStudent();
            }
        }else {
            List<RoundResult> results = TestCache.getInstance().getResults().get(student);
            if (results == null || results.size() < testNo){
                return student;
            }
        }
        return null;
    }


    private boolean dispatchDeviceSuccesive(List<StuDevicePair> pairs) {
        for (int i = 0 ; i < pairs.size() ; i++){
            Student student = pairs.get(i).getStudent();
            if (student == null){
                continue;
            }
            StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),student.getStudentCode());
            if (studentItem != null && studentItem.getExamType() == 2){
                List<RoundResult> results = TestCache.getInstance().getResults().get(student);
                if (results == null || results.isEmpty()){
                    pairs.get(i).setStudent(student);
                }else {
                    pairs.get(i).setStudent(nextStudent());
                }
            }else {
                List<RoundResult> results = TestCache.getInstance().getResults().get(student);
                if (results == null || results.size() < testNo){
                    pairs.get(i).setStudent(student);
                }else {
                    pairs.get(i).setStudent(nextStudent());
                }
            }
        }
        for (int i = 0 ; i < pairs.size() ; i++){
            if (pairs.get(i).getStudent() != null){
                return true;
            }
        }
        return false;
    }

    private boolean dispatchDevicePersonal(List<StuDevicePair> pairs){
        boolean needAnotherTest = false;
        // 清空成绩
        // 手柄号不变,将已经测试完成的考生移除掉
        for(StuDevicePair pair : pairs){
            Student student = pair.getStudent();
            if(student == null){
                continue;
            }
            // 有测试次数没完的,继续测试
            List<RoundResult> results = TestCache.getInstance().getResults().get(student);
            if(results == null || results.size() < testNo){
                needAnotherTest = true;
            }else{
                pair.setStudent(null);
            }
        }
        return needAnotherTest;
    }
}
