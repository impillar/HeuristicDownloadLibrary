package com.battery.batterysaver.learning;

import java.util.ArrayList;
import java.util.List;

/**
 * The decision model is to decide when to stop resting and start downloading.
 * The model records the sampled voltage value, especially the incremental voltage.
 * At first, we carry on some experiments and get the statistics about the probability 
 * how many times the voltage increases, the voltage value is at peak. According to this 
 * probability, we choose to when to stop resting
 * @author pillar
 *
 */
public class VoltageIncrementModel implements VoltageModel {

	public static final int TIME_THRESHOLD = 1000 * 10;
	public static final int INCREMENT_TIME = 10;
	
	/**
	 * The probability:
	 * 1 - 0
	 * 2 - 0.05  when voltage increases two times, the probability of voltage being peak
	 */
	private double[] prob = new double[]{
			0,		//0
			0,		//1
			0.05,	//2
			0.25,	//3
			0.47,	//4
			0.33,	//5
			0.12,	//6
			0,		//7
			0,		//8
			0,		//9
			0		//10
	};
	private int count=0;
	private List<Long> history;
	private long startTime;
	private int increment = 0;
	
	public VoltageIncrementModel(){
		history = new ArrayList<Long>();
	}
	
	public void init(){
		startTime = System.currentTimeMillis();
		history = new ArrayList<Long>();
		increment = 0;
	}
	
	@Override
	public boolean resting(long num) {
		
		long current = (Long) num;
		long currentTime = System.currentTimeMillis();
		
		if (currentTime - startTime >= TIME_THRESHOLD)	return true;
		
		if (history.size() != 0 && current > history.get(history.size()-1)){
			increment++;
			if (increment > 10 || Math.random() < prob[increment]){
				return true;
			}
		}
		history.add(current);
		return false;
	}

	@Override
	public void startDownloadingInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopDownloadingInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean working(long vol) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String printStatus() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
