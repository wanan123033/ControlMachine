package com.feipulai.exam.activity.jump_rope.pair;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseActivity;
import com.feipulai.exam.activity.jump_rope.adapter.DevicePairAdapter;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.view.DividerItemDecoration;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class JumpRopePairActivity extends BaseActivity
        implements JumpRopePairContract.View,
        DevicePairAdapter.OnItemClickListener {

    private static final int UPDATE_SPECIFIC_ITEM = 0x1;
    private static final int UPDATE_ITEMS = 0x2;
    private static final String TAG = "JumpRopePairActivity";
    @BindView(R.id.sp_hand_group)
    Spinner mSpGroup;
    @BindView(R.id.sw_auto_pair)
    Switch mSwAutoPair;

    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;

    private DevicePairAdapter mAdapter;
    private MyHandler mHandler = new MyHandler(this);
    private JumpRopePairPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_pair);
        ButterKnife.bind(this);
        presenter = new JumpRopePairPresenter(this, this);
        presenter.start();
    }

    @Override
    public void initView(JumpRopeSetting setting, List pairs) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SerialConfigs.GROUP_NAME);
        mSpGroup.setAdapter(adapter);
        mSpGroup.setSelection(setting.getDeviceGroup());

        boolean isAutoPair = setting.isAutoPair();
        mSwAutoPair.setChecked(isAutoPair);

        mRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        mRvPairs.addItemDecoration(dividerItemDecoration);

        mAdapter = new DevicePairAdapter(this, pairs);
        mRvPairs.setAdapter(mAdapter);
        mRvPairs.setClickable(true);
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.resumePair();
    }

    @Override
    public void onItemClick(int position) {
        presenter.changeFocusPosition(position);
    }

    @Override
    public void updateSpecificItem(int position) {
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = position;
        mHandler.sendMessage(msg);
    }

    @Override
    public void updateAllItems() {
        mHandler.sendEmptyMessage(UPDATE_ITEMS);
    }

    @Override
    public void select(int position) {
        int oldSelectPosition = mAdapter.getSelected();
        mAdapter.setSelected(position);
        updateSpecificItem(oldSelectPosition);
        updateSpecificItem(position);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_SPECIFIC_ITEM:
                mAdapter.notifyItemChanged(msg.arg1);
                break;

            case UPDATE_ITEMS:
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    @OnItemSelected(R.id.sp_hand_group)
    public void onItemSelect(int pos) {
        presenter.changeDeviceGroup(pos);
    }


    @OnClick({R.id.sw_auto_pair})
    public void btnOnClick(View v) {
        switch (v.getId()) {
            case R.id.sw_auto_pair:
                presenter.changeAutoPair(mSwAutoPair.isChecked());
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.pausePair();
        presenter.saveSettings();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stopPair();
    }

}
