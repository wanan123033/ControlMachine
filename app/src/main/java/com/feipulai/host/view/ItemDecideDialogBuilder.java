package com.feipulai.host.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.feipulai.host.entity.Item;

import java.util.List;

/**
 * Created by James on 2018/10/25.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ItemDecideDialogBuilder extends AlertDialog.Builder{
	
	private List<Item> mItems;
	
	/**
	 * 当前机器码对应多个项目代码时,且已保存了当前的项目成绩,这些成绩的项目代码为default,此时弹框更新这些项目代码
	 *
	 * @param context  context
	 * @param items    当前机器码对应的项目代码
	 * @param title    dialog的标题
	 * @param listener 选择回调
	 */
	public ItemDecideDialogBuilder(final Context context,List<Item> items,String title,DialogInterface.OnClickListener listener){
		super(context);
		mItems = items;
		
		setTitle(title);
		setCancelable(false);
		
		final String[] itemNames = new String[mItems.size()];
		for(int i = 0;i < mItems.size();i++){
			itemNames[i] = mItems.get(i).getItemName();
		}
		
		setSingleChoiceItems(itemNames,0,listener);
	}
	
}
