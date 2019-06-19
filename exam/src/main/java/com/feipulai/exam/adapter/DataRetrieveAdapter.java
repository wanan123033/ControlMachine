package com.feipulai.exam.adapter;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.R;
import com.feipulai.exam.bean.DataRetrieveBean;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by James on 2018/1/3 0003.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class DataRetrieveAdapter extends BaseQuickAdapter<DataRetrieveBean, DataRetrieveAdapter.ViewHolder> {


    public DataRetrieveAdapter(@Nullable List<DataRetrieveBean> data) {
        super(R.layout.item_data_retieve, data);
    }

    @Override
    protected void convert(final ViewHolder viewHolder, DataRetrieveBean retieveData) {
        viewHolder.mTvStuCode.setText(retieveData.getStudentCode());
        viewHolder.mTvStuName.setText(retieveData.getStudentName());
        viewHolder.mTvSex.setText(retieveData.getSex() == 0 ? "男" : "女");
        viewHolder.mTvTestState.setText(retieveData.getTestState() == 0 ? "未测" : "已测");
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
            viewHolder.mTvScore.setText(TextUtils.equals(retieveData.getResult(), "X") ? retieveData.getResult() : Integer.valueOf(retieveData.getResult()) == -1000 ? "" : retieveData.getResult());
        } else {
            viewHolder.mTvScore.setText(TextUtils.equals(retieveData.getResult(), "X") ? retieveData.getResult() : Integer.valueOf(retieveData.getResult()) == -1000 ? "" :
                    ResultDisplayUtils.getStrResultForDisplay(Integer.valueOf(retieveData.getResult())));
        }

        //将位置设置为CheckBox的tag
        viewHolder.mCbSelect.setTag(viewHolder.getLayoutPosition());
        viewHolder.mCbSelect.setChecked(retieveData.isChecked());
//        viewHolder.mCbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//            }
//        });
        viewHolder.setOnCheckedChangeListener(R.id.cb_select, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (viewHolder.getLayoutPosition()!=-1){
                            getData().get(viewHolder.getLayoutPosition()).setChecked(isChecked);
                            notifyItemChanged(viewHolder.getLayoutPosition());
                        }

                    }
                });
            }
        });
        if (retieveData.isChecked()) {
            viewHolder.mTvStuCode.setSelected(true);
            viewHolder.mTvScore.setSelected(true);
            viewHolder.mTvSex.setSelected(true);
            viewHolder.mTvStuName.setSelected(true);
            viewHolder.mTvTestState.setSelected(true);
            viewHolder.mViewCbContent.setSelected(true);
        } else {
            viewHolder.mTvStuCode.setSelected(false);
            viewHolder.mTvScore.setSelected(false);
            viewHolder.mTvSex.setSelected(false);
            viewHolder.mTvStuName.setSelected(false);
            viewHolder.mTvTestState.setSelected(false);
            viewHolder.mViewCbContent.setSelected(false);
        }
    }


    class ViewHolder extends BaseViewHolder {
        @BindView(R.id.cb_select)
        CheckBox mCbSelect;
        @BindView(R.id.tv_stuCode)
        TextView mTvStuCode;
        @BindView(R.id.tv_stuName)
        TextView mTvStuName;
        @BindView(R.id.tv_sex)
        TextView mTvSex;
        @BindView(R.id.tv_testState)
        TextView mTvTestState;
        @BindView(R.id.tv_score)
        TextView mTvScore;
        //@BindView(R.id.ll_detail)
        //LinearLayout mLlDetail;
        @BindView(R.id.view_cb_content)
        RelativeLayout mViewCbContent;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}