package com.battery.batterysaver.logs;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.battery.batterysaver.logger.LoggerCpu;
import com.battery.batterysaver.logger.LoggerCurrent;
import com.battery.batterysaver.logger.LoggerVoltage;
import com.battery.batterysaver.logger.LoggerWiFi;

public class LoggerHandler extends ServiceHandler {
	
	ArrayList<LoggerBase> loggers = new ArrayList<LoggerBase>();
	
	public LoggerHandler(Context context) {
		super(context);
		
		loggers.add(LoggerCurrent.getInstance());
		loggers.add(LoggerVoltage.getInstance());
		loggers.add(LoggerWiFi.getInstance());
		loggers.add(LoggerCpu.getInstance());
		
		
		
		
		for(int i=0;i<loggers.size();i++){
			loggers.get(i).setContext(context);
		}
	}
	
	@Override
	protected void onTimer() {
		Log.d("LoggerHandler", "onTimer " + System.currentTimeMillis() + " " + loggers.get(1).getFilePath());
		for(int i=0;i<loggers.size();i++){
			loggers.get(i).triggerLog();
		}
	}
	
	@Override
	protected void start() {
		Log.d("LoggerHandler", "start " + System.currentTimeMillis());
		for(int i=0;i<loggers.size();i++){
			loggers.get(i).startLog();
		}
	}
	
	@Override
	protected void stop() {
		for(int i=0;i<loggers.size();i++){
			loggers.get(i).stopLog();
		}
	}
}
