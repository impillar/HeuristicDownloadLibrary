package com.battery.batterysaver.logs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.battery.batterysaver.utils.Utils;

public class LoggerBase {
	public String logFilePath;
	public String logFileName;
	public BufferedWriter bufferedWriter;
	public Context context;

	protected String getLogFileName(){
		return "default.log";
	}
	
	protected Context getContext() {
		return context;
	}
	
	public LoggerBase(){
		initFile();
	}
	
	protected void initFile() {
		this.logFileName = getLogFileName();
		this.logFilePath = Utils.getWorkPathString() + File.separator + logFileName;
		
		//Log.d("LoggerBase", this.logFilePath);
		
		try {
			boolean append = Utils.checkFileExists(logFilePath);
			bufferedWriter = new BufferedWriter(new FileWriter(logFilePath, append));
		} catch (IOException e) {
			e.printStackTrace();
			LoggerError.getInstance().log(LoggerError.createBufferedWritter, getFilePath());
			Log.d(Utils.tAGErrorString, "create BufferedWriter error: new BufferedWriter(new FileWriter(" + this.logFilePath + "))");
		}
	}
	
	private boolean println(String text){
		return print(text + "\t@" + Utils.getCurrentTimeString() + "\n");
	}
	
	private boolean print(String text){
		if(!Utils.checkFileExists(logFilePath)){
			initFile();
		}
		
		try {
			bufferedWriter.write(text);
			bufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			LoggerError.getInstance().log(LoggerError.writeBufferedWritter, getFilePath());
			Log.d(Utils.tAGErrorString, "file BufferedWriter write error: " + bufferedWriter.toString() + " " + text);
			return false;
		}
		return true;
	}
	
	public void triggerLog() {
		
	}
	public void startLog(){
		
	}
	public void stopLog() {

	}
	
	public String loggingItem() {
		return null;
	} 
	
	////////////////
	public String getFilePath() {
		return logFilePath;
	}
	
	public String getFileName(){
		return logFileName;
	}
	
	public String getFileContent() throws IOException{
		return Utils.getFileContent(logFilePath);
	}
	
	public long getFileSize() {
		return Utils.getFileSize(logFilePath);
	}
	
	public long getSizeThreshold(){
		return Utils.getLogFileSizeTh();
	}
	
	public void log(String tag){
		println(tag);
	}
	
	public void log(String tag, String value){
		println(tag + "\t" + value);
	}

	public void trucateFile(){
		Utils.removeFile(getFilePath());
	}
	
	public void closeFile(){
		try {
			bufferedWriter.close();
		} catch (IOException e) {
			LoggerError.getInstance().log(LoggerError.closeBufferedWritter, bufferedWriter.toString());
			Log.d(Utils.tAGErrorString, "file BufferedWriter write error: " + bufferedWriter.toString());
			e.printStackTrace();
		}
	}

	public void setContext(Context context) {
		this.context = context;
	}

	

	
}
