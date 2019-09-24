package com.feipulai.host.activity.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.feipulai.host.config.BaseEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;

/**
 * Fragment基础类
 */

@SuppressWarnings("WeakerAccess")
public abstract class BaseFragment extends Fragment {
    protected Context mContext;
    protected View mRoot;
    protected Bundle mBundle;
    protected LayoutInflater mInflater;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
        mBundle = getArguments();
        initBundle(mBundle);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null)
                parent.removeView(mRoot);
        } else {
            mRoot = inflater.inflate(getLayoutId(), container, false);
            mInflater = inflater;
            // Do something
            onBindViewBefore(mRoot);
            // Bind view
            ButterKnife.bind(this, mRoot);
            // Get savedInstanceState
            if (savedInstanceState != null)
                onRestartInstance(savedInstanceState);
            // Init
            initWidget(mRoot);
            initData();
            registerEvent(mRoot);
        }
        return mRoot;
    }

    protected void onBindViewBefore(View root) {
        // ...
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mBundle = null;
    }

    protected void initWidget(View root) {

    }
    protected <T extends View> T findView(int viewId) {
        return (T) mRoot.findViewById(viewId);
    }
    protected abstract int getLayoutId();

    protected void initBundle(Bundle bundle) {

    }

    protected void registerEvent(View root) {

    }

    protected abstract void initData();

    protected void onRestartInstance(Bundle bundle) {

    }
    @Subscribe
    public void onEvent(BaseEvent event) {

    }

    @Subscribe
    public void onEventMainThread(BaseEvent event) {

    }

    @Subscribe
    public void onEventBackgroundThread(BaseEvent event) {

    }

    @Subscribe
    public void onEventAsync(BaseEvent event) {

    }
}
