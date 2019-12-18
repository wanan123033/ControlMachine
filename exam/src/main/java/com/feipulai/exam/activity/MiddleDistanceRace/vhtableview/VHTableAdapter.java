package com.feipulai.exam.activity.MiddleDistanceRace.vhtableview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.DialogUtil;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.RaceResultBean;
import com.feipulai.exam.netUtils.MGsonConverterFactory;

import java.util.ArrayList;

public class VHTableAdapter implements VHBaseAdapter {
    private Context context;
    private ArrayList<String> titleData;
    private ArrayList<RaceResultBean> dataList;
    private int carryMode;
    private int digital;
    private OnResultItemLongClick listener;

    public VHTableAdapter(Context context, ArrayList<String> titleData, ArrayList<RaceResultBean> dataList, int carryMode, int digital, OnResultItemLongClick longClickListener) {
        this.context = context;
        this.titleData = titleData;
        this.dataList = dataList;
        this.carryMode = carryMode;
        this.digital = digital;
        listener = longClickListener;
    }

    //表格内容的行数，不包括标题行
    @Override
    public int getContentRows() {
        return dataList.size();
    }

    //列数
    @Override
    public int getContentColumn() {
        return titleData.size();
    }

    //标题的view，这里从0开始，这里要注意，一定要有view返回去，不能为null，每一行
    // 各列的宽度就等于标题行的列的宽度，且边框的话，自己在这里和下文的表格单元格view里面设置
    @Override
    public View getTitleView(int columnPosition, ViewGroup parent) {

        TextView tv_item = new TextView(context);
        tv_item.setBackgroundResource(R.drawable.bg_shape_gray);
        if (0 == columnPosition) {
            tv_item.setWidth(50);
            tv_item.setPadding(5, 8, 5, 8);
            tv_item.setTextSize(14);
        } else {
            tv_item.setWidth(100);
            tv_item.setPadding(5, 8, 5, 8);
            tv_item.setTextSize(14);
        }
        tv_item.setHeight(50);
        tv_item.setText(titleData.get(columnPosition));
        tv_item.setGravity(Gravity.CENTER);
        tv_item.setTextColor(context.getResources().getColor(R.color.black));
        return tv_item;
    }

    //表格正文的view，行和列都从0开始，宽度的话在载入的时候，默认会是以标题行各列的宽度，高度的话自适应
    @Override
    public View getTableCellView(int contentRow, int contentColum, View view, ViewGroup parent) {
        if (null == view) {
            view = new TextView(context);
        }

        String content = dataList.get(contentRow).getResults()[contentColum];

        if (contentColum == 0) {
            view.setBackgroundResource(dataList.get(contentRow).getColor());
        } else {
            if (dataList.get(contentRow).isSelect()) {
                view.setBackgroundResource(R.color.green_yellow);
            } else {
                view.setBackgroundResource(R.drawable.bg_shape_gray);
            }
        }

        if (contentColum > dataList.get(contentRow).getCycle() + 2) {
            ((TextView) view).setText("X");
        } else {
            if (contentColum < 2) {
                ((TextView) view).setText(content);
            } else if (contentColum == 2) {
                switch (dataList.get(contentRow).getResultState()) {
                    case 1:
                        if (carryMode == 0) {
                            ((TextView) view).setText(TextUtils.isEmpty(content) ? "" : DateUtil.caculateTime(Long.parseLong(content), 3, carryMode));
                        } else {
                            ((TextView) view).setText(TextUtils.isEmpty(content) ? "" : DateUtil.caculateTime(Long.parseLong(content), digital, carryMode));
                        }
                        break;
                    case 2:
                        ((TextView) view).setText("DQ");
                        break;
                    case 3:
                        ((TextView) view).setText("DNF");
                        break;
                    case 4:
                        ((TextView) view).setText("DNS");
                        break;
                    case 5:
                        ((TextView) view).setText("DT");
                        break;
                }
            } else {
                if (carryMode == 0) {
                    ((TextView) view).setText(TextUtils.isEmpty(content) ? "" : DateUtil.caculateTime(Long.parseLong(content), 3, carryMode));
                } else {
                    ((TextView) view).setText(TextUtils.isEmpty(content) ? "" : DateUtil.caculateTime(Long.parseLong(content), digital, carryMode));
                }
            }
        }

        view.setPadding(5, 8, 5, 8);
        ((TextView) view).setTextSize(14);
        ((TextView) view).setGravity(Gravity.CENTER);
        //为了更灵活一些，在VHTableView没做设置边框，在这里通过背景实现，我这里的背景边框是顺手设的，要是想美观点的话，对应的边框做一下对应的设置就好
        ((TextView) view).setTextColor(context.getResources().getColor(R.color.black));
        return view;
    }


    @Override
    public Object getItem(int contentRow) {
        return dataList.get(contentRow);
    }


    //每一行被点击的时候的回调
    @Override
    public void OnClickContentRowItem(int row, View convertView) {
        Log.i("OnClickContentRowItem", "-----------------");
        listener.resultListClick(row);
    }

    @Override
    public void OnLongClickContentRowItem(int row, View convertView) {
        showSingSelect(row);
    }

    private int choice = -1;

    /**
     * 单选 dialog
     */
    private void showSingSelect(final int row) {
        //默认选中第一个
        final String[] items = {"正常", "DQ（犯规）", "DNF（未完成）", "DNS（放弃）", "DT（测试）"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("成绩状态（" + "姓名：" + dataList.get(row).getStudentName() + ")")
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        choice = i;
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.resultListLongClick(row, choice + 1);
                    }
                });
        builder.create().show();
    }

    public interface OnResultItemLongClick {
        void resultListLongClick(int row, int state);

        void resultListClick(int row);
    }
}
