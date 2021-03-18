package com.feipulai.exam.activity.situp.newSitUp;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;

import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.ArmStateResult;
import com.feipulai.device.serial.beans.ShoulderResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.view.DividerItemDecoration;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.OnClick;

public class NewSitUpPairActivity extends BaseTitleActivity implements NewSitUpPairContract.View{

    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;
    private NewDevicePairAdapter mAdapter;
//    private List<DeviceCollect> deviceCollects = new ArrayList<>();
    private NewSitUpPairPresenter presenter;
    @BindView(R.id.sw_auto_pair)
    Switch mSwAutoPair;

    private static final int UPDATE_SPECIFIC_ITEM = 0x1;
    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_new_sit_up_pair;
    }

    @Override
    protected void initData() {
        presenter = new NewSitUpPairPresenter(this,this);
        presenter.setDevice(1);
        presenter.start();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备连接");
    }

    @Override
    public void initView(boolean isAutoPair, List<DeviceCollect> deviceCollects) {
        mSwAutoPair.setChecked(isAutoPair);
        mRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        mRvPairs.addItemDecoration(dividerItemDecoration);
        mAdapter = new NewDevicePairAdapter(this,deviceCollects);
        mRvPairs.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new NewDevicePairAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int viewId, int position) {
                mAdapter.setSelected(position);
                switch (viewId){
                    case R.id.tv_arm:
//                        mAdapter.setSelectDevice(2);
//                        presenter.setDevice(2);
                        LogUtils.operation("选择设备device："+ 2+"position:"+position);
                        presenter.changeFocusPosition(position, 2);
                        break;
                    case R.id.tv_sit_up:
//                        mAdapter.setSelectDevice(1);
//                        presenter.setDevice(1);
                        LogUtils.operation("选择设备device："+ 1+"position:"+position);
                        presenter.changeFocusPosition(position, 1);
                        break;
                }
            }
        });

    }

    @Override
    public void updateSpecificItem(int position,int device) {
        LogUtils.operation("更新设备device："+ device+"position:"+position);
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = position;
        msg.arg2 = device;
        mHandler.sendMessage(msg);
    }

    @Override
    public void select(int position,int device) {
        LogUtils.operation("处理设备device："+ device+"position:"+position);
//        int oldSelectPosition = mAdapter.getSelected();
//        mAdapter.setSelected(position);
//        updateSpecificItem(oldSelectPosition,device);
        updateSpecificItem(position,device);
    }

    @Override
    public void showToast(String msg) {
        toastSpeak(msg);
    }

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case UPDATE_SPECIFIC_ITEM:
                mAdapter.setSelectDevice(msg.arg2);
                mAdapter.notifyItemChanged(msg.arg1);
                break;
        }
    }

    @OnClick({R.id.sw_auto_pair})
    public void btnOnClick(View v) {
        switch (v.getId()) {
            case R.id.sw_auto_pair:
                LogUtils.operation("勾选了自动匹配");
                presenter.changeAutoPair(mSwAutoPair.isChecked());
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.life("SitPullPairActivity onPause");
        presenter.saveSettings();
        presenter.stopPair();
    }
}
