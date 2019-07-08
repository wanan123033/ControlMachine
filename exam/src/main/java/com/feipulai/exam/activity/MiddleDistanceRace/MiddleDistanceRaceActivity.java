package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.common.view.baseToolbar.DisplayUtil;
import com.feipulai.device.tcp.NettyClient;
import com.feipulai.device.tcp.NettyListener;
import com.feipulai.device.tcp.TcpConfig;
import com.feipulai.device.udp.UdpClient;
import com.feipulai.device.udp.result.UDPResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ColorSelectAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.MiddleRaceGroupAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.RaceTimingAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ResultShowAdapter;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.ScheduleAdapter;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.ChipGroup;
import com.feipulai.exam.entity.ChipInfo;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.view.MiddleRace.ScrollablePanel;
import com.feipulai.floatingactionbutton.FloatingActionButton;
import com.feipulai.floatingactionbutton.FloatingActionsMenu;
import com.zyyoona7.popup.EasyPopup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.GROUP_3;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.GROUP_4;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.GROUP_FINISH;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_BACK;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_NOMAL;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_TIMING;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_WAITING;
import static com.feipulai.exam.config.SharedPrefsConfigs.FIRST_TIME;
import static com.feipulai.exam.config.SharedPrefsConfigs.MACHINE_IP;
import static com.feipulai.exam.config.SharedPrefsConfigs.MACHINE_PORT;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_NUMBER;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_TIME_FIRST;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_TIME_SPAN;
import static com.feipulai.exam.config.SharedPrefsConfigs.SPAN_TIME;

public class MiddleDistanceRaceActivity extends BaseTitleActivity implements UdpClient.UDPChannelListerner, NettyListener, RaceTimingAdapter.MyClickListener, ChannelFutureListener, MiddleRaceGroupAdapter.OnItemClickListener, ColorSelectAdapter.OnItemClickListener {

    @BindView(R.id.btn_udp_send)
    Button btnUdpSend;
    @BindView(R.id.sp_race_item)
    Spinner spRaceItem;
    @BindView(R.id.sp_race_state)
    Spinner spRaceState;
    @BindView(R.id.rv_race_student_group)
    RecyclerView rvRaceStudentGroup;
    @BindView(R.id.rv_race_group)
    RecyclerView rvRaceGroup;
    @BindView(R.id.sp_race_schedule)
    Spinner spRaceSchedule;
    @BindView(R.id.race_scrollablePanel)
    ScrollablePanel scrollablePanel;
    //    @BindView(R.id.view_style)
//    View viewStyle;
    @BindView(R.id.ll_show_item)
    LinearLayout llShowItem;
    @BindView(R.id.floatMenu)
    FloatingActionsMenu floatMenu;
    @BindView(R.id.float_button_show)
    FloatingActionButton floatButtonShow;
    @BindView(R.id.float_button_connect)
    FloatingActionButton floatButtonConnect;
    @BindView(R.id.float_button_item_set)
    FloatingActionButton floatButtonItemSet;
    private String TAG = "MiddleDistanceRaceActivity";
    private final int MESSAGE_A = 1;
    private boolean isFlag = true;

