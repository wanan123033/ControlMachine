package com.feipulai.host.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.entity.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2018/5/28 0028.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Deprecated
public class ItemSelectActivity extends BaseActivity{

	private int machineCode;
	private List<Tuple> mTupleList = new ArrayList<>();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_code_select);
		machineCode = getIntent().getIntExtra(SplashScreenActivity.MACHINE_CODE,0);
		// init();
	}

	// private void init(){
	//
	// 	List<Item> itemList =  DBManager.getInstance().queryItemsByMachineCode(machineCode);
	// 	for(int i = 0;i < itemList.size();i++){
	// 		Item item = itemList.get(i);
	// 		mTupleList.add(new Tuple(item.getItemCode(),item.getItemName()));
	// 	}
	//
	// 	RecyclerView recyclerView = findViewById(R.id.rv_item);
	// 	GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
	// 	recyclerView.setLayoutManager(layoutManager);
	// 	TupleAdapter adapter = new TupleAdapter(mTupleList);
	// 	recyclerView.setAdapter(adapter);
	//
	// 	adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
	//
	// 		@Override
	// 		public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
	// 			startActivity(mTupleList.get(position).getItemCode());
	// 		}
	// 	});
	// }
	//
	//
	// private void startActivity(String itemCode) {
	// 	// 更新保存的机器码和项目代码
	// 	SharedPrefsUtil.putValue(ItemSelectActivity.this, SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.MACHINE_CODE,machineCode);
	// 	SharedPrefsUtil.putValue(ItemSelectActivity.this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.ITEM_CODE,itemCode);
	// 	TestConfigs.init(machineCode,itemCode);
	//
	// 	SharedPrefsUtil.putValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.HOST_ID, 1);
	//
	// 	Intent intent = new Intent();
	// 	intent.setClass(ItemSelectActivity.this,MainActivity.class);
	// 	ActivityCollector.finishAll();
	// 	startActivity(intent);
	// }
	
}
