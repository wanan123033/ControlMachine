package com.feipulai.host.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.R;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;


/**
 * 等待提示框封装类
 */
public class AddStudentDialog {


    @BindView(R.id.edit_stu_code)
    EditText editStuCode;
    @BindView(R.id.edit_stu_name)
    EditText editStuName;
    @BindView(R.id.sp_stu_sex)
    Spinner spStuSex;
    private Dialog dialog;
    private Window window = null;
    private int sex = Student.MALE;
    private String[] sexData = new String[]{"男", "女"};

    public AddStudentDialog(Context context) {
        init(context);
    }

    protected void init(Context context) {
        dialog = new Dialog(context, R.style.dialog_style);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.view_add_student, null);
        ButterKnife.bind(this, view);
        window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);
        spStuSex.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, sexData));
    }

    @OnItemSelected({R.id.sp_stu_sex})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_stu_sex:
                sex = position;
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
                if (isCheckData()) {
                    addStudent();
                }
                break;
        }
    }


    private boolean isCheckData() {

        if (TextUtils.isEmpty(editStuCode.getText().toString())) {
            ToastUtils.showShort("请输入考号");
            return false;
        }
        if (TextUtils.isEmpty(editStuName.getText().toString())) {
            ToastUtils.showShort("请输入姓名");
            return false;
        }
        if (!StuSearchEditText.patternStuCode(editStuCode.getText().toString().trim())) {
            ToastUtils.showShort("请输入正确格式的考号");
            return false;
        }
        return true;
    }

    private void addStudent() {
        Student student = DBManager.getInstance().queryStudentByCode(editStuCode.getText().toString().trim());
        if (student == null) {
            student = new Student();
            student.setStudentCode(editStuCode.getText().toString().trim());
            student.setStudentName(editStuName.getText().toString().trim());
            student.setSex(sex);
            // 插入学生信息
            if (SettingHelper.getSystemSetting().isNetCheckTool()) {

            } else {
                LogUtils.operation("考生添加" + student.toString());
                try {
                    // 插入学生信息
                    DBManager.getInstance().insertStudent(student);
                } catch (Exception e) {
                    ToastUtils.showShort("该考生考好已存在");
                    return;
                }
            }

        }
        StudentItem studentItem = new StudentItem();
        studentItem.setStudentCode(editStuCode.getText().toString());
        studentItem.setItemCode(TestConfigs.getCurrentItemCode());
        studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        if (SettingHelper.getSystemSetting().isNetCheckTool()) {

        } else {

            DBManager.getInstance().insertStudentItem(studentItem);
            ToastUtils.showShort("考生添加成功");
        }

        EventBus.getDefault().post(new BaseEvent(student, EventConfigs.TEMPORARY_ADD_STU));
        dismissDialog();
    }

    /**
     * 显示对话框
     *
     * @param student                  考生
     * @param isCanceledOnTouchOutside 收点击dialog 之外 dialog消失
     */
    public void showDialog(Student student, Boolean isCanceledOnTouchOutside) {
        if (student == null) {
            if (dialog != null && !dialog.isShowing()) {
                dialog.show();
            }
            return;
        }
        editStuCode.setText(student.getStudentCode());
        editStuName.setText(student.getStudentName());
        spStuSex.setSelection(student.getSex());
        dialog.setCancelable(isCanceledOnTouchOutside);
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);


        //        dismissDialog();
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


    /**
     * 设置对话框取消监听
     * <p>
     * <p>
     * <p>
     * <br/> @version 1.0
     * <br/> @createTime 2015/11/23 15:32
     * <br/> @updateTime 2015/11/23 15:32
     * <br/> @createAuthor yeqing
     * <br/> @updateAuthor yeqing
     * <br/> @updateInfo (此处输入修改内容,若无修改可不写.)
     *
     * @param listener
     */
    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        if (null != dialog) {
            dialog.setOnDismissListener(listener);
        }
    }


}
