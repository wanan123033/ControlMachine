package com.feipulai.exam.activity.sargent_jump.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pengjf on 2019/4/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class DeviceListAdapter extends BaseMultiItemQuickAdapter<DeviceDetail, BaseViewHolder> {

    private int testCount = 1;
    private boolean isNextClickStart = true;
    private boolean isGroup;

    public void setNextClickStart(boolean nextClickStart) {
        isNextClickStart = nextClickStart;
    }

    public DeviceListAdapter(@Nullable List<DeviceDetail> data) {
        super(data);
        addItemType(DeviceDetail.ITEM_ONE, R.layout.item_device_one_list);
        addItemType(DeviceDetail.ITEM_MORE, R.layout.item_device_list);
    }

    public DeviceListAdapter(@Nullable List<DeviceDetail> data, boolean isGroup) {
        super(data);
        this.isGroup = isGroup;
        addItemType(DeviceDetail.ITEM_ONE, R.layout.item_device_one_list);
        if (isGroup) {
            addItemType(DeviceDetail.ITEM_MORE, R.layout.item_group_device_list);
        } else {
            addItemType(DeviceDetail.ITEM_MORE, R.layout.item_device_list);
        }

    }

    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }

    @Override
    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        if (viewType == DeviceDetail.ITEM_ONE)
            return new OneViewHolder(getItemView(R.layout.item_device_one_list, parent));
        else {
            return new MoreViewHolder(getItemView(isGroup ? R.layout.item_group_device_list : R.layout.item_device_list, parent));
        }
//        return super.onCreateDefViewHolder(parent, viewType);
    }

    @Override
    protected void convert(final BaseViewHolder holder, final DeviceDetail item) {
        switch (holder.getItemViewType()) {
            case DeviceDetail.ITEM_MORE:
                final MoreViewHolder moreHelper = (MoreViewHolder) holder;
                if (item.isDeviceOpen()) {
                    moreHelper.swDeviceClose.setChecked(item.isDeviceOpen());
                }
                moreHelper.cbDeviceState.setText(String.format("%d号设备状态", moreHelper.getLayoutPosition() + 1));

                if (item.getStuDevicePair().getTimeResult() != null) {
                    moreHelper.itemTxtTestResult.setText(item.getStuDevicePair().getTimeResult()[0]);
                }
                if (testCount == 1) {
                    moreHelper.itemTxtTestResult.setVisibility(View.VISIBLE);
                    moreHelper.itemTxtTestResult1.setVisibility(View.INVISIBLE);
                    moreHelper.itemTxtTestResult2.setVisibility(View.INVISIBLE);
                } else if (testCount == 2) {
                    moreHelper.itemTxtTestResult1.setVisibility(View.VISIBLE);
                    moreHelper.itemTxtTestResult2.setVisibility(View.INVISIBLE);
                    moreHelper.itemTxtTestResult1.setText(item.getStuDevicePair().getTimeResult()[1]);
                } else if (testCount == 3) {
                    moreHelper.itemTxtTestResult1.setVisibility(View.VISIBLE);
                    moreHelper.itemTxtTestResult2.setVisibility(View.VISIBLE);
                    moreHelper.itemTxtTestResult1.setText(item.getStuDevicePair().getTimeResult()[1]);
                    moreHelper.itemTxtTestResult2.setText(item.getStuDevicePair().getTimeResult()[2]);

                }

                int state = item.getStuDevicePair().getBaseDevice().getState();
                if (state == BaseDeviceState.STATE_FREE || state == BaseDeviceState.STATE_NOT_BEGAIN) {
                    moreHelper.txtStart.setEnabled(true);
                    moreHelper.txtStart.setBackgroundResource(R.drawable.btn_click_bg_selected);
                    moreHelper.txtStart.setTextColor(Color.WHITE);
                } else {
                    moreHelper.txtStart.setEnabled(false);
                    moreHelper.txtStart.setBackgroundResource(R.drawable.btn_click_bg_unselected);
                    moreHelper.txtStart.setTextColor(Color.BLUE);
                }

                moreHelper.swDeviceClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        moreHelper.swDeviceClose.setChecked(isChecked);
                        item.setDeviceOpen(isChecked);
                    }
                });
                if (item.isPunish() && item.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_END) {
                    moreHelper.txtPunish.setVisibility(View.VISIBLE);
                } else {
                    moreHelper.txtPunish.setVisibility(View.GONE);
                }
                moreHelper.txtConfirm.setVisibility(item.isConfirmVisible() ? View.VISIBLE : View.GONE);
                moreHelper.txtStart.setVisibility(item.isConfirmVisible() ? View.GONE : View.VISIBLE);
                moreHelper.txtStart.setVisibility(item.isConfirmVisible() ? View.GONE : View.VISIBLE);

                moreHelper.addOnClickListener(R.id.txt_skip).addOnClickListener(R.id.txt_start);
                moreHelper.addOnClickListener(R.id.txt_punish);
                if (item.getStuDevicePair() != null) {
                    if (item.getStuDevicePair().getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                        moreHelper.cbDeviceState.setChecked(true);
                    } else {
                        moreHelper.cbDeviceState.setChecked(false);
                    }

                    if (item.getStuDevicePair().getStudent() != null) {
                        Student student = item.getStuDevicePair().getStudent();
                        moreHelper.txtStuCode.setText(student.getStudentCode());
                        moreHelper.txtStuName.setText(student.getStudentName());
                    } else {
                        moreHelper.txtStuCode.setText("");
                        moreHelper.txtStuName.setText("");
                    }
                }

                break;

            case DeviceDetail.ITEM_ONE:
                OneViewHolder oneViewHolder = (OneViewHolder) holder;
                if (isGroup) {
                    oneViewHolder.llStuDetail.setVisibility(View.GONE);
                } else {
                    oneViewHolder.llStuDetail.setVisibility(View.VISIBLE);
                }
                if (item.getStuDevicePair().getStudent() != null) {
                    oneViewHolder.setText(R.id.txt_stu_sex, item.getStuDevicePair().getStudent().getSex() == 0 ? "男" : "女");
                } else {
                    oneViewHolder.setText(R.id.txt_stu_sex, "");
                }
