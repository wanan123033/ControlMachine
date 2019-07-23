package com.feipulai.exam.activity.base;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.common.view.baseToolbar.StatusBarUtil;
import com.feipulai.exam.R;

import butterknife.ButterKnife;

/**
 * @author ww
 * @time 2019/7/23 9:18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class MiddleBaseTitleActivity extends BaseActivity {

    /**
     * 标题
     */
    public BaseToolbar mBaseToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int LayoutID = setLayoutResID();
        initBundle(getIntent().getExtras());
        if (NO_LAYOUT_CONTENT != LayoutID) {
//            initContentView();
            setTransparent(this);
            setContentView(setLayoutResID());
            ButterKnife.bind(this);
            initViews();
//            setSystemBarColor();

        }
        initData();

    }

    private void setTransparent(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //注意要清除 FLAG_TRANSLUCENT_STATUS flag
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.colorPrimary));
        }
    }



    /**
     * 设置状态栏颜色
     */
    protected void setSystemBarColor() {
        //        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary));
    }

    private void initContentView() {
        /*** 这里可以对Toolbar进行统一的预设置 */
        BaseToolbar.Builder builder = new BaseToolbar.Builder(this).setStatusBarColor(ContextCompat.getColor(this, R
                .color.colorPrimary))//统一设置颜色
                .setBackButton(R.mipmap.icon_white_goback)//统一设置返回键
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary)).setTitleTextColor
                        (ContextCompat.getColor(this, R.color.white)).setSubTextColor(ContextCompat.getColor(this, R
                        .color.white));
        StatusBarUtil.setImmersiveTransparentStatusBar(this);//设置沉浸式透明状态栏 配合使用
        builder = setToolbar(builder);
        if (builder != null) {
            mBaseToolbar = builder.build();
            getToolbar().setStatusBarTransparent();
        }

        if (mBaseToolbar != null) {
            LinearLayout layout = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            layout.setLayoutParams(params);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(mBaseToolbar);
            View mView = getLayoutInflater().inflate(setLayoutResID(), layout, false);
            layout.addView(mView);
            setContentView(layout);
        } else {
            setContentView(setLayoutResID());

        }
    }


    /**
     * 设置界面布局
     * <br/> author wzl
     * <br/> date 2017/11/15 下午7:10
     */
    protected abstract int setLayoutResID();

    /**
     * 根据id查找控件
     * <br/> author wzl
     * <br/> date 2017/11/15 下午8:41
     */
    protected void initViews() {
    }

    /**
     * 初始化
     * <br/> author wzl
     * <br/> date 2017/11/15 下午7:29
     */
    protected abstract void initData();


    /**
     * 不需要toolbar的可以不用管
     *
     * @return
     */
    @Nullable
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return null;
    }


    /**
     * 获取标题
     * <h3>Version</h3> 1.0
     * <h3>CreateAuthor</h3> zzs
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3>
     */
    public BaseToolbar getToolbar() {
        return mBaseToolbar;
    }

    /**
     * 显示标题
     * <h3>Version</h3> 1.0
     * <h3>CreateAuthor</h3> zzs
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3>
     */
    public void showToolbar() {
        if (mBaseToolbar != null) mBaseToolbar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏标题
     * <h3>Version</h3> 1.0
     * <h3>CreateAuthor</h3> zzs
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3>
     */
    public void hideToolbar() {
        if (mBaseToolbar != null) mBaseToolbar.setVisibility(View.GONE);
    }

    /**
     * 获取传递的Intent数据
     */
    protected void initBundle(Bundle bundle) {

    }

    /**
     * 设置全屏
     *
     * @param isShowStatusBar     是否显示状态栏
     * @param isShowNavigationBar 是否显示底部操作栏
     */
    public void setFullscreen(boolean isShowStatusBar, boolean isShowNavigationBar) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        if (!isShowStatusBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (!isShowNavigationBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }


    /**
     * 无布局内容标记
     */
    public static final int NO_LAYOUT_CONTENT = 0;

}
