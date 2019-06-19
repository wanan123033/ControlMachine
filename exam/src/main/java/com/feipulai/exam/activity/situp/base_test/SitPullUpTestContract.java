package com.feipulai.exam.activity.situp.base_test;

import com.feipulai.exam.activity.jump_rope.base.test.RadioTestContract;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;

import java.util.List;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface SitPullUpTestContract {

    interface Presenter extends RadioTestContract.Presenter {
        void punish();

        void penalize(int number);
    }

    interface View<Setting> extends RadioTestContract.View<Setting> {

        void enablePenalize(boolean enable);

        void showPenalizeDialog(int max);

    }

}
