package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.common.utils.ScannerGunManager;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.GroupingAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ItemTouchHelperCallback;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.ChipGroup;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Student;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * created by ww on 2020/7/6.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GroupingDialog extends Dialog {
    TextView tvGroupingItem;
    TextView tvGroupNo;
    EditText etGroupInput;
    Button btnGroupQuery;
    RecyclerView rvGrouping;
    Button btnCancel;
    Button btnSure;
    private OnGroupPopListener listener;
    private Context context;
    private String itemCode;
    private List<Student> groupIngStudents = new ArrayList<>();
    private GroupingAdapter groupingAdapter;
    private List<Group> groups;
    private ScannerGunManager scannerGunManager;

    public GroupingDialog(@NonNull Context context) {
        super(context);
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setListener(OnGroupPopListener listener) {
        this.listener = listener;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.pop_group);
        setCanceledOnTouchOutside(false);

        scannerGunManager = new ScannerGunManager(new ScannerGunManager.OnScanListener() {
            @Override
            public void onResult(String code) {
                if (listener != null) {
                    listener.queryStudent(code);
                }
            }
        });

        initView();
        initListener();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("scan", "event= " + event);
        if (scannerGunManager != null && scannerGunManager.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void initListener() {
        btnGroupQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etGroupInput.getText())) {
                    return;
                }
                listener.queryStudent(etGroupInput.getText().toString());
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupIngStudents.clear();
                groupingAdapter.notifyDataSetChanged();
                etGroupInput.setText("");
                dismiss();
            }
        });

        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupIngStudents.isEmpty()) {
                    dismiss();
                    return;
                }
                Group group = new Group();
                group.setIsTestComplete(0);
                group.setItemCode(itemCode);
                group.setScheduleNo(DBManager.getInstance().queryItemSchedulesByItemCode(itemCode).get(0).getScheduleNo());
                group.setGroupNo(groups.size() + 1);
                group.setGroupType(groupIngStudents.get(0).getSex());
                group.setSortName("组");

                DBManager.getInstance().insertGroup(group);

                GroupItem groupItem;
                for (int i = 0; i < groupIngStudents.size(); i++) {
                    groupItem = new GroupItem();
                    groupItem.setItemCode(itemCode);
                    groupItem.setTrackNo(i + 1);
                    groupItem.setStudentCode(groupIngStudents.get(i).getStudentCode());
                    groupItem.setScheduleNo(group.getScheduleNo());
                    groupItem.setGroupNo(group.getGroupNo());
                    groupItem.setGroupType(group.getGroupType());
                    groupItem.setSortName("组");
                    DBManager.getInstance().insertGroupItem(groupItem);
                }

                groupIngStudents.clear();
                groupingAdapter.notifyDataSetChanged();
                etGroupInput.setText("");
                dismiss();
                listener.addGroup();
            }
        });
    }

    private void initView() {
        etGroupInput = findViewById(R.id.et_group_input);
        btnGroupQuery = findViewById(R.id.btn_group_query);
        tvGroupingItem = findViewById(R.id.tv_grouping_item);
        rvGrouping = findViewById(R.id.rv_grouping);
        tvGroupNo = findViewById(R.id.tv_group_no);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSure = findViewById(R.id.btn_sure);

        groupingAdapter = new GroupingAdapter(context, groupIngStudents);
        rvGrouping.setLayoutManager(new LinearLayoutManager(context));
        rvGrouping.setAdapter(groupingAdapter);

        ItemTouchHelperCallback helperCallback = new ItemTouchHelperCallback(groupingAdapter);
        helperCallback.setSwipeEnable(true);
        helperCallback.setDragEnable(true);
        ItemTouchHelper helper = new ItemTouchHelper(helperCallback);
        helper.attachToRecyclerView(rvGrouping);
    }

    public void showDialog(String itemName) {
        if (!isShowing()) {
            show();
            //设置宽度全屏，要设置在show的后面
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            getWindow().getDecorView().setPadding(0, 0, 0, 0);
            getWindow().setAttributes(layoutParams);
        }
        tvGroupingItem.setText(itemName);
        groups = DBManager.getInstance().queryGroup(itemCode);
        tvGroupNo.setText(String.valueOf(groups.size() + 1) + "组");
    }

    public void addStudent(Student student) {
        if (student == null) {
            etGroupInput.setText("");
            return;
        }
        List<ChipGroup> chips = DBManager.getInstance().queryChipGroups();
        if (!chips.isEmpty() && chips.get(0).getStudentNo() <= groupIngStudents.size()) {
            ToastUtils.showShort("已达最大人数");
            return;
        }

        if (etGroupInput != null) {
            etGroupInput.setText(student.getStudentCode());
            etGroupInput.setSelection(student.getStudentCode().length());
        }
        if (!groupIngStudents.contains(student)) {
            groupIngStudents.add(student);
            groupingAdapter.notifyDataSetChanged();
            smoothMoveToPosition(rvGrouping, groupIngStudents.size() - 1);
        } else {
            ToastUtils.showShort("已存在");
        }
    }

    /**
     * 滑动到指定位置
     */
    private void smoothMoveToPosition(RecyclerView mRecyclerView, final int position) {
        // 第一个可见位置
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // 最后一个可见位置
        int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
        if (position < firstItem) {
            // 第一种可能:跳转位置在第一个可见位置之前，使用smoothScrollToPosition
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // 第二种可能:跳转位置在第一个可见位置之后，最后一个可见项之前
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                // smoothScrollToPosition 不会有效果，此时调用smoothScrollBy来滑动到指定位置
                mRecyclerView.smoothScrollBy(0, top);
            }
        } else {
            // 第三种可能:跳转位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
            mRecyclerView.smoothScrollToPosition(position);
        }
    }

    public interface OnGroupPopListener {
        void queryStudent(String code);

        void addGroup();
    }
}
