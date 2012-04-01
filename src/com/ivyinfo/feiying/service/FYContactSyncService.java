package com.ivyinfo.feiying.service;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import com.ivyinfo.contact.ContactManager;
import com.ivyinfo.contact.ContactManagerFactory;

public class FYContactSyncService extends Service {
	private ServiceHandler mServiceHandler;

	private ContentObserver mObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// 当联系人表发生变化时进行相应的操作
			Log.d("feiying", "Contacts Modified");

			ContactManager cm = ContactManagerFactory.getContactManager();
			cm.setIsModifyFlag(true);
			cm.getAllContactsByNameSort();
		}
	};

	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceHandler = new ServiceHandler(thread.getLooper());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// If we get killed, after returning from here, restart
		Log.d("feiying", "FYContactSyncService started");

		mServiceHandler.sendEmptyMessage(0);

		getContentResolver().registerContentObserver(
				ContactsContract.Contacts.CONTENT_URI, true, mObserver);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(mObserver);
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				Log.d("feiying", "init contact manager");
				ContactManager cm = ContactManagerFactory.getContactManager();
				if (cm == null) {
					ContactManagerFactory
							.initContactManager(FYContactSyncService.this);
					cm = ContactManagerFactory.getContactManager();
				}
				cm.getAllContactsByNameSort();
			}
		}
	}
}
