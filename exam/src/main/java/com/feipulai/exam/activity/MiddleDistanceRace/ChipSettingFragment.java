package com.feipulai.exam.activity.MiddleDistanceRace;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.tcp.NettyClient;
import com.feipulai.device.tcp.NettyListener;
import com.feipulai.device.tcp.TcpConfig;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ChipSettingAdapter;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.ChipInfo;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.feipulai.exam.config.SharedPrefsConfigs.MACHINE_IP;
import static com.feipulai.exam.config.SharedPrefsConfigs.MACHINE_PORT;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE;
import static com.feipulai.exam.config.SharedPrefsConfigs.VEST_CHIP_NO;

/**
 * created by ww on 2019/6/24.
 */
public class ChipSettingFragment extends Fragment implements NettyListener, ChipSettingAdapter.OnItemClickListener, CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.rv_chip_setting)
    RecyclerView rvChipSetting;
    @BindView(R.id.rl_chip_add)
    RelativeLayout rlChipAdd;
    @BindView(R.id.tv_chip_ID1)
    TextView tvChipID1;
    @BindView(R.id.cb_chip_connect)
    CheckBox cbChipConnect;
    @BindView(R.id.view_state)
    View viewState;
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
                case 1:
                    chipAdapter.notifyDataSetChanged();
                    rvChipSetting.getLayoutManager().scrollToPosition((Integer) msg.obj);
                    break;
                case 2:
                    send();
                    mHandler.sendEmptyMessageDelayed(2, 8000);
                    break;
                default:
                    break;
            }
            return false;
        }
    });
    private List<ChipInfo> chipInfos;
    private ChipSettingAdapter chipAdapter;
    private int chipNo;
    private String machine_ip;
    private String machine_port;
    private boolean isFlag;
    private DialogUtil dialogUtil;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chip_setting, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();
        machine_ip = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MACHINE_IP, "");
        machine_port = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MACHINE_PORT, "0");

        isFlag = getActivity().getIntent().getBooleanExtra("isFlag", false);
        initEvent();
        if (isFlag) {
            initSocket();
        }
        return view;
    }

    //随便发送一个东西保持tcp不断
    private void send() {
        nettyClient.sendMsgToServer(TcpConfig.CMD_NOTHING, null);
    }


    //    初始化 连接设备
    private void initSocket() {
        nettyClient = new NettyClient(machine_ip, Integer.parseInt(machine_port));
        if (!nettyClient.getConnectStatus()) {
            nettyClient.setListener(this);
            nettyClient.connect(false);
        }
        mHandler.sendEmptyMessageDelayed(2, 8000);
    }

    private boolean isVisible;//当前fragment是否显示
    private List<String> cards = new ArrayList<>();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisibleToUser) {
            chipNo = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, VEST_CHIP_NO, 2);//一个背心对应的芯片数
            chipInfos.clear();
            chipInfos.addAll(DBManager.getInstance().queryAllChipInfo());
            chipAdapter.notifyDataSetChanged();

            cards.clear();
            for (ChipInfo chip : chipInfos
            ) {
                if (!TextUtils.isEmpty(chip.getChipID1())) {
                    cards.add(chip.getChipID1());
                }

                if (!TextUtils.isEmpty(chip.getChipID2())) {
                    cards.add(chip.getChipID2());
                }
            }
        }
    }

    private boolean isFinish = false;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("ChipSettingFragment", "--------------onDestroyView");
        unbinder.unbind();
        isFinish = true;
        //停止计时命令
