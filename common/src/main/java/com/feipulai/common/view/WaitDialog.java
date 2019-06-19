package com.feipulai.common.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.common.R;
import com.wang.avi.AVLoadingIndicatorView;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2018/1/8.
 */
public class WaitDialog extends Dialog{
	
	public Button btn;
	private TextView title;
	
	public WaitDialog(Context context){
		super(context, R.style.loadingDialogStyle);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_wait_handpair);
		AVLoadingIndicatorView aVi = (AVLoadingIndicatorView)this.findViewById(R.id.avi);
		aVi.show();
		RelativeLayout rl = (RelativeLayout)this.findViewById(R.id.ll_wait_dialog);
		rl.getBackground().setAlpha(210);
		btn = (Button)this.findViewById(R.id.btn_wait_pair);
		title = (TextView)findViewById(R.id.tv_wait_title);
	}
	
	/**
	 * 必须在弹框显示出来后再调用
	 * @param title
	 */
	@Override
	public void setTitle(CharSequence title){
		this.title.setText(title);
	}
	
}
