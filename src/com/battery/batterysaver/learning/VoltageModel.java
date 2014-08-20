package com.battery.batterysaver.learning;

public interface VoltageModel {

	public void startDownloadingInit();
	
	public void stopDownloadingInit();
	
	public boolean resting(long vol);

	public boolean working(long vol);
	
	public String printStatus();
}
