package com.feipulai.common.utils.print;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipulai.common.R;
import com.feipulai.common.dbutils.FileItemBean;
import com.feipulai.common.dbutils.FileSelectActivity;

import java.util.List;

public class PrintDataAdapter extends RecyclerView.Adapter<PrintDataAdapter.ViewHolder> {
    private LayoutInflater mInflater;

    private List<PrintBean.PrintDataBean> mPrintDataBeans;

    public PrintDataAdapter(Context context, List<PrintBean.PrintDataBean> printDataBeans) {
        mInflater = LayoutInflater.from(context);
        mPrintDataBeans = printDataBeans;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View convertView = mInflater.inflate(R.layout.item_a4_content, viewGroup, false);

        return new ViewHolder(convertView);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtTilte1.setText(mPrintDataBeans.get(position).getPrintString1());
        holder.txtTilte2.setText(mPrintDataBeans.get(position).getPrintString2());

        if (mPrintDataBeans.get(position).getPrintString3().length() > 4) {
            holder.txtTilte3.setTextSize(21f);
        } else {
            holder.txtTilte3.setTextSize(25f);
        }

        holder.txtTilte3.setText(mPrintDataBeans.get(position).getPrintString1());

        if (mPrintDataBeans.get(position).getPrintString4().length() > 8) {
            holder.txtTilte3.setTextSize(21f);
        } else {
            holder.txtTilte3.setTextSize(25f);
        }
        String printString4 = mPrintDataBeans.get(position).getPrintString4();

        if (TextUtils.equals(printString4, "\n")) {
            SpannableString spannableString = new SpannableString(printString4);
            int start = printString4.lastIndexOf("\n");
            spannableString.setSpan(new AbsoluteSizeSpan(17, true), start, printString4.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.txtTilte4.setText(spannableString);
        } else {
            holder.txtTilte4.setText(printString4);
        }

        holder.txtTilte5.setText(mPrintDataBeans.get(position).getPrintString5());
        holder.txtTilte6.setText(mPrintDataBeans.get(position).getPrintString6());
        holder.txtTilte7.setText(mPrintDataBeans.get(position).getPrintString7());
        holder.txtTilte8.setText(mPrintDataBeans.get(position).getPrintString8());
    }

    @Override
    public int getItemCount() {
        return mPrintDataBeans.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTilte1;
        private TextView txtTilte2;
        private TextView txtTilte3;
        private TextView txtTilte4;
        private TextView txtTilte5;
        private TextView txtTilte6;
        private TextView txtTilte7;
        private TextView txtTilte8;

        public ViewHolder(View view) {
            super(view);
            txtTilte1 = view.findViewById(R.id.item_txt_title1);
            txtTilte2 = view.findViewById(R.id.item_txt_title2);
            txtTilte3 = view.findViewById(R.id.item_txt_title3);
            txtTilte4 = view.findViewById(R.id.item_txt_title4);
            txtTilte5 = view.findViewById(R.id.item_txt_title5);
            txtTilte6 = view.findViewById(R.id.item_txt_title6);
            txtTilte7 = view.findViewById(R.id.item_txt_title7);
            txtTilte8 = view.findViewById(R.id.item_txt_title8);
        }
    }

}