    private Handler mHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_A:
                    ToastUtils.showShort(msg.obj.toString());
                    break;
                case 2:
                    if (isConnect) {
                        send();
                        mHander.sendEmptyMessageDelayed(5, 5000);
                    }
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
                case 5:
                    send2();
                    mHander.sendEmptyMessageDelayed(5, 5000);
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
    private RaceTimingAdapter raceTimingAdapter;
    private List<Item> itemList;
    private String[] items;
    private EasyPopup mCirclePop;
    private TextView tvGroupName;
    private RecyclerView rvColorSelect;
    private Button btnAddColor;
    private Button btnSelectColor;
    private List<ChipGroup> colorGroups;
    private ColorSelectAdapter colorGroupAdapter;
    private int timers;
    private ResultShowAdapter resultAdapter;
    private EasyPopup mMachinePop;
    private EditText etIP;
    private EditText etPort;
    private Button btnConnect;
    private Button btnSyncTime;
    private RadioGroup rgVersion;
    private Context mContext;
    private int firstTime;//芯片首次接收时间间隔
    private int spanTime;//芯片接收时间间隔

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_middle_distance_race;
    }


    private List<Schedule> scheduleList = new ArrayList<>();
    private List<Group> groupList = new ArrayList<>();
    private List<TimingBean> timingLists = new ArrayList<>();
    private List<RaceResultBean2> resultDataList;

    @Override
    protected void initData() {
        mContext = this;
        initPopup();

        getItems();

        itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, items);
        spRaceItem.setAdapter(itemAdapter);

        scheduleAdapter = new ScheduleAdapter(this, scheduleList);
        spRaceSchedule.setAdapter(scheduleAdapter);

        //所有组信息recycleView
        groupAdapter = new MiddleRaceGroupAdapter(groupList);
        rvRaceStudentGroup.setLayoutManager(new LinearLayoutManager(this));
        rvRaceStudentGroup.setAdapter(groupAdapter);

        //计时器数量
        timers = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_NUMBER, 3);

        TimingBean timingBean;
        for (int i = 0; i < timers; i++) {
            timingBean = new TimingBean(0, 0, 0, "", "", 0);
            timingLists.add(timingBean);
        }

        //选中组recycleView
        raceTimingAdapter = new RaceTimingAdapter(this, timingLists, this);
        rvRaceGroup.setLayoutManager(new LinearLayoutManager(this));
        rvRaceGroup.setAdapter(raceTimingAdapter);

        resultDataList = new ArrayList<>();

        //添加标题栏
        RaceResultBean2 raceResultBean = new RaceResultBean2();
        String[] strings = new String[cycleNo + 3];
        strings[0] = "道次";
        strings[1] = "姓名";
        strings[2] = "最终成绩";
        for (int i = 3; i < cycleNo + 3; i++) {
            strings[i] = "第" + (i - 2) + "圈";
        }
        raceResultBean.setNo("0");
        raceResultBean.setResults(strings);
        raceResultBean.setCycle(itemList.get(mItemPosition).getCycleNo());
        raceResultBean.setVestNo(0);
        raceResultBean.setItemCode(itemList.get(mItemPosition).getItemCode());
        resultDataList.add(raceResultBean);

        resultAdapter = new ResultShowAdapter(resultDataList);
        scrollablePanel.setPanelAdapter(resultAdapter);

        groupAdapter.setOnRecyclerViewItemClickListener(this);

        updateSchedules();

//        getGroupList();

        initConnectPop();
//        initTiming();

    }

    private int cycleNo = 0;//当前项目圈数

    private void getItems() {
        itemList = DBManager.getInstance().queryItemsByMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        items = new String[itemList.size()];

        for (int i = 0; i < itemList.size(); i++) {
            if (cycleNo < itemList.get(i).getCycleNo()) {
                cycleNo = itemList.get(i).getCycleNo();
            }
        }

        for (int i = 0; i < itemList.size(); i++) {
            items[i] = itemList.get(i).getItemName();
        }
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
     * 初始化已经选入比赛的组别
     */
//    private void initTiming() {
//        //循环所有组，将对应的状态（是否有已选入比赛的组别）显示出来
//        for (int i = 0; i < groupList.size(); i++) {
//            if (groupList.get(i).getIsTestComplete() == GROUP_3) {
//                for (TimingBean timing : timingLists
//                        ) {
//                    if (timing.getNo() == 0) {
//                        groupPosition = i;
//                        getGroupName();//获取组名
//                        timing.setNo(i + 1);//分配组的序号
//                        timing.setColor(Integer.parseInt(groupList.get(i).getColorId()));
//                        timing.setItemGroupName(groupName);//组名
//                        timing.setItemCode(itemList.get(mItemPosition).getItemCode());
//                        addResultList();
//                        break;
//                    }
//                }
//            }
//        }
//        Log.i("timingLists", "----" + timingLists.toString());
//        raceTimingAdapter.notifyDataSetChanged();
//    }

    /**
     * 获取日程分组
     */
    private void getGroupList() {
        groupList.clear();
        if (scheduleNo == null || scheduleNo.isEmpty()) {
            return;
        }
        List<Group> dbGroupList = DBManager.getInstance().getGroupByScheduleNoAndItem(scheduleNo, itemList.get(mItemPosition).getItemCode(), groupStatePosition);
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
                clickBack();
            }
        });
