package com.feipulai.exam.activity.base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import butterknife.ButterKnife;

/**
 * created by ww on 2019/6/24.
 */
public abstract class BaseFragmentActivity extends FragmentActivity {
    protected BaseFragmentActivity act;
    protected final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        act = this;
        setContentView(getLayoutID());
        ButterKnife.bind(this);
        initView();
        initData();
        initListener();
    }

    @LayoutRes
    protected abstract int getLayoutID();

    protected abstract void initListener();

    protected abstract void initView();

    protected abstract void initData();

    @SuppressWarnings("unchecked")
    protected <E> E f(int id) {
        return (E) findViewById(id);
    }
}
