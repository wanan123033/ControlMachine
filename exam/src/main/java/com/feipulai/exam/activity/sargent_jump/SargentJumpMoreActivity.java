package com.feipulai.exam.activity.sargent_jump;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.feipulai.exam.R;
import com.feipulai.exam.adapter.DeviceListAdapter;
import com.feipulai.exam.bean.DeviceDetail;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SargentJumpMoreActivity extends AppCompatActivity {

    @BindView(R.id.rv_device_list)
    RecyclerView rvDeviceList;
    private List<DeviceDetail> deviceDetails = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sargent_jump_more);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        DeviceListAdapter deviceListAdapter = new DeviceListAdapter(deviceDetails);
        GridLayoutManager layoutManager = new GridLayoutManager(this,4);
        rvDeviceList.setLayoutManager(layoutManager);
        rvDeviceList.setAdapter(deviceListAdapter);
    }
}
