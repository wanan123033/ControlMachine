package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
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
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ItemCycleAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.GroupItemBean;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.utils.db.DataBaseExecutor;
import com.feipulai.exam.utils.db.DataBaseRespon;
import com.feipulai.exam.utils.db.DataBaseTask;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.feipulai.exam.config.SharedPrefsConfigs.FIRST_TIME;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_CARRY;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_DIGITAL;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_NUMBER;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_TIME_FIRST;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE_TIME_SPAN;
import static com.feipulai.exam.config.SharedPrefsConfigs.SPAN_TIME;

/**
 * created by ww on 2019/6/24.
 */
public class BaseSettingFragment extends Fragment implements AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener, ItemCycleAdapter.OnItemClickListener {
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
    @BindView(R.id.rv_race_cycles)
    RecyclerView rvRaceCycles;
    @BindView(R.id.rb_1)
    RadioButton rb1;
    @BindView(R.id.rb_2)
    RadioButton rb2;
    @BindView(R.id.rb_3)
    RadioButton rb3;
    @BindView(R.id.rg_digital)
    RadioGroup rgDigital;
    private Context mContext;
    private int baseNo;
    private int time_first;//首次接收时间
    private int time_span;//最小时间间隔
    private int carry_mode;//进位方式（0四舍五入1非零取整2非零进位）
    private int[] rbCarry = {R.id.rb_carry_mode_1, R.id.rb_carry_mode_2, R.id.rb_carry_mode_3};
    private int[] rbDigital = {R.id.rb_1, R.id.rb_2, R.id.rb_3};
    Unbinder unbinder;
    private List<Item> itemList;
    private ItemCycleAdapter itemCycleAdapter;
    //    private int carryMode;//进位（1.四舍五入 2.非零取整 3.非零进位）
    private int digital;//位数

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
        //默认为服务的设置
        carry_mode = TestConfigs.sCurrentItem.getCarryMode() == 0 ? 1 : TestConfigs.sCurrentItem.getCarryMode();
        digital = TestConfigs.sCurrentItem.getDigital() == 0 ? 1 : TestConfigs.sCurrentItem.getDigital();

