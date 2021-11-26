package com.feipulai.exam.activity.jump_rope.base.result;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.adapter.ResultDisplayAdapter;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

public class RadioResultActivity
        extends BaseTitleActivity {

    public static final int BACK_TO_CHECK = 0x02;

    @BindView(R.id.tv_group_name)
    TextView mTvGroupName;
    @BindView(R.id.ll_title)
    LinearLayout mLlTitle;
    @BindView(R.id.rv_results)
    RecyclerView mRvResults;
    @BindView(R.id.btn_print)
    Button btnPrint;
    @BindView(R.id.rv_results_resit)
    RecyclerView rv_results_resit;
    @BindView(R.id.ll_title_resit)
    LinearLayout ll_title_resit;

    private SystemSetting systemSetting;
    private ResultDisplayAdapter mAdapter;
    private boolean needPrint;
    private Map<Student, List<RoundResult>> results;

    private Map<Student,List<RoundResult>> resitResult;
    private Map<Student,List<RoundResult>> nomalResult;
    private ResultDisplayAdapter mAdapter2;


    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("成绩确认");
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_radio_result;
    }

    @Override
    protected void initData() {

        systemSetting = SettingHelper.getSystemSetting();

        results = TestCache.getInstance().getResults();
        initResult();
        if (systemSetting.getTestPattern() == SystemSetting.GROUP_PATTERN) {
            mTvGroupName.setText(InteractUtils.generateGroupText(TestCache.getInstance().getGroup()));
            mTvGroupName.setVisibility(View.VISIBLE);
            updateGroupState();
        }
        for (int i = 0; i < TestConfigs.getMaxTestCount(this); i++) {
            TextView textView = new TextView(this);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            textView.setText("成绩" + (i + 1));
            mLlTitle.addView(textView);
        }
        initAdapter();
        initAdapter2();
        mRvResults.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_results_resit.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(false);
        dividerItemDecoration.setDrawBorderLeftAndRight(false);
        mRvResults.addItemDecoration(dividerItemDecoration);

        DividerItemDecoration dividerItemDecoration2 = new DividerItemDecoration(this);
        dividerItemDecoration2.setDrawBorderTopAndBottom(false);
        dividerItemDecoration2.setDrawBorderLeftAndRight(false);
        rv_results_resit.addItemDecoration(dividerItemDecoration2);

        if (systemSetting.isAutoPrint()) {
            printResult();
        }
//		if (systemSetting.isRtUpload()) {
//			if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
//				ToastUtils.showShort("自动上传成绩需下载更新项目信息");
//			} else {
//				List<UploadResults> uploadResults = new ArrayList<>();
//				for (Student student : TestCache.getInstance().getAllStudents()) {
//					String groupNo;
//					String scheduleNo;
//					String testNo;
//					List<RoundResult> roundResultList = TestCache.getInstance().getResults().get(student);
//					if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN) {
//						Group group = TestCache.getInstance().getGroup();
//						groupNo = group.getGroupNo() + "";
//						scheduleNo = group.getScheduleNo();
//						testNo = "1";
//					} else {
//						StudentItem studentItem = TestCache.getInstance().getStudentItemMap().get(student);
//						scheduleNo = studentItem.getScheduleNo();
//						groupNo = "";
//						testNo = TestCache.getInstance().getTestNoMap().get(student) + "";
//					}
//					UploadResults uploadResult = new UploadResults(scheduleNo,
//							TestConfigs.getCurrentItemCode(), student.getStudentCode()
//							, testNo, groupNo, RoundResultBean.beanCope(roundResultList));
//					uploadResults.add(uploadResult);
//				}
//				Logger.i("自动上传成绩:" + uploadResults.toString());
//				ServerMessage.uploadResult(/*null,*/ uploadResults);
//			}
//		}



    }

    private void initAdapter2() {
        int sum = 0;
        Set<Student> students = resitResult.keySet();
        for (Student student : students){
            List<RoundResult> results = resitResult.get(student);
            if (results.size() > sum){
                sum = results.size();
            }
        }
        for (int i = 0 ; i < sum ; i++){
            TextView textView = new TextView(this);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            textView.setText("成绩" + (i + 1));
            ll_title_resit.addView(textView);
        }
        mAdapter2 = new ResultDisplayAdapter(resitResult, sum);
        rv_results_resit.setAdapter(mAdapter2);
    }

    private void initResult() {
        Log.e("TAG----",results.toString());
        resitResult = new IdentityHashMap<>();
        nomalResult = new IdentityHashMap<>();
        if (results != null){
            Set<Student> students = results.keySet();
            for (Student student : students){
                List<RoundResult> results = this.results.get(student);
                List<RoundResult> resitResultList = new ArrayList<>();
                List<RoundResult> nomalResultList = new ArrayList<>();
                for (RoundResult result : results){
                    if (result.getExamType() == 2){
                        resitResultList.add(result);
                    }else {
                        nomalResultList.add(result);
                    }
                }
                resitResult.put(student,resitResultList);
                nomalResult.put(student,nomalResultList);
            }
        }

    }

    private void updateGroupState() {
        int isTestComplete = Group.FINISHED;
        Group group = TestCache.getInstance().getGroup();
        List<Student> students = DBManager.getInstance().getStudentsByGroup(group);
        if (students != null && students.size() > 0) {
            for (Student student : students) {
                List<RoundResult> resultList = results.get(student);
                if (resultList == null || resultList.size() == 0) {
                    resultList = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
                }
                // 未测 或 测试次数不够
                if (resultList == null || resultList.size() < TestConfigs.getMaxTestCount(this)) {
                    isTestComplete = Group.NOT_FINISHED;
                    break;
                }
            }
            if (isTestComplete != group.getIsTestComplete()) {
                group.setIsTestComplete(isTestComplete);
                DBManager.getInstance().updateGroup(group);
            }
        }
    }

    private void initAdapter() {
        mAdapter = new ResultDisplayAdapter(nomalResult, TestConfigs.getMaxTestCount(this));
        mRvResults.setAdapter(mAdapter);
    }

    @OnClick({R.id.btn_ok, R.id.btn_print})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_ok:
                finish();
                break;
            case R.id.btn_print:
                printResult();
                break;
        }
    }

    @Override
    public void finish() {
        setResult(BACK_TO_CHECK);
        super.finish();
    }

    private void printResult() {
        btnPrint.setEnabled(false);
        TestCache testCache = TestCache.getInstance();
        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN &&
                (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4 ||SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_CUSTOM_APP)) {
            InteractUtils.printA4Result(this, testCache.getGroup());
        } else {
            InteractUtils.printResults(testCache.getGroup(),
                    testCache.getAllStudents(), results,
                    TestConfigs.getMaxTestCount(this),
                    testCache.getTrackNoMap());
        }

        btnPrint.setEnabled(true);
    }
}
