package com.feipulai.testandroid.base;


/**
 * Created by pengjf on 2020/1/15.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class BaseMvpActivity<T extends BasePresenter> extends BaseActivity implements BaseView{
    public T presenter;
    @Override
    protected void onDestroy() {
        if (presenter!= null){
            presenter.detachView();
        }
        super.onDestroy();
    }
}
