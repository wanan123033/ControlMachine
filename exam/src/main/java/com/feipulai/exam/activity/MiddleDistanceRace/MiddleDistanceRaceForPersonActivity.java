package com.feipulai.exam.activity.MiddleDistanceRace;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.ScannerGunManager;
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
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.GroupingAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ItemTouchHelperCallback;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.MiddleRaceGroupAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.RaceTimingAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.GroupItemBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.RaceResultBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.SelectResultBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.ServiceTcpBean;
import com.feipulai.exam.activity.MiddleDistanceRace.vhtableview.VHTableAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.vhtableview.VHTableResultAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.vhtableview.VHTableView;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
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
import com.kk.taurus.playerbase.assist.InterEvent;
import com.kk.taurus.playerbase.assist.OnVideoViewEventHandler;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.player.IPlayer;
import com.kk.taurus.playerbase.receiver.ReceiverGroup;
import com.kk.taurus.playerbase.widget.BaseVideoView;
import com.orhanobut.logger.Logger;
import com.ww.fpl.videolibrary.camera.HkCameraManager;
import com.ww.fpl.videolibrary.play.play.DataInter;
import com.ww.fpl.videolibrary.play.play.ReceiverGroupManager;
import com.ww.fpl.videolibrary.play.util.PUtil;
import com.zyyoona7.popup.EasyPopup;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import static com.feipulai.exam.config.SharedPrefsConfigs.CAMERA_IP;
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
import static com.ww.fpl.videolibrary.camera.HKConfig.HK_PORT;
import static com.ww.fpl.videolibrary.camera.HKConfig.HK_PSW;
import static com.ww.fpl.videolibrary.camera.HKConfig.HK_PSW_PRE;
import static com.ww.fpl.videolibrary.camera.HKConfig.HK_USER;
import static com.ww.fpl.videolibrary.camera.HKConfig.HK_USER_PRE;

