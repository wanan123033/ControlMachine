package com.feipulai.common.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.feipulai.common.R;

public class StopUseButton extends AppCompatButton {
	
	public StopUseButton(Context context) {
		super(context);
	}
	
	public StopUseButton(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	
	public StopUseButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
		if (text.equals("暂停使用")) {
			Drawable pause = getResources().getDrawable(R.drawable.pause);
			pause.setBounds( 0, 0, pause.getMinimumWidth(),pause.getMinimumHeight());
		    setCompoundDrawables(null,pause,null,null);
			setBackgroundColor(Color.parseColor("#1e90ff"));
		}else if(text.equals("恢复使用")){
			Drawable resume = getResources().getDrawable(R.drawable.resume);
			resume.setBounds( 0, 0, resume.getMinimumWidth(),resume.getMinimumHeight());
			setCompoundDrawables(null,resume,null,null);
		    setBackgroundColor(Color.parseColor("green"));
		}
		// Logger.i("StopUseButton.setText:" + text);
		super.setText(text, type);
	}
	
}
