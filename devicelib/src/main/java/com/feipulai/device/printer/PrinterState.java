package com.feipulai.device.printer;

/**
 * Created by James on 2018/11/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class PrinterState{
	
	// 缺纸
	private boolean paperLack;
	//过热
	private boolean overHeat;
	
	public PrinterState(byte data){
		paperLack = (data & 0x40) == 0x40;
		overHeat = (data & 0x80) == 0x80;
	}
	
	public boolean isPaperLack(){
		return paperLack;
	}
	
	public boolean isOverHeat(){
		return overHeat;
	}
	
	@Override
	public String toString(){
		return "PrinterState{" +
				"paperLack=" + paperLack +
				", overHeat=" + overHeat +
				'}';
	}
	
}
