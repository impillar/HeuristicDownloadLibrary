package com.battery.batterysaver.logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

import com.battery.batterysaver.logs.LoggerBase;
import com.battery.batterysaver.utils.Utils;

public class LoggerEvent extends LoggerBase {

	static LoggerEvent instance;
	private BufferedReader readStream;
	
	public static LoggerEvent getInstance(){
		if(instance == null){
			instance = new LoggerEvent();
		}
		return instance;
	}
	
	@Override
	protected String getLogFileName() {
		return "event.log";
	}

	@Override
	public void startLog() {
	}
	
	@Override
	public void stopLog() {
	}
	
	public void triggerLog(String event){
		log(event);
	}
	
}
