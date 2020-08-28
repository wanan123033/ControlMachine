package com.feipulai.exam.activity.setting;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2020/8/24
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PrintSettingAdapter extends BaseQuickAdapter<PrintSetting.PrintItem, PrintSettingAdapter.ViewHolder> {

    private boolean isWrite = false;
    private int etFocusPos = -1;

    public void setWrite(boolean write) {
        isWrite = write;
    }

    public boolean isWrite() {
        return isWrite;
    }

    public PrintSettingAdapter(@Nullable List<PrintSetting.PrintItem> data) {
        super(R.layout.item_print_setting, data);
    }

    @Override
    protected void convert(final ViewHolder helper, final PrintSetting.PrintItem item) {
        helper.setText(R.id.item_txt_number, (helper.getLayoutPosition() + 1) + "、");
        helper.setChecked(R.id.item_cb_use, item.isUse());
        helper.setText(R.id.item_txt_name, item.getName());

        helper.cbUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setUse(helper.cbUse.isChecked());
            }
        });
        if (isWrite) {
            helper.cbUse.setEnabled(true);
            helper.txtName.setEnabled(true);
        } else {
            helper.cbUse.setEnabled(false);
            helper.txtName.setEnabled(false);
        }
        helper.txtName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    etFocusPos = helper.getLayoutPosition();
                }
            }
        });
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.txtName.removeTextChangedListener(textWatcher);
    }

    @Override
    public void onViewAttachedToWindow(final ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.txtName.addTextChangedListener(textWatcher);
        if (etFocusPos == holder.getAdapterPosition()) {
            holder.txtName.requestFocus();
            holder.txtName.setSelection(holder.txtName.getText().length());
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            getData().get(etFocusPos).setName(s.toString());
        }
    };

    class ViewHolder extends BaseViewHolder {

        @BindView(R.id.item_cb_use)
        CheckBox cbUse;
        @BindView(R.id.item_txt_number)
        TextView txtNumber;
        @BindView(R.id.item_txt_name)
        EditText txtName;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
