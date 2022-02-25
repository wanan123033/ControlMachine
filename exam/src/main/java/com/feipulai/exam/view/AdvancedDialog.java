package com.feipulai.exam.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.AdvancedStuAdapter;
import com.feipulai.exam.adapter.ScoreAdapter;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 放弃列表
 */
public class AdvancedDialog extends DialogFragment {

    @BindView(R.id.tvCount)
    TextView tvCount;
    @BindView(R.id.rv_stu)
    RecyclerView rv_stu;
   private List<BaseStuPair> pairList ;
    private long groupId = -1;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_abandon_list, container);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCount.setText("存在("+pairList.size()+")未测试考生,是否确认成绩上传");
        rv_stu.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_stu.setAdapter(new AdvancedStuAdapter(pairList));
    }

    public void setArguments(List<BaseStuPair> pairList) {
       this. pairList=pairList;
    }
    public void setArguments(List<BaseStuPair> pairList,long groupId) {
        this. pairList=pairList;
        this.groupId=groupId;
    }
    @OnClick({R.id.tv_cancel, R.id.tv_commit})
    public void onViewClick(View view) {
        switch (view.getId()){
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_commit:
                for (BaseStuPair pair : pairList) {
                    for (int i = 0; i < TestConfigs.getMaxTestCount(); i++) {
                        RoundResult roundResult = new RoundResult();
                        SystemSetting systemSetting = SettingHelper.getSystemSetting();
                        if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
                            StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(pair.getStudent().getStudentCode());
                            roundResult.setExamType(studentItem.getExamType());
                            roundResult.setScheduleNo(studentItem.getScheduleNo());
                        } else {
                            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(TestConfigs.getCurrentItemCode(), pair.getStudent().getStudentCode());
                            roundResult.setExamType(groupItem.getExamType());
                            roundResult.setScheduleNo(groupItem.getScheduleNo());
                        }
                        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                        roundResult.setStudentCode(pair.getStudent().getStudentCode());
                        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
                        roundResult.setMachineResult(0);
                        roundResult.setTestNo(1);
                        roundResult.setUpdateState(0);
                        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
                        if (groupId != -1) {
                            roundResult.setGroupId(groupId);
                        }
                        roundResult.setRoundNo(i + 1);
                        roundResult.setIsLastResult(0);
                        roundResult.setUpdateState(0);
                        roundResult.setResult(0);
                        roundResult.setResultState(RoundResult.RESULT_STATE_WAIVE);
                        roundResult.setTestTime(System.currentTimeMillis() + "");
                        roundResult.setEndTime(System.currentTimeMillis() + "");
                        if (roundResult.getRoundNo() == 1) {
                            roundResult.setIsLastResult(1);
                        }
                        DBManager.getInstance().insertRoundResult(roundResult);
                    }
                }
                EventBus.getDefault().post(new BaseEvent(EventConfigs.AUTO_ADD_RESULT));
                dismiss();
                break;
        }
    }
}
