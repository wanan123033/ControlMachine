package com.feipulai.wireless.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.device.serial.beans.VitalCapacityResult;
import com.feipulai.wireless.R;
import com.feipulai.wireless.adapter.DevicesAdapter;
import com.feipulai.wireless.beans.BasePair;
import com.feipulai.wireless.utils.SharedPrefsUtil;
import com.feipulai.wireless.widgets.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WirelessManager.WirelessListener, View.OnClickListener {

    private List<BasePair> pairList = new ArrayList<>();
    private static final String TAG = "MainActivity";
    private WirelessManager mWirelessManager;
    private DevicesAdapter mAdapter;
    private final String FREQUENCY = "FREQUENCY";
    //频段
    private int frequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mWirelessManager = WirelessManager.getInstance();
        mWirelessManager.setListener(this);
        frequency = SharedPrefsUtil.getIntValue(this, FREQUENCY, 110);
    }

    private void initView() {
        RecyclerView rvDevices = findViewById(R.id.ry_device);
        for (int i = 1; i < 9; i++) {
            BasePair pair = new BasePair(i, 1, 0, 0);
            pairList.add(pair);
        }
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.addItemDecoration(new DividerItemDecoration(this));
        mAdapter = new DevicesAdapter(pairList,1);
        rvDevices.setAdapter(mAdapter);
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (pairList.get(position).getPair() == 1) {
                    switch (view.getId()) {
                        case R.id.btn_start_test:
                            mWirelessManager.startTest(pairList.get(position).getDeviceId());
                            break;
                    }

                }
            }
        });

        TextView test = findViewById(R.id.tv_test);
        test.setOnClickListener(this);
    }


    @Override
    public void onPareListener() {
        for (BasePair pair : pairList) {
            if (pair.getPair() == 0) {//没有配对
                mWirelessManager.sendPair(pair.getDeviceId());
                break;
            }
        }

    }

    @Override
    public void onPairSuccess(VitalCapacityResult result) {
        for (BasePair pair : pairList) {
            if (pair.getDeviceId() == result.getDeviceId()) {
                pair.setCount(0);
                pair.setPower(result.getPower());
                pair.setState(result.getState());
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
        mWirelessManager.setFrequency(0);
    }

    @Override
    public void onResult(VitalCapacityResult result) {

    }

    @Override
    public void onStop(VitalCapacityResult result) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_test:
                List<BasePair> lists = new ArrayList();
                for (BasePair pair : pairList) {
                    if (pair.getPair() == 1){
                        lists.add(pair);
                    }
                }
                if(lists.size() == 0){
                    Toast.makeText(this,"当前无已配对设备请先配对",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this,ControlActivity.class);
                intent.putParcelableArrayListExtra("pairList", (ArrayList<? extends Parcelable>) lists);
                intent.putExtras(intent);
                startActivity(intent);
                break;
        }
    }
}
