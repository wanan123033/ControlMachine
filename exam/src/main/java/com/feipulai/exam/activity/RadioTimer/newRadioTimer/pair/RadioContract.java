package com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair;

public class RadioContract {
    public interface Presenter {
        void start(int deviceId);
        void changeFocusPosition(int position);

        void changeAutoPair(boolean isAutoPair);

        void stopPair();

        void saveSettings();
    }

    interface View {

        void updateSpecificItem(int focusPosition);

        void select(int position);

        void showToast(String msg);
    }
}
