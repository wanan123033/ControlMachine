package com.feipulai.host.activity.sporttime;

import android.view.View;
import android.widget.TextView;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.R;
import com.feipulai.host.activity.situp.pair.SitPullPairActivity;
import com.feipulai.host.activity.situp.pair.SitUpPairPresenter;
import com.feipulai.host.config.TestConfigs;

import butterknife.BindView;


public class SportPairActivity extends SitPullPairActivity {

    @BindView(R.id.tv_init_way)
    public TextView txtInitWay;
    @Override
    public SitUpPairPresenter getPresenter() {
        return new SportTimerPairPresenter(this,this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setInitWayVisible();
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ){
            txtInitWay.setVisibility(View.GONE);
        }
    }
    public void setInitWayVisible(){
        txtInitWay.setVisibility(View.VISIBLE);
    }
}
