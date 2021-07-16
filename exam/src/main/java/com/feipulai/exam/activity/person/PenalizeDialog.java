package com.feipulai.exam.activity.person;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.adapter.PenalizeResultAdapter;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Student;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zzs on  2020/12/31
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PenalizeDialog {

    @BindView(R.id.tip)
    TextView tip;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.tv_no_student)
    TextView tvNoStudent;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.view_txt_cancel)
    TextView viewTxtCancel;
    @BindView(R.id.view_txt_confirm)
    TextView viewTxtConfirm;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.turn_last)
    TextView turnLast;
    @BindView(R.id.turn_next)
    TextView turnNext;
    @BindView(R.id.rv_penalize)
    RecyclerView rvPenalize;
    private Context context;
    private String[] title = {"犯规", "中退", "放弃", "正常"};
    private Dialog dialog;
    private Window window = null;
    private int testTimes;//测试次数
    private int state;//0未检录1已检录未测试2已检录正在测试
    private PenalizeResultAdapter mAdapter;
    private List<String> mList;
    private Student lastStudent;
    private String[] lastResult;//上一个学生成绩
    private Student student;//当前学生
    private String[] results;
    /**
     * @param context
     */
    public PenalizeDialog(Context context,int testTimes) {
        this.context = context;
        this.testTimes = testTimes;
        for (int i = 0; i < testTimes; i++) {
            mList.add("");
        }
        init();
    }


    protected void init() {
        dialog = new Dialog(context, R.style.dialog_style);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_penalize, null);
        ButterKnife.bind(this, view);
        window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        mAdapter = new PenalizeResultAdapter(mList);
        GridLayoutManager layoutManager = new GridLayoutManager(context, testTimes);
        rvPenalize.setLayoutManager(layoutManager);
        rvPenalize.setAdapter(mAdapter);
    }


    @OnClick({R.id.turn_last, R.id.turn_next, R.id.view_txt_cancel, R.id.view_txt_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.turn_last:
                turnNext.setVisibility(View.VISIBLE);
                turnLast.setVisibility(View.INVISIBLE);
                mList = Arrays.asList(lastResult);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.turn_next:
                turnNext.setVisibility(View.INVISIBLE);
                turnLast.setVisibility(View.VISIBLE);
                mList = Arrays.asList(results);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.view_txt_cancel:
                dialog.dismiss();
                break;
            case R.id.view_txt_confirm:
                dialog.dismiss();
                break;
        }
    }

    public void showDialog(int tip) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
        tvTitle.setText(title[tip]);
    }

    public void setData(int state,Student student ,String[] result,Student lastStudent,String[] lastResult){
        this.lastResult = lastResult;
        this.results = result;
        this.student = student;
        this.state = state;
        switch (state){
            case 0:
                if (null == lastStudent){
                    llContent.setVisibility(View.GONE);
                    tvNoStudent.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    },2000);
                }else {
                    mList = Arrays.asList(lastResult);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case 1:
                turnLast.setVisibility(View.VISIBLE);
                if (student!=null){
                    mList = Arrays.asList(result);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case 2:
                if (student!=null){
                    mList = Arrays.asList(result);
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    public void dismissDialog(){
        if (dialog!=null && dialog.isShowing())
            dialog.dismiss();
        this.student = null;
        this.student = null;
    }
}
