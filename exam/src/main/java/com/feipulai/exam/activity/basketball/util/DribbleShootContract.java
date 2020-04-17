package com.feipulai.exam.activity.basketball.util;

/**
 * Created by pengjf on 2020/4/13.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface DribbleShootContract {
    interface View {
        void onSetConnect(int state);
        void onSettingResult(int result);
    }


    interface DribbleShootPresenter{
        void setWait();
        void setStart();
        void setStop();
        void setEnd();
    }
}
