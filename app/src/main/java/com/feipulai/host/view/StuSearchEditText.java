package com.feipulai.host.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
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
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.adapter.SearchResultAdapter;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by James on 2018/2/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

/**
 * 用于输入学号或身份证号,同时实时显示学生信息结果到指定的{@link ListView}中
 * note:必须指定显示数据的listview和activity,机必须调用{@link #//setData(ListView, BaseCheckActivity2)}
 */
@SuppressLint("AppCompatCustomView")
public class StuSearchEditText extends RelativeLayout {

    @BindView(R.id.et_input_text)
    EditText etInputText;
    @BindView(R.id.img_delete)
    ImageView imgDelete;

    private ListView mLvResults;
    private List<Student> mStudentList = new ArrayList<>();
    private BaseCheckActivity mActivity;
    private RecyclerView mRecyclerView;
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

    private void search() {
        if (patternStuCode(etInputText.getText().toString())) {
            //模糊搜索考号
            mStudentList = DBManager.getInstance().fuzzyQueryByStuCode(etInputText.getText().toString(), 20, 0);
            if (mStudentList == null || mStudentList.size() == 0) {
                mLvResults.setVisibility(View.GONE);
            } else {
                SearchResultAdapter adapter = new SearchResultAdapter(getContext(), mStudentList);
                mLvResults.setAdapter(adapter);
                mLvResults.setVisibility(View.VISIBLE);
                if (mRecyclerView != null) {
                    mRecyclerView.setVisibility(GONE);
                }
            }
        } else {
            ToastUtils.showShort("请输入正常学生考号");
        }
        if (mStudentList != null && mStudentList.size() > 0) {
            for (Student student : mStudentList) {
                if (etInputText.getText().toString().equals(student.getStudentCode()) || etInputText.getText().toString().equals(student.getIdCardNo())) {
                    // 找到已有的,检录
                    mActivity.checkInput(student);
                    clrarData();
                    return;
                }
            }
        }
        if (mStudentList.size() == 0) {
            Student student = new Student();
            student.setStudentCode(etInputText.getText().toString());
            showAddHint(student);
            clrarData();
        }
    }

    private void clrarData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                etInputText.setText("");
            }
        }, 100);
    }

    private void init() {

        View view = LayoutInflater.from(mContext).inflate(R.layout.view_stu_search_edittext, null);
        ButterKnife.bind(this, view);
        addView(view);
//        setInputType(EditorInfo.TYPE_CLASS_TEXT);
//        setImeOptions(EditorInfo.IME_ACTION_GO);
        //todo 与扫描枪扫描查询冲突重檢入
        etInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (SettingHelper.getSystemSetting().getCheckTool() == 3) {
                        etInputText.setText("");
                        showInput(false);
                        return true;
                    }
//                    if (!TextUtils.isEmpty(etInputText.getText().toString())) {
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                search();
//                            }
//                        }, 200);
//                    }
                    return true;
                }
                return false;
            }
        });
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
                        if (patternStuCode(str)) {
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
                        if (mRecyclerView != null) {
                            mRecyclerView.setVisibility(GONE);
                        }
                    }
                }
            }

        });
    }

    private void showAddHint(final Student student) {
        new SweetAlertDialog(mContext).setTitleText(mContext.getString(R.string.addStu_dialog_title))
                .setContentText(mContext.getString(R.string.addStu_dialog_content))
                .setConfirmText(mContext.getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                new AddStudentDialog(mContext).showDialog(student, false);
            }
        }).setCancelText(mContext.getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();
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

    public void setData(RecyclerView recyclerView, ListView listView, BaseCheckActivity activity) {
        mRecyclerView = recyclerView;
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

    public static boolean patternStuCode(String stuCode) {
        Pattern p = Pattern
                .compile("\\w+");
        Matcher m = p.matcher(stuCode);
        return m.matches();
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
}
