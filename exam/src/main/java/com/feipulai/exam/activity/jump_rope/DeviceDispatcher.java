package com.feipulai.exam.activity.jump_rope;

import android.util.Log;

import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;

import java.util.ArrayList;
import java.util.List;

import static org.greenrobot.eventbus.EventBus.TAG;

/**
 * Created by James on 2019/2/13 0013.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class DeviceDispatcher{

	private int index = -1;
	private List<Integer> updateIndex;// 分配的新考生的索引
	private final int testNo;

	public DeviceDispatcher(int testNo){
		this.testNo = testNo;
	}

	/**
	 * 依据考生信息和当前的考生设备配对信息,继续分配考生
	 *
	 * @param pairs       考生设备配对信息
	 * @param groupMode   分组模式,循环或者连续
	 * @return 返回设备是否分配成功,也即是否还需要继续测试
	 */
	public boolean dispatchDevice(List<StuDevicePair> pairs,int groupMode){
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

		// 没有分配过
		if(index == -1){
			index = pairs.size();
		}
		// TODO: 2019/2/13 0013 14:20 分组模式如果需要知道更新的考生,需要通过EventBus将updateIndex发送出去
		if(groupMode == TestConfigs.GROUP_PATTERN_LOOP){
			return dispatchDeviceLoop(pairs);
		}
		return dispatchDeviceSuccesive(pairs);
	}

	// 个人模式下分配手柄,只需要将测试完成的考生移除掉即可
	private boolean dispatchDevicePersonal(List<StuDevicePair> pairs){
		boolean needAnotherTest = false;
		// 清空成绩
		// 手柄号不变,将已经测试完成的考生移除掉
		for(StuDevicePair pair : pairs){
			// clearResult(pair);
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

	private boolean dispatchDeviceLoop(List<StuDevicePair> pairs){
		updateIndex = new ArrayList<>(pairs.size());
		boolean hasMore = true;

		List<Student> dispatchStudents = new ArrayList<>(pairs.size());
		int oldIndex = index;
		Student student = nextTestStudent(dispatchStudents);
		index = student == null ? 0/*没有更多考生了,从头开始找*/ : oldIndex;

		for(int i = 0;i < pairs.size();i++){
			StuDevicePair pair = pairs.get(i);
			// clearResult(pair);

			if (hasMore){
				student = nextTestStudent(dispatchStudents);
				if (student != null) {
					pair.setStudent(student);
					updateIndex.add(i);
					dispatchStudents.add(student);
					continue;
				} else {
					hasMore = false;
				}
			}
			pair.setStudent(null);
		}
		return dispatchStudents.size() > 0;
	}

	private boolean dispatchDeviceSuccesive(List<StuDevicePair> pairs){
		updateIndex = new ArrayList<>(pairs.size());

		List<Student> students = TestCache.getInstance().getAllStudents();
		if (index == students.size()){
			index = 0;
		}

		List<Student> dispatchStudents = new ArrayList<>(pairs.size());

		boolean hasMore = true;
		boolean needProceed = false;
		for (StuDevicePair pair: pairs) {
			// clearResult(pair);
			Student student = pair.getStudent();
			if(student != null){
				// 有测试次数没完的,继续测试
				List<RoundResult> results = TestCache.getInstance().getResults().get(student);
				Log.i(TAG, "testNo:" + testNo);
				if(results == null || results.size() < testNo){
					dispatchStudents.add(student);
					continue;
				}else{
                    pair.setStudent(null);
                }
			}
            needProceed = true;
		}
		// 当前所有人都需要进行测试
        if (!needProceed){
            return true;
        }
		for(int i = 0;i < pairs.size();i++){
			StuDevicePair pair = pairs.get(i);
			if (pair.getStudent() != null){
			    continue;
            }
			if(hasMore){
				Student stu = nextTestStudent(dispatchStudents);
                if (stu != null) {
                    pair.setStudent(stu);
                    updateIndex.add(i);
                    dispatchStudents.add(stu);
				}else{
					hasMore = false;
				}
			}
		}
		return dispatchStudents.size() > 0;
	}

	private Student nextTestStudent(List<Student> dispatchStudents){
		List<Student> students = TestCache.getInstance().getAllStudents();
		Student result = null;

		for (; index < students.size(); index ++) {
			Student student = students.get(index);
			List<RoundResult> results = TestCache.getInstance().getResults().get(student);
			// Log.d(TAG, "index : " + index);
			// Log.d(TAG, "students.size():" + students.size());
			// 找到了考生
			if ((results == null || results.size() < testNo) && !dispatchStudents.contains(student)) {
				result = student;
				index ++;
				break;
			}
		}
		return result;
	}

}
