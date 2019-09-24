package com.feipulai.host.activity.jump_rope.base.result;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.printer.PrinterState;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.jump_rope.adapter.ResultDisplayAdapter;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.bean.TestCache;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class RadioResultActivity
        extends BaseTitleActivity implements PrinterManager.OnPrinterListener {

    public static final int BACK_TO_CHECK = 0x1;

    private static final int CHECK_PRINT_SERVICE = 0x02;

    @BindView(R.id.rv_results)
    RecyclerView mRvResults;

    private ResultDisplayAdapter mAdapter;

    private Handler mHandler = new MyHandler(this);
    private ItemSubscriber itemSubscriber;

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case CHECK_PRINT_SERVICE:
                if (SettingHelper.getSystemSetting().isAutoPrint()) {
                    printResult();
                }
                break;
        }
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_radio_result;
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle(R.string.radio_result_title);
    }

    @Override
    protected void initData() {
        mRvResults.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(false);
        dividerItemDecoration.setDrawBorderLeftAndRight(false);
        mRvResults.addItemDecoration(dividerItemDecoration);

        initAdapter();

        PrinterManager.getInstance().setOnPrinterListener(this);
        PrinterManager.getInstance().getState();

        if (SettingHelper.getSystemSetting().isAutoPrint()) {
            // 如果要打印成绩等1s检查打印机是否可用
            mHandler.sendEmptyMessageDelayed(CHECK_PRINT_SERVICE, 1000);
        }
        // Log.i("mIsUpload", mIsUpload + "");
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            itemSubscriber = new ItemSubscriber();
            if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                ToastUtils.showShort(R.string.upload_result_hint);
            } else {
                List<RoundResult> results = new ArrayList<>(TestCache.getInstance().getSaveResults().values());
                if (results.size() != 0) {
                    itemSubscriber.setDataUpLoad(results, this);
                }
            }
        }
    }


    private void initAdapter() {
        TestCache testCache = TestCache.getInstance();
        mAdapter = new ResultDisplayAdapter(testCache.getTestingPairs(),
                testCache.getSaveResults(), testCache.getBestResults());
        mRvResults.setAdapter(mAdapter);
    }

    @OnClick({R.id.btn_ok, R.id.btn_print})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_ok:
                finish();
                break;

            case R.id.btn_print:
                printResult();
                break;
        }
    }

    @Override
    public void onPrinterListener(Message msg) {
        if (msg.what == SerialConfigs.PRINTER_STATE) {
            PrinterState state = (PrinterState) msg.obj;
            if (state.isPaperLack()) {
                toastSpeak(getString(R.string.print_error_hint_1));
            } else if (state.isOverHeat()) {
                toastSpeak(getString(R.string.print_error_hint_2));
            } else {

            }
        }
    }

    @Override
    public void finish() {
        setResult(BACK_TO_CHECK);
        super.finish();
    }

    private void printResult() {
        TestCache testCache = TestCache.getInstance();
        InteractUtils.printResults(SettingHelper.getSystemSetting().getHostId(), testCache.getTestingPairs(),
                testCache.getSaveResults(), testCache.getBestResults());
    }

}