//                if (item.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_END) {
                if (item.getStuDevicePair().getResult() == -999) {
                    oneViewHolder.setText(R.id.txt_test_result, "");
                } else if (item.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_END
                        || item.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_ONUSE) {
                    oneViewHolder.setText(R.id.txt_test_result, item.getStuDevicePair().getResultState() == RoundResult.RESULT_STATE_FOUL ?
                            "X" : ResultDisplayUtils.getStrResultForDisplay(item.getStuDevicePair().getResult()));
                }

//                }
//                   else if (item.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN
//                        || item.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
//                    oneViewHolder.setText(R.id.txt_test_result, "");
//                }

                if (item.getStuDevicePair().getTimeResult() != null) {
                    oneViewHolder.itemTxtTestResult.setText(item.getStuDevicePair().getTimeResult()[0]);
                }
                if (testCount == 1) {
                    oneViewHolder.getView(R.id.view_content1).setVisibility(View.VISIBLE);
                    oneViewHolder.getView(R.id.view_content2).setVisibility(View.INVISIBLE);
                    oneViewHolder.getView(R.id.view_content3).setVisibility(View.INVISIBLE);
                } else if (testCount == 2) {
                    oneViewHolder.getView(R.id.view_content1).setVisibility(View.VISIBLE);
                    oneViewHolder.getView(R.id.view_content2).setVisibility(View.VISIBLE);
                    oneViewHolder.getView(R.id.view_content3).setVisibility(View.INVISIBLE);
                    oneViewHolder.itemTxtTestResult1.setText(item.getStuDevicePair().getTimeResult()[1]);
                } else if (testCount == 3) {
                    oneViewHolder.getView(R.id.view_content1).setVisibility(View.VISIBLE);
                    oneViewHolder.getView(R.id.view_content2).setVisibility(View.VISIBLE);
                    oneViewHolder.getView(R.id.view_content3).setVisibility(View.VISIBLE);
                    oneViewHolder.itemTxtTestResult1.setText(item.getStuDevicePair().getTimeResult()[1]);
                    oneViewHolder.itemTxtTestResult2.setText(item.getStuDevicePair().getTimeResult()[2]);
                }

                oneViewHolder.txtStart.setVisibility(item.isConfirmVisible() ? View.GONE : View.VISIBLE);

                if (!isNextClickStart) {
                    oneViewHolder.txtStart.setVisibility(View.GONE);
                }
                if (item.isPunish()) {
                    oneViewHolder.txtPunish.setVisibility(View.VISIBLE);
                } else {
                    oneViewHolder.txtPunish.setVisibility(View.GONE);
                }
                oneViewHolder.addOnClickListener(R.id.txt_skip).addOnClickListener(R.id.txt_start);
                oneViewHolder.addOnClickListener(R.id.txt_punish);
                if (item.getStuDevicePair() != null) {
                    if (item.getStuDevicePair().getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                        oneViewHolder.cbDeviceState.setChecked(true);
                    } else {
                        oneViewHolder.cbDeviceState.setChecked(false);
                    }

                    if (item.getStuDevicePair().getStudent() != null) {
                        Student student = item.getStuDevicePair().getStudent();
                        oneViewHolder.txtStuCode.setText(student.getStudentCode());
                        oneViewHolder.txtStuName.setText(student.getStudentName());
                    } else {
                        oneViewHolder.txtStuCode.setText("");
                        oneViewHolder.txtStuName.setText("");
                    }
                }
                break;
        }

    }


    static class MoreViewHolder extends BaseViewHolder {
        @BindView(R.id.cb_device_state)
        CheckBox cbDeviceState;
        @BindView(R.id.sw_device_close)
        CheckBox swDeviceClose;
        @BindView(R.id.txt_stu_name)
        TextView txtStuName;
        @BindView(R.id.txt_stu_code)
        TextView txtStuCode;
        @BindView(R.id.txt_skip)
        TextView txtSkip;
        @BindView(R.id.txt_start)
        TextView txtStart;
        @BindView(R.id.txt_confirm)
        TextView txtConfirm;
        @BindView(R.id.txt_punish)
        TextView txtPunish;
        @BindView(R.id.item_txt_test_result)
        TextView itemTxtTestResult;
        @BindView(R.id.item_txt_test_result1)
        TextView itemTxtTestResult1;
        @BindView(R.id.item_txt_test_result2)
        TextView itemTxtTestResult2;


        public MoreViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    static class OneViewHolder extends BaseViewHolder {
        @BindView(R.id.ll_stu_detail)
        LinearLayout llStuDetail;

        @BindView(R.id.cb_device_state)
        CheckBox cbDeviceState;
        @BindView(R.id.txt_stu_name)
        TextView txtStuName;
        @BindView(R.id.txt_stu_code)
        TextView txtStuCode;
        @BindView(R.id.txt_skip)
        TextView txtSkip;
        @BindView(R.id.txt_start)
        TextView txtStart;
        @BindView(R.id.txt_punish)
        TextView txtPunish;
        @BindView(R.id.item_txt_test_result)
        TextView itemTxtTestResult;
        @BindView(R.id.item_txt_test_result1)
        TextView itemTxtTestResult1;
        @BindView(R.id.item_txt_test_result2)
        TextView itemTxtTestResult2;


        public OneViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
