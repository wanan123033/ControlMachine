package com.feipulai.host.activity.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.grip_dynamometer.pair.GripPairActivity;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.vccheck.adapter.DeviceListAdapter;
import com.feipulai.host.activity.vccheck.pair.VcPairActivity;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.bean.DeviceDetail;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.UploadResultUtil;
import com.feipulai.host.netUtils.netapi.ServerIml;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.view.StuSearchEditText;
import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by pengjf on 2019/10/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * <p>
 * 一对多基类
 */
public abstract class BaseMoreActivity extends BaseCheckActivity {

    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.txt_led_setting)
    TextView txtLedSetting;
    @BindView(R.id.tv_device_pair)
    TextView tvDevicePair;
    @BindView(R.id.rv_device_list)
    RecyclerView rvDeviceList;
    @BindView(R.id.lv_results)
    ListView lvResults;
    private LEDManager mLEDManager;
    private Intent serverIntent;
    public int MAX_DEVICE_COUNT = 4;
    private int deviceCount;
    public List<DeviceDetail> deviceDetails = new ArrayList<>();
    private DeviceListAdapter deviceListAdapter;
    private ClearHandler clearHandler = new ClearHandler();
    private LedHandler ledHandler = new LedHandler();
    private boolean isNextClickStart = true;
    private boolean isPenalize;
    private long startTime;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_base_more;
    }

    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        if (SettingHelper.getSystemSetting().isRtUpload()) {
//            serverIntent = new Intent(this, UploadService.class);
//            startService(serverIntent);
        }
    }

    private void init() {
        mLEDManager = new LEDManager();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        PrinterManager.getInstance().init();
        etInputText.setData(lvResults, this);

        setDeviceCount(setTestDeviceCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLEDManager.clearScreen(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        for (int i = 0; i < setTestDeviceCount(); i++) {
            StringBuilder data = new StringBuilder();
            data.append(i + 1).append("号机");//1号机         空闲
            for (int j = 0; j < 7; j++) {
                data.append(" ");
            }
            data.append("空闲");
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data.toString(), 0, i, false, true);
        }
        if (!isUse()) {
            if (deviceDetails.size() != setTestDeviceCount()) {
                setDeviceCount(setTestDeviceCount());
            }
        }
    }

    private void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
        deviceDetails.clear();
        for (int i = 0; i < deviceCount; i++) {
            DeviceDetail detail = new DeviceDetail();
            detail.getStuDevicePair().getBaseDevice().setDeviceId(i + 1);
            detail.getStuDevicePair().setTimeResult(new String[setTestCount()]);
            detail.setDeviceOpen(true);
            if (deviceCount == 1) {
                detail.getStuDevicePair().setResult(-999);
                detail.setItemType(DeviceDetail.ITEM_ONE);
            }
            deviceDetails.add(detail);
        }
        initView();
        if (afrFrameLayout != null) {
            if (deviceCount == 1) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) afrFrameLayout.getLayoutParams();
                layoutParams.setMargins(258, 0, 0, 0);
                afrFrameLayout.setLayoutParams(layoutParams);
            } else {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) afrFrameLayout.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
                afrFrameLayout.setLayoutParams(layoutParams);
            }

        }
    }

    private int setTestCount() {
        return deviceCount;
    }

    public void updateAdapterTestCount() {

        deviceListAdapter.setTestCount(setTestCount());
        for (DeviceDetail deviceDetail : deviceDetails) {
            deviceDetail.getStuDevicePair().setTimeResult(new String[setTestCount()]);
        }
        deviceListAdapter.notifyDataSetChanged();
    }

    /**
     * 是否存在使用中设备
     *
     * @return
     */
    public boolean isUse() {
        boolean isOnUse = false;
        for (DeviceDetail deviceDetail : deviceDetails) {
            if (deviceDetail.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_ONUSE) {
                return true;
            }
        }
        return isOnUse;


    }

    public abstract int setTestDeviceCount();

    private void initView() {
        deviceListAdapter = new DeviceListAdapter(deviceDetails);
        GridLayoutManager layoutManager = new GridLayoutManager(this, deviceDetails.size());
        rvDeviceList.setLayoutManager(layoutManager);
        deviceListAdapter.setTestCount(setTestCount());
        rvDeviceList.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int pos) {
                BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
                switch (view.getId()) {
                    case R.id.txt_start:
                        if (pair.getStudent() == null) {
                            toastSpeak("当前无学生测试");
                            return;
                        }
                        if (deviceDetails.get(pos).getRound() >= setTestCount()) {
                            toastSpeak("当前学生测试完成");
                            stuSkip(pos);
                            return;
                        }
                        startTime = System.currentTimeMillis();
                        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
                            sendTestCommand(pair, pos);
//                            view.setBackgroundColor(ContextCompat.getColor(BaseMoreActivity.this, R.color.gray_btn_bg_color));
                            deviceDetails.get(pos).getStuDevicePair().getTimeResult()[0] = "开始测试";
                            deviceListAdapter.notifyItemChanged(pos);
                        }
                        break;
                    case R.id.txt_skip:
                        if (pair.getStudent() != null) {
                            stuSkipDialog(pair.getStudent(), pos);
                        }
                        break;
                    default:
                        break;
                }


            }
        });
    }

    private void stuSkipDialog(final Student student, final int pos) {

        new AlertDialog.Builder(this).setMessage("是否跳过" + student.getStudentName() + "考生测试")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.i("stuSkip:" + student.toString());
                        //测试结束学生清除 ，设备设置空闲状态
                        stuSkip(pos);
                        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
                    }
                }).setNegativeButton("取消", null).show();
    }

    public void setTxtEnable(int deviceId, boolean enable) {
        deviceListAdapter.setTxtStartEnable(deviceId, enable);
    }

    public void setNextClickStart(boolean nextClickStart) {
        isNextClickStart = nextClickStart;
        deviceListAdapter.setNextClickStart(nextClickStart);
    }

    public void setFaultEnable(boolean isPenalize) {
        this.isPenalize = isPenalize;
    }

    @Override
    public void onCheckIn(Student student) {
        int index = 0;
        boolean canUseDevice = false;
        for (int i = 0; i < deviceCount; i++) {
            DeviceDetail deviceDetail = deviceDetails.get(i);
            if (deviceDetail.isDeviceOpen() && deviceDetail.getStuDevicePair().isCanTest()
                    && deviceDetail.getStuDevicePair().getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                index = i;
                canUseDevice = true;
                break;
            }
        }

        if (!canUseDevice) {
            toastSpeak("当前无设备可添加学生测试");
            return;
        }


        addStudent(student, index);
        if (!isNextClickStart) {
            deviceDetails.get(index).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
            deviceDetails.get(index).getStuDevicePair().setResult(-999);
            deviceListAdapter.notifyItemChanged(index);
            sendTestCommand(deviceDetails.get(index).getStuDevicePair(), index);
        }
    }

    private void stuSkip(int pos) {
        deviceDetails.get(pos).getStuDevicePair().setStudent(null);
        deviceDetails.get(pos).getStuDevicePair().setCanTest(true);
        deviceDetails.get(pos).getStuDevicePair().setTimeResult(new String[setTestCount()]);
        deviceDetails.get(pos).getStuDevicePair().setCanTest(true);
        deviceListAdapter.notifyItemChanged(pos);
    }


    private void addStudent(Student student, int index) {
        DeviceDetail deviceDetail = deviceDetails.get(index);
        deviceDetail.getStuDevicePair().setStudent(student);
        deviceDetail.getStuDevicePair().setCanTest(false);
        deviceDetail.getStuDevicePair().setBaseHeight(0);
        int count = deviceDetail.getRound();
        toastSpeak(String.format(getString(R.string.test_speak_hint), student.getStudentName(), count + 1)
                , String.format(getString(R.string.test_speak_hint), student.getStudentName(), count + 1));

        setShowLed(deviceDetail.getStuDevicePair(), index);
        deviceListAdapter.notifyItemChanged(index);
    }

    private void setShowLed(BaseStuPair pair, int index) {
        Student student = pair.getStudent();
        if (student == null)
            return;
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        if (ledMode == 0) {
            String str = student.getStudentName();
            int length = InteractUtils.stringLength(str);
            int temp = 12 - length;
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            if (temp > 0) {
                for (int i = 0; i < temp; i++) {
                    sb.append(" ");
                }
                str = sb.append("开始").toString();

            } else {
                str = InteractUtils.getStrWithLength(str, 4);
                sb.append(str).append(" ").append(" ").append(" ").append(" ").append("开始");
            }

            byte[] data = new byte[0];
            try {
                data = str.getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, index,
                    false, true);
        } else {
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index,
                    pair.getStudent().getStudentName(), mLEDManager.getX(pair.getStudent().getLEDStuName()), 0,
                    true, false);
        }
    }


    @OnClick({R.id.txt_led_setting, R.id.tv_device_pair,R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_led_setting:
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;
            case R.id.tv_device_pair:
                if (machineCode == ItemDefault.CODE_WLJ) {
                    startActivity(new Intent(this, GripPairActivity.class));
                } else {
                    startActivity(new Intent(this, VcPairActivity.class));
                }

                break;
            case R.id.img_AFR:
//                gotoUVCFaceCamera();
                showAFR();
                break;
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title).addRightText("外接屏幕", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(BaseMoreActivity.this, LEDSettingActivity.class));
            }
        }).addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        });
    }

    /**
     * 更新学生成绩
     *
     * @param baseStu
     */
    public synchronized void updateResult(@NonNull BaseStuPair baseStu) {
        int deviceId = baseStu.getBaseDevice().getDeviceId();
        BaseStuPair pair = null;
        int index = 0;
        for (int i = 0; i < deviceCount; i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            if (id == deviceId) {
                pair = deviceDetails.get(i).getStuDevicePair();
                index = i;
                break;
            }

        }
        if (null != pair.getBaseDevice()) {
            pair.setResultState(baseStu.getResultState());
            pair.setResult(baseStu.getResult());
            refreshDevice(index);
            updateResultLed(pair, index);
            deviceListAdapter.notifyItemChanged(index);
        }
    }

    private void updateResultLed(BaseStuPair baseStu, int index) {
        if (baseStu.getStudent() == null)
            return;
        String result = ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        if (ledMode == 0) {
            int x = ResultDisplayUtils.getStringLength(result);
            int len = ResultDisplayUtils.getStringLength(baseStu.getStudent().getStudentName());
            StringBuilder sb = new StringBuilder();
            sb.append(baseStu.getStudent().getStudentName());
            for (int i = 0; i < (16 - len - x); i++) {
                sb.append(" ");
            }
            sb.append(result);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), sb.toString(), 0, index, false, true);
        } else {
            byte[] data = new byte[16];
            String str = "当前：";
            try {
                byte[] strData = str.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                byte[] resultData = result.getBytes("GB2312");
                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index, data, 0, 1, false, true);
        }

    }

    public synchronized void updateDevice(@NonNull BaseDeviceState deviceState) {
        Logger.i("updateDevice==>" + deviceState.toString());
        int deviceId = deviceState.getDeviceId();
        BaseStuPair pair = null;
        int index = 0;
        for (int i = 0; i < deviceCount; i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            if (id == deviceId) {
                pair = deviceDetails.get(i).getStuDevicePair();
                index = i;
                break;
            }

        }

        if (pair != null) {
            pair.getBaseDevice().setState(deviceState.getState());
            //状态为测试已结束
            if (deviceState.getState() == BaseDeviceState.STATE_END) {
                if (pair.getStudent() != null) {
                    Logger.i("考生" + pair.getStudent().toString());
                }
                Logger.i("设备成绩信息STATE_END==>" + deviceState.toString());
                //状态为测试已结束
                if (isPenalize && pair.getResultState() != RoundResult.RESULT_STATE_FOUL) {

                    if (setTestDeviceCount() == 1) {
                        showPenalize(index);
                    } else {
                        deviceDetails.get(index).setConfirmVisible(true);
                        deviceListAdapter.notifyItemChanged(index);
                    }
                } else {
                    pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                    pair.setCanTest(true);
                    doResult(pair, index);
                }


            }
        }
        refreshDevice(index);
    }

    /**
     * 展示判罚
     */
    private void showPenalize(final int index) {
        final BaseStuPair pair = deviceDetails.get(index).getStuDevicePair();
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText(getString(R.string.confirm_result));
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                updateResult(pair);
                doResult(pair, index);
            }
        }).setCancelText(getString(R.string.foul)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                pair.setResultState(RoundResult.RESULT_STATE_FOUL);
                updateResult(pair);
                doResult(pair, index);
            }
        }).show();
        deviceDetails.get(index).setConfirmVisible(false);
        deviceListAdapter.notifyItemChanged(index);
    }

    public void refreshDevice(int index) {
        if (deviceDetails.get(index).getStuDevicePair().getBaseDevice() != null) {
            deviceListAdapter.notifyItemChanged(index);
        }
    }


    /**
     * 处理结果
     */
    private synchronized void doResult(BaseStuPair pair, int index) {
//        deviceListAdapter.getViewByPosition(index,R.id.txt_start).setBackground(ContextCompat.getDrawable(this,R.drawable.btn_click_bg_selected));
        DeviceDetail detail = deviceDetails.get(index);
        String[] timeResult = detail.getStuDevicePair().getTimeResult();
        if (detail.getRound() >= timeResult.length)//防止
            return;
        //设置设备成绩
        timeResult[detail.getRound()] = ((pair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" :
                ResultDisplayUtils.getStrResultForDisplay(pair.getResult()));
        detail.getStuDevicePair().setTimeResult(timeResult);

        //保存成绩
        saveResult(pair);
        printResult(pair);
//        broadResult(pair);
        detail.setRound(detail.getRound() + 1);
        pair.setCanTest(true);
        if (detail.getRound() < setTestCount()) {
            if (pair.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                //测试结束学生清除 ，设备设置空闲状态
                detail.setRound(0);
                //4秒后清理学生信息
                Message msg = new Message();
                msg.what = pair.getBaseDevice().getDeviceId();
                msg.obj = detail;
                clearHandler.sendMessageDelayed(msg, 4000);
                return;
            }

            if (detail.getRound() < setTestCount()) {
                toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), detail.getRound() + 1)
                        , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), detail.getRound() + 1));
            }
            Message msg = new Message();
            msg.obj = pair;
            ledHandler.sendMessageDelayed(msg, 2000);
            pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
        } else {
            detail.setRound(0);
            //4秒后清理学生信息
            Message msg = new Message();
            msg.obj = detail;
            clearHandler.sendMessageDelayed(msg, 4000);
        }


    }

    private void printResult(BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        Student student = baseStuPair.getStudent();
        if (student == null)
            return;
//        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(
                String.format(getString(R.string.host_name), TestConfigs.sCurrentItem.getItemName(), SettingHelper.getSystemSetting().getHostId()));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_code), student.getStudentCode()));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_name), student.getStudentName()));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_result), (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) ?
                        getString(R.string.foul) : ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_time), TestConfigs.df.format(Calendar.getInstance().getTime())));
        PrinterManager.getInstance().print(" \n");
    }

    private void saveResult(BaseStuPair baseStuPair) {
        if (baseStuPair.getStudent() == null)
            return;
        Logger.i("saveResult==>" + baseStuPair.toString());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
        roundResult.setItemCode(itemCode);
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        //DateUtil.getCurrentTime2("yyyy-MM-dd HH:mm:ss")
        roundResult.setTestTime(startTime + "");
        roundResult.setPrintTime(System.currentTimeMillis() + "");
        roundResult.setRoundNo(1);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode());
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // 这个时候就要同时修改这两个成绩了
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
            } else {
                roundResult.setIsLastResult(0);
            }
        } else {
            // 第一次测试
            roundResult.setIsLastResult(1);
        }

        DBManager.getInstance().insertRoundResult(roundResult);


        //成绩上传判断成绩类型获取最后成绩
        if (TestConfigs.sCurrentItem.getfResultType() == 0) {
            //最好
            if (bestResult != null && bestResult.getIsLastResult() == 1)
                uploadResult(roundResult, bestResult);
            else
                uploadResult(roundResult, roundResult);
        } else {
            //最后
            uploadResult(roundResult, roundResult);
        }
    }

    /**
     * 成绩上传
     *
     * @param roundResult 当前成绩
     * @param lastResult  最后成绩
     */
    private void uploadResult(RoundResult roundResult, RoundResult lastResult) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort(R.string.upload_result_hint);
            return;
        }

        ServerIml.uploadResult(UploadResultUtil.getUploadData(roundResult, lastResult));
    }

    public class LedHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseStuPair pair = (BaseStuPair) msg.obj;
            setShowLed(pair, pair.getBaseDevice().getDeviceId() - 1);
        }

    }

    private class ClearHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DeviceDetail detail = (DeviceDetail) msg.obj;
            detail.getStuDevicePair().setTimeResult(new String[setTestCount()]);
            detail.getStuDevicePair().setStudent(null);
            deviceListAdapter.notifyDataSetChanged();
        }
    }

    public abstract void gotoItemSetting();

    public abstract void sendTestCommand(BaseStuPair pair, int index);

    @Override
    public void finish() {
        for (DeviceDetail deviceDetail : deviceDetails) {
            if (deviceDetail.getStuDevicePair().getStudent() != null) {
                toastSpeak("测试中,不允许退出当前界面");
                return;
            }
        }
        super.finish();
    }
}
