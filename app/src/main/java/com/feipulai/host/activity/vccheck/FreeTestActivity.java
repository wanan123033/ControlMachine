package com.feipulai.host.activity.vccheck;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialPorter;
import com.feipulai.device.serial.beans.VCResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.entity.Student;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
@Deprecated
public class FreeTestActivity extends BaseActivity {
//    private static final String TAG = "FreeTestActivity";
    @BindView(R.id.tv_free_test_result)
    TextView mResult;
    @BindView(R.id.tv_retest)
    TextView tvRetest;
    @BindView(R.id.iv_head_photo)
    ImageView ivHeadPhoto;
    @BindView(R.id.tv_stuCode)
    TextView tvStuCode;
    @BindView(R.id.tv_stuName)
    TextView tvStuName;
    @BindView(R.id.tv_stu_sex)
    TextView tvStuSex;
    @BindView(R.id.tv_stu_mark)
    TextView tvStuMark;
    @BindView(R.id.view_top)
    View viewTop;
    private ExecutorService mExecutorService;
    private SerialPorter mSerialManager;
//    /** 是否重置*/
//    private boolean reset;
    private TestState mTestState = TestState.UN_STARTED;
    private GetResultRunnable resultRunnable = new GetResultRunnable();

    private Handler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_test);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
	    // TODO: 2018/11/9 0009 17:06
	    //mSerialManager = SerialPorter.getInstance();
        //mSerialManager.setHandler(mHandler);
        //mSerialManager.sendCommand(new ConvertCommand(new RS232SetCommand(9, 8, 0, 1)));
        mExecutorService = Executors.newSingleThreadExecutor();
        mExecutorService.submit(resultRunnable);

        sendCommand();

        Intent intent = getIntent();
        Student student = (Student) intent.getSerializableExtra("student");
        if (null != student) {
            tvStuCode.setText("学号:" + student.getStudentCode());
            tvStuName.setText("姓名:" + student.getStudentName());
            tvStuSex.setText("性别:" + (student.getSex() == 0 ? "女" : "男"));
        }
    }

    private void sendCommand() {
        toastSpeak("请测试");
        MyHandler.originValueUpdated = false ;
        mTestState = TestState.WAIT_RESULT ;
        resultRunnable.setTestState(mTestState);
    }

    @OnClick(R.id.tv_retest)
    public void onViewClicked() {
        mTestState = TestState.WAIT_RESULT ;
        resultRunnable.setTestState(mTestState);
//        reset = true;
        tvStuMark.setText("成绩:");
        mResult.setText("");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();

    }

    private static class MyHandler extends Handler{
        private WeakReference<FreeTestActivity> weakReference;
        private int currentValue;
        private int finalResultCout;
        public static boolean originValueUpdated;

        public MyHandler(FreeTestActivity activity){
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FreeTestActivity activity = weakReference.get();
            switch (msg.what) {
                case SerialConfigs.VITAL_CAPACITY_RESULT:
                    //等待数据更新,更新则认为已经开始吹气
                    VCResult result = (VCResult) msg.obj;
                    Log.i("zzs", "===>" + result.getResult() + "ml");
                    if (activity.mTestState == TestState.WAIT_RESULT) {
                        //todo updateDevice
                        Log.i("zzs", "WAIT_RESULT==currentValue=>" + currentValue + "ml");
                        //currentValue值非0变动时为第一次吹气

                        if (result.getResult() != currentValue) {
                            //在开始记录之前,必须保证这个值被初始化u=0
                            if (!originValueUpdated) {
                                currentValue = result.getResult();
                                originValueUpdated = true;
                                break;
                            }
                            //开始计数
                            currentValue = 0;
                            activity.mTestState = TestState.RESULT_UPDATING;
                            originValueUpdated = false;
                        }
                        //Logger.i(result.toString());
                    } else if (activity.mTestState == TestState.RESULT_UPDATING) {
                        //连续5次结果一致说明已经是最后结果
                        if (result.getResult() == currentValue) {
                            finalResultCout++;
                            if (finalResultCout == 5) {
                                result.setResult(currentValue);
                                activity.onResultArrived(result);
                                currentValue = 0;
                                finalResultCout = 0;
                            }
                        } else if (result.getResult() < currentValue && currentValue > 0) {
                            //值降低了,这个时候就是存在换气了,直接锁定最终结果
                            result.setResult(currentValue);
                            activity.onResultArrived(result);
                            currentValue = 0;
                            finalResultCout = 0;
                        } else {
                            currentValue = result.getResult();

                        }
                        activity.onResultUpdate(result);
                    } else if (activity.mTestState == TestState.DATA_DEALING) {
                        activity.onResultUpdate(result);
                        activity.resultRunnable.setTestState(TestState.UN_STARTED);
                        activity.mTestState = TestState.UN_STARTED;
                    }
                    break;
            }
        }
    }

    private void onResultUpdate(VCResult result) {
//        if (!reset){
            tvStuMark.setText("成绩:" +result.getResult()+"ml");
            mResult.setText(result.getResult()+"ml");
//        }

    }

    private void onResultArrived(VCResult result) {
        mTestState = TestState.DATA_DEALING;
//        if (!reset){
            tvStuMark.setText("成绩:" +result.getResult()+"ml");
            mResult.setText(result.getResult()+"ml");
//        }
//        reset = false ;
    }

    private class GetResultRunnable implements Runnable {
        TestState testState = TestState.UN_STARTED;

        public void setTestState(TestState testState) {
            this.testState = testState;
        }

        @Override
        public void run() {
            byte[] retrieve = {(byte) 0xaa, (byte) 0xc1, 0x00, 0x00, (byte) 0xc1};
            while (true) {
                if (testState != TestState.UN_STARTED) {
                    Log.i("zzs", "===>" + "sendCommand");
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, retrieve));
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
