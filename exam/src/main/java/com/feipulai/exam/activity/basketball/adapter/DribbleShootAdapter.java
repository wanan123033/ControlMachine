package com.feipulai.exam.activity.basketball.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.basketball.result.BasketBallResult;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2019/6/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DribbleShootAdapter extends RecyclerView.Adapter<DribbleShootAdapter.ViewHolder>{
    private Context mContext;
    private List<BasketBallResult> dataList;

    public DribbleShootAdapter(Context context ,List dataList){
        mContext = context;
        this.dataList = dataList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_basket_dribble, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.cbName.setText(dataList.get(i).getName());
        viewHolder.cbName.setChecked(dataList.get(i).isState());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbName;

        public ViewHolder(View itemView) {
            super(itemView);
            cbName = itemView.findViewById(R.id.cb_name);
        }
    }
}
