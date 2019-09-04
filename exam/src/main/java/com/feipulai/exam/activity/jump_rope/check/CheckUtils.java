package com.feipulai.exam.activity.jump_rope.check;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.JumpDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by James on 2019/2/12 0012.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class CheckUtils {

    public static List<StuDevicePair> newPairs(int size) {
        List<StuDevicePair> pairs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            StuDevicePair pair = new StuDevicePair();
            BaseDeviceState state;
            switch (TestConfigs.sCurrentItem.getMachineCode()) {

                case ItemDefault.CODE_TS:
                    state = new JumpDeviceState();
                    break;

                case ItemDefault.CODE_YWQZ:
                case ItemDefault.CODE_YTXS:
                case ItemDefault.CODE_PQ:
                case ItemDefault.CODE_FWC:
                case ItemDefault.CODE_LQYQ:
                case ItemDefault.CODE_ZQYQ:
                    state = new BaseDeviceState();
                    break;
                default:
                    throw new IllegalArgumentException("machine code not supported");

            }
            state.setState(BaseDeviceState.STATE_DISCONNECT);
            state.setDeviceId(i + 1);
            pair.setBaseDevice(state);
            pairs.add(pair);
        }
        return pairs;
    }

    public static void stopUse(List<StuDevicePair> pairs, int position) {
        pairs.get(position).getBaseDevice().setState(BaseDeviceState.STATE_STOP_USE);
    }

    public static void resumeUse(List<StuDevicePair> pairs, int position) {
        pairs.get(position).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
    }

    public static void groupCheck(List<StuDevicePair> pairs) {
        // 分组测试,直接分配考生
        TestCache testCache = TestCache.getInstance();
        Map<Object, Object> map = TestConfigs.baseGroupMap;
        testCache.setGroup((Group) map.get("group"));
        List<BaseStuPair> pairList = (List<BaseStuPair>) map.get("basePairStu");
        int size = Math.min(pairList.size(), pairs.size());
        // 给手柄分配考生 并初始化 TestCache
        for (int i = 0; i < size; i++) {
            pairs.get(i).setStudent(pairList.get(i).getStudent());

            Student student = pairList.get(i).getStudent();
            testCache.getAllStudents().add(student);
            testCache.getResults().put(student, (List<RoundResult>) map.get(student));
            testCache.getTrackNoMap().put(student, pairList.get(i).getTrackNo());
        }
        for (int i = size; i < pairList.size(); i++) {
            Student student = pairList.get(i).getStudent();
            testCache.getAllStudents().add(student);
            testCache.getResults().put(student, (List<RoundResult>) map.get(student));
            testCache.getTrackNoMap().put(student, pairList.get(i).getTrackNo());
        }
        map.clear();
    }

    public static void groupCheck(List<StuDevicePair> pairs, int maxTestCount, int testPattern) {
        // 分组测试,直接分配考生
        TestCache testCache = TestCache.getInstance();
        Map<Object, Object> map = TestConfigs.baseGroupMap;
        testCache.setGroup((Group) map.get("group"));
        List<BaseStuPair> pairList = (List<BaseStuPair>) map.get("basePairStu");
        int size = Math.min(pairList.size(), pairs.size());
        int addPostion = 0;
        if (testPattern == TestConfigs.GROUP_PATTERN_SUCCESIVE) {//连续
            for (BaseStuPair stuPair : pairList) {
                //  查询学生成绩 当有成绩则添加数据跳过测试
                List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                        (stuPair.getStudent().getStudentCode(), testCache.getGroup().getId() + "");
                if ((roundResultList == null || roundResultList.size() == 0 || roundResultList.size() < maxTestCount)
                        && addPostion < size) {
                    pairs.get(addPostion).setStudent(stuPair.getStudent());
                    Student student = stuPair.getStudent();
                    testCache.getAllStudents().add(student);
                    testCache.getResults().put(student, (List<RoundResult>) map.get(student));
                    testCache.getTrackNoMap().put(student, stuPair.getTrackNo());
                    addPostion++;
                    if (addPostion == size) {
                        break;
                    }
                }
            }
        } else {
            for (int i = 0; i < maxTestCount; i++) {
                if (addPostion != 0) {
                    break;
                }
                for (BaseStuPair stuPair : pairList) {
                    //  查询学生成绩 当有成绩则添加数据跳过测试
                    List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                            (stuPair.getStudent().getStudentCode(), testCache.getGroup().getId() + "");
                    if ((roundResultList.size() < (i + 1))
                            && addPostion < size) {
                        pairs.get(addPostion).setStudent(stuPair.getStudent());
                        Student student = stuPair.getStudent();
                        testCache.getAllStudents().add(student);
                        testCache.getResults().put(student, (List<RoundResult>) map.get(student));
                        testCache.getTrackNoMap().put(student, stuPair.getTrackNo());
                        addPostion++;
                        if (addPostion == size) {
                            break;
                        }
                    }
                }
            }
        }
//        // 给手柄分配考生 并初始化 TestCache
//        for (int i = 0; i < size; i++) {
//            pairs.get(i).setStudent(pairList.get(i).getStudent());
//
//            Student student = pairList.get(i).getStudent();
//            testCache.getAllStudents().add(student);
//            testCache.getResults().put(student, (List<RoundResult>) map.get(student));
//            testCache.getTrackNoMap().put(student, pairList.get(i).getTrackNo());
//        }
        for (int i = size; i < pairList.size(); i++) {
            Student student = pairList.get(i).getStudent();
            testCache.getAllStudents().add(student);
            testCache.getResults().put(student, (List<RoundResult>) map.get(student));
            testCache.getTrackNoMap().put(student, pairList.get(i).getTrackNo());
        }
        map.clear();
    }

}
