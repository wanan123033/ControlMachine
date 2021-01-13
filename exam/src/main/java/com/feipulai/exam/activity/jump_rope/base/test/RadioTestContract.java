package com.feipulai.exam.activity.jump_rope.base.test;

import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;

import java.util.List;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface RadioTestContract {

    interface Presenter {

        void start();

        void stopUse();

        void resumeUse();

        void startTest();

        void restartTest();

        boolean checkFinalResults();

        void saveResults();

        void changeBadDevice();

        void quitTest();

        void confirmResults();

        void finishTest();

        int stateOfPosition(int position);

        void setFocusPosition(int position);

        void cancelChangeBad();

        void stopNow();

        void dispatchDevices();

        void setInputResult(int result, int state);
    }

    interface View<Setting> {

        void initView(List<StuDevicePair> pairs, Setting setting);

        void updateSpecificItem(int index);

        void tickInUI(String text);

        void changeBadSuccess();

        void updateStates();

        void enableStopRestartTest(boolean enable);

        void enableStartTest(boolean enable);

        void enableConfirmResults(boolean enable);

        void showWaitFinalResultDialog(boolean showDialog);

        void showToast(String msg);

        void showForConfirmResults();

        void setViewForStart();

        void quitTest();

        void finishTest();

        void showChangeBadDialog();

        void enableStopUse(boolean enable);

        void showDisconnectForConfirmResults();

        void enableFinishTest(boolean enable);

        void enableChangeBad(boolean enable);
    }

}
