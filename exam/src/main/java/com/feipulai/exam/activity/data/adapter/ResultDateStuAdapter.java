package com.feipulai.exam.activity.data.adapter;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.R;
import com.feipulai.exam.bean.DataRetrieveBean;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

/**
 * Created by zzs on  2021/10/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ResultDateStuAdapter extends BaseQuickAdapter<DataRetrieveBean, BaseViewHolder> {
    private int selectPosition = 0;

    public int getSelectPosition() {
        return selectPosition;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }

    public ResultDateStuAdapter(@Nullable List<DataRetrieveBean> data) {
        super(R.layout.item_data_upload_stu, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, DataRetrieveBean retrieveBean) {
        helper.setText(R.id.item_txt_stu_name, retrieveBean.getStudentName());
        helper.setText(R.id.item_txt_stu_code, retrieveBean.getStudentCode());
        helper.setText(R.id.item_txt_stu_sex, retrieveBean.getSex() == 0 ? "男" : "女");
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
            helper.setText(R.id.item_txt_score, TextUtils.equals(retrieveBean.getResult(), "X") ? retrieveBean.getResult() : retrieveBean.getResult());
        } else {
            helper.setText(R.id.item_txt_score, TextUtils.equals(retrieveBean.getResult(), "X") ? retrieveBean.getResult() :
                    ResultDisplayUtils.getStrResultForDisplay(Integer.valueOf(retrieveBean.getResult())));
        }
    }
}
