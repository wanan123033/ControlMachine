package com.feipulai.host.activity.jump_rope.bean;

import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class TestCache{
	
	// 正在测试的学生和设备配对信息
	private List<StuDevicePair> testingPairs;
	Map<Student, RoundResult> saveResults;
	Map<Student, RoundResult> bestResults;
	
	private static final TestCache instance = new TestCache();
	
	private TestCache(){}
	
	public static TestCache getInstance(){
		return instance;
	}
	
	public void init(){
		testingPairs = new ArrayList<>();
		bestResults = null;
		saveResults = null;
	}
	
	public void clear(){
		testingPairs = null;
		bestResults = null;
		saveResults = null;
	}
	
	public List<StuDevicePair> getTestingPairs(){
		return testingPairs;
	}
	
	public void setTestingPairs(List<StuDevicePair> testingPairs){
		this.testingPairs = testingPairs;
	}
	
	public Map<Student, RoundResult> getSaveResults() {
		return saveResults;
	}
	
	public void setSaveResults(Map<Student, RoundResult> saveResults) {
		this.saveResults = saveResults;
	}
	
	public Map<Student, RoundResult> getBestResults() {
		return bestResults;
	}
	
	public void setBestResults(Map<Student, RoundResult> bestResults) {
		this.bestResults = bestResults;
	}
	
}
