package com.feipulai.exam.activity.MiddleDistanceRace;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.common.utils.DialogUtils;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.tcp.NettyClient;
import com.feipulai.device.tcp.NettyListener;
import com.feipulai.device.tcp.TcpConfig;
import com.feipulai.device.udp.UdpClient;
import com.feipulai.device.udp.result.UDPResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.MiddleRaceGroupAdapter;
import com.feipulai.exam.adapter.RaceTimingAdapter;
import com.feipulai.exam.adapter.ScheduleAdapter;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.utils.DateUtil;
import com.feipulai.exam.view.MiddleRace.ScrollablePanel;
import com.feipulai.exam.view.baseToolbar.BaseToolbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class MiddleDistanceRaceActivity extends BaseTitleActivity implements UdpClient.UDPChannelListerner, NettyListener, RaceTimingAdapter.MyClickListener, ChannelFutureListener, MiddleRaceGroupAdapter.OnItemClickListener {

    @BindView(R.id.btn_udp_send)
    Button btnUdpSend;
    @BindView(R.id.et_udp_input)
    EditText etUdpInput;
    @BindView(R.id.sp_race_item)
    Spinner spRaceItem;
    @BindView(R.id.sp_race_state)
    Spinner spRaceState;
    @BindView(R.id.rv_race_student_group)
    RecyclerView rvRaceStudentGroup;
    @BindView(R.id.rv_race_group)
    RecyclerView rvRaceGroup;
    //    @BindView(R.id.rv_race_result)
//    RecyclerView rvMiddleRaceResult;
    @BindView(R.id.sp_race_schedule)
    Spinner spRaceSchedule;
    @BindView(R.id.et_udp_input2)
    EditText etUdpInput2;
    @BindView(R.id.timer1)
    Chronometer timer1;
    @BindView(R.id.timer2)
    Chronometer timer2;
    @BindView(R.id.btn_start1)
    Button btnStart1;
    @BindView(R.id.btn_start2)
    Button btnStart2;
    @BindView(R.id.race_scrollablePanel)
    ScrollablePanel scrollablePanel;
    private String TAG = "MiddleDistanceRaceActivity";
    private final int MESSAGE_A = 1;

    private Handler mHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_A:
                    ToastUtils.showShort(msg.obj.toString());
                    break;
                case 3:
                    raceTimingAdapter.notifyDataSetChanged();
                    break;
                case 4:
                    for (TimingBean timingBean : timingLists
                            ) {
                        //开始计时之后，所有准备状态下的计时器需要改变为正在计时状态
                        if (timingBean.getState() == 1) {
                            timingBean.setState(4);
                        }
                    }
                    Log.i("timingLists", timingLists.toString());
                    break;
                default:
                    break;
            }
            return false;
        }
    });
    private NettyClient nettyClient;
    private MiddleRaceGroupAdapter groupAdapter;
    private ScheduleAdapter scheduleAdapter;
    private String scheduleNo;
    private ArrayAdapter<String> itemAdapter;
    private long startTime;
    private long startTime2;
    private RaceTimingAdapter raceTimingAdapter;
    private List<Item> itemList;
    private String[] items;
