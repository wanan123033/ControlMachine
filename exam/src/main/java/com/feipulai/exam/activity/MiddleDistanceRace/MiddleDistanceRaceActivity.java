package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
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
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ColorGroupAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ColorSelectAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.MiddleRaceGroupAdapter;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.RaceTimingAdapter;
import com.feipulai.exam.adapter.ScheduleAdapter;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.ChipGroup;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.view.MiddleRace.ScrollablePanel;
import com.zyyoona7.popup.EasyPopup;

import java.util.ArrayList;
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
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.GROUP_5;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_NOMAL;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_NUMBER;

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
    @BindView(R.id.view_style)
    View viewStyle;
    private String TAG = "MiddleDistanceRaceActivity";
    private final int MESSAGE_A = 1;

    private Handler mHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_A:
                    ToastUtils.showShort(msg.obj.toString());
                    break;
                case 2:
                    send();
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
    private RaceTimingAdapter raceTimingAdapter;
    private List<Item> itemList;
    private String[] items;
    private EasyPopup mCirclePop;
    private TextView tvGroupName;
    private RecyclerView rvColorSelect;
    private Button btnAddColor;
    private Button btnSelectColor;
    private ArrayList<ChipGroup> colorGroups;
    private ColorSelectAdapter colorGroupAdapter;
    private int timers;
    private TestPanelAdapter resultAdapter;

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
        initPopup();

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

        //计时器数量
        timers = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_NUMBER, 3);

        TimingBean timingBean;
        for (int i = 0; i < timers; i++) {
            timingBean = new TimingBean(0, 0, 0, "", 0);
            timingLists.add(timingBean);
        }

        //选中组recycleView
        raceTimingAdapter = new RaceTimingAdapter(this, timingLists, this);
        rvRaceGroup.setLayoutManager(new LinearLayoutManager(this));
        rvRaceGroup.setAdapter(raceTimingAdapter);

        resultDataList = new ArrayList<>();

        //添加标题栏
        RaceResultBean2 raceResultBean = new RaceResultBean2();
        String[] strings = new String[7];
        strings[0] = "道次";
        strings[1] = "姓名";
        strings[2] = "最终成绩";
        for (int i = 3; i < 7; i++) {
            strings[i] = "第" + (i - 2) + "圈";
        }
        raceResultBean.setNo("0");
        raceResultBean.setResults(strings);
        resultDataList.add(raceResultBean);

        resultAdapter = new TestPanelAdapter(resultDataList);
        scrollablePanel.setPanelAdapter(resultAdapter);

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

        // TODO: 2019/6/27 此处待修改
        //循环所有组，将对应的状态（是否有已选入比赛的组别）显示出来
        for (int i = 0; i < groupList.size(); i++) {
            if (groupList.get(i).getIsTestComplete() == GROUP_3) {
                for (TimingBean timing : timingLists
                        ) {
                    if (timing.getNo() == 0) {
                        timing.setNo(i + 1);//分配组的序号
                        timing.setColor(Integer.parseInt(groupList.get(i).getRemark2()));
                        timing.setItemGroupName(groupName);//组名
                        break;
                    }
                }
            }
        }
        Log.i("timingLists","----"+timingLists.toString());
        raceTimingAdapter.notifyDataSetChanged();
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
        }).addRightText("主机参数设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            mHander.sendEmptyMessageDelayed(2, 500);
        } else {
            mHander.sendEmptyMessageDelayed(2, 200);
        }
    }

    //发送连接设备命令
    private void send() {
        nettyClient.sendMsgToServer(TcpConfig.CMD_CONNECT, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!nettyClient.getConnectStatus()) {
            nettyClient.connect();
        }
        mHander.sendEmptyMessageDelayed(2, 300);

        int timers2 = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_NUMBER, 3);

        if (timers2 > timers) {
            TimingBean timingBean;
            for (int i = 0; i < timers2 - timers; i++) {
                timingBean = new TimingBean(0, 0, 0, "",0);
                timingLists.add(timingBean);
            }
            raceTimingAdapter.notifyDataSetChanged();
        } else if (timers2 < timers) {
            timingLists.clear();
            TimingBean timingBean;
            for (int i = 0; i < timers2; i++) {
                timingBean = new TimingBean(0, 0, 0, "",0);
                timingLists.add(timingBean);
            }
            resultDataList.clear();
            scrollablePanel.notifyDataSetChanged();

            //添加标题栏
            RaceResultBean2 raceResultBean = new RaceResultBean2();
            String[] strings = new String[7];
            strings[0] = "道次";
            strings[1] = "姓名";
            strings[2] = "最终成绩";
            for (int i = 3; i < 7; i++) {
                strings[i] = "第" + (i - 2) + "圈";
            }
            raceResultBean.setNo("0");
            raceResultBean.setResults(strings);
            resultDataList.add(0, raceResultBean);

            raceTimingAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        nettyClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止计时命令
//        nettyClient.sendMsgToServer(TcpConfig.getCmdEndTiming(), this);

        nettyClient.disconnect();
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showShort(cardId1 + "---" + cardId2);
                if (cardId1.equals("fd2010f20161101e00010945")) {
//                    currentTime = DateUtil.getDeltaT2(startTime);
                } else if (cardId1.equals("fd2010f20161101e00010005")) {
//                    currentTime = DateUtil.getDeltaT2(startTime2);
                }
            }
        });
    }

    @Override
    public void onConnected(String text) {
        Log.i("onConnected", "onConnected-----------------------");
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
                        viewStyle.setBackgroundResource(R.drawable.blue_circle);
//                        ToastUtils.showShort("连接成功");
                    }
                } else {
                    viewStyle.setBackgroundResource(R.drawable.red_circle);
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

    @OnClick({R.id.btn_udp_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
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
    public void clickTimingDelete(final int position) {
        deleteTiming(position);
    }

    private void deleteTiming(final int position) {
        DialogUtil.showCommonDialog(this, "是否删除当前组", new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                //在成绩显示列删除选中组的所有考生
                Iterator<RaceResultBean2> it = resultDataList.iterator();
                while (it.hasNext()) {
                    String x = it.next().getNo();
                    if (x.equals(timingLists.get(position).getNo() + "")) {
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
                timingLists.get(position).setNo(0);
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
        int width = DisplayUtil.getScreenWidthPx(this);
        mCirclePop = EasyPopup.create()
                .setContentView(this, R.layout.layout_pop_color)
                .setBackgroundDimEnable(true)
                .setDimValue(0.5f)
                //是否允许点击PopupWindow之外的地方消失
                .setFocusAndOutsideEnable(false)
                .setHeight(height * 2 / 3)
                .setWidth(width / 2)
                .apply();
        tvGroupName = mCirclePop.findViewById(R.id.tv_group_name);
        rvColorSelect = mCirclePop.findViewById(R.id.rv_color_select);
        btnAddColor = mCirclePop.findViewById(R.id.btn_color_select_add);
        btnSelectColor = mCirclePop.findViewById(R.id.btn_color_select);

        colorGroups = new ArrayList<>();
        colorGroups.addAll(DBManager.getInstance().queryAllChipGroup());
        colorGroupAdapter = new ColorSelectAdapter(colorGroups);
        rvColorSelect.setLayoutManager(new LinearLayoutManager(this));
        rvColorSelect.setAdapter(colorGroupAdapter);

        colorGroupAdapter.setOnRecyclerViewItemClickListener(this);
        btnAddColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupList.get(groupPosition).setRemark1(colorGroups.get(mColorGroupPosition).getColorGroupName());//备注1字段---颜色组名
                groupList.get(groupPosition).setRemark2(String.valueOf(colorGroups.get(mColorGroupPosition).getColor()));//备注2字段---颜色
                groupAdapter.notifyDataSetChanged();

                addToTiming();
                mCirclePop.dismiss();
            }
        });

        btnSelectColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupList.get(groupPosition).setIsTestComplete(TimingBean.GROUP_4);//关联状态
                groupList.get(groupPosition).setRemark1(colorGroups.get(mColorGroupPosition).getColorGroupName());//备注1字段---颜色组名
                groupList.get(groupPosition).setRemark2(String.valueOf(colorGroups.get(mColorGroupPosition).getColor()));//备注2字段---颜色
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
                        } else if (testComplete == GROUP_5) {
                            ToastUtils.showShort("该组已完成");
                            break;
                        }
                        tvGroupName.setText(groupName);
                        mCirclePop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                        break;
                    case 1:
                        if (testComplete == GROUP_3) {
                            ToastUtils.showShort("该组已选入，不可取消关联");
                            break;
                        } else if (testComplete == GROUP_5) {
                            ToastUtils.showShort("该组已完成");
                            break;
                        }
                        groupList.get(groupPosition).setIsTestComplete(0);
                        groupList.get(groupPosition).setRemark1("");//备注1字段---颜色组名
                        groupList.get(groupPosition).setRemark2("");//备注2字段---颜色
                        groupAdapter.notifyDataSetChanged();
                        DBManager.getInstance().updateGroup(groupList.get(groupPosition));
                        break;
                    case 2:
                        if (testComplete == GROUP_3) {
                            ToastUtils.showShort("该组已存在");
                            break;
                        } else if (testComplete == GROUP_5) {
                            ToastUtils.showShort("该组已完成");
                            break;
                        }
                        addToTiming();
                        break;
                    case 3:
                        if (testComplete == GROUP_4) {
                            ToastUtils.showShort("该组还未选入");
                            break;
                        } else if (testComplete == GROUP_5) {
                            ToastUtils.showShort("该组已完成");
                            break;
                        }
                        for (int i = 0; i < timingLists.size(); i++) {
                            if (timingLists.get(i).getNo() == groupPosition + 1) {
                                deleteTiming(i);
                                break;
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

        //先判断当前选入的组是否已存在
//        for (TimingBean timingBean : timingLists
//                ) {
//            if (timingBean.getNo() == position + 1) {
//                ToastUtils.showShort("该组已存在");
//                return;
//            }
//        }

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

        groupName = sex + items[mItemPosition] + "第" + groupList.get(position).getGroupNo() + "组";

        showListDialog();
    }

    /**
     * 添加到计时器中
     */
    private void addToTiming() {
        List<GroupItem> groupItems = DBManager.getInstance().queryGroupItem(groupList.get(groupPosition).getItemCode(), groupList.get(groupPosition).getGroupNo(), groupList.get(groupPosition).getGroupType());
        String[] strings2;
        RaceResultBean2 raceResultBean;
        for (GroupItem groupItem : groupItems
                ) {
            strings2 = new String[7];
            strings2[0] = groupItem.getTrackNo() + "";
            strings2[1] = DBManager.getInstance().queryStudentByStuCode(groupItem.getStudentCode()).getStudentName();
            raceResultBean = new RaceResultBean2();
            raceResultBean.setResults(strings2);
            raceResultBean.setNo(groupPosition + 1 + "");
            resultDataList.add(raceResultBean);
        }

        Log.i("resultDataList", resultDataList.toString());
        scrollablePanel.notifyDataSetChanged();

        //从所有组中选入比赛（循环所有已入场的组，当首个出现no为0即空白组时，跳出循环并分配当前选中）
        for (int i = 0; i < timingLists.size(); i++) {
            if (timingLists.get(i).getNo() == 0) {
                timingLists.get(i).setNo(groupPosition + 1);//分配组的序号
                timingLists.get(i).setItemGroupName(groupName);//组名
                break;
            }
        }
        raceTimingAdapter.notifyDataSetChanged();

        groupList.get(groupPosition).setIsTestComplete(TimingBean.GROUP_3);
        groupAdapter.notifyDataSetChanged();
        DBManager.getInstance().updateGroup(groupList.get(groupPosition));
    }

    private int mColorGroupPosition;

    @Override
    public void onColorSelectClick(int position) {
        mColorGroupPosition = position;
        colorGroupAdapter.changeBackGround(position);
        colorGroupAdapter.notifyDataSetChanged();
    }
}
