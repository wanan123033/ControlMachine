package com.feipulai.exam.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

/**
 * Created by zzs on  2020/12/31
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class EditResultDialog {


    @BindView(R.id.edit_stu_result)
    EditText editStuResult;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_unit)
    TextView txtUnit;
    @BindView(R.id.sp_result_state)
    Spinner spResultState;
    /**
     * 提示框对象
     */
    private Dialog dialog;
    private Window window = null;
    // 1:正常 2:犯规 3:中退 4:弃权
    private String[] resultStateArray = new String[]{"正常", "犯规", "中退", "弃权"};
    private int resultState = 1;

    public EditResultDialog(Context context) {
        init(context);
    }


    /**
     * 初始话对话框
     * <p>
     * <p>
     *
     * @param context
     */
    protected void init(Context context) {
        dialog = new Dialog(context, R.style.dialog_style);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.view_input_result, null);
        ButterKnife.bind(this, view);
        window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);
        spResultState.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, resultStateArray));
    }

    @OnItemSelected({R.id.sp_result_state})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_result_state:
                resultState = position + 1;
                break;

        }
    }

    @OnClick({R.id.view_txt_cancel, R.id.view_txt_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.view_txt_cancel:
                dismissDialog();
                break;
            case R.id.view_txt_confirm:
                if (resultState== RoundResult.RESULT_STATE_NORMAL){
                    if (TextUtils.isEmpty(editStuResult.getText().toString().trim())) {
                        ToastUtils.showShort("请输入成绩");
                        return;
                    }
                }else{
                    editStuResult.setText("0");
                }

                if (listener != null) {
                    listener.inputResult(editStuResult.getText().toString(), resultState);
                }
                break;
        }
    }

    /**
     * 显示对话框
     */
    public void showDialog(Student student) {
        if (student == null) {
            return;
        }
        txtStuCode.setText(student.getStudentCode());
        txtStuName.setText(student.getStudentName());
        txtUnit.setText(TestConfigs.sCurrentItem.getUnit());
        spResultState.setSelection(0);
        resultState = 1;
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * 关闭等待对话框
     * <p>
     * <br/> @version 1.0
     * <br/> @createTime 2015/11/19 18:33
     * <br/> @updateTime 2015/11/19 18:33
     * <br/> @createAuthor yeqing
     * <br/> @updateAuthor yeqing
     * <br/> @updateInfo (此处输入修改内容,若无修改可不写.)
     */
    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 判断进度条是否显示
     * <p>
     * <p>
     * <br/> @version 1.0
     * <br/> @createTime 2015/12/2 16:43
     * <br/> @updateTime 2015/12/2 16:43
     * <br/> @createAuthor yeqing
     * <br/> @updateAuthor yeqing
     * <br/> @updateInfo (此处输入修改内容,若无修改可不写.)
     *
     * @return
     */
    public boolean isShow() {
        return dialog != null && dialog.isShowing();
    }

    private OnInputResultListener listener;


    public void setListener(OnInputResultListener listener) {
        this.listener = listener;
    }

    public interface OnInputResultListener {
        void inputResult(String result, int state);
    }
}