        //如果本地修改了设置则本地设置优先
        baseNo = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_NUMBER, 3);
        time_first = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_TIME_FIRST, 10);
        time_span = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_TIME_SPAN, 10);
        carry_mode = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_CARRY, carry_mode);
        digital = SharedPrefsUtil.getValue(mContext, MIDDLE_RACE, MIDDLE_RACE_DIGITAL, digital);

        //如果从服务器下载的进位方式为0（不取舍），则必为千分位
        if (carry_mode == 0) {
            rgDigital.check(rbDigital[2]);
        } else {
            rgCarryMode.check(rbCarry[carry_mode - 1]);
            rgDigital.check(rbDigital[digital - 1]);
        }

        spBaseNo.setSelection(baseNo - 1);
        rgCarryMode.check(rbCarry[carry_mode - 1]);
        rgDigital.check(rbDigital[digital - 1]);


        spBaseNo.setOnItemSelectedListener(this);
        rgCarryMode.setOnCheckedChangeListener(this);
        rgDigital.setOnCheckedChangeListener(this);
        etMiddleRaceTime_Span.setText(time_span + "");
        etMiddleRaceTimeFirst.setText(time_first + "");
        etMiddleRaceTimeFirst.setSelection(String.valueOf(time_first).length());

        itemList = new ArrayList<>();
        itemList.addAll(DBManager.getInstance().queryItemsByMachineCode(TestConfigs.sCurrentItem.getMachineCode()));
        itemCycleAdapter = new ItemCycleAdapter(itemList);
        rvRaceCycles.setLayoutManager(new LinearLayoutManager(mContext));
        rvRaceCycles.setAdapter(itemCycleAdapter);
        itemCycleAdapter.setOnRecyclerViewItemClickListener(this);
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
        Log.i("BaseFragment", "onPause----------");
        time_first = TextUtils.isEmpty(etMiddleRaceTimeFirst.getText().toString()) ? FIRST_TIME : Integer.parseInt(etMiddleRaceTimeFirst.getText().toString());
        time_span = TextUtils.isEmpty(etMiddleRaceTime_Span.getText().toString()) ? SPAN_TIME : Integer.parseInt(etMiddleRaceTime_Span.getText().toString());
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MIDDLE_RACE_TIME_FIRST, time_first);
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MIDDLE_RACE_TIME_SPAN, time_span);
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MIDDLE_RACE_NUMBER, baseNo);
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MIDDLE_RACE_CARRY, carry_mode);
        SharedPrefsUtil.putValue(mContext, MIDDLE_RACE, MIDDLE_RACE_DIGITAL, digital);

        //本地修改了进位及小数位则数据库更新
        if (isChange) {
            List<Item> items = DBManager.getInstance().queryItemsByMachineCode(TestConfigs.sCurrentItem.getMachineCode());
            for (Item item : items
                    ) {
                item.setDigital(digital);
                item.setCarryMode(carry_mode);
            }
            DBManager.getInstance().updateItems(items);
        }
    }

    private boolean isChange = false;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getId()) {
            case R.id.rg_carry_mode:
                for (int i = 0; i < rbCarry.length; i++) {
                    if (rbCarry[i] == checkedId) {
                        carry_mode = i + 1;
                        ((MiddleRaceSettingActivity) getActivity()).setChange(true);
                        isChange = true;
                        break;
                    }
                }
                break;
            case R.id.rg_digital:
                for (int i = 0; i < rbDigital.length; i++) {
                    if (rbDigital[i] == checkedId) {
                        digital = i + 1;
                        ((MiddleRaceSettingActivity) getActivity()).setChange(true);
                        isChange = true;
                        break;
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemCycleLongClick(final int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_item_setting, null);
        final EditText etCycle = view.findViewById(R.id.et_cycle);
        final EditText etItemCode = view.findViewById(R.id.et_item_code);
        etItemCode.setText(itemList.get(position).getItemCode() == null ? "" : itemList.get(position).getItemCode());
        etCycle.setText(itemList.get(position).getCycleNo());
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(mContext);
        inputDialog.setTitle(itemList.get(position).getItemName()).setView(view);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MiddleRaceSettingActivity) getActivity()).setChange(true);

                        if (!TextUtils.isEmpty(etCycle.getText().toString())) {
                            itemList.get(position).setCycleNo(Integer.parseInt(etCycle.getText().toString()));
                        }

                        if (!TextUtils.isEmpty(etItemCode.getText().toString())) {
                            Item item = DBManager.getInstance().queryItemByCode(etItemCode.getText().toString());
                            if (item!=null&&!item.getItemName().equals(itemList.get(position).getItemName())) {
                                ToastUtils.showShort("不能设置相同项目代码");
                                return;
                            }
                            String oldItemCode = itemList.get(position).getItemCode();
                            itemList.get(position).setItemCode(etItemCode.getText().toString());
                            itemCycleAdapter.notifyDataSetChanged();
                            //更新item中的itemCode
                            DBManager.getInstance().updateItem(itemList.get(position));
                            updateAllItemCode(oldItemCode,etItemCode.getText().toString());
                        }

                    }
                }).show();
    }

    private void updateAllItemCode(final String oldItemCode, final String newItemCode) {
        DataBaseExecutor.addTask(new DataBaseTask(mContext, getString(R.string.loading_update), false) {
            @Override
            public DataBaseRespon executeOper() {
                //更新所有group中的itemCode
                List<Group> groups = DBManager.getInstance().queryGroupByItemCode(oldItemCode);
                for (Group group:groups
                     ) {
                    group.setItemCode(newItemCode);
                }
                DBManager.getInstance().updateGroups(groups);

                //更新所有groupItem中的itemCode
                List<GroupItem> groupItems = DBManager.getInstance().queryGroupItemByCode(oldItemCode);
                for (GroupItem groupItem:groupItems
                     ) {
                    groupItem.setItemCode(newItemCode);
                }
                DBManager.getInstance().updateGroupItems(groupItems);

                //

                return new DataBaseRespon(true, "", null);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                ToastUtils.showShort("数据更新完毕");
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }
}
