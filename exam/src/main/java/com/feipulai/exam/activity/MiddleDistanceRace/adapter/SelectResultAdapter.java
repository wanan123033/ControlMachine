package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.RaceResultBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.SelectResultBean;
import com.feipulai.exam.view.MiddleRace.PanelAdapter;

import java.util.List;

/**
 * created by ww on 2019/6/18.
 */
public class SelectResultAdapter extends PanelAdapter {
    private List<List<String>> datas;
    private String[] title = {"道次", "考号", "姓名", "最终成绩"};

    public SelectResultAdapter(List<List<String>> data) {
        this.datas = data;
    }

    @Override
    public int getRowCount() {
        return datas.size();
    }

    @Override
    public int getColumnCount() {
        return datas.get(1).size();//0位置为空，为标题栏放在adapter中处理
    }

    @Override
    public int getItemViewType(int row, int column) {
        return super.getItemViewType(row, column);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int row, int column) {
        TitleViewHolder titleViewHolder = (TitleViewHolder) holder;

        if (row == 0) {
            if (column < 4) {
                titleViewHolder.titleTextView.setText(title[column]);
            } else {
                titleViewHolder.titleTextView.setText("第" + (column - 3) + "圈");
            }
        } else {
            if (column < 3) {
                titleViewHolder.titleTextView.setText(datas.get(row).get(column));
            } else {
//                titleViewHolder.titleTextView.setText(DateUtil.getDeltaT(Long.parseLong(datas.get(row).get(column))));
                titleViewHolder.titleTextView.setText(datas.get(row).get(column));
            }
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectResultAdapter.TitleViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_title, parent, false));
    }

    private static class TitleViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;

        public TitleViewHolder(View view) {
            super(view);
            this.titleTextView = view.findViewById(R.id.title);
        }
    }
}
