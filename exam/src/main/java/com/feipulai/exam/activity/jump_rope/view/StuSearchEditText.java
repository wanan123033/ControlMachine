package com.feipulai.exam.activity.jump_rope.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.SearchResultAdapter;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Student;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by James on 2018/12/10 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

/**
 * 用于输入学号或身份证号,同时实时显示学生信息结果到指定的{@link ListView}中
 * note:必须指定显示数据的listview和activity,机必须调用{@link #setResultView(ListView)}
 */
@SuppressLint("AppCompatCustomView")
public class StuSearchEditText extends RelativeLayout implements AdapterView.OnItemClickListener {
    @BindView(R.id.et_input_text)
    EditText etInputText;
    @BindView(R.id.img_delete)
    ImageView imgDelete;
    private ListView mLvResults;
    private List<Student> mStudentList;
    private volatile OnCheckedInListener listener;
    private Context mContext;

    public StuSearchEditText(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public StuSearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public StuSearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_stu_search_edittext, null);
        ButterKnife.bind(this, view);
        addView(view);
        etInputText.addTextChangedListener(new TextWatcher() {
            //input deals
            //CharSequence s参数表示当前TextView内部的mText成员变量，实际上就是当前显示的文本；
            //int start参数表示需要改变的文字区域的起点，即选中的文本区域的起始点；
            //int count参数表示需要改变的文字的字符数目，即选中的文本区域的字符的数目；
            //int after参数表示替换的文字的字符数目。
            //特别的，当TextView删除文本的时候，after的值为0，此时TextView使用用空字符串代替需要改变的文字区域来达到删除文字的目的。
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            //CharSequence s参数表示当前TextView内部的mText成员变量，此时的mText已经被修改过了，但此时mText所表示的文本还没有被显示到UI组件上;
            //int start参数表示改变的文字区域的起点;
            //int before参数表示改变的文字区域在改变前的旧的文本长度，即选中文字区域的文本长度；
            //int count参数表示改变的文字区域在修改后的新的文本长度。
            //特别的，当TextView添加文本的时候，before 的值为0，此时相当于TextView将空的字符区域用新的文本代替。
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                String str = s.toString();
                SearchResultAdapter adapter;
                if (mLvResults != null) {
                    if (length == 0) {
                        mLvResults.setVisibility(View.GONE);
                        imgDelete.setVisibility(INVISIBLE);
                        return;
                    }
                    imgDelete.setVisibility(VISIBLE);
                    //外接扫描不实时查
                    if (SettingHelper.getSystemSetting().getCheckTool() == 3) {
                        return;
                    }

                    if (length == 18) {
                        //精确搜索身份证
                        Student student = DBManager.getInstance().queryStudentByIDCode(str);
                        mStudentList = new ArrayList<>();
                        if (student != null) {
                            mStudentList.add(student);
                        }
                    } else {
                        //模糊搜索考号
                        if (com.feipulai.exam.view.StuSearchEditText.patternStuCode(str)) {
                            //模糊搜索考号
                            mStudentList = DBManager.getInstance().fuzzyQueryByStuCode(str, 20, 0);
                        } else {
                            ToastUtils.showShort("请输入正常学生考号");
                        }

                    }
                    if (mStudentList == null || mStudentList.size() == 0) {
                        mLvResults.setVisibility(View.GONE);
                    } else {
                        adapter = new SearchResultAdapter(getContext(), mStudentList);
                        mLvResults.setAdapter(adapter);
                        mLvResults.setVisibility(View.VISIBLE);
                    }
                }
            }

        });
        etInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_GO
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    if (TextUtils.isEmpty(v.getText().toString().trim())) {
                        return true;
                    }
                    LogUtil.logDebugMessage("KEYCODE_ENTER======>");
//                    mStudentList = DBManager.getInstance().fuzzyQueryByStuCode(etInputText.getText().toString(), 20, 0);
//                    if (mStudentList == null || mStudentList.size() == 0) {
//                        mLvResults.setVisibility(View.GONE);
//                    } else {
//                        SearchResultAdapter adapter = new SearchResultAdapter(getContext(), mStudentList);
//                        mLvResults.setAdapter(adapter);
//                        mLvResults.setVisibility(View.VISIBLE);
//                    }
                    search(etInputText.getText().toString());


                    return true;
                }
                return false;
            }
        });
    }

    private void check(Student student) {
        if (listener != null) {
            listener.onInputCheck(student);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                etInputText.setText("");
            }
        }, 100);
    }

    public void setResultView(ListView listView) {
        mLvResults = listView;
        mLvResults.setOnItemClickListener(this);
    }

    public void setOnCheckedInListener(OnCheckedInListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Student student = mStudentList.get(position);
        //Log.i("james","position:" + position + "mStudentList:" + mStudentList.toString());
        check(student);
    }

    public interface OnCheckedInListener {
        /**
         * 手动输入检录时调用,需要检查该学生是否已有成绩,是否允许重测
         *
         * @param student 需要检录的学生
         * @return 检录成功时返回true, 否则返回false
         */
        boolean onInputCheck(Student student);
    }

    public void search(String dataText) {
        mStudentList = DBManager.getInstance().fuzzyQueryByStuCode(dataText, 20, 0);
        if (mStudentList == null || mStudentList.size() == 0) {
            mLvResults.setVisibility(View.GONE);
        } else {
            SearchResultAdapter adapter = new SearchResultAdapter(getContext(), mStudentList);
            mLvResults.setAdapter(adapter);
            mLvResults.setVisibility(View.VISIBLE);
        }
        if (mStudentList != null && mStudentList.size() > 0) {
            for (Student student : mStudentList) {
                if (dataText.equals(student.getStudentCode()) || dataText.equals(student.getIdCardNo())) {
                    // 找到已有的,检录
                    check(student);
                    return;
                }
            }
        }
        // 添加考生,检录
        Student student = new Student();
        student.setStudentCode(dataText);
        check(student);
    }

    @OnClick({R.id.img_delete, R.id.txt_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_delete:
                etInputText.setText("");
                break;
            case R.id.txt_search:
                search(etInputText.getText().toString());
                break;
        }
    }
}
