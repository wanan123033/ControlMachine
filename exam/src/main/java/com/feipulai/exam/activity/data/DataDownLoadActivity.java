package com.feipulai.exam.activity.data;

import static com.feipulai.exam.tcp.TCPConst.SCHEDULE;
import static com.feipulai.exam.tcp.TCPConst.TRACK;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.common.view.baseToolbar.StatusBarUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.tcp.SendTcpClientThread;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LoginActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.ItemAdapter;
import com.feipulai.exam.adapter.ScheduleAdapter;
import com.feipulai.exam.bean.ScheduleBean;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.HttpManager;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.tcp.CommonListener;
import com.feipulai.exam.tcp.TcpDownLoadUtil;
import com.feipulai.exam.view.OperateProgressBar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class DataDownLoadActivity extends BaseTitleActivity implements RadioGroup.OnCheckedChangeListener, TextWatcher, AdapterView.OnItemSelectedListener {
    @BindView(R.id.rg_examType)
    RadioGroup rg_examType;
    @BindView(R.id.et_sever_ip)
    EditText et_sever_ip;
    @BindView(R.id.sp_schedule)
    Spinner sp_schedule;
    @BindView(R.id.sp_item)
    Spinner sp_item;
    @BindView(R.id.tv_http)
    TextView tv_http;
    @BindView(R.id.tv_tcp)
    TextView tv_tcp;
    @BindView(R.id.tv_down_up)
    TextView tv_down_up;
    @BindView(R.id.tv_sum)
    TextView tv_sum;
    @BindView(R.id.tv_face)
    TextView tv_face;
    @BindView(R.id.tv_disconnected)
    TextView tv_disconnected;
    @BindView(R.id.txt_test)
    TextView txt_test;
    @BindView(R.id.txt_login)
    TextView txt_login;

    private SystemSetting setting;
    private int examType = StudentItem.EXAM_NORMAL;
    private HttpSubscriber subscriber = null;
    private List<Item> itemList;
    private ScheduleAdapter scheduleAdapter;
    private List<Schedule> scheduleList;
    private ItemAdapter itemAdapter;
    private int downType;

    private Schedule currentSchedule;
    private Item currentItem;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_download;
    }

    @Override
    protected void initData() {
        subscriber = new HttpSubscriber();
        setting = SettingHelper.getSystemSetting();
        rg_examType.setOnCheckedChangeListener(this);
        et_sever_ip.addTextChangedListener(this);
        scheduleList = new ArrayList<>();
        scheduleAdapter = new ScheduleAdapter(this, scheduleList);
        sp_schedule.setAdapter(scheduleAdapter);
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this,itemList);
        sp_item.setAdapter(itemAdapter);

        sp_schedule.setOnItemSelectedListener(this);
        sp_item.setOnItemSelectedListener(this);
        if (tv_down_up.getVisibility() == View.VISIBLE){
            et_sever_ip.setText(setting.getServerIp());
            tv_disconnected.setText("HTTP服务器");
        }else {
            et_sever_ip.setText(setting.getTcpIp());
            tv_disconnected.setText("TCP服务器");
        }
        initAfrCount();

        initScheduleItem();

    }

    @OnClick({R.id.btn_default,R.id.txt_login,R.id.tv_down_whole,R.id.tv_down_up,R.id.tv_down_one,R.id.tv_down,
            R.id.tv_http,R.id.tv_tcp,R.id.tv_back,R.id.txt_test})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_http:
                downType = 0;
                tv_http.setTextColor(getResources().getColor(R.color.white));
                tv_tcp.setTextColor(getResources().getColor(R.color.white_grey));
                tv_down_up.setVisibility(View.VISIBLE);
                et_sever_ip.setText(setting.getServerIp());
                tv_disconnected.setText("HTTP服务器");
                txt_login.setVisibility(View.VISIBLE);
                txt_test.setVisibility(View.GONE);
                break;
            case R.id.tv_tcp:
                downType = 1;
                tv_http.setTextColor(getResources().getColor(R.color.white_grey));
                tv_tcp.setTextColor(getResources().getColor(R.color.white));
                tv_down_up.setVisibility(View.GONE);
                et_sever_ip.setText(setting.getTcpIp());
                tv_disconnected.setText("TCP服务器");
                txt_login.setVisibility(View.GONE);
                txt_test.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_default:
                et_sever_ip.setText(TestConfigs.DEFAULT_IP_ADDRESS);
                setting.setServerIp(TestConfigs.DEFAULT_IP_ADDRESS);
                break;
            case R.id.txt_login:
                gotoLogin();
                break;
            case R.id.tv_down_whole:
                if (downType == 0) {
                    OperateProgressBar.showLoadingUi(DataDownLoadActivity.this, "正在下载数据...");
                    ServerMessage.downloadData(DataDownLoadActivity.this, examType, "");
                }else {
                    dataDownload(setting.getTcpIp(),0);
                }
                break;
            case R.id.tv_down_up:
                if (downType == 0) {
                    OperateProgressBar.showLoadingUi(DataDownLoadActivity.this, "正在下载数据...");
                    String lastTime = SharedPrefsUtil.getValue(DataDownLoadActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.LAST_DOWNLOAD_TIME, "");
                    ServerMessage.downloadData(DataDownLoadActivity.this, examType, lastTime);
                }
                break;
            case R.id.tv_down_one:
                if (downType == 0){
                    downScheduleAll();
                }else {
                    dataDownload(setting.getTcpIp(),1);
                }
                break;
            case R.id.tv_down:
                if (downType == 0) {
                    downStudentGroup();
                }else {
                    dataDownload(setting.getTcpIp(),2);
                }
                break;
            case R.id.tv_back:
                finish();
                break;
            case R.id.txt_test:
                testTcpConnect();
                break;
        }
    }

    private SendTcpClientThread tcpClientThread;

    private void testTcpConnect() {
        if (tcpClientThread == null) {
            String ipStr = null;
            String portStr = null;
            try {
                String tcpIp = SettingHelper.getSystemSetting().getTcpIp();
                ipStr = tcpIp.split(":")[0];
                portStr = tcpIp.split(":")[1];
            } catch (Exception e) {
                ToastUtils.showShort("请输入正确的TCP地址");
                return;
            }

            tcpClientThread = new SendTcpClientThread(ipStr, Integer.parseInt(portStr), new SendTcpClientThread.SendTcpListener() {
                @Override
                public void onMsgReceive(String text) {

                }

                @Override
                public void onSendFail(final String msg) {

                }

                @Override
                public void onConnectFlag(boolean isConnect) {
                    Log.e("onConnectFlag", "---------" + isConnect);
                    if (isConnect) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("服务器连接成功");
                            }
                        });

                    } else {
                        tcpClientThread.exit = true;
                        tcpClientThread = null;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("服务器连接失败");
                            }
                        });

                    }
                }
            });
            tcpClientThread.start();
        } else {
            if (tcpClientThread.isInterrupted()) {
                tcpClientThread.start();
            }
        }
    }

    private void gotoLogin() {
        String url = et_sever_ip.getText().toString().trim() + "/app/";
        if (!url.startsWith("http")) {//修改IP
            url = "http://" + url;
        }
        if (!NetWorkUtils.isValidUrl(url)) {
            toastSpeak("非法的服务器地址");
            return;
        }
        SettingHelper.updateSettingCache(setting);
        HttpManager.resetManager();
        startActivity(new Intent(this, LoginActivity.class));
    }
    private int position;
    private void downStudentGroup() {
        OperateProgressBar.showLoadingUi(this,"数据下载中...");
        String lastTime = SharedPrefsUtil.getValue(DataDownLoadActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.LAST_DOWNLOAD_TIME, "");
        String scheduleNo = null;
        if (currentSchedule.getScheduleNo().equals("-2")){  // 全部日程
            scheduleNo = "";
        }else {
            scheduleNo = currentSchedule.getScheduleNo();
        }
        final List<Item> items = new ArrayList<>();
        if (currentItem.getItemCode().equals("-99")){
            items.addAll(DBManager.getInstance().dumpAllItems());
        }else {
            items.add(currentItem);
        }
        final String finalScheduleNo = scheduleNo;
        subscriber.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
            @Override
            public void onSuccess(int bizType) {
                OperateProgressBar.removeLoadingUiIfExist(DataDownLoadActivity.this);
                ToastUtils.showShort("数据下载成功！");
                initAfrCount();
            }

            @Override
            public void onFault(int bizType) {
                OperateProgressBar.removeLoadingUiIfExist(DataDownLoadActivity.this);
                ToastUtils.showShort("数据下载失败！");
            }

            @Override
            public void onRequestData(Object data) {

            }
        });

        for (Item item : items){
            subscriber.getItemStudent(lastTime,item.getItemCode(),1,examType,scheduleNo);
            subscriber.getItemGroupAll(item.getItemCode(), "", 1, examType);
        }

    }

    private void downScheduleAll() {
        OperateProgressBar.showLoadingUi(this,"数据下载中...");
        subscriber.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case HttpSubscriber.SCHEDULE_BIZ://日程
                        subscriber.getItemAll(DataDownLoadActivity.this);
                        break;
                    case HttpSubscriber.ITEM_BIZ://项目
                        OperateProgressBar.removeLoadingUiIfExist(DataDownLoadActivity.this);
                        itemList.clear();
                        if (TestConfigs.sCurrentItem != null) {
                            if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                                itemList = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);
                            }
                            initScheduleItem();
                            ToastUtils.showShort("日程，项目下载完成");
                        }else {
                            ToastUtils.showShort("项目选择错误，请重新选择项目！");
                        }
                        break;
                }
            }

            @Override
            public void onFault(int bizType) {
                OperateProgressBar.removeLoadingUiIfExist(DataDownLoadActivity.this);
            }

            @Override
            public void onRequestData(Object data) {

            }
        });
        subscriber.getScheduleAll();

    }

    /**
     * TCP 下载数据   0 下载全部 1只下载日程跟项目   2 下载学生
     * @param tcpip
     * @param downType
     */
    public void dataDownload(String tcpip, final int downType) {
        if (TextUtils.isEmpty(tcpip)) {
            OperateProgressBar.removeLoadingUiIfExist(this);
            Toast.makeText(getApplicationContext(), "请输入正确的TCP地址", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tcpip.contains(":")) {
            OperateProgressBar.showLoadingUi(this);
            String ip = tcpip.substring(0, tcpip.indexOf(":"));
            String port = tcpip.substring(tcpip.indexOf(":") + 1);
            TcpDownLoadUtil tcpDownLoad = new TcpDownLoadUtil(DataDownLoadActivity.this, ip, port, new CommonListener() {
                @Override
                public void onCommonListener(int no, final String string) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toastSpeak(string);
                            OperateProgressBar.removeLoadingUiIfExist(DataDownLoadActivity.this);
                            initAfrCount();
                            if (downType == 0 || downType == 1){
                                initScheduleItem();
                            }
                        }
                    });
                }
            });
            if (downType == 0 || downType == 1) {
                tcpDownLoad.getTcp(SCHEDULE, "", 0,downType);
            }else {
                String scheduleNo=sp_schedule.getSelectedItemPosition()==0?"0":scheduleList.get(sp_schedule.getSelectedItemPosition()).getScheduleNo();

                if (!currentItem.getItemCode().equals("-99")) {
                        tcpDownLoad.getTcp(TRACK, currentItem.getItemName(), Integer.valueOf(scheduleNo),0);
                }else {
                    final List<Item> items = new ArrayList<>();
                    if (currentItem.getItemCode().equals("-99")){
                        items.addAll(DBManager.getInstance().dumpAllItems());
                    }else {
                        items.add(currentItem);
                    }
                    for (Item item : items){
                        tcpDownLoad.getTcp(TRACK, item.getItemName(), Integer.valueOf( scheduleNo),0);
                    }
                }
            }
        } else {
            OperateProgressBar.removeLoadingUiIfExist(this);
            Toast.makeText(getApplicationContext(), "请输入正确的TCP地址", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void initScheduleItem() {
        List<Schedule> schedules = DBManager.getInstance().getSchedules();
        scheduleList.clear();
        scheduleList.add(new Schedule("-2", "全部日程", ""));
        scheduleList.addAll(schedules);
        scheduleAdapter.notifyDataSetChanged();

        itemList.clear();
        itemList.add(TestConfigs.sCurrentItem);
        itemAdapter.notifyDataSetChanged();
        currentItem = itemList.get(0);
        currentSchedule = scheduleList.get(0);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_nomal){
            examType = StudentItem.EXAM_NORMAL;
        }else if (checkedId == R.id.rb_deferred){
            examType = StudentItem.EXAM_DELAYED;
        }else if (checkedId == R.id.rb_resit){
            examType = StudentItem.EXAM_MAKE;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (downType == 0) {
            setting.setServerIp(et_sever_ip.getText().toString().trim());
        }else {
            setting.setTcpIp(et_sever_ip.getText().toString().trim());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.sp_schedule:
                currentSchedule = scheduleList.get(position);
                break;
            case R.id.sp_item:
                currentItem = itemList.get(position);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.DATA_DOWNLOAD_SUCCEED) {
            OperateProgressBar.removeLoadingUiIfExist(DataDownLoadActivity.this);
            ToastUtils.showShort("数据下载完成");
            initAfrCount();
        } else if (baseEvent.getTagInt() == EventConfigs.DATA_DOWNLOAD_FAULT) {
            OperateProgressBar.removeLoadingUiIfExist(DataDownLoadActivity.this);
        } else if (baseEvent.getTagInt() == EventConfigs.TOKEN_ERROR) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
    private void initAfrCount() {

        DataBaseExecutor.addTask(new DataBaseTask() {
            @Override
            public DataBaseRespon executeOper() {
                int stuCount = DBManager.getInstance().getItemStudent("-2", TestConfigs.getCurrentItemCode(), -1, 0).size();
                int afrCount = DBManager.getInstance().queryByItemStudentFeatures().size();

                return new DataBaseRespon(true, stuCount + "", afrCount);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                tv_sum.setText("考生数量:"+Integer.valueOf(respon.getInfo()));
                tv_face.setText("考生特征:"+respon.getObject());
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });


    }
}
