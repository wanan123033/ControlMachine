package com.feipulai.exam.activity.sargent_jump.more_device;

import android.os.Bundle;

import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.ButterKnife;

public abstract class GroupMoreActivity extends BaseCheckActivity {

    private List<BaseStuPair> stuPairsList;
    private Group group;
    /**
     * 是否停止测试
     */
    private boolean isStop = true;
    /**
     * 当前测试次数位
     */
    private int roundNo = 1;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_more;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        PrinterManager.getInstance().init();
        group = (Group) TestConfigs.baseGroupMap.get("group");
    }

    @Override
    public void onCheckIn(Student student) {
        Logger.i("onCheckIn====>" + student.toString());
        if (student == null) {
            toastSpeak("该考生不存在");
            return;
        }
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            toastSpeak("无此项目");
            return;
        }

        //是否开启身份验证
        if (SettingHelper.getSystemSetting().isIdentityMark() && stuPairsList.size() > 0) {
            //考生分组测试的成绩
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
            //学生在分组中是否有进行检入
            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, student.getStudentCode());
            if ((roundResultList.size() == 0 || roundResultList.size() < setTestCount()) && groupItem != null) {
                isStop = false;
                roundNo = roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;
                gotoTest(student);
                groupItem.setIdentityMark(1);
                DBManager.getInstance().updateStudentGroupItem(groupItem);
            } else if (groupItem == null) {//没报名
                toastSpeak(student.getSpeakStuName() + "考生没有在选择的分组内，无法测试",
                        student.getStudentName() + "考生没有在选择的分组内，无法测试");
            } else if (roundResultList.size() > 0) {
                toastSpeak(student.getSpeakStuName() + "考生已测试完成",
                        student.getStudentName() + "考生已测试完成");
            }
        }
    }

    /**
     * 设置项目测试次数
     */
    public abstract int setTestCount();

    /**
     * 定位考生测试
     *
     * @param student
     */
    private void gotoTest(Student student) {

    }
}
