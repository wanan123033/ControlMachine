package com.feipulai.host.activity.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.PullToRefreshView;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.CheckDeviceOpener;
import com.feipulai.device.ic.ICCardDealer;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.device.ic.entity.StuInfo;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.height_weight.HWConfigs;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.setting.SystemSetting;
import com.feipulai.host.adapter.DataRetrieveAdapter;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.view.StuSearchEditText;
import com.orhanobut.logger.Logger;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 数据查询
 * Created by james on 2018/1/2.
 */
public class DataRetrieveActivity extends BaseTitleActivity
        implements CompoundButton.OnCheckedChangeListener,
        OnRefreshLoadmoreListener, CheckDeviceOpener.OnCheckDeviceArrived {

    public static final String DATA_EXTRA = "data_extra";
    public final static String UPDATE_MESSAGE = "com.feipulai.host.update_data_message";

    @BindView(R.id.rb_all)
    RadioButton mRbAll;
    @BindView(R.id.et_input_text)
    EditText mEtInputText;
    @BindView(R.id.btn_query)
    Button mBtnQuery;
    @BindView(R.id.btn_upload)
    Button mBtnUpload;
    @BindView(R.id.cb_select_all)
    CheckBox mCbSelectAll;
    @BindView(R.id.rv_results)
    RecyclerView mRvResults;
    @BindView(R.id.refreshview)
    PullToRefreshView mRefreshView;
    @BindView(R.id.cb_un_upload)
    CheckBox cbUnUpload;
    @BindView(R.id.cb_uploaded)
    CheckBox cbUploaded;
    @BindView(R.id.cb_tested)
    CheckBox cbTested;
    @BindView(R.id.cb_un_tested)
    CheckBox cbUnTested;
    @BindView(R.id.txt_stu_sumNumber)
    TextView txtStuSumNumber;
    @BindView(R.id.txt_stu_manNumber)
    TextView txtStuManNumber;
    @BindView(R.id.txt_stu_womemNumber)
    TextView txtStuWomemNumber;

    private DataRetrieveAdapter mAdapter;
    private List<DataRetrieveBean> mList;
    private static final int LOAD_ITEMS = 100;
    private int mPageNum;

    public BroadcastReceiver receiver;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_retrieve;
    }

    @Override
    protected void initData() {
        mRbAll.setChecked(true);
        registerReceiver();
        mCbSelectAll.setOnCheckedChangeListener(this);
        cbUploaded.setOnCheckedChangeListener(this);
        cbUnUpload.setOnCheckedChangeListener(this);
        cbUnTested.setOnCheckedChangeListener(this);
        cbTested.setOnCheckedChangeListener(this);
        mRbAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbUploaded.setChecked(false);
                    cbUnUpload.setChecked(false);
                    cbTested.setChecked(false);
                    cbUnTested.setChecked(false);
                    mPageNum = 0;
                    //查全部
                    setAllList();
                }
            }
        });

        mEtInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    if (mRbAll.isChecked()) {
                        mPageNum = 0;
                        mList.clear();
                        setAllList();
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mRbAll.setChecked(true);
                    }
                }
            }
        });

        setListAdapter();
        //查全部
        setAllList();

    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle(R.string.data_retrieve_title);
    }

    @Override
    public void onICCardFound(NFCDevice nfcd) {
        ICCardDealer icCardDealer = new ICCardDealer(nfcd);
        StuInfo stuInfo = icCardDealer.IC_ReadStuInfo();

        if (stuInfo == null || TextUtils.isEmpty(stuInfo.getStuCode())) {
//            TtsManager.getInstance().speak("读卡(ka3)失败");
            InteractUtils.toast(this, getString(R.string.read_iccard_failed));
            return;
        }

        Logger.i("iccard readInfo:" + stuInfo.toString());
        setUiQueryData(stuInfo.getStuCode());
    }

    @Override
    public void onIdCardRead(IDCardInfo idCardInfo) {
        Student student = DBManager.getInstance().queryStudentByIDCode(idCardInfo.getId());
        if (student == null) {
            InteractUtils.toast(this, getString(R.string.student_nonentity));
        } else {
            setUiQueryData(student.getStudentCode());
        }
    }

    @Override
    public void onQrArrived(String qrCode) {
        setUiQueryData(qrCode);
    }

    @Override
    public void onQRWrongLength(int length, int expectLength) {
        InteractUtils.toast(this, getString(R.string.qr_length_error));
    }


    @Override
    public void onLoadmore(RefreshLayout refreshlayout) {
        if (TextUtils.isEmpty(mEtInputText.getText().toString().trim())) {

            if (mRbAll.isChecked()) {
                setAllList();
            } else {
                chooseStudent();
            }
        } else {
            queryData(mEtInputText.getText().toString().trim());
        }
        mAdapter.notifyDataSetChanged();
        Logger.i("loading");
        // 加载完数据设置为不加载状态,将加载进度收起来
        mRefreshView.finishRefreshAndLoad();
    }

    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        mPageNum = 0;
        mRefreshView.setLoadmoreFinished(false);
        if (TextUtils.isEmpty(mEtInputText.getText().toString().trim())) {

            if (mRbAll.isChecked()) {
                setAllList();
            } else {
                chooseStudent();
            }
        } else {
            queryData(mEtInputText.getText().toString().trim());
        }
        mRefreshView.finishRefreshAndLoad();
    }

    private void setUiQueryData(final String stuCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEtInputText.setText(stuCode);
                mPageNum = 0;
                queryData(stuCode);
            }
        });
    }


    @OnClick({R.id.btn_query, R.id.btn_upload/*, R.id.rb_all*/})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btn_query:
                String input = mEtInputText.getText().toString().trim();
                mPageNum = 0;
                if (!StuSearchEditText.patternStuCode(input)) {
                    ToastUtils.showShort(R.string.data_search_hint);
                } else {
                    queryData(input);
                }
                break;

            case R.id.btn_upload:
                List<RoundResult> roundResults = new ArrayList<>();
                for (int i = 0; i < mList.size(); i++) {
                    DataRetrieveBean bean = mList.get(i);
                    if (bean.isChecked() && bean.getTestState() == 1) {
                        List<RoundResult> results = DBManager.getInstance().queryUploadStudentResults(bean.getStudentCode(), false);
                        roundResults.addAll(results);
                    }
                }
                if (roundResults.size() == 0) {
                    ToastUtils.showShort(R.string.please_no_upload_stu);
                } else {
                    //上传数据前先进行项目信息校验
                    ItemSubscriber subscriber = new ItemSubscriber();
                    subscriber.getItemAll(MyApplication.TOKEN, this, null, roundResults);
                }
                break;

