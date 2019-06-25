package com.feipulai.exam.activity.MiddleDistanceRace;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.feipulai.exam.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * created by ww on 2019/6/24.
 */
public class ChipSettingFragment extends Fragment implements View.OnLongClickListener {
    @BindView(R.id.rv_chip_setting)
    RecyclerView rvChipSetting;
    @BindView(R.id.rl_chip_add)
    RelativeLayout rlChipAdd;
    private Context mContext;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chip_setting, container, false);
        ButterKnife.bind(this, view);
        mContext = getActivity();

        initEvent();
        return view;
    }

    private void initEvent() {

        rlChipAdd.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {

        return false;
    }
}
