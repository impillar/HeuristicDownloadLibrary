package com.battery.batterysaver.logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.os.Process;
import android.util.Log;

import com.battery.batterysaver.logs.LoggerBase;
import com.battery.batterysaver.utils.Utils;

public class LoggerDownload extends LoggerBase{
	int pID;
	private BufferedReader readStream;
	
	public LoggerDownload() {
		pID = Process.myPid();
	}
	
	static LoggerDownload instance;
	public static LoggerDownload getInstance(){
		if(instance == null){
			instance = new LoggerDownload();
		}
		return instance;
	}
	
	@Override
	protected String getLogFileName() {
		return "download.log";
	}
	
	public void triggerLog(String msg) {
		log(msg);
	}
}
