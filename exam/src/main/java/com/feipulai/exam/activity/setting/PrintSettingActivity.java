package com.feipulai.exam.activity.setting;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.google.gson.Gson;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

/**
 * Created by zzs on  2020/8/24
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PrintSettingActivity extends BaseTitleActivity {


    @BindView(R.id.sp_result_type)
    Spinner spResultType;
    @BindView(R.id.rv_table)
    RecyclerView rvTable;
    @BindView(R.id.rv_sign)
    RecyclerView rvSign;

    private PrintSettingAdapter tableAdapter;
    private PrintSettingAdapter signAdapter;
    private PrintSetting printSetting;
    private Gson gson = new Gson();

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_print_setting;
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("A4打印设置");
    }

    @Override
    protected void initData() {
        printSetting = SharedPrefsUtil.loadFormSource(this, PrintSetting.class);
        if (printSetting == null) {
            printSetting = new PrintSetting();
        }
        tableAdapter = new PrintSettingAdapter(printSetting.getTableHeadleList());
        signAdapter = new PrintSettingAdapter(printSetting.getSignatureList());
        rvTable.setLayoutManager(new GridLayoutManager(this, 2));
        rvSign.setLayoutManager(new GridLayoutManager(this, 2));
        rvTable.setAdapter(tableAdapter);
        rvSign.setAdapter(signAdapter);

        ArrayAdapter spTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.print_A4_type)));
        spTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spResultType.setAdapter(spTypeAdapter);
    }

    @OnItemSelected(R.id.sp_result_type)
    public void spinnerItemSelected(int position) {

        printSetting.setPrintResultType(position);

    }

    @OnClick({R.id.txt_default, R.id.txt_write})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_default:
                break;
            case R.id.txt_write:
                if (tableAdapter.isWrite()) {//保存
                    tableAdapter.setWrite(false);
                    signAdapter.setWrite(false);
                    tableAdapter.notifyDataSetChanged();
                    signAdapter.notifyDataSetChanged();
                    printSetting.setSignatureJson(gson.toJson(signAdapter.getData()));
                    printSetting.setTableHeadleJson(gson.toJson(tableAdapter.getData()));
                } else {//编辑
                    tableAdapter.setWrite(true);
                    signAdapter.setWrite(true);
                    tableAdapter.notifyDataSetChanged();
                    signAdapter.notifyDataSetChanged();
                }

                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPrefsUtil.save(this, printSetting);
    }
}
