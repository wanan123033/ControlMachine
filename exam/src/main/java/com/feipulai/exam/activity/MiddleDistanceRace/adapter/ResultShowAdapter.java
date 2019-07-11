package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.RaceResultBean;
import com.feipulai.exam.view.MiddleRace.PanelAdapter;

import java.util.List;

/**
 * created by ww on 2019/6/18.
 */
public class ResultShowAdapter extends PanelAdapter {
    private List<RaceResultBean> datas;

    public ResultShowAdapter(List<RaceResultBean> data) {
        this.datas = data;
    }

    @Override
    public int getRowCount() {
        return datas.size();
    }

    @Override
    public int getColumnCount() {
        return datas.get(0).getResults().length;
    }

    @Override
    public int getItemViewType(int row, int column) {
        return super.getItemViewType(row, column);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int row, int column) {
        String string = datas.get(row).getResults()[column];
        TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
        if (column == 0) {
            titleViewHolder.titleTextView.setBackgroundResource(datas.get(row).getColor());
        }

        if (row == 0) {
            titleViewHolder.titleTextView.setText(string);
            return;
        }
        if (column > datas.get(row).getCycle() + 2) {
            titleViewHolder.titleTextView.setText("X");
        } else {
            if (column > 1) {
                titleViewHolder.titleTextView.setText(TextUtils.isEmpty(string) ? "" : DateUtil.getDeltaT(Long.parseLong(string)));
            } else {
                titleViewHolder.titleTextView.setText(string);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ResultShowAdapter.TitleViewHolder(LayoutInflater.from(parent.getContext())
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
