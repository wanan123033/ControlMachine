package com.feipulai.exam.activity.MiddleDistanceRace;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.DisplayUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ColorGroupAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.GridViewColorAdapter;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.ChipGroup;
import com.feipulai.exam.entity.ChipInfo;
import com.zyyoona7.popup.EasyPopup;

import java.util.ArrayList;
import java.util.Iterator;
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
public class OtherSettingFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, ColorGroupAdapter.OnItemClickListener {
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
    private int groupColor = 0;
    private EditText etGroupName;
    private TextView tvGroupColor;
    private Spinner spGroupStyle;
    private EditText etGroupNo;
    private Button btnCancel;
    private Button btnSure;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_other_setting, container, false);
        unbinder = ButterKnife.bind(this, mView);
        mContext = getActivity();
        initEvent();
        return mView;
    }


    private void initEvent() {
        colorGroups = new ArrayList<>();
        chipNo = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, VEST_CHIP_NO, 2);

        spVestChipNo.setSelection(chipNo - 1);

        colorGroups.addAll(DBManager.getInstance().queryAllChipGroup());
        colorGroupAdapter = new ColorGroupAdapter(colorGroups);
        rvColorGroup.setLayoutManager(new LinearLayoutManager(mContext));
        rvColorGroup.setAdapter(colorGroupAdapter);

        colorGroupAdapter.setOnRecyclerViewItemClickListener(this);


        int height = DisplayUtil.getScreenHightPx(mContext);
        int width = DisplayUtil.getScreenWidthPx(mContext);
        colors = new ArrayList<>();

        ColorSelectBean colorSelectBean = new ColorSelectBean(R.color.swipe_color_1, false);
        ColorSelectBean colorSelectBean2 = new ColorSelectBean(R.color.blue, false);
        ColorSelectBean colorSelectBean3 = new ColorSelectBean(R.color.background_color, false);
        ColorSelectBean colorSelectBean4 = new ColorSelectBean(R.color.colorAccent, false);
        ColorSelectBean colorSelectBean5 = new ColorSelectBean(R.color.test_first_color, false);
        ColorSelectBean colorSelectBean6 = new ColorSelectBean(R.color.viewfinder_frame, false);
        ColorSelectBean colorSelectBean7 = new ColorSelectBean(R.color.viewfinder_laser, false);
        ColorSelectBean colorSelectBean8 = new ColorSelectBean(R.color.blue_25, false);
        ColorSelectBean colorSelectBean9 = new ColorSelectBean(R.color.green_yellow, false);

        colors.add(colorSelectBean);
        colors.add(colorSelectBean2);
        colors.add(colorSelectBean3);
        colors.add(colorSelectBean4);
        colors.add(colorSelectBean5);
        colors.add(colorSelectBean6);
        colors.add(colorSelectBean7);
        colors.add(colorSelectBean8);
        colors.add(colorSelectBean9);
        colorAdapter = new GridViewColorAdapter(mContext, colors);

        spVestChipNo.setOnItemSelectedListener(this);

        mCirclePop = EasyPopup.create()
                .setContentView(mContext, R.layout.layout_pop_chip_group)
                .setBackgroundDimEnable(true)
                .setDimValue(0.5f)
                //是否允许点击PopupWindow之外的地方消失
                .setFocusAndOutsideEnable(false)
                .setHeight(height * 3 / 4)
                .setWidth(width * 4 / 5)
                .apply();

        gvColor = mCirclePop.findViewById(R.id.gv_color);
        etGroupName = mCirclePop.findViewById(R.id.et_color_group_name);
        tvGroupColor = mCirclePop.findViewById(R.id.tv_group_color);
        spGroupStyle = mCirclePop.findViewById(R.id.sp_group_style);
        etGroupNo = mCirclePop.findViewById(R.id.et_color_group_no);
        btnCancel = mCirclePop.findViewById(R.id.btn_color_cancel);
        btnSure = mCirclePop.findViewById(R.id.btn_color_sure);
        btnCancel.setOnClickListener(this);
        btnSure.setOnClickListener(this);
        spGroupStyle.setOnItemSelectedListener(this);

        gvColor.setAdapter(colorAdapter);
        gvColor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                groupColor = colors.get(position).getColorId();
                tvGroupColor.setBackgroundResource(groupColor);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, VEST_CHIP_NO, chipNo);
    }

    @OnClick({R.id.btn_clear_chip, R.id.btn_color_add, R.id.btn_import_chip, R.id.btn_explore_chip})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_clear_chip:
                DialogUtil.showCommonDialog(mContext, "是否清空芯片及颜色组所有信息", new DialogUtil.DialogListener() {
                    @Override
                    public void onPositiveClick() {
                        DBManager.getInstance().deleteAllChip();
                        colorGroups.clear();
                        colorGroupAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
                break;
            case R.id.btn_color_add:
                mCirclePop.showAtLocation(mView, Gravity.CENTER, 0, 0);
                break;
            case R.id.btn_import_chip:
                break;
            case R.id.btn_explore_chip:
                break;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
//            colorGroups.clear();
//            colorGroups.addAll(DBManager.getInstance().queryAllChipGroup());
//            colorGroupAdapter.notifyDataSetChanged();
        }
    }

    private int groupStyle = 0;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_vest_chip_no:
                chipNo = position + 1;
                break;
            case R.id.sp_group_style:
                groupStyle = position;
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private List<ChipInfo> chipInfos;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_color_cancel:
                mCirclePop.dismiss();
                break;
            case R.id.btn_color_sure:
                String groupName = etGroupName.getText().toString();
                String groupNo = etGroupNo.getText().toString();
                if (groupColor == 0 || TextUtils.isEmpty(groupName) || TextUtils.isEmpty(groupNo)) {
                    ToastUtils.showShort("添加内容为空");
                    break;
                }

                long no1 = DBManager.getInstance().queryChipGroup(groupName);
                long no2 = DBManager.getInstance().queryChipGroup(groupColor);
                if (no1 > 0) {
                    ToastUtils.showShort("该组组名已存在");
                    break;
                }
                if (no2 > 0) {
                    ToastUtils.showShort("该组颜色已存在");
                    break;
                }
                //保存颜色组到数据库
                ChipGroup chipGroup = new ChipGroup();
                chipGroup.setColor(groupColor);
                chipGroup.setColorGroupName(groupName);
                chipGroup.setGroupType(groupStyle);
                chipGroup.setStudentNo(Integer.parseInt(groupNo));
                DBManager.getInstance().insertChipGroup(chipGroup);

                //保存芯片信息到数据库（颜色组创建后芯片组根据颜色组信息自动创建）
                chipInfos = new ArrayList<>();
                ChipInfo chipInfo;
                for (int i = 0; i < Integer.parseInt(groupNo); i++) {
                    chipInfo = new ChipInfo();
                    chipInfo.setColorGroupName(groupName);
                    chipInfo.setVestNo(i + 1);
                    chipInfo.setColor(groupColor);
                    chipInfos.add(chipInfo);
                }
                DBManager.getInstance().insertChipInfos(chipInfos);

                colorGroups.add(chipGroup);
                colorGroupAdapter.notifyDataSetChanged();
                mCirclePop.dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onColorGroupLongClick(final int position) {
        final String groupName = colorGroups.get(position).getColorGroupName();
        Log.i("groupName","---------"+groupName);
        final List<ChipInfo> chips = DBManager.getInstance().queryChipInfoHasChipID(groupName);
        String text;
        if (chips != null && chips.size() > 0) {
            text = groupName + "组已绑定芯片ID，将会一起清除";
        } else {
            text = "是否删除" + groupName + "组";
        }
        DialogUtil.showCommonDialog(mContext, text, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                DBManager.getInstance().deleteChipGroup(colorGroups.get(position));
                Iterator<ChipGroup> it = colorGroups.iterator();
                while (it.hasNext()) {
                    String x = it.next().getColorGroupName();
                    if (x.equals(groupName)) {
                        it.remove();
                    }
                }
                colorGroupAdapter.notifyDataSetChanged();
                DBManager.getInstance().deleteChipInfo(groupName);
            }

            @Override
            public void onNegativeClick() {

            }
        });
    }
}
