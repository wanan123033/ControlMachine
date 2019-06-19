package com.feipulai.host.activity.jump_rope.base.result;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.printer.PrinterState;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.jump_rope.adapter.ResultDisplayAdapter;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.bean.TestCache;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;
import com.feipulai.host.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RadioResultActivity
		extends BaseActivity implements PrinterManager.OnPrinterListener {
	
	public static final int BACK_TO_CHECK = 0x1;
	
	private static final int CHECK_PRINT_SERVICE = 0x02;
	
	@BindView(R.id.rv_results)
	RecyclerView mRvResults;
	
	private ResultDisplayAdapter mAdapter;
	private boolean needPrint;
	
	private Handler mHandler = new MyHandler(this);
	private boolean mIsUpload;
	private ItemSubscriber itemSubscriber;
	
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case CHECK_PRINT_SERVICE:
				if (needPrint) {
					printResult();
				}
				break;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radio_result);
		ButterKnife.bind(this);
		init();
	}
	
	private void init() {
		needPrint = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.AUTO_PRINT, false);
		mIsUpload = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.REAL_TIME_UPLOAD, false);
		
		mRvResults.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
		dividerItemDecoration.setDrawBorderTopAndBottom(false);
		dividerItemDecoration.setDrawBorderLeftAndRight(false);
		mRvResults.addItemDecoration(dividerItemDecoration);
		
		initAdapter();
		
		PrinterManager.getInstance().setOnPrinterListener(this);
		PrinterManager.getInstance().getState();
		
		if (needPrint) {
			// 如果要打印成绩等1s检查打印机是否可用
			mHandler.sendEmptyMessageDelayed(CHECK_PRINT_SERVICE, 1000);
		}
		// Log.i("mIsUpload", mIsUpload + "");
		if (mIsUpload) {
			itemSubscriber = new ItemSubscriber();
			if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
				ToastUtils.showShort("自动上传成绩需先下载更新项目信息");
			}else {
				List<RoundResult> results = new ArrayList<>(TestCache.getInstance().getSaveResults().values());
				if (results.size() != 0) {
					itemSubscriber.setDataUpLoad(results,this);
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
				toastSpeak("打印机缺纸");
			} else if (state.isOverHeat()) {
				toastSpeak("打印机过热");
			} else {
				needPrint = true;
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
		InteractUtils.printResults(hostId, testCache.getTestingPairs(),
				testCache.getSaveResults(), testCache.getBestResults());
	}
	
}
