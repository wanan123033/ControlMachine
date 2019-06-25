package com.feipulai.exam.activity.MiddleDistanceRace;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.tcp.NettyClient;
import com.feipulai.device.tcp.NettyListener;
import com.feipulai.exam.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * created by ww on 2019/6/24.
 */
public class ChipSettingFragment extends Fragment implements View.OnLongClickListener, NettyListener {
    @BindView(R.id.rv_chip_setting)
    RecyclerView rvChipSetting;
    @BindView(R.id.rl_chip_add)
    RelativeLayout rlChipAdd;
    private Context mContext;
    private Unbinder unbinder;
    private NettyClient nettyClient;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    ToastUtils.showShort("chip-" + msg.obj.toString());
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chip_setting, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();

        initEvent();

        initSocket();
        return view;
    }

    String host = "192.168.0.177";
    int port = 1401;

    //    初始化 连接设备
    private void initSocket() {
        nettyClient = new NettyClient(host, port);
        if (!nettyClient.getConnectStatus()) {
            nettyClient.setListener(this);
            nettyClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("onResume", "------------------");
        if (!nettyClient.getConnectStatus()) {
            nettyClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("onPause", "----------------");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (nettyClient != null)
            nettyClient.disconnect();
    }

    private void initEvent() {
        rlChipAdd.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {

        return false;
    }

    @Override
    public void onMessageResponse(Object msg) {

    }

    @Override
    public void onMessageReceive(long time, String cardId1, String cardId2) {
        mHandler.sendMessage(mHandler.obtainMessage(0, cardId1 + "---" + cardId2));
    }

    @Override
    public void onConnected(String text) {

    }

    @Override
    public void onMessageFailed(Object msg) {

    }

    @Override
    public void onStartTiming(long time) {

    }

    @Override
    public void onServiceStatusConnectChanged(int statusCode) {

    }
}
