package com.feipulai.exam.activity.sport_timer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.sport_timer.adapter.SportDeviceAdapter;
import com.feipulai.exam.activity.sport_timer.bean.DeviceState;
import com.feipulai.exam.view.DividerItemDecoration;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceDialog extends Dialog {

    @BindView(R.id.rv_device_list)
    RecyclerView mRvPairs;
    private SportDeviceAdapter mAdapter;
    private Context context;
    private List<DeviceState> devices;
    public DeviceDialog(Context context,List<DeviceState> devices) {
        super(context, R.style.loadingDialogStyle);
        this.context = context;
        this.devices = devices;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device);
        ButterKnife.bind(this);
        mRvPairs.setLayoutManager(new GridLayoutManager(context, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        mRvPairs.addItemDecoration(dividerItemDecoration);

        mAdapter = new SportDeviceAdapter(devices);
        mRvPairs.setAdapter(mAdapter);
    }

    public void setDeviceState(int index,int state){
        devices.get(index).setDeviceState(state);
        mAdapter.notifyDataSetChanged();
    }
}
