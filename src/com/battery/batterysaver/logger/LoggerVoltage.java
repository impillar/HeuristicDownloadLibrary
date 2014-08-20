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

import edu.ntu.cltk.data.StringUtil;

public class LoggerVoltage extends LoggerBase {

	static LoggerVoltage instance;
	private BufferedReader readStream;
	// Different model may have the file in different locations
	public static String[] VOLTAGE_NOW = {
		"/sys/class/power_supply/battery/voltage_now",						// Nexus S, Nexus 4, Nexus 5, Nexus 7
		"/sys/devices/i2c-3/3-0055/power_supply/bq27520/voltage_now"		// Sony Xperia Android 4.0.4 Model LT26w
	};
	public static String VOLTAGE_NOW2 = "";
	
	
	public static LoggerVoltage getInstance(){
		if(instance == null){
			instance = new LoggerVoltage();
		}
		return instance;
	}
	
	@Override
	protected String getLogFileName() {
		return "voltage.log";
	}

	@Override
	public void startLog() {
		registerReceiver();
	}
	
	@Override
	public void stopLog() {
		unregisterReceiver();
	}
	
	@Override
	public String loggingItem(){
		for (String file : VOLTAGE_NOW){
			String tmp = Utils.ReadSysfile(file);
			if (!StringUtil.isEmpty(tmp)){
				return tmp;
			}
		}
		return StringUtil.EMPTY;
	}
	
	public void registerReceiver(){
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        getContext().registerReceiver(broadcastReceiver, filter);
	}
	
	public void unregisterReceiver(){
		getContext().unregisterReceiver(broadcastReceiver);
	}
	
	@Override
	public void triggerLog() {
		String voltage = "-1";
		for (String file : VOLTAGE_NOW){
			voltage = Utils.ReadSysfile("/sys/class/power_supply/battery/voltage_now");
			if (!StringUtil.isEmpty(voltage)){
				break;
			}
		}					
		log(voltage);
	}
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent){
			
			boolean isPresent = intent.getBooleanExtra("present", false);
			String technology = intent.getStringExtra("technology");
			int plugged = intent.getIntExtra("plugged", -1);
			int scale = intent.getIntExtra("scale", -1);
			int health = intent.getIntExtra("health", 0);
			int status = intent.getIntExtra("status", 0);
			int rawlevel = intent.getIntExtra("level", -1);
            int level = 0;
            
            Bundle bundle = intent.getExtras();
            
            Log.d("BatteryLevel", bundle.toString());
            
            if(isPresent){
	            if (rawlevel >= 0 && scale > 0) {
	                level = (rawlevel * 100) / scale;
	            }
	            
	            String info = "Battery Level: " + level + "%\n";
	            
	            info += ("Technology: " + technology + "\n");
	            info += ("Plugged: " + getPlugTypeString(plugged) + "\n");
	            info += ("Health: " + getHealthString(health) + "\n");
	            info += ("Status: " + getStatusString(status) + "\n");
	            
	            String logString = bundle.toString().replace("Bundle[{", "").replace("}]", "");
	            String[] splited = logString.split(",");
	            List<String> list = Arrays.asList(splited);
	            for(int i=0;i<list.size();i++){
	            	list.set(i,list.get(i).trim());
	            }
	            Collections.sort(list);
	            logString = "";
	            for(int i=0;i<list.size()-1;i++){
	            	logString += list.get(i) + "; "; 
	            }
	            logString += list.get(list.size()-1);
	            logString = logString.trim();
	            logString = logString.replace("\\s+", "\t");
	            log(logString);
            }
            else{
            	log("Battery not present");
            }
		}
	};
	
	
	private String getPlugTypeString(int plugged){
		String plugType = "Unknown";
		switch(plugged){
			case BatteryManager.BATTERY_PLUGGED_AC: plugType = "AC";	break;
			case BatteryManager.BATTERY_PLUGGED_USB: plugType = "USB";	break;
		}
		
		return plugType;
	}
	
	private String getHealthString(int health){
		String healthString = "Unknown";
		switch(health){
			case BatteryManager.BATTERY_HEALTH_DEAD: healthString = "Dead"; break;
			case BatteryManager.BATTERY_HEALTH_GOOD: healthString = "Good"; break;
			case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: healthString = "Over Voltage"; break;
			case BatteryManager.BATTERY_HEALTH_OVERHEAT: healthString = "Over Heat"; break;
			case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE: healthString = "Failure"; break;
		}
		return healthString;
	}
	
	private String getStatusString(int status)	{
		String statusString = "Unknown";
		switch(status){	
			case BatteryManager.BATTERY_STATUS_CHARGING: statusString = "Charging"; break;
			case BatteryManager.BATTERY_STATUS_DISCHARGING: statusString = "Discharging"; break;
			case BatteryManager.BATTERY_STATUS_FULL: statusString = "Full"; break;
			case BatteryManager.BATTERY_STATUS_NOT_CHARGING: statusString = "Not Charging"; break;
		}
		return statusString;
	}
}
