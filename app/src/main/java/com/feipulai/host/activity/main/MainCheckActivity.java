package com.feipulai.host.activity.main;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.jump_rope.adapter.CheckPairAdapter;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.jump_rope.check.CheckUtils;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.Student;
import com.feipulai.host.view.StuSearchEditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainCheckActivity extends BaseCheckActivity implements CheckPairAdapter.OnItemClickListener {
    @BindView(R.id.et_select)
    StuSearchEditText etSelect;
    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;
    @BindView(R.id.lv_results)
    ListView lvResults;
    private CheckPairAdapter mAdapter;
    private ArrayList<StuDevicePair> stuDevicePairs;
    private int currentPos;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_main_check;
    }
    @Override
    protected BaseToolbar.Builder setToolbar(BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId());
        } else {
            title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId())
                    + "-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title).addRightText(R.string.item_setting_title, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        stuDevicePairs = (ArrayList<StuDevicePair>) CheckUtils.newPairs(getDeviceSumFormSetting());
        initView(stuDevicePairs);
    }
    public void initView(List pairs) {

        etSelect.setData(lvResults, this);

        mRvPairs.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        mRvPairs.addItemDecoration(dividerItemDecoration);
        mRvPairs.setHasFixedSize(true);
        mRvPairs.setClickable(true);

        mAdapter = new CheckPairAdapter(this, pairs);
        mAdapter.setOnItemClickListener(this);
        mRvPairs.setAdapter(mAdapter);
    }
    @OnClick({R.id.tv_del,R.id.tv_test})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_test:
                joinTest();
                break;
            case R.id.tv_del:
                allDelStudent();
                break;

        }
    }

    private void joinTest() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("stuList",stuDevicePairs);
        IntentUtil.gotoActivity(MainCheckActivity.this, TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode()),bundle);
    }

    private void allDelStudent() {
        for (int i = 0 ; i < stuDevicePairs.size() ; i++){
            stuDevicePairs.get(i).setStudent(null);
        }
        mAdapter.notifyDataSetChanged();
        select(0);
    }

    private int getDeviceSumFormSetting() {
        return 15;
    }

    @Override
    public void onCheckIn(Student student) {
        for (int i = 0 ; i < stuDevicePairs.size() ; i++){
            Student student1 = stuDevicePairs.get(i).getStudent();
            if (student1 != null && student1.getStudentCode().equals(student.getStudentCode())){
                toastSpeak("已检录");
                return;
            }
        }
        stuDevicePairs.get(currentPos).setStudent(student);
        mAdapter.notifyItemChanged(currentPos);
        select(currentPos + 1);
    }

    @Override
    public void onItemClick(View view, int position) {
        select(position);

    }
    public void select(int position) {
        int oldPosition = mAdapter.getSelected();
        mAdapter.setSelected(position);
        mAdapter.notifyItemChanged(oldPosition);
        mAdapter.notifyItemChanged(position);
        currentPos = position;
    }
}
