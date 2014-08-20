package com.battery.batterysaver.logs;

import java.util.ArrayList;

public class ScheduleServiceNotifier implements ScheduleServiceListener {
	ArrayList<ScheduleServiceListener> listeners = new ArrayList<ScheduleServiceListener>();
	
	public void addListener(ScheduleServiceListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(ScheduleServiceListener listener){
		listeners.remove(listener);
	}
	
}
