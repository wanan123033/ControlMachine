package com.feipulai.exam.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.exam.R;

/**
 * Created by pengjf on 2018/11/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class CommonPopupWindow extends Dialog {
    private Context context;
    private OnPopItemClickListener onPopItemClickListener;
    private RecyclerView rv_pop;
    BaseQuickAdapter mAdapter;

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
    }

    private void setAdapter(BaseQuickAdapter adapter) {

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (onPopItemClickListener != null) {
                    onPopItemClickListener.itemClick(position);
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
        }

    }

    public interface OnPopItemClickListener {
        void itemClick(int position);
    }
}
