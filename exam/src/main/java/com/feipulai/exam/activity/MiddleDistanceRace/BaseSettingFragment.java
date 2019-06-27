package com.feipulai.exam.activity.MiddleDistanceRace;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.feipulai.exam.config.SharedPrefsConfigs.FIRST_TIME;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_CARRY;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_NUMBER;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_TIME_FIRST;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_TIME_SPAN;
import static com.feipulai.exam.config.SharedPrefsConfigs.SPAN_TIME;

/**
 * created by ww on 2019/6/24.
 */
public class BaseSettingFragment extends Fragment implements AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener {
    @BindView(R.id.sp_base_no)
    Spinner spBaseNo;
    @BindView(R.id.et_middle_race_time_first)
    EditText etMiddleRaceTimeFirst;
    @BindView(R.id.et_middle_race_time_span)
    EditText etMiddleRaceTime_Span;
    @BindView(R.id.rb_carry_mode_1)
    RadioButton rbCarryMode1;
    @BindView(R.id.rb_carry_mode_2)
    RadioButton rbCarryMode2;
    @BindView(R.id.rb_carry_mode_3)
    RadioButton rbCarryMode3;
    @BindView(R.id.rg_carry_mode)
    RadioGroup rgCarryMode;
    private Context mContext;
    private int baseNo;
    private int time_first;//首次接收时间
    private int time_span;//最小时间间隔
    private int carry_mode;//进位方式（0四舍五入1非零取整2非零进位）
    private int[] rbCarry = {R.id.rb_carry_mode_1, R.id.rb_carry_mode_2, R.id.rb_carry_mode_3};
    Unbinder unbinder;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_base_setting, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();

        initEvent();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    private void initEvent() {
        baseNo = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_NUMBER, 3);
        time_first = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_TIME_FIRST, 10);
        time_span = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_TIME_SPAN, 10);
        carry_mode = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_CARRY, 0);

        spBaseNo.setSelection(baseNo - 1);
        rgCarryMode.check(rbCarry[carry_mode]);

        spBaseNo.setOnItemSelectedListener(this);
        rgCarryMode.setOnCheckedChangeListener(this);
        etMiddleRaceTime_Span.setText(time_span + "");
        etMiddleRaceTimeFirst.setText(time_first + "");
        etMiddleRaceTimeFirst.setSelection(String.valueOf(time_first).length());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        baseNo = position + 1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("BaseSettingFragment", "onPause--------------");
        time_first = TextUtils.isEmpty(etMiddleRaceTimeFirst.getText().toString()) ? FIRST_TIME : Integer.parseInt(etMiddleRaceTimeFirst.getText().toString());
        time_span = TextUtils.isEmpty(etMiddleRaceTime_Span.getText().toString()) ? SPAN_TIME : Integer.parseInt(etMiddleRaceTime_Span.getText().toString());
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MIDDLE_RACE_TIME_FIRST, time_first);
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MIDDLE_RACE_TIME_SPAN, time_span);
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MIDDLE_RACE_NUMBER, baseNo);
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MIDDLE_RACE_CARRY, carry_mode);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i = 0; i < rbCarry.length; i++) {
            if (rbCarry[i] == checkedId) {
                carry_mode = i;
                break;
            }
        }
    }
}
