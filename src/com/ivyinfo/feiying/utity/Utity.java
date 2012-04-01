package com.ivyinfo.feiying.utity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.SmsManager;

public class Utity {
	/**
	 * 从服务器取图片
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getHttpBitmap(String url) {
		URL myFileUrl = null;
		Bitmap bitmap = null;
		try {
			myFileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setConnectTimeout(0);
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			bitmap = null;
		}
		return bitmap;
	}

	/**
	 * send SMS via intent
	 * 
	 * @param toNum
	 * @param content
	 * @param context
	 */
	public static void sendSMS(String toNum, String content, Context context,
			BroadcastReceiver recv) {

		String SENT_SMS_ACTION = "SENT_SMS_ACTION";
		Intent sentIntent = new Intent(SENT_SMS_ACTION);
		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
				sentIntent, 0);
		// register the Broadcast Receivers
		BroadcastReceiver receiver = null;
		if (recv != null) {
			receiver = recv;
		} else {
			receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context _context, Intent _intent) {
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						break;
					}
				}
			};
		}
		context.registerReceiver(receiver, new IntentFilter(SENT_SMS_ACTION));

		// 直接调用短信接口发短信
		SmsManager smsManager = SmsManager.getDefault();
		List<String> divideContents = smsManager.divideMessage(content);
		for (String text : divideContents) {
			smsManager.sendTextMessage(toNum, null, text, sentPI, null);
		}

//		context.unregisterReceiver(receiver);
	}

	/**
	 * send SMS to multiple numbers
	 * 
	 * @param numList
	 * @param content
	 * @param context
	 */
	public static void sendMultiSMS(List<String> numList, String content,
			Context context, BroadcastReceiver recv) {
		if (numList != null) {
			for (String number : numList) {
				sendSMS(number, content, context, recv);
			}
		}
	}
}
