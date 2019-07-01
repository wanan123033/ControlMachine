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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.tcp.NettyClient;
import com.feipulai.device.tcp.NettyListener;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ChipSettingAdapter;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.ChipInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE;
import static com.feipulai.exam.config.SharedPrefsConfigs.VEST_CHIP_NO;

/**
 * created by ww on 2019/6/24.
 */
public class ChipSettingFragment extends Fragment implements NettyListener, ChipSettingAdapter.OnItemClickListener {
    @BindView(R.id.rv_chip_setting)
    RecyclerView rvChipSetting;
    @BindView(R.id.rl_chip_add)
    RelativeLayout rlChipAdd;
    @BindView(R.id.tv_chip_ID1)
    TextView tvChipID1;
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
//                    ((LinearLayoutManager) rvChipSetting.getLayoutManager()).scrollToPositionWithOffset((Integer) msg.obj, 0);
                    rvChipSetting.getLayoutManager().scrollToPosition((Integer) msg.obj);
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

    private boolean isVisible;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisibleToUser) {
            chipNo = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, VEST_CHIP_NO, 2);//一个背心对应的芯片数
            chipInfos.clear();
            chipInfos.addAll(DBManager.getInstance().queryAllChipInfo());
            chipAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("ChipSettingFragment", "--------------onDestroyView");
        unbinder.unbind();
        DBManager.getInstance().updateChipInfo(chipInfos);
        if (nettyClient != null)
            nettyClient.disconnect();
    }

    private void initEvent() {
        chipInfos = new ArrayList<>();
        chipAdapter = new ChipSettingAdapter(chipInfos);
        rvChipSetting.setLayoutManager(new LinearLayoutManager(mContext));
        rvChipSetting.setAdapter(chipAdapter);
        chipAdapter.setOnRecyclerViewItemClickListener(this);

        tvChipID1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DialogUtil.showCommonDialog(mContext, "是否清楚全部芯片ID1", new DialogUtil.DialogListener() {
                    @Override
                    public void onPositiveClick() {
                        for (ChipInfo chip : chipInfos
                                ) {
                            chip.setChipID1("");
                            chip.setChipID2("");
                        }
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
        mHandler.sendMessage(mHandler.obtainMessage(0, Arrays.toString(cardIds)));
        if (isVisible) {
            //将芯片id信息按先后顺序并且无重复填充到chipInfos
            if (chipNo == 1) {//如果一个背心对应1个芯片
                for (int i = 0; i < cardIds.length; i++) {
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
                        //当chipInfos中存在cardIds中的值时不允许填充并退出
                        if (cardIds[i].equals(chipInfos.get(j).getChipID1())) {
                            if (i == cardIds.length - 1) {
                                chipAdapter.changeBackGround(1, j);
                            }
                            mHandler.sendMessage(mHandler.obtainMessage(1, j));
                            break;
                        }
                    }
                }
            } else {//如果一个背心对应2个芯片，接收到n个cardId
                for (int i = 0; i < cardIds.length; i++) {
                    for (int j = 0; j < chipInfos.size(); j++) {
                        //当chipID1为空填充进chipInfos
                        if (TextUtils.isEmpty(chipInfos.get(j).getChipID1())) {
                            chipInfos.get(j).setChipID1(cardIds[i]);
                            //最后一行背景高亮
                            if (i == cardIds.length - 1) {
                                chipAdapter.changeBackGround(1, j);
                            }
                            mHandler.sendMessage(mHandler.obtainMessage(1, j));
                            break;
                        } else {
                            //chipID1不为空则要考虑是否和cardIds[i]相同，如果相同则跳出里循环
                            if (cardIds[i].equals(chipInfos.get(j).getChipID1())) {
                                //最后一个行背景高亮
                                if (i == cardIds.length - 1) {
                                    chipAdapter.changeBackGround(1, j);
                                }
                                mHandler.sendMessage(mHandler.obtainMessage(1, j));
                                break;
                            } else {
                                //当chipID1既不为空也不和cardIds[i]相同时就要开始看ChipID2了，接下来逻辑和ChipID1类似
                                if (TextUtils.isEmpty(chipInfos.get(j).getChipID2())) {
                                    chipInfos.get(j).setChipID2(cardIds[i]);
                                    //最后一行背景高亮
                                    if (i == cardIds.length - 1) {
                                        chipAdapter.changeBackGround(2, j);
                                    }
                                    mHandler.sendMessage(mHandler.obtainMessage(1, j));
                                    break;
                                } else {
                                    if (cardIds[i].equals(chipInfos.get(j).getChipID2())) {
                                        //最后一行背景高亮
                                        if (i == cardIds.length - 1) {
                                            chipAdapter.changeBackGround(2, j);
                                        }
                                        mHandler.sendMessage(mHandler.obtainMessage(1, j));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }


//            for (int i = 0; i < chipInfos.size(); i++) {
//                //当芯片表中某一行第一个ID为空，可以直接填充两个ID（ID2可能为""）
//                if (TextUtils.isEmpty(chipInfos.get(i).getChipID1())) {
//                    //当为非第一行时，判断上一行的第二个ID是否和接收的ID相同
//                    if (i > 0) {
//                        if (cardId1.equals(chipInfos.get(i - 1).getChipID2())) {
//                            chipAdapter.changeBackGround(2, i - 1);
//                            mHandler.sendMessage(mHandler.obtainMessage(1, i - 1));
//                            break;
//                        }
//                    }
//                    chipInfos.get(i).setChipID1(cardId1);
//                    chipInfos.get(i).setChipID2(cardId2);
//                    if ("".equals(cardId2)) {
//                        chipAdapter.changeBackGround(1, i);
//                    } else {
//                        chipAdapter.changeBackGround(2, i);
//                    }
//                    mHandler.sendMessage(mHandler.obtainMessage(1, i));
//                    break;
//                } else {
//                    //当芯片表中某一行第一个ID不为空，需要先判断这个ID和接收到的ID1，如果相同则跳出循环并使该行ID背景变色
//                    if (cardId1.equals(chipInfos.get(i).getChipID1())) {
//                        chipAdapter.changeBackGround(1, i);
//                        mHandler.sendMessage(mHandler.obtainMessage(1, i));
//                        break;
//                    }
//                    //当芯片表中某一行第二个ID为空，直接插入ID1到第二个ID中，并插入ID2到下一行的ID1中（ID2可能为""）
//                    if (TextUtils.isEmpty(chipInfos.get(i).getChipID2())) {
//                        chipInfos.get(i).setChipID2(cardId1);
//                        chipInfos.get(i + 1).setChipID1(cardId2);
//                        if ("".equals(cardId2)) {
//                            chipAdapter.changeBackGround(2, i);
//                            mHandler.sendMessage(mHandler.obtainMessage(1, i));
//                        } else {
//                            chipAdapter.changeBackGround(1, i + 1);
//                            mHandler.sendMessage(mHandler.obtainMessage(1, i + 1));
//                        }
//                        break;
//                    } else {
//                        //当芯片表中某一行第二个ID不为空，需要先判断这个ID和接收到的ID1，如果相同则跳出循环并使该行ID背景变色
//                        if (cardId1.equals(chipInfos.get(i).getChipID2())) {
//                            chipAdapter.changeBackGround(2, i);
//                            mHandler.sendMessage(mHandler.obtainMessage(1, i));
//                            break;
//                        }
//                    }
//                }
//            }
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
    public void onServiceStatusConnectChanged(int statusCode) {

    }

    @Override
    public void onChipSettingLongClick(final int position) {
        DialogUtil.showCommonDialog(mContext, "是否清除" + chipInfos.get(position).getColorGroupName() + chipInfos.get(position).getVestNo() + "芯片ID标签", new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                chipInfos.get(position).setChipID1("");
                chipInfos.get(position).setChipID2("");
                DBManager.getInstance().updateChipInfo(chipInfos.get(position));
                chipAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNegativeClick() {

            }
        });
    }
}
