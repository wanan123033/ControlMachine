package com.feipulai.exam.activity.MiddleDistanceRace;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ColorGroupAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.GridViewColorAdapter;
import com.feipulai.exam.entity.ChipGroup;
import com.zyyoona7.popup.EasyPopup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE;
import static com.feipulai.exam.config.SharedPrefsConfigs.VEST_CHIP_NO;

/**
 * created by ww on 2019/6/24.
 */
public class OtherSettingFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    @BindView(R.id.rv_color_group)
    RecyclerView rvColorGroup;
    @BindView(R.id.sp_vest_chip_no)
    Spinner spVestChipNo;
    @BindView(R.id.btn_clear_chip)
    Button btnClearChip;
    @BindView(R.id.btn_color_add)
    Button btnColorAdd;
    @BindView(R.id.btn_import_chip)
    Button btnImportChip;
    @BindView(R.id.btn_explore_chip)
    Button btnExploreChip;
    Unbinder unbinder;
    private ColorGroupAdapter colorGroupAdapter;
    private List<ChipGroup> colorGroups;
    private Context mContext;
    private int chipNo;//背心芯片数（目前仅1或者2）
    private EasyPopup mCirclePop;
    private View mView;
    private List<ColorSelectBean> colors;
    private GridViewColorAdapter colorAdapter;
    private GridView gvColor;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_other_setting, container, false);
        unbinder = ButterKnife.bind(this, mView);
        mContext = getActivity();
        initEvent();
        return mView;
    }


    private void initEvent() {
        chipNo = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, VEST_CHIP_NO, 2);

        spVestChipNo.setSelection(chipNo - 1);

        colorGroups = new ArrayList<>();
        colorGroupAdapter = new ColorGroupAdapter(colorGroups);
        rvColorGroup.setAdapter(colorGroupAdapter);


        colors=new ArrayList<>();
        colorAdapter = new GridViewColorAdapter(mContext, colors);

        spVestChipNo.setOnItemSelectedListener(this);

        ColorSelectBean colorSelectBean=new ColorSelectBean(R.color.swipe_color_1,false);
        ColorSelectBean colorSelectBean2=new ColorSelectBean(R.color.blue,false);
        ColorSelectBean colorSelectBean3=new ColorSelectBean(R.color.background_color,false);
        ColorSelectBean colorSelectBean4=new ColorSelectBean(R.color.colorAccent,false);
        ColorSelectBean colorSelectBean5=new ColorSelectBean(R.color.test_first_color,false);
        ColorSelectBean colorSelectBean6=new ColorSelectBean(R.color.viewfinder_frame,false);
        ColorSelectBean colorSelectBean7=new ColorSelectBean(R.color.viewfinder_laser,false);
        ColorSelectBean colorSelectBean8=new ColorSelectBean(R.color.blue_25,false);
        ColorSelectBean colorSelectBean9=new ColorSelectBean(R.color.green_yellow,false);

        colors.add(colorSelectBean);
        colors.add(colorSelectBean2);
        colors.add(colorSelectBean3);
        colors.add(colorSelectBean4);
        colors.add(colorSelectBean5);
        colors.add(colorSelectBean6);
        colors.add(colorSelectBean7);
        colors.add(colorSelectBean8);
        colors.add(colorSelectBean9);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_clear_chip, R.id.btn_color_add, R.id.btn_import_chip, R.id.btn_explore_chip})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_clear_chip:
                break;
            case R.id.btn_color_add:
                mCirclePop = EasyPopup.create()
                        .setContentView(mContext, R.layout.layout_pop_chip_group)
                        .setBackgroundDimEnable(false)
                        //是否允许点击PopupWindow之外的地方消失
                        .setFocusAndOutsideEnable(true)
                        .apply();
                mCirclePop.showAtLocation(getActivity().getWindow().getDecorView(),Gravity.CENTER,0,0);

                gvColor = mCirclePop.findViewById(R.id.gv_color);

                gvColor.setAdapter(colorAdapter);
                gvColor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        for (int i = 0; i < colors.size(); i++) {
                            if (i == position) {
                                colors.get(i).setSelect(true);
                            } else {
                                colors.get(i).setSelect(false);
                            }
                        }
                        colorAdapter.notifyDataSetChanged();
                    }
                });

                break;
            case R.id.btn_import_chip:
                break;
            case R.id.btn_explore_chip:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, VEST_CHIP_NO, chipNo);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        chipNo = position + 1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
