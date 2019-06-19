package com.feipulai.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import com.feipulai.common.R;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.header.WaterDropHeader;
import com.scwang.smartrefresh.header.WaveSwipeHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;

/**
 * 上下拉刷新布局控件
 * Created by zzs on 2018/3/16.
 */

public class PullToRefreshView extends SmartRefreshLayout {
    private Context mContext;

    public PullToRefreshView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initAttri(context, attrs, 0);
        initHead(context, attrs, 0);
        init();
    }

    public PullToRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttri(context, attrs, defStyleAttr);
        initHead(context, attrs, defStyleAttr);
        init();

    }

    private void initAttri(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefreshView, defStyleAttr, 0);
        if (a.hasValue(R.styleable.PullToRefreshView_modes)) {
            mode = a.getInteger(R.styleable.PullToRefreshView_modes, MODE_BOTH);
        }

        if (mode == MODE_PULL_DOWN_TO_REFRESH) {
            setEnableRefresh(true);
            setEnableLoadmore(false);
        }
        if (mode == MODE_PULL_UP_TO_REFRESH) {
            setEnableRefresh(false);
            setEnableLoadmore(true);
        }
        if (mode == MODE_BOTH) {
            setEnableRefresh(true);
            setEnableLoadmore(true);
        }
        if (mode == MODE_ALL_NONE) {
            setEnableRefresh(false);
            setEnableLoadmore(false);
            setEnablePureScrollMode(true);
        }
        setEnableAutoLoadmore(true);//是否启用列表惯性滑动到底部时自动加载更多
    }

    /**
     * 设置下拉样式
     * <h3>Version</h3> 1.0
     * <h3>CreateTime</h3> 2018/3/16,20:40
     * <h3>UpdateTime</h3> 2018/3/16,20:40
     * <h3>CreateAuthor</h3> zzs
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3>
     */
    private void initHead(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefreshView, defStyleAttr, 0);
        if (a.hasValue(R.styleable.PullToRefreshView_headstyle)) {
            headstyle = a.getInteger(R.styleable.PullToRefreshView_headstyle, HEADSTYLE_COPYIOS);
        }

        if (headstyle == HEADSTYLE_DEFAULT) {
            setRefreshHeader(new WaveSwipeHeader(mContext));//设置下拉样式 水滴
            setEnableHeaderTranslationContent(false);//是否下拉Header的时候向下平移列表或者内容
            setEnableFooterTranslationContent(true);
        }
        if (headstyle == HEADSTYLE_CLASSICS) {
            setRefreshHeader(new MaterialHeader(mContext));//设置下拉样式 默认
            setEnableHeaderTranslationContent(true);//是否下拉Header的时候向下平移列表或者内容
            setEnableFooterTranslationContent(true);
        }
        if (headstyle == HEADSTYLE_COPYIOS) {
            setRefreshHeader(new WaterDropHeader(mContext));//设置下拉样式 ios
            setEnableHeaderTranslationContent(true);//是否下拉Header的时候向下平移列表或者内容
            setEnableFooterTranslationContent(true);
        }

    }

    private void init() {
        setDisableContentWhenRefresh(true);//是否在刷新的时候禁止列表的操作
        setDisableContentWhenLoading(true);//是否在加载的时候禁止列表的操作
        setEnableAutoLoadmore(true);

        setRefreshFooter(new ClassicsFooter(mContext)); //设置上拉样式
        setPrimaryColorsId(R.color.colorAccent, android.R.color.white);//水滴颜色与进度箭头颜色
        Log.d("refresh", "mode--------->" + mode);
    }


    /**
     * 完成刷新和加载更多
     * <h3>Version</h3> 1.0
     * <h3>CreateTime</h3> 2018/3/16,20:39
     * <h3>UpdateTime</h3> 2018/3/16,20:39
     * <h3>CreateAuthor</h3> zzs
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3>
     */
    public void finishRefreshAndLoad() {
        if (isRefreshing()) {
            finishRefresh();
        }
        if (isLoading()) {
            finishLoadmore();
        }

    }

    /**
     * 支持下拉刷新
     */
    public static final int MODE_PULL_DOWN_TO_REFRESH = 0x1;
    /**
     * 支持上拉刷新
     */
    public static final int MODE_PULL_UP_TO_REFRESH = 0x2;
    /**
     * 支持下拉和上拉刷新
     */
    public static final int MODE_BOTH = 0x3;
    /**
     * 支持拉动
     */
    public static final int MODE_ALL_NONE = 0x4;
    /**
     * 当前列表支持模式 （继续下拉还是松开刷新）
     */
    public int mode = MODE_BOTH;
    /**
     * 头部样式
     */
    public int headstyle = HEADSTYLE_CLASSICS;
    /**
     * 默认水滴
     */
    public static final int HEADSTYLE_DEFAULT = 0x1;
    /**
     * 经典
     */
    public static final int HEADSTYLE_CLASSICS = 0x2;
    /**
     * 仿IOS
     */
    public static final int HEADSTYLE_COPYIOS = 0x3;
    //     setLoadmoreFinished设置数据全部加载完成，将不能再次触发加载功能
