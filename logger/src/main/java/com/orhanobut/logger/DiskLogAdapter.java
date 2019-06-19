package com.orhanobut.logger;

public class DiskLogAdapter implements LogAdapter {

	private final FormatStrategy formatStrategy;

	public DiskLogAdapter(String filePah) {
		formatStrategy = AndroidDiskFormatStrategy.newBuilder().path(filePah).build();
	}

	public DiskLogAdapter(FormatStrategy formatStrategy) {
		this.formatStrategy = formatStrategy;
	}

	@Override
	public boolean isLoggable(int priority, String tag) {
		return true;
	}

	@Override
	public void log(int priority, String tag, String message) {
		formatStrategy.log(priority, tag, message);
	}

}