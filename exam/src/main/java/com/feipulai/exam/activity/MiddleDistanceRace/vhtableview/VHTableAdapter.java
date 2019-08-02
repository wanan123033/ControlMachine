package com.feipulai.exam.activity.MiddleDistanceRace.vhtableview;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.RaceResultBean;

import java.util.ArrayList;

public class VHTableAdapter implements VHBaseAdapter {
    private Context context;
    private ArrayList<String> titleData;
    private ArrayList<RaceResultBean> dataList;
    private int carryMode;
    private int digital;

    public VHTableAdapter(Context context, ArrayList<String> titleData, ArrayList<RaceResultBean> dataList, int carryMode, int digital) {
        this.context = context;
        this.titleData = titleData;
        this.dataList = dataList;
        this.carryMode = carryMode;
        this.digital = digital;
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
            view.setBackgroundResource(R.drawable.bg_shape_gray);
        }

        if (contentColum > dataList.get(contentRow).getCycle() + 2) {
            ((TextView) view).setText("X");
        } else {
            if (contentColum > 1) {
                if (carryMode == 0) {
                    ((TextView) view).setText(TextUtils.isEmpty(content) ? "" : DateUtil.caculateTime(Long.parseLong(content), 3, carryMode));
                } else {
                    ((TextView) view).setText(TextUtils.isEmpty(content) ? "" : DateUtil.caculateTime(Long.parseLong(content), digital, carryMode));
                }
            } else {
                ((TextView) view).setText(content);
            }
        }

        view.setPadding(5, 8, 5, 8);
        ((TextView) view).setTextSize(14);
        ((TextView) view).setGravity(Gravity.CENTER);
        //为了更灵活一些，在VHTableView没收做设置边框，在这里通过背景实现，我这里的背景边框是顺手设的，要是想美观点的话，对应的边框做一下对应的设置就好
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
    }


}
