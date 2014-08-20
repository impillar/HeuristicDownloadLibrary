package com.battery.batterysaver.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.ntu.cltk.data.StringUtil;
import edu.ntu.cltk.file.FileUtil;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class Utils {

	public static String guessName(String srcUrl) {
		String[] arr = srcUrl.split("/");
		String url = null;
		if (arr.length > 0) {
			url = arr[arr.length - 1];
		} else {
			url = srcUrl;
		}
		return url;
	}

	public static Context context;

	static String workPathString = Environment.getExternalStorageDirectory().getPath() + "/batterysaver/logs";
	
	public static String tAGDebugString = "DEBUG";
	public static String tAGErrorString = "ERROR";
	public static String tAGEventString = "EVENT";
	public static ArrayList<String> tagOfCurrentLogFileString = new ArrayList<String>();

	static long taskPeriod = (long)(1) * secondMs();//5*60*1000;
	static long logFileSizeTh =  5*1024; //10*1024;

	public static long getLogFileSizeTh() {
		return logFileSizeTh;
	}
	
	public static long getFileSize(String filePath){
		File file = new File(filePath);
		return file.length();
	}
	
	public static String getFileContent(String filePath) throws IOException{
		String text = "";
		File file = new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while (reader.ready()) {
			text += (reader.readLine() + "\n");
		}
		reader.close();
		return text;
	}
	
	public static void copyfile(String srFile, String dtFile){
		try{
			  File f1 = new File(srFile);
			  File f2 = new File(dtFile);
			  InputStream in = new FileInputStream(f1);
			  
			  //For Append the file.
			  //  OutputStream out = new FileOutputStream(f2,true);
	
			  //For Overwrite the file.
			  OutputStream out = new FileOutputStream(f2);
	
			  byte[] buf = new byte[1024];
			  int len;
			  while ((len = in.read(buf)) > 0){
				  out.write(buf, 0, len);
			  }
			  in.close();
			  out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static long getTaskPeriod() {
		return taskPeriod;
	}
	
	public static Integer getDayNumber(){
        Calendar now = Calendar.getInstance();
        Integer month = now.get(Calendar.MONTH);
        Integer day = now.get(Calendar.DAY_OF_MONTH);
        return Integer.parseInt(month + "" + day);
	}
	
	public static Integer getHourNumber(){
        Calendar now = Calendar.getInstance();
        Integer hour = now.get(Calendar.HOUR_OF_DAY);
        return hour;
	}

	public static boolean initialUtil(Context context) {
		Utils.context = context;
		boolean ret = true;

		File file;
		file = new File(workPathString);
		if(!file.exists()){
			ret = file.mkdirs();
		}
		
		Log.d("Util", workPathString);
		
		return ret;
	}

	public static String getWorkPathString() {
		File file;
		file = new File(workPathString);
		if(!file.exists()){
			file.mkdirs();
		}
		return workPathString;
	}

	public static String simplifyString(String string){
		string = string.trim();
		string = string.replaceAll(" +|\t+|\n+", "_");
		return string;
	}

	public static boolean checkFileExists(String filePathString){
		File file;
		file = new File(filePathString);
		if(!file.exists()){
			return false;
		}
		return true;
	}

	public static File getFile(String filePathString){
		File file = new File(filePathString);
		if(!checkFileExists(filePathString)){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public static void showToast(Context context,String text, int duration){
		Toast.makeText(context, text, duration).show();
	}
	
	public static String getWifiMacSystem(){
		return getWifiMacSystem(context);
	}

	public static String getWifiMacSystem(Context context){
		//return "getWifiAddress";
		WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return wifiMgr.getConnectionInfo().getMacAddress();
	}

	
	public static String getBluetoothMacSystem(){
		//return "getBluetoothAddress";
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		return adapter.getAddress();
	}
	
	public static String getImeiSystem(){
		return getImeiSystem(context);
	}

	public static String getImeiSystem(Context context){
		String imei = new String();
		TelephonyManager telephonemanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonemanager.getDeviceId();
//		try{
//		   imei = telephonemanager.getDeviceId();
//		}
//	    catch(Exception e){
//	    	e.printStackTrace();
//	    	Toast.makeText(context, "IMEI code read error", Toaist.LENGTH_SHORT).show();
//	    }
		return imei;
	}

	public static String getPhoneModel(){
		return simplifyString(Build.MODEL);
	}

	public static String getPhoneNumber(Context context){
		String phoneNumber = new String();
		TelephonyManager telephonemanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		phoneNumber = telephonemanager.getLine1Number();
//		try{
//		   phoneNumber = telephonemanager.getLine1Number();
//		}
//	    catch(Exception e){
//	    	e.printStackTrace();
//	    	Toast.makeText(context, "phone number read error", Toast.LENGTH_SHORT).show();
//	    }
		return phoneNumber;
	}

	public static boolean isServiceRunning(Context context,String classString){
		ActivityManager myManager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService =
				(ArrayList<RunningServiceInfo>) myManager.getRunningServices(100);
		for(int i = 0 ; i<runningService.size();i++){
		   if(runningService.get(i).service.getClassName().toString().equals
				   (classString)){
		    return true;
		   }
		}
		return false;
	}

	public static void addLogFileExceptTag(String tag){
		tagOfCurrentLogFileString.add(tag);
	}

	public static void removeFile(String filePath){
		File file = new File(filePath);
		if(file.exists()){
			file.delete();
		}
	}

	public static void removeFiles(){
		tagOfCurrentLogFileString.add("list.conf");
		removeFiles(workPathString, tagOfCurrentLogFileString);
	}

	public static void removeFiles(String pathString, ArrayList<String> exceptTagList){
		File cacheDir = new File(pathString);

	    File[]	fileList;
	    fileList = cacheDir.listFiles();
	    for(int i = 0;i < fileList.length; i++)   {
	    	boolean isContainOne = false;
	    	for(int j=0;j<exceptTagList.size();j++){
	    		if(fileList[i].getName().contains(exceptTagList.get(j))){
	    			isContainOne = true;
	    		}
	    	}
	    	if(!isContainOne){
	    		fileList[i].delete();
	    	}
	    }
	}

	public static String toString(Integer integer){
		return String.valueOf(integer);
	}

	public static String toString(byte[] bytes){
		return new String(bytes);
	}

	public static String getCurrentTimeString() {
		String timeString = "";
		timeString = String.valueOf(System.currentTimeMillis());
		return timeString;
	}

	public static boolean checkWifiConnection() {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		
//		boolean isWifi = manager.getNetworkInfo(
//		                        ConnectivityManager.TYPE_WIFI).isConnected();
		
		NetworkInfo activeInfo = manager.getActiveNetworkInfo();
		
		//if(isWifi){
		if(activeInfo != null && activeInfo.getType() == ConnectivityManager.TYPE_WIFI){
			//NetworkInfo networkInfo = manager.getActiveNetworkInfo();
			WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
			String name = wifiInfo.getSSID();
			if(name.contains("SUTD") || name.contains("sutd")){
				return true;
			}
		}

		return false;
	}

	public static String changeIntegerIPAddressToString(int ipAddressInteger){;

		String ip = String.format("%d.%d.%d.%d",
		(ipAddressInteger & 0xff),
		(ipAddressInteger >> 8 & 0xff),
		(ipAddressInteger >> 16 & 0xff),
		(ipAddressInteger >> 24 & 0xff));

		return ip;
	}

	public static boolean checkSimCardExist(){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
		if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT){
		  return true;
		} else {
		  return false;
		}
	}

	public static boolean isSimCardReady(){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
		if (tm.getSimState() == TelephonyManager.SIM_STATE_READY){
		  return true;
		} else {
		  return false;
		}
	}

	public static Long secondMs(){
		return (long) (1000);
	}

	public static Long minuteMs(){
		return (long) (60 * 1000);
	}

	public static Long oneHourMs(){
		return (long) (60 * 60 * 1000);
	}

	public static String millisecondsToString(String time) {
		Long timeLong = Long.parseLong(time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeLong);
		Date date = calendar.getTime();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		return simpleDateFormat.format(date);
	}

	public static boolean checkQuestionTagExist(String questionTag) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static boolean checkEmail(String email) {
		String[] splited = email.split("@");
		if(splited.length == 2){
			if(splited[0].length() > 0 && splited[1].length() > 0){
				return true;
			}
		}
		return false;
	}

	public static String getFileName(String filePath) {
		if(filePath.contains("/")){
			String[] splited = filePath.split("/");
			return splited[splited.length-1].trim();
		}else {
			return filePath;
		}
	}
	
	public static String getString(String[] strings){
		String retString = "";
		for(int i=0;i<strings.length-1;i++){
			retString += strings[i] + "::"; 
		}
		retString += strings[strings.length-1];
		return retString;
	}
	
	public static String ReadSysfile(String filePath){
	  ProcessBuilder cmd;
	  String result="";
	  if (!FileUtil.fileExists(filePath)){
		  return StringUtil.EMPTY;
	  }
	  try{
		  String[] args = {"/system/bin/cat", filePath};
		  cmd = new ProcessBuilder(args);

		  Process process = cmd.start();
		  InputStream in = process.getInputStream();
		  byte[] re = new byte[1024];
		  while(in.read(re) != -1){
			  result = result + new String(re);
			  result = result.trim();
		  }
		  in.close();
	  } catch(IOException ex){
		  ex.printStackTrace();
	  }
	  return result;
	 }
}
