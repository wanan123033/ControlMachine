package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.GroupingAdapter;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.ItemTouchHelperCallback;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.ChipGroup;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Student;
import com.zyyoona7.popup.EasyPopup;

import java.util.ArrayList;
import java.util.List;

/**
 * created by ww on 2020/7/3.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AddGroupPop {

    private Context context;
    private EasyPopup groupPop;
    private GroupPopListener listener;
    private String itemCode;

    public AddGroupPop(Context context, String itemCode, int height, int width, GroupPopListener listener) {
        this.context = context;
        this.listener = listener;
        this.itemCode = itemCode;
        initWindow(height, width);
    }

    private EditText groupInput;
    private TextView groupingItem;
    private TextView tvGroupeNo;
    private RecyclerView rvGrouping;
    private List<Student> groupIngStudents = new ArrayList<>();
    private GroupingAdapter groupingAdapter;
    private List<Group> groups;

    private void initWindow(int height, int width) {
        groupPop = EasyPopup.create()
                .setContentView(context, R.layout.pop_group)
                .setBackgroundDimEnable(true)
                .setDimValue(0.5f)
                //是否允许点击PopupWindow之外的地方消失
                .setFocusAndOutsideEnable(false)
                .setHeight(height*4/5)
                .setWidth(width*4/5)
                .apply();

        groupInput = groupPop.findViewById(R.id.et_group_input);
        Button btnQuery = groupPop.findViewById(R.id.btn_group_query);
        groupingItem = groupPop.findViewById(R.id.tv_grouping_item);
        rvGrouping = groupPop.findViewById(R.id.rv_grouping);
        tvGroupeNo = groupPop.findViewById(R.id.tv_grouping_no);
        Button btnCancel = groupPop.findViewById(R.id.btn_cancel);
        Button btnSure = groupPop.findViewById(R.id.btn_sure);

        groupingAdapter = new GroupingAdapter(context, groupIngStudents);
        rvGrouping.setLayoutManager(new LinearLayoutManager(context));
        rvGrouping.setAdapter(groupingAdapter);

        ItemTouchHelperCallback helperCallback = new ItemTouchHelperCallback(groupingAdapter);
        helperCallback.setSwipeEnable(true);
        helperCallback.setDragEnable(true);
        ItemTouchHelper helper = new ItemTouchHelper(helperCallback);
        helper.attachToRecyclerView(rvGrouping);

        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(groupInput.getText())) {
                    return;
                }
                listener.queryStudent(groupInput.getText().toString());
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupIngStudents.clear();
                groupingAdapter.notifyDataSetChanged();
                groupInput.setText("");
                groupPop.dismiss();
            }
        });

        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupIngStudents.isEmpty()) {
                    groupPop.dismiss();
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
                groupInput.setText("");
                groupPop.dismiss();
                listener.addGroup();
            }
        });
    }

    public void showGroupPop(String itemName, Activity activity) {
        if (groupPop!=null&&!groupPop.isShowing()) {
            groupingItem.setText(itemName);
            groups = DBManager.getInstance().queryGroup(itemCode);
            tvGroupeNo.setText(String.valueOf(groups.size() + 1) + "组");
            groupPop.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        }
    }

    public void addStudent(Student student) {
        if (student == null) {
            groupInput.setText("");
            return;
        }
        List<ChipGroup> chips = DBManager.getInstance().queryChipGroups();
        if (!chips.isEmpty() && chips.get(0).getStudentNo() <= groupIngStudents.size()) {
            ToastUtils.showShort("已达最大人数");
            return;
        }

        if (groupInput != null) {
            groupInput.setText(student.getStudentCode());
            groupInput.setSelection(student.getStudentCode().length());
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

    interface GroupPopListener {
        void queryStudent(String code);

        void addGroup();
    }
}