//    private MiddleRaceResultAdapter raceResultAdapter;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_middle_distance_race;
    }


    private List<Schedule> scheduleList = new ArrayList<>();
    private List<Group> groupList = new ArrayList<>();
    private List<TimingBean> timingLists = new ArrayList<>();
    //    private List<MiddleTimingResultBean> resultDatas;
    private List<List<String>> resultDataList;

    @Override
    protected void initData() {
        initSocket();

        getItems();

        for (int i = 0; i < itemList.size(); i++) {
            items[i] = itemList.get(i).getItemName();
        }

        itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, items);
        spRaceItem.setAdapter(itemAdapter);

        scheduleAdapter = new ScheduleAdapter(this, scheduleList);
        spRaceSchedule.setAdapter(scheduleAdapter);

        //所有组信息recycleView
        groupAdapter = new MiddleRaceGroupAdapter(groupList);
        rvRaceStudentGroup.setLayoutManager(new LinearLayoutManager(this));
        rvRaceStudentGroup.setAdapter(groupAdapter);

        TimingBean timingBean;
        for (int i = 0; i < 3; i++) {
            timingBean = new TimingBean(0, 0, 0, "");
            timingLists.add(timingBean);
        }
        Log.i("timingLists", "---------" + timingLists.size());
        //选中组recycleView
        raceTimingAdapter = new RaceTimingAdapter(this, timingLists, this);
        rvRaceGroup.setLayoutManager(new LinearLayoutManager(this));
        rvRaceGroup.setAdapter(raceTimingAdapter);

        //成绩显示recycleView

        resultDataList = new ArrayList<>();

        List<String> strings = new ArrayList<>();

        strings.add("道次");
        strings.add("姓名");
        strings.add("最终成绩");
        strings.add("第1圈");
        strings.add("第2圈");
        strings.add("第3圈");
        strings.add("第4圈");
        resultDataList.add(strings);

        TestPanelAdapter testPanelAdapter = new TestPanelAdapter(resultDataList);
        scrollablePanel.setPanelAdapter(testPanelAdapter);


        groupAdapter.setOnRecyclerViewItemClickListener(this);

        updateSchedules();
    }

    private void getItems() {
        itemList = DBManager.getInstance().queryItemsByMachineCode(TestConfigs.sCurrentItem.getMachineCode());

        items = new String[itemList.size()];
    }

    /**
     * 获取日程
     */
    private void updateSchedules() {
        scheduleList.clear();
        List<Schedule> dbSchedule = DBManager.getInstance().getAllSchedules();
        scheduleList.addAll(dbSchedule);
        scheduleAdapter.notifyDataSetChanged();
        if (scheduleList != null && scheduleList.size() > 0) {
            scheduleNo = scheduleList.get(0).getScheduleNo();
        }
    }

    /**
     * 获取日程分组
     */
    private void getGroupList() {
        groupList.clear();
        if (scheduleNo == null || scheduleNo.isEmpty()) {
            return;
        }
        List<Group> dbGroupList = DBManager.getInstance().getGroupByScheduleNoAndItem(scheduleNo, itemList.get(mItemPosition).getItemCode());
        groupList.addAll(dbGroupList);
        groupAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title).addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }).addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjectSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjectSetting();
            }
        });
    }

    private void startProjectSetting() {
        IntentUtil.gotoActivityForResult(this, MiddleRaceSettingActivity.class, 1);
    }

    String host = "192.168.0.177";
    int port = 1401;

    //初始化 连接设备
    private void initSocket() {
        nettyClient = new NettyClient(host, port);
        if (!nettyClient.getConnectStatus()) {
            nettyClient.setListener(this);
            nettyClient.connect();
            send();
        } else {
            nettyClient.disconnect();
        }
    }

    private void send() {
        if (!nettyClient.getConnectStatus()) {//获取连接状态，必须连接才能点。
            ToastUtils.showShort("正在连接中");
        } else {
            nettyClient.sendMsgToServer(TcpConfig.CMD_CONNECT, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHander.removeMessages(2);

        //停止计时命令
        nettyClient.sendMsgToServer(TcpConfig.getCmdEndTiming(), this);

        mHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                nettyClient.disconnect();
            }
        }, 1000);
//        UdpClient.getInstance().close();
    }

    @Override
    public void channelInactive() {

    }

    @Override
    public void onDataArrived(UDPResult result) {

    }

    private String currentTime;

    /**
     * 回调客户端接收的信息  解析 数据流
     *
     * @param msg
     */
    @Override
    public void onMessageResponse(final Object msg) {
    }

    @Override
    public void onMessageReceive(long time, final String cardId1, final String cardId2) {
//        for (TimingBean timing : timingLists
//                ) {
//            if (timing.getState() == TimingBean.TIMING_STATE_TIMING) {
//            }
//        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showShort(cardId1 + "---" + cardId2);
                if (cardId1.equals("fd2010f20161101e00010945")) {
                    currentTime = DateUtil.getDeltaT2(startTime);
                    etUdpInput.setText(currentTime);
                } else if (cardId1.equals("fd2010f20161101e00010005")) {
                    currentTime = DateUtil.getDeltaT2(startTime2);
                    etUdpInput2.setText(currentTime);
                }
            }
        });
    }

    @Override
    public void onConnected(String text) {
        mHander.sendMessage(mHander.obtainMessage(1, text));
    }

    @Override
    public void onMessageFailed(Object msg) {

    }

    @Override
    public void onStartTiming(long time) {
        Log.i("onMessageResponse", "开始计时---------------");
        //        // 记录开始的时间ms数
        startTime = System.currentTimeMillis();
        Log.i("timingLists", timingLists.toString());
        mHander.sendEmptyMessage(3);
        for (TimingBean timing : timingLists
                ) {
            //当前处于等待状态的组别开始计时
            if (timing.getState() == TimingBean.TIMING_STATE_WAITING) {
                timing.setTime(time);
            }
        }
        mHander.sendEmptyMessageDelayed(4, 1500);
    }

    @Override
    public void onServiceStatusConnectChanged(final int statusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
                    Log.e(TAG, "STATUS_CONNECT_SUCCESS:");
                    if (nettyClient.getConnectStatus()) {
//                        ToastUtils.showShort("连接成功");
                    }
                } else {
                    Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);
                    if (!nettyClient.getConnectStatus()) {
//                        ToastUtils.showShort("网路不好，正在重连");
                    }
                }
            }
        });
    }

    private int mItemPosition = 0;

    @OnItemSelected({R.id.sp_race_schedule, R.id.sp_race_item, R.id.sp_race_state})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_race_schedule:
                scheduleNo = scheduleList.get(position).getScheduleNo();
                break;
            case R.id.sp_race_item:
                mItemPosition = position;
