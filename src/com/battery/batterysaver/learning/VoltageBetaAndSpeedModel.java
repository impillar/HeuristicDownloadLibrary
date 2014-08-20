package com.battery.batterysaver.learning;

import java.util.logging.Logger;

import android.util.Log;

import com.battery.batterysaver.Constants;
import com.battery.batterysaver.logger.LoggerDebugger;

import edu.ntu.cltk.data.Percentile;
import edu.ntu.cltk.data.PrimUtil;

public class VoltageBetaAndSpeedModel extends VoltageBetaModel {

	private double instantSpeed;				//Instant speed. When determining if start downloading, using the average speed in the previous round, determining if stop downloading, using the average speed in the current round
	private double leftTime;					//The left time to complete the task
	private long currentFileSize = 0;			//Current size of downloaded file
	private long startFrom = 0;					//The time that the app is running from
	private long startSingleFrom = 0;			//The time that the app starts downloading in one round
	private long fileSizeSingleFrom = 0;		//The downloaded size of the file when the app starts downloading in one round
	private Percentile<Double> speed;
	
	private LoggerDebugger debuggerLogger = null;

	public static int UPPER_BOUND = 80;
	public static int LOWER_BOUND = 20;
	
	public static String TAG = VoltageBetaAndSpeedModel.class.getName();
	
	public VoltageBetaAndSpeedModel() {
		super();
		speed = new Percentile<Double>();
		debuggerLogger = LoggerDebugger.getInstance();
	}

	@Override
	public void startDownloadingInit(){
		super.startDownloadingInit();
		
		if (startFrom == 0){
			startFrom = System.nanoTime();
		}
		startSingleFrom = System.nanoTime();
		fileSizeSingleFrom = currentFileSize;
	}
	
	@Override
	public void stopDownloadingInit(){
		Long endSingleFrom = System.nanoTime();
		if (endSingleFrom - startSingleFrom == 0)	instantSpeed = 0;
		else		instantSpeed = 1.0 * (currentFileSize - fileSizeSingleFrom) / (endSingleFrom - startSingleFrom);
	}
	
	@Override
	public boolean resting(long vol) {
		//Re-calculate the left time
		leftTime = Constants.TESTING_SECOND - (System.nanoTime() - startFrom) / 1000000000;
		boolean sup = super.resting(vol);
		boolean file = (leftTime * speed.evaluate(UPPER_BOUND) > Constants.TESTING_FILE_SIZE - currentFileSize);
		//debuggerLogger.log("Resting(and) - Super: " + sup + ", File: " + file);
		return sup && file;
	}

	public double getInstantSpeed(){
		return this.instantSpeed;
	}
	
	public boolean working(long vol, long currentFileSize) {
		this.currentFileSize = currentFileSize;
		leftTime = Constants.TESTING_SECOND - (System.nanoTime() - startFrom) / 1000000000;
		double timeElapsed = (System.nanoTime() - startSingleFrom) / 1000000000.0;
		if (PrimUtil.equalsToZero(timeElapsed))	instantSpeed = 0;
		else	instantSpeed = (currentFileSize - fileSizeSingleFrom)/timeElapsed;
		speed.add(instantSpeed);
		//Log.i(TAG, String.format("Working - Speed: %.2f, Time: %.2f, %.2f <= %d", speed.evaluate(20), leftTime, speed.evaluate(20) * leftTime, (Constants.TESTING_FILE_SIZE - this.currentFileSize)));
		boolean sup = super.working(vol);
		boolean file = (leftTime * speed.evaluate(LOWER_BOUND) <= Constants.TESTING_FILE_SIZE - currentFileSize); 
		//debuggerLogger.log("Working(or) - Super: " + sup + ", File: " + file);
		return currentFileSize < Constants.TESTING_FILE_SIZE && (sup || file);
	}
}
