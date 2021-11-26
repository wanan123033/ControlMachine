package com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair;

public class RadioContract {
    public interface Presenter {
        void start(int deviceId, int point);
        void changeFocusPosition(int position, int point);

        void changeAutoPair(boolean isAutoPair);

        void stopPair();

        void saveSettings();
    }

    public interface View {

        void updateSpecificItem(int focusPosition, int point);

        void select(int position, int point);

        void showToast(String msg);
    }
}
