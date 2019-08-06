package com.feipulai.host.activity.situp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.jump_rope.task.LEDContentGenerator;
import com.feipulai.common.jump_rope.task.LEDResultDisplayTask;
import com.feipulai.common.jump_rope.task.LEDResultDisplayer;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.printer.PrinterState;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseResultDisplayAdapter;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;
import com.feipulai.host.utils.ResultDisplayUtils;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SitUpResultActivity
        extends BaseActivity
        implements LEDContentGenerator,
        BaseQuickAdapter.OnItemClickListener,
        PrinterManager.OnPrinterListener {

    public static final String TEST_RESULTS = "TEST_RESULTS";
    private static final int MSG_DATA_SAVED = 0X01;
    private static final int CHECK_PRINT_SERVICE = 0x02;

    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;
    @BindView(R.id.btn_ok)
    Button mBtnOk;

    private List<BaseStuPair> mPairs;

    private int mCurrentPosition;

    private String mTestTime;
    private String mPrintTime;
    private ProgressDialog mProgressDialog;
    private BaseResultDisplayAdapter mAdapter;

    private ExecutorService mExecutor;
    private LEDResultDisplayTask mLEDResultDisplayTask;

    private ItemSubscriber itemSubscriber;

    @SuppressWarnings("handlerleak")
    private Handler mHandler = new MyHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sit_up_result);
        ButterKnife.bind(this);
        init();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        Intent intent = getIntent();
        mPairs = (List<BaseStuPair>) intent.getSerializableExtra(TEST_RESULTS);


        mExecutor = Executors.newFixedThreadPool(1);
        mLEDResultDisplayTask = new LEDResultDisplayTask(new LEDResultDisplayer(mPairs.size(), SettingHelper.getSystemSetting().getHostId(), this));
        mExecutor.execute(mLEDResultDisplayTask);

        mRvPairs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(false);
        dividerItemDecoration.setDrawBorderLeftAndRight(false);
        mRvPairs.addItemDecoration(dividerItemDecoration);

        initAdapter();

        mTestTime = TestConfigs.df.format(Calendar.getInstance().getTime());

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("保存成绩");
        mProgressDialog.setMessage("保存成绩中...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            itemSubscriber = new ItemSubscriber();
        }
        if (SettingHelper.getSystemSetting().isAutoPrint()) {
//			isAutoPrint = false;
            PrinterManager.getInstance().setOnPrinterListener(this);
            PrinterManager.getInstance().getState();
            // 如果要打印成绩等1s检查打印机是否可用
            mHandler.sendEmptyMessageDelayed(CHECK_PRINT_SERVICE, 1000);
        } else {
            saveRoundResults();
        }
    }

    private void initAdapter() {
        mAdapter = new BaseResultDisplayAdapter(mPairs);
        mAdapter.setSelectItem(-1);
        mRvPairs.setAdapter(mAdapter);

        mRvPairs.setClickable(true);
        mAdapter.setOnItemClickListener(this);
    }

    @OnClick(R.id.btn_ok)
    public void onViewClicked() {
        finish();
    }

    // @Override
    public String generate(BaseStuPair pair) {
        String showContent;
        int deviceId = pair.getBaseDevice().getDeviceId();
        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_STOP_USE) {
            showContent = String.format("%-3d", deviceId) + "暂停使用";
        } else {
            String studentName = pair.getStudent().getStudentName();
            if (studentName.length() >= 4) {
                studentName = studentName.substring(0, 4);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(studentName);
                int spaces = 8 - studentName.length() * 2;
                //Log.i("james","spaces:" + spaces);
                for (int j = 0; j < spaces; j++) {
                    sb.append(' ');
                }
                studentName = sb.toString();
                //Log.i("james",studentName);
            }
            showContent = String.format("%-3d", deviceId) +
                    studentName + String.format("%-3d", pair.getResult());
        }
        return showContent;
    }

    @Override
    public void onPrinterListener(Message msg) {
        if (msg.what == SerialConfigs.PRINTER_STATE) {
            PrinterState state = (PrinterState) msg.obj;
            if (state.isPaperLack()) {
                toastSpeak("打印机缺纸");
            } else if (state.isOverHeat()) {
                toastSpeak("打印机过热");
            } else {
//				isAutoPrint = true;
            }
            //Log.i("james","receive state:" + state.toString());
        }
    }

    @Override
    public String generate(int position) {
        return null;
    }

    private static class MyHandler extends Handler {

        private WeakReference<SitUpResultActivity> mReference;

        public MyHandler(SitUpResultActivity reference) {
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SitUpResultActivity activity = mReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_DATA_SAVED:
                    ToastUtils.showShort("数据保存成功");
                    activity.mProgressDialog.dismiss();
                    break;

                case CHECK_PRINT_SERVICE:
                    activity.saveRoundResults();
                    break;
            }
        }

    }

    private void saveRoundResults() {
        int size = mPairs.size();
        for (int i = 0; i < size; i++) {
            // TODO: 2018/8/17 0017 11:28 这里手柄状态不对的成绩是否要保存?
            saveRoundResult(mPairs.get(i));
        }
        mHandler.sendEmptyMessage(MSG_DATA_SAVED);
    }

    @Override
    public void finish() {
        mLEDResultDisplayTask.cancel();
        Intent intent = new Intent(this, SitUpCheckActivity.class);
        startActivity(intent);
        super.finish();
    }

    private void printResult(BaseStuPair pair, int result) {
        String displayResult = ResultDisplayUtils.getStrResultForDisplay(result);
        PrinterManager.getInstance().print("\n");
        PrinterManager.getInstance().print("仰卧起坐  " + SettingHelper.getSystemSetting().getHostId() + "号机\n");
        PrinterManager.getInstance().print("考  号:" + pair.getStudent().getStudentCode() + "\n");
        PrinterManager.getInstance().print("姓  名:" + pair.getStudent().getStudentName() + "\n");
        PrinterManager.getInstance().print("设备号:" + pair.getBaseDevice().getDeviceId() + "\n");
        PrinterManager.getInstance().print("成  绩:" + displayResult + "\n");
        PrinterManager.getInstance().print("打印时间:" + mPrintTime + "\n");
        PrinterManager.getInstance().print("\n");
    }

    private void saveRoundResult(BaseStuPair pair) {

        Student student = pair.getStudent();

        int maxValue = TestConfigs.sCurrentItem.getMaxValue();
        int minValue = TestConfigs.sCurrentItem.getMinValue();

        //保存成绩
        RoundResult roundResult = new RoundResult();

        int result = pair.getResult();
        // 转换为数据库格式  数据库中单位也是 "次",无需转换
        //result = result;
        if (!(maxValue == 0 && minValue == 0)) {
            if (result > maxValue) {
                result = maxValue;
            } else if (minValue != 0 && minValue > result) {
                result = minValue;
            }
        }

        //设置数据库格式成绩其他内容
        roundResult.setStudentCode(student.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setResult(result);
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(mTestTime);
        roundResult.setRoundNo(1);

        // 找最好成绩比较
        // 决定本次成绩是否为最好成绩
        RoundResult bestResult = DBManager.getInstance().queryBestScore(student.getStudentCode());

        if (bestResult != null) {
            // 原有最好成绩犯规(体侧不存在) 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= result) {
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

        if (SettingHelper.getSystemSetting().isAutoPrint()) {
            mPrintTime = TestConfigs.df.format(Calendar.getInstance().getTime());
            roundResult.setPrintTime(mPrintTime);
            printResult(pair, result);
            DBManager.getInstance().updateRoundResult(roundResult);
        }

        if (SettingHelper.getSystemSetting().isRtUpload()) {
            //选最好成绩为最终成绩
            if (TestConfigs.sCurrentItem.getFResultType() == 0) {
                itemSubscriber.setDataUpLoad(roundResult, bestResult);
            } else {
                itemSubscriber.setDataUpLoad(roundResult, roundResult);
            }
        }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLEDResultDisplayTask.cancel();
        mExecutor.shutdown();
    }

}
