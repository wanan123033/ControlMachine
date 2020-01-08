package com.feipulai.exam.activity.jump_rope.base.check;

import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;

import java.util.List;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface RadioCheckContract {

    interface Presenter extends IndividualCheckFragment.OnIndividualCheckInListener {
        void start();

        void changeBadDevice();

        void stopUse();

        void resumeUse();

        void deleteStudent();

        void deleteAll();

        void setFocusPosition(int position);

        void cancelChangeBad();

        void startTest();

        int stateOfPosition(int position);

        void settingChanged();

        void resetLED();

        void resumeGetStateAndDisplay();

        void pauseGetStateAndDisplay();

        void saveSetting();

        void finishGetStateAndDisplay();

        void refreshEveryThing();

        void showStuInfo(int position);


    }

    interface View<Setting> {
        void initView(SystemSetting systemSetting, Setting setting, List<StuDevicePair> pairs);

        void updateSpecificItem(int index);

        void changeBadSuccess();

        void select(int position);

        void showStuInfo(Student student, List<RoundResult> results);

        void showChangeBadDialog();

        void showToast(String msg);

        void updateAllItems();

        void startTest();

        void refreshPairs(List<StuDevicePair> pairs);

        void showLowBatteryStartDialog();

        void showConstraintStartDialog(boolean contaisLowBattery);
    }

}
