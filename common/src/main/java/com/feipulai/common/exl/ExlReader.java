package com.feipulai.common.exl;

import com.github.mjdev.libaums.fs.UsbFile;

/**
 * Created by James on 2018/11/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public abstract class ExlReader{
	
	protected ExlListener listener;

	/**
	 * 创建对象时必须传入回调listener
	 * @param listener 读写结果返回(成功或失败,及原因)
	 */
	public ExlReader(ExlListener listener){
		if(listener == null){
			throw new RuntimeException("cant be null");
		}
		this.listener = listener;
	}
	
	/**
	 * 读指定excel文件数据
	 * @param file 指定excel文件路径
	 */
	public void readExlData(final UsbFile file){
		new Thread(){
			@Override
			public void run(){
				read(file);
			}
		}.start();
	}
	
	/**
	 * 子类实现的具体读取方式
	 * @param path 指定excel文件路径,该方法在子线程中完成
	 */
	protected abstract void read(UsbFile path);
	
}
