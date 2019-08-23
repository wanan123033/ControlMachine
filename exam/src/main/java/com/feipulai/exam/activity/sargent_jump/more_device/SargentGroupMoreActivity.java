package com.feipulai.exam.activity.sargent_jump.more_device;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.adapter.StuAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.DeviceListAdapter;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class SargentGroupMoreActivity extends BaseCheckActivity {
    @BindView(R.id.txt_group_name)
    TextView txtGroupName;
    @BindView(R.id.rv_device_list)
    RecyclerView rvDeviceList;
    @BindView(R.id.rv_test_stu)
    RecyclerView rvTestStu;
    private LEDManager mLEDManager;
    private List<Student> studentList;
    private Group group;
    private DeviceListAdapter deviceListAdapter;
    private StuAdapter stuAdapter;
    private int MAX_COUNT = 4;
    private List<DeviceDetail> deviceDetails = new ArrayList<>();
    private int deviceCount;
    /**
     * 是否停止测试
     */
    private boolean isStop = true;
    /**
     * 当前测试次数位
     */
    private int roundNo = 1;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_more;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        PrinterManager.getInstance().init();
        group = (Group) TestConfigs.baseGroupMap.get("group");

        initData();
        mLEDManager = new LEDManager();
        mLEDManager.link(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

        rvTestStu.setLayoutManager(new LinearLayoutManager(this));

        //给 界面左侧recyclerView 添加学生
        studentList = new ArrayList<>();
        List<BaseStuPair> pairList = new ArrayList<>();
        pairList.addAll((List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu"));
        for (BaseStuPair pair : pairList) {
            studentList.add(pair.getStudent());
        }
        stuAdapter = new StuAdapter(studentList);
        rvTestStu.setAdapter(stuAdapter);

        StringBuffer sbName = new StringBuffer();
        sbName.append(group.getGroupType() == Group.MALE ? "男子" :
                (group.getGroupType() == Group.FEMALE ? "女子" : "男女混合"));
        sbName.append(group.getSortName() + String.format("第%1$d组", group.getGroupNo()));
        txtGroupName.setText(sbName);

        for (int i = 0; i < MAX_COUNT; i++) {
            DeviceDetail detail = new DeviceDetail();
            detail.getStuDevicePair().getBaseDevice().setDeviceId(i + 1);
            detail.setDeviceOpen(true);
            deviceDetails.add(detail);
        }
        deviceListAdapter = new DeviceListAdapter(deviceDetails);
        deviceListAdapter.setTestCount(3);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        rvDeviceList.setLayoutManager(layoutManager);
        rvDeviceList.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int pos) {
                switch (view.getId()){
                    case R.id.txt_skip://跳过
                        toSkip(pos);
                        break;
                    case R.id.txt_start://开始
                        toStart(pos);
                        break;
                }
            }
        });


        getTestStudent(group);
    }

    private void toStart(int pos) {

    }

    protected  void toSkip(int pos){
        //跳过成绩保存
        if (studentList == null || studentList.size() == 0 || stuAdapter.getTestPosition() == -1) {
            return;
        }
        //是否开启身份验证
        if (SettingHelper.getSystemSetting().isIdentityMark()) {
            //学生在分组中是否有进行检入
            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group
                    , studentList.get(stuAdapter.getTestPosition()).getStudentCode());
            if (groupItem.getIdentityMark() == 0) {
                return;
            }
        }
        Logger.i("studentSkip=>跳过考生：" + studentList.get(stuAdapter.getTestPosition()));

        if (stuAdapter.getTestPosition() == studentList.size() - 1 && roundNo >= setTestCount()){
            //全部次数测试完，
            toastSpeak("当前小组所有人员都测试完");
            return;
        }

        //设置测试学生，当学生有满分跳过则寻找需要测试学生
        if (stuAdapter.getTestPosition() == studentList.size() - 1) {
            if (setTestPattern() == 0) { //连续测试
                //全部次数测试完，
                allTestComplete();
                return;
            } else if (setTestPattern() == 1 && setTestCount() > roundNo) {
                //循环测试到最后一位，当前测试次数小于测试次数则进行下一轮测试
                roundNo++;
                stuAdapter.setTestPosition(0);

                return;
            } else {
                allTestComplete();
            }
        } else {
            if (setTestPattern() == 0) {//连续测试 下一位

            } else {
                stuAdapter.setTestPosition(stuAdapter.getTestPosition() + 1);

            }

            //todo print
        }
    }

    private void allTestComplete() {
        //全部次数测试完，
        toastSpeak("分组考生全部测试完成，请选择下一组");
        roundNo = 1;
        stuAdapter.setTestPosition(-1);
        stuAdapter.notifyDataSetChanged();
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
    }

    /**
     * 获取考生
     *
     * @param group
     */
    private void getTestStudent(Group group) {
        stuAdapter.setTestPosition(-1);
        //身份验证模式
        if (SettingHelper.getSystemSetting().isIdentityMark()) {
            stuAdapter.notifyDataSetChanged();
            return;
        }

        if (setTestPattern() == 0) {//连续模式
            for (int i = 0; i < studentList.size(); i++) {
                //  查询学生成绩 当有成绩则添加数据跳过测试
                List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                        (studentList.get(i).getStudentCode(), group.getId() + "");

                if (roundResultList == null || roundResultList.size() == 0 || roundResultList.size() < setTestCount()) {

                    roundNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;
                    allotStudent(i,roundResultList);

                }
            }
        } else {//循环测试
            for (int i = 0; i < studentList.size(); i++) {
                //  查询学生成绩 当有成绩则添加数据跳过测试
                List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                        (studentList.get(i).getStudentCode(), group.getId() + "");
                if ((roundResultList == null || roundResultList.size() == 0)) {
                    roundNo = 1;
                    allotStudent(i, roundResultList);
                }else if (stuAdapter.getTestPosition()!=-1 && roundResultList.size()< setTestCount() &&
                        roundNo > roundResultList.size()){//查找成绩个数小于当前轮次的学生
                    allotStudent(i, roundResultList);
                }
                roundNo++;
            }

        }

    }

    /**
     *分配考生
     */
    private void allotStudent(int i, List<RoundResult> roundResultList) {
        int index = -1;
        for (DeviceDetail detail : deviceDetails) {
            index++;
            if (detail.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
                detail.getStuDevicePair().setStudent(studentList.get(i));
                detail.getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                setStuPairsData(index, roundResultList);
                rvTestStu.scrollToPosition(i);
                stuAdapter.setTestPosition(i);
                deviceListAdapter.notifyItemChanged(index);
                break;
            }
        }
    }

    /**
     * 设置位置考生已测成绩
     *
     * @param index
     * @param roundResultList
     */
    public void setStuPairsData(int index, List<RoundResult> roundResultList) {
        String[] result = new String[setTestCount()];

        for (int j = 0; j < roundResultList.size(); j++) {
            switch (roundResultList.get(j).getResultState()) {
                case RoundResult.RESULT_STATE_FOUL:
                    result[j] = "X";
                    break;
                case -2:
                    result[j] = "中退";
                    break;
                default:
                    result[j] = ResultDisplayUtils.getStrResultForDisplay(roundResultList.get(j).getResult());
                    break;
            }
        }
        deviceDetails.get(index).getStuDevicePair().setTimeResult(result);
        deviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCheckIn(Student student) {
        Logger.i("onCheckIn====>" + student.toString());
        if (student == null) {
            toastSpeak("该考生不存在");
            return;
        }
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            toastSpeak("无此项目");
            return;
        }

        //是否开启身份验证
        if (SettingHelper.getSystemSetting().isIdentityMark() && studentList.size() > 0) {
            //考生分组测试的成绩
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
            //学生在分组中是否有进行检入
            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, student.getStudentCode());
            if ((roundResultList.size() == 0 || roundResultList.size() < setTestCount()) && groupItem != null) {
                isStop = false;
                roundNo = roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;
                gotoTest(student);
                groupItem.setIdentityMark(1);
                DBManager.getInstance().updateStudentGroupItem(groupItem);
            } else if (groupItem == null) {//没报名
                toastSpeak(student.getSpeakStuName() + "考生没有在选择的分组内，无法测试",
                        student.getStudentName() + "考生没有在选择的分组内，无法测试");
            } else if (roundResultList.size() > 0) {
                toastSpeak(student.getSpeakStuName() + "考生已测试完成",
                        student.getStudentName() + "考生已测试完成");
            }
        }
    }

    /**
     * 定位考生测试
     *
     * @param student
     */
    private void gotoTest(Student student) {
        for (int i = 0; i < deviceDetails.size(); i++) {
            BaseStuPair pair = deviceDetails.get(i).getStuDevicePair();
            if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE
                    && pair.getStudent() == null) {
                pair.setStudent(student);
            }
            studentList.indexOf(pair);
            break;
        }

    }

    /**
     * 设置项目测试次数
     */
    public abstract int setTestCount();

    public abstract void initData();

    /**
     * 设置项目测试次数 0 连续 1 循环
     */
    public abstract int setTestPattern();
}
