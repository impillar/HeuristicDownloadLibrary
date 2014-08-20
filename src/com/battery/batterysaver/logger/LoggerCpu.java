package com.battery.batterysaver.logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.os.Process;
import android.util.Log;

import com.battery.batterysaver.logs.LoggerBase;
import com.battery.batterysaver.utils.Utils;

public class LoggerCpu extends LoggerBase {

	int pID;
	private BufferedReader readStream;
	
	public LoggerCpu() {
		pID = Process.myPid();
	}
	
	static LoggerCpu instance;
	public static LoggerCpu getInstance(){
		if(instance == null){
			instance = new LoggerCpu();
		}
		return instance;
	}
	
	@Override
	protected String getLogFileName() {
		return "cpu.log";
	}
	
	@Deprecated
	@Override
	public void triggerLog() {
		try {
			String cpuFreq = Utils.ReadSysfile("/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state");
			cpuFreq = cpuFreq.replace("\n", " ").replace("\\s+", "\t");
			Log.d("LoggerCpu", cpuFreq);
			
			readStream = new BufferedReader(new FileReader("/proc/stat"));
			String cpuStat = readStream.readLine();
			readStream.close();

			readStream = new BufferedReader(new FileReader("/proc/"+pID+"/stat"));
			String myCpuStat = readStream.readLine();
			readStream.close();
			
			log(cpuStat + "; " + cpuFreq + "; " + myCpuStat);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
