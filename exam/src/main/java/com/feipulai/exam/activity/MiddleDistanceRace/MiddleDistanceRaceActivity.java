package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.DisplayUtil;
import com.feipulai.device.tcp.NettyClient;
import com.feipulai.device.tcp.NettyListener;
import com.feipulai.device.tcp.TcpConfig;
import com.feipulai.device.udp.UdpClient;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.device.udp.result.UDPResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ColorSelectAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.MiddleRaceGroupAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.RaceTimingAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.SelectResultAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.GroupItemBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.RaceResultBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.SelectResultBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.ServiceTcpBean;
import com.feipulai.exam.activity.MiddleDistanceRace.vhtableview.VHTableAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.vhtableview.VHTableView;
import com.feipulai.exam.activity.base.MiddleBaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.ScheduleAdapter;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.SharedPrefsConfigs;
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
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.view.MiddleRace.ScrollablePanel;
import com.orhanobut.logger.Logger;
import com.zyyoona7.popup.EasyPopup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.GROUP_3;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.GROUP_4;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.GROUP_FINISH;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.GROUP_TIMING;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.GROUP_WAIT;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_START;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_BACK;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_NOMAL;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_TIMING;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_WAITING;
import static com.feipulai.exam.config.SharedPrefsConfigs.FIRST_TIME;
import static com.feipulai.exam.config.SharedPrefsConfigs.MACHINE_IP;
import static com.feipulai.exam.config.SharedPrefsConfigs.MACHINE_PORT;
import static com.feipulai.exam.config.SharedPrefsConfigs.MACHINE_SERVER_PORT;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_CARRY;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_DIGITAL;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_NUMBER;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_TIME_FIRST;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_TIME_SPAN;
import static com.feipulai.exam.config.SharedPrefsConfigs.SPAN_TIME;

public class MiddleDistanceRaceActivity extends MiddleBaseTitleActivity implements UdpClient.UDPChannelListerner, NettyListener, RaceTimingAdapter.MyClickListener, ChannelFutureListener, MiddleRaceGroupAdapter.OnItemClickListener, ColorSelectAdapter.OnItemClickListener, AdapterView.OnItemSelectedListener {

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
    @BindView(R.id.ll_show_item)
    LinearLayout llShowItem;
    @BindView(R.id.btn_find)
    Button btnFind;
    @BindView(R.id.btn_middle_back)
    Button btnMiddleBack;
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.btn_setting)
    ImageTextButton btnSetting;
    @BindView(R.id.btn_fullscreen)
    ImageTextButton btnFullscreen;
    @BindView(R.id.btn_image_connect)
    ImageTextButton imageConnect;
    @BindView(R.id.view_connect_state)
    View viewConnectState;
    @BindView(R.id.vht_table)
    VHTableView resultShowTable;
    private String TAG = "MiddleDistanceRaceActivity";
    private final int MESSAGE_A = 1;
    private boolean isFlag = true;
    private int mItemPosition = 0;
    private int groupStatePosition = 0;
    private int schedulePosition = 0;
    private boolean isAutoSelect = true;//spinner初始化标识，用以禁止重复多次执行

    private Handler mHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_A:
                    ToastUtils.showShort(msg.obj.toString());
                    break;
                case 2:
                    if (isConnect) {
                        sendConnect();
                    }
                    break;
                case 3:
