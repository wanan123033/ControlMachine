package com.feipulai.testandroid.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.testandroid.R;
import com.feipulai.testandroid.base.BaseMvpActivity;
import com.feipulai.testandroid.utils.ToastUtil;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GripActivity extends BaseMvpActivity<GripPresenter> implements GripContract.View{
    @BindView(R.id.btn_pair)
    TextView btnPair;
    @BindView(R.id.btn_pair_res)
    TextView btnPairRes;
    @BindView(R.id.btn_test)
    TextView btnTest;
    @BindView(R.id.btn_vc)
    TextView btnVc;
    @BindView(R.id.et_value)
    EditText etValue;
    @BindView(R.id.btn_adjust)
    TextView btnAdjust;
    private int TARGET_FREQUENCY;
    private int check;
    @Override
    public int getLayoutId() {
        return R.layout.activity_vc_pair;
    }

    @Override
    public void initView() {
        MachineCode.machineCode = ItemDefault.CODE_WLJ;
        TARGET_FREQUENCY = SerialConfigs.sProChannels.get(ItemDefault.CODE_WLJ) + 1 - 1;
        ButterKnife.bind(this);
        presenter = new GripPresenter(TARGET_FREQUENCY,this);
        presenter.attachView(this);
        presenter.sendEmpty();
    }

    @Override
    public void showLoading() {
        btnPairRes.setText("正在匹配");
    }

    @Override
    public void hideLoading() {
        btnPairRes.setText("匹配成功");
    }

    @Override
    public void setGripResult(int result){
        BigDecimal bigDecimal = new BigDecimal(result/1000.0);
        double round = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        btnVc.setText(round+"kg");
    }

    @Override
    public void onError(String err) {
        ToastUtil.show(this,err);
    }

    @OnClick({R.id.btn_pair, R.id.btn_test, R.id.btn_adjust})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_pair:
                presenter.deviceMatch();
                break;
            case R.id.btn_test:
                presenter.test();
                break;
            case R.id.btn_adjust:
                String ad = etValue.getText().toString().trim();
                if (TextUtils.isEmpty(ad)) {
                    ToastUtil.show(this, "必须输入校准值");
                    return;
                }
                double d = Double.valueOf(ad);
                check = (int) (d*10);
                if (check<= 0){
                    ToastUtil.show(this, "校准值必须大于0");
                    return;
                }
                presenter.verify(check);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}
