package com.battery.batterysaver;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.battery.batterysaver.interfaces.DownloadTask;
import com.battery.batterysaver.learning.VoltageBetaModel;
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
	
	private LoggerBase cpuLogger = LoggerCpu.getInstance();
	protected LoggerBase voltageLogger = LoggerVoltage.getInstance();
	protected LoggerBase currentLogger = LoggerCurrent.getInstance();
	private LoggerBase wifiLogger = LoggerWiFi.getInstance();
	
	protected List<HashMap<String, String>> tasks;
	protected Handler mHandler;
	private Context context;
	
	protected int status;
	private int effectTime = 0;
	protected String urlPath;
	protected int downloadTime = 0;
	protected int restTime = 0;
	protected long startFrom = 0;
	protected boolean firstDownloading = true;
	
	protected ScheduledFuture<?> startFuture = null;
	protected ScheduledFuture<?> pauseFuture = null;
	protected ScheduledFuture<?> loggingFuture = null;
	
	private boolean isDownload = true;
	protected LoggerDownload downloadLogger = LoggerDownload.getInstance();
	protected long byteDownloaded = 0;
	protected long fileSize = 0;
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
			}else if (status == DownloadTask.TASK_DOWNLOADING && firstDownloading == false){
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
			singleDownloadTask(urlPath);		
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
	
	public DownloadTaskImpl(Handler mHandler, List listData, Context mContext) {
		this.mHandler = mHandler;
		this.tasks = listData;
		this.context = mContext;
		scheduledExecutor = Executors.newScheduledThreadPool(2);
		
		vdm = new VoltageBetaModel();
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
		HashMap<String, String> task = this.tasks.get(0);
		
		fileSize = 0;
		
		this.urlPath = task.get("url");
		this.downloadTime = Integer.parseInt(task.get("download"));
		this.restTime = Integer.parseInt(task.get("rest"));
			
		if (Constants.AUTO_MODE == 1){
			/**************************/
			/*	Dynamically decide    */
			/**************************/
			status = DownloadTask.TASK_DOWNLOADING;
			startFuture = scheduledExecutor.schedule(startDownloadRn, 0, TimeUnit.MILLISECONDS);
			pauseFuture = scheduledExecutor.schedule(pauseDownloadRn, downloadTime, TimeUnit.SECONDS);
			loggingFuture = scheduledExecutor.scheduleAtFixedRate(controlRn, 0, Constants.LOGGING, TimeUnit.MILLISECONDS);
		}else if (restTime > 0 ){
			/**************************/
			/*	Statically decide     */
			/**************************/
			startFuture = scheduledExecutor.scheduleAtFixedRate(startDownloadRn, 0, downloadTime+restTime, TimeUnit.SECONDS);
			pauseFuture = scheduledExecutor.scheduleAtFixedRate(pauseDownloadRn, downloadTime, downloadTime+restTime, TimeUnit.SECONDS);
			loggingFuture = scheduledExecutor.scheduleAtFixedRate(loggingRn, 0, Constants.LOGGING, TimeUnit.MILLISECONDS);
		}else{
			status = DownloadTask.TASK_DOWNLOADING;
			startFuture = scheduledExecutor.schedule(startDownloadRn, 0, TimeUnit.SECONDS);
			loggingFuture = scheduledExecutor.scheduleAtFixedRate(loggingRn, 0, Constants.LOGGING, TimeUnit.MILLISECONDS);
		}
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
				urlConnection.connect();
				is = urlConnection.getInputStream();
				long contentLength = urlConnection.getContentLength();
				fileName = Constants.DEFAULT_STORE_DIRECTORY + UUID.randomUUID() + Utils.guessName(urlPath);
				fos = new FileOutputStream(fileName);
				byte data[] = new byte[1024];
				int len = 0;
				long progress = 0;
				while ((len = is.read(data)) != -1){
					
					byteDownloaded += len;
					fileSize += len;
					
					if (progress != byteDownloaded * 100 / contentLength){
						progress = byteDownloaded * 100 / contentLength;
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
		if (restTime <= 0 ){
			return (System.currentTimeMillis() - startFrom)/1000;
		}
		return this.effectTime;
	}

}