/**
 * @author ww
 * @time 2019/10/18 13:30
 * 中长跑个人模式（需要在此新建分组）
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MiddleDistanceRaceForPersonActivity extends BaseCheckMiddleActivity implements UdpClient.UDPChannelListerner, NettyListener, RaceTimingAdapter.MyClickListener, ChannelFutureListener, MiddleRaceGroupAdapter.OnItemClickListener, ColorSelectAdapter.OnItemClickListener, AdapterView.OnItemSelectedListener {

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
    @BindView(R.id.btn_grouping)
    ImageTextButton btnGrouping;
    @BindView(R.id.btn_circle_delete)
    ImageTextButton btnCircleDelete;
    @BindView(R.id.btn_circle_add)
    ImageTextButton btnCircleAdd;
    @BindView(R.id.Sur_Player)
    SurfaceView SurPlayer;
    @BindView(R.id.btn_camera)
    ImageTextButton btnCamera;
    @BindView(R.id.tv1)
    TextView tv1;
    @BindView(R.id.tv2)
    TextView tv2;
    @BindView(R.id.tv3)
    TextView tv3;
    @BindView(R.id.tv_item_race_no)
    TextView tvItemRaceNo;
    @BindView(R.id.tv_item_race_item)
    TextView tvItemRaceItem;
    @BindView(R.id.tv_item_race_number)
    TextView tvItemRaceNumber;
    @BindView(R.id.tv_item_race_state)
    TextView tvItemRaceState;
    @BindView(R.id.ll_race_group)
    LinearLayout llRaceGroup;
    private String TAG = "MiddleDistanceRaceForPersonActivity";
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

    private double freeTime;
    private double freeSpace;
    Runnable timeRun = new Runnable() {
        @Override
        public void run() {
            if (dialogUtil == null || MiddleDistanceRaceForPersonActivity.this.isDestroyed()) {
                return;
            }
            //每隔半小时扫描存储空间
            //检查存储空间大小，给予提示，录像15M/sec
            freeSpace = new BigDecimal((float) FileUtil.getFreeSpaceStorage() / (1024 * 1024 * 1024)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            freeTime = new BigDecimal(freeSpace * 1024 / (15 * 60)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            //当可用空间支持录像时长小于1小时给出弹窗提示
            if (freeTime < 1) {
                showSpaceNotice();
            } else {
                ToastUtils.showShort("注意：当前存储空间支持录像约" + freeTime + "小时");
            }
            mHander.postDelayed(this, 1000 * 60 * 30);
        }
    };
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
    public static MiddleDistanceRaceForPersonActivity instance;
    private int width;
    private String machine_ip;
    private String machine_port;
    private String server_Port;
    private Intent bindIntent;
    //    private long lastServiceTime;
    private DataSource mDataSource;
    //    private VideoPlayWindow videoPlayer;
    private HkCameraManager hkCamera;
    private boolean hkInit;
    private String camera_ip;
    private DialogUtil dialogUtil;
    private ReceiverGroup mReceiverGroup;
    private Button btnVideoPlay;
    private Button btnVideoStartPause;
    private ImageView ivControl;
    private VHTableView tableResult;
    private VHTableResultAdapter tableResultAdapter;
    private EditText etHKUser;
    private EditText etHKPassWord;
    private String hk_user;
    private String hk_psw;
    private View parentView;
    private TextView tvVideoResult;

    @Override
    protected int setLayoutResID() {
        parentView = LayoutInflater.from(this).inflate(R.layout.activity_middle_distance_race, null);
        return R.layout.activity_middle_distance_race;
    }


    private List<Schedule> scheduleList = new ArrayList<>();
    private List<TimingBean> timingLists = new ArrayList<>();
    private ArrayList<RaceResultBean> resultDataList;
    private List<GroupItemBean> groupItemBeans = new ArrayList<>();
    private ArrayList<String> titleData = new ArrayList<>();
    private boolean isBind = false;
    private int currentRow = -1;

    @Override
    protected void initData() {
        mContext = this;
        instance = this;

        dialogUtil = new DialogUtil(mContext);

        mHander.postDelayed(timeRun, 60 * 1000 * 30);

        height = DisplayUtil.getScreenHightPx(this);
        width = DisplayUtil.getScreenWidthPx(this);

        btnGrouping.setVisibility(View.VISIBLE);

        btnSetting.setText("设置");
        btnSetting.setImgResource(R.drawable.btn_setting_selecor);
        imageConnect.setText("连接设备");
        imageConnect.setImgResource(R.drawable.btn_connect_selecor);
        btnFullscreen.setText("隐藏组别");
        btnFullscreen.setImgResource(R.drawable.btn_fullscreen_selecor);
        btnCamera.setText("显示录像");
        btnCamera.setImgResource(R.drawable.btn_fullscreen_selecor);
        btnGrouping.setText("分组");
        btnGrouping.setImgResource(R.mipmap.grouping);
        btnCircleDelete.setText("减一圈");
        btnCircleDelete.setImgResource(R.drawable.btn_delete_selecor);
        btnCircleAdd.setText("加一圈");
        btnCircleAdd.setImgResource(R.drawable.btn_add_selecor);

        schedulePosition = getIntent().getIntExtra("schedulePosition", 0);
        mItemPosition = getIntent().getIntExtra("mItemPosition", 0);
        groupStatePosition = getIntent().getIntExtra("groupStatePosition", 0);

        Item item = TestConfigs.sCurrentItem;
        carryMode = item.getCarryMode();
        itemCode = item.getItemCode();
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
        hk_user = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, HK_USER_PRE, HK_USER);
        hk_psw = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, HK_PSW_PRE, HK_PSW);
//        lastServiceTime = SharedPrefsUtil.getValue(mContext, MyTcpService.SERVICE_CONNECT, MyTcpService.SERVICE_CONNECT, 0L);
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

        //成绩列表点击、长按事件
        final VHTableAdapter tableShowAdapter = new VHTableAdapter(mContext, titleData, resultDataList, carryMode, digital, new VHTableAdapter.OnResultItemLongClick() {
            @Override
            public void resultListLongClick(int row, int state) {
                resultDataList.get(row).setResultState(state);
                resultShowTable.notifyContent();
            }

            @Override
            public void resultListClick(int row) {
                currentRow = row;
                for (int i = 0; i < resultDataList.size(); i++) {
                    if (row == i) {
                        resultDataList.get(i).setSelect(true);
                    } else {
                        resultDataList.get(i).setSelect(false);
                    }
                }
                resultShowTable.notifyContent();
            }
        });

        resultShowTable.setAdapter(tableShowAdapter);

        groupAdapter.setOnRecyclerViewItemClickListener(this);

        updateSchedules();

        getGroupList();

        initCamera();
        //连接设备弹窗
        initConnectPop();

        //当上一次服务开启时间小于12小时，每次进入自动开启服务
//        if (!TextUtils.isEmpty(server_Port) && lastServiceTime != 0 && (System.currentTimeMillis() - lastServiceTime) < 12 * 60 * 60 * 1000) {
//            currentPort = Integer.parseInt(server_Port);
//            bindTcpService();
//        }
    }

    @Override
    protected void initViews() {
        ScannerGunManager.getInstance().setScanListener(new ScannerGunManager.OnScanListener() {
            @Override
            public void onResult(String code) {
                Log.i("scannerGunManager", "->" + code);
                showGroupPop();
                if (TextUtils.isEmpty(code)) {
                    return;
                }
                onQrArrived(code);
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ScannerGunManager.getInstance().dispatchKeyEvent(event.getKeyCode(), event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initCamera() {
        camera_ip = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, CAMERA_IP, "");
        mDataSource = new DataSource();
    }

    private void showSpaceNotice() {
        dialogUtil.showCommonDialog("本机剩余存储空间" + freeSpace + "G,仅支持录像" + freeTime + "小时", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
            }

            @Override
            public void onNegativeClick() {
            }
        });
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
     * 上传之后会更新数据库，在此更新内存中的数据
     */
    public void refreshItemList() {
        String itemName = itemList.get(mItemPosition).getItemName();
        itemList = DBManager.getInstance().queryItemsByMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        for (int i = 0; i < itemList.size(); i++) {
            items[i] = itemList.get(i).getItemName();
            if (itemName.equals(items[i])) {
                mItemPosition = i;
            }
        }
        itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, items);
        spRaceItem.setAdapter(itemAdapter);
        spRaceItem.setSelection(mItemPosition);
    }

    public static String itemCode;

    /**
     * 获取日程分组
     */
    private void getGroupList() {
        groupItemBeans.clear();
        if (scheduleNo == null || scheduleNo.isEmpty()) {
            return;
        }

        itemCode = itemList.get(mItemPosition).getItemCode();
        if (TextUtils.isEmpty(itemCode)) {
            ToastUtils.showShort("项目代码为空");
            groupItemBeans.clear();
            groupAdapter.notifyDataSetChanged();
        } else {
            //这里设置全局项目代码，中长跑项目最好不要使用全局项目代码，因为中长跑项目一个界面允许出现多个项目代码
//            TestConfigs.sCurrentItem.setItemCode(itemCode);
//            TestConfigs.sCurrentItem=DBManager.getInstance().queryItemByCode(itemCode);
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
                toastSpeak("考试中，请勿退出");
                return;
            }
        }
        clickBack();
    }

    private void clickBack() {
        dialogUtil.showCommonDialog("确定退出？", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
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
                    ToastUtils.showLong("接收到上道终端发送的分组数据");
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
        etHKUser = mMachinePop.findViewById(R.id.et_hk_user);
        etHKPassWord = mMachinePop.findViewById(R.id.et_hk_password);
        btnConnect = mMachinePop.findViewById(R.id.btn_connect_machine);
        Button btndisConnect = mMachinePop.findViewById(R.id.btn_disconnect_machine);
        btnSyncTime = mMachinePop.findViewById(R.id.btn_sync_time);
        Button btnStart = mMachinePop.findViewById(R.id.btn_start_server);
        Button btnStartCamera = mMachinePop.findViewById(R.id.btn_start_camera);
        rgVersion = mMachinePop.findViewById(R.id.rg_machine_version);
        TextView serverIP = mMachinePop.findViewById(R.id.tv_server_ip);
        final EditText serverPort = mMachinePop.findViewById(R.id.et_server_port);
        final EditText cameraIP = mMachinePop.findViewById(R.id.et_camera_ip);

        etHKUser.setText(hk_user);
        etHKPassWord.setText(hk_psw);
        etIP.setText(machine_ip);
        etIP.setSelection(machine_ip.length());
        etPort.setText(machine_port);
        cameraIP.setText(camera_ip);
        serverIP.setText("服务器：" + NetWorkUtils.getLocalOrWlanIp());
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
//                SharedPrefsUtil.putValue(mContext, MyTcpService.SERVICE_CONNECT, MyTcpService.SERVICE_CONNECT, System.currentTimeMillis());
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
                nettyClient.sendMsgToServer(TcpConfig.getCmdUpdateDate(), MiddleDistanceRaceForPersonActivity.this);
                ToastUtils.showShort("同步完成");
            }
        });

        btnStartCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(cameraIP.getText())) {
                    ToastUtils.showShort("请先输入摄像头ip");
                    return;
                }
                hkCamera.setStrIP(cameraIP.getText().toString());
                if (hkInit) {
                    hkCamera.login2HK(etHKUser.getText().toString(), etHKPassWord.getText().toString());
                    mHander.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hkCamera.startPreview();
                        }
                    }, 600);
                } else {
                    ToastUtils.showShort("海康摄像头初始化失败");
                }
            }
        });

        mMachinePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MACHINE_IP, etIP.getText().toString());
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MACHINE_PORT, etPort.getText().toString());
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MACHINE_SERVER_PORT, serverPort.getText().toString());
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, CAMERA_IP, cameraIP.getText().toString());
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, HK_USER_PRE, etHKUser.getText().toString().trim());
                SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, HK_PSW_PRE, etHKPassWord.getText().toString().trim());
            }
        });
    }

    private void bindTcpService() {
        bindIntent = new Intent(mContext, MyTcpService.class);

        startService(bindIntent);
        isBind = bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    public void unBindService() {
        if (isBind && serviceConnection != null) {
            unbindService(serviceConnection);
            myTcpService.unRegisterCallBack(callBack);
            if (myBinder != null) {
                myBinder.stopWork();
            }
        }
    }

    private GroupingDialog groupingDialog;

    private void showGroupPop() {
        if (groupingDialog == null || !itemCode.equals(groupingDialog.getItemCode())) {
            if (groupingDialog != null) {
                groupingDialog.dismiss();
            }
            groupingDialog = new GroupingDialog(mContext);
            groupingDialog.setContext(mContext);
            groupingDialog.setItemCode(itemCode);
            groupingDialog.setListener(new GroupingDialog.OnGroupPopListener() {
                @Override
                public void queryStudent(String code) {
                    if (TextUtils.isEmpty(code)) {
                        return;
                    }
                    onQrArrived(code);
                }

                @Override
                public void addGroup() {
                    updateSchedules();
                    getGroupList();
                }
            });
        }
        groupingDialog.showDialog(itemList.get(mItemPosition).getItemName());

//        if (groupingPop == null) {
//            groupingPop = new GroupingPop(mContext, itemCode, new GroupingPop.OnGroupPopListener() {
//                @Override
//                public void queryStudent(String code) {
//                    if (TextUtils.isEmpty(code)) {
//                        return;
//                    }
//                    onQrArrived(code);
//                }
//
//                @Override
//                public void addGroup() {
//                    updateSchedules();
//                    getGroupList();
//                }
//            });
//        }
//        groupingPop.showGroupPop(itemList.get(mItemPosition).getItemName(), this);
    }

//    private EasyPopup groupPop;
//    private EditText groupInput;
//    private TextView groupingItem;
//    private TextView tvGroupeNo;
//    private RecyclerView rvGrouping;
//    private List<Student> groupIngStudents = new ArrayList<>();
//    private GroupingAdapter groupingAdapter;
//    private List<Group> groups;

    /**
     * 分组弹窗
     */
//    @SuppressLint("ClickableViewAccessibility")
//    private void showGroupPop2() {
//        if (groupPop == null) {
//            groupPop = EasyPopup.create()
//                    .setContentView(this, R.layout.pop_group)
//                    .setBackgroundDimEnable(true)
//                    .setDimValue(0.5f)
//                    //是否允许点击PopupWindow之外的地方消失
//                    .setFocusAndOutsideEnable(false)
//                    .setHeight(height)
//                    .setWidth(width)
//                    .apply();
//
//            groupInput = groupPop.findViewById(R.id.et_group_input);
//            Button btnQuery = groupPop.findViewById(R.id.btn_group_query);
//            groupingItem = groupPop.findViewById(R.id.tv_grouping_item);
//            rvGrouping = groupPop.findViewById(R.id.rv_grouping);
//            tvGroupeNo = groupPop.findViewById(R.id.tv_group_no);
//            Button btnCancel = groupPop.findViewById(R.id.btn_cancel);
//            Button btnSure = groupPop.findViewById(R.id.btn_sure);
//
//            groupingAdapter = new GroupingAdapter(mContext, groupIngStudents);
//            rvGrouping.setLayoutManager(new LinearLayoutManager(mContext));
//            rvGrouping.setAdapter(groupingAdapter);
//
//            ItemTouchHelperCallback helperCallback = new ItemTouchHelperCallback(groupingAdapter);
//            helperCallback.setSwipeEnable(true);
//            helperCallback.setDragEnable(true);
//            ItemTouchHelper helper = new ItemTouchHelper(helperCallback);
//            helper.attachToRecyclerView(rvGrouping);
//
//            //防止点击editText崩溃
//            groupInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
//
//            btnQuery.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (TextUtils.isEmpty(groupInput.getText())) {
//                        return;
//                    }
//                    onQrArrived(groupInput.getText().toString());
//                }
//            });
//
//            btnCancel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    groupIngStudents.clear();
//                    groupingAdapter.notifyDataSetChanged();
//                    groupInput.setText("");
//                    groupPop.dismiss();
//                }
//            });
//
//            btnSure.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (groupIngStudents.isEmpty()) {
//                        groupPop.dismiss();
//                        return;
//                    }
//                    Group group = new Group();
//                    group.setIsTestComplete(0);
//                    group.setItemCode(itemCode);
//                    group.setScheduleNo(DBManager.getInstance().queryItemSchedulesByItemCode(itemCode).get(0).getScheduleNo());
//                    group.setGroupNo(groups.size() + 1);
//                    group.setGroupType(groupIngStudents.get(0).getSex());
//                    group.setSortName("组");
//
//                    DBManager.getInstance().insertGroup(group);
//
//                    GroupItem groupItem;
//                    for (int i = 0; i < groupIngStudents.size(); i++) {
//                        groupItem = new GroupItem();
//                        groupItem.setItemCode(itemCode);
//                        groupItem.setTrackNo(i + 1);
//                        groupItem.setStudentCode(groupIngStudents.get(i).getStudentCode());
//                        groupItem.setScheduleNo(group.getScheduleNo());
//                        groupItem.setGroupNo(group.getGroupNo());
//                        groupItem.setGroupType(group.getGroupType());
//                        groupItem.setSortName("组");
//                        DBManager.getInstance().insertGroupItem(groupItem);
//                    }
//
//                    updateSchedules();
//                    getGroupList();
//
//                    groupIngStudents.clear();
//                    groupingAdapter.notifyDataSetChanged();
//                    groupInput.setText("");
//                    groupPop.dismiss();
//                }
//            });
//        }
//
//        if (groupPop != null && !groupPop.isShowing()) {
//            groupingItem.setText(itemList.get(mItemPosition).getItemName());
//            groups = DBManager.getInstance().queryGroup(itemCode);
//            tvGroupeNo.setText(String.valueOf(groups.size() + 1) + "组");
//            if (!this.isFinishing()) {
//                groupPop.showAtLocation(parentView, Gravity.CENTER, 0, 0);
//            }
//        }
//    }

    private List<List<String>> selectResults = new ArrayList<>();
    private int videoPosition;
    private String videoResult;
    private boolean hasStart = false;
    private boolean userPause = true;
    private ArrayList<String> resultTitles = new ArrayList<>();
    private Map<Integer, RoundResult> completeResults = new HashMap<>();
    private BaseVideoView mVideoView;
    private RelativeLayout rlVideo;
    private String[] timeLong;

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
                .apply();
        tableResult = mSelectPop.findViewById(R.id.vht_table_result);
        rlVideo = mSelectPop.findViewById(R.id.rl_video);

        tableResultAdapter = new VHTableResultAdapter(mContext, resultTitles, selectResults, new VHTableResultAdapter.OnResultItemLongClick() {
            @Override
            public void resultListLongClick(final int row) {
                if (!userPause || videoPosition == 0) {
                    ToastUtils.showShort("录像未暂停，无法获取成绩");
                    return;
                }
                final int track = Integer.parseInt(selectResults.get(row).get(0));
                dialogUtil.showCommonDialog(completeResults.get(track).getStudentCode() + "  获取最终成绩：" + videoResult, 0, new DialogUtil.DialogListener() {
                    @Override
                    public void onPositiveClick() {
                        if (completeResults.get(track).getCycleResult() == null) {
                            ToastUtils.showShort("无圈次成绩");
                            return;
                        }
                        ToastUtils.showShort(videoResult);
                        completeResults.get(track).setResult(videoPosition - seekTime);
                        int[] cResult = DataUtil.byteArray2RgbArray(completeResults.get(track).getCycleResult());
                        cResult[cResult.length - 1] = videoPosition - seekTime;
                        completeResults.get(track).setCycleResult(DataUtil.byteArray2RgbArray(cResult));
                        DBManager.getInstance().updateRoundResult(completeResults.get(track));
                        selectResults.get(row).set(3, videoResult);
                        selectResults.get(row).set(selectResults.get(row).size() - 1, videoResult);
                        tableResult.notifyContent();
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
            }
        });

        mVideoView = mSelectPop.findViewById(R.id.baseVideoView);
        btnVideoPlay = mSelectPop.findViewById(R.id.btn_video_play);
        btnVideoStartPause = mSelectPop.findViewById(R.id.btn_video_start_pause);
        ivControl = mSelectPop.findViewById(R.id.iv_video_control);
        tvVideoResult = mSelectPop.findViewById(R.id.tv_video_result);
//        String startTime = groupItemBeans.get(groupPosition).getGroup().getRemark1();
//        if (TextUtils.isEmpty(startTime)) {
//            rlVideo.setVisibility(View.GONE);
//        }else {
//            List<String> paths = PUtil.getFilesAllName(hkCamera.PATH);
//            timeLong = new String[0];
//            for (String path : paths
//                    ) {
//                timeLong = path.replace(".mp4", "").split("_");
//                if (timeLong.length == 2 && Long.parseLong(startTime) >= Long.parseLong(timeLong[0]) && Long.parseLong(startTime) <= Long.parseLong(timeLong[1])) {
//                    mDataSource.setData(hkCamera.PATH + path);
//                    mDataSource.setTitle("发令时刻：" + DateUtil.formatTime2(Long.parseLong(startTime), "yyyy/MM/dd HH:mm:ss:SSS"));
//                    break;
//                }
//            }
//            if (TextUtils.isEmpty(mDataSource.getData())) {
//                rlVideo.setVisibility(View.GONE);
//                return;
//            }
//        }
        updateVideo();

        ivControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = mVideoView.getState();
                if (state == IPlayer.STATE_PLAYBACK_COMPLETE)
                    return;

                if (mVideoView.isInPlaybackState()) {
                    if (userPause) {
                        mVideoView.resume();
                        userPause = false;
                    } else {
                        mVideoView.pause();
                        userPause = true;
                        videoPosition = mVideoView.getCurrentPosition();
                        if (carryMode == 0) {
                            videoResult = DateUtil.caculateTime(videoPosition - seekTime, 3, carryMode);
                        } else {
                            videoResult = DateUtil.caculateTime(videoPosition - seekTime, digital, carryMode);
                        }
                    }
                } else {
                    if (userPause) {
                        if (hasStart) {
                            mVideoView.rePlay(seekTime);
                        } else {
                            initPlay();
                        }
                        userPause = false;
                    } else {
                        mVideoView.stop();
                        userPause = true;
                    }
                }
                if (userPause) {
                    tvVideoResult.setText("成绩：" + videoResult);
                    ivControl.setImageResource(R.mipmap.ic_video_player_btn_play);
                } else {
                    tvVideoResult.setText("成绩：");
                    ivControl.setImageResource(R.mipmap.ic_video_player_btn_pause);
                }
            }
        });
        btnVideoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = mVideoView.getState();
                if (state == IPlayer.STATE_PLAYBACK_COMPLETE)
                    return;
                if (mVideoView.isInPlaybackState()) {
                    if (!userPause)
                        mVideoView.resume();
                } else {
                    mVideoView.rePlay(seekTime);
                }
                initPlay();
            }
        });
        btnVideoStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = mVideoView.getState();
                if (state == IPlayer.STATE_PLAYBACK_COMPLETE)
                    return;
                if (mVideoView.isInPlaybackState()) {
                    mVideoView.pause();
                    videoPosition = mVideoView.getCurrentPosition();
                } else {
                    mVideoView.stop();
                }
            }
        });

        mSelectPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mVideoView.stop();
                mVideoView.setDataSource(null);
                videoResult = "";
                seekTime = 0;
                hasStart = false;
                userPause = true;
                speed = 1.0f;
                ivControl.setImageResource(R.mipmap.ic_video_player_btn_play);
            }
        });
    }

    private void updateVideo() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mVideoView.getLayoutParams();
        layoutParams.width = width * 2 / 3 - 20;
        layoutParams.height = (width * 2 / 3 - 20) * 720 / 1280;
        layoutParams.setMargins(0, 0, 0, 0);
        mVideoView.setLayoutParams(layoutParams);

        mReceiverGroup = ReceiverGroupManager.get().getReceiverGroup(this);
        mReceiverGroup.getGroupValue().putBoolean(DataInter.Key.KEY_CONTROLLER_TOP_ENABLE, true);
        mVideoView.setReceiverGroup(mReceiverGroup);
        mVideoView.setEventHandler(onVideoViewEventHandler);
    }

    //快进时间
    private int seekTime;

    private void initPlay() {
        if (!hasStart) {
            String startTime = groupItemBeans.get(groupPosition).getGroup().getRemark1();
            if (TextUtils.isEmpty(startTime)) {
                ToastUtils.showShort("找不到录像文件");
                return;
            }
            List<String> paths = PUtil.getFilesAllName(hkCamera.PATH);
            if (paths == null) {
                ToastUtils.showShort("找不到录像文件");
                return;
            }
            String[] timeLong = new String[0];
            for (String path : paths
            ) {
                timeLong = path.replace(".mp4", "").split("_");
                if (timeLong.length == 2 && Long.parseLong(startTime) >= Long.parseLong(timeLong[0]) && Long.parseLong(startTime) <= Long.parseLong(timeLong[1])) {
                    mDataSource.setData(hkCamera.PATH + path);
                    mDataSource.setTitle("发令时刻：" + DateUtil.formatTime2(Long.parseLong(startTime), "yyyy/MM/dd HH:mm:ss:SSS"));
                    break;
                }
            }
            File dateFile = new File(mDataSource.getData());
            if (TextUtils.isEmpty(mDataSource.getData()) || dateFile.length() < 1000) {
                ToastUtils.showShort("找不到录像文件");
                return;
            }
            //发令时刻-视频开始录制时间=需要快进的时间（最终成绩=视频暂停时间-快进时间）
            seekTime = (int) (Long.parseLong(startTime) - Long.parseLong(timeLong[0]));
            mVideoView.setDataSource(mDataSource);
            mVideoView.start(seekTime);
            mVideoView.setSpeed(speed);
            hasStart = true;
        }
    }

    private float speed = 1;
    private OnVideoViewEventHandler onVideoViewEventHandler = new OnVideoViewEventHandler() {
        @Override
        public void onAssistHandle(BaseVideoView assist, int eventCode, Bundle bundle) {
//            super.onAssistHandle(assist, eventCode, bundle);
            switch (eventCode) {
                case DataInter.Event.EVENT_CODE_ERROR_SHOW:
                    mVideoView.stop();
                    break;
                case DataInter.Event.EVENT_CODE_REQUEST_SPEED_X_HALF:
                    speed = 0.5f;
                    if (mVideoView != null && mVideoView.isInPlaybackState()) {
                        mVideoView.setSpeed((float) 0.5);
                    }
                    break;
                case DataInter.Event.EVENT_CODE_REQUEST_SPEED_X_1:
                    speed = 1.0f;
                    if (mVideoView != null && mVideoView.isInPlaybackState()) {
                        mVideoView.setSpeed((float) 1.0);
                    }
                    break;
                case DataInter.Event.EVENT_CODE_REQUEST_SPEED_X_2:
                    speed = 2.0f;
                    if (mVideoView != null && mVideoView.isInPlaybackState()) {
                        mVideoView.setSpeed((float) 2.0);
                    }
                    break;
                case InterEvent.CODE_REQUEST_PAUSE:
                    requestPause(assist, bundle);
                    userPause = true;
                    break;
                case InterEvent.CODE_REQUEST_RESUME:
                    requestResume(assist, bundle);
                    break;
                case InterEvent.CODE_REQUEST_SEEK:
                    requestSeek(assist, bundle);
                    break;
                case InterEvent.CODE_REQUEST_STOP:
                    requestStop(assist, bundle);
                    break;
                case InterEvent.CODE_REQUEST_RESET:
                    requestReset(assist, bundle);
                    break;
                case InterEvent.CODE_REQUEST_RETRY:
                    requestRetry(assist, bundle);
                    break;
                case InterEvent.CODE_REQUEST_REPLAY:
//                    requestReplay(assist, bundle);
                    mVideoView.rePlay(seekTime);
                    ReceiverGroupManager.get().control.resetSpeed();
                    break;
                case InterEvent.CODE_REQUEST_PLAY_DATA_SOURCE:
                    requestPlayDataSource(assist, bundle);
                    break;
            }
        }

        @Override
        public void requestRetry(BaseVideoView videoView, Bundle bundle) {
            if (PUtil.isTopActivity(MiddleDistanceRaceForPersonActivity.this)) {
                super.requestRetry(videoView, bundle);
            }
        }
    };


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
                        nettyClient.setListener(MiddleDistanceRaceForPersonActivity.this);
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
                mHander.sendEmptyMessageDelayed(5, 8000);
            }
        }
        //时间同步
        mHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnect) {
                    nettyClient.sendMsgToServer(TcpConfig.getCmdUpdateDate(), MiddleDistanceRaceForPersonActivity.this);
                }
            }
        }, 3000);
    }

    //发送连接设备命令
    private void sendConnect() {
        if (nettyClient != null)
            nettyClient.sendMsgToServer(TcpConfig.CMD_CONNECT, this);
    }

    private void sendDisConnect() {
        if (nettyClient != null)
            nettyClient.sendMsgToServer(TcpConfig.getCmdEndTiming(), this);
    }

    //随便发送一个东西保持tcp不断
    private void send2() {
        if (nettyClient != null)
            nettyClient.sendMsgToServer(TcpConfig.CMD_NOTHING, null);
    }


    //初始化完成之后才允许跳转其它界面
    private boolean isInitOk = false;

    @Override
    protected void onResume() {
        super.onResume();
        mHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (hkCamera == null) {
                    hkCamera = new HkCameraManager(MiddleDistanceRaceForPersonActivity.this, camera_ip, HK_PORT, hk_user, hk_psw);
                    if (TextUtils.isEmpty(camera_ip)) {
                        isInitOk = true;
                    } else {
                        hkInit = hkCamera.initeSdk();
                        if (hkInit) {
                            hkCamera.login2HK(etHKUser.getText().toString(), etHKPassWord.getText().toString());
                            mHander.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isInitOk = true;
                                    if (hkCamera.startPreview()) {
                                        btnCamera.performClick();
                                    }
                                }
                            }, 600);
                        } else {
                            isInitOk = true;
                            ToastUtils.showShort("海康摄像头初始化失败");
                        }
                    }
                } else {
                    isInitOk = true;
                    if (hkInit) {
                        hkCamera.startPreview();
                    }
                }
            }
        }, 1500);
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
        dialogUtil.dismiss();
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
        if (hkCamera != null) {
            hkCamera.stopPreview();
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

        mHander.removeCallbacks(timeRun);
        timeRun = null;
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

        if (hkCamera != null) {
            hkCamera.loginOut();
            hkCamera.clearSdk();
        }

        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView = null;
        }

