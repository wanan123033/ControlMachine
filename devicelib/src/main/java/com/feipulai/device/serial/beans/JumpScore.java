package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by james on 2017/10/27.
 */

public class JumpScore {
	
	private boolean isFoul;
	private int score;
	
	public JumpScore(byte[] result) {
		checkFoul(result);
		//获取到的成绩需要加50cm
		score = ((result[9] & 0xff) << 8) + (result[10] & 0xff) + 50;
		LogUtils.normal("立定跳远返回数据(解析前):"+result.length+"---"+StringUtility.bytesToHexString(result)+"---\n(解析后):"+toString());
	}
	
	public JumpScore() {
	}
	
	private void checkFoul(byte[] result) {
		//若byte8的最高位为1或 byte11/byte12/byte13这3个字节都为0xff，则表示犯规；
		//犯规成绩也在byte9和byte10中(可以通过取消犯规，变为有效成绩)
		//成绩的单位是cm
		if ((result[8] & 0x80) == 0x80) {
			isFoul = true;
			return;
		} else {
			for (int i = 11; i < 14; i++) {
				if ((result[i] & 0xff) != 0xff) {
					return;
				}
				isFoul = true;
			}
		}
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public boolean isFoul() {
		return isFoul;
	}
	
	public void setFoul(boolean foul) {
		isFoul = foul;
	}

	@Override
	public String toString() {
		return "JumpScore{" +
				"isFoul=" + isFoul +
				", score=" + score +
				'}';
	}
}
