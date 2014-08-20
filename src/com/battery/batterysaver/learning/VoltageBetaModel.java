package com.battery.batterysaver.learning;

import com.battery.batterysaver.Constants;

public class VoltageBetaModel implements VoltageModel{

	// For deciding when to start downloading
	// Beta = avg(t * v) - avg(t) * avg(v)
	protected int timeUnit = 0;
	protected double sumTbyV = 0;
	protected double sumT = 0;
	protected double sumV = 0;
	protected double maxBeta = 0;
	protected double sumTPower = 0;
	
	
	// For deciding when to stop downloading
	protected long preVoltage = 0;
	
	@Override
	public void startDownloadingInit() {
		timeUnit = 0;
		maxBeta = 0;
		sumTbyV = 0;
		sumT = 0;
		sumV = 0;
		sumTPower = 0;
	}
	
	@Override
	public void stopDownloadingInit(){
		
	}

	protected double calBeta(){
		if ( sumTPower / timeUnit - (sumT / timeUnit) * (sumT / timeUnit) == 0) 	return 0;
		return (sumTbyV / timeUnit - ( sumT / timeUnit ) * ( sumV / timeUnit ))
				/ ( sumTPower / timeUnit - (sumT / timeUnit) * (sumT / timeUnit)); 
	}
	
	@Override
	public boolean resting(long vol) {
		timeUnit++;
		sumTbyV += vol * timeUnit;
		sumT += timeUnit;
		sumV += vol;
		sumTPower += timeUnit * timeUnit;
		
		double beta = calBeta();
		if (beta > maxBeta){
			maxBeta = beta;
		}else if (maxBeta != 0 && /*beta / maxBeta <= 0.5*/ beta <= Constants.DECLINE_STEP/Constants.PAR_FOR_BETA && timeUnit >= Constants.STARTING_POINT * 1.0 / Constants.LOGGING){
			return false;
		}
		return true;
	}

	@Override
	public boolean working(long vol) {
		if (preVoltage == 0){
			preVoltage = vol;
		}else if (preVoltage - vol >= Constants.DECLINE_STEP){
			preVoltage = vol;
			return false;
		}
		return true;
	}

	@Override
	public String printStatus() {
		StringBuilder sb = new StringBuilder("{");
		sb.append(String.format("PV:%d, TU:%d, BT:%.2f, MBT:%.2f", preVoltage, timeUnit, calBeta(), maxBeta));
		sb.append("}");
		return sb.toString();
	}

}
