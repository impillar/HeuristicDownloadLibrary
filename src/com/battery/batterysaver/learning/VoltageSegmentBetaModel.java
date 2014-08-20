package com.battery.batterysaver.learning;

import com.battery.batterysaver.Constants;

import edu.ntu.cltk.data.PrimUtil;

public class VoltageSegmentBetaModel implements VoltageModel {

	private long[] voltageSamples = new long[Constants.SAMPLE_NUM];
	private long[] timeSamples = new long[Constants.SAMPLE_NUM];
	private int _size = 0;
	private double maxBeta = 0;
	private int timeUnit = 0;

	
	// For deciding when to stop downloading
	protected long preVoltage = 0;

	@Override
	public void startDownloadingInit() {
	}

	@Override
	public void stopDownloadingInit() {
		voltageSamples = new long[Constants.SAMPLE_NUM];
		timeSamples = new long[Constants.SAMPLE_NUM];
		_size = 0;
		timeUnit = 0;
	}
	
	private int insert(long vol){
		if (_size == Constants.SAMPLE_NUM && vol != voltageSamples[_size-1]){
			for (int i = 0 ; i < Constants.SAMPLE_NUM - 1; i++){
				this.voltageSamples[i] = this.voltageSamples[i+1];
			}
			this.voltageSamples[Constants.SAMPLE_NUM-1] = vol;
			return 1;
		}else if (_size == 0 || (_size < Constants.SAMPLE_NUM && vol != voltageSamples[_size-1])){
			this.voltageSamples[_size++] = vol;
			return 1;
		}
		return 0;
	}
	
	private int insert(long vol, long timestamp){
		if (_size == Constants.SAMPLE_NUM && timestamp - this.timeSamples[Constants.SAMPLE_NUM-1] >= 1000){
			for (int i = 0 ; i < Constants.SAMPLE_NUM - 1; i++){
				this.timeSamples[i] = this.timeSamples[i+1];
				this.voltageSamples[i] = this.voltageSamples[i+1];
			}
			this.voltageSamples[Constants.SAMPLE_NUM-1] = vol;
			this.timeSamples[Constants.SAMPLE_NUM-1] = timestamp;
			return 1;
		}else if (_size == 0 || (_size < Constants.SAMPLE_NUM && timestamp - this.timeSamples[_size-1] > 1000)){
			this.voltageSamples[_size] = vol;
			this.timeSamples[_size++] = timestamp;
			return 1;
		}
		return 0;
	}

	protected double calBeta() {
		if(_size != Constants.SAMPLE_NUM)	return 0.0;
		double numerator = 0;
		double denominator = 0;
		double timeSum = 0, volSum = 0;
		for (int i = 0 ; i < Constants.SAMPLE_NUM; i++){
			//timeSum += (i + 1);
			timeSum += this.timeSamples[i] - this.timeSamples[0];
			volSum += voltageSamples[i];
		}
		
		for (int i = 0; i < Constants.SAMPLE_NUM; i++){
			numerator += ( Constants.SAMPLE_NUM * (this.timeSamples[i]-this.timeSamples[0]) - timeSum ) * (Constants.SAMPLE_NUM * this.voltageSamples[i] - volSum );
			denominator += Math.pow(Constants.SAMPLE_NUM * (timeSamples[i]-this.timeSamples[0]) - timeSum, 2);
		}
		if (PrimUtil.equalsToZero(denominator))
			return 0.0;
		return numerator / denominator;
	}

	@Override
	public boolean resting(long vol) {
		
		timeUnit += insert(vol);
		
		double beta = calBeta();
		
		System.out.println("beta:"+beta);
		if (beta > maxBeta) {
			maxBeta = beta;
		}
		if (beta <= Constants.DECLINE_STEP / Constants.PAR_FOR_BETA
				&& timeUnit > Constants.SAMPLE_NUM/* && timeUnit > Constants.STARTING_POINT * 1.0 / Constants.LOGGING*/) {
			return false;
		}
		return true;
	}
	
	public boolean resting(long vol, long timestamp) {
		
		int ret = insert(vol, timestamp);
		timeUnit += ret;
		
		double beta = calBeta();
		
		if (ret == 1)
			System.out.println("beta:"+beta*1000);
		if (beta > maxBeta) {
			maxBeta = beta;
		}
		if (beta * 1000 <= Constants.DECLINE_STEP / Constants.PAR_FOR_BETA
				&& timeUnit > Constants.SAMPLE_NUM/* && timeUnit > Constants.STARTING_POINT * 1.0 / Constants.LOGGING*/) {
			return false;
		}
		return true;
	}

	@Override
	public boolean working(long vol) {
		if (preVoltage == 0) {
			preVoltage = vol;
		} else if (preVoltage - vol >= Constants.DECLINE_STEP) {
			preVoltage = vol;
			return false;
		}
		return true;
	}

	@Override
	public String printStatus() {
		StringBuilder sb = new StringBuilder("{");
		sb.append(String.format("PV:%d, TU:%d, BT:%.2f, MBT:%.2f", preVoltage,
				timeUnit, calBeta(), maxBeta));
		sb.append("}");
		return sb.toString();
	}

}
