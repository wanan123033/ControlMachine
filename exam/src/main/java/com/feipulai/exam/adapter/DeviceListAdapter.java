package com.feipulai.exam.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.entity.Student;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pengjf on 2019/4/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class DeviceListAdapter extends BaseQuickAdapter<DeviceDetail, DeviceListAdapter.ViewHolder> {


    private int testCount = 1;

    public DeviceListAdapter(@Nullable List<DeviceDetail> data) {
        super(R.layout.item_device_list, data);
    }

    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }

    @Override
    protected void convert(final ViewHolder helper, final DeviceDetail item) {

        if (item.isDeviceOpen()) {
            helper.swDeviceClose.setChecked(item.isDeviceOpen());
        }
        helper.cbDeviceState.setText(String.format("%d号设备状态", helper.getLayoutPosition() + 1));
        if (item.getStuDevicePair() != null) {
            if (item.getStuDevicePair().getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                helper.cbDeviceState.setChecked(false);
            }

            if (item.getStuDevicePair().getStudent() != null) {
                Student student = item.getStuDevicePair().getStudent();
                helper.txtStuCode.setText(student.getStudentCode());
                helper.txtStuName.setText(student.getStudentName());
            }
        }

        if (testCount == 2) {
            helper.itemTxtTestResult1.setVisibility(View.VISIBLE);
            helper.itemTxtTestResult2.setVisibility(View.INVISIBLE);
        }
        if (testCount == 3) {
            helper.itemTxtTestResult1.setVisibility(View.VISIBLE);
            helper.itemTxtTestResult2.setVisibility(View.VISIBLE);
        }
        helper.addOnClickListener(R.id.txt_skip).addOnClickListener(R.id.txt_start);
        helper.swDeviceClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                helper.swDeviceClose.setChecked(isChecked);
                item.setDeviceOpen(isChecked);
            }
        });

    }

    static class ViewHolder extends BaseViewHolder {
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
        @BindView(R.id.item_txt_test_result)
        TextView itemTxtTestResult;
        @BindView(R.id.item_txt_test_result1)
        TextView itemTxtTestResult1;
        @BindView(R.id.item_txt_test_result2)
        TextView itemTxtTestResult2;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


}