//         setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);//水滴颜色与进度箭头颜色
//         setDragRate(0.5f);//显示下拉高度/手指真实下拉高度=阻尼效果
//         setReboundDuration(300);//回弹动画时长（毫秒）
//         setHeaderMaxDragRate(2);//最大显示下拉高度/Header标准高度
//         setFooterMaxDragRate(2);//最大显示下拉高度/Footer标准高度
//         setHeaderHeight(100);//Header标准高度（显示下拉高度>=标准高度 触发刷新）
//         setHeaderHeightPx(100);//同上-像素为单位
//         setFooterHeight(100);//Footer标准高度（显示上拉高度>=标准高度 触发加载）
//         setFooterHeightPx(100);//同上-像素为单位
//         setEnableRefresh(true);//是否启用下拉刷新功能
//         setEnableLoadmore(true);//是否启用上拉刷新功能
//         setEnableAutoLoadmore(true);//是否启用列表惯性滑动到底部时自动加载更多
//         setEnablePureScrollMode(false);//是否启用纯滚动模式
//         setEnableNestedScroll(false);//是否启用嵌套滚动
//         setEnableOverScrollBounce(true);//是否启用越界回弹
//         setEnableScrollContentWhenLoaded(true);//是否在加载完成时滚动列表显示新的内容
//         setEnableHeaderTranslationContent(true);//是否下拉Header的时候向下平移列表或者内容
//         setEnableFooterTranslationContent(true);//是否上啦Footer的时候向上平移列表或者内容
//         setEnableLoadmoreWhenContentNotFull(true);//是否在列表不满一页时候开启上拉加载功能
//         setDisableContentWhenRefresh(false);//是否在刷新的时候禁止列表的操作
//         setDisableContentWhenLoading(false);//是否在加载的时候禁止列表的操作
//         setOnMultiPurposeListener(new OnMultiPurposeListener());//设置多功能监听器
//         setScrollBoundaryDecider(new ScrollBoundaryDecider());//设置滚动边界判断
//         setRefreshHeader(new ClassicsHeader(this));//设置Header
//         setRefreshFooter(new ClassicsFooter(this));//设置Footer
//         autoRefresh();//自动刷新
//         autoLoadmore();//自动加载
//         autoRefresh(400);//延迟400毫秒后自动刷新
//         autoLoadmore(400);//延迟400毫秒后自动加载
//         finishRefresh();//结束刷新
//         finishLoadmore();//结束加载
//         finishRefresh(3000);//延迟3000毫秒后家属刷新
//         finishLoadmore(3000);//延迟3000毫秒后结束加载
//         finishRefresh(false);//结束刷新（刷新失败）
//         finishLoadmore(false);//结束加载（加载失败）
}
