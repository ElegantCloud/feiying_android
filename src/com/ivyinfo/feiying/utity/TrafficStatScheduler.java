package com.ivyinfo.feiying.utity;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class TrafficStatScheduler {

	private Timer timer;
	private TimerTask task;
	private static TrafficStatScheduler instance;

	private TrafficStatScheduler() {
		timer = new Timer();
		task = new TrafficStatTimerTask();
	}

	public static TrafficStatScheduler getInstance() {
		if (instance == null) {
			instance = new TrafficStatScheduler();
		}
		return instance;
	}

	public void start() {
		Log.d("traffic", "start task");
		long time = 1000L;
		timer.schedule(task, 1, time);
	}

	public void stop() {
		timer.cancel();
	}

	class TrafficStatTimerTask extends TimerTask {

		@Override
		public void run() {
//			long bytes = TrafficStats.getUidRxBytes(android.os.Process.myUid());
//			print("getUidRxBytes", bytes);
//			bytes = TrafficStats.getUidTxBytes(android.os.Process.myUid());
//			print("getUidTxBytes", bytes);
//			bytes = TrafficStats.getMobileRxBytes();
//			print("getMobileRxBytes", bytes);
//			bytes = TrafficStats.getMobileTxBytes();
//			print("getMobileTxBytes", bytes);
//			bytes = TrafficStats.getTotalRxBytes();
//			print("getTotalRxBytes", bytes);
//			bytes = TrafficStats.getTotalTxBytes();
//			print("getTotalTxBytes", bytes);
			
//			long bytes = TrafficStatsFile.getMobileRxBytes();
//			print("TrafficStatsFile.getMobileRxBytes", bytes);
			
			
			
		}
		
		private void print(String info, long bytes) {
			int kb = (int) (bytes / 1024);
			int mb = kb / 1024;
			Log.d("traffic", info + " bytes: " + bytes + " KB: " + kb + " MB: " + mb);
		}
	}
}
