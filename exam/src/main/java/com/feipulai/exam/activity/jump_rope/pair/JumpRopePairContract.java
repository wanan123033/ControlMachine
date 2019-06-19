package com.feipulai.exam.activity.jump_rope.pair;

import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;

import java.util.List;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public interface JumpRopePairContract {

    interface Presenter {
        void start();
        void changeFocusPosition(int position);

        void changeDeviceGroup(int deviceGroup);

        void changeAutoPair(boolean isAutoPair);

        void resumePair();

        void pausePair();

        void stopPair();

        void saveSettings();
    }

    interface View<Presenter> {

        void initView(JumpRopeSetting setting, List<StuDevicePair> stuDevicePairs);

        void updateSpecificItem(int focusPosition);

        void updateAllItems();

        void select(int position);
    }

}
