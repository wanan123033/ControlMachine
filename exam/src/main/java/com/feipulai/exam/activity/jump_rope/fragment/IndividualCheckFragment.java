package com.feipulai.exam.activity.jump_rope.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.CheckDeviceOpener;
import com.feipulai.device.ic.ICCardDealer;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.device.ic.entity.StuInfo;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.jump_rope.view.StuSearchEditText;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.view.AddStudentDialog;
import com.orhanobut.logger.Logger;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by James on 2018/12/10 0010.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class IndividualCheckFragment
        extends Fragment
        implements StuSearchEditText.OnCheckedInListener,
        CheckDeviceOpener.OnCheckDeviceArrived {

    private static final int CHECK_IN = 0x0;

    @BindView(R.id.et_select)
    StuSearchEditText mEtSelect;

    private ListView lvResults;
    private OnIndividualCheckInListener listener;

    private MyHandler mHandler = new MyHandler(this);

    private static final int STUDENT_CODE = 0x0;
    private static final int ID_CARD_NO = 0x1;

    private Student mStudent;
    private StudentItem mStudentItem;
    private List<RoundResult> mResults;
    private SystemSetting systemSetting;
    private SweetAlertDialog addDialog;

    public void setResultView(ListView lvResults) {
        this.lvResults = lvResults;
    }

    public void setOnIndividualCheckInListener(OnIndividualCheckInListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_individual_check, container, false);
        ButterKnife.bind(this, view);
        mEtSelect.setResultView(lvResults);
        mEtSelect.setOnCheckedInListener(this);
        EventBus.getDefault().register(this);
        systemSetting = SharedPrefsUtil.loadFormSource(getActivity(), SystemSetting.class);
        return view;
    }

    @Override
    public void onResume() {
        CheckDeviceOpener.getInstance().setQrLength(systemSetting.getQrLength());
        CheckDeviceOpener.getInstance().setOnCheckDeviceArrived(this);
        int checkTool = SettingHelper.getSystemSetting().getCheckTool();
        CheckDeviceOpener.getInstance().open(getActivity(), checkTool == SystemSetting.CHECK_TOOL_IDCARD,
                checkTool == SystemSetting.CHECK_TOOL_ICCARD,
                checkTool == SystemSetting.CHECK_TOOL_QR);
        // CheckDeviceOpener.getInstance().open(getActivity(), true, true, true);
        super.onResume();
    }

    @Subscribe
    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent.getTagInt() == EventConfigs.TEMPORARY_ADD_STU && listener != null) {
            Student student = (Student) baseEvent.getData();
            StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
            hideSoftInput();
            listener.onIndividualCheckIn(student, studentItem, null);
        }
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mEtSelect.getWindowToken(), 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CheckDeviceOpener.getInstance().close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        //null.unbind();
    }

    private void checkInUIThread(Student student) {
        Message msg = Message.obtain();
        msg.what = CHECK_IN;
        msg.obj = student;
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean onInputCheck(Student student) {
        boolean needAdd = checkQulification(student.getStudentCode(), STUDENT_CODE);
        if (needAdd) {
            showAddHint(student);
        }
        return true;
    }

    @Override
    public void onICCardFound(NFCDevice nfcd) {
        long startTime = System.currentTimeMillis();
        ICCardDealer icCardDealer = new ICCardDealer(nfcd);
        StuInfo stuInfo = icCardDealer.IC_ReadStuInfo();

        if (stuInfo == null || TextUtils.isEmpty(stuInfo.getStuCode())) {
            TtsManager.getInstance().speak("读卡(ka3)失败");
            InteractUtils.toast(getActivity(), "读卡失败");
            return;
        }

        Logger.i("处理IC卡时间:" + (System.currentTimeMillis() - startTime) + "ms");
        Logger.i("iccard readInfo:" + stuInfo.toString());
        boolean needAdd = checkQulification(stuInfo.getStuCode(), STUDENT_CODE);
        if (needAdd) {
            Student student = new Student();
            student.setStudentCode(stuInfo.getStuCode());
            student.setStudentName(stuInfo.getStuName());
            student.setSex(stuInfo.getSex());
            showAddHint(student);
        }
    }

    @Override
    public void onIdCardRead(IDCardInfo idCardInfo) {
        boolean needAdd = checkQulification(idCardInfo.getId(), ID_CARD_NO);
        if (needAdd) {
            Student student = new Student();
            student.setIdCardNo(idCardInfo.getId());
            showAddHint(student);
        }
    }

    @Override
    public void onQrArrived(String qrCode) {
        boolean needAdd = checkQulification(qrCode, STUDENT_CODE);
        if (needAdd) {
            Student student = new Student();
            student.setStudentCode(qrCode);
            showAddHint(student);
        }
    }

    @Override
    public void onQRWrongLength(int length, int expectLength) {
        // InteractUtils.toast(getActivity(), "条码/二维码长度不正确,长度:" + length + "\t预期长度:" + expectLength);
        InteractUtils.toast(getActivity(), "条码与当前设置位数不一致,请重扫条码");
    }

    // 可能在子线程运行
    // 返回是否需要新增考生
    private boolean checkQulification(String code, int flag) {
        Student student = null;
        boolean canTemporaryAdd = SettingHelper.getSystemSetting().isTemporaryAddStu();
        switch (flag) {

            case ID_CARD_NO:
                student = DBManager.getInstance().queryStudentByIDCode(code);
                break;

            case STUDENT_CODE:
                student = DBManager.getInstance().queryStudentByStuCode(code);
                break;

        }
        if (student == null) {
            // Log.i("james",canTemporaryAdd + "");
            if (!canTemporaryAdd) {
                InteractUtils.toastSpeak(getActivity(), "该考生不存在");
            }
            return canTemporaryAdd;
        }
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            InteractUtils.toastSpeak(getActivity(), "无此项目");
            return false;
        }
        List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
        if (results != null && results.size() >= TestConfigs.getMaxTestCount(getActivity())) {
            InteractUtils.toastSpeak(getActivity(), "该考生已测试");
            return false;
        }
        mStudent = student;
        mStudentItem = studentItem;
        mResults = results;
        // 可以直接检录
        checkInUIThread(student);
        return false;
    }

    private static class MyHandler extends Handler {

        private WeakReference<IndividualCheckFragment> mReference;

        public MyHandler(IndividualCheckFragment reference) {
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            IndividualCheckFragment fragment = mReference.get();
            if (fragment == null) {
                return;
            }
            switch (msg.what) {
                case CHECK_IN:
                    fragment.check();
                    break;
            }
        }
    }

    private void check() {
        if (listener != null) {
            hideSoftInput();
            listener.onIndividualCheckIn(mStudent, mStudentItem, mResults);
        }
    }

    private void showAddHint(final Student student) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (addDialog == null) {
                    addDialog = new SweetAlertDialog(getActivity()).setTitleText(getString(R.string.addStu_dialog_title))
                            .setContentText(getString(R.string.addStu_dialog_content))
                            .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                    addDialog = null;
                                    new AddStudentDialog(getActivity()).showDialog(student, false);
                                }
                            }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                    addDialog = null;
                                }
                            });
                }

                if (addDialog != null) {
                    addDialog.show();
                }
            }
        });
    }

    public interface OnIndividualCheckInListener {
        /**
         * 这里传入的考生信息均通过了验证
         * 检录的考生----对应的报名信息-----报名信息对应的成绩
         */
        void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results);
    }

}