//                .addRightText("项目设置", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startProjectSetting();
//            }
//        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startProjectSetting();
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        clickBack();
    }

    private void clickBack() {
        DialogUtil.showCommonDialog(mContext, "确定退出？", new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                finish();
            }

            @Override
            public void onNegativeClick() {

            }
        });
    }

    /**
     * 连接设备
     */
    private void initConnectPop() {
        String machine_ip = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MACHINE_IP, "");
        String machine_port = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MACHINE_PORT, "0");

        int height = DisplayUtil.getScreenHightPx(this);
        mMachinePop = EasyPopup.create()
                .setContentView(this, R.layout.pop_machine_connect)
                .setBackgroundDimEnable(true)
                .setDimValue(0.5f)
                //是否允许点击PopupWindow之外的地方消失
                .setFocusAndOutsideEnable(true)
                .setHeight(height * 2 / 3)
                .apply();
        etIP = mMachinePop.findViewById(R.id.et_machine_ip);
        etPort = mMachinePop.findViewById(R.id.et_machine_port);
        btnConnect = mMachinePop.findViewById(R.id.btn_connect_machine);
        btnSyncTime = mMachinePop.findViewById(R.id.btn_sync_time);
        rgVersion = mMachinePop.findViewById(R.id.rg_machine_version);

        etIP.setText(machine_ip);
        etIP.setSelection(machine_ip.length());
        etPort.setText(machine_port);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etIP.getText().toString()) || TextUtils.isEmpty(etPort.getText().toString())) {
                    ToastUtils.showShort("IP地址为空");
                    return;
                }
                initSocket(etIP.getText().toString(), Integer.parseInt(etPort.getText().toString()));
            }
        });

        btnSyncTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nettyClient == null) {
                    ToastUtils.showShort("请先连接设备");
                    return;
                }
                nettyClient.sendMsgToServer(TcpConfig.getCmdUpdateDate(), MiddleDistanceRaceActivity.this);
            }
        });
        mMachinePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MACHINE_IP, etIP.getText().toString());
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MACHINE_PORT, etPort.getText().toString());
            }
        });
    }

    private void startProjectSetting() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFlag", !isFlag);
        IntentUtil.gotoActivityForResult(this, MiddleRaceSettingActivity.class, bundle, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2 && requestCode == 1) {
            boolean isChange = data.getBooleanExtra("isChange", false);
            if (isChange) {
                recreate();
            }
        }

    }

    private boolean isConnect = false;//设备是否连接成功
    private boolean isFirst = true;

    //初始化 连接设备
    private void initSocket(String ip, int port) {
        isFirst = true;
        nettyClient = new NettyClient(ip, port);
        if (!nettyClient.getConnectStatus()) {
            nettyClient.setListener(this);
            nettyClient.connect();
            mHander.sendEmptyMessageDelayed(2, 400);
        } else {
            mHander.sendEmptyMessageDelayed(2, 200);
        }
    }

    //发送连接设备命令
    private void send() {
        nettyClient.sendMsgToServer(TcpConfig.CMD_CONNECT, this);
    }

    //随便发送一个东西保持tcp不断
    private void send2() {
        nettyClient.sendMsgToServer(TcpConfig.CMD_NOTHING, this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        firstTime = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_TIME_FIRST, FIRST_TIME);
        spanTime = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_TIME_SPAN, SPAN_TIME);

        getItems();

        getGroupList();

        colorGroups.clear();
        colorGroups.addAll(DBManager.getInstance().queryAllChipGroup());

        if (nettyClient != null && !nettyClient.getConnectStatus()) {
            nettyClient.connect();
            mHander.sendEmptyMessageDelayed(2, 300);
        }

        int timers2 = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_NUMBER, 3);

        if (timers2 > timers) {
            TimingBean timingBean;
            for (int i = 0; i < timers2 - timers; i++) {
                timingBean = new TimingBean(0, 0, 0, "", "", 0);
                timingLists.add(timingBean);
            }
            raceTimingAdapter.notifyDataSetChanged();
        } else if (timers2 < timers) {
            timingLists.clear();
            TimingBean timingBean;
            for (int i = 0; i < timers2; i++) {
                timingBean = new TimingBean(0, 0, 0, "", "", 0);
                timingLists.add(timingBean);
            }
            resultDataList.clear();
            scrollablePanel.notifyDataSetChanged();

            //添加标题栏
            RaceResultBean2 raceResultBean = new RaceResultBean2();
            String[] strings = new String[cycleNo + 3];
            strings[0] = "道次";
            strings[1] = "姓名";
            strings[2] = "最终成绩";
            for (int i = 3; i < cycleNo + 3; i++) {
                strings[i] = "第" + (i - 2) + "圈";
            }
            raceResultBean.setNo("0");
            raceResultBean.setResults(strings);
            raceResultBean.setCycle(itemList.get(mItemPosition).getCycleNo());
            raceResultBean.setVestNo(0);
            raceResultBean.setItemCode(itemList.get(mItemPosition).getItemCode());
            resultDataList.add(0, raceResultBean);
            raceTimingAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (nettyClient != null) {
            mHander.removeMessages(5);
            isFlag = true;
            nettyClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止计时命令
//        nettyClient.sendMsgToServer(TcpConfig.getCmdEndTiming(), this);
        if (nettyClient != null)
            nettyClient.disconnect();

        groupList.clear();
        for (Item item : itemList
                ) {
            if (item.getItemCode() != null) {
                groupList.addAll(DBManager.getInstance().queryGroupByItemCode(item.getItemCode()));
            }
        }

        for (Group group : groupList
                ) {
            if (group.getIsTestComplete() == TimingBean.GROUP_3) {
                group.setIsTestComplete(TimingBean.GROUP_4);
            }
        }
        DBManager.getInstance().updateGroups(groupList);
//        UdpClient.getInstance().close();
    }

    @Override
    public void channelInactive() {

    }

    @Override
    public void onDataArrived(UDPResult result) {

    }

    /**
     * 回调客户端接收的信息  解析 数据流
     *
     * @param msg
     */
    @Override
    public void onMessageResponse(final Object msg) {

    }

    @Override
    public void onMessageReceive(long time, final String[] cardIds) {
        for (String card : cardIds
                ) {
            ChipInfo chipInfo = DBManager.getInstance().queryChipInfo(card);
            if (chipInfo == null) {
                continue;
            }
            for (int i = 0; i < resultDataList.size(); i++) {
                if (resultDataList.get(i).getColor() == chipInfo.getColor() && resultDataList.get(i).getVestNo() == chipInfo.getVestNo()) {
                    String[] result = resultDataList.get(i).getResults();
                    if (resultDataList.get(i).getStartTime() == 0) {
                        break;
                    }
                    String usedTime = String.valueOf(time - resultDataList.get(i).getStartTime());
                    for (int j = 0; j < resultDataList.get(i).getCycle(); j++) {
                        if (TextUtils.isEmpty(result[j + 3])) {
                            if (j == 0) {
                                if (Long.parseLong(usedTime) < firstTime * 1000) {
                                    Log.i("firstTime--------", "usedTime:" + usedTime);
                                    break;
                                }
                            } else {
                                if ((time - resultDataList.get(i).getStartTime() - Long.parseLong(result[j + 2])) < spanTime * 1000) {
                                    Log.i("spanTime--------", "usedTime:" + (time - Long.parseLong(result[j + 2])));
                                    break;
                                }
                            }
                            result[j + 3] = usedTime;
                            if (j == resultDataList.get(i).getCycle() - 1) {
                                result[2] = usedTime;
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scrollablePanel.notifyDataSetChanged();
                ToastUtils.showShort(Arrays.toString(cardIds));
            }
        });
    }

    @Override
    public void onConnected(String text) {
        isConnect = true;
        isFirst = false;
        Log.i("onConnected", "onConnected-----------------------");
        if (isFlag) {
            mHander.sendMessage(mHander.obtainMessage(1, text));
            isFlag = false;
        }
    }

    @Override
    public void onMessageFailed(Object msg) {

    }

    @Override
    public void onStartTiming(long time) {
        Log.i("onMessageResponse", "开始计时---------" + time);
        Log.i("timingLists", timingLists.toString());
        mHander.sendEmptyMessage(3);
        for (TimingBean timing : timingLists
                ) {
            //当前处于等待状态的组别开始计时
            if (timing.getState() == TIMING_STATE_WAITING) {
                timing.setTime(time);
                //将开始时间保存到每个人的数据中
                for (RaceResultBean2 raceResultBean2 : resultDataList
                        ) {
                    if (raceResultBean2.getColor() == timing.getColor()) {
                        raceResultBean2.setStartTime(time);
                    }
                }
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
                    isConnect = true;
                    mHander.sendEmptyMessage(2);
                    Log.e(TAG, "STATUS_CONNECT_SUCCESS:");
                    floatMenu.setNormalColor(getResources().getColor(R.color.green_yellow));
                } else {
                    if (isFirst) {
                        ToastUtils.showShort("连接失败");
                        isFirst = false;
                    }
                    isConnect = false;
                    floatMenu.setNormalColor(getResources().getColor(R.color.Red));
                    Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);
                }
            }
        });
    }

    private int mItemPosition = 0;
    private int groupStatePosition = 0;

    @OnItemSelected({R.id.sp_race_schedule, R.id.sp_race_item, R.id.sp_race_state})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_race_schedule:
                scheduleNo = scheduleList.get(position).getScheduleNo();
                break;
            case R.id.sp_race_item:
                mItemPosition = position;
                break;
            case R.id.sp_race_state:
                groupStatePosition = position;
                break;
        }
        getGroupList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    private boolean isShow = true;

    @OnClick({R.id.btn_udp_send, R.id.float_button_show, R.id.float_button_connect, R.id.float_button_item_set})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_udp_send:
                nettyClient.sendMsgToServer(TcpConfig.getCmdStartTiming(), this);
                break;
            case R.id.float_button_show:
                isShow = !isShow;
                if (isShow) {
                    floatButtonShow.setTitle("隐藏项目组");
                    floatButtonShow.setIcon(R.drawable.sign_out);
                    llShowItem.setVisibility(View.VISIBLE);
                } else {
                    floatButtonShow.setTitle("显示项目组");
                    floatButtonShow.setIcon(R.drawable.sign_in);
                    llShowItem.setVisibility(View.GONE);
                }
                floatMenu.collapse();
                break;
            case R.id.float_button_connect:
                mMachinePop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                floatMenu.collapse();
                break;
            case R.id.float_button_item_set:
                startProjectSetting();
                floatMenu.collapse();
                break;
        }
    }


    /**
     * 点击“等待发令”按钮回调
     */
    @Override
    public void clickTimingWaitListener(int position, final RaceTimingAdapter.VH holder) {
        if (!isConnect) {
            ToastUtils.showShort("请先连接设备");
            return;
        }

        if (timingLists.get(position).getNo() != 0) {
            raceTimingAdapter.notifyBackGround(holder, TIMING_STATE_WAITING);
        } else {
            ToastUtils.showShort("请先选入组别");
            return;
        }
        timingLists.get(position).setState(TIMING_STATE_WAITING);
        //点击等待，须清除当前等待组的以往成绩
        for (RaceResultBean2 resultBean2 : resultDataList
                ) {
            if (resultBean2.getColor() == timingLists.get(position).getColor()) {
                resultBean2.setStartTime(0);
                String[] result = resultBean2.getResults();
                for (int i = 0; i < result.length; i++) {
                    if (i > 1 && !TextUtils.isEmpty(result[i])) {
                        result[i] = "";
                    }
                }
            }
        }
        scrollablePanel.notifyDataSetChanged();
    }

    /**
     * 点击“违规返回”按钮回调
     */
    @Override
    public void clickTimingBackListener(final int position, final RaceTimingAdapter.VH holder) {
        DialogUtil.showCommonDialog(this, "是否违规返回", new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                raceTimingAdapter.notifyBackGround(holder, TIMING_STATE_BACK);
                timingLists.get(position).setState(TIMING_STATE_BACK);
                raceTimingAdapter.notifyDataSetChanged();
                //初始化开始时间
                for (RaceResultBean2 resultBean2 : resultDataList
                        ) {
                    if (resultBean2.getColor() == timingLists.get(position).getColor()) {
                        resultBean2.setStartTime(0);
                    }
                }
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
    public void clickTimingCompleteListener(final int position, final RaceTimingAdapter.VH holder) {
        DialogUtil.showCommonDialog(this, "是否完成计时", new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                timingLists.get(position).setState(TimingBean.TIMING_STATE_COMPLETE);
                raceTimingAdapter.notifyBackGround(holder, TimingBean.TIMING_STATE_COMPLETE);
                raceTimingAdapter.notifyDataSetChanged();

                String scheduleNo = "";
                //更新项目组状态
                for (Group group : groupList
                        ) {
                    if (!TextUtils.isEmpty(group.getColorId()) && timingLists.get(position).getColor() == Integer.parseInt(group.getColorId())) {
                        group.setIsTestComplete(GROUP_FINISH);
                        DBManager.getInstance().updateGroup(group);
                        groupAdapter.notifyDataSetChanged();
                        scheduleNo = group.getScheduleNo();
                        break;
                    }
                }

                List<RoundResult> roundResults = new ArrayList<>();
                RoundResult roundResult;

                for (RaceResultBean2 resultBean2 : resultDataList
                        ) {
                    if (resultBean2.getColor() == timingLists.get(position).getColor()) {
                        //初始化开始时间
                        resultBean2.setStartTime(0);

                        int[] resultInts = new int[resultBean2.getCycle()];
                        String[] result = resultBean2.getResults();
                        for (int i = 3; i < result.length; i++) {
                            if (TextUtils.isEmpty(result[i])) {
                                break;
                            } else {
                                resultInts[i - 3] = Integer.parseInt(result[i]);
                            }
                        }
                        int lastResult = Integer.parseInt(TextUtils.isEmpty(result[2]) ? "0" : result[2]);
                        // TODO: 2019/7/5 成绩保存及上传
                        roundResult = new RoundResult();
                        roundResult.setItemCode(resultBean2.getItemCode());
                        roundResult.setMachineCode(machineCode);
                        roundResult.setResult(lastResult);
                        roundResult.setRoundNo(1);
                        roundResult.setStudentCode(resultBean2.getStudentCode());
                        roundResult.setTestNo(1);
                        roundResult.setIsLastResult(1);
                        roundResult.setMachineResult(lastResult);
                        roundResult.setScheduleNo(scheduleNo);
                        roundResult.setResultState(1);

                        byte[] cycleResult = DataUtil.byteArray2RgbArray(resultInts);
                        roundResult.setCycleResult(cycleResult);
                        roundResults.add(roundResult);
                    }
                }
                DBManager.getInstance().insertRoundResults(roundResults);

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
    public void clickTimingDelete(final int position) {
        if (timingLists.get(position).getNo() == 0) {
            return;
        }
        deleteTiming(position);
    }

    private void deleteTiming(final int position) {
        if (timingLists.get(position).getState() == TIMING_STATE_WAITING || timingLists.get(position).getState() == TIMING_STATE_TIMING) {
            ToastUtils.showShort("该状态下不能取消比赛");
            return;
        }
        DialogUtil.showCommonDialog(this, "是否删除当前组", new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                for (int i = 0; i < itemList.size(); i++) {
                    if (itemList.get(i).getItemCode().equals(timingLists.get(position).getItemCode())) {
                        spRaceItem.setSelection(i);
                        break;
                    }
                }

                //在成绩显示列删除选中组的所有考生
                Iterator<RaceResultBean2> it = resultDataList.iterator();
                while (it.hasNext()) {
                    RaceResultBean2 raceResultBean2 = it.next();
                    String x = raceResultBean2.getNo();
                    String y = raceResultBean2.getItemCode();
                    if (y.equals(itemList.get(mItemPosition).getItemCode()) && x.equals(timingLists.get(position).getNo() + "")) {
                        it.remove();
                    }
                }

                scrollablePanel.notifyDataSetChanged();

                for (int i = 0; i < groupList.size(); i++) {
                    if (i + 1 == timingLists.get(position).getNo()) {
                        groupList.get(i).setIsTestComplete(TimingBean.GROUP_4);
                        groupAdapter.notifyDataSetChanged();
                        break;
                    }
                }

                //要删除的组恢复默认状态
                timingLists.get(position).setState(TIMING_STATE_NOMAL);
                timingLists.get(position).setItemGroupName("");
                timingLists.get(position).setColor(0);
                timingLists.get(position).setNo(0);
                timingLists.get(position).setItemCode("");
                timingLists.get(position).setTime(0);
                raceTimingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNegativeClick() {

            }
        });
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

    private void initPopup() {
        int height = DisplayUtil.getScreenHightPx(this);
        mCirclePop = EasyPopup.create()
                .setContentView(this, R.layout.layout_pop_color)
                .setBackgroundDimEnable(true)
                .setDimValue(0.5f)
                //是否允许点击PopupWindow之外的地方消失
                .setFocusAndOutsideEnable(true)
                .setHeight(height * 2 / 3)
                .apply();
        tvGroupName = mCirclePop.findViewById(R.id.tv_group_name);
        rvColorSelect = mCirclePop.findViewById(R.id.rv_color_select);
        btnAddColor = mCirclePop.findViewById(R.id.btn_color_select_add);
        btnSelectColor = mCirclePop.findViewById(R.id.btn_color_select);

        colorGroups = new ArrayList<>();
        colorGroupAdapter = new ColorSelectAdapter(colorGroups);
        rvColorSelect.setLayoutManager(new LinearLayoutManager(this));
        rvColorSelect.setAdapter(colorGroupAdapter);

        colorGroupAdapter.setOnRecyclerViewItemClickListener(this);
        btnAddColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupList.get(groupPosition).setColorGroupName(colorGroups.get(mColorGroupPosition).getColorGroupName());//备注1字段---颜色组名
                groupList.get(groupPosition).setColorId(String.valueOf(colorGroups.get(mColorGroupPosition).getColor()));//备注2字段---颜色
                groupAdapter.notifyDataSetChanged();

                addToTiming();
                mCirclePop.dismiss();
            }
        });

        btnSelectColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupList.get(groupPosition).setIsTestComplete(TimingBean.GROUP_4);//关联状态
                groupList.get(groupPosition).setColorGroupName(colorGroups.get(mColorGroupPosition).getColorGroupName());//备注1字段---颜色组名
                groupList.get(groupPosition).setColorId(String.valueOf(colorGroups.get(mColorGroupPosition).getColor()));//备注2字段---颜色
                groupAdapter.notifyDataSetChanged();

                DBManager.getInstance().updateGroup(groupList.get(groupPosition));
                mCirclePop.dismiss();
            }
        });
    }

    /**
     * 长按组别item弹出下列选项
     */
    private void showListDialog() {
        final String[] items = {"关联颜色", "取消关联", "选入比赛", "取消选入"};
        final AlertDialog.Builder listDialog = new AlertDialog.Builder(this);
        listDialog.setTitle(groupName);
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int testComplete = groupList.get(groupPosition).getIsTestComplete();
                switch (which) {
                    case 0:
                        if (testComplete == GROUP_3) {
                            ToastUtils.showShort("该组已选入");
                            break;
                        } else if (testComplete == GROUP_FINISH) {
                            ToastUtils.showShort("该组已完成");
                            break;
                        }
                        colorGroupAdapter.notifyDataSetChanged();
                        tvGroupName.setText(groupName);
                        mCirclePop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                        break;
                    case 1:
                        if (testComplete == GROUP_3) {
                            ToastUtils.showShort("该组已选入，不可取消关联");
                            break;
                        } else if (testComplete == GROUP_FINISH) {
                            ToastUtils.showShort("该组已完成");
                            break;
                        }
                        groupList.get(groupPosition).setIsTestComplete(0);
                        groupList.get(groupPosition).setColorGroupName("");//备注1字段---颜色组名
                        groupList.get(groupPosition).setColorId("");//备注2字段---颜色
                        groupAdapter.notifyDataSetChanged();
                        DBManager.getInstance().updateGroup(groupList.get(groupPosition));
                        break;
                    case 2:
                        if (testComplete == GROUP_3) {
                            ToastUtils.showShort("该组已存在");
                        } else if (testComplete == GROUP_FINISH) {
                            ToastUtils.showShort("该组已完成");
                        } else if (testComplete == GROUP_4) {
                            addToTiming();
                        } else {
                            ToastUtils.showShort("请先关联颜色组");
                        }
                        break;
                    case 3:
                        if (testComplete == GROUP_4) {
                            ToastUtils.showShort("该组还未选入");
                            break;
                        } else if (testComplete == GROUP_FINISH) {
                            ToastUtils.showShort("该组已完成");
                            break;
                        } else if (testComplete == GROUP_3) {
                            for (int i = 0; i < timingLists.size(); i++) {
                                if (timingLists.get(i).getNo() == groupPosition + 1) {
                                    deleteTiming(i);
                                    break;
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        listDialog.show();
    }


    private String groupName = "";
    private int groupPosition;

    @Override
    public void onMiddleRaceGroupLongClick(int position) {
        groupPosition = position;

        getGroupName();

        showListDialog();
    }

    /**
     * 获取项目全称
     */
    private void getGroupName() {
        String sex = "";
        switch (groupList.get(groupPosition).getGroupType()) {
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
        groupName = sex + items[mItemPosition] + "第" + groupList.get(groupPosition).getGroupNo() + "组";
    }

    /**
     * 添加到计时器中
     */
    private void addToTiming() {
        for (TimingBean timing : timingLists
                ) {
            if (timing.getColor() == Integer.parseInt(groupList.get(groupPosition).getColorId())) {
                ToastUtils.showShort("当前颜色组已存在");
                return;
            }
        }

        addResultList();

        //从所有组中选入比赛（循环所有已入场的组，当首个出现no为0即空白组时，跳出循环并分配当前选中）
        for (int i = 0; i < timingLists.size(); i++) {
            if (timingLists.get(i).getNo() == 0) {
                timingLists.get(i).setNo(groupPosition + 1);//分配组的序号
                timingLists.get(i).setItemGroupName(groupName);//组名
                timingLists.get(i).setItemCode(itemList.get(mItemPosition).getItemCode());
                timingLists.get(i).setColor(Integer.parseInt(groupList.get(groupPosition).getColorId()));
                break;
            }
        }
        raceTimingAdapter.notifyDataSetChanged();

        groupList.get(groupPosition).setIsTestComplete(TimingBean.GROUP_3);
        groupAdapter.notifyDataSetChanged();
        DBManager.getInstance().updateGroup(groupList.get(groupPosition));
    }

    /**
     * 添加已选入比赛组的所有人到成绩显示列表
     */
    private void addResultList() {
        List<GroupItem> groupItems = DBManager.getInstance().queryGroupItem(groupList.get(groupPosition).getItemCode(), groupList.get(groupPosition).getGroupNo(), groupList.get(groupPosition).getGroupType());
        String[] strings2;
        RaceResultBean2 raceResultBean;
        for (int i = 0; i < groupItems.size(); i++) {
            Student student = DBManager.getInstance().queryStudentByStuCode(groupItems.get(i).getStudentCode());
            strings2 = new String[cycleNo + 3];
            strings2[0] = groupItems.get(i).getTrackNo() + "";
            strings2[1] = student.getStudentName();
            raceResultBean = new RaceResultBean2();
            raceResultBean.setResults(strings2);
            raceResultBean.setNo(groupPosition + 1 + "");
            raceResultBean.setVestNo(i + 1);
            raceResultBean.setStudentCode(student.getStudentCode());
            raceResultBean.setStudentName(student.getStudentName());
            raceResultBean.setColor(Integer.parseInt(groupList.get(groupPosition).getColorId()));
            raceResultBean.setCycle(itemList.get(mItemPosition).getCycleNo());
            raceResultBean.setItemCode(itemList.get(mItemPosition).getItemCode());
            resultDataList.add(raceResultBean);
        }

        scrollablePanel.notifyDataSetChanged();
    }

    private int mColorGroupPosition;

    @Override
    public void onColorSelectClick(int position) {
        mColorGroupPosition = position;
        colorGroupAdapter.changeBackGround(position);
        colorGroupAdapter.notifyDataSetChanged();
    }
}
