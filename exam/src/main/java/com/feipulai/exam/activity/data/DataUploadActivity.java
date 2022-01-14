package com.feipulai.exam.activity.data;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.data.adapter.ItemSelectAdapter;
import com.feipulai.exam.activity.data.adapter.ResultDateAdapter;
import com.feipulai.exam.activity.data.adapter.ResultDateStuAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.DataRetrieveBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.HWConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 数据上传
 * Created by zzs on  2021/10/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DataUploadActivity extends BaseTitleActivity {


    @BindView(R.id.sp_select_items)
    Spinner spSelectItems;
    @BindView(R.id.rvResultDate)
    RecyclerView rvResultDate;
    @BindView(R.id.rvStudent)
    RecyclerView rvStudent;
    private List<Item> itemList = new ArrayList<>();
    private Item mCurrentItem;
    private List<String> resultDateList = new ArrayList<>();
    private ResultDateAdapter resultDateAdapter;
    private ResultDateStuAdapter stuAdapter;
    private List<DataRetrieveBean> retrieveBeanList = new ArrayList<>();

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_upload;
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("成绩上传");
    }

    @Override
    protected void initData() {
        setItemInit();
        resultDateList.addAll(DBManager.getInstance().getResultTimeData(getItemCode()));
        resultDateAdapter = new ResultDateAdapter(resultDateList);
        rvResultDate.setLayoutManager(new LinearLayoutManager(this));
        rvResultDate.setAdapter(resultDateAdapter);
        resultDateAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                resultDateAdapter.setSelectPosition(position);
                resultDateAdapter.notifyDataSetChanged();
                getStudentList();
            }
        });

        rvStudent.setLayoutManager(new LinearLayoutManager(this));
        stuAdapter = new ResultDateStuAdapter(retrieveBeanList);
        rvStudent.setAdapter(stuAdapter);
        if (resultDateList.size() != 0) {
            getStudentList();
        }
    }

    private String getItemCode() {
        if (TextUtils.isEmpty(mCurrentItem.getItemCode())) {
            return TestConfigs.DEFAULT_ITEM_CODE;
        } else {
            return mCurrentItem.getItemCode();
        }
    }

    private void setItemInit() {
        int index = 0;
        spSelectItems.setVisibility(View.VISIBLE);
        itemList = DBManager.getInstance().queryItemsByMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            mCurrentItem = itemList.get(0);
        } else {
            for (int i = 0; i < itemList.size(); i++) {
                if (TextUtils.equals(itemList.get(i).getItemCode(), TestConfigs.getCurrentItemCode())) {
                    index = i;
                    mCurrentItem = itemList.get(i);
                }
            }
        }

        spSelectItems.setAdapter(new ItemSelectAdapter(this, itemList));
        spSelectItems.setSelection(index);
    }

    @OnClick({R.id.btnAllUpload, R.id.btnUpload, R.id.btnNotUpload})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnAllUpload:
                uploadResultAll(true);
                break;
            case R.id.btnNotUpload:
                uploadResultAll(false);
                break;
            case R.id.btnUpload:
                List<String> studentCode = new ArrayList<>();

                for (int i = 0; i < retrieveBeanList.size(); i++) {
                    DataRetrieveBean bean = retrieveBeanList.get(i);
                    studentCode.add(bean.getStudentCode());
                }
                if (studentCode.size() == 0) {
                    ToastUtils.showShort("无数据可以上传");
                } else {
                    getStudentData(studentCode);
//                    ServerMessage.baseUploadResult(this, DBManager.getInstance().getUploadResultsByStuCode(mCurrentItem.getItemCode(), studentCode));
//                    //tcp
//                    if (SettingHelper.getSystemSetting().isTCP()) {
//                        ServerMessage.uploadTCPResult(this, DBManager.getInstance().getUploadResultsByStuCode(mCurrentItem.getItemCode(), studentCode));
//                    } else {
//                        ServerMessage.uploadResult(this, DBManager.getInstance().getUploadResultsByStuCode(mCurrentItem.getItemCode(), studentCode));
//
//                    }
                }


                break;
        }
    }

    @OnItemSelected({R.id.sp_select_items})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_select_items:
                mCurrentItem = itemList.get(position);
                resultDateList.clear();
                resultDateList.addAll(DBManager.getInstance().getResultTimeData(getItemCode()));
                resultDateAdapter.setSelectPosition(0);
                resultDateAdapter.notifyDataSetChanged();
                getStudentList();
                break;

        }
    }

    private void getStudentList() {
        if (resultDateList.size() == 0) {
            return;
        }
        DataBaseExecutor.addTask(new DataBaseTask(this, getString(R.string.loading_hint), true) {

            @Override
            public DataBaseRespon executeOper() {

                List<Student> studentList = DBManager.getInstance().getResultTimeDataStudent(getItemCode(), resultDateList.get(resultDateAdapter.getSelectPosition()));
                retrieveBeanList.clear();
                for (int i = 0; i < studentList.size(); i++) {
                    //获取学生信息
                    Student student = studentList.get(i);
                    String result = displaStuResult(student.getStudentCode());
                    retrieveBeanList.add(new DataRetrieveBean(student.getStudentCode(), student.getStudentName(), student.getSex(), student.getPortrait(), TextUtils.equals(result, "-1000") ? 0 : 1, result));
                }
                return new DataBaseRespon(true, "", null);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {

                stuAdapter.notifyDataSetChanged();
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

    private void uploadResultAll(final boolean isUploadAll) {
        DataBaseExecutor.addTask(new DataBaseTask(this, getString(R.string.loading_hint), false) {
            @Override
            public DataBaseRespon executeOper() {
                List<UploadResults> uploadResultsList = new ArrayList<>();
                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                    List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);
                    for (Item item : itemList) {
                        List<UploadResults> dbResultsList = DBManager.getInstance().getUploadResultsAll(isUploadAll, item.getItemCode());
                        if (dbResultsList != null && dbResultsList.size() > 0)
                            uploadResultsList.addAll(dbResultsList);
                    }
                } else {
                    uploadResultsList = DBManager.getInstance().getUploadResultsAll(isUploadAll, TestConfigs.getCurrentItemCode());
                }

                return new DataBaseRespon(true, "", uploadResultsList);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                List<UploadResults> results = (List<UploadResults>) respon.getObject();
                Log.e("UploadResults", "---------" + results.size());
                ServerMessage.baseUploadResult(DataUploadActivity.this, results);

            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }

    private void getStudentData(final List<String> studentCode) {
        DataBaseExecutor.addTask(new DataBaseTask(this, getString(R.string.loading_hint), false) {
            @Override
            public DataBaseRespon executeOper() {


                return new DataBaseRespon(true, "", DBManager.getInstance().getUploadResultsByStuCode(mCurrentItem.getItemCode(), studentCode));
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                List<UploadResults> results = (List<UploadResults>) respon.getObject();
                Log.e("UploadResults", "---------" + results.size());
                ServerMessage.baseUploadResult(DataUploadActivity.this, results);

            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });

    }
}
