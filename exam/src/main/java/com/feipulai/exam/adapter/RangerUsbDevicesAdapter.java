package com.feipulai.exam.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.ranger.usb.ListItem;
import com.feipulai.exam.activity.ranger.usb.RangerUsbDevicesActivity;

import java.util.List;
import java.util.Locale;

public class RangerUsbDevicesAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private final List<ListItem> listItems;
    private final Context context;
    private AdapterView.OnItemClickListener itemClickListener;

    public RangerUsbDevicesAdapter(Context context,List<ListItem> listItems) {
        this.listItems = listItems;
        this.context = context;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_list_item,viewGroup,false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder baseViewHolder, final int i) {
        TextView text1 = baseViewHolder.getView(R.id.text1);
        TextView text2 = baseViewHolder.getView(R.id.text2);
        final ListItem item = listItems.get(i);
        if(item.driver == null)
            text1.setText("<no driver>");
        else if(item.driver.getPorts().size() == 1)
            text1.setText(item.driver.getClass().getSimpleName().replace("SerialDriver",""));
        else
            text1.setText(item.driver.getClass().getSimpleName().replace("SerialDriver","")+", Port "+item.port);
        text2.setText(String.format(Locale.US, "Vendor %04X, Product %04X", item.device.getVendorId(), item.device.getProductId()));
        baseViewHolder.getView(R.id.ll_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null){
                    itemClickListener.onItemClick(null,baseViewHolder.getView(R.id.ll_item),i,i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