//        try {
//            if (mySocketServer != null)
//                mySocketServer.stopServerAsync();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        UdpClient.getInstance().close();
    }

    @Override
    public void onCheckIn(Student student) {
//        List<ChipGroup> chips = DBManager.getInstance().queryChipGroups();
//        if (!chips.isEmpty() && chips.get(0).getStudentNo() <= groupIngStudents.size()) {
//            toastSpeak("已达最大人数");
//            return;
//        }
        showGroupPop();
//        if (groupInput != null) {
//            groupInput.setText(student.getStudentCode());
//            groupInput.setSelection(student.getStudentCode().length());
//        }
//        if (!groupIngStudents.contains(student)) {
//            groupIngStudents.add(student);
//            groupingAdapter.notifyDataSetChanged();
//            smoothMoveToPosition(rvGrouping, groupIngStudents.size() - 1);
//        } else {
//            toastSpeak("已存在");
//        }
        groupingDialog.addStudent(student);
        groupingDialog.showDialog(itemList.get(mItemPosition).getItemName());
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
        Logger.i("解析接收到的芯片数据:" + Arrays.toString(cardIds));
        boolean isFind = false;//标记当前一轮芯片是否有效
        for (String card : cardIds
        ) {
            Log.i("card", "-------------" + card);
            ChipInfo chipInfo = DBManager.getInstance().queryChipInfoByID(card);
            if (chipInfo == null) {
                continue;
            }
            Log.i("chipInfo", "---" + chipInfo.toString());
            for (int i = 0; i < resultDataList.size(); i++) {
                if (resultDataList.get(i).getColor() == chipInfo.getColor() && resultDataList.get(i).getVestNo() == chipInfo.getVestNo()) {
                    Log.i("resultDataList", "---" + resultDataList.get(i).toString());
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
        Logger.i(TAG + "中长跑点击了开始计时" + timingLists.toString());
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
    private boolean isCameraShow = false;

    @OnClick({R.id.btn_camera, R.id.btn_circle_add, R.id.btn_circle_delete, R.id.btn_find, R.id.btn_middle_back, R.id.tv_back, R.id.btn_setting, R.id.btn_fullscreen, R.id.btn_image_connect, R.id.btn_grouping})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_camera:
                isCameraShow = !isCameraShow;
                if (isCameraShow) {
                    btnCamera.setText("隐藏录像");
                    btnCamera.setImgResource(R.drawable.btn_fullscreen_selecor);
                    SurPlayer.setVisibility(View.VISIBLE);
                } else {
                    btnCamera.setText("显示录像");
                    btnCamera.setImgResource(R.drawable.btn_fullscreen_exit_selecor);
                    SurPlayer.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_circle_add:
                if (currentRow == -1 || resultDataList.get(currentRow).getStartTime() == 0) {
                    return;
                }
                final long time = resultDataList.get(currentRow).getStartTime();
                final long useTime = System.currentTimeMillis() - time;
                Log.i("useTime", "------" + useTime);

                final String usedTime = String.valueOf(useTime);

                dialogUtil.showCommonDialog("确定加圈？", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
                    @Override
                    public void onPositiveClick() {
                        String[] result = resultDataList.get(currentRow).getResults();
                        for (int j = 0; j < resultDataList.get(currentRow).getCycle(); j++) {
                            if (TextUtils.isEmpty(result[j + 3])) {
                                if (j == 0) {
                                    if (useTime < firstTime * 1000) {
                                        Log.i("firstTime--------", "usedTime:" + usedTime);
                                        break;
                                    }
                                } else {
                                    if ((useTime - Long.parseLong(result[j + 2])) < spanTime * 1000) {
                                        Log.i("spanTime--------", "usedTime:" + (time - Long.parseLong(result[j + 2])));
                                        break;
                                    }
                                }
                                result[j + 3] = usedTime;
                                if (j == resultDataList.get(currentRow).getCycle() - 1) {
                                    result[2] = usedTime;
                                }
                                break;
                            }
                        }
                        resultShowTable.notifyContent();
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
                break;
            case R.id.btn_circle_delete:
                if (currentRow == -1 || resultDataList.get(currentRow).getStartTime() == 0) {
                    return;
                }
                dialogUtil.showCommonDialog("确定减圈？", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
                    @Override
                    public void onPositiveClick() {
                        if (currentRow == -1 || resultDataList.get(currentRow).getStartTime() == 0) {
                            return;
                        }
                        String[] result2 = resultDataList.get(currentRow).getResults();
                        for (int j = 3; j < result2.length; j++) {
                            if (TextUtils.isEmpty(result2[j])) {
                                if (j > 3) {
                                    result2[j - 1] = "";
                                }
                                break;
                            } else {
                                //删除最后一个成绩，同时最终成绩也一起删掉
                                if (j == result2.length - 1) {
                                    result2[j] = "";
                                    result2[2] = "";
                                }
                            }
                        }
                        resultShowTable.notifyContent();
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
                break;
            case R.id.btn_find:
                showInput();
                break;
            case R.id.btn_middle_back:
            case R.id.tv_back:
                onBackPressed();
                break;
            case R.id.btn_setting:
                if (!isInitOk) {
                    ToastUtils.showShort("正在自动初始化，请稍候几秒");
                    return;
                }

                for (TimingBean timingBean : timingLists
                ) {
                    if (timingBean.getState() == TIMING_STATE_WAITING || timingBean.getState() == TIMING_STATE_TIMING) {
                        toastSpeak("考试中，请勿跳转其它界面");
                        return;
                    }
                }
                startProjectSetting();
                break;
            case R.id.btn_fullscreen:
                isShow = !isShow;
                if (isShow) {
                    hkCamera.ChangeSurFace_Center();
                    btnFullscreen.setText("隐藏组别");
                    btnFullscreen.setImgResource(R.drawable.btn_fullscreen_selecor);
                    llShowItem.setVisibility(View.VISIBLE);
                } else {
                    hkCamera.ChangeSurFace_Left();
                    btnFullscreen.setText("显示组别");
                    btnFullscreen.setImgResource(R.drawable.btn_fullscreen_exit_selecor);
                    llShowItem.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_image_connect:
                mMachinePop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                break;
            case R.id.btn_grouping:
                showGroupPop();
                break;

        }
    }

    /**
     * 小组定位
     */
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
            toastSpeak("请先连接设备");
            return;
        }

        if (cycleNo == 0) {
            toastSpeak("请先设置圈数");
            return;
        }

        if (timingLists.get(position).getNo() != 0) {
            raceTimingAdapter.notifyBackGround(holder, TIMING_STATE_WAITING);
        } else {
            toastSpeak("请先选入组别");
            return;
        }

        Logger.i(TAG + "中长跑点击了等待发令" + timingLists.get(position).toString());

        if (hkCamera != null && !hkCamera.m_bSaveRealData) {
            hkCamera.startRecord(System.currentTimeMillis());
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
//            if (timingLists.get(position).getNo() == groupItemBeans.get(i).getGroup().getGroupNo()) {
            if (timingLists.get(position).getItemGroupName().equals(groupItemBeans.get(i).getGroupItemName())) {
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
        dialogUtil.showCommonDialog("是否违规返回", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                Logger.i(TAG + "中长跑点击了违规返回按钮" + timingLists.get(position).toString());
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
                for (int i = 0; i < groupItemBeans.size(); i++) {
//                    if (timingLists.get(position).getNo() == groupItemBeans.get(i).getGroup().getGroupNo()) {
                    if (timingLists.get(position).getItemGroupName().equals(groupItemBeans.get(i).getGroupItemName())) {
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
        //先判断列表中是否有未结束的
        for (RaceResultBean resultBean2 : resultDataList
        ) {
            if (resultBean2.getColor() == timingLists.get(position).getColor()) {
                //最终成绩为空表示未结束
                if (TextUtils.isEmpty(resultBean2.getResults()[2])) {
                    showFinishDialog(position, holder, "本组成绩未接收完整，是否结束？", android.R.drawable.ic_dialog_alert);
                    return;
                }
            }
        }
        showFinishDialog(position, holder, "是否完成计时？", android.R.drawable.ic_dialog_info);
    }

    private List<UploadResults> uploadResults = new ArrayList<>();

    private void showFinishDialog(final int position, final RaceTimingAdapter.VH holder, String notice, int bitmapId) {
        dialogUtil.showCommonDialog(notice, bitmapId, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                //当前所有组是否还存在正在计时或者等待中的小组，否则停止录像
                Logger.i(TAG + "中长跑点击了完成计时按钮" + timingLists.get(position).toString());
                timingLists.get(position).setState(TimingBean.TIMING_STATE_COMPLETE);

                for (int i = 0; i < timingLists.size(); i++) {
                    if (timingLists.get(i).getState() == TIMING_STATE_WAITING || timingLists.get(i).getState() == TIMING_STATE_TIMING) {
                        break;
                    } else if (i == timingLists.size() - 1) {
                        if (hkCamera != null) {
                            hkCamera.stopRecord(System.currentTimeMillis());
                        }
                    }
                }

                long startTime = timingLists.get(position).getTime();
                uploadResults.clear();
                List<RoundResult> roundResults = new ArrayList<>();

                Group dbGroupList = null;
                if (timingLists.get(position).getItemCode().equals(itemList.get(mItemPosition).getItemCode())) {
                    //更新项目组状态
                    for (GroupItemBean groupItemBean : groupItemBeans
                    ) {
                        if (timingLists.get(position).getItemGroupName().equals(groupItemBean.getGroupItemName())) {
                            //当前组完成之后更新状态并移除
                            groupItemBean.getGroup().setIsTestComplete(GROUP_FINISH);
                            groupItemBean.getGroup().setRemark1(startTime + "");//增加发令时刻
                            DBManager.getInstance().updateGroup(groupItemBean.getGroup());
                            groupItemBeans.remove(groupItemBean);
                            groupAdapter.notifyDataSetChanged();
                            dbGroupList = groupItemBean.getGroup();
                            break;
                        }
                    }
                } else {
                    dbGroupList = DBManager.getInstance().getGroupByNo(timingLists.get(position).getItemCode(), timingLists.get(position).getNo(), timingLists.get(position).getColor());

                    dbGroupList.setIsTestComplete(GROUP_FINISH);
                    dbGroupList.setRemark1(startTime + "");//增加发令时刻
                    DBManager.getInstance().updateGroup(dbGroupList);
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
                        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
                        roundResult = DBManager.getInstance().insertRoundResult2(roundResult);

                        roundResults.add(roundResult);

                        uploadResult = new UploadResults(dbGroupList.getScheduleNo(), resultBean2.getItemCode()
                                , resultBean2.getStudentCode(), "1", dbGroupList, RoundResultBean.beanCope2(roundResult, dbGroupList));//需要上传的成绩对象

//                        uploadResult.setGroupNo(resultBean2.getNo());
//                        uploadResult.setSiteScheduleNo(dbGroupList.getScheduleNo());
//                        uploadResult.setRoundResultList(RoundResultBean.beanCope2(roundResult));
//                        uploadResult.setStudentCode(resultBean2.getStudentCode());
//                        uploadResult.setTestNum("1");
//                        uploadResult.setExamItemCode(resultBean2.getItemCode());
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

                raceTimingAdapter.notifyItemChanged(position);
                raceTimingAdapter.notifyBackGround(holder, TimingBean.TIMING_STATE_COMPLETE);
                //自动上传成绩
                if (SettingHelper.getSystemSetting().isRtUpload()) {
                    if (itemName != null) {
                        ServerMessage.uploadZCPResult(mContext, itemName, uploadResults);
                    }
                }
                Logger.i(TAG + "成绩:" + roundResults.toString());
                //自动打印
                if (SettingHelper.getSystemSetting().isAutoPrint() &&
                        (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4 || SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_CUSTOM_APP)) {
                    InteractUtils.printA4Result(mContext, dbGroupList);
                } else {
                    MiddlePrintUtil.print(roundResults, completeBeans, digital, carryMode);
                }
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
        dialogUtil.showCommonDialog("是否删除当前组", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                Logger.i(TAG + "中长跑点击了删除按钮" + timingLists.get(position).toString());
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

    /**
     * 组别操作弹窗
     */
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
                    toastSpeak("请先设置颜色组");
                    mCirclePop.dismiss();
                    return;
                }
                //颜色组设置的人数必须>=当前选择组别的人数
                if (colorGroups.get(mColorGroupPosition).getStudentNo() < groupItemBeans.get(groupPosition).getGroupItems().size()) {
                    toastSpeak("人数不匹配");
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
                    toastSpeak("请先设置颜色组");
                    mCirclePop.dismiss();
                    return;
                }

                //颜色组设置的人数必须>=当前选择组别的人数
                if (colorGroups.get(mColorGroupPosition).getStudentNo() < groupItemBeans.get(groupPosition).getGroupItems().size()) {
                    toastSpeak("人数不匹配");
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
        String[] selectItems = new String[]{"查看成绩", "上传成绩", "打印成绩"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(this);
        listDialog.setTitle(groupName);
        listDialog.setItems(selectItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        selectResults.clear();
                        resultTitles.clear();
                        completeResults.clear();
                        resultTitles.add("道次");
                        resultTitles.add("考号");
                        resultTitles.add("姓名");
                        resultTitles.add("最终成绩");

                        List<GroupItem> groupItems = groupItemBeans.get(groupPosition).getGroupItems();
                        SelectResultBean selectResultBean;
                        List<String> strings;
                        int resultCycles = 0;
                        for (GroupItem groupItem : groupItems
                        ) {
                            Student student = DBManager.getInstance().queryStudentByStuCode(groupItem.getStudentCode());
                            selectResultBean = new SelectResultBean();
                            selectResultBean.setStudentName(student.getStudentName());
                            selectResultBean.setStudentCode(student.getStudentCode());
                            selectResultBean.setSex(student.getSex());
                            selectResultBean.setTrackNo(groupItem.getTrackNo());

                            RoundResult roundResult = DBManager.getInstance().queryResultByStudentCode(groupItem.getStudentCode(), groupItem.getItemCode(), groupItemBeans.get(groupPosition).getGroup().getId());
                            completeResults.put(groupItem.getTrackNo(), roundResult);
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
                                resultCycles = results.length;
                                for (int result : results
                                ) {
                                    String resultString;
                                    if (carryMode == 0) {
                                        resultString = DateUtil.caculateTime(result, 3, carryMode);
                                    } else {
                                        resultString = DateUtil.caculateTime(result, digital, carryMode);
                                    }
                                    strings.add(resultString);
                                }

                                if (resultCycles < cycleNo) {
                                    for (int i = resultCycles; i < cycleNo; i++) {
                                        strings.add("X");
                                    }
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
                        for (int i = 1; i < resultCycles + 1; i++) {
                            resultTitles.add("第" + i + "圈");
                        }

                        if (resultCycles < cycleNo) {
                            for (int i = resultCycles; i < cycleNo; i++) {
                                resultTitles.add("第" + (i + 1) + "圈");
                            }
                        }

                        if (mSelectPop == null) {
                            initSelectPop();
                            tableResult.setAdapter(tableResultAdapter);
                        } else {
                            tableResult.notifyContent();
                        }
                        updateVideo();
                        mSelectPop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                        break;
                    case 1:
                        uploadResults.clear();
                        List<GroupItem> groupItems2 = groupItemBeans.get(groupPosition).getGroupItems();
                        Group group = groupItemBeans.get(groupPosition).getGroup();
                        UploadResults uploadResult;
                        String itemName = DBManager.getInstance().queryItemByCode(group.getItemCode()).getItemName();
                        for (GroupItem groupItem : groupItems2
                        ) {
                            RoundResult roundResult = DBManager.getInstance().queryResultByStudentCode(groupItem.getStudentCode(), groupItem.getItemCode(), groupItemBeans.get(groupPosition).getGroup().getId());
                            uploadResult = new UploadResults(groupItem.getScheduleNo(), roundResult.getItemCode()
                                    , groupItem.getStudentCode(), "1", group, RoundResultBean.beanCope2(roundResult, group));//需要上传的成绩对象
                            uploadResults.add(uploadResult);
                        }
                        if (itemName != null) {
                            ServerMessage.uploadZCPResult(mContext, itemName, uploadResults);
                        }
                        break;
                    case 2:
                        MiddlePrintUtil.print2(mContext, groupItemBeans.get(groupPosition), digital, carryMode);
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
                                    toastSpeak("该组已选入比赛");
                                    return;
                                }
                            }
                            addToTiming();
                            break;
                        } else if (testComplete == GROUP_FINISH) {
                            toastSpeak("该组已完成");
                            break;
                        }
                        colorGroupAdapter.notifyDataSetChanged();
                        tvGroupName.setText(groupName);
                        mCirclePop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                        break;
                    case 1:
                        if (testComplete == GROUP_3) {
                            toastSpeak("该组已选入比赛");
                            break;
                        } else if (testComplete == GROUP_FINISH) {
                            toastSpeak("该组已完成");
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
                                    toastSpeak("该组已存在");
                                    return;
                                }
                            }
                            addToTiming();
                        } else if (testComplete == GROUP_FINISH) {
                            toastSpeak("该组已完成");
                        } else if (testComplete == GROUP_4) {
                            addToTiming();
                        } else {
                            toastSpeak("请先关联颜色组");
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
                toastSpeak("当前颜色组已存在");
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
                    toastSpeak("计时器已满");
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
            raceResultBean.setVestNo(groupItems.get(i).getTrackNo());
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

        Logger.i("当前选中人员信息:" + resultDataList.toString());
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
