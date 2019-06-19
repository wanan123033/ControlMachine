package com.feipulai.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.netUtils.HttpManager;
import com.feipulai.host.netUtils.netapi.UserSubscriber;
import com.feipulai.host.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class SettingActivity extends BaseActivity implements TextWatcher{
	
	@BindView(R.id.et_test_name)
	EditText mEtTestName;
	@BindView(R.id.et_test_site)
	EditText mEtTestSite;
	@BindView(R.id.et_sever_ip)
	EditText mEtSeverIp;
	@BindView(R.id.sp_host_id)
	Spinner mSpHostId;
	@BindView(R.id.sw_auto_broadcast)
	Switch mSwAutoBroadcast;
	@BindView(R.id.sw_rt_upload)
	Switch mSwRtUpload;
	@BindView(R.id.sw_auto_print)
	Switch mSwAutoPrint;
	
	private int hostId;
	private List<Integer> hostIdList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_system_setting);
		ButterKnife.bind(this);
		
		hostId = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.HOST_ID,1);
		
		String serverAddress = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.IP_ADDRESS,TestConfigs
				.DEFAULT_IP_ADDRESS);
		String testName = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.TEST_NAME,"");
		String testSite = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.TEST_SITE,"");
		
		mEtTestName.setText(testName);
		mEtTestSite.setText(testSite);
		mEtSeverIp.setText(serverAddress);
		
		mEtTestSite.addTextChangedListener(this);
		mEtTestName.addTextChangedListener(this);
		mEtSeverIp.addTextChangedListener(this);
		
		hostIdList = new ArrayList<>();
		Integer maxHostId = ItemDefault.HOST_IDS_MAP.get(TestConfigs.sCurrentItem.getMachineCode());
		for (int i = 1; i <= maxHostId; i++) {
			hostIdList.add(i);
		}
		
		mSpHostId.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,hostIdList));
		mSpHostId.setSelection(hostId - 1);
		
		mSwAutoBroadcast.setChecked(SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.GRADE_BROADCAST,true));
		mSwRtUpload.setChecked(SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.REAL_TIME_UPLOAD,false));
		mSwAutoPrint.setChecked(SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.AUTO_PRINT,false));
	}
	
	@OnItemSelected({R.id.sp_host_id})
	public void spinnerItemSelected(Spinner spinner,int position){
		switch(spinner.getId()){
			
			case R.id.sp_host_id:
				hostId = position + 1;
				break;
			
		}
	}
	
	@OnClick({R.id.sw_auto_broadcast,R.id.sw_rt_upload,R.id.sw_auto_print,R.id.btn_bind,R.id.btn_default,R.id.btn_net_setting})
	public void onViewClicked(View view){
		switch(view.getId()){
			
			case R.id.sw_auto_broadcast:
				SharedPrefsUtil.putValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.GRADE_BROADCAST,mSwAutoBroadcast.isChecked());
				break;
			
			case R.id.sw_rt_upload:
				SharedPrefsUtil.putValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.REAL_TIME_UPLOAD,mSwRtUpload.isChecked());
				break;
			
			case R.id.sw_auto_print:
				SharedPrefsUtil.putValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.AUTO_PRINT,mSwAutoPrint.isChecked());
				break;
			
			case R.id.btn_bind:
				bind();
				break;
			
			case R.id.btn_default:
				mEtSeverIp.setText(TestConfigs.DEFAULT_IP_ADDRESS);
				SharedPrefsUtil.putValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.IP_ADDRESS,TestConfigs.DEFAULT_IP_ADDRESS);
				break;
			
			case R.id.btn_net_setting:
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				break;
			
		}
		
	}
	
	/**
	 * 绑定服务器
	 */
	private void bind(){
		String url = mEtSeverIp.getText().toString().trim() + "/app/";
		if(!url.startsWith("http")){//修改IP
			url = "http://" + url;
		}
		if (!NetWorkUtils.isValidUrl(url)) {
			toastSpeak("非法的服务器地址");
		    return;
		}
		HttpManager.getInstance().changeBaseUrl(url);
		UserSubscriber subscriber = new UserSubscriber();
		subscriber.takeBind(hostId,this);
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		SharedPrefsUtil.putValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.HOST_ID,hostId);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s,int start,int count,int after){}
	
	@Override
	public void onTextChanged(CharSequence s,int start,int before,int count){}
	
	@Override
	public void afterTextChanged(Editable s){
		SharedPrefsUtil.putValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.TEST_NAME,mEtTestName.getText().toString().trim());
		SharedPrefsUtil.putValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.TEST_SITE,mEtTestSite.getText().toString().trim());
		
		String url = mEtSeverIp.getText().toString().trim() + "/app/";
		if(!url.startsWith("http")){//修改IP
			url = "http://" + url;
		}
		if (!NetWorkUtils.isValidUrl(url)) {
			return;
		}
		SharedPrefsUtil.putValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.IP_ADDRESS,mEtSeverIp.getText().toString().trim());
	}
	
}
