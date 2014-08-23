package com.battery.batterysaver;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.battery.batterysaver.interfaces.DownloadTask;
import com.battery.batterysaver.learning.ProgressiveVoltageModel;
import com.battery.batterysaver.learning.VoltageModel;
import com.battery.batterysaver.logger.LoggerCpu;
import com.battery.batterysaver.logger.LoggerCurrent;
import com.battery.batterysaver.logger.LoggerDownload;
import com.battery.batterysaver.logger.LoggerVoltage;
import com.battery.batterysaver.logger.LoggerWiFi;
import com.battery.batterysaver.logs.LoggerBase;
import com.battery.batterysaver.utils.Utils;

import edu.ntu.cltk.file.FileUtil;

public class DownloadTaskImpl implements DownloadTask {

	public static String TAG = DownloadTaskImpl.class.getName();
	public static final long FILE_LIMIT = 1 << 29;	// 521M
	
	private String appId;
	private String url;
	private long duration;
	private String folder;
	
	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	private LoggerBase cpuLogger = LoggerCpu.getInstance();
	protected LoggerBase voltageLogger = LoggerVoltage.getInstance();
	protected LoggerBase currentLogger = LoggerCurrent.getInstance();
	private LoggerBase wifiLogger = LoggerWiFi.getInstance();
	
	protected int status;
	private int effectTime = 0;
	protected long startFrom = 0;
	protected boolean firstDownloading = true;
	
	protected ScheduledFuture<?> startFuture = null;
	protected ScheduledFuture<?> pauseFuture = null;
	protected ScheduledFuture<?> loggingFuture = null;
	
	private boolean isDownload = true;
	protected LoggerDownload downloadLogger = LoggerDownload.getInstance();
	protected long byteDownloaded = 0;
	protected long fileSize = 0;
	protected long fileCapacity = 0;
	protected VoltageModel vdm;
	protected int logDownloadedByte = 0;
	
	
	protected ScheduledExecutorService scheduledExecutor;
	
	protected Runnable controlRn = new Runnable(){
		@Override
		public void run(){
			String volStr = voltageLogger.loggingItem();
			if (status == DownloadTask.TASK_PAUSED){
				if (!vdm.resting(Long.parseLong(volStr))){
					status = DownloadTask.TASK_DOWNLOADING;
					// Do some initial work before the next downloading
					vdm.startDownloadingInit();
					startFuture = scheduledExecutor.schedule(startDownloadRn, 0, TimeUnit.MILLISECONDS);
				}
			}else if (status == DownloadTask.TASK_DOWNLOADING){
				if (!vdm.working(Long.parseLong(volStr))){
					// Do some initial work before the next resting
					vdm.stopDownloadingInit();			
					status = DownloadTask.TASK_PAUSED;
				}
			}
			String tag = (status==DownloadTask.TASK_DOWNLOADING?"1":"0") + "\t" + volStr + "\t" + currentLogger.loggingItem()+"\t"/*+getEffectiveTime()+"\t"+vdm.printStatus()*/;
			LoggerBase lb = new LoggerBase();
			lb.log(tag);
			
			logDownloadedByte++;
			if (logDownloadedByte % 10 == 0){
				downloadLogger.triggerLog((status==DownloadTask.TASK_DOWNLOADING?"1":"0") + " " + byteDownloaded);
			}
		}
	};
	
	protected Runnable loggingRn = new Runnable(){
		@Override
		public void run(){
			loggingSingleEntry();
			downloadLogger.triggerLog((status==DownloadTask.TASK_DOWNLOADING?"1":"0")+" "+byteDownloaded);
		}
	};
	
	protected Runnable startDownloadRn = new Runnable(){

		@Override
		public void run() {
			singleDownloadTask(url);		
		}
		
	};
	protected Runnable pauseDownloadRn = new Runnable(){

		@Override
		public void run() {
			status = DownloadTask.TASK_PAUSED;
			firstDownloading = false;
			// Log the current download
			//1 stands for the pause
			//downloadLogger.triggerLog(0+" "+byteDownloaded+"");
			//loggingSingleEntry();
		}
		
	};
	
	protected void loggingSingleEntry(){
		String tag = (status==DownloadTask.TASK_DOWNLOADING?"1":"0") + "\t" + voltageLogger.loggingItem() + "\t" + currentLogger.loggingItem()+"\t"+getEffectiveTime();
		LoggerBase lb = new LoggerBase();
		lb.log(tag);
	}
	
	public DownloadTaskImpl(String appId, String url, long duration, String folder) {
		
		this.appId = appId;
		this.url = url;
		this.duration = duration;
		this.folder = folder;
		
		scheduledExecutor = Executors.newScheduledThreadPool(2);
		
		vdm = new ProgressiveVoltageModel();
	}

	@Override
	public void pauseDownload(){
		this.status = DownloadTask.TASK_PAUSED;
	}
	
	@Override
	public void stopDownload() {
		this.status = DownloadTask.TASK_STOPPED;
		if (startFuture!=null) startFuture.cancel(true);
		if (pauseFuture!=null) pauseFuture.cancel(true);
		if (loggingFuture!=null) loggingFuture.cancel(true);
	}

	@Override
	public void startDownload() {
		
		fileSize = 0;
			
		/**************************/
		/*	Dynamically decide    */
		/**************************/
		status = DownloadTask.TASK_DOWNLOADING;
		startFuture = scheduledExecutor.schedule(startDownloadRn, 0, TimeUnit.MILLISECONDS);
		//pauseFuture = scheduledExecutor.schedule(pauseDownloadRn, downloadTime, TimeUnit.SECONDS);
		loggingFuture = scheduledExecutor.scheduleAtFixedRate(controlRn, 0, Constants.LOGGING, TimeUnit.MILLISECONDS);
		
		startFrom = System.currentTimeMillis();
	}
	
	private void singleDownloadTask(String urlPath){
		
		InputStream is = null;
		FileOutputStream fos = null;
		String fileName = null;
		while (true){
			try {
				if (status != DownloadTask.TASK_DOWNLOADING){
					return;
				}
				URL url = new URL(urlPath);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setConnectTimeout(1000 * 60 * 5);
				
				if (fileCapacity == 0){
					fileCapacity = urlConnection.getContentLength();
					if (fileCapacity == 0){
						Log.i(TAG, String.format("The file %s is an empty file", url));
						return;
					}
				}
				
				urlConnection.setRequestProperty("RANGE", String.format("byte=%d-%d", fileSize, fileCapacity));
				urlConnection.connect();
				is = urlConnection.getInputStream();
				fileName = folder + Utils.guessName(urlPath);
				fos = new FileOutputStream(fileName, true /*append*/);
				byte data[] = new byte[1024];
				int len = 0;
				long progress = 0;
				while ((len = is.read(data)) != -1){
					
					byteDownloaded += len;
					fileSize += len;
					
					if (progress != byteDownloaded * 100 / fileCapacity){
						progress = byteDownloaded * 100 / fileCapacity;
					}
					fos.write(data, 0, len);
					if (fileSize > 1<<20)	fos.flush();
					if (status != DownloadTask.TASK_DOWNLOADING){
						break;
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				if (is != null){
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fos != null){
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fileName != null)	FileUtil.deleteFile(fileName);
			}
		}
	}

	@Override
	public int getTaskStatus() {
		return this.status;
	}

	@Override
	public long getEffectiveTime() {
		return this.effectTime;
	}

	@Override
	public double getProgress() {
		if (fileCapacity == 0)	return 0;
		return fileSize / fileCapacity * 1.0;
	}

}