//            case R.id.rb_all:
//                if (mRbAll.isChecked()) {
//                    setCheckStates(true, false, false, false, false);
//                    setAllList();
//                }
//                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckDeviceOpener.getInstance().setQrLength(SettingHelper.getSystemSetting().getQrLength());
        CheckDeviceOpener.getInstance().setOnCheckDeviceArrived(this);
        int checkTool = SettingHelper.getSystemSetting().getCheckTool();
        CheckDeviceOpener.getInstance().open(this, checkTool == SystemSetting.CHECK_TOOL_IDCARD,
                checkTool == SystemSetting.CHECK_TOOL_ICCARD,
                checkTool == SystemSetting.CHECK_TOOL_QR);
        // CheckDeviceOpener.getInstance().open(this, true, true, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CheckDeviceOpener.getInstance().close();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_select_all:
                for (int i = 0; i < mList.size(); i++) {
                    mList.get(i).setChecked(isChecked);
                }
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.cb_tested://已测
                if (isChecked) {
                    cbTested.setChecked(true);
                    mRbAll.setChecked(false);
                    cbUnTested.setChecked(false);
                }
                selectChoose(isChecked);
                break;
            case R.id.cb_un_tested://未测
                if (isChecked) {
                    cbUnTested.setChecked(true);
                    mRbAll.setChecked(false);
                    cbTested.setChecked(false);
                }
                selectChoose(isChecked);
                break;
            case R.id.cb_uploaded://已上传
                if (isChecked) {
                    cbUploaded.setChecked(true);
                    mRbAll.setChecked(false);
                    cbUnUpload.setChecked(false);
                }
                selectChoose(isChecked);
                break;
            case R.id.cb_un_upload://未上传
                if (isChecked) {
                    cbUnUpload.setChecked(true);
                    mRbAll.setChecked(false);
                    cbUploaded.setChecked(false);
                }
                selectChoose(isChecked);
                break;

        }

    }

    public void selectChoose(boolean isChecked) {
        //非选择全部的复选框

        mPageNum = 0;
        mList.clear();
        //刷选条件必须至少有一个
        if (cbUploaded.isChecked() || cbUnUpload.isChecked() || cbTested.isChecked() || cbUnTested.isChecked()) {
            Logger.i("cbUploaded===>" + cbUploaded.isChecked());
            Logger.i("cbUnUpload===>" + cbUnUpload.isChecked());
            Logger.i("cbTested===>" + cbTested.isChecked());
            Logger.i("cbUnTested===>" + cbUnTested.isChecked());
            chooseStudent();

        } else {
            Logger.i("mRbAll===>" + mRbAll.isChecked());
            mPageNum = 0;
            if (!mRbAll.isChecked()) {
                mRbAll.setChecked(true);
//                //查全部
//                setAllList();
            }
        }
    }

    /**
     * 查找所有学生信息
     */
    private void setAllList() {

        DataBaseExecutor.addTask(new DataBaseTask(this, getString(R.string.loading_hint), true) {
            @Override
            public DataBaseRespon executeOper() {
                List<Student> studentList = DBManager.getInstance().getItemStudent(
                        LOAD_ITEMS, LOAD_ITEMS * mPageNum);
                return new DataBaseRespon(true, "", studentList);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                Student student;
                List<Student> studentList = (List<Student>) respon.getObject();
                if (mPageNum == 0) {
                    mList.clear();
                    Map<String, Object> countMap = DBManager.getInstance().getItemStudenCount();
                    setStuCount(countMap.get("count"), countMap.get("women_count"), countMap.get("man_count"));
                    Logger.i("zzs===>" + countMap.toString());
                }
                if (studentList == null || studentList.size() == 0) {
                    ToastUtils.showShort(R.string.no_more_data);
                    mRefreshView.finishRefreshAndLoad();
                    mRefreshView.setLoadmoreFinished(true);
                    mAdapter.notifyDataSetChanged();
                    return;
                }
                //这个必须在获取到数据后再自增
                mPageNum++;
                for (int i = 0; i < studentList.size(); i++) {
                    //获取学生信息
                    student = studentList.get(i);
                    String result = displaStuResult(student.getStudentCode());
                    mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), TextUtils.equals(result, "-1000") ? 0 : 1, result, mCbSelectAll.isChecked()));
                }
                mRefreshView.finishRefreshAndLoad();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }


    // 模糊查询学生信息
    private void queryData(final String inputText) {

        if (TextUtils.isEmpty(inputText)) {
            ToastUtils.showShort(R.string.please_edit_search);
            return;
        }
        DataBaseExecutor.addTask(new DataBaseTask(this, getString(R.string.loading_hint), true) {
            @Override
            public DataBaseRespon executeOper() {
                List<Student> students = DBManager.getInstance().fuzzyQueryByStuCode(inputText, LOAD_ITEMS, LOAD_ITEMS *
                        mPageNum);
                return new DataBaseRespon(true, "", students);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                List<Student> students = (List<Student>) respon.getObject();
                if (mPageNum == 0) {
                    if (students == null || students.size() == 0) {
                        ToastUtils.showShort(R.string.student_nonentity);
                        mList.clear();
                        mAdapter.notifyDataSetChanged();
                        return;
                    }
                    mList.clear();
                } else {
                    if (students == null || students.size() == 0) {
                        ToastUtils.showShort(R.string.no_more_data);
                        mAdapter.notifyDataSetChanged();
                        mRefreshView.finishRefreshAndLoad();
                        mRefreshView.setLoadmoreFinished(true);
                        return;
                    }
                }
                mPageNum++;
                for (int i = 0; i < students.size(); i++) {
                    Student student = students.get(i);
                    String result = displaStuResult(student.getStudentCode());
                    mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), TextUtils.equals(result, "-1000") ? 0 : 1, result, mCbSelectAll.isChecked()));

                }
                mRefreshView.finishRefreshAndLoad();
                mAdapter.notifyDataSetChanged();
                Map<String, Object> countMap = DBManager.getInstance().fuzzyQueryByStuCodeCount(inputText);
                setStuCount(countMap.get("count"), countMap.get("women_count"), countMap.get("man_count"));
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });

    }

    private String displaStuResult(String studentCode) {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
            RoundResult mLastHeightResult = DBManager.getInstance().queryLastScoreByStuCode(studentCode, HWConfigs
                    .HEIGHT_ITEM);
            RoundResult mLastWeightResult = DBManager.getInstance().queryLastScoreByStuCode(studentCode, HWConfigs.WEIGHT_ITEM);

            if (mLastHeightResult != null && mLastWeightResult != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(ResultDisplayUtils.getStrResultForDisplay(mLastHeightResult.getResult(), HWConfigs.HEIGHT_ITEM));
                sb.append("/");
                sb.append(ResultDisplayUtils.getStrResultForDisplay(mLastWeightResult.getResult(), HWConfigs.WEIGHT_ITEM));
                return sb.toString();
            }
            return "-1000";
        } else {
            RoundResult result = DBManager.getInstance().queryResultsByStudentCodeIsLastResult(studentCode);
            return result != null ? (result.getResultState() == RoundResult.RESULT_STATE_FOUL ? "X" : result.getResult()) + "" : "-1000";
        }

    }

    private void setListAdapter() {
        mList = new ArrayList<>();
        mAdapter = new DataRetrieveAdapter(mList);
        mRvResults.setLayoutManager(new LinearLayoutManager(this));
        mRvResults.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(DataRetrieveActivity.this, DataDisplayActivity.class);
                intent.putExtra(DATA_EXTRA, mList.get(position));
                startActivity(intent);
                Logger.i("onItemClick:" + position);
            }
        });
        mRefreshView.setOnRefreshLoadmoreListener(this);
        mRefreshView.setEnableAutoLoadmore(true);
    }

    private void chooseStudent() {

        DataBaseExecutor.addTask(new DataBaseTask(this, getString(R.string.loading_hint), true) {
            @Override
            public DataBaseRespon executeOper() {
                List<Student> studentList = DBManager.getInstance().getChooseStudentList(cbTested.isChecked(),
                        cbUnTested.isChecked(),
                        cbUploaded.isChecked(), cbUnUpload.isChecked(), LOAD_ITEMS, LOAD_ITEMS * mPageNum);
                return new DataBaseRespon(true, "", studentList);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                Student student;
                List<Student> studentList = (List<Student>) respon.getObject();

                if (mPageNum == 0) {
                    mList.clear();
                    Map<String, Object> countMap = DBManager.getInstance().getChooseStudentCount(cbTested.isChecked(), cbUnTested.isChecked(),
                            cbUploaded.isChecked(), cbUnUpload.isChecked());
                    setStuCount(countMap.get("count"), countMap.get("women_count"), countMap.get("man_count"));
                    Logger.i("zzs===>" + countMap.toString());
                }
                if (studentList == null || studentList.size() == 0) {
                    ToastUtils.showShort(R.string.no_more_data);
                    mRefreshView.finishRefreshAndLoad();
                    mRefreshView.setLoadmoreFinished(true);
                    mAdapter.notifyDataSetChanged();
                    return;
                }

                mPageNum++;
                for (int i = 0; i < studentList.size(); i++) {
                    student = studentList.get(i);
                    //是否刷选已测或未测
                    if (cbTested.isChecked() || cbUnTested.isChecked()) {
                        if (cbTested.isChecked()) {
                            mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), 1, displaStuResult(student.getStudentCode()), mCbSelectAll.isChecked()));
                        } else {
                            mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), 0, "-1000", mCbSelectAll.isChecked()));
                        }
                    } else if (cbUploaded.isChecked() || cbUnUpload.isChecked()) {

                        mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), 1, displaStuResult(student.getStudentCode()), mCbSelectAll.isChecked()));
                    }
                }
                mAdapter.notifyDataSetChanged();
                mRefreshView.finishRefreshAndLoad();
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });


    }


    private void setStuCount(Object sumCount, Object womenCount, Object mamCount) {
        txtStuSumNumber.setText(sumCount + "");
        txtStuManNumber.setText(mamCount + "");
        txtStuWomemNumber.setText(womenCount + "");
    }


    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_MESSAGE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(UPDATE_MESSAGE)) {
                    mPageNum = 0;
                    mRefreshView.autoRefresh();
                }
            }
        };
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CheckDeviceOpener.getInstance().destroy();
        unregisterReceiver(receiver);
    }

}
