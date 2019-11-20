package com.feipulai.exam.activity.volleyball.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.entity.Student;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceListAdapter extends BaseQuickAdapter<DeviceDetail, DeviceListAdapter.ViewHolder> {
    private int setting;
    private VolleyBallSetting setting1;

    public DeviceListAdapter(@Nullable List<DeviceDetail> data) {
        super(R.layout.item_device_list_volleyball, data);
        setting1 = SharedPrefsUtil.loadFormSource(mContext, VolleyBallSetting.class);
        if (setting1 != null) {
            this.setting = setting1.getTestTime();
        }
    }

    private int testCount = 1;

    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }

    @Override
    protected void convert(final ViewHolder helper, final DeviceDetail item) {
        helper.swDeviceClose.setChecked(item.isDeviceOpen());
        int state = item.getStuDevicePair().getBaseDevice().getState();
        helper.cbDeviceState.setText((helper.getAdapterPosition() + 1) + "号设备状态");
        helper.cbDeviceState.setChecked(state != BaseDeviceState.STATE_ERROR);
        if (item.getStuDevicePair().getStudent() != null) {
            Student student = item.getStuDevicePair().getStudent();
            helper.txtStuCode.setText(student.getStudentCode());
            helper.txtStuName.setText(student.getStudentName());
            if (state != BaseDeviceState.STATE_ERROR) {
                helper.prepView(true, false, false, setting, setting1.isPenalize());
            }
        } else {
            helper.txtStuCode.setText("");
            helper.txtStuName.setText("");
            if (state != BaseDeviceState.STATE_ERROR) {
                helper.prepView(false, false, false, setting, setting1.isPenalize());
            }
        }
        if (item.getStuDevicePair().getTimeResult() != null) {
            helper.itemTxtTestResult.setText(getShow(1, item.getStuDevicePair().getTimeResult()));
//            helper.itemTxtTestResult.setBackgroundColor(Color.BLACK);
        }

        if (testCount >= 2) {
            helper.itemTxtTestResult1.setVisibility(View.VISIBLE);
//            helper.itemTxtTestResult1.setBackgroundColor(Color.BLACK);
            helper.itemTxtTestResult2.setVisibility(View.VISIBLE);
            helper.itemTxtTestResult1.setText(getShow(2, item.getStuDevicePair().getTimeResult()));
        }
        if (testCount >= 3) {
            helper.itemTxtTestResult2.setVisibility(View.VISIBLE);
//            helper.itemTxtTestResult2.setBackgroundColor(Color.BLACK);
            helper.itemTxtTestResult2.setText(getShow(3, item.getStuDevicePair().getTimeResult()));
        }

        if (testCount == 1) {
            helper.itemTxtTestResult.setVisibility(View.VISIBLE);
            helper.itemTxtTestResult1.setVisibility(View.GONE);
            helper.itemTxtTestResult2.setVisibility(View.GONE);
        } else if (testCount == 2) {
            helper.itemTxtTestResult.setVisibility(View.VISIBLE);
            helper.itemTxtTestResult1.setVisibility(View.VISIBLE);
            helper.itemTxtTestResult2.setVisibility(View.GONE);
        } else if (testCount == 3) {
            helper.itemTxtTestResult.setVisibility(View.VISIBLE);
            helper.itemTxtTestResult1.setVisibility(View.VISIBLE);
            helper.itemTxtTestResult2.setVisibility(View.VISIBLE);
        }

        if (item.getStuDevicePair().getStudent() != null) {
            if (item.getStuDevicePair().getRoundNo() == 0) {
                helper.itemTxtTestResult.setBackgroundColor(Color.GREEN);
                helper.itemTxtTestResult1.setBackgroundColor(Color.BLACK);
                helper.itemTxtTestResult2.setBackgroundColor(Color.BLACK);
            } else if (item.getStuDevicePair().getRoundNo() == 1) {
                helper.itemTxtTestResult.setBackgroundColor(Color.BLACK);
                helper.itemTxtTestResult1.setBackgroundColor(Color.GREEN);
                helper.itemTxtTestResult2.setBackgroundColor(Color.BLACK);
            } else if (item.getStuDevicePair().getRoundNo() == 2) {
                helper.itemTxtTestResult.setBackgroundColor(Color.BLACK);
                helper.itemTxtTestResult1.setBackgroundColor(Color.BLACK);
                helper.itemTxtTestResult2.setBackgroundColor(Color.GREEN);
            }
        } else {
            helper.itemTxtTestResult.setBackgroundColor(Color.BLACK);
            helper.itemTxtTestResult1.setBackgroundColor(Color.BLACK);
            helper.itemTxtTestResult2.setBackgroundColor(Color.BLACK);
        }

        helper.item_txt_state.setText("");
        if (item.getStuDevicePair().getStudent() != null) {
            Log.e("TAG", "deviceId=" + item.getStuDevicePair().getBaseDevice().getDeviceId() + ",state=" + state);
            if (state != BaseDeviceState.STATE_ERROR) {
                if (state == BaseDeviceState.STATE_FREE || state == BaseDeviceState.STATE_NOT_BEGAIN) {
                    helper.prepView(true, false, false, setting, setting1.isPenalize());
                    helper.item_txt_state.setText("设备空闲");
                } else if (state == BaseDeviceState.STATE_ONUSE) {
                    Log.e("TAG", state + "," + item.getTime());
                    if (item.getTime() >= 0) {
                        helper.prepView(false, true, false, setting, setting1.isPenalize());
                        helper.item_txt_state.setText("倒计时:" + item.getTime() + "秒");
                    } else {
                        helper.item_txt_state.setText("");
                        helper.prepView(false, true, false, setting, setting1.isPenalize());
                    }
                } else if (state == BaseDeviceState.STATE_END) {
                    helper.prepView(false, false, true, setting, setting1.isPenalize());
                    helper.item_txt_state.setText("测试结束");
                } else if (state == BaseDeviceState.STATE_PRE_TIME) {
                    if (item.getTime() > 0) {
                        helper.prepView(false, true, false, setting, setting1.isPenalize());
                        helper.item_txt_state.setText(item.getTime() + "");
                    } else {
                        helper.item_txt_state.setText("");
                        helper.prepView(false, true, false, setting, setting1.isPenalize());
                    }
                }
            }
        }
        helper.swDeviceClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                helper.swDeviceClose.setChecked(isChecked);
                item.setDeviceOpen(isChecked);


            }
        });

        helper.addOnClickListener(R.id.txt_start)
                .addOnClickListener(R.id.txt_end)
                .addOnClickListener(R.id.txt_time)
                .addOnClickListener(R.id.txt_gave_up)
                .addOnClickListener(R.id.txt_confirm)
                .addOnClickListener(R.id.txt_js)
                .addOnClickListener(R.id.txt_fq)
                .addOnClickListener(R.id.txt_penalty);
    }

    public String getShow(int num, String[] result) {
        Log.e("getShow", "----" + Arrays.toString(result));
        if (result == null) {
            return "";
        } else {
            return result[num - 1];
        }
    }

    public static class ViewHolder extends BaseViewHolder {

        @BindView(R.id.txt_start)
        TextView txt_start;
        @BindView(R.id.txt_end)
        TextView txt_end;
        @BindView(R.id.txt_time)
        TextView txt_time;
        @BindView(R.id.txt_gave_up)
        TextView txt_gave_up;
        @BindView(R.id.txt_confirm)
        TextView txt_confirm;
        @BindView(R.id.txt_penalty)
        TextView txt_penalty;

        @BindView(R.id.txt_stu_name)
        TextView txtStuName;
        @BindView(R.id.txt_stu_code)
        TextView txtStuCode;
        @BindView(R.id.txt_js)
        TextView txt_js;
        @BindView(R.id.txt_fq)
        TextView txt_fq;

        @BindView(R.id.item_txt_test_result)
        TextView itemTxtTestResult;
        @BindView(R.id.item_txt_test_result1)
        TextView itemTxtTestResult1;
        @BindView(R.id.item_txt_test_result2)
        TextView itemTxtTestResult2;

        @BindView(R.id.sw_device_close)
        CheckBox swDeviceClose;
        @BindView(R.id.cb_device_state)
        CheckBox cbDeviceState;

        @BindView(R.id.rl_1)
        RelativeLayout rl_1;
        @BindView(R.id.rl_2)
        RelativeLayout rl_2;
        @BindView(R.id.rl_3)
        RelativeLayout rl_3;
        @BindView(R.id.rl_4)
        RelativeLayout rl_4;

        @BindView(R.id.item_txt_state)
        TextView item_txt_state;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void prepView(boolean flag1, boolean flag2, boolean flag3, int setting, boolean penalize) {
            txt_start.setVisibility(flag1 ? View.VISIBLE : View.GONE);
            txt_end.setVisibility(flag1 ? View.VISIBLE : View.GONE);
            rl_1.setVisibility(flag1 ? View.VISIBLE : View.GONE);

            txt_time.setVisibility(flag2 && setting > 0 ? View.GONE : View.GONE);
            txt_gave_up.setVisibility(flag2 && setting > 0 ? View.VISIBLE : View.GONE);
            rl_2.setVisibility(flag2 && setting > 0 ? View.VISIBLE : View.GONE);

            rl_4.setVisibility(flag2 && setting == 0 ? View.VISIBLE : View.GONE);
            txt_js.setVisibility(flag2 && setting == 0 ? View.VISIBLE : View.GONE);
            txt_fq.setVisibility(flag2 && setting == 0 ? View.VISIBLE : View.GONE);


            txt_confirm.setVisibility(flag3 ? View.VISIBLE : View.GONE);
            txt_penalty.setVisibility(flag3 && penalize ? View.VISIBLE : View.GONE);
            rl_3.setVisibility(flag3 ? View.VISIBLE : View.GONE);


        }


    }
}
