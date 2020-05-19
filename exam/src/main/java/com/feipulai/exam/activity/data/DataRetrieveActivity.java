package com.feipulai.exam.activity.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
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
import com.feipulai.exam.activity.data.adapter.DataRetrieveAdapter;
import com.feipulai.exam.activity.data.adapter.ItemSelectAdapter;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.DataRetrieveBean;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.HWConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.PrintResultUtil;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.StuSearchEditText;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.examlogger.LogUtils;
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
import butterknife.OnItemSelected;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 数据查询
 * Created by james on 2018/1/2.
 */
public class DataRetrieveActivity extends BaseTitleActivity
        implements CompoundButton.OnCheckedChangeListener,
        OnRefreshLoadmoreListener, CheckDeviceOpener.OnCheckDeviceArrived {

    public static final String DATA_EXTRA = "data_extra";
    public static final String DATA_ITEM_CODE = "itemCode";
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
    @BindView(R.id.txt_sp_title)
    TextView txtSpTitle;
    @BindView(R.id.sp_select_items)
    Spinner spSelectItems;
    private List<Item> itemList;
    private DataRetrieveAdapter mAdapter;
    private List<DataRetrieveBean> mList;
    private List<String> printList = new ArrayList<>();
    private static final int LOAD_ITEMS = 100;
    private int mPageNum;
    public BroadcastReceiver receiver;
    private Item mCurrentItem;
    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            return false;
        }
    });

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_retrieve;
    }

    @Override
    protected void initData() {
        mCurrentItem = TestConfigs.sCurrentItem;
        PrinterManager.getInstance().init();
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
        setMoreItemInit();
        //查全部
        setAllList();

    }


    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("数据查询");
    }


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
                    //tcp
                    if (SettingHelper.getSystemSetting().isTCP()) {
                        ServerMessage.uploadTCPResult(this, DBManager.getInstance().getUploadResultsByStuCode(getItemCode(), studentCode));
                    } else {
                        if (mCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                            ServerMessage.uploadZCPResult(this, mCurrentItem.getItemName(), DBManager.getInstance().getUploadResultsByStuCode(getItemCode(), studentCode));
                        } else {
                            ServerMessage.uploadResult(this, DBManager.getInstance().getUploadResultsByStuCode(getItemCode(), studentCode));
                        }
                    }
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
        LogUtils.life("DataRetrieveActivity onResume");
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
        LogUtils.life("DataRetrieveActivity onPause");
        CheckDeviceOpener.getInstance().close();
    }


    @OnClick({R.id.btn_print})
    public void onClickPrint() {
        boolean isCheck = false;
//        boolean isTest = false;
        printList.clear();
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).isChecked()) {
                isCheck = true;
                if (mList.get(i).getTestState() == 1) {
                    printList.add(mList.get(i).getStudentCode());
                }

            }

        }
        if (isCheck) {
            if (printList.size() == 0) {
                ToastUtils.showShort("选中全部考生都未测试");
                return;
            }
            if (printList.size() > 10) {
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                        .setContentText(getString(R.string.print_more_hint))
                        .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        ToastUtils.showShort("开始打印");
                        PrintResultUtil.printResult(printList);
                    }
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                }).show();

            } else {
                ToastUtils.showShort("开始打印");
                PrintResultUtil.printResult(printList);
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

    @OnItemSelected({R.id.sp_select_items})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_select_items:
                mCurrentItem = itemList.get(position);
                mPageNum = 0;
                //刷选条件必须至少有一个
                if (cbUploaded.isChecked() || cbUnUpload.isChecked() || cbTested.isChecked() || cbUnTested.isChecked()) {
                    //选择未测，证明没有成绩，所有选择已上传与未上传都是空列表
                    if (cbUnTested.isChecked() && (cbUploaded.isChecked() || cbUnUpload.isChecked())) {
                        mList.clear();
                        mAdapter.notifyDataSetChanged();
                    } else {
                        chooseStudent();
                    }
                } else if (!TextUtils.isEmpty(mEtInputText.getText().toString())) {
                    queryData(mEtInputText.getText().toString());
                } else {
                    mRbAll.setChecked(true);
                    //查全部
                    setAllList();
                }
                break;
        }
    }

    private void setMoreItemInit() {
        switch (TestConfigs.sCurrentItem.getMachineCode()) {
            case ItemDefault.CODE_ZCP:
                txtSpTitle.setVisibility(View.VISIBLE);
                spSelectItems.setVisibility(View.VISIBLE);

                itemList = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);
                mCurrentItem = itemList.get(0);
                spSelectItems.setAdapter(new ItemSelectAdapter(this, itemList));
                break;
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
//            PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()) + "\n");
            PrinterManager.getInstance().print("\n");


        }


    }

    private String getItemCode() {
//        if (mCurrentItem.getItemCode() == null){
//            int machineCode = mCurrentItem.getMachineCode();
//            if (machineCode == ItemDefault.CODE_ZFP){
//                List<Item> items = DBManager.getInstance().queryItemsByMachineCode(18);
//                mCurrentItem.setItemCode(items.get(0).getItemCode());
//                return items.get(0).getItemCode();
//            }else {
//                return mCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : mCurrentItem.getItemCode();
//            }
//        }else {
//            return mCurrentItem.getItemCode();
//        }
        return mCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : mCurrentItem.getItemCode();
    }


    /**
     * 查找所有学生信息
     */
    private void setAllList() {
        DBManager.getInstance().queryItemByCode(mCurrentItem.getMachineCode() + "");
        DataBaseExecutor.addTask(new DataBaseTask(this, getString(R.string.loading_hint), true) {
            @Override
            public DataBaseRespon executeOper() {
                List<Student> studentList = DBManager.getInstance().getItemStudent(getItemCode(), LOAD_ITEMS, LOAD_ITEMS *
                        mPageNum);
                return new DataBaseRespon(true, "", studentList);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                Student student;
                List<Student> studentList = (List<Student>) respon.getObject();
                if (mPageNum == 0) {
                    mList.clear();
                    Map<String, Object> countMap = DBManager.getInstance().getItemStudenCount(getItemCode());
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
                    mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), student.getPortrait(), TextUtils.equals(result, "-1000") ? 0 : 1, result, mCbSelectAll.isChecked()));
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
            RoundResult result = DBManager.getInstance().queryResultsByStudentCodeIsLastResult(getItemCode(), studentCode);
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
                List<Student> students = DBManager.getInstance().fuzzyQueryByStuCode(getItemCode(), inputText, LOAD_ITEMS, LOAD_ITEMS *
                        mPageNum);
                return new DataBaseRespon(true, "", students);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                List<Student> students = (List<Student>) respon.getObject();
                if (mPageNum == 0) {
                    if (students == null || students.size() == 0) {
                        ToastUtils.showShort("该考生不存在");
                        mList.clear();
                        mAdapter.notifyDataSetChanged();
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
                    mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), student.getPortrait(), TextUtils.equals(result, "-1000") ? 0 : 1, result, mCbSelectAll.isChecked()));

                }
                mRefreshView.finishRefreshAndLoad();
                mAdapter.notifyDataSetChanged();
                Map<String, Object> countMap = DBManager.getInstance().fuzzyQueryByStuCodeCount(getItemCode(), inputText);
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
                intent.putExtra(DATA_ITEM_CODE, getItemCode());
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
//            if (isChecked) {
            chooseStudent();
//            }

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

    private void chooseStudent() {
        DataBaseExecutor.addTask(new DataBaseTask(this, getString(R.string.loading_hint), true) {
            @Override
            public DataBaseRespon executeOper() {
                List<Student> studentList = DBManager.getInstance().getChooseStudentList(getItemCode(), cbTested.isChecked(),
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
                    Map<String, Object> countMap = DBManager.getInstance().getChooseStudentCount(getItemCode(), cbTested.isChecked(), cbUnTested.isChecked(),
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
                            mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), student.getPortrait(), 1, displaStuResult(student.getStudentCode()), mCbSelectAll.isChecked()));
                        } else {
                            mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), student.getPortrait(), 0, "-1000", mCbSelectAll.isChecked()));
                        }
                    } else if (cbUploaded.isChecked() || cbUnUpload.isChecked()) {

                        mList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), student.getPortrait(), 1, displaStuResult(student.getStudentCode()), mCbSelectAll.isChecked()));
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
        LogUtils.life("DataRetrieveActivity onDestroy");
        CheckDeviceOpener.getInstance().destroy();
        unregisterReceiver(receiver);
    }

    public final static String UPDATE_MESSAGE = "com.feipulai.host.update_data_message";


}
