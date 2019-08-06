package com.feipulai.host.activity.situp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.jump_rope.facade.GetStateLedFacade;
import com.feipulai.common.jump_rope.task.OnGetStateWithLedListener;
import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.common.view.WaitDialog;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.serial.beans.SitPushUpSetFrequencyResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.LEDSettingActivity;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.base.BaseCheckPairAdapter;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.jump_rope.bean.JumpDeviceState;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.feipulai.host.view.StuSearchEditText;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SitUpCheckActivity
        extends BaseCheckActivity
        implements BaseQuickAdapter.OnItemClickListener,
        OnGetStateWithLedListener,
        RadioManager.OnRadioArrivedListener {

    private static final int UPDATE_STATES = 0x1;
    private static final int UPDATE_SPECIFIC_ITEM = 0x2;
    private static final int NO_PAIR_RESPONSE_ARRIVED = 0x3;
    private static final int DISMISS_DIALOG = 0x4;
    @BindView(R.id.iv_portrait)
    ImageView mIvPortrait;
    @BindView(R.id.tv_studentCode)
    TextView mTvStudentCode;
    @BindView(R.id.tv_studentName)
    TextView mTvStudentName;
    @BindView(R.id.tv_gender)
    TextView mTvGender;
    @BindView(R.id.tv_grade)
    TextView mTvGrade;
    @BindView(R.id.tv_project_setting)
    TextView mTvProjectSetting;
    @BindView(R.id.et_select)
    StuSearchEditText mEtSelect;
    @BindView(R.id.linear_view_select)
    LinearLayout mLinearViewSelect;
    @BindView(R.id.lv_results)
    ListView mLvResults;
    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;
    @BindView(R.id.btn_device_pair)
    Button mBtnDevicePair;
    @BindView(R.id.btn_change_bad)
    Button mBtnChangeBad;
    @BindView(R.id.btn_start_test)
    Button mBtnStartTest;
    @BindView(R.id.btn_stop_use)
    Button mBtnStopUse;
    @BindView(R.id.btn_led_setting)
    Button mBtnLedSetting;
    @BindView(R.id.btn_delete_student)
    Button mBtnDeleteStudent;
    @BindView(R.id.btn_del_all)
    Button mBtnDelAll;
    @BindView(R.id.view_bottom)
    LinearLayout mViewBottom;

    private volatile int mCurrentPosition;
    private volatile int[] mCurrentConnect;
    private int mMaxDeviceNo;
    private LEDManager mLEDManager;
    private final int projectCode = SitPushUpManager.PROJECT_CODE_SIT_UP;
    private SitPushUpManager mSitPushUpManager;
    private List<BaseStuPair> mPairs;
    private BaseCheckPairAdapter mAdapter;
    private GetStateLedFacade mCheckingTasksFacade;
    private Handler mHandler = new MyHandler(this);
    private volatile boolean mLinking;
    private int mTargetFrequency;
    private int mCurrentFrequency;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sit_up_check);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        mMaxDeviceNo = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.SIT_UP_TEST_NUMBER, 20);
        mTargetFrequency = SerialConfigs.sProChannels.get(ItemDefault.CODE_YWQZ) + SettingHelper.getSystemSetting().getHostId() - 1;

        mLEDManager = new LEDManager();
        mSitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);

        mEtSelect.setData(mLvResults, this);

        mCurrentConnect = new int[mMaxDeviceNo + 1];

        mRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        mRvPairs.addItemDecoration(dividerItemDecoration);

        initAdapter();

        mCheckingTasksFacade = new GetStateLedFacade(this);
    }

    private void initAdapter() {

        mPairs = new ArrayList<>(mMaxDeviceNo);

        for (int i = 0; i < mMaxDeviceNo; i++) {
            BaseStuPair pair = new BaseStuPair();

            BaseDeviceState state = new BaseDeviceState();
            state.setState(BaseDeviceState.STATE_DISCONNECT);
            state.setDeviceId(i + 1);

            pair.setBaseDevice(state);

            mPairs.add(pair);
        }
        mAdapter = new BaseCheckPairAdapter(mPairs);
        mRvPairs.setAdapter(mAdapter);

        mRvPairs.setClickable(true);
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        int maxNo = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.SIT_UP_TEST_NUMBER, 20);

        if (maxNo != mMaxDeviceNo) {
            mCheckingTasksFacade.pause();
            if (maxNo < mMaxDeviceNo) {
                //手柄数变小了,直接把后面的删除掉
                for (int i = mMaxDeviceNo - 1; i >= maxNo; i--) {
                    mPairs.remove(i);
                }
            } else {
                //手柄数变多了,添加一些就可以了
                for (int i = mMaxDeviceNo; i < maxNo; i++) {
                    BaseStuPair pair = new BaseStuPair();
                    JumpDeviceState state = new JumpDeviceState();
                    // state.setFactoryId(JumpDeviceState.INVALID_FACTORY_ID);
                    state.setState(BaseDeviceState.STATE_DISCONNECT);
                    state.setDeviceId(i + 1);
                    // pair.setBaseDevice(state);
                    mPairs.add(pair);
                }
            }
            mMaxDeviceNo = maxNo;
            mAdapter.notifyDataSetChanged();
            mCheckingTasksFacade.resume();
        }

        mCheckingTasksFacade.pause();

        ProgressDialog dialog = ProgressDialog.show(this, "加载模块中...", "加载模块中...");

        RadioManager.getInstance().setOnRadioArrived(this);
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(mTargetFrequency)));
        mCurrentFrequency = mTargetFrequency;

        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.sCurrentItem.getItemName());

        for (int i = 0; i < 3; i++) {
            mSitPushUpManager.endTest();
        }
        mCheckingTasksFacade.resume();
        dialog.dismiss();
    }

    @Override
    public void onCheckIn(Student stu) {

        mTvStudentCode.setText(stu.getStudentCode());
        mTvStudentName.setText(stu.getStudentName());
        mTvGender.setText(stu.getSex() == 0 ? "男" : "女");
        RoundResult lastResult = DBManager.getInstance().queryLastScoreByStuCode(stu.getStudentCode());
        String displayResult = "";
        if (lastResult == null) {
            displayResult = "";
        } else if (lastResult.getResultState() != RoundResult.RESULT_STATE_FOUL) {
            displayResult = ResultDisplayUtils.getStrResultForDisplay(lastResult.getResult());
        } else {
            displayResult = "X";
        }
        mTvGrade.setText(displayResult);

        mCheckingTasksFacade.pause();

        //如果这个人已经检录过了,将原来绑定的手柄解绑,绑定到这个新的手柄上
        for (int i = 0; i < mPairs.size(); i++) {
            BaseStuPair pair = mPairs.get(i);
            Student student = pair.getStudent();
            if (student != null && student.getStudentCode().equals(stu.getStudentCode())) {
                pair.setStudent(null);
                mAdapter.notifyItemChanged(i);
                break;
            }
        }

        //绑定设备
        BaseStuPair pair = mPairs.get(mCurrentPosition);
        pair.setStudent(stu);
        BaseDeviceState deviceState = pair.getBaseDevice();

        int oldPosition = mCurrentPosition;

        while (mCurrentPosition < mMaxDeviceNo - 1) {
            mCurrentPosition++;
            pair = mPairs.get(mCurrentPosition);
            int state = pair.getBaseDevice().getState();
            if (pair.getStudent() == null
                    && (state == BaseDeviceState.STATE_FREE || state == BaseDeviceState.STATE_LOW_BATTERY)) {
                break;
            }
        }
        mAdapter.setSelectItem(mCurrentPosition);

        mAdapter.notifyItemChanged(oldPosition);
        if (mCurrentPosition != oldPosition) {
            mAdapter.notifyItemChanged(mCurrentPosition);
        }

        //Logger.i("update mHandStudents");
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), stu.getStudentName(), 5, 0, true, false);

        if (lastResult == null) {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), deviceState.getDeviceId() + "号设备", 4, 1, false, true);
        } else {
            mLEDManager.showString(ItemDefault.CODE_YWQZ, SettingHelper.getSystemSetting().getHostId(), deviceState.getDeviceId() + "号设备", 4, 1, false, false);
            mLEDManager.showString(ItemDefault.CODE_YWQZ, SettingHelper.getSystemSetting().getHostId(), "已有成绩:" + mTvGrade.getText(), 2, 3, false, true);
        }

        mCheckingTasksFacade.letDisplayWait3Sec();
        mCheckingTasksFacade.resume();
    }

    @OnClick({R.id.tv_project_setting, R.id.btn_device_pair, R.id.btn_change_bad, R.id.btn_start_test, R.id.btn_stop_use, R.id.btn_led_setting, R.id
            .btn_delete_student, R.id.btn_del_all})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.tv_project_setting:
                startActivity(new Intent(this, SitUpSettingActivity.class));
                break;

            case R.id.btn_device_pair:
                startActivity(new Intent(this, SitUpPairActivity.class));
                break;

            case R.id.btn_change_bad:
                mCheckingTasksFacade.pause();
                mLinking = true;
                mCurrentFrequency = 0;
                RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
                show();
                break;

            case R.id.btn_start_test:
                boolean isTestAllowed = false;
                mCheckingTasksFacade.pause();
                List<BaseStuPair> mTestingPairs = new ArrayList<>();
                for (BaseStuPair pair : mPairs) {
                    if (pair.getStudent() != null) {
                        isTestAllowed = true;
                        mTestingPairs.add(pair);
                    }
                }
                if (!isTestAllowed) {
                    toastSpeak("必须存在配对好的学生和设备才能开始计数");
                    mCheckingTasksFacade.resume();
                    break;
                }

                Intent intent = new Intent(this, SitUpTimingActivity.class);
                intent.putExtra(SitUpTimingActivity.TESTING_PAIRS, (Serializable) mTestingPairs);
                startActivity(intent);
                mCheckingTasksFacade.finish();
                // 退出,避免在后台占用资源
                finish();
                break;

            case R.id.btn_stop_use:
                mPairs.get(mCurrentPosition).getBaseDevice().setState(BaseDeviceState.STATE_STOP_USE);
                mAdapter.notifyItemChanged(mCurrentPosition);
                break;

            case R.id.btn_led_setting:
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;

            case R.id.btn_delete_student:
                mPairs.get(mCurrentPosition).setStudent(null);
                mAdapter.notifyItemChanged(mCurrentPosition);
                break;

            case R.id.btn_del_all:
                mCheckingTasksFacade.pause();
                for (BaseStuPair pair : mPairs) {
                    pair.setStudent(null);
                }
                mAdapter.notifyDataSetChanged();
                mCheckingTasksFacade.resume();
                break;

        }
    }

    private void updateSpecificItem(int position) {
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = position;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        int oldPosition = mCurrentPosition;
        mAdapter.setSelectItem(position);
        mCurrentPosition = position;
        if (oldPosition != mCurrentPosition) {
            mAdapter.notifyItemChanged(oldPosition);
            mAdapter.notifyItemChanged(position);
        }
    }

    private void setState(SitPushUpStateResult stateResult) {
        if (stateResult == null) {
            return;
        }
        int deviceId = stateResult.getDeviceId();
        if (deviceId > mMaxDeviceNo
                || stateResult.getState() != SitPushUpManager.STATE_FREE
                || mCurrentConnect[deviceId] != 0) {
            return;
        }
        BaseDeviceState deviceState = mPairs.get(stateResult.getDeviceId() - 1).getBaseDevice();
        if (deviceState.getState() == BaseDeviceState.STATE_STOP_USE) {
            return;
        }

        boolean lowBattery = stateResult.getBatteryLeft() <= 10;
        if (lowBattery) {
            deviceState.setState(BaseDeviceState.STATE_LOW_BATTERY);
        } else {
            deviceState.setState(BaseDeviceState.STATE_FREE);
        }
        mCurrentConnect[deviceId]++;
        updateSpecificItem(deviceId - 1);
    }

    @Override
    public void onGettingState(int position) {
        mSitPushUpManager.getState(position + 1);
    }

    @Override
    public void onStateRefreshed() {
        int size = mMaxDeviceNo;
        int oldState;
        for (int i = 0; i < size; i++) {
            BaseStuPair handStuPair = mPairs.get(i);
            BaseDeviceState deviceState = handStuPair.getBaseDevice();
            oldState = deviceState.getState();
            if (mCurrentConnect[deviceState.getDeviceId()] == 0
                    && oldState != BaseDeviceState.STATE_DISCONNECT
                    && oldState != BaseDeviceState.STATE_STOP_USE) {
                deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
                updateSpecificItem(i);
            }
        }
        mCurrentConnect = new int[mMaxDeviceNo + 1];
    }

    @Override
    public int getDeviceCount() {
        return mMaxDeviceNo;
    }

    // @Override
    // public Student getStuInPosition(int position){
    // 	return mPairs.get(position).getStudent();
    // }

    @Override
    public String getStringToShow(int position) {
        BaseStuPair stuPair = mPairs.get(position);
        BaseDeviceState jumpRopState = stuPair.getBaseDevice();
        return String.format("%-3s", jumpRopState.getDeviceId()/*) + String.format("%-6s",getStuInPosition(position).getStudentName()*/);
    }

    @Override
    public int getHostId() {
        return SettingHelper.getSystemSetting().getHostId();
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {

            case SerialConfigs.SIT_UP_GET_STATE:
                SitPushUpStateResult stateResult = (SitPushUpStateResult) msg.obj;
                //Log.i("james",stateResult.toString());
                setState(stateResult);
                break;

            case SerialConfigs.SIT_UP_MACHINE_BOOT_RESPONSE:
                SitPushUpSetFrequencyResult setFrequencyResult = (SitPushUpSetFrequencyResult) msg.obj;
                checkConnectingDevice(setFrequencyResult);
                break;

        }
    }



    private static class MyHandler extends Handler {

        private WeakReference<SitUpCheckActivity> mReference;

        public MyHandler(SitUpCheckActivity reference) {
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SitUpCheckActivity activity = mReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {

                case UPDATE_STATES:
                    activity.mAdapter.notifyDataSetChanged();
                    break;

                case UPDATE_SPECIFIC_ITEM:
                    activity.mAdapter.notifyItemChanged(msg.arg1);
                    break;

                case NO_PAIR_RESPONSE_ARRIVED:
                    if (activity.mLinking) {
                        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
                        activity.mCurrentFrequency = 0;
                    }
                    break;

                case DISMISS_DIALOG:
                    activity.mWaitDialog.dismiss();
                    break;

            }
        }
    }

    private void checkConnectingDevice(SitPushUpSetFrequencyResult result) {
        //Log.i("james",result.toString());
        if (!mLinking) {
            return;
        }
        if (result == null || result.getProjectCode() != projectCode) {
            return;
        }
        if (mCurrentFrequency == 0) {
            // 0频段接收到的结果,肯定是设备的开机广播
            if (result.getFrequency() == mTargetFrequency && result.getDeviceId() == mCurrentPosition + 1) {
                mCurrentFrequency = mTargetFrequency;
                RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(mTargetFrequency)));
                onNewDeviceConnect();
            } else {
                mSitPushUpManager.setFrequency(projectCode, result.getFrequency(), mCurrentPosition + 1, SettingHelper.getSystemSetting().getHostId());
                mCurrentFrequency = mTargetFrequency;
                // 那个铁盒子就是有可能等这么久才收到回复
                mHandler.sendEmptyMessageDelayed(NO_PAIR_RESPONSE_ARRIVED, 5000);
            }
        } else if (mCurrentFrequency == mTargetFrequency) {
            //在主机的目的频段收到的,肯定是设置频段后收到的设备广播
            if (result.getDeviceId() == mCurrentPosition + 1 && result.getFrequency() == mTargetFrequency) {
                onNewDeviceConnect();
            }
        }
    }

    private void onNewDeviceConnect() {
        mLinking = false;
        mPairs.get(mCurrentPosition).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        mCurrentConnect[mPairs.get(mCurrentPosition).getBaseDevice().getDeviceId()]++;
        updateSpecificItem(mCurrentPosition);
        mHandler.sendEmptyMessage(DISMISS_DIALOG);
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(mTargetFrequency)));
        mCheckingTasksFacade.resume();
    }

    private void show() {
        mWaitDialog = new WaitDialog(this);
        mWaitDialog.setCanceledOnTouchOutside(false);
        mWaitDialog.show();
        mWaitDialog.setTitle("请重启待连接设备");
        mWaitDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinking = false;
                RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(mTargetFrequency)));
                mWaitDialog.dismiss();
                mCheckingTasksFacade.resume();
            }
        });
    }

    @Override
    protected void onPause() {
        mCheckingTasksFacade.pause();
        RadioManager.getInstance().setOnRadioArrived(null);
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCheckingTasksFacade.finish();
    }

}
