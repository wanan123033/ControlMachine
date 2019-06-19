package com.feipulai.common.jump_rope.task;


/**
 * Created by James on 2018/8/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public interface OnGetStateWithLedListener{
	/**
	 * 发送获取状态命令
	 *
	 * @param position 表示本次需要给第position个设备发送获取状态命令
	 */
	void onGettingState(int position);
	
	/**
	 * 连续发送3轮获取状态消息后,回调该函数,这里面进行连接是否OK等业务逻辑处理
	 */
	void onStateRefreshed();
	
	/**
	 * 这里返回设备具体数量,用于获取设备信息时指定
	 *
	 * @return 设备具体数量
	 */
	int getDeviceCount();
	
	/**
	 * 对于第position个位置,LED屏幕应显示的内容
	 *
	 * @param position 位置
	 * @return LED屏幕应显示的内容
	 */
	String getStringToShow(int position);
	
	/**
	 * 返回主机号
	 *
	 * @return 返回主机号
	 */
	int getHostId();
	
}
