package com.feipulai.host.activity.pullup.test;


import com.feipulai.host.activity.jump_rope.base.test.RadioTestContract;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface PullUpTestContract {



    interface View<Setting> extends RadioTestContract.View<Setting> {

        void showToast(String msg);
    }

}
