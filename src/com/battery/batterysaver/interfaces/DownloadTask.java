package com.battery.batterysaver.interfaces;

public interface DownloadTask {

	public static final int TASK_DOWNLOADING = 0x0001;
	public static final int TASK_PAUSED = 0x0002;
	public static final int TASK_STOPPED = 0x0003;
	
	public void stopDownload();
	
	public void pauseDownload();
	
	public void startDownload();
	
	public int getTaskStatus();
	
	public long getEffectiveTime();
	
}
