package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Toast;

import com.feipulai.common.dbutils.FileSelectActivity;
import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.DisplayUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ColorGroupAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.GridViewColorAdapter;
import com.feipulai.exam.activity.data.DataRetrieveActivity;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.ChipGroup;
import com.feipulai.exam.entity.ChipInfo;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.exl.ColorGroupExLReader;
import com.feipulai.exam.view.OperateProgressBar;
import com.orhanobut.logger.Logger;
import com.zyyoona7.popup.EasyPopup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_CANCELED;
import static com.feipulai.exam.activity.MiddleDistanceRace.bean.MiddleBean.colorIds;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE;
import static com.feipulai.exam.config.SharedPrefsConfigs.VEST_CHIP_NO;

/**
 * created by ww on 2019/6/24.
 */
public class OtherSettingFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, ColorGroupAdapter.OnItemClickListener, ExlListener {
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
    private AlertDialog.Builder builder;
    private boolean isAddFlag = true;//判断是否是编辑还是新增的标识

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
        ColorSelectBean colorSelectBean;
        for (int id : colorIds
                ) {
            colorSelectBean = new ColorSelectBean(id, false);
            colors.add(colorSelectBean);
        }
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
                        ((MiddleRaceSettingActivity) getActivity()).setChange(true);

                        DBManager.getInstance().deleteAllChip();
                        colorGroups.clear();
                        colorGroupAdapter.notifyDataSetChanged();

