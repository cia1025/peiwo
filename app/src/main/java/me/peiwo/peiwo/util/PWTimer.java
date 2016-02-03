package me.peiwo.peiwo.util;

import android.os.CountDownTimer;

public class PWTimer extends CountDownTimer {
	public int count;

	public PWTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
	}

	@Override
	public void onFinish() {
		CustomLog.d("syf", "PWTimer onFinish 1 ");
	}

	@Override
	public void onTick(long arg0) {
		count++;
		CustomLog.d("syf", "PWTimer arg0 = " + arg0 + ", count = " + count);
	}
}
