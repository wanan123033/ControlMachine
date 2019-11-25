package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.entity.Student;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * created by ww on 2019/6/12.
 */
public class GroupingAdapter extends RecyclerView.Adapter<GroupingAdapter.VH> implements ItemTouchHelperListener {


    private List<Student> students;
    private Context mContext;
    private AlertDialog alertDialog;

    @Override
    public void onMove(int fromPosition, int toPosition) {
        //原始数据移动
        Collections.swap(students, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    private int positionDel;

    @Override
    public void onSwipe(int position) {
        positionDel = position;
        Log.i("onSwipe", "----------" + position);
        popAlertDialog();
    }

    @Override
    public void onClear() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    private void popAlertDialog() {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            alertDialog = builder
                    .setMessage("确定删除？")
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ItemTouchHelperCallback.isIdle = false;
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ItemTouchHelperCallback.isIdle = false;
                            students.remove(positionDel);
                            notifyItemRemoved(positionDel);
                            dialog.dismiss();
                        }
                    })
                    .create();
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    Log.i("notifyDataSetChanged", "----------" + students.toString());
                    notifyDataSetChanged();
                }
            });
        }
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    //创建ViewHolder
    public class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_grouping_name)
        TextView tvGroupingName;
        @BindView(R.id.tv_grouping_code)
        TextView tvGroupingCode;
        @BindView(R.id.tv_grouping_no)
        TextView tvGroupingNo;
        @BindView(R.id.ll_grouping)
        LinearLayout llGrouping;

        public VH(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }


    public GroupingAdapter(Context context, List<Student> data) {
        this.students = data;
        mContext = context;
        Log.i("students", "---" + students.toString());
    }

    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        holder.tvGroupingNo.setText(position + 1 + "");
        holder.tvGroupingName.setText(students.get(position).getStudentName());
        holder.tvGroupingCode.setText(students.get(position).getStudentCode());

        if (position == students.size() - 1) {
            holder.llGrouping.setBackgroundResource(R.color.blue);
        } else {
            holder.llGrouping.setBackgroundResource(R.color.white);
        }
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_student, parent, false);
        return new VH(v);
    }
}
