package com.feipulai.exam.activity.data;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.PullToRefreshView;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.CheckDeviceOpener;
import com.feipulai.device.ic.ICCardDealer;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.device.ic.entity.StuInfo;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LoginActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.DataRetrieveAdapter;
import com.feipulai.exam.bean.DataRetrieveBean;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.HWConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.utils.db.DataBaseExecutor;
import com.feipulai.exam.utils.db.DataBaseRespon;
import com.feipulai.exam.utils.db.DataBaseTask;
import com.feipulai.exam.view.StuSearchEditText;
import com.orhanobut.logger.Logger;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
    @BindView(R.id.rb_all)
    RadioButton mRbAll;
    @BindView(R.id.et_input_text)
    EditText mEtInputText;
    @BindView(R.id.btn_query)
    Button mBtnQuery;
    @BindView(R.id.imgbtn_scan)
    ImageButton mImgbtnScan;
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
    //	private Item mCurrentItem;

    private DataRetrieveAdapter mAdapter;
    private List<DataRetrieveBean> mList;
    private List<DataRetrieveBean> printList = new ArrayList<>();
    private static final int LOAD_ITEMS = 100;
    private int mPageNum;
//    private ProgressDialog mProgressDialog;


    public BroadcastReceiver receiver;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_retrieve;
    }

    @Override
    protected void initData() {
        PrinterManager.getInstance().init();
        mRbAll.setChecked(true);
        registerReceiver();
//        SoftInputUtil.disableShowSoftInput(mEtInputText);
        mCbSelectAll.setOnCheckedChangeListener(this);
        cbUploaded.setOnCheckedChangeListener(this);
        cbUnUpload.setOnCheckedChangeListener(this);
        cbUnTested.setOnCheckedChangeListener(this);
        cbTested.setOnCheckedChangeListener(this);
        mRbAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRbAll.isChecked()) {
                    cbUploaded.setChecked(false);
                    cbUnUpload.setChecked(false);
                    cbTested.setChecked(false);
                    cbUnTested.setChecked(false);
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
        return builder.setTitle("数据查询").addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

//    @Override
//    public void onCheckIn(Student student) {
//        mEtInputText.setText(student.getStudentCode());
//        queryData(student.getStudentCode());
//    }


    @Override
    public void onICCardFound(NFCDevice nfcd) {
        ICCardDealer icCardDealer = new ICCardDealer(nfcd);
        StuInfo stuInfo = icCardDealer.IC_ReadStuInfo();

        if (stuInfo == null || TextUtils.isEmpty(stuInfo.getStuCode())) {
            TtsManager.getInstance().speak("读卡(ka3)失败");
            InteractUtils.toast(this, "读卡失败");
            return;
        }

        Logger.i("iccard readInfo:" + stuInfo.toString());
        setUiQueryData(stuInfo.getStuCode());

    }

    @Override
    public void onIdCardRead(IDCardInfo idCardInfo) {
        Student student = DBManager.getInstance().queryStudentByIDCode(idCardInfo.getId());
        if (student == null) {
            InteractUtils.toast(this, "该考生不存在");
        } else {
            setUiQueryData(student.getStudentCode());
        }


    }

    @Override
    public void onQrArrived(String qrCode) {
        setUiQueryData(qrCode);
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

    @Override
    public void onQRWrongLength(int length, int expectLength) {
        InteractUtils.toast(this, "条码与当前设置位数不一致,请重扫条码");
    }

    @OnClick({R.id.btn_query, R.id.imgbtn_scan, R.id.btn_upload})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btn_query:
                String input = mEtInputText.getText().toString().trim();
                mPageNum = 0;
                if (!StuSearchEditText.patternStuCode(input)) {
                    ToastUtils.showShort("请输入正常学生考号或身份证");
                } else {
                    queryData(input);
                }

                break;

            case R.id.btn_upload:
                List<String> studentCode = new ArrayList<>();
//                mProgressDialog = ProgressDialog.show(this, "数据上传中", "数据上传中", true);

                for (int i = 0; i < mList.size(); i++) {
                    DataRetrieveBean bean = mList.get(i);
                    if (bean.isChecked() && bean.getTestState() == 1) {
                        studentCode.add(bean.getStudentCode());
                    }
                }
                if (studentCode.size() == 0) {
//                    mProgressDialog.dismiss();
                    ToastUtils.showShort("未选中数据上传");
                } else {

                    ServerMessage.uploadResult(this, DBManager.getInstance().getUploadResultsByStuCode(studentCode));
                }
                break;
//            case R.id.btn_print:
//                clickPrint();
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


    @OnClick(R.id.btn_print)
    public void onClickPrint() {
        boolean isCheck = false;
//        boolean isTest = false;
        printList.clear();
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).isChecked()) {
                isCheck = true;
                if (mList.get(i).getTestState() == 1) {
                    printList.add(mList.get(i));
                }

            }

        }
        if (isCheck) {
            if (printList.size() == 0) {
                ToastUtils.showShort("选中全部考生都未测试");
                return;
            }
            if (printList.size() > 10) {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("提示")
                        .setMessage("选择打印考生数量过多，确定继续打印？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ToastUtils.showShort("开始打印");
                                for (DataRetrieveBean dataRetrieveBean : printList) {
                                    List<RoundResult> results = DBManager.getInstance().queryResultsByStudentCode(dataRetrieveBean.getStudentCode());
                                    printResult(dataRetrieveBean, results);
                                }
                            }
                        })
                        .setNegativeButton("否", null)
                        .show();
            } else {
                ToastUtils.showShort("开始打印");
                for (DataRetrieveBean dataRetrieveBean : printList) {
                    List<RoundResult> results = DBManager.getInstance().queryResultsByStudentCode(dataRetrieveBean.getStudentCode());
                    printResult(dataRetrieveBean, results);
                }
            }


        } else {
            ToastUtils.showShort("未选中数据打印");
        }
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.TOKEN_ERROR) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void printResult(DataRetrieveBean dataRetrieveBean, List<RoundResult> printResults) {

        Map<Integer, List<RoundResult>> resultMap = new HashMap<>();

        for (RoundResult printResult : printResults) {
            List<RoundResult> roundResultList = resultMap.get(printResult.getExamType());
            if (roundResultList == null) {
                roundResultList = new ArrayList<>();
                roundResultList.add(printResult);
            } else {
                roundResultList.add(printResult);
            }
            resultMap.put(printResult.getExamType(), roundResultList);
        }
        for (Map.Entry<Integer, List<RoundResult>> entity : resultMap.entrySet()) {
            List<RoundResult> printResultList = entity.getValue();


            PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机");
            PrinterManager.getInstance().print("考  号:" + dataRetrieveBean.getStudentCode());
            PrinterManager.getInstance().print("姓  名:" + dataRetrieveBean.getStudentName());
            for (int i = 0; i < TestConfigs.getMaxTestCount(this); i++) {

                if (printResultList.size() > i) {
                    switch (printResultList.get(i).getResultState()) {
                        //0正常  -1000犯规    -2中退    -3放弃
                        case RoundResult.RESULT_STATE_NORMAL:
                            PrinterManager.getInstance().print(String.format("第 %1$d 次：", i + 1) +
                                    ResultDisplayUtils.getStrResultForDisplay(printResultList.get(i).getResult()));
                            break;
                        case RoundResult.RESULT_STATE_FOUL:
                            PrinterManager.getInstance().print(String.format("第 %1$d 次：", i + 1) +
                                    "X");
                            break;
                        case RoundResult.RESULT_STATE_BACK:
                            PrinterManager.getInstance().print(String.format("第 %1$d 次：", i + 1) +
                                    "中退");
                            break;
                        case RoundResult.RESULT_STATE_WAIVE:
                            PrinterManager.getInstance().print(String.format("第 %1$d 次：", i + 1) +
                                    "放弃");
                            break;
                    }
                } else {
                    PrinterManager.getInstance().print(String.format("第 %1$d 次：", i + 1));
                }
            }
            PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()) + "\n");
            PrinterManager.getInstance().print("\n");


        }


    }


    /**
     * 查找所有学生信息
     */
    private void setAllList() {
        DataBaseExecutor.addTask(new DataBaseTask(this, getString(R.string.loading_hint), true) {
            @Override
            public DataBaseRespon executeOper() {
                List<Student> studentList = DBManager.getInstance().getItemStudent(LOAD_ITEMS, LOAD_ITEMS *
                        mPageNum);
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
                    ToastUtils.showShort("没有更多数据了");
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
                    mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), TextUtils.equals(result, "-1000") ? 0 : 1, result));
                }
                mRefreshView.finishRefreshAndLoad();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }

    private String displaStuResult(String studentCode) {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
            List<RoundResult> heightResults = DBManager.getInstance().queryResultsByStudentCode(studentCode, HWConfigs
                    .HEIGHT_ITEM);
            List<RoundResult> weightResults = DBManager.getInstance().queryResultsByStudentCode(studentCode, HWConfigs
                    .WEIGHT_ITEM);

            Collections.sort(heightResults, roundResultComparator);
            Collections.sort(weightResults, roundResultComparator);
            if (heightResults.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(ResultDisplayUtils.getStrResultForDisplay(heightResults.get(0).getResult(), HWConfigs.HEIGHT_ITEM));
                sb.append("/");
                sb.append(ResultDisplayUtils.getStrResultForDisplay(weightResults.get(0).getResult(), HWConfigs.WEIGHT_ITEM));
                return sb.toString();
            }
            return "-1000";
        } else {
            RoundResult result = DBManager.getInstance().queryResultsByStudentCodeIsLastResult(studentCode);
            return result != null ? (result.getResultState() == RoundResult.RESULT_STATE_FOUL ? "X" : result.getResult()) + "" : "-1000";
        }

    }

    private Comparator<RoundResult> roundResultComparator = Collections.reverseOrder(new Comparator<RoundResult>() {
        @Override
        public int compare(RoundResult lhs, RoundResult rhs) {
            return lhs.getTestTime().compareTo(rhs.getTestTime());
        }
    });

    /**
     * 模糊查询学生信息
     *
     * @param inputText
     */
    private void queryData(final String inputText) {
        if (TextUtils.isEmpty(inputText)) {
            ToastUtils.showShort("请输入搜索内容");
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
                        ToastUtils.showShort("该考生不存在");
                        return;
                    }
                    mList.clear();
                } else {
                    if (students == null || students.size() == 0) {
                        ToastUtils.showShort("没有更多数据了");
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
                    mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), TextUtils.equals(result, "-1000") ? 0 : 1, result));

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
                break;
            case R.id.cb_un_tested://未测
                if (isChecked) {
                    cbUnTested.setChecked(true);
                    mRbAll.setChecked(false);
                    cbTested.setChecked(false);
                }
                break;
            case R.id.cb_uploaded://已上传
                if (isChecked) {
                    cbUploaded.setChecked(true);
                    mRbAll.setChecked(false);
                    cbUnUpload.setChecked(false);
                }
                break;
            case R.id.cb_un_upload://未上传
                if (isChecked) {
                    cbUnUpload.setChecked(true);
                    mRbAll.setChecked(false);
                    cbUploaded.setChecked(false);
                }
                break;

        }
        //非选择全部的复选框
        if (buttonView.getId() != R.id.cb_select_all) {
            mPageNum = 0;
            //刷选条件必须至少有一个
            if (cbUploaded.isChecked() || cbUnUpload.isChecked() || cbTested.isChecked() || cbUnTested.isChecked()) {
                //选择未测，证明没有成绩，所有选择已上传与未上传都是空列表
                if (cbUnTested.isChecked() && (cbUploaded.isChecked() || cbUnUpload.isChecked())) {
                    mList.clear();
                } else {
                    chooseStudent();
                }
            } else {
                mRbAll.setChecked(true);
                //查全部
                setAllList();
            }

        }
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
                    ToastUtils.showShort("没有更多数据了");
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
                            mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), 1, displaStuResult(student.getStudentCode())));
                        } else {
                            mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), 0, "-1000"));
                        }
                    } else if (cbUploaded.isChecked() || cbUnUpload.isChecked()) {

                        mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), 1, displaStuResult(student.getStudentCode())));
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

//    @Override
//    public void onRefresh() {
//        // Stop the refreshing indicator
//        mSwiperefresh.setRefreshing(false);
//    }
//
//    @Override
//    public void onLoadMore() {
//        //如果这里设置adapter,会导致每次都自动跳转到第一个item显示
//        //如果这里不设置adapter,那么在SwipeRefreshView中的
//        //mListView.addFooterView(mFooterView)无效,
//        //mListView.removeFooterView(mFooterView)会抛出异常导致奔溃
//        //暂时的解决方案:我们对mFooterView的需求并不高,直接去掉就行,这里直接
//        //notifyDataSetChanged,跳转的问题也解决了
//        //setListAdapter();
//        if (TextUtils.isEmpty(mEtInputText.getText().toString().trim())) {
//
//            if (mRbAll.isChecked()) {
//                setAllList();
//            } else {
//                chooseStudent();
//            }
//        } else {
//            queryData(mEtInputText.getText().toString().trim());
//        }
//        mAdapter.notifyDataSetChanged();
//        Logger.i("loading");
//        // 加载完数据设置为不加载状态,将加载进度收起来
//        mSwiperefresh.setLoading(false);
//    }


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

    public final static String UPDATE_MESSAGE = "com.feipulai.host.update_data_message";


}
