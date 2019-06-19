package com.feipulai.common.exl;

import com.github.mjdev.libaums.fs.UsbFile;

/**
 * Created by James on 2018/11/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public abstract class ExlWriter{
	
	protected ExlListener listener;
	
	/**
	 * 创建对象时必须传入回调listener
	 * @param listener 读写结果返回(成功或失败,及原因)
	 */
	public ExlWriter(ExlListener listener){
		if(listener == null){
			throw new RuntimeException("cant be null");
		}
		this.listener = listener;
	}
	
	/**
	 * 写数据到指定excel中
	 * @param file 指定excel文件路径
	 */
	public void writeExelData(final UsbFile file){
		new Thread(){
			@Override
			public void run(){
				write(file);
			}
		}.start();
	}
	
	/**
	 * 子类实现的具体写入方式
	 * @param file 指定excel文件路径,该方法在子线程中完成
	 */
	protected abstract void write(UsbFile file);
	
}
