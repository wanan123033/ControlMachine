package com.feipulai.common.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.feipulai.common.R;

/**
 * Created by zzs on  2019/8/23
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class EditDialog {
    TextView txtDialogTitle;
    TextView txtDialogContent;
    EditText editContent;
    Button btnConfirm;
    Button btnCancel;
    private Context mContext;
    public Dialog dialog;
    /**
     * 标题
     */
    private String mTitle;
    /**
     * 内容消息
     */
    private String mMessage;
    /**
     * 输入提示
     */
    private String editHint;
    private String editText;
    /**
     * 是否可取消
     */
    private boolean canelable = false;

    /**
     * 默认点击事件
     */
    private View.OnClickListener onDefaultClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            dialog.dismiss();
        }
    };
    private View.OnClickListener onNegativeListener = onDefaultClickListener;

    public EditDialog(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_edit_dialog, null);
        txtDialogTitle = view.findViewById(R.id.txt_dialog_title);
        txtDialogContent = view.findViewById(R.id.txt_dialog_content);
        editContent = view.findViewById(R.id.edit_content);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnConfirm = view.findViewById(R.id.btn_confirm);

        dialog = DialogUtils.create(mContext, view, canelable);
    }

    public void show() {
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onPositiveListener.OnClickListener(dialog, editContent.getText().toString());
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onNegativeListener.onClick(v);
            }
        });
        if (!TextUtils.isEmpty(mTitle)) {
            txtDialogTitle.setVisibility(View.VISIBLE);
            txtDialogTitle.setText(mTitle);
        }
        if (!TextUtils.isEmpty(mMessage)) {
            txtDialogContent.setVisibility(View.VISIBLE);
            txtDialogContent.setText(mMessage);
        }

        editContent.setHint(editHint);
        editContent.setText(editText);
        dialog.show();
    }

    /**
     * 创建对话框
     *
     * @author wzl
     */
    public static class Builder {
        private EditDialog editDialog;

        public Builder(Context context) {
            editDialog = new EditDialog(context);
        }

        /**
         * 设置标题
         *
         * @param title 标题
         * @return
         */
        public Builder setTitle(String title) {
            editDialog.mTitle = title;
            return this;
        }

        /**
         * 设置消息
         *
         * @param message 消息
         * @return
         */
        public Builder setMessage(String message) {
            editDialog.mMessage = message;
            return this;
        }

        /**
         * 输入提示
         *
         * @param editHint 提示
         * @return
         */
        public Builder setEditHint(String editHint) {
            editDialog.editHint = editHint;
            return this;
        }

        /**
         * 输入提示
         *
         * @param editText 提示
         * @return
         */
        public Builder setEditText(String editText) {
            editDialog.editText = editText;
            return this;
        }
        /**
         * 设置确定按钮点击监听
         *
         * @param onClickListener
         * @return
         */
        public Builder setPositiveButton(OnConfirmClickListener onClickListener) {
            editDialog.onPositiveListener = onClickListener;
            return this;
        }

        /**
         * 设置是否可取消
         *
         * @param canelable
         * @return
         */
        public Builder setCanelable(boolean canelable) {
            editDialog.canelable = canelable;
            return this;
        }

        /**
         * 创建对话框
         *
         * @return
         */
        public EditDialog build() {
            return editDialog;
        }
    }

    private OnConfirmClickListener onPositiveListener;

    public interface OnConfirmClickListener {
        void OnClickListener(Dialog dialog, String content);
    }

    public void setOnConfirmClickListener(OnConfirmClickListener listener) {
        this.onPositiveListener = listener;
    }
}
