package com.feipulai.exam.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.SearchResultAdapter;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by James on 2018/2/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

/**
 * 用于输入学号或身份证号,同时实时显示学生信息结果到指定的{@link ListView}中
 * note:必须指定显示数据的listview和activity,机必须调用{@link #setData(ListView, BaseCheckActivity)}
 */
@SuppressLint("AppCompatCustomView")
public class StuSearchEditText2 extends RelativeLayout {
    @BindView(R.id.et_input_text)
    EditText etInputText;
    @BindView(R.id.img_delete)
    ImageView imgDelete;
    private ListView mLvResults;
    private List<Student> mStudentList;
    private BaseCheckActivity mActivity;
    private Context mContext;

    public StuSearchEditText2(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public StuSearchEditText2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public StuSearchEditText2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void search() {
        if (StuSearchEditText.patternStuCode(etInputText.getText().toString())) {
            //模糊搜索考号
            mStudentList = DBManager.getInstance().fuzzyQueryByStuCode(etInputText.getText().toString(), 20, 0);
            if (mStudentList == null || mStudentList.size() == 0) {
                mLvResults.setVisibility(View.GONE);
            } else {
                SearchResultAdapter adapter = new SearchResultAdapter(getContext(), mStudentList);
                mLvResults.setAdapter(adapter);
                mLvResults.setVisibility(View.VISIBLE);

            }
        } else {
            ToastUtils.showShort("请输入正常学生考号");
        }
        if (mStudentList.size() == 0 && SettingHelper.getSystemSetting().isTemporaryAddStu()) {
            Student student = new Student();
            student.setStudentCode(etInputText.getText().toString());
            showAddHint(student);
        }
    }

    private void init() {
//        setInputType(EditorInfo.TYPE_CLASS_TEXT);
//        setImeOptions(EditorInfo.IME_ACTION_GO);
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_stu_search_edittext, null);
        ButterKnife.bind(this, view);
        addView(view);
        etInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (!TextUtils.isEmpty(etInputText.getText().toString())
                            && SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                        search();
                    }

                    return true;
                }
                return false;
            }
        });
        etInputText.addTextChangedListener(new txtWatcher());
    }

    private void showAddHint(final Student student) {
        new AlertDialog.Builder(mActivity)
                .setCancelable(false)
                .setTitle("提示")
                .setMessage("无考生信息，是否新增")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AddStudentDialog(mActivity).showDialog(student, false);
                    }
                })
                .setNegativeButton("否", null)
                .show();

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_STAR) {
            etInputText.append("X");
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void setData(ListView listView, BaseCheckActivity activity) {
        mActivity = activity;
        mLvResults = listView;
        mLvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Student student = mStudentList.get(position);
                //Log.i("james","position:" + position + "mStudentList:" + mStudentList.toString());
                showInput(false);
                mActivity.checkInput(student);
                Logger.i("input student code:" + student.getStudentCode());
                etInputText.setText("");
            }
        });
    }

    /**
     * 是否关闭键盘
     *
     * @param show true 显示， false 关闭键盘
     */
    public void showInput(boolean show) {
        try {
            if (show) {
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInputFromInputMethod(mActivity.getCurrentFocus().getApplicationWindowToken(), 0);
            } else {
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getApplicationWindowToken(), 0);
            }
        } catch (NullPointerException e1) {

        } catch (Exception e) {
        }
    }


    public interface ShowListListener {
        void onShowListener(boolean isShow);
    }

    private ShowListListener showListListener;

    public void setShowListListener(ShowListListener showListListener) {
        this.showListListener = showListListener;
    }

    @OnClick({R.id.img_delete, R.id.txt_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_delete:
                etInputText.setText("");
                break;
            case R.id.txt_search:
                search();
                break;
        }
    }

    class txtWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

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
                    showListListener.onShowListener(false);
                    mLvResults.setVisibility(View.GONE);
                    imgDelete.setVisibility(INVISIBLE);
                    return;
                }
                //外接扫描不实时查
                if (SettingHelper.getSystemSetting().getCheckTool() == 3) {
                    return;
                }
                imgDelete.setVisibility(VISIBLE);
                if (length == 18) {
                    //精确搜索身份证
                    Student student = DBManager.getInstance().queryStudentByIDCode(str);
                    mStudentList = new ArrayList<>();
                    if (student != null) {
                        mStudentList.add(student);
                    }
                } else {
                    if (StuSearchEditText.patternStuCode(str)) {
                        //模糊搜索考号
                        mStudentList = DBManager.getInstance().fuzzyQueryByStuCode(str, 20, 0);
                    } else {
                        ToastUtils.showShort("请输入正常学生考号");
                    }

                }
                if (mStudentList == null || mStudentList.size() == 0) {
                    showListListener.onShowListener(false);
                    mLvResults.setVisibility(View.GONE);
                } else {
                    adapter = new SearchResultAdapter(getContext(), mStudentList);
                    mLvResults.setAdapter(adapter);
                    showListListener.onShowListener(true);
                    mLvResults.setVisibility(View.VISIBLE);

                }
            }
        }
    }

}
