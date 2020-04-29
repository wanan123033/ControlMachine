package com.feipulai.exam.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.exam.R;
import com.feipulai.exam.adapter.GroupAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengjf on 2018/11/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class CommonPopupWindow extends Dialog {
    private Context context;
    private OnPopItemClickListener onPopItemClickListener;
    private RecyclerView rv_pop;
    BaseQuickAdapter mAdapter;
    List results ;
    private SearchView searchView;

    public void setOnPopItemClickListener(OnPopItemClickListener onPopItemClickListener) {
        this.onPopItemClickListener = onPopItemClickListener;
    }

    /**
     * @param context
     */
    public CommonPopupWindow(Context context, BaseQuickAdapter adapter) {
        super(context);
        this.context = context;
        mAdapter = adapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_layout);
        //设置背景色为透明，解决设置圆角后有白色直角的问题
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        Window win = getWindow();
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.x = -100;//设置x坐标
//        params.y = -10;//设置y坐标
//        params.width = 500;
//        params.height = 400;
//        win.setAttributes(params);
        rv_pop = findViewById(R.id.rv_popup_window);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_pop.setLayoutManager(layoutManager);
        setAdapter(mAdapter);
        searchView = findViewById(R.id.et_search);
        if (mAdapter instanceof GroupAdapter) {
            searchView.setIconifiedByDefault(false);//设为true则搜索栏 缩小成俄日一个图标点击展开
            //设置该SearchView显示搜索按钮
            searchView.setSubmitButtonEnabled(true);
            searchView.setQueryHint("输入您想查找的内容");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!TextUtils.isEmpty(newText)) {
                        mAdapter.setNewData(((GroupAdapter) mAdapter).search(newText, results));
                    } else {
                        mAdapter.setNewData(results);
                    }
                    mAdapter.notifyDataSetChanged();

                    return false;
                }
            });

        } else {
            searchView.setVisibility(View.GONE);
        }


    }

    public void updateAdapter(List group){
        results = new ArrayList();
        results.addAll(group);
        mAdapter.getData().clear();
        mAdapter.getData().addAll(results);

        mAdapter.notifyDataSetChanged();
    }

    private void setAdapter(BaseQuickAdapter adapter) {

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (onPopItemClickListener != null) {
                    if (searchView.getVisibility() == View.VISIBLE) {
                        onPopItemClickListener.itemClick(results.indexOf(adapter.getData().get(position)));
                    } else {
                        onPopItemClickListener.itemClick(position);
                    }

                    if (isShowing())
                        dismiss();
                }
            }
        });
        rv_pop.setAdapter(adapter);
    }

    public void showPopOrDismiss() {
        if (isShowing()) {
            dismiss();

        } else {
            show();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    hideSoftKeyboard(context);
                }
            });
        }
    }

    public interface OnPopItemClickListener {
        void itemClick(int position);
    }

    public void hideSoftKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (searchView != null)
            imm.showSoftInput(searchView, InputMethodManager.SHOW_FORCED);

        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0); //强制隐藏键盘
    }


    @Override
    public void dismiss() {
        View view = getCurrentFocus();
        if (view instanceof TextView) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
        super.dismiss();
    }
}
