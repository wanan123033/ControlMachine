package com.feipulai.exam.activity.sargent_jump.more_device;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.person.adapter.BasePersonTestResultAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.entity.Student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BaseMoreOneView extends RelativeLayout {

    @BindView(R.id.img_portrait)
    ImageView imgPortrait;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_stu_sex)
    TextView txtStuSex;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.cb_device_state)
    CheckBox cbDeviceState;
    @BindView(R.id.tv_base_height)
    TextView tvBaseHeight;
    @BindView(R.id.tv_resurvey)
    TextView tvResurvey;
    @BindView(R.id.tv_inBack)
    TextView tvInBack;
    @BindView(R.id.tv_abandon)
    TextView tvAbandon;
    @BindView(R.id.txt_start_test)
    TextView txtStartTest;
    @BindView(R.id.tv_get_data)
    TextView tvGetData;
    private Context mContext;
    private List<String> resultList = new ArrayList<>();
    public BasePersonTestResultAdapter adapter;
    private OnClickListener listener;
    private boolean showGetData;
    private boolean showStartTest;

    public void setShowGetData(boolean showGetData) {
        this.showGetData = showGetData;
        if (showGetData) {
            tvGetData.setVisibility(View.VISIBLE);
        } else {
            tvGetData.setVisibility(View.INVISIBLE);
        }
    }

    public void setShowStartTest(boolean showStartTest) {
        this.showStartTest = showStartTest;
        if (showStartTest) {
            txtStartTest.setVisibility(View.VISIBLE);
        } else {
            txtStartTest.setVisibility(View.INVISIBLE);
        }
    }

    public void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    public BaseMoreOneView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BaseMoreOneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }


    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_base_more_device_one, null);
        ButterKnife.bind(this, view);
        addView(view);
        adapter = new BasePersonTestResultAdapter(resultList);
        rvTestResult.setLayoutManager(new LinearLayoutManager(mContext));
        //给RecyclerView设置适配器
        rvTestResult.setAdapter(adapter);
        if (SettingHelper.getSystemSetting().isAgainTest()) {
            tvResurvey.setEnabled(true);
            tvResurvey.setVisibility(View.VISIBLE);
        } else {
            tvResurvey.setVisibility(View.GONE);
        }
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                adapter.setSelectPosition(i);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 初始化成绩个数
     *
     * @param testCount
     */
    public void initResultCount(int testCount) {
        resultList.clear();
        for (int i = 0; i < testCount; i++) {
            resultList.add("");
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 初始化数据
     *
     * @param pair
     */
    public void initData(BaseStuPair pair) {
        resultList.clear();
        resultList.addAll(Arrays.asList(pair.getTimeResult()));
        adapter.notifyDataSetChanged();
        adapter.setSelectPosition(-1);
        adapter.setIndexPostion(-1);
        refreshTxtStu(pair.getStudent());
    }

    /**
     * 加载学生信息
     */
    private void refreshTxtStu(@NonNull Student student) {
        if (student != null) {
            setBtnEnabled(true, true,true);
            txtStuName.setText(student.getStudentName());
            txtStuSex.setText((student.getSex() == Student.MALE ? "男" : "女"));
            txtStuCode.setText(student.getStudentCode());
            Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + student.getStudentCode() + ".jpg")
                    .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
        } else {
            txtStuName.setText("");
            txtStuSex.setText("");
            txtStuCode.setText("");
            imgPortrait.setImageResource(R.mipmap.icon_head_photo);
        }
    }

    public void refreshDeviceState(BaseDeviceState deviceState) {
        if (deviceState != null) {
            if (deviceState.getState() != BaseDeviceState.STATE_ERROR) {
                cbDeviceState.setChecked(true);
            } else {
                cbDeviceState.setChecked(false);
            }
        }
    }

    /**
     * 设置成绩信息
     *
     * @param pair
     */
    public void setResultData(BaseStuPair pair) {
        resultList.clear();
        resultList.addAll(Arrays.asList(pair.getTimeResult()));
        adapter.notifyDataSetChanged();
    }

    /**
     * 定位成绩
     */
    public void indexResult(int index) {
        adapter.setIndexPostion(index);
        adapter.setSelectPosition(index);
        adapter.notifyDataSetChanged();
    }

    public void setBtnEnabled(boolean inBack, boolean abandon, boolean resurvey) {

        tvInBack.setEnabled(inBack);
        tvAbandon.setEnabled(abandon);
        if (SettingHelper.getSystemSetting().isAgainTest()) {
            tvResurvey.setEnabled(resurvey);
            tvResurvey.setVisibility(View.VISIBLE);
        } else {
            tvResurvey.setVisibility(View.GONE);
        }


    }
    /**
     * 获取成绩选中下标
     *
     * @return
     */
    public int getSelectPosition() {
        return adapter.getSelectPosition();
    }

    public int getAFRFrameLayoutResID() {
        return R.id.one_frame_camera;
    }

    @OnClick({R.id.tv_foul, R.id.tv_normal, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_resurvey, R.id.txt_start_test,
            R.id.txt_stu_skip,R.id.tv_get_data})
    public void onViewClicked(View view) {
        if (listener != null) {
            listener.onClick(view);
        }

    }


}
