package com.feipulai.testandroid.activity;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.testandroid.R;
import com.feipulai.testandroid.base.BaseMvpActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GripActivity extends BaseMvpActivity<GripPresenter> {
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
    private int currentFrequency;
    private int TARGET_FREQUENCY;
    private int currentDeviceId = 1;
    private final int NO_PAIR_RESPONSE_ARRIVED = 1;
    private final int GET_SCORE_RESPONSE = 2;
    private final int SEND_EMPTY = 3;
    private static final String TAG = "GripActivity";

    @Override
    public int getLayoutId() {
        return R.layout.activity_vc_pair;
    }

    @Override
    public void initView() {
        MachineCode.machineCode = ItemDefault.CODE_WLJ;
        TARGET_FREQUENCY = SerialConfigs.sProChannels.get(ItemDefault.CODE_WLJ) + 1 - 1;
        ButterKnife.bind(this);

        presenter = new GripPresenter();
        presenter.attachView(this);
        presenter.sendEmpty();

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
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
                presenter.verify();
                break;
        }
    }


}
