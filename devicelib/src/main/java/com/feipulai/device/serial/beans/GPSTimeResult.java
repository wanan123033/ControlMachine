package com.feipulai.device.serial.beans;

public class GPSTimeResult {
	
	private int sequence;
	private String time;
	
	public GPSTimeResult(byte[] data) {
		sequence = ((data[4] & 0xff) << 8) + (data[5] & 0xff);
		StringBuilder sb = new StringBuilder(26);
		for (int i = 6; i <= 30; i++) {
			sb.append((char)(data[i] & 0xff));
		}
		sb.append("  ");
		time = sb.toString();
	}
	
	public int getSequence() {
		return sequence;
	}
	
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
}
