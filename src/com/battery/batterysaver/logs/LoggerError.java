package com.battery.batterysaver.logs;

import com.battery.batterysaver.logger.LoggerWiFi;

public class LoggerError extends LoggerBase {
	public static final String createBufferedWritter = "CreateBufferedWritter";
	public static final String writeBufferedWritter = "WriteBufferedWritter";
	public static final String fileNotFound = "FileNotFound";
	public static final String IOException = "IOException";
	public static final String closeBufferedWritter = "closeBufferedWritter";
	
	
	static LoggerError instance;
	public static LoggerError getInstance(){
		if(instance == null){
			instance = new LoggerError();
		}
		return instance;
	}
	
	@Override
	protected String getLogFileName() {
		return "error.log";
	}

}
