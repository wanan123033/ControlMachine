package com.feipulai.exam.view;

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
import com.feipulai.exam.R;
import com.feipulai.exam.adapter.ScheduleAdapter;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.ItemSchedule;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;


/**
 * 等待提示框封装类
 */
public class AddStudentDialog {


    @BindView(R.id.sp_schedule)
    Spinner spSchedule;
    @BindView(R.id.edit_stu_code)
    EditText editStuCode;
    @BindView(R.id.edit_stu_name)
    EditText editStuName;
    @BindView(R.id.sp_stu_sex)
    Spinner spStuSex;
    @BindView(R.id.edit_stu_school)
    EditText editStuSchool;
    /**
     * 提示框对象
     */
    private Dialog dialog;
    private List<Schedule> scheduleList = new ArrayList<>();
    private ScheduleAdapter scheduleAdapter;
    private Window window = null;
    private int sex = Student.MALE;
    private String schedult;
    private String[] sexData = new String[]{"男", "女"};

    public AddStudentDialog(Context context) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.view_add_student, null);
        ButterKnife.bind(this, view);
        window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);
        spStuSex.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, sexData));
        scheduleAdapter = new ScheduleAdapter(context, scheduleList);
        spSchedule.setAdapter(scheduleAdapter);
        getSchedule();
    }


    @OnItemSelected({R.id.sp_stu_sex, R.id.sp_schedule})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_stu_sex:
                sex = position;
                break;
            case R.id.sp_schedule:
                schedult = scheduleList.get(position).getScheduleNo();
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

    /**
     * 获取日程
     */
    private void getSchedule() {
        scheduleList.clear();
        List<Schedule> dbSchedule = DBManager.getInstance().getAllSchedules();
        scheduleList.addAll(dbSchedule);
        scheduleAdapter.notifyDataSetChanged();
        if (scheduleList.size() > 0) {
            schedult = scheduleList.get(0).getScheduleNo();
        } else {
            DBManager.getInstance().insertSchedules(new Schedule("-1", "946659661", "946659661"));
            ItemSchedule itemSchedule = new ItemSchedule();
            itemSchedule.setScheduleNo("-1");
            itemSchedule.setItemCode(TestConfigs.DEFAULT_ITEM_CODE);
            List<ItemSchedule> dbItemScheduleList = new ArrayList<>();
            dbItemScheduleList.add(itemSchedule);
            DBManager.getInstance().insertItemSchedulesList(dbItemScheduleList);
            dbSchedule = DBManager.getInstance().getAllSchedules();
            scheduleList.addAll(dbSchedule);
            scheduleAdapter.notifyDataSetChanged();
            schedult = scheduleList.get(0).getScheduleNo();
        }
        //schedult = scheduleList.size() > 0 ? scheduleList.get(0).getScheduleNo() : "";
    }

    private boolean isCheckData() {
        if (TextUtils.isEmpty(schedult)) {
            ToastUtils.showShort("请选择日程");
            return false;
        }
        if (TextUtils.isEmpty(editStuCode.getText().toString())) {
            ToastUtils.showShort("请输入考号");
            return false;
        }
//        if (TextUtils.isEmpty(editStuName.getText().toString())) {
//            ToastUtils.showShort("请输入姓名");
//            return false;
//        }
        //if (TextUtils.isEmpty(editStuSchool.getText().toString())) {
        //    ToastUtils.showShort("请输入单位");
        //    return false;
        //}
        //if (TextUtils.isEmpty(editStuSchool.getText().toString())) {
        //    ToastUtils.showShort("请输入学校");
        //    return false;
        //}
        return true;
    }

    private void addStudent() {
        Student student = new Student();
        student.setStudentCode(editStuCode.getText().toString());
        student.setStudentName(editStuName.getText().toString());
        student.setSex(sex);
        try {
            // 插入学生信息
            DBManager.getInstance().insertStudent(student);
        } catch (Exception e) {
            ToastUtils.showShort("该考生考好已存在");
            return;
        }

        StudentItem studentItem = new StudentItem();
        studentItem.setStudentCode(editStuCode.getText().toString());
        studentItem.setItemCode(TestConfigs.getCurrentItemCode());
        studentItem.setScheduleNo(schedult);
        studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        studentItem.setExamType(0);
        DBManager.getInstance().insertStudentItem(studentItem);
        ToastUtils.showShort("考生添加成功");
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
        if (student == null)
            return;
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