//        if (nettyClient != null) {
//            nettyClient.sendMsgToServer(TcpConfig.getCmdEndTiming(), null);
//        }
        DBManager.getInstance().updateChipInfo(chipInfos);
        Logger.i("芯片配置完成:" + chipInfos.toString());
        mHandler.removeMessages(2);
        mHandler.removeMessages(1);
        if (nettyClient != null)
            nettyClient.disconnect();
    }

    private void initEvent() {
        dialogUtil = new DialogUtil(mContext);
        cbChipConnect.setOnCheckedChangeListener(this);

        chipInfos = new ArrayList<>();
        chipAdapter = new ChipSettingAdapter(chipInfos);
        rvChipSetting.setLayoutManager(new LinearLayoutManager(mContext));
        rvChipSetting.setAdapter(chipAdapter);
        chipAdapter.setOnRecyclerViewItemClickListener(this);

        tvChipID1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialogUtil.showCommonDialog("是否清楚全部芯片ID", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
                    @Override
                    public void onPositiveClick() {
                        for (ChipInfo chip : chipInfos
                        ) {
                            chip.setChipID1("");
                            chip.setChipID2("");
                        }
                        cards.clear();
                        chipAdapter.notifyDataSetChanged();
                        DBManager.getInstance().updateChipInfo(chipInfos);
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
                return false;
            }
        });
    }

    @Override
    public void onMessageResponse(Object msg) {

    }


    @Override
    public void onMessageReceive(long time, String[] cardIds) {
//        mHandler.sendMessage(mHandler.obtainMessage(0, Arrays.toString(cardIds)));
        Log.i("onMessageReceive", "" + isVisible + "----" + isSelect);
        if (isVisible && isSelect) {
            //将芯片id信息按先后顺序并且无重复填充到chipInfos
            if (chipNo == 1) {//如果一个背心对应1个芯片
                for (int i = 0; i < cardIds.length; i++) {
                    //先看接收到id在是否已存在
                    if (cards.contains(cardIds[i])) {
                        for (int j = 0; j < chipInfos.size(); j++) {
                            //当chipInfos中存在cardIds中的值时不允许填充并退出
                            if (cardIds[i].equals(chipInfos.get(j).getChipID1())) {
                                if (i == cardIds.length - 1) {
                                    chipAdapter.changeBackGround(1, j);
                                }
                                mHandler.sendMessage(mHandler.obtainMessage(1, j));
                                break;
                            }
                        }
                        continue;
                    }
                    if (cards.size() < chipInfos.size()) {
                        cards.add(cardIds[i]);
                        for (int j = 0; j < chipInfos.size(); j++) {
                            //填充芯片id到chipInfos
                            if (TextUtils.isEmpty(chipInfos.get(j).getChipID1())) {
                                chipInfos.get(j).setChipID1(cardIds[i]);
                                if (i == cardIds.length - 1) {
                                    chipAdapter.changeBackGround(1, j);
                                }
                                mHandler.sendMessage(mHandler.obtainMessage(1, j));
                                break;
                            }
                        }
                    }
                }
            } else {//如果一个背心对应2个芯片，接收到n个cardId
                for (int i = 0; i < cardIds.length; i++) {
                    //先看接收到id在是否已存在
                    if (cards.contains(cardIds[i])) {
                        for (int j = 0; j < chipInfos.size(); j++) {
                            //当chipInfos中存在cardIds中的值时不允许填充并退出
                            if (cardIds[i].equals(chipInfos.get(j).getChipID1())) {
                                if (i == cardIds.length - 1) {
                                    chipAdapter.changeBackGround(1, j);
                                }
                                mHandler.sendMessage(mHandler.obtainMessage(1, j));
                                break;
                            }

                            if (cardIds[i].equals(chipInfos.get(j).getChipID2())) {
                                if (i == cardIds.length - 1) {
                                    chipAdapter.changeBackGround(2, j);
                                }
                                mHandler.sendMessage(mHandler.obtainMessage(1, j));
                                break;
                            }

                        }
                        continue;
                    }
                    if (cards.size() < chipInfos.size() * 2) {
                        cards.add(cardIds[i]);

                        for (int j = 0; j < chipInfos.size(); j++) {
                            //填充芯片id到chipInfos
                            if (TextUtils.isEmpty(chipInfos.get(j).getChipID1())) {
                                chipInfos.get(j).setChipID1(cardIds[i]);
                                if (i == cardIds.length - 1) {
                                    chipAdapter.changeBackGround(1, j);
                                }
                                mHandler.sendMessage(mHandler.obtainMessage(1, j));
                                break;
                            }

                            if (TextUtils.isEmpty(chipInfos.get(j).getChipID2())) {
                                chipInfos.get(j).setChipID2(cardIds[i]);
                                if (i == cardIds.length - 1) {
                                    chipAdapter.changeBackGround(2, j);
                                }
                                mHandler.sendMessage(mHandler.obtainMessage(1, j));
                                break;
                            }
                        }
                    }


//                    for (int j = 0; j < chipInfos.size(); j++) {
//                        //当chipID1为空填充进chipInfos
//                        if (TextUtils.isEmpty(chipInfos.get(j).getChipID1())) {
//                            chipInfos.get(j).setChipID1(cardIds[i]);
//                            //最后一行背景高亮
//                            if (i == cardIds.length - 1) {
//                                chipAdapter.changeBackGround(1, j);
//                            }
//                            mHandler.sendMessage(mHandler.obtainMessage(1, j));
//                            break;
//                        } else {
//                            //chipID1不为空则要考虑是否和cardIds[i]相同，如果相同则跳出里循环
//                            if (cardIds[i].equals(chipInfos.get(j).getChipID1())) {
//                                //最后一个行背景高亮
//                                if (i == cardIds.length - 1) {
//                                    chipAdapter.changeBackGround(1, j);
//                                }
//                                mHandler.sendMessage(mHandler.obtainMessage(1, j));
//                                break;
//                            } else {
//                                //当chipID1既不为空也不和cardIds[i]相同时就要开始看ChipID2了，接下来逻辑和ChipID1类似
//                                if (TextUtils.isEmpty(chipInfos.get(j).getChipID2())) {
//                                    chipInfos.get(j).setChipID2(cardIds[i]);
//                                    //最后一行背景高亮
//                                    if (i == cardIds.length - 1) {
//                                        chipAdapter.changeBackGround(2, j);
//                                    }
//                                    mHandler.sendMessage(mHandler.obtainMessage(1, j));
//                                    break;
//                                } else {
//                                    if (cardIds[i].equals(chipInfos.get(j).getChipID2())) {
//                                        //最后一行背景高亮
//                                        if (i == cardIds.length - 1) {
//                                            chipAdapter.changeBackGround(2, j);
//                                        }
//                                        mHandler.sendMessage(mHandler.obtainMessage(1, j));
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
                }
            }
        }
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
    public void onServiceStatusConnectChanged(final int statusCode) {
        if (!isFinish)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (viewState != null) {
                        if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
                            viewState.setBackgroundResource(R.drawable.blue_circle);
                        } else {
                            viewState.setBackgroundResource(R.drawable.red_circle);
                        }
                    }
                }
            });
    }

    @Override
    public void onChipSettingLongClick(final int position) {
        dialogUtil.showCommonDialog("是否清除" + chipInfos.get(position).getColorGroupName() + chipInfos.get(position).getVestNo() + "芯片ID标签", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                chipInfos.get(position).setChipID1("");
                chipInfos.get(position).setChipID2("");
                DBManager.getInstance().updateChipInfo(chipInfos.get(position));
                Iterator<String> iterator = cards.iterator();
                while (iterator.hasNext()) {
                    String value = iterator.next();
                    if (chipInfos.get(position).getChipID1().equals(value) || chipInfos.get(position).getChipID2().equals(value)) {
                        iterator.remove();
                    }
                }
                chipAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNegativeClick() {

            }
        });
    }

    private boolean isSelect = false;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isFlag) {
            ToastUtils.showShort("请先连接设备");
            return;
        }
        isSelect = isChecked;
        if (isChecked) {
            nettyClient.sendMsgToServer(TcpConfig.getCmdStartTiming(), null);
        }
//        else {
//            nettyClient.sendMsgToServer(TcpConfig.getCmdEndTiming(), null);
//        }
    }
}