//                    raceTimingAdapter.notifyDataSetChanged();
                    raceTimingAdapter.notifyItemChanged((Integer) msg.obj);
                    break;
                case 4:
                    for (TimingBean timingBean : timingLists
                            ) {
                        //开始计时之后，所有启动状态的计时器需要改变为正在计时状态
                        if (timingBean.getState() == TIMING_START) {
                            timingBean.setState(TIMING_STATE_TIMING);
                        }
                    }
                    Log.i("timingLists", timingLists.toString());
                    break;
                case 5:
                    send2();
                    mHander.sendEmptyMessageDelayed(5, 8000);
                    break;
                case 6:
                    sendDisConnect();
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
    //    private ResultShowAdapter resultAdapter;
    private EasyPopup mMachinePop;
    private EditText etIP;
    private EditText etPort;
    private Button btnConnect;
    private Button btnSyncTime;
    private RadioGroup rgVersion;
    private Context mContext;
    private int firstTime;//芯片首次接收时间间隔
    private int spanTime;//芯片接收时间间隔
    private int height;
    private EasyPopup mSelectPop;
    private ScrollablePanel resultScroll;
    private int carryMode;
    private int digital;
    private boolean isChange = false;
    public static MiddleDistanceRaceActivity instance;
    private int width;
    private String machine_ip;
    private String machine_port;
    private String server_Port;
    private Intent bindIntent;
    private long lastServiceTime;
    private Button btndisConnect;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_middle_distance_race3;
    }


    private List<Schedule> scheduleList = new ArrayList<>();
    private List<TimingBean> timingLists = new ArrayList<>();
    private ArrayList<RaceResultBean> resultDataList;
    private List<GroupItemBean> groupItemBeans = new ArrayList<>();
    private ArrayList<String> titleData = new ArrayList<>();
    private boolean isBind = false;

    @Override
    protected void initData() {
        mContext = this;
        instance = this;

        height = DisplayUtil.getScreenHightPx(this);
        width = DisplayUtil.getScreenWidthPx(this);

        btnSetting.setText("设置");
        btnSetting.setImgResource(R.drawable.btn_setting_selecor);
        imageConnect.setText("连接设备");
        imageConnect.setImgResource(R.drawable.btn_connect_selecor);
        btnFullscreen.setText("隐藏组别");
        btnFullscreen.setImgResource(R.drawable.btn_fullscreen_selecor);


        schedulePosition = getIntent().getIntExtra("schedulePosition", 0);
        mItemPosition = getIntent().getIntExtra("mItemPosition", 0);
        groupStatePosition = getIntent().getIntExtra("groupStatePosition", 0);

        Item item = TestConfigs.sCurrentItem;
        carryMode = item.getCarryMode();
        digital = item.getDigital() == 0 ? 1 : item.getDigital();
        carryMode = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_CARRY, carryMode);
        digital = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_DIGITAL, digital);

        initPopup();

        getItems();
        itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, items);
        spRaceItem.setAdapter(itemAdapter);
        spRaceItem.setSelection(mItemPosition, false);
        spRaceItem.setOnItemSelectedListener(this);

        scheduleAdapter = new ScheduleAdapter(this, scheduleList);
        spRaceSchedule.setAdapter(scheduleAdapter);
        spRaceSchedule.setSelection(schedulePosition, false);
        spRaceSchedule.setOnItemSelectedListener(this);

        spRaceState.setSelection(groupStatePosition, false);
        spRaceState.setOnItemSelectedListener(this);

        machine_ip = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MACHINE_IP, "");
        machine_port = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MACHINE_PORT, "1401");
        server_Port = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MACHINE_SERVER_PORT, "4040");
        lastServiceTime = SharedPrefsUtil.getValue(mContext, MyTcpService.SERVICE_CONNECT, MyTcpService.SERVICE_CONNECT, 0L);
        //所有组信息recycleView
        groupAdapter = new MiddleRaceGroupAdapter(groupItemBeans);
        rvRaceStudentGroup.setLayoutManager(new LinearLayoutManager(this));
        rvRaceStudentGroup.setAdapter(groupAdapter);

        //计时器数量
        timers = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_NUMBER, 3);

        TimingBean timingBean;
        for (int i = 0; i < timers; i++) {
            timingBean = new TimingBean(0, 0, 0, "", "", 0, 0);
            timingLists.add(timingBean);
        }

        //选中组recycleView
        raceTimingAdapter = new RaceTimingAdapter(this, timingLists, this);
        rvRaceGroup.setLayoutManager(new LinearLayoutManager(this));
        rvRaceGroup.setAdapter(raceTimingAdapter);

        resultDataList = new ArrayList<>();

        //添加标题栏
        titleData.add("道次");
        titleData.add("姓名");
        titleData.add("最终成绩");
        for (int i = 3; i < cycleNo + 3; i++) {
            titleData.add("第" + (i - 2) + "圈");
        }

        final VHTableAdapter tableShowAdapter = new VHTableAdapter(this, titleData, resultDataList, carryMode, digital, new VHTableAdapter.OnResultItemLongClick() {
            @Override
            public void resultListLongClick(int row, int state) {
                Log.i("resultListLongClick", row + "---" + state);
                resultDataList.get(row).setResultState(state);
                resultShowTable.notifyContent();
            }
        });

        resultShowTable.setAdapter(tableShowAdapter);

        groupAdapter.setOnRecyclerViewItemClickListener(this);

        updateSchedules();

        getGroupList();

        //连接设备弹窗
        initConnectPop();

        initSelectPop();

        //当上一次服务开启时间小于12小时，每次进入自动开启服务
        if (!TextUtils.isEmpty(server_Port) && lastServiceTime != 0 && (System.currentTimeMillis() - lastServiceTime) < 12 * 60 * 60 * 1000) {
            currentPort = Integer.parseInt(server_Port);
            bindTcpService();
        }
    }

    private int cycleNo = 0;//当前项目圈数

    private void getItems() {
        itemList = DBManager.getInstance().queryItemsByMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        items = new String[itemList.size()];

        for (int i = 0; i < itemList.size(); i++) {
            String cycle = SharedPrefsUtil.getValue(mContext, SharedPrefsConfigs.DEFAULT_PREFS, itemList.get(i).getItemName(), "0");
            if (cycleNo < Integer.parseInt(cycle)) {
                cycleNo = Integer.parseInt(cycle);
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
        List<Schedule> dbSchedule = DBManager.getInstance().queryCurrentSchedules();
        scheduleList.addAll(dbSchedule);
        scheduleAdapter.notifyDataSetChanged();

        if (scheduleList != null && scheduleList.size() > 0) {
            scheduleNo = scheduleList.get(schedulePosition).getScheduleNo();
        }
        spRaceSchedule.setSelection(schedulePosition, false);
    }

    /**
     * 获取日程分组
     */
    private void getGroupList() {
        Log.i("spinnerItemSelected", "getGroupList------------");
        groupItemBeans.clear();
        if (scheduleNo == null || scheduleNo.isEmpty()) {
            return;
        }

        final String itemCode = itemList.get(mItemPosition).getItemCode();
        if (TextUtils.isEmpty(itemCode)) {
            ToastUtils.showShort("项目代码为空");
            groupItemBeans.clear();
            groupAdapter.notifyDataSetChanged();
        } else {
            DataBaseExecutor.addTask(new DataBaseTask(mContext, getString(R.string.loading_hint), true) {
                @Override
                public DataBaseRespon executeOper() {
                    List<Group> dbGroupList = DBManager.getInstance().getGroupByScheduleNoAndItem(scheduleNo, itemCode, groupStatePosition);
                    for (Group group : dbGroupList
                            ) {
                        String sex = "";
                        switch (group.getGroupType()) {
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
                        String itemGroupName = sex + itemList.get(mItemPosition).getItemName() + "第" + group.getGroupNo() + "组";
                        List<GroupItem> groupItems = DBManager.getInstance().queryGroupItem(group.getScheduleNo(), group.getItemCode(), group.getGroupNo(), group.getGroupType());
                        groupItemBeans.add(new GroupItemBean(group, groupItems, itemGroupName, itemList.get(mItemPosition).getItemName()));
                    }
                    return new DataBaseRespon(true, "", null);
                }

                @Override
                public void onExecuteSuccess(DataBaseRespon respon) {
                    groupAdapter.notifyDataSetChanged();
                    rvRaceStudentGroup.scrollToPosition(0);
                }

                @Override
                public void onExecuteFail(DataBaseRespon respon) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        for (TimingBean timingBean : timingLists
                ) {
            if (timingBean.getState() == TIMING_STATE_WAITING || timingBean.getState() == TIMING_STATE_TIMING) {
                ToastUtils.showShort("考试中，请勿退出");
                return;
            }
        }
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

    private MyTcpService.Work myBinder;
    private MyTcpService myTcpService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MyTcpService.Work) service;
            myTcpService = myBinder.getMyService();

            myTcpService.registerCallBack(callBack);

            if (myBinder != null && !myTcpService.isWork) {
                myBinder.startWork(currentPort);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (myTcpService != null) {
                myTcpService.unRegisterCallBack(callBack);
            }
        }
    };

    private MyTcpService.CallBack callBack = new MyTcpService.CallBack() {
        @Override
        public void postMessage(final ServiceTcpBean message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getItems();
                    ToastUtils.showLong("上道终端上传分组数据");
                    for (int i = 0; i < itemList.size(); i++) {
                        if (itemList.get(i).getItemName().equals(message.getItemName())) {
                            spRaceItem.setSelection(i);
                            mItemPosition = i;
                            break;
                        } else {
                            if (i == itemList.size() - 1) {
                                itemAdapter.notifyDataSetChanged();
                                for (int j = 0; j < itemList.size(); i++) {
                                    if (itemList.get(j).getItemName().equals(message.getItemName())) {
                                        mItemPosition = j;
                                        spRaceItem.setSelection(j);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (scheduleList.size() == 0) {
                        updateSchedules();
                    } else {
                        for (int i = 0; i < scheduleList.size(); i++) {
                            if (scheduleList.get(i).getScheduleNo().equals(message.getSchedule().getScheduleNo())) {
                                spRaceSchedule.setSelection(i);
                                schedulePosition = i;
                                break;
                            } else {
                                if (i == scheduleList.size() - 1) {
                                    updateSchedules();
                                    for (int j = 0; j < scheduleList.size(); j++) {
                                        if (scheduleList.get(i).getScheduleNo().equals(message.getSchedule().getScheduleNo())) {
                                            spRaceSchedule.setSelection(j);
                                            schedulePosition = j;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    getGroupList();
                }
            });
        }

        @Override
        public void postConnectMessage(final String info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort(info);
                }
            });
        }
    };

    private int currentPort;

    /**
     * 连接设备
     */
    private void initConnectPop() {
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
        btndisConnect = mMachinePop.findViewById(R.id.btn_disconnect_machine);
        btnSyncTime = mMachinePop.findViewById(R.id.btn_sync_time);
        Button btnStart = mMachinePop.findViewById(R.id.btn_start_server);
        rgVersion = mMachinePop.findViewById(R.id.rg_machine_version);
        TextView serverIP = mMachinePop.findViewById(R.id.tv_server_ip);
        final EditText serverPort = mMachinePop.findViewById(R.id.et_server_port);

        etIP.setText(machine_ip);
        etIP.setSelection(machine_ip.length());
        etPort.setText(machine_port);
        serverIP.setText("服务器：" + NetWorkUtils.getLocalIp());
        serverPort.setText(server_Port);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(serverPort.getText())) {
                    ToastUtils.showShort("请先设置端口");
                    return;
                }
                currentPort = Integer.parseInt(serverPort.getText().toString());
                //当端口改变重启服务端
                if (!server_Port.equals(serverPort.getText().toString())) {
                    myBinder.stopWork();
                    myBinder.startWork(currentPort);
                }

                if (myTcpService != null && myTcpService.isWork) {
                    ToastUtils.showShort("当前服务已开启");
                    return;
                }
                bindTcpService();
                //存储当前开启服务的时间，间隔12小时每次进入当前activity自动打开服务，超过之后需要点击按钮开启服务
                SharedPrefsUtil.putValue(mContext, MyTcpService.SERVICE_CONNECT, MyTcpService.SERVICE_CONNECT, System.currentTimeMillis());
            }
        });

        btndisConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnect) {
                    if (nettyClient != null) {
                        mHander.removeMessages(5);
                        nettyClient.disconnect();
                    }
                }
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etIP.getText().toString()) || TextUtils.isEmpty(etPort.getText().toString())) {
                    ToastUtils.showShort("IP地址为空");
                    return;
                }
                if (isConnect) {
                    ToastUtils.showShort("当前已连接");
                    return;
                }
                //配置网络
                if (SettingHelper.getSystemSetting().isAddRoute() && !TextUtils.isEmpty(NetWorkUtils.getLocalIp())) {
                    String locatIp = NetWorkUtils.getLocalIp();
                    String routeIp = locatIp.substring(0, locatIp.lastIndexOf("."));
                    UdpLEDUtil.shellExec("ip route add " + routeIp + ".0/24 dev eth0 proto static scope link table wlan0 \n");
                }
                btnConnect.setEnabled(false);
                mHander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initSocket(etIP.getText().toString(), Integer.parseInt(etPort.getText().toString()));
                    }
                }, 1000);
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
                ToastUtils.showShort("同步完成");
            }
        });
        mMachinePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MACHINE_IP, etIP.getText().toString());
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MACHINE_PORT, etPort.getText().toString());
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MACHINE_SERVER_PORT, serverPort.getText().toString());
            }
        });
    }

    private void bindTcpService() {
        bindIntent = new Intent(mContext, MyTcpService.class);

//        if (!isWorked())
        startService(bindIntent);
        isBind = bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 判断服务是否已启动
     *
     * @return
     */
    private boolean isWorked() {
        ActivityManager myManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals("com.feipulai.exam.activity.MiddleDistanceRace.MyTcpService")) {
                return true;
            }
        }
        return false;
    }

    public void unBindService() {
        if (isBind && serviceConnection != null) {
            unbindService(serviceConnection);
            myTcpService.unRegisterCallBack(callBack);
            if (myBinder != null && (System.currentTimeMillis() - lastServiceTime) > 12 * 60 * 60 * 1000) {
                myBinder.stopWork();
            }
        }
    }

    private List<List<String>> selectResults;

    /**
     * 初始化查询成绩pop
     */
    private void initSelectPop() {
        mSelectPop = EasyPopup.create()
                .setContentView(this, R.layout.pop_race_select)
                .setBackgroundDimEnable(true)
                .setDimValue(0.5f)
                //是否允许点击PopupWindow之外的地方消失
                .setFocusAndOutsideEnable(true)
                .setHeight(height * 2 / 3)
                .setWidth(width * 3 / 4)
                .apply();
        resultScroll = mSelectPop.findViewById(R.id.result_scroll);

        selectResults = new ArrayList<>();
        resultScroll.setPanelAdapter(new SelectResultAdapter(selectResults));
    }

    private boolean isIntentFlag = false;//跳转监听（当跳转时在onPause中断开连接）

    private void startProjectSetting() {
        isIntentFlag = true;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFlag", !isFlag);
        bundle.putInt("schedulePosition", schedulePosition);
        bundle.putInt("mItemPosition", mItemPosition);
        bundle.putInt("groupStatePosition", groupStatePosition);
        IntentUtil.gotoActivityForResult(this, MiddleRaceSettingActivity.class, bundle, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult", "-----------");
    }

    private boolean isConnect = false;//设备是否连接成功
    private boolean isFirst = true;

    //初始化 连接设备
    private void initSocket(final String ip, final int port) {
        if (nettyClient == null) {
            mHander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isFirst = true;
                    nettyClient = new NettyClient(ip, port);
                    if (!nettyClient.getConnectStatus()) {
                        nettyClient.setListener(MiddleDistanceRaceActivity.this);
                        nettyClient.connect(isFirst);
                        mHander.sendEmptyMessageDelayed(2, 400);
                    } else {
                        mHander.sendEmptyMessageDelayed(2, 200);
                    }
                    mHander.sendEmptyMessageDelayed(5, 8000);
                }
            }, 2000);
        } else {
            if (!nettyClient.getConnectStatus()) {
                nettyClient.connect(isFirst);
                mHander.sendEmptyMessageDelayed(2, 300);//先发送连接设备命令
//                mHander.sendEmptyMessageDelayed(6, 1000);//再发送结束命令
                mHander.sendEmptyMessageDelayed(5, 8000);
            }
        }
    }

    //发送连接设备命令
    private void sendConnect() {
        nettyClient.sendMsgToServer(TcpConfig.CMD_CONNECT, this);
    }

    private void sendDisConnect() {
        nettyClient.sendMsgToServer(TcpConfig.getCmdEndTiming(), this);
    }

    //随便发送一个东西保持tcp不断
    private void send2() {
        nettyClient.sendMsgToServer(TcpConfig.CMD_NOTHING, null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        firstTime = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_TIME_FIRST, FIRST_TIME);
        spanTime = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_TIME_SPAN, SPAN_TIME);

        colorGroups.clear();
        colorGroups.addAll(DBManager.getInstance().queryAllChipGroup());

        int timers2 = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_NUMBER, 3);

        if (timers2 > timers) {
            TimingBean timingBean;
            for (int i = 0; i < timers2 - timers; i++) {
                timingBean = new TimingBean(0, 0, 0, "", "", 0, 0);
                timingLists.add(timingBean);
            }
            raceTimingAdapter.notifyDataSetChanged();
        } else if (timers2 < timers) {
            timingLists.clear();
            TimingBean timingBean;
            for (int i = 0; i < timers2; i++) {
                timingBean = new TimingBean(0, 0, 0, "", "", 0, 0);
                timingLists.add(timingBean);
            }
            resultDataList.clear();

            //添加标题栏
            titleData.add("道次");
            titleData.add("姓名");
            titleData.add("最终成绩");
            for (int i = 3; i < cycleNo + 3; i++) {
                titleData.add("第" + (i - 2) + "圈");
            }

            raceTimingAdapter.notifyDataSetChanged();
//            resultShowTable.setAdapter(tableShowAdapter);
            resultShowTable.notifyContent();

            for (GroupItemBean bean : groupItemBeans
                    ) {
                if (bean.getGroup().getIsTestComplete() == TimingBean.GROUP_3) {
                    bean.getGroup().setIsTestComplete(GROUP_4);
                }
            }
            groupAdapter.notifyDataSetChanged();
        }
        timers = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_NUMBER, 3);

        initSocket(machine_ip, Integer.parseInt(machine_port));
    }


    @Override
    protected void onPause() {
        super.onPause();
        DialogUtil.dismiss();
        //当前界面隐藏后台不断开连接，跳转其它界面时断开连接
        if (isIntentFlag && nettyClient != null) {
            //停止计时命令
//            nettyClient.sendMsgToServer(TcpConfig.getCmdEndTiming(), this);
            mHander.removeMessages(5);
            isFlag = true;
            try {
                nettyClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            isIntentFlag = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止计时命令
//        nettyClient.sendMsgToServer(TcpConfig.getCmdEndTiming(), this);
        if (nettyClient != null) {
            mHander.removeMessages(5);
            nettyClient.disconnect();
        }

        unBindService();
        instance = null;

        List<Group> groupList = new ArrayList<>();
        for (Item item : itemList
                ) {
            if (item.getItemCode() != null) {
                groupList.addAll(DBManager.getInstance().queryGroupByItemCode(item.getItemCode()));
            }
        }

        for (Group group : groupList
                ) {
            if (group.getIsTestComplete() == TimingBean.GROUP_3 || group.getIsTestComplete() == TimingBean.GROUP_WAIT || group.getIsTestComplete() == TimingBean.GROUP_TIMING) {
                group.setIsTestComplete(TimingBean.GROUP_4);
            }
        }
        DBManager.getInstance().updateGroups(groupList);


//        try {
//            if (mySocketServer != null)
//                mySocketServer.stopServerAsync();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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
        boolean isFind = false;//标记当前一轮芯片是否有效
        for (String card : cardIds
                ) {
            Log.i("card", "-------------" + card);
            ChipInfo chipInfo = DBManager.getInstance().queryChipInfoByID(card);
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
                            isFind = true;
                            break;
                        }
                    }
                    break;
                }
            }
            if (isFind) {//优化，当同时接收到的最后一个芯片并且有效才开始刷新界面
                isFind = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultShowTable.notifyContent();
                    }
                });
            }
        }
    }

    @Override
    public void onConnected(String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnConnect.setEnabled(true);
            }
        });
        isConnect = true;
        isFirst = false;
