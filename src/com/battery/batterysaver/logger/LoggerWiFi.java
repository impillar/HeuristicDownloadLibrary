package com.battery.batterysaver.logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.battery.batterysaver.logs.LoggerBase;
import com.battery.batterysaver.utils.Utils;

public class LoggerWiFi extends LoggerBase {

	private BufferedReader readStream;
	
	static LoggerWiFi instance;
	public static LoggerWiFi getInstance(){
		if(instance == null){
			instance = new LoggerWiFi();
		}
		return instance;
	}
	
	@Override
	protected String getLogFileName() {
		return "wifi.log";
	}

	
	
	@Override
	public void triggerLog() {
		
		boolean fileExist = Utils.checkFileExists("/proc/self/net/dev");
		Log.d("LoggerWiFi", "fileExist: " + fileExist);
		
		try {
			readStream = new BufferedReader(new FileReader("/proc/self/net/dev"));
			ArrayList<String> lines = new ArrayList<String>();
			boolean end = false;
			while(!end){
				String tline = readStream.readLine().trim();
				Log.d("LoggerWiFi", "line: " + tline);
				lines.add(tline);
				if(tline.contains("wlan")){
					end = true;
				}
			}
			readStream.close();
			
			Log.d("LoggerWiFi", "file lines: " + lines.size());
			
			String cellState = "",wifiState = "";
			for(int i=0;i<lines.size();i++){
				String line = lines.get(i);
				if(line.contains("rmnet0")){
					cellState = line;
				}else if(line.contains("wlan")){
					wifiState = line;
				}
			}
			
			log(cellState + "; " + wifiState);
		} catch (IOException e) {
			Log.d("LoggerWiFi", "IOException: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
