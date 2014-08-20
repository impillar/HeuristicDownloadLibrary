package com.battery.batterysaver.learning;

import android.util.Log;

public class ProgressiveVoltageModel extends VoltageBetaModel {

	public static double recoveryRate = .8;
	public static final int MINUS = 0;
	public static final int PLUS  = 1;
	
	public long declineStep = 1;		//The unit is mV, which need to time 1000 when calculating

	@Override
	public boolean resting(long vol) {
		if (super.resting(vol) == false){
			//If the recovery rate exceeds the set one, we increase the decline step
			if (preVoltage * recoveryRate < vol){
				decideDeclineStep(PLUS);
			}else{
				decideDeclineStep(MINUS);
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean working(long vol) {
		Log.i("Working", String.format("Pre-Voltage: %d, Voltage: %d, Decline-Step: %d", preVoltage, vol, declineStep));
		if (preVoltage - vol >= declineStep * 1000){
			return false;
		}
		return true;
	}

	private void decideDeclineStep(int operate){
		if (operate == MINUS){
			if (declineStep > 1){
				declineStep--;
				Log.i("ProgressiveVoltageModel", "Decline Step: -1");
			}
		}else if (operate == PLUS){
			if (declineStep < Long.MAX_VALUE / 2){
				declineStep *= 2;
				Log.i("ProgressiveVoltageModel", "Decline Step: double");
			}
		}
	}

}