                        //清除所有颜色组时，先前绑定的项目组必须一起清除关联关系
                        List<Group> groups = DBManager.getInstance().loadAllGroup();
                        for (Group group : groups
                                ) {
                            if (!TextUtils.isEmpty(group.getColorGroupName())) {
                                group.setColorGroupName("");
                                group.setColorId("");
                                group.setIsTestComplete(0);
                            }
                        }
                        DBManager.getInstance().updateGroups(groups);
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
                break;
            case R.id.btn_color_add:
                isAddFlag = true;
                tvGroupColor.setBackgroundColor(Color.TRANSPARENT);
                mCirclePop.showAtLocation(mView, Gravity.CENTER, 0, 0);
                break;
            case R.id.btn_import_chip:
                DialogUtil.showCommonDialog(mContext, "当前所有芯片分组及颜色组将删除", new DialogUtil.DialogListener() {
                    @Override
                    public void onPositiveClick() {
                        DBManager.getInstance().deleteAllChip();
                        colorGroups.clear();
                        colorGroupAdapter.notifyDataSetChanged();
                        Intent intent = new Intent();
                        intent.setClass(mContext, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_FILE);
                        startActivityForResult(intent, 1);
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
                break;
            case R.id.btn_explore_chip:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == 1) {
            OperateProgressBar.showLoadingUi(getActivity(), "正在读取exel文件...");
            Logger.i(" exel文件导入");
            Logger.i("保存路径：" + FileSelectActivity.sSelectedFile);
            new ColorGroupExLReader(this).readExlData(FileSelectActivity.sSelectedFile);
        }
    }

    @Override
    public void onExlResponse(final int responseCode, final String reason) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (responseCode) {
                    case ExlListener.EXEL_READ_SUCCESS:
                        OperateProgressBar.removeLoadingUiIfExist(getActivity());
                        ToastUtils.showShort(reason);
                        Intent intent = new Intent(DataRetrieveActivity.UPDATE_MESSAGE);
                        getActivity().sendBroadcast(intent);

                        colorGroups.addAll(DBManager.getInstance().queryAllChipGroup());
                        colorGroupAdapter.notifyDataSetChanged();
                        break;

                    case ExlListener.EXEL_READ_FAIL:
                        OperateProgressBar.removeLoadingUiIfExist(getActivity());
                        ToastUtils.showShort(reason);
                        break;

                    case ExlListener.EXEL_WRITE_SUCCESS:
                        OperateProgressBar.removeLoadingUiIfExist(getActivity());
                        ToastUtils.showShort(reason);
                        break;

                    case ExlListener.EXEL_WRITE_FAILED:
                        OperateProgressBar.removeLoadingUiIfExist(getActivity());
                        Toast.makeText(mContext, reason, Toast.LENGTH_SHORT).show();
                        ToastUtils.showShort(reason);
                        break;
                }
            }

        });
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_color_cancel:
                mCirclePop.dismiss();
                break;
            case R.id.btn_color_sure:
                if (isAddFlag) {
                    addColorGroup();
                } else {
                    editColorGroup();
                }

                mCirclePop.dismiss();
                break;
            default:
                break;
        }
    }

    //新增颜色组
    private void addColorGroup() {
        String groupName = etGroupName.getText().toString();
        String groupNo = etGroupNo.getText().toString();
        if (groupColor == 0 || TextUtils.isEmpty(groupName) || TextUtils.isEmpty(groupNo)) {
            ToastUtils.showShort("添加内容为空");
            return;
        }

        long no1 = DBManager.getInstance().queryChipGroup(groupName);
        long no2 = DBManager.getInstance().queryChipGroup(groupColor);
        if (no1 > 0) {
            ToastUtils.showShort("该组组名已存在");
            return;
        }
        if (no2 > 0) {
            ToastUtils.showShort("该组颜色已存在");
            return;
        }
        //保存颜色组到数据库
        ChipGroup chipGroup = new ChipGroup();
        chipGroup.setColor(groupColor);
        chipGroup.setColorGroupName(groupName);
        chipGroup.setGroupType(groupStyle);
        chipGroup.setStudentNo(Integer.parseInt(groupNo));
        DBManager.getInstance().insertChipGroup(chipGroup);

        //保存芯片信息到数据库（颜色组创建后芯片组根据颜色组信息自动创建）
        ArrayList<ChipInfo> chipInfos = new ArrayList<>();
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
    }

    //编辑颜色组
    private void editColorGroup() {
        String groupName = etGroupName.getText().toString();
        String groupNo = etGroupNo.getText().toString();
        if (groupColor == 0 || TextUtils.isEmpty(groupName) || TextUtils.isEmpty(groupNo)) {
            ToastUtils.showShort("添加内容为空");
            return;
        }

        if (groupName.equals(firstColorName) && groupColor == firstColorId && Integer.parseInt(groupNo) == firstNo) {
            return;
        }

        long no1 = DBManager.getInstance().queryChipGroup(groupName);
        long no2 = DBManager.getInstance().queryChipGroup(groupColor);
        if (no1 > 0 && !groupName.equals(firstColorName)) {
            ToastUtils.showShort("该组组名已存在");
            return;
        }
        if (no2 > 0 && groupColor != firstColorId) {
            ToastUtils.showShort("该组颜色已存在");
            return;
        }

        colorGroups.get(colorPosition).setColor(groupColor);
        colorGroups.get(colorPosition).setStudentNo(Integer.parseInt(groupNo));
        colorGroups.get(colorPosition).setColorGroupName(groupName);
        colorGroups.get(colorPosition).setGroupType(groupStyle);
        DBManager.getInstance().updateChipGroup(colorGroups.get(colorPosition));

        //查询所有当前编辑的颜色绑定的芯片信息
        List<ChipInfo> chipInfos = DBManager.getInstance().queryChipInfoByColor(firstColorName);
        //替换掉芯片信息表中原来的颜色和颜色组名
        for (ChipInfo chip : chipInfos
                ) {
            chip.setColorGroupName(groupName);
            chip.setColor(groupColor);
        }


        //当编辑后人数减少，需要删除芯片信息
        if (firstNo > Integer.parseInt(groupNo)) {
            DBManager.getInstance().updateChipInfo(chipInfos);
            DBManager.getInstance().deleteSomeChipInfos(chipInfos.subList(Integer.parseInt(groupNo), firstNo));
        } else {//当编辑后人数增加，需要增加到芯片信息
            ChipInfo chipInfo;
            for (int i = firstNo; i < Integer.parseInt(groupNo); i++) {
                chipInfo = new ChipInfo();
                chipInfo.setColorGroupName(groupName);
                chipInfo.setVestNo(i + 1);
                chipInfo.setColor(groupColor);
                chipInfos.add(chipInfo);
            }
            DBManager.getInstance().insertChipInfos2(chipInfos);
        }

        colorGroupAdapter.notifyDataSetChanged();
    }

    private int colorPosition;

    @Override
    public void onColorGroupLongClick(final int position) {
        colorPosition = position;
        showListDialog(position);
    }

    private String firstColorName;//编辑之前的组名
    private int firstColorId;//编辑之前的颜色
    private int firstNo;//编辑之前的组人数

    /**
     * 列表 dialog
     */
    private void showListDialog(final int position) {
        final String[] items = {"编辑", "删除"};
        builder = new AlertDialog.Builder(mContext)
                .setTitle("当前组别：" + colorGroups.get(position).getColorGroupName())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                firstColorName = colorGroups.get(position).getColorGroupName();
                                firstColorId = colorGroups.get(position).getColor();
                                firstNo = colorGroups.get(position).getStudentNo();

                                isAddFlag = false;
                                mCirclePop.showAtLocation(mView, Gravity.CENTER, 0, 0);
                                etGroupName.setText(firstColorName);
                                etGroupNo.setText(firstNo + "");

                                groupColor = firstColorId;
                                tvGroupColor.setBackgroundResource(groupColor);
                                break;
                            case 1:
                                showDeleteDialog(position);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    private void showDeleteDialog(final int position) {
        final String groupName = colorGroups.get(position).getColorGroupName();
        List<ChipInfo> chips = DBManager.getInstance().queryChipInfoHasChipID(groupName);
        String text;
        if (chips != null && chips.size() > 0) {
            text = groupName + "组已绑定芯片ID，将会一起清除";
        } else {
            text = "是否删除" + groupName + "组";
        }
        DialogUtil.showCommonDialog(mContext, text, new DialogUtil.DialogListener() {
            @Override
            public void onPositiveClick() {
                ((MiddleRaceSettingActivity) getActivity()).setChange(true);

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

                //清除所有颜色组时，先前绑定的项目组必须一起清除关联关系
                List<Group> groups = DBManager.getInstance().queryGroupByColorName(groupName);
                for (Group group : groups
                        ) {
                    if (!TextUtils.isEmpty(group.getColorGroupName())) {
                        group.setColorGroupName("");
                        group.setColorId("");
                        group.setIsTestComplete(0);
                    }
                }
                DBManager.getInstance().updateGroups(groups);
            }

            @Override
            public void onNegativeClick() {

            }
        });
    }

}
