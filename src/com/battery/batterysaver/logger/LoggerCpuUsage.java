package com.battery.batterysaver.logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.os.Process;
import android.util.Log;

import com.battery.batterysaver.logs.LoggerBase;
import com.battery.batterysaver.utils.Utils;

public class LoggerCpuUsage extends LoggerBase {

	private BufferedReader readStream;
	private long cpuTime = 0;
	private long totalTime = 0;

	public LoggerCpuUsage() {
		// initilize the cpuTime and totalTime
		long[] times = readData();
		if (times != null) {
			this.cpuTime = times[0];
			this.totalTime = times[0] + times[1];
		}
	}

	static LoggerCpuUsage instance;

	public static LoggerCpuUsage getInstance() {
		if (instance == null) {
			instance = new LoggerCpuUsage();
		}
		return instance;
	}

	/**
	 * Read the state for the system, and return the current cpu time and idle
	 * time
	 * 
	 * @return
	 */
	public long[] readData() {
		try {
			readStream = new BufferedReader(new FileReader("/proc/stat"));
			String cpuStat = readStream.readLine();
			readStream.close();
			if (cpuStat != null) {
				String[] cpuArr = cpuStat.split(" ");
				long cpuTime = Long.parseLong(cpuArr[2])
						+ Long.parseLong(cpuArr[3]) + Long.parseLong(cpuArr[4])
						+ Long.parseLong(cpuArr[6]) + Long.parseLong(cpuArr[7])
						+ Long.parseLong(cpuArr[8]);
				long idleTime = Long.parseLong(cpuArr[5]);
				return new long[] { cpuTime, idleTime };
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String loggingItem() {
		long[] times = readData();
		if (times != null) {
			double usage = 0;
			long cpuTime = times[0];
			long idleTime = times[1];
			if (this.cpuTime != 0 && (cpuTime + idleTime - this.totalTime) != 0) {
				usage = (cpuTime - this.cpuTime) * 100.0
						/ (cpuTime + idleTime - this.totalTime);
			}
			this.cpuTime = cpuTime;
			this.totalTime = cpuTime + idleTime;

			return String.format("%.2f", usage);
		}
		return "0";
	}

	@Override
	protected String getLogFileName() {
		return "cpuusage.log";
	}

	@Override
	public void triggerLog() {
		try {
			readStream = new BufferedReader(new FileReader("/proc/stat"));
			String cpuStat = readStream.readLine();
			readStream.close();

			log(cpuStat);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
