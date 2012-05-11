package com.ivyinfo.feiying.activity.other;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.ivyinfo.feiying.activity.system.RegisterAndLoginActivity;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.CommonConstants;
import com.ivyinfo.user.User;
import com.ivyinfo.user.UserBean;
import com.ivyinfo.user.UserManager;

public class SplashActivity extends Activity {
	private SharedPreferences userInfoSettings;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_view);
		userInfoSettings = getSharedPreferences(
				CommonConstants.user_info.name(), 0);
		String userName = userInfoSettings.getString(User.username, "");
		String userkey = userInfoSettings.getString(User.userkey, "");
		String status = userInfoSettings.getString(User.status, "unopened");
		UserBean user = UserManager.getInstance()
				.setUserInfo(userName, userkey);
		user.setStatus(status);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int nowWidth = dm.widthPixels; // 当前分辨率 宽度
		int nowHeigth = dm.heightPixels; // 当前分辨率高度
		Log.d("feiying", "width: " + nowWidth + " height: " + nowHeigth);

		// Log.d("feiying", "Model: " + Build.MODEL);
		// Log.d("feiying", "BOARD: " + Build.BOARD);
		// Log.d("feiying", "BRAND: " + Build.BRAND);
		// Log.d("feiying", "CPU_ABI: " + Build.CPU_ABI);
		// Log.d("feiying", "DEVICE: " + Build.DEVICE);
		// Log.d("feiying", "DISPLAY: " + Build.DISPLAY);
		// Log.d("feiying", "FINGERPRINT: " + Build.FINGERPRINT);
		// Log.d("feiying", "HOST: " + Build.HOST);
		// Log.d("feiying", "ID: " + Build.ID);
		// Log.d("feiying", "MANUFACTURER: " + Build.MANUFACTURER);
		// Log.d("feiying", "PRODUCT: " + Build.PRODUCT);
		// Log.d("feiying", "TAGS: " + Build.TAGS);
		// Log.d("feiying", "TIME: " + Build.TIME);
		// Log.d("feiying", "TYPE: " + Build.TYPE);
		// Log.d("feiying", "USER: " + Build.USER);
		// Log.d("feiying", "CODENAME: " + Build.VERSION.CODENAME);
		// Log.d("feiying", "INCREMENTAL: " + Build.VERSION.INCREMENTAL);
		// Log.d("feiying", "RELEASE: " + Build.VERSION.RELEASE);
		// Log.d("feiying", "SDK: " + Build.VERSION.SDK);
		// Log.d("feiying", "SDK_INT: " + Build.VERSION.SDK_INT);

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Intent intent = new Intent(SplashActivity.this,
						RegisterAndLoginActivity.class);
				startActivity(intent);
				SplashActivity.this.finish();
			}
		}).start();
	}
}
