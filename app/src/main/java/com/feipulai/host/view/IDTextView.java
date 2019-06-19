package com.feipulai.host.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by James on 2018/2/6 0006.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@SuppressLint("AppCompatCustomView")
public class IDTextView extends TextView{
	
	private String mText;
	
	public IDTextView(Context context){
		super(context);
	}
	
	public IDTextView(Context context,@Nullable AttributeSet attrs){
		super(context,attrs);
	}
	
	public IDTextView(Context context,@Nullable AttributeSet attrs,int defStyleAttr){
		super(context,attrs,defStyleAttr);
	}
	
	//public IDTextView(Context context,@Nullable AttributeSet attrs,int defStyleAttr,int defStyleRes){
	//	super(context,attrs,defStyleAttr,defStyleRes);
	//}
	
	
	@Override
	public void setText(CharSequence text,BufferType type){
		if(text != null && text.length() == 18){
			mText = text.toString();
			//将身份证号中间的出生日期模糊处理
			String id = mText.substring(0,6) + "********" + mText.substring(14);
			text = id;
		}
		super.setText(text,type);
	}
	
	@Override
	public String getText(){
		return mText;
	}
	
}
