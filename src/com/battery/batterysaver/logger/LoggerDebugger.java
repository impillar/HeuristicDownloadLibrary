package com.battery.batterysaver.logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.os.Process;
import android.util.Log;

import com.battery.batterysaver.logs.LoggerBase;
import com.battery.batterysaver.utils.Utils;

public class LoggerDebugger extends LoggerBase{
	int pID;
	private BufferedReader readStream;
	
	public LoggerDebugger() {
		pID = Process.myPid();
	}
	
	static LoggerDebugger instance;
	public static LoggerDebugger getInstance(){
		if(instance == null){
			instance = new LoggerDebugger();
		}
		return instance;
	}
	
	@Override
	protected String getLogFileName() {
		return "debugging.log";
	}
	
	public void triggerLog(String msg) {
		log(msg);
	}
}
