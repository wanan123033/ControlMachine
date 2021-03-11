package com.feipulai.exam.activity.situp.newSitUp;

import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;

import java.util.List;

public interface NewSitUpPairContract{
    interface Presenter {
        void start();
        void changeFocusPosition(int position,int device);

        void changeAutoPair(boolean isAutoPair);

        void stopPair();

        void saveSettings();
    }

    interface View {
        void initView(boolean isAutoPair, List<DeviceCollect> stuDevicePairs);

        void updateSpecificItem(int focusPosition,int device);

        void select(int position,int device);

        void showToast(String msg);
    }
}
