package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
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
import com.feipulai.exam.activity.MiddleDistanceRace.bean.GroupItemBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.RaceResultBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.SelectResultBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.ServiceTcpBean;
import com.feipulai.exam.activity.MiddleDistanceRace.vhtableview.VHTableAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.vhtableview.VHTableResultAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.vhtableview.VHTableView;
import com.feipulai.exam.activity.base.MiddleBaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.ScheduleAdapter;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
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
import java.lang.reflect.Array;
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
 * ??????????????????????????????????????????
 * ??????????????????????????????????????????   ????????????:??????
 */
public class MiddleDistanceRaceForGroupActivity extends MiddleBaseTitleActivity implements UdpClient.UDPChannelListerner, NettyListener, RaceTimingAdapter.MyClickListener, ChannelFutureListener, MiddleRaceGroupAdapter.OnItemClickListener, ColorSelectAdapter.OnItemClickListener, AdapterView.OnItemSelectedListener {

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
    @BindView(R.id.btn_camera)
    ImageTextButton btnCamera;
    @BindView(R.id.Sur_Player)
    SurfaceView SurPlayer;
    private String TAG = "MiddleDistanceRaceForGroupActivity";
    private final int MESSAGE_A = 1;
    private boolean isFlag = true;
    private int mItemPosition = 0;
    private int groupStatePosition = 0;
    private int schedulePosition = 0;
    private boolean isAutoSelect = true;//spinner????????????????????????????????????????????????

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
                        //????????????????????????????????????????????????????????????????????????????????????
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
            if (dialogUtil == null || MiddleDistanceRaceForGroupActivity.this.isDestroyed()) {
                return;
            }
            //?????????????????????????????????
            //????????????????????????????????????????????????15M/sec
            freeSpace = new BigDecimal((float) FileUtil.getFreeSpaceStorage() / (1024 * 1024 * 1024)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            freeTime = new BigDecimal(freeSpace * 1024 / (15 * 60)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            //???????????????????????????????????????1????????????????????????
            if (freeTime < 1) {
                showSpaceNotice();
            } else {
                ToastUtils.showShort("??????????????????????????????????????????" + freeTime + "??????");
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
    //    private ResultShowAdapter resultAdapter;
    private EasyPopup mMachinePop;
    private EditText etIP;
    private EditText etPort;
    private Button btnConnect;
    private Button btnSyncTime;
    private RadioGroup rgVersion;
    private Context mContext;
    private int firstTime;//??????????????????????????????
    private int spanTime;//????????????????????????
    private int height;
    private EasyPopup mSelectPop;
    //    private ScrollablePanel resultScroll;
    private int carryMode;
    private int digital;
    private boolean isChange = false;
    public static MiddleDistanceRaceForGroupActivity instance;
    private int width;
    private String machine_ip;
    private String machine_port;
    private String server_Port;
    private Intent bindIntent;
    //    private long lastServiceTime;
    private Button btndisConnect;
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
    private RelativeLayout rlVideo;
    private TextView tvVideoResult;

    @Override
    protected int setLayoutResID() {
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

        height = DisplayUtil.getScreenHightPx(this);
        width = DisplayUtil.getScreenWidthPx(this);

        dialogUtil = new DialogUtil(mContext);

        mHander.postDelayed(timeRun, 60 * 1000 * 30);

        btnSetting.setText("??????");
        btnSetting.setImgResource(R.drawable.btn_setting_selecor);
        imageConnect.setText("????????????");
        imageConnect.setImgResource(R.drawable.btn_connect_selecor);
        btnFullscreen.setText("????????????");
        btnFullscreen.setImgResource(R.drawable.btn_fullscreen_selecor);
        btnCamera.setText("????????????");
        btnCamera.setImgResource(R.drawable.btn_fullscreen_selecor);
        btnGrouping.setText("??????");
        btnGrouping.setImgResource(R.drawable.ic_launcher);
        btnGrouping.setImgResource(R.mipmap.grouping);
        btnCircleDelete.setText("?????????");
        btnCircleDelete.setImgResource(R.drawable.btn_delete_selecor);
        btnCircleAdd.setText("?????????");
        btnCircleAdd.setImgResource(R.drawable.btn_add_selecor);


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
        hk_user = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, HK_USER_PRE, HK_USER);
        hk_psw = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, HK_PSW_PRE, HK_PSW);
//        lastServiceTime = SharedPrefsUtil.getValue(mContext, MyTcpService.SERVICE_CONNECT, MyTcpService.SERVICE_CONNECT, 0L);
        //???????????????recycleView
        groupAdapter = new MiddleRaceGroupAdapter(groupItemBeans);
        rvRaceStudentGroup.setLayoutManager(new LinearLayoutManager(this));
        rvRaceStudentGroup.setAdapter(groupAdapter);

        initCamera();
        //???????????????
        timers = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MIDDLE_RACE_NUMBER, 3);

        TimingBean timingBean;
        for (int i = 0; i < timers; i++) {
            timingBean = new TimingBean(0, 0, 0, "", "", 0, 0);
            timingLists.add(timingBean);
        }

        //?????????recycleView
        raceTimingAdapter = new RaceTimingAdapter(this, timingLists, this);
        rvRaceGroup.setLayoutManager(new LinearLayoutManager(this));
        rvRaceGroup.setAdapter(raceTimingAdapter);

        resultDataList = new ArrayList<>();

        //???????????????
        titleData.add("??????");
        titleData.add("??????");
        titleData.add("????????????");
        for (int i = 3; i < cycleNo + 3; i++) {
            titleData.add("???" + (i - 2) + "???");
        }

        final VHTableAdapter tableShowAdapter = new VHTableAdapter(this, titleData, resultDataList, carryMode, digital, new VHTableAdapter.OnResultItemLongClick() {
            @Override
            public void resultListLongClick(int row, int state) {
                Log.i("resultListLongClick", row + "---" + state);
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

        //??????????????????
        initConnectPop();

        //????????????????????????????????????12???????????????????????????????????????
//        if (!TextUtils.isEmpty(server_Port) && lastServiceTime != 0 && (System.currentTimeMillis() - lastServiceTime) < 12 * 60 * 60 * 1000) {
//            currentPort = Integer.parseInt(server_Port);
//            bindTcpService();
//        }
    }

    private DataSource mDataSource;
    private HkCameraManager hkCamera;
    private boolean hkInit;
    private BaseVideoView mVideoView;

    private void initCamera() {
        camera_ip = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, CAMERA_IP, "");
        mDataSource = new DataSource();
    }

    private void showSpaceNotice() {
        dialogUtil.showCommonDialog("????????????????????????" + freeSpace + "G,???????????????" + freeTime + "??????", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
            }

            @Override
            public void onNegativeClick() {
            }
        });
    }

