package com.feipulai.common.utils;

import android.os.Build;
import android.text.InputType;
import android.widget.EditText;

import java.lang.reflect.Method;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/12/27.
 */

public class SoftInputUtil {
	/**
	 * 禁止Edittext弹出软键盘,光标依然正常显示。
	 * @param text
	 */
	public static void disableShowSoftInput(EditText text) {
		text.setFocusable(true);
		//text.setShowSoftInputOnFocus(false);
		if (Build.VERSION.SDK_INT <= 10) {
			text.setInputType(InputType.TYPE_NULL);
		} else {
			Class<EditText> cls = EditText.class;
			Method method;
			try {
				method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
				method.setAccessible(true);
				method.invoke(text, false);
			} catch (Exception e) {
			}
		}
	}
}
