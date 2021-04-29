package com.feipulai.testandroid.base;

/**
 * Created by pengjf on 2020/1/15.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasePresenter<V extends BaseView> {
    protected V mView;


    /**
     * 绑定view，一般在初始化中调用该方法
     *
     * @param view view
     */
    public void attachView(V view) {
        this.mView = view;
    }

    /**
     * 解除绑定view，一般在onDestroy中调用
     */

    public void detachView() {
        this.mView = null;
    }

    /**
     * View是否绑定
     *
     * @return
     */
    public boolean isViewAttached() {
        return mView != null;
    }


}