//                item = itemList.get(position);
                break;
            case R.id.sp_race_state:
                break;
        }
        getGroupList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_start1, R.id.btn_start2, R.id.btn_udp_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start1:
                break;
            case R.id.btn_start2:
                startTime2 = System.currentTimeMillis();
//                mHander.sendEmptyMessage(4);
                break;
            case R.id.btn_udp_send:
                nettyClient.sendMsgToServer(TcpConfig.getCmdStartTiming(), this);
                break;
        }
    }


    /**
     * 点击“等待发令”按钮回调
     */
    @Override
    public void clickTimingWaitListener(int position, final RaceTimingAdapter.VH holder) {
        ToastUtils.showShort(position + "");
        timingLists.get(position).setState(1);
    }

    /**
     * 点击“违规返回”按钮回调
     */
    @Override
    public void clickTimingBackListener(final int position, final RaceTimingAdapter.VH holder) {
        DialogUtil.showCommonDialog(this, "是否违规返回", new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                raceTimingAdapter.notifyBackGround(holder, TimingBean.TIMING_STATE_BACK);
                timingLists.get(position).setState(2);
                raceTimingAdapter.notifyDataSetChanged();
                Log.i("clickTimingBackListener", timingLists.toString());
            }

            @Override
            public void onNegativeClick() {

            }
        });
    }

    /**
     * 点击“完成计时”按钮回调
     */
    @Override
    public void clickTimingCompleteListener(int position, final RaceTimingAdapter.VH holder) {
        DialogUtil.showCommonDialog(this, "是否完成计时", new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                raceTimingAdapter.notifyBackGround(holder, TimingBean.TIMING_STATE_COMPLETE);
            }

            @Override
            public void onNegativeClick() {

            }
        });
    }

    /**
     * 点击删除按钮回调
     */
    @Override
    public void clickTimingDelete(int position) {

    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            Log.i(TAG, "Write successful");
        } else {
            mHander.sendMessage(mHander.obtainMessage(1, "发送指令失败"));
            Log.e(TAG, "Write error");
        }
    }


    @Override
    public void onLongClick(int position) {
        //先判断当前选入的组是否已存在
        for (TimingBean timingBean : timingLists
                ) {
            if (timingBean.getNo() == position + 1) {
                ToastUtils.showShort("该组已存在");
                return;
            }
        }

        ToastUtils.showShort("----" + position + 1);
        groupList.get(position).getGroupNo();
        List<GroupItem> groupItems = DBManager.getInstance().queryGroupItem(groupList.get(position).getItemCode(), groupList.get(position).getGroupNo(), groupList.get(position).getGroupType());

        List<String> strings2;
        for (GroupItem groupItem : groupItems
                ) {
            strings2 = new ArrayList<>();
            strings2.add(groupItem.getTrackNo() + "");
            strings2.add(DBManager.getInstance().queryStudentByStuCode(groupItem.getStudentCode()).getStudentName());
            strings2.add("");
            strings2.add("");
            strings2.add("");
            strings2.add("");
            strings2.add("");
            resultDataList.add(strings2);
        }

        Log.i("resultDataList", resultDataList.toString());
        scrollablePanel.notifyDataSetChanged();

        //从所有组中选入比赛（循环所有已入场的组，当首个出现no为0即空白组时，跳出循环并分配当前选中）
        for (int i = 0; i < timingLists.size(); i++) {
            if (timingLists.get(i).getNo() == 0) {
                timingLists.get(i).setNo(position + 1);//分配组的序号
                String sex = "";
                switch (groupList.get(position).getGroupType()) {
                    case 0:
                        sex = "男子";
                        break;
                    case 1:
                        sex = "女子";
                        break;
                    case 2:
                        sex = "混合";
                        break;
                    default:
                        break;
                }
                timingLists.get(i).setItemGroupName(sex + items[mItemPosition] + "第" + groupList.get(position).getGroupNo() + "组");//组名
                break;
            }
        }
        raceTimingAdapter.notifyDataSetChanged();
    }
}
