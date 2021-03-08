package com.feipulai.exam.activity.situp.newSitUp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.ArmStateResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class DeviceChangeActivity extends BaseTitleActivity {

    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;
    @BindView(R.id.btn_change_bad)
    Button btnChangeBad;
    private List<DeviceCollect> deviceCollects = new ArrayList<>();
    private DeviceChangeAdapter mAdapter;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_device_change;
    }

    @Override
    protected void initData() {
        SitUpSetting setting = SharedPrefsUtil.loadFormSource(this, SitUpSetting.class);
        int deviceSum = setting.getDeviceSum();
        for (int i = 0; i < deviceSum; i++) {
            ArmStateResult armStateResult = new ArmStateResult();
            armStateResult.setDeviceId(i+1);
            SitPushUpStateResult stateResult = new SitPushUpStateResult();
            stateResult.setDeviceId(i+1);
            DeviceCollect deviceCollect = new DeviceCollect(stateResult,armStateResult);
            deviceCollects.add(deviceCollect);
        }
        mRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        mRvPairs.addItemDecoration(dividerItemDecoration);
        mAdapter = new DeviceChangeAdapter(this,deviceCollects);
        mRvPairs.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new DeviceChangeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int viewId, int position) {
                mAdapter.setSelected(position);

                switch (viewId){
                    case R.id.tv_arm:
                        mAdapter.setSelectDevice(2);
                        break;
                    case R.id.tv_sit_up:
                        mAdapter.setSelectDevice(1);
                        break;
                }
            }
        });
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备详情");
    }


    @OnClick({R.id.btn_change_bad})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_change_bad:

                break;
        }
    }
}
