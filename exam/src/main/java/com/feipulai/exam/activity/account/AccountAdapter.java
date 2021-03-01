package com.feipulai.exam.activity.account;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.setting.MonitoringBean;
import com.feipulai.exam.entity.Account;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2019/12/31
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AccountAdapter extends BaseQuickAdapter<Account, AccountAdapter.ViewHolder> {
    private int seletePosition = -1;

    public void setSeletePosition(int seletePosition) {
        this.seletePosition = seletePosition;
    }

    public int getSeletePosition() {
        return seletePosition;
    }

    public AccountAdapter(@Nullable List<Account> data) {
        super(R.layout.item_account_list, data);
    }

    @Override
    protected void convert(final ViewHolder viewHolder, Account account) {
        viewHolder.txtCreateTime.setText(DateUtil.formatTime(account.getCreateTime(),"yyyyMMddHHmmss"));
        if (account.getUpdateTime()!=null){
            viewHolder.txtUpdateTime.setText(DateUtil.formatTime(account.getUpdateTime(),"yyyyMMddHHmmss"));
        }
        viewHolder.txtName.setText(account.getAccount());


        if (seletePosition == viewHolder.getLayoutPosition()) {
            viewHolder.txtName.setSelected(true);
            viewHolder.txtCreateTime.setSelected(true);
            viewHolder.txtUpdateTime.setSelected(true);
        } else {
            viewHolder.txtName.setSelected(false);
            viewHolder.txtCreateTime.setSelected(false);
            viewHolder.txtUpdateTime.setSelected(false);
        }
    }


    class ViewHolder extends BaseViewHolder {

        @BindView(R.id.item_txt_name)
        TextView txtName;
        @BindView(R.id.item_txt_create_time)
        TextView txtCreateTime;
        @BindView(R.id.item_txt_update_time)
        TextView txtUpdateTime;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
