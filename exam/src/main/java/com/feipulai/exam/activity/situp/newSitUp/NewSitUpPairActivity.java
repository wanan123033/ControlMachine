package com.feipulai.exam.activity.situp.newSitUp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.ArmStateResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class NewSitUpPairActivity extends BaseTitleActivity implements SitPullUpPairContract.View{

    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;
    private NewDevicePairAdapter mAdapter;
    private List<DeviceCollect> deviceCollects = new ArrayList<>();
    private NewSitUpPairPresenter presenter;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_new_sit_up_pair;
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
        mAdapter = new NewDevicePairAdapter(this,deviceCollects);
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
        presenter = new NewSitUpPairPresenter(this,this);
        presenter.start();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备连接");
    }

    @Override
    public void initView(boolean isAutoPair, List<StuDevicePair> stuDevicePairs) {

    }

    @Override
    public void updateSpecificItem(int focusPosition) {

    }

    @Override
    public void select(int position) {

    }

    @Override
    public void showToast(String msg) {

    }
}
