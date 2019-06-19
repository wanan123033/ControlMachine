package com.feipulai.common.jump_rope.task;


/**
 * Created by James on 2018/8/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public interface LEDContentGenerator{
	/**
	 * 根据指定位置的学生设备配对信息,得到需要在LED屏幕上显示的字符串
	 */
	String generate(int position);

}
