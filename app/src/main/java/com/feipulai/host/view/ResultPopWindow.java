package com.feipulai.host.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.host.R;
import com.feipulai.host.adapter.PopAdapter;


/**
 * Created by pengjf on 2019/4/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultPopWindow extends PopupWindow{
    private Context context;
    private final PopupWindow popupWindow;
    private BaseQuickAdapter adapter;
    private CommonPopupWindow.OnPopItemClickListener onPopItemClickListener;
    private final RecyclerView rv_pop;

    public void setOnPopItemClickListener(CommonPopupWindow.OnPopItemClickListener onPopItemClickListener) {
        this.onPopItemClickListener = onPopItemClickListener;
    }

    /**
     * @param context
     */
    public ResultPopWindow(Context context, PopAdapter popAdapter) {
        this.context = context;
        View contentView = LayoutInflater.from(context).inflate(
                R.layout.pop_layout, null);
        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT, true);
        rv_pop = contentView.findViewById(R.id.rv_popup_window);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_pop.setLayoutManager(layoutManager);
        adapter = popAdapter ;
        setAdapter();
    }

    private void setAdapter() {
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (onPopItemClickListener != null) {
                    onPopItemClickListener.itemClick(position);
                    if (popupWindow != null && popupWindow.isShowing())
                        popupWindow.dismiss();
                }
            }
        });
        rv_pop.setAdapter(adapter);
    }

    public void showPopOrDismiss(View view) {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            popupWindow.showAsDropDown(view);
        }

    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public interface OnPopItemClickListener {
        void itemClick(int position);
    }

    public void notifyPop(){
        adapter.notifyDataSetChanged();
    }

}
