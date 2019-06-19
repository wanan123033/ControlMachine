package com.feipulai.host.activity.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.host.R;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.orhanobut.logger.Logger;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * (个人测试)的人脸识别与自由测试基类
 * 采用事件总线的方式做数据的更新 具体可参见
 * Created by zzs on 2018/8/10
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class BasePersonFaceIDActivity extends BaseActivity {


    @BindView(R.id.surfaceview)
    SurfaceView surfaceview;
    @BindView(R.id.img_portrait)
    ImageView imgPortrait;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_stu_sex)
    TextView txtStuSex;
    @BindView(R.id.txt_stu_result)
    TextView txtStuResult;
    @BindView(R.id.iv_device_state)
    ImageView ivDeviceState;

    /**
     * 当前设备
     */
    private BaseStuPair pair = new BaseStuPair();
    private OnMalfunctionClickListener listener;
    //是否自动打印
    private boolean isAutoPrint;
    public int hostId;
    //是否自动播报
    private boolean mNeedBroadcast;
    //是否自动上传成绩
    private boolean mIsResultUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base4_faceid);
        ButterKnife.bind(this);
        mNeedBroadcast = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.GRADE_BROADCAST, true);
        isAutoPrint = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.AUTO_PRINT, false);
        mIsResultUpload = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.REAL_TIME_UPLOAD, false);
        hostId = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.HOST_ID, 1);
        ivDeviceState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_ERROR && listener != null) {
                    listener.malfunctionClickListener(pair);
                }
            }
        });

        if (findDevice() == null) {
            ivDeviceState.setImageResource(R.drawable.ic_pan_tool);
        } else {
            //添加测试设备
            addDevice(findDevice());
        }
        //Intent空则是自由测试
        if (getIntent() != null) {
            Student student = (Student) getIntent().getSerializableExtra(Student.BEAN_KEY);
            //当有学生存在则是学生测试，没有学生则是自由测试
            if (student != null) {
                pair.setStudent(student);
                refreshTxtStu(student);
                //设备存在发送测试指令
                if (pair.getBaseDevice() != null) {
                    sendTestCommand(pair);
                }
            } else {
                //设备存在发送测试指令
                if (pair.getBaseDevice() != null) {
                    sendTestCommand(pair);
                }
            }
        } else {
            //设备存在发送测试指令
            if (pair.getBaseDevice() != null) {
                sendTestCommand(pair);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrinterManager.getInstance().close();
    }

    /**
     * 查询设备
     * 找到设备后需添加设备调用  addDevice方法
     */
    public abstract BaseDeviceState findDevice();

    /**
     * 发送测试指令 并且此时应将设备状态改变
     */
    public abstract void sendTestCommand(@NonNull BaseStuPair baseStuPair);

//    /**
//     * 设置项目单位
//     */
//    public abstract String setUnit();

    /**
     * 加载学生信息
     */
    private void refreshTxtStu(@NonNull Student student) {
        txtStuName.setText(student.getStudentName());
        txtStuSex.setText(student.getSex() == 0 ? "男" : "女");
        txtStuCode.setText(student.getStudentCode());
    }

    /**
     * 修改设备状态
     */
    public void updateDevice(@NonNull BaseDeviceState deviceState) {
        Logger.i("updateDevice==>" + deviceState.toString());
        if (pair.getBaseDevice() != null && pair.getBaseDevice().getDeviceId() == deviceState.getDeviceId()) {
            pair.getBaseDevice().setState(deviceState.getState());
            //状态为测试已结束
            if (deviceState.getState() == BaseDeviceState.STATE_END) {
                //保存成绩
                if (pair.getStudent() != null) {
                    saveResult(pair);
                    printResult(pair);
                }
                broadResult(pair);
                pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
            }
        }
        refreshDevice();
    }

    private void refreshDevice() {
        if (pair.getBaseDevice() != null) {
            if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                ivDeviceState.setImageResource(R.drawable.ic_radio_checked);
            } else {
                ivDeviceState.setImageResource(R.drawable.ic_pan_tool);
            }

        }

    }

    /**
     * 更新学生成绩
     *
     * @param baseStu
     */
    public synchronized void updateResult(@NonNull BaseStuPair baseStu) {
        Logger.i("updateResult==>" + baseStu.toString());
        if (null != pair.getBaseDevice()
                && baseStu.getBaseDevice().getDeviceId() == baseStu.getBaseDevice().getDeviceId()) {
            pair.getBaseDevice().setState(baseStu.getBaseDevice().getState());
            pair.setResult(baseStu.getResult());
            txtStuResult.setText(ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult()));
            refreshDevice();
        }
    }


    public synchronized void addDevice(@NonNull BaseDeviceState device) {
        if (pair.getBaseDevice() != null && pair.getBaseDevice().getDeviceId() == device.getDeviceId())
            return;//已经有了设备就不再添加
        pair.setBaseDevice(device);
        refreshDevice();
    }

    private void saveResult(@NonNull BaseStuPair baseStuPair) {
        Logger.i("saveResult==>" + baseStuPair.toString());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
        roundResult.setItemCode(itemCode);
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        roundResult.setTestTime(TestConfigs.df.format(Calendar.getInstance().getTime()));
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
        if (!mIsResultUpload) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }

//        new RequestBiz().setDataUpLoad(roundResult, lastResult);
        new ItemSubscriber().setDataUpLoad(roundResult, lastResult);

    }

    /**
     * 播报结果
     */
    private void broadResult(@NonNull BaseStuPair baseStuPair) {
        if (mNeedBroadcast) {
            //成绩状态是否为犯规
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak((baseStuPair.getStudent() == null ? "" : baseStuPair.getStudent().getStudentName()) + "犯规");
            } else {
                TtsManager.getInstance().speak((baseStuPair.getStudent() == null
                        ? "" : baseStuPair.getStudent().getStudentName()) + ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult()));
            }
        }
    }

    private void printResult(@NonNull BaseStuPair baseStuPair) {
        if (!isAutoPrint)
            return;
        Student student = baseStuPair.getStudent();
        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + hostId + "号机\n");
        PrinterManager.getInstance().print("考  号:" + student.getStudentCode() + "\n");
        PrinterManager.getInstance().print("姓  名:" + student.getStudentName() + "\n");
        PrinterManager.getInstance().print("成  绩:" + ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult()) + "\n");
        PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()) + "\n");
        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(" \n");
    }

    public interface OnMalfunctionClickListener {
        void malfunctionClickListener(@NonNull BaseStuPair baseStuPair);
    }

    public void setOnMalfunctionClickListener(OnMalfunctionClickListener listener) {
        this.listener = listener;
    }

    public void refreshTxt(Student student) {
        if (student != null) {
            txtStuName.setText(student.getStudentName());
            txtStuSex.setText(student.getSex() == 0 ? "男" : "女");
            txtStuCode.setText(student.getStudentCode());
        }

        txtStuResult.setText("");

    }

}
