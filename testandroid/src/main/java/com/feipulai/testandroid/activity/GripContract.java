package com.feipulai.testandroid.activity;

import com.feipulai.testandroid.base.BaseView;

/**
 * Created by pengjf on 2020/1/15.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface GripContract {

    interface View extends BaseView{
        @Override
        void showLoading();

        @Override
        void hideLoading();

    }

    interface Presenter {
        void sendEmpty();
        void deviceMatch();
        void test();
        void verify();
    }


}