//        Log.i("onConnected", "onConnected-----------------------");
        if (isFlag) {
            TtsManager.getInstance().speak("设备连接成功");
            mHander.sendMessage(mHander.obtainMessage(1, text));
            isFlag = false;
        }
    }

    @Override
    public void onMessageFailed(Object msg) {

    }

    @Override
    public void onStartTiming(long time) {
//        Log.i("onMessageResponse", "开始计时---------" + time);
//        Log.i("timingLists", timingLists.toString());
        int timerNo = 0;
        for (TimingBean timing : timingLists
                ) {
            //当前处于等待状态的组别开始计时
            if (timing.getState() == TIMING_STATE_WAITING) {
                timing.setState(TIMING_START);//开始计时
                timing.setTime(time);
                //将开始时间保存到每个人的数据中
                for (RaceResultBean raceResultBean2 : resultDataList
                        ) {
                    if (raceResultBean2.getColor() == timing.getColor()) {
                        raceResultBean2.setStartTime(time);
                    }
                }
                mHander.sendMessage(mHander.obtainMessage(3, timerNo));
            }
            timerNo++;
        }
//        Log.i("timingLists", timingLists.toString());
        mHander.sendEmptyMessageDelayed(4, 1000);
        //更新组别中的状态
        for (int i = 0; i < groupItemBeans.size(); i++) {
            if (groupItemBeans.get(i).getGroup().getIsTestComplete() == GROUP_WAIT) {
                groupItemBeans.get(i).getGroup().setIsTestComplete(GROUP_TIMING);
                final int finalI = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        groupAdapter.notifyItemChanged(finalI);
                    }
                });
            }
        }
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
                    viewConnectState.setBackgroundResource(R.drawable.blue_circle);
                } else {
                    if (isFirst) {
                        ToastUtils.showShort("连接失败");
                        mHander.removeMessages(5);
                        nettyClient.disconnect();
                        nettyClient = null;
                        isFirst = false;
                        btnConnect.setEnabled(true);
                    }
                    isConnect = false;
                    viewConnectState.setBackgroundResource(R.drawable.red_circle);
//                    Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private boolean isShow = true;

    @OnClick({R.id.btn_find, R.id.btn_middle_back, R.id.tv_back, R.id.btn_setting, R.id.btn_fullscreen, R.id.btn_image_connect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_find:
                showInput();
                break;
            case R.id.btn_middle_back:
                onBackPressed();
                break;
            case R.id.tv_back:
                onBackPressed();
                break;
            case R.id.btn_setting:
                for (TimingBean timingBean : timingLists
                        ) {
                    if (timingBean.getState() == TIMING_STATE_WAITING || timingBean.getState() == TIMING_STATE_TIMING) {
                        ToastUtils.showShort("考试中，请勿跳转其它界面");
                        return;
                    }
                }
                startProjectSetting();
                break;
            case R.id.btn_fullscreen:
                isShow = !isShow;
                if (isShow) {
                    btnFullscreen.setText("隐藏组别");
                    btnFullscreen.setImgResource(R.drawable.btn_fullscreen_selecor);
                    llShowItem.setVisibility(View.VISIBLE);
                } else {
                    btnFullscreen.setText("显示组别");
                    btnFullscreen.setImgResource(R.drawable.btn_fullscreen_exit_selecor);
                    llShowItem.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_image_connect:
                mMachinePop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                break;

        }
    }

    private void showInput() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("输入需要定位的小组组号").setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String number = editText.getText().toString();
                        if (TextUtils.isEmpty(number)) {
                            ToastUtils.showShort("查询为空");
                            return;
                        }
                        for (int j = 0; j < groupItemBeans.size(); j++) {
                            if (number.equals(groupItemBeans.get(j).getGroup().getGroupNo() + "")) {
                                smoothMoveToPosition(rvRaceStudentGroup, j);
                                break;
                            } else {
                                if (j == groupItemBeans.size() - 1) {
                                    ToastUtils.showShort("查无此组");
                                }
                            }
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * 滑动到指定位置
     */
    private void smoothMoveToPosition(RecyclerView mRecyclerView, final int position) {
        // 第一个可见位置
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // 最后一个可见位置
        int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
        if (position < firstItem) {
            // 第一种可能:跳转位置在第一个可见位置之前，使用smoothScrollToPosition
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // 第二种可能:跳转位置在第一个可见位置之后，最后一个可见项之前
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                // smoothScrollToPosition 不会有效果，此时调用smoothScrollBy来滑动到指定位置
                mRecyclerView.smoothScrollBy(0, top);
            }
        } else {
            // 第三种可能:跳转位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
            mRecyclerView.smoothScrollToPosition(position);
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

        if (cycleNo == 0) {
            ToastUtils.showShort("请先设置圈数");
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
        for (RaceResultBean resultBean2 : resultDataList
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
        resultShowTable.notifyContent();

        for (int i = 0; i < groupItemBeans.size(); i++) {
            if (timingLists.get(position).getNo() == groupItemBeans.get(i).getGroup().getGroupNo()) {
                groupItemBeans.get(i).getGroup().setIsTestComplete(GROUP_WAIT);
                groupAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * 点击“违规返回”按钮回调
     */
    @Override
    public void clickTimingBackListener(final int position, final RaceTimingAdapter.VH holder) {
        DialogUtil.showCommonDialog(this, "是否违规返回", new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                Logger.d(TAG + "中长跑点击了违规返回按钮" + timingLists.get(position).toString());
                raceTimingAdapter.notifyBackGround(holder, TIMING_STATE_BACK);
                timingLists.get(position).setState(TIMING_STATE_BACK);
//                raceTimingAdapter.notifyDataSetChanged();
                //初始化开始时间
                for (RaceResultBean resultBean2 : resultDataList
                        ) {
                    if (resultBean2.getColor() == timingLists.get(position).getColor()) {
                        resultBean2.setStartTime(0);
                    }
                }
                Log.i("clickTimingBackListener", timingLists.toString());

                for (int i = 0; i < groupItemBeans.size(); i++) {
                    if (timingLists.get(position).getNo() == groupItemBeans.get(i).getGroup().getGroupNo()) {
                        groupItemBeans.get(i).getGroup().setIsTestComplete(GROUP_3);
                        groupAdapter.notifyItemChanged(i);
                        break;
                    }
                }
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
                Logger.d(TAG + "中长跑点击了完成计时按钮" + timingLists.get(position).toString());
                timingLists.get(position).setState(TimingBean.TIMING_STATE_COMPLETE);
//                raceTimingAdapter.notifyBackGround(holder, TimingBean.TIMING_STATE_COMPLETE);
//                raceTimingAdapter.notifyDataSetChanged();

                List<UploadResults> uploadResults = new ArrayList<>();
                List<RoundResult> roundResults = new ArrayList<>();

                Group dbGroupList = null;
                if (timingLists.get(position).getItemCode().equals(itemList.get(mItemPosition).getItemCode())) {
                    //更新项目组状态
                    for (GroupItemBean groupItemBean : groupItemBeans
                            ) {
                        if (timingLists.get(position).getItemGroupName().equals(groupItemBean.getGroupItemName())) {
                            //当前组完成之后更新状态并移除
                            groupItemBean.getGroup().setIsTestComplete(GROUP_FINISH);
                            DBManager.getInstance().updateGroup(groupItemBean.getGroup());
                            groupItemBeans.remove(groupItemBean);
                            groupAdapter.notifyDataSetChanged();
                            dbGroupList = groupItemBean.getGroup();
                            break;
                        }
                    }
                } else {
//                    Log.i("dbGroupList", "----" + timingLists.get(position).getItemCode());
//                    Log.i("dbGroupList", "----" + timingLists.get(position).getNo());
//                    Log.i("dbGroupList", "----" + timingLists.get(position).getColor());
                    dbGroupList = DBManager.getInstance().getGroupByNo(timingLists.get(position).getItemCode(), timingLists.get(position).getNo(), timingLists.get(position).getColor());

                    dbGroupList.setIsTestComplete(GROUP_FINISH);
                    DBManager.getInstance().updateGroup(dbGroupList);
//                    Log.i("dbGroupList", "----" + dbGroupList.toString());
                }

                //更新组别
                RoundResult roundResult;
                UploadResults uploadResult;
                String itemName = null;
                for (RaceResultBean resultBean2 : resultDataList
                        ) {
                    if (resultBean2.getColor() == timingLists.get(position).getColor()) {
                        //初始化开始时间
                        resultBean2.setStartTime(0);
                        itemName = resultBean2.getItemName();

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
                        roundResult = new RoundResult();//保存到数据库的成绩对象

                        roundResult.setItemCode(resultBean2.getItemCode());
                        roundResult.setMachineCode(machineCode);
                        roundResult.setResult(lastResult);
                        roundResult.setRoundNo(1);
                        roundResult.setStudentCode(resultBean2.getStudentCode());
                        roundResult.setTestNo(1);
                        roundResult.setIsLastResult(1);
                        roundResult.setMachineResult(lastResult);
                        roundResult.setScheduleNo(dbGroupList.getScheduleNo());
                        roundResult.setResultState(resultBean2.getResultState());
                        roundResult.setExamType(dbGroupList.getExamType());
                        roundResult.setTestTime(System.currentTimeMillis() + "");
                        roundResult.setGroupId(dbGroupList.getId());

                        byte[] cycleResult = DataUtil.byteArray2RgbArray(resultInts);
                        roundResult.setCycleResult(cycleResult);

                        roundResult = DBManager.getInstance().insertRoundResult2(roundResult);

                        roundResults.add(roundResult);

                        uploadResult = new UploadResults();//需要上传的成绩对象
                        uploadResult.setGroupNo(resultBean2.getNo());
                        uploadResult.setSiteScheduleNo(dbGroupList.getScheduleNo());
                        uploadResult.setRoundResultList(RoundResultBean.beanCope2(roundResult));
                        uploadResult.setStudentCode(resultBean2.getStudentCode());
                        uploadResult.setTestNum("1");
                        uploadResult.setExamItemCode(resultBean2.getItemCode());
                        uploadResults.add(uploadResult);
                    }
                }

                //在成绩显示列删除选中组的所有考生
                Iterator<RaceResultBean> it = resultDataList.iterator();

                List<RaceResultBean> completeBeans = new ArrayList<>();//当前完成的组别
                while (it.hasNext()) {
                    RaceResultBean raceResultBean2 = it.next();
                    String x = raceResultBean2.getNo();
                    String y = raceResultBean2.getItemCode();

                    if (TextUtils.isEmpty(x) || TextUtils.isEmpty(y)) {
                        continue;
                    }

                    if (y.equals(timingLists.get(position).getItemCode()) && x.equals(timingLists.get(position).getNo() + "")) {
                        it.remove();
                        completeBeans.add(raceResultBean2);
                    }
                }

                resultShowTable.notifyContent();

                //完成后直接删除
                timingLists.get(position).setState(TIMING_STATE_NOMAL);
                timingLists.get(position).setItemGroupName("");
                timingLists.get(position).setColor(0);
                timingLists.get(position).setNo(0);
                timingLists.get(position).setStudentNo(0);
                timingLists.get(position).setItemCode("");
                timingLists.get(position).setTime(0);

//                raceTimingAdapter.notifyDataSetChanged();
                raceTimingAdapter.notifyItemChanged(position);
                raceTimingAdapter.notifyBackGround(holder, TimingBean.TIMING_STATE_COMPLETE);
                //自动上传成绩
                if (SettingHelper.getSystemSetting().isRtUpload()) {
//                    Logger.i("自动上传成绩:" + uploadResults.toString());
//                    ServerMessage.uploadResult(uploadResults);
                    if (itemName != null) {
                        ServerMessage.uploadZCPResult(mContext, itemName, uploadResults);
                    }
                }
                Logger.i(TAG + "成绩", roundResults.toString());
                //自动打印
                MiddlePrintUtil.print(roundResults, completeBeans, digital, carryMode);

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
    public void clickTimingDelete(final int position, final RaceTimingAdapter.VH holder) {
        if (timingLists.get(position).getNo() == 0) {
            return;
        }
        deleteTiming(position, holder);
    }

    private void deleteTiming(final int position, final RaceTimingAdapter.VH holder) {
        if (timingLists.get(position).getState() == TIMING_STATE_WAITING || timingLists.get(position).getState() == TIMING_STATE_TIMING) {
            ToastUtils.showShort("该状态下不能取消比赛");
            return;
        }
        DialogUtil.showCommonDialog(this, "是否删除当前组", new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                Logger.d(TAG + "中长跑点击了删除按钮" + timingLists.get(position).toString());
                //在成绩显示列删除选中组的所有考生
                Iterator<RaceResultBean> it = resultDataList.iterator();
                while (it.hasNext()) {
                    RaceResultBean raceResultBean2 = it.next();
                    String x = raceResultBean2.getNo();
                    String y = raceResultBean2.getItemCode();
                    if (TextUtils.isEmpty(x) || TextUtils.isEmpty(y)) {
                        continue;
                    }
                    if (x.equals(timingLists.get(position).getNo() + "") && y.equals(timingLists.get(position).getItemCode())) {
                        it.remove();
                    }
                }

                resultShowTable.notifyContent();

                Group group = DBManager.getInstance().queryGroup(timingLists.get(position).getItemCode(), timingLists.get(position).getNo());

                group.setIsTestComplete(TimingBean.GROUP_4);
                groupAdapter.notifyDataSetChanged();

                //要删除的组恢复默认状态
                timingLists.get(position).setState(TIMING_STATE_NOMAL);
                timingLists.get(position).setItemGroupName("");
                timingLists.get(position).setColor(0);
                timingLists.get(position).setNo(0);
                timingLists.get(position).setStudentNo(0);
                timingLists.get(position).setItemCode("");
                timingLists.get(position).setTime(0);
                raceTimingAdapter.notifyItemChanged(position, holder);
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
                if (colorGroups.isEmpty()) {
                    ToastUtils.showShort("请先设置颜色组");
                    mCirclePop.dismiss();
                    return;
                }

                groupItemBeans.get(groupPosition).getGroup().setColorGroupName(colorGroups.get(mColorGroupPosition).getColorGroupName());//备注1字段---颜色组名
                groupItemBeans.get(groupPosition).getGroup().setColorId(String.valueOf(colorGroups.get(mColorGroupPosition).getColor()));//备注2字段---颜色
                groupAdapter.notifyDataSetChanged();

                addToTiming();
                mCirclePop.dismiss();
            }
        });

        btnSelectColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorGroups.isEmpty()) {
                    ToastUtils.showShort("请先设置颜色组");
                    mCirclePop.dismiss();
                    return;
                }
                groupItemBeans.get(groupPosition).getGroup().setIsTestComplete(TimingBean.GROUP_4);//关联状态
                groupItemBeans.get(groupPosition).getGroup().setColorGroupName(colorGroups.get(mColorGroupPosition).getColorGroupName());//备注1字段---颜色组名
                groupItemBeans.get(groupPosition).getGroup().setColorId(String.valueOf(colorGroups.get(mColorGroupPosition).getColor()));//备注2字段---颜色
                groupAdapter.notifyDataSetChanged();

                DBManager.getInstance().updateGroup(groupItemBeans.get(groupPosition).getGroup());
                mCirclePop.dismiss();
            }
        });
    }

    private void showCompleteDialog() {
        String[] selectItems = new String[]{"查看成绩", "打印成绩"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(this);
        listDialog.setTitle(groupName);
        listDialog.setItems(selectItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        selectResults.clear();

                        List<GroupItem> groupItems = groupItemBeans.get(groupPosition).getGroupItems();
                        SelectResultBean selectResultBean;
                        List<String> strings;
                        for (GroupItem groupItem : groupItems
                                ) {
                            Student student = DBManager.getInstance().queryStudentByStuCode(groupItem.getStudentCode());
                            selectResultBean = new SelectResultBean();
                            selectResultBean.setStudentName(student.getStudentName());
                            selectResultBean.setStudentCode(student.getStudentCode());
                            selectResultBean.setSex(student.getSex());
                            selectResultBean.setTrackNo(groupItem.getTrackNo());

                            RoundResult roundResult = DBManager.getInstance().queryResultByStudentCode(groupItem.getStudentCode(), groupItem.getItemCode(), groupItemBeans.get(groupPosition).getGroup().getId());

                            strings = new ArrayList<>();
                            strings.add(groupItem.getTrackNo() + "");
                            strings.add(student.getStudentCode());
                            strings.add(student.getStudentName());

                            String lastResult;
                            if (carryMode == 0) {
                                lastResult = DateUtil.caculateTime(roundResult.getResult(), 3, carryMode);
                            } else {
                                lastResult = DateUtil.caculateTime(roundResult.getResult(), digital, carryMode);
                            }
                            switch (roundResult.getResultState()) {
                                case 2:
                                    lastResult = "DQ";
                                    break;
                                case 3:
                                    lastResult = "DNF";
                                    break;
                                case 4:
                                    lastResult = "DNS";
                                    break;
                                case 5:
                                    lastResult = "DT";
                                    break;
                            }
                            strings.add(lastResult);
//                            strings.add(student.getSex()==0?"男":"女");
                            if (roundResult.getCycleResult() != null) {
                                int[] results = DataUtil.byteArray2RgbArray(roundResult.getCycleResult());
                                for (int result : results
                                        ) {
                                    String resultString;
                                    if (carryMode == 0) {
                                        resultString = DateUtil.caculateTime(result, 3, carryMode);
                                    } else {
                                        resultString = DateUtil.caculateTime(result, digital, carryMode);
                                    }
                                    strings.add(resultString);
//                                    strings.add(DateUtil.caculateTime(result, digital + 1, carryMode + 1));
                                }
                            }
                            selectResults.add(strings);
                        }

                        Collections.sort(selectResults, new Comparator<List<String>>() {
                            @Override
                            public int compare(List<String> o1, List<String> o2) {
                                return Integer.valueOf(o1.get(0)).compareTo(Integer.valueOf(o2.get(0)));//按照道次升序排列
                            }
                        });
                        selectResults.add(0, null);//添加标题栏
                        resultScroll.notifyDataSetChanged();

                        mSelectPop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                        break;
                    case 1:
                        MiddlePrintUtil.print2(groupItemBeans.get(groupPosition), digital, carryMode);
                        break;
                    default:
                        break;
                }
            }
        });
        listDialog.show();
    }

    /**
     * 长按未完成组别弹出下列选项
     */
    private void showListDialog() {
        String[] selectItems = {"关联颜色", "取消关联", "选入比赛"};

        AlertDialog.Builder listDialog = new AlertDialog.Builder(this);
        listDialog.setTitle(groupName);
        listDialog.setItems(selectItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int testComplete = groupItemBeans.get(groupPosition).getGroup().getIsTestComplete();
                switch (which) {
                    case 0:
                        if (testComplete == GROUP_3) {
                            for (TimingBean timing : timingLists
                                    ) {
                                if (groupItemBeans.get(groupPosition).getGroupItemName().equals(timing.getItemGroupName())) {
                                    ToastUtils.showShort("该组已选入");
                                    return;
                                }
                            }
                            addToTiming();
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
                        groupItemBeans.get(groupPosition).getGroup().setIsTestComplete(0);
                        groupItemBeans.get(groupPosition).getGroup().setColorGroupName("");//备注1字段---颜色组名
                        groupItemBeans.get(groupPosition).getGroup().setColorId("");//备注2字段---颜色
                        groupAdapter.notifyDataSetChanged();
                        DBManager.getInstance().updateGroup(groupItemBeans.get(groupPosition).getGroup());
                        break;
                    case 2:
                        if (testComplete == GROUP_3) {
                            for (TimingBean timing : timingLists
                                    ) {
                                if (groupItemBeans.get(groupPosition).getGroupItemName().equals(timing.getItemGroupName())) {
                                    ToastUtils.showShort("该组已存在");
                                    return;
                                }
                            }
                            addToTiming();
                        } else if (testComplete == GROUP_FINISH) {
                            ToastUtils.showShort("该组已完成");
                        } else if (testComplete == GROUP_4) {
                            addToTiming();
                        } else {
                            ToastUtils.showShort("请先关联颜色组");
                        }
                        break;
                    case 3:
//                        if (testComplete == GROUP_4) {
//                            ToastUtils.showShort("该组还未选入");
//                            break;
//                        } else if (testComplete == GROUP_FINISH) {
//                            ToastUtils.showShort("该组已完成");
//                            break;
//                        } else if (testComplete == GROUP_3) {
//                            for (int i = 0; i < timingLists.size(); i++) {
//                                if (timingLists.get(i).getNo() == groupPosition + 1) {
//                                    deleteTiming(i, holder);
//                                    break;
//                                }
//                            }
//                        }
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

        if (groupStatePosition == 0) {
            showListDialog();//未考完
        } else {
            showCompleteDialog();//已考完
        }
    }

    /**
     * 获取项目全称
     */
    private void getGroupName() {
        String sex = "";
        switch (groupItemBeans.get(groupPosition).getGroup().getGroupType()) {
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
        groupName = sex + items[mItemPosition] + "第" + groupItemBeans.get(groupPosition).getGroup().getGroupNo() + "组";
    }

    private int addPosition;//新加入的计时器所占位置

    /**
     * 添加到计时器中
     */
    private void addToTiming() {
        for (TimingBean timing : timingLists
                ) {
            if (timing.getColor() == Integer.parseInt(groupItemBeans.get(groupPosition).getGroup().getColorId())) {
                ToastUtils.showShort("当前颜色组已存在");
                return;
            }
        }

        //从所有组中选入比赛（循环所有已入场的组，当首个出现no为0即空白组时，跳出循环并分配当前选中）
        for (int i = 0; i < timingLists.size(); i++) {
            if (timingLists.get(i).getNo() == 0) {
                if (i > 0) {
                    addPosition = i * timingLists.get(i - 1).getStudentNo();
                } else {
                    addPosition = 0;
                }
                timingLists.get(i).setNo(groupItemBeans.get(groupPosition).getGroup().getGroupNo());//分配组的组号
                timingLists.get(i).setItemGroupName(groupName);//组名
                timingLists.get(i).setStudentNo(groupItemBeans.get(groupPosition).getGroupItems().size());
                timingLists.get(i).setItemCode(itemList.get(mItemPosition).getItemCode());
                timingLists.get(i).setColor(Integer.parseInt(groupItemBeans.get(groupPosition).getGroup().getColorId()));
                break;
            } else {
                if (i == timingLists.size() - 1) {
                    ToastUtils.showShort("计时器已满，请等待其他组结束");
                    return;
                }
            }
        }

        raceTimingAdapter.notifyDataSetChanged();

        addResultList();

        groupItemBeans.get(groupPosition).getGroup().setIsTestComplete(TimingBean.GROUP_3);
        groupAdapter.notifyDataSetChanged();
        DBManager.getInstance().updateGroup(groupItemBeans.get(groupPosition).getGroup());
    }

    /**
     * 添加已选入比赛组的所有人到成绩显示列表
     */
    private void addResultList() {
        List<GroupItem> groupItems = DBManager.getInstance().queryGroupItem(scheduleNo, groupItemBeans.get(groupPosition).getGroup().getItemCode(), groupItemBeans.get(groupPosition).getGroup().getGroupNo(), groupItemBeans.get(groupPosition).getGroup().getGroupType());
        String[] strings2;
        RaceResultBean raceResultBean;
        for (int i = 0; i < groupItems.size(); i++) {
            Student student = DBManager.getInstance().queryStudentByStuCode(groupItems.get(i).getStudentCode());
            strings2 = new String[cycleNo + 3];
            strings2[0] = groupItems.get(i).getTrackNo() + "";
            strings2[1] = student.getStudentName();
            raceResultBean = new RaceResultBean();
            raceResultBean.setResults(strings2);
            raceResultBean.setNo(groupItemBeans.get(groupPosition).getGroup().getGroupNo() + "");
            raceResultBean.setVestNo(i + 1);
            raceResultBean.setResultState(RaceResultBean.STATE_NORMAL);
            raceResultBean.setStudentCode(student.getStudentCode());
            raceResultBean.setStudentName(student.getStudentName());
            raceResultBean.setColor(Integer.parseInt(groupItemBeans.get(groupPosition).getGroup().getColorId()));

            String cycle = SharedPrefsUtil.getValue(mContext, SharedPrefsConfigs.DEFAULT_PREFS, itemList.get(mItemPosition).getItemName(), "0");
            raceResultBean.setCycle(Integer.parseInt(cycle));
            raceResultBean.setItemCode(itemList.get(mItemPosition).getItemCode());
            raceResultBean.setItemName(itemList.get(mItemPosition).getItemName());
            resultDataList.add(addPosition, raceResultBean);
        }

//        Log.i(TAG, resultDataList.toString());
        resultShowTable.notifyContent();
    }

    private int mColorGroupPosition;

    @Override
    public void onColorSelectClick(int position) {
        mColorGroupPosition = position;
        colorGroupAdapter.changeBackGround(position);
        colorGroupAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_race_schedule:
                schedulePosition = position;
                scheduleNo = scheduleList.get(position).getScheduleNo();
                break;
            case R.id.sp_race_item:
                mItemPosition = position;
                break;
            case R.id.sp_race_state:
                groupStatePosition = position;
                break;
        }
        if (isAutoSelect) {
            isAutoSelect = false;
            return;
        }
        getGroupList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
