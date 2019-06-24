package com.feipulai.exam.activity.MiddleDistanceRace;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.view.MiddleRace.PanelAdapter;

import java.util.List;

/**
 * created by ww on 2019/6/18.
 */
public class TestPanelAdapter extends PanelAdapter {
    private List<RaceResultBean2> datas;

    public TestPanelAdapter(List<RaceResultBean2> data) {
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
        titleViewHolder.titleTextView.setText(string);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TestPanelAdapter.TitleViewHolder(LayoutInflater.from(parent.getContext())
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
