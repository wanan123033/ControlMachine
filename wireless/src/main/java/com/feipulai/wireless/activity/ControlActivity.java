package com.feipulai.wireless.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.device.serial.beans.VitalCapacityResult;
import com.feipulai.wireless.R;
import com.feipulai.wireless.adapter.DevicesAdapter;
import com.feipulai.wireless.beans.BasePair;
import com.feipulai.wireless.utils.SharedPrefsUtil;
import com.feipulai.wireless.widgets.DividerItemDecoration;

import java.util.List;

public class ControlActivity extends AppCompatActivity implements WirelessManager.WirelessListener {
    private DevicesAdapter mAdapter;
    private WirelessManager mWirelessManager;
    private List<BasePair> pairList;
    public final static int DELAY_STOP = 0X1000;
    private final String FREQUENCY = "FREQUENCY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        mWirelessManager = WirelessManager.getInstance();
        int frequency = SharedPrefsUtil.getIntValue(this, FREQUENCY, 110);
        mWirelessManager.setFrequency(frequency);
        mWirelessManager.setListener(this);
        mWirelessManager.startQueryThread();
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        pairList = intent.getParcelableArrayListExtra("pairList");
        RecyclerView rvDevices = findViewById(R.id.ry_device);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.addItemDecoration(new DividerItemDecoration(this));
        mAdapter = new DevicesAdapter(pairList, 2);
        rvDevices.setAdapter(mAdapter);

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.btn_start_test:
                        mWirelessManager.startTest(pairList.get(position).getDeviceId());
                        break;
                    case R.id.btn_set_free:
                        mWirelessManager.settingFree(pairList.get(position).getDeviceId());
                        break;
                }

            }
        });

    }

    @Override
    public void onPareListener() {

    }

    @Override
    public void onPairSuccess(VitalCapacityResult result) {

    }

    @Override
    public void onResult(VitalCapacityResult result) {
        for (BasePair pair : pairList) {
            if (pair.getDeviceId() == result.getDeviceId()) {
                pair.setCount(result.getCapacity());
                pair.setPower(result.getPower());
                if (result.getState() != 4) {
                    //结束是暂不更新，交给另一个地方处理
                    pair.setState(result.getState());
                }
                pair.setPair(1);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });

            break;
        }
    }

    @Override
    public void onStop(VitalCapacityResult result) {
        for (BasePair pair : pairList) {
            if (pair.getState() != 4 && pair.getDeviceId() == result.getDeviceId()) {
                Message msg = Message.obtain();
                msg.what = DELAY_STOP;
                msg.obj = pair;
                mHandler.sendMessageDelayed(msg, 500);
                break;
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case DELAY_STOP:
                    stop((BasePair) msg.obj);
                    break;
            }
            return false;
        }
    });

    private void stop(BasePair pair) {
        pair.setState(4);//结束
        mWirelessManager.stopTest(pair.getDeviceId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWirelessManager.stopQuery();
    }
}