    private int cycleNo = 0;//??????????????????

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
     * ????????????
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
    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.UPLOAD_RESULT_SUCCEED || baseEvent.getTagInt() == EventConfigs.UPLOAD_RESULT_FAULT) {
            refreshItemList();
        }
    }
    /**
     * ???????????????????????????????????????????????????????????????
     */
    public void refreshItemList() {
        String itemName = itemList.get(mItemPosition).getItemName();
        itemList = DBManager.getInstance().queryItemsByMachineCode(TestConfigs.sCurrentItem.getMachineCode());
//        for (Item item0 : itemList
//        ) {
//            item0.setItemCode(DBManager.getInstance().queryItemByName(item0.getItemName()).getItemCode());
//        }
        for (int i = 0; i < itemList.size(); i++) {
            items[i] = itemList.get(i).getItemName();
            if (itemName.equals(items[i])) {
                mItemPosition = i;
            }
        }
//        Log.i("spinnerItemSelected", Arrays.toString(items));
//        itemAdapter.notifyDataSetChanged();
        itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, items);
        spRaceItem.setAdapter(itemAdapter);
        spRaceItem.setSelection(mItemPosition);
    }

    /**
     * ??????????????????
     */
    private void getGroupList() {
//        Log.i("spinnerItemSelected", "getGroupList------------");
        groupItemBeans.clear();
        if (scheduleNo == null || scheduleNo.isEmpty()) {
            return;
        }

        final String itemCode = itemList.get(mItemPosition).getItemCode();
        if (TextUtils.isEmpty(itemCode)) {
            ToastUtils.showShort("??????????????????");
            groupItemBeans.clear();
            groupAdapter.notifyDataSetChanged();
        } else {
//            Log.i("spinnerItemSelected", "->" + scheduleNo + "->" + itemCode + "->" + groupStatePosition);
//            Log.i("spinnerItemSelected", itemList.toString());
//            TestConfigs.sCurrentItem.setItemCode(itemCode);
//            TestConfigs.sCurrentItem = DBManager.getInstance().queryItemByCode(itemCode);
            DataBaseExecutor.addTask(new DataBaseTask(mContext, getString(R.string.loading_hint), true) {
                @Override
                public DataBaseRespon executeOper() {
                    List<Group> dbGroupList = DBManager.getInstance().getGroupByScheduleNoAndItem(scheduleNo, itemCode, groupStatePosition);
                    for (Group group : dbGroupList
                    ) {
                        String sex = "";
                        switch (group.getGroupType()) {
                            case 0:
                                sex = "??????";
                                break;
                            case 1:
                                sex = "??????";
                                break;
                            case 2:
                                sex = "??????";
                                break;
                            default:
                                break;
                        }
                        String itemGroupName = sex + itemList.get(mItemPosition).getItemName() + "???" + group.getGroupNo() + "???";
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
                toastSpeak("????????????????????????");
                return;
            }
        }
        clickBack();
    }

    private void clickBack() {
        dialogUtil.showCommonDialog("???????????????", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
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
            Log.i("postMessage", message.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getItems();
                    ToastUtils.showLong("??????????????????????????????????????????");
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
     * ????????????
     */
    private void initConnectPop() {
        mMachinePop = EasyPopup.create()
                .setContentView(this, R.layout.pop_machine_connect)
                .setBackgroundDimEnable(true)
                .setDimValue(0.5f)
                //??????????????????PopupWindow?????????????????????
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
        serverIP.setText("????????????" + NetWorkUtils.getLocalOrWlanIp());
        serverPort.setText(server_Port);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(serverPort.getText())) {
                    ToastUtils.showShort("??????????????????");
                    return;
                }
                currentPort = Integer.parseInt(serverPort.getText().toString());
                //??????????????????????????????
                if (!server_Port.equals(serverPort.getText().toString())) {
                    myBinder.stopWork();
                    myBinder.startWork(currentPort);
                }

                if (myTcpService != null && myTcpService.isWork) {
                    ToastUtils.showShort("?????????????????????");
                    return;
                }
                bindTcpService();
                //??????????????????????????????????????????12????????????????????????activity???????????????????????????????????????????????????????????????
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
                    ToastUtils.showShort("IP????????????");
                    return;
                }
                if (isConnect) {
                    ToastUtils.showShort("???????????????");
                    return;
                }
                //????????????
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
                    toastSpeak("??????????????????");
                    return;
                }
                nettyClient.sendMsgToServer(TcpConfig.getCmdUpdateDate(), MiddleDistanceRaceForGroupActivity.this);
                ToastUtils.showShort("????????????");
            }
        });

        btnStartCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(cameraIP.getText())) {
                    ToastUtils.showShort("?????????????????????ip");
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
                    ToastUtils.showShort("??????????????????????????????");
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


    private List<List<String>> selectResults = new ArrayList<>();
    private int videoPosition;
    private String videoResult;
    private boolean hasStart = false;
    private boolean userPause = true;
    private ArrayList<String> resultTitles = new ArrayList<>();

    /**
     * ?????????????????????pop
     */
    private void initSelectPop() {
        mSelectPop = EasyPopup.create()
                .setContentView(this, R.layout.pop_race_select)
                .setBackgroundDimEnable(true)
                .setDimValue(0.5f)
                //??????????????????PopupWindow?????????????????????
                .setFocusAndOutsideEnable(true)
                .apply();
        tableResult = mSelectPop.findViewById(R.id.vht_table_result);

        tableResultAdapter = new VHTableResultAdapter(mContext, resultTitles, selectResults, new VHTableResultAdapter.OnResultItemLongClick() {
            @Override
            public void resultListLongClick(final int row) {
                if (!userPause || videoPosition == 0) {
                    ToastUtils.showShort("????????????????????????????????????");
                    return;
                }
                final int track = Integer.parseInt(selectResults.get(row).get(0));
                dialogUtil.showCommonDialog(completeResults.get(track).getStudentCode() + "  ?????????????????????" + videoResult, 0, new DialogUtil.DialogListener() {
                    @Override
                    public void onPositiveClick() {
                        if (completeResults.get(track).getCycleResult() == null) {
                            ToastUtils.showShort("???????????????");
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
        rlVideo = mSelectPop.findViewById(R.id.rl_video);
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
//                    mDataSource.setTitle("???????????????" + DateUtil.formatTime2(Long.parseLong(startTime), "yyyy/MM/dd HH:mm:ss:SSS"));
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
                    tvVideoResult.setText("?????????" + videoResult);
                    ivControl.setImageResource(R.mipmap.ic_video_player_btn_play);
                } else {
                    tvVideoResult.setText("?????????");
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

    //????????????
    private int seekTime;

    private void initPlay() {
        if (!hasStart) {
            String startTime = groupItemBeans.get(groupPosition).getGroup().getRemark1();
            if (TextUtils.isEmpty(startTime)) {
                ToastUtils.showShort("?????????????????????");
                return;
            }
            List<String> paths = PUtil.getFilesAllName(hkCamera.PATH);
            if (paths == null) {
                ToastUtils.showShort("?????????????????????");
                return;
            }
            String[] timeLong = new String[0];
            for (String path : paths
            ) {
                timeLong = path.replace(".mp4", "").split("_");
                if (timeLong.length == 2 && Long.parseLong(startTime) >= Long.parseLong(timeLong[0]) && Long.parseLong(startTime) <= Long.parseLong(timeLong[1])) {
                    mDataSource.setData(hkCamera.PATH + path);
                    mDataSource.setTitle("???????????????" + DateUtil.formatTime2(Long.parseLong(startTime), "yyyy/MM/dd HH:mm:ss:SSS"));
                    break;
                }
            }
            File dateFile = new File(mDataSource.getData());
            if (TextUtils.isEmpty(mDataSource.getData()) || dateFile.length() < 1000) {
                ToastUtils.showShort("?????????????????????");
                return;
            }
            //????????????-????????????????????????=????????????????????????????????????=??????????????????-???????????????
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
            if (PUtil.isTopActivity(MiddleDistanceRaceForGroupActivity.this)) {
                super.requestRetry(videoView, bundle);
            }
        }
    };


    private boolean isIntentFlag = false;//??????????????????????????????onPause??????????????????

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

    private boolean isConnect = false;//????????????????????????
    private boolean isFirst = true;

    //????????? ????????????
    private void initSocket(final String ip, final int port) {
        if (nettyClient == null) {
            mHander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isFirst = true;
                    nettyClient = new NettyClient(ip, port);
                    if (!nettyClient.getConnectStatus()) {
                        nettyClient.setListener(MiddleDistanceRaceForGroupActivity.this);
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
                mHander.sendEmptyMessageDelayed(2, 300);//???????????????????????????
//                mHander.sendEmptyMessageDelayed(6, 1000);//?????????????????????
                mHander.sendEmptyMessageDelayed(5, 8000);
            }
        }
        //????????????
        mHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnect) {
                    nettyClient.sendMsgToServer(TcpConfig.getCmdUpdateDate(), MiddleDistanceRaceForGroupActivity.this);
                }
            }
        }, 3000);
    }

    //????????????????????????
    private void sendConnect() {
        if (nettyClient != null)
            nettyClient.sendMsgToServer(TcpConfig.CMD_CONNECT, this);
    }

    //????????????????????????
    private void sendDisConnect() {
        if (nettyClient != null)
            nettyClient.sendMsgToServer(TcpConfig.getCmdEndTiming(), this);
    }

    //??????????????????????????????tcp??????
    private void send2() {
        if (nettyClient != null)
            nettyClient.sendMsgToServer(TcpConfig.CMD_NOTHING, null);
    }

    //????????????????????????????????????????????????
    private boolean isInitOk = false;

    @Override
    protected void onResume() {
        super.onResume();
        mHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (hkCamera == null) {
                    hkCamera = new HkCameraManager(MiddleDistanceRaceForGroupActivity.this, camera_ip, HK_PORT, hk_user, hk_psw);
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
                            ToastUtils.showShort("??????????????????????????????");
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

            //???????????????
            titleData.add("??????");
            titleData.add("??????");
            titleData.add("????????????");
            for (int i = 3; i < cycleNo + 3; i++) {
                titleData.add("???" + (i - 2) + "???");
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
        //???????????????????????????????????????????????????????????????????????????
        if (isIntentFlag && nettyClient != null) {
            //??????????????????
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
        //??????????????????
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
    public void channelInactive() {

    }

    @Override
    public void onDataArrived(UDPResult result) {

    }

    /**
     * ??????????????????????????????  ?????? ?????????
     *
     * @param msg
     */
    @Override
    public void onMessageResponse(final Object msg) {

    }

    @Override
    public void onMessageReceive(long time, final String[] cardIds) {
        boolean isFind = false;//????????????????????????????????????
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
            if (isFind) {//?????????????????????????????????????????????????????????????????????????????????
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
            TtsManager.getInstance().speak("??????????????????");
            mHander.sendMessage(mHander.obtainMessage(1, text));
            isFlag = false;
        }
    }

    @Override
    public void onMessageFailed(Object msg) {

    }


    @Override
    public void onStartTiming(long time) {
        Logger.i(TAG + "??????????????????????????????" + timingLists.toString());
//        Log.i("onMessageResponse", "????????????---------" + time);
        int timerNo = 0;
        for (TimingBean timing : timingLists
        ) {
            //?????????????????????????????????????????????
            if (timing.getState() == TIMING_STATE_WAITING) {
                timing.setState(TIMING_START);//????????????
                timing.setTime(time);
                //?????????????????????????????????????????????
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
        //????????????????????????
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
                        ToastUtils.showShort("??????????????????");
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
    private boolean isCameraShow = false;

    @OnClick({R.id.btn_camera, R.id.btn_circle_add, R.id.btn_circle_delete, R.id.btn_find, R.id.btn_middle_back, R.id.tv_back, R.id.btn_setting, R.id.btn_fullscreen, R.id.btn_image_connect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_camera:
                isCameraShow = !isCameraShow;
                if (isCameraShow) {
                    btnCamera.setText("????????????");
                    btnCamera.setImgResource(R.drawable.btn_fullscreen_selecor);
                    SurPlayer.setVisibility(View.VISIBLE);
                } else {
                    btnCamera.setText("????????????");
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

                dialogUtil.showCommonDialog("???????????????", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
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
                dialogUtil.showCommonDialog("???????????????", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
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
                                //????????????????????????????????????????????????????????????
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
                    ToastUtils.showShort("???????????????????????????????????????");
                    return;
                }

                for (TimingBean timingBean : timingLists
                ) {
                    if (timingBean.getState() == TIMING_STATE_WAITING || timingBean.getState() == TIMING_STATE_TIMING) {
                        toastSpeak("????????????????????????????????????");
                        return;
                    }
                }
                startProjectSetting();
                break;
            case R.id.btn_fullscreen:
                isShow = !isShow;
                if (isShow) {
                    hkCamera.ChangeSurFace_Center();
                    btnFullscreen.setText("????????????");
                    btnFullscreen.setImgResource(R.drawable.btn_fullscreen_selecor);
                    llShowItem.setVisibility(View.VISIBLE);
                } else {
                    hkCamera.ChangeSurFace_Left();
                    btnFullscreen.setText("????????????");
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("?????????????????????????????????").setView(editText)
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String number = editText.getText().toString();
                        if (TextUtils.isEmpty(number)) {
                            ToastUtils.showShort("????????????");
                            return;
                        }
                        for (int j = 0; j < groupItemBeans.size(); j++) {
                            if (number.equals(groupItemBeans.get(j).getGroup().getGroupNo() + "")) {
                                smoothMoveToPosition(rvRaceStudentGroup, j);
                                break;
                            } else {
                                if (j == groupItemBeans.size() - 1) {
                                    ToastUtils.showShort("????????????");
                                }
                            }
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * ?????????????????????
     */
    private void smoothMoveToPosition(RecyclerView mRecyclerView, final int position) {
        // ?????????????????????
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // ????????????????????????
        int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
        if (position < firstItem) {
            // ???????????????:???????????????????????????????????????????????????smoothScrollToPosition
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // ???????????????:????????????????????????????????????????????????????????????????????????
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                // smoothScrollToPosition ??????????????????????????????smoothScrollBy????????????????????????
                mRecyclerView.smoothScrollBy(0, top);
            }
        } else {
            // ???????????????:???????????????????????????????????????????????????smoothScrollToPosition??????????????????????????????????????????
            // ?????????onScrollStateChanged??????????????????smoothMoveToPosition????????????????????????????????????
            mRecyclerView.smoothScrollToPosition(position);
        }
    }

    /**
     * ????????????????????????????????????
     */
    @Override
    public void clickTimingWaitListener(int position, final RaceTimingAdapter.VH holder) {
        if (!isConnect) {
            toastSpeak("??????????????????");
            return;
        }

        if (cycleNo == 0) {
            toastSpeak("??????????????????");
            return;
        }

        if (timingLists.get(position).getNo() != 0) {
            raceTimingAdapter.notifyBackGround(holder, TIMING_STATE_WAITING);
        } else {
            toastSpeak("??????????????????");
            return;
        }
        Logger.i(TAG + "??????????????????????????????" + timingLists.get(position).toString());

        if (hkCamera != null && !hkCamera.m_bSaveRealData) {
            hkCamera.startRecord(System.currentTimeMillis());
        }

        timingLists.get(position).setState(TIMING_STATE_WAITING);

        //??????????????????????????????????????????????????????
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
//           &&timingLists.get(position).getNo() == groupItemBeans.get(i).getGroup().getGroupNo()
            if (timingLists.get(position).getItemGroupName().equals(groupItemBeans.get(i).getGroupItemName())) {
                groupItemBeans.get(i).getGroup().setIsTestComplete(GROUP_WAIT);
                groupAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * ????????????????????????????????????
     */
    @Override
    public void clickTimingBackListener(final int position, final RaceTimingAdapter.VH holder) {
        dialogUtil.showCommonDialog("??????????????????", android.R.drawable.ic_dialog_alert, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                Logger.i(TAG + "????????????????????????????????????" + timingLists.get(position).toString());
                raceTimingAdapter.notifyBackGround(holder, TIMING_STATE_BACK);
                timingLists.get(position).setState(TIMING_STATE_BACK);
//                raceTimingAdapter.notifyDataSetChanged();
                //?????????????????????
                for (RaceResultBean resultBean2 : resultDataList
                ) {
                    if (resultBean2.getColor() == timingLists.get(position).getColor()) {
                        resultBean2.setStartTime(0);
                    }
                }
                Log.i("clickTimingBackListener", timingLists.toString());

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
     * ????????????????????????????????????
     */
    @Override
    public void clickTimingCompleteListener(final int position, final RaceTimingAdapter.VH holder) {
        //???????????????????????????????????????
        for (RaceResultBean resultBean2 : resultDataList
        ) {
            if (resultBean2.getColor() == timingLists.get(position).getColor()) {
                //?????????????????????????????????
                if (TextUtils.isEmpty(resultBean2.getResults()[2])) {
                    showFinishDialog(position, holder, "?????????????????????????????????????????????", android.R.drawable.ic_dialog_alert);
                    return;
                }
            }
        }
        showFinishDialog(position, holder, "?????????????????????", android.R.drawable.ic_dialog_info);
    }

    private List<UploadResults> uploadResults = new ArrayList<>();

    private void showFinishDialog(final int position, final RaceTimingAdapter.VH holder, String notice, int ic_dialog_info) {
        dialogUtil.showCommonDialog(notice, ic_dialog_info, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                Logger.i(TAG + "????????????????????????????????????" + timingLists.get(position).toString());
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

                List<RoundResult> roundResults = new ArrayList<>();
                uploadResults.clear();
                Group dbGroupList = null;
                if (timingLists.get(position).getItemCode().equals(itemList.get(mItemPosition).getItemCode())) {
                    //?????????????????????
                    for (GroupItemBean groupItemBean : groupItemBeans
                    ) {
                        if (timingLists.get(position).getItemGroupName().equals(groupItemBean.getGroupItemName())) {
                            //??????????????????????????????????????????
                            groupItemBean.getGroup().setIsTestComplete(GROUP_FINISH);
                            groupItemBean.getGroup().setRemark1(startTime + "");//??????????????????
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
                    dbGroupList.setRemark1(startTime + "");//??????????????????
                    DBManager.getInstance().updateGroup(dbGroupList);
                }
                if (dbGroupList==null){
                    dbGroupList = DBManager.getInstance().getGroupByNo(timingLists.get(position).getItemCode(), timingLists.get(position).getNo(), timingLists.get(position).getColor());

                }
                //????????????
                RoundResult roundResult;
                UploadResults uploadResult;
                String itemName = null;
                for (RaceResultBean resultBean2 : resultDataList
                ) {
                    if (resultBean2.getColor() == timingLists.get(position).getColor()) {
                        //?????????????????????
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
                        roundResult = new RoundResult();//?????????????????????????????????

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
                        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
                        byte[] cycleResult = DataUtil.byteArray2RgbArray(resultInts);
                        roundResult.setCycleResult(cycleResult);

                        roundResult = DBManager.getInstance().insertRoundResult2(roundResult);

                        roundResults.add(roundResult);

                        uploadResult = new UploadResults(dbGroupList.getScheduleNo(), resultBean2.getItemCode()
                                , resultBean2.getStudentCode(), "1", dbGroupList, RoundResultBean.beanCope2(roundResult, dbGroupList));//???????????????????????????

                        uploadResults.add(uploadResult);
                    }
                }

                //????????????????????????????????????????????????
                Iterator<RaceResultBean> it = resultDataList.iterator();

                List<RaceResultBean> completeBeans = new ArrayList<>();//?????????????????????
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

                //?????????????????????
                timingLists.get(position).setState(TIMING_STATE_NOMAL);
                timingLists.get(position).setItemGroupName("");
                timingLists.get(position).setColor(0);
                timingLists.get(position).setNo(0);
                timingLists.get(position).setStudentNo(0);
                timingLists.get(position).setItemCode("");
                timingLists.get(position).setTime(0);

                raceTimingAdapter.notifyItemChanged(position);
                raceTimingAdapter.notifyBackGround(holder, TimingBean.TIMING_STATE_COMPLETE);
                //??????????????????
                if (SettingHelper.getSystemSetting().isRtUpload()) {
//                    Logger.i("??????????????????:" + uploadResults.toString());
//                    ServerMessage.uploadResult(uploadResults);
                    if (itemName != null) {
//                        ServerMessage.uploadZCPResult(mContext, itemName, uploadResults);
                        ServerMessage.baseUploadResult(mContext, uploadResults);
                    }
                }
                Logger.i(TAG + "??????:" + roundResults.toString());
                //????????????

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
     * ????????????????????????
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
            toastSpeak("??????????????????????????????");
            return;
        }
        dialogUtil.showCommonDialog("?????????????????????", android.R.drawable.ic_dialog_info, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                Logger.i(TAG + "??????????????????????????????" + timingLists.get(position).toString());
                //????????????????????????????????????????????????
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

                //?????????????????????????????????
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
            mHander.sendMessage(mHander.obtainMessage(1, "??????????????????"));
            Log.e(TAG, "Write error");
        }
    }

    private void initPopup() {
        int height = DisplayUtil.getScreenHightPx(this);
        mCirclePop = EasyPopup.create()
                .setContentView(this, R.layout.layout_pop_color)
                .setBackgroundDimEnable(true)
                .setDimValue(0.5f)
                //??????????????????PopupWindow?????????????????????
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
                    toastSpeak("?????????????????????");
                    mCirclePop.dismiss();
                    return;
                }
                //??????????????????????????????>=???????????????????????????
                if (colorGroups.get(mColorGroupPosition).getStudentNo() < groupItemBeans.get(groupPosition).getGroupItems().size()) {
                    toastSpeak("???????????????");
                    return;
                }
                groupItemBeans.get(groupPosition).getGroup().setColorGroupName(colorGroups.get(mColorGroupPosition).getColorGroupName());//??????1??????---????????????
                groupItemBeans.get(groupPosition).getGroup().setColorId(String.valueOf(colorGroups.get(mColorGroupPosition).getColor()));//??????2??????---??????
                groupAdapter.notifyDataSetChanged();

                addToTiming();
                mCirclePop.dismiss();
            }
        });

        btnSelectColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorGroups.isEmpty()) {
                    toastSpeak("?????????????????????");
                    mCirclePop.dismiss();
                    return;
                }

                //??????????????????????????????>=???????????????????????????
                if (colorGroups.get(mColorGroupPosition).getStudentNo() < groupItemBeans.get(groupPosition).getGroupItems().size()) {
                    toastSpeak("???????????????");
                    return;
                }

                groupItemBeans.get(groupPosition).getGroup().setIsTestComplete(TimingBean.GROUP_4);//????????????
                groupItemBeans.get(groupPosition).getGroup().setColorGroupName(colorGroups.get(mColorGroupPosition).getColorGroupName());//??????1??????---????????????
                groupItemBeans.get(groupPosition).getGroup().setColorId(String.valueOf(colorGroups.get(mColorGroupPosition).getColor()));//??????2??????---??????
                groupAdapter.notifyDataSetChanged();

                DBManager.getInstance().updateGroup(groupItemBeans.get(groupPosition).getGroup());
                mCirclePop.dismiss();
            }
        });
    }

    private Map<Integer, RoundResult> completeResults = new HashMap<>();

    private void showCompleteDialog() {
        String[] selectItems = new String[]{"????????????", "????????????", "????????????"};
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
                        resultTitles.add("??????");
                        resultTitles.add("??????");
                        resultTitles.add("??????");
                        resultTitles.add("????????????");

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
//                            strings.add(student.getSex()==0?"???":"???");
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
                                return Integer.valueOf(o1.get(0)).compareTo(Integer.valueOf(o2.get(0)));//????????????????????????
                            }
                        });
                        for (int i = 1; i < resultCycles + 1; i++) {
                            resultTitles.add("???" + i + "???");
                        }

                        if (resultCycles < cycleNo) {
                            for (int i = resultCycles; i < cycleNo; i++) {
                                resultTitles.add("???" + (i + 1) + "???");
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
                                    , groupItem.getStudentCode(), "1", group, RoundResultBean.beanCope2(roundResult, group));//???????????????????????????
                            uploadResults.add(uploadResult);
                        }
                        if (itemName != null) {
//                            ServerMessage.uploadZCPResult(mContext, itemName, uploadResults);
                            ServerMessage.baseUploadResult(mContext, uploadResults);
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
     * ???????????????????????????????????????
     */
    private void showListDialog() {
        String[] selectItems = {"????????????", "????????????", "????????????"};

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
                                    toastSpeak("?????????????????????");
                                    return;
                                }
                            }
                            addToTiming();
                            break;
                        } else if (testComplete == GROUP_FINISH) {
                            toastSpeak("???????????????");
                            break;
                        }
                        colorGroupAdapter.notifyDataSetChanged();
                        tvGroupName.setText(groupName);
                        mCirclePop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                        break;
                    case 1:
                        if (testComplete == GROUP_3) {
                            toastSpeak("?????????????????????");
                            break;
                        } else if (testComplete == GROUP_FINISH) {
                            toastSpeak("???????????????");
                            break;
                        }
                        groupItemBeans.get(groupPosition).getGroup().setIsTestComplete(0);
                        groupItemBeans.get(groupPosition).getGroup().setColorGroupName("");//??????1??????---????????????
                        groupItemBeans.get(groupPosition).getGroup().setColorId("");//??????2??????---??????
                        groupAdapter.notifyDataSetChanged();
                        DBManager.getInstance().updateGroup(groupItemBeans.get(groupPosition).getGroup());
                        break;
                    case 2:
                        if (testComplete == GROUP_3) {
                            for (TimingBean timing : timingLists
                            ) {
                                if (groupItemBeans.get(groupPosition).getGroupItemName().equals(timing.getItemGroupName())) {
                                    toastSpeak("???????????????");
                                    return;
                                }
                            }
                            addToTiming();
                        } else if (testComplete == GROUP_FINISH) {
                            toastSpeak("???????????????");
                        } else if (testComplete == GROUP_4) {
                            addToTiming();
                        } else {
                            toastSpeak("?????????????????????");
                        }
                        break;
                    case 3:
//                        if (testComplete == GROUP_4) {
//                            ToastUtils.showShort("??????????????????");
//                            break;
//                        } else if (testComplete == GROUP_FINISH) {
//                            ToastUtils.showShort("???????????????");
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
            showListDialog();//?????????
        } else {
            showCompleteDialog();//?????????
        }
    }

    /**
     * ??????????????????
     */
    private void getGroupName() {
        String sex = "";
        switch (groupItemBeans.get(groupPosition).getGroup().getGroupType()) {
            case 0:
                sex = "??????";
                break;
            case 1:
                sex = "??????";
                break;
            case 2:
                sex = "??????";
                break;
            default:
                break;
        }
        groupName = sex + items[mItemPosition] + "???" + groupItemBeans.get(groupPosition).getGroup().getGroupNo() + "???";
    }

    private int addPosition;//?????????????????????????????????

    /**
     * ?????????????????????
     */
    private void addToTiming() {
        for (TimingBean timing : timingLists
        ) {
            if (timing.getColor() == Integer.parseInt(groupItemBeans.get(groupPosition).getGroup().getColorId())) {
                toastSpeak("????????????????????????");
                return;
            }
        }

        //???????????????????????????????????????????????????????????????????????????no???0??????????????????????????????????????????????????????
        for (int i = 0; i < timingLists.size(); i++) {
            if (timingLists.get(i).getNo() == 0) {
                if (i > 0) {
                    addPosition = i * timingLists.get(i - 1).getStudentNo();
                } else {
                    addPosition = 0;
                }
                timingLists.get(i).setNo(groupItemBeans.get(groupPosition).getGroup().getGroupNo());//??????????????????
                timingLists.get(i).setItemGroupName(groupName);//??????
                timingLists.get(i).setStudentNo(groupItemBeans.get(groupPosition).getGroupItems().size());
                timingLists.get(i).setItemCode(itemList.get(mItemPosition).getItemCode());
                timingLists.get(i).setColor(Integer.parseInt(groupItemBeans.get(groupPosition).getGroup().getColorId()));
                break;
            } else {
                if (i == timingLists.size() - 1) {
                    toastSpeak("??????????????????????????????????????????");
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
     * ?????????????????????????????????????????????????????????
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

        Logger.i("????????????????????????:" + resultDataList.toString());
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
