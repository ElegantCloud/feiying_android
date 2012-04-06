package com.ivyinfo.feiying.activity;

import java.util.zip.Inflater;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.CommonConstants;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;
import com.ivyinfo.feiying.service.FYContactSyncService;
import com.ivyinfo.feiying.utity.VersionManager;

public class FeiYingMainActivity extends TabActivity {
	/** Called when the activity is first created. */
	private MessageHandler messageHandler;
//	private String businessStatusGetUrl;
	
//	private SharedPreferences userInfoSettings;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		messageHandler = new MessageHandler(Looper.myLooper());
		VersionManager.localVersion = getString(R.string.version);
		VersionManager.updateURL = getString(R.string.host)
				+ getString(R.string.app_download_url);

		TabHost tabHost = getTabHost(); // The activity TabHost

		addTab(tabHost, "share", R.string.share, ShareActivity.class,
				R.drawable.ic_tab_share);
		addTab(tabHost, "favor", R.string.fav, FavorActivity.class,
				R.drawable.ic_tab_star);
		addTab(tabHost, "home", R.string.home, HomeActivity.class,
				R.drawable.ic_tab_home);
		addTab(tabHost, "channel", R.string.channel, ChannelActivity.class,
				R.drawable.ic_tab_channel);
		addTab(tabHost, "more", R.string.more, MoreActivity.class,
				R.drawable.ic_tab_more);

		tabHost.setCurrentTab(2);

		Intent intent = new Intent(this, FYContactSyncService.class);
		startService(intent);

		// IntentFilter networkStateChangedFilter = new IntentFilter();
		// networkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		checkVersion();
		
//		businessStatusGetUrl = getString(R.string.host) + getString(R.string.business_status_get_url);
//		
//		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
//			
//			@Override
//			public void onTabChanged(String tabId) {
//				HttpUtils.startHttpPostRequestWithSignature(businessStatusGetUrl, null, statusRL, null);
//			}
//		});
	}
	
//	private ResponseListener statusRL = new ResponseListener() {
//		
//		@Override
//		public void onComplete(int status, String responseText) {
//			try {
//				JSONObject obj = new JSONObject(responseText);
//				String businessStatus = obj.getString(User.status);
//				if (!businessStatus.equals(BusinessStatus.opened.name())) {
//					UserBean user = UserManager.getInstance().getUser();
//					user.setUserkey("");
//					user.setStatus(businessStatus);
//					saveUserAccount();
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//	};

//	private void saveUserAccount() {
//		UserBean user = UserManager.getInstance().getUser();
//		userInfoSettings = getSharedPreferences(
//				CommonConstants.user_info.name(), 0);
//		userInfoSettings.edit().putString(User.username, user.getName())
//				.putString(User.userkey, user.getUserkey())
//				.putString(User.status, user.getStatus()).commit();
//	}
	
	private void checkVersion() {
		HttpUtils.startHttpPostRequest(getString(R.string.host)
				+ getString(R.string.version_get_url), null, verRL, null);
	}

	private ResponseListener verRL = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Log.d("feiying", "verRL - onComplete - status: " + status
					+ " response: " + responseText);
			switch (status) {
			case 200:
				try {
					JSONObject jsonObject = new JSONObject(responseText);

					Message message = Message.obtain();
					message.obj = jsonObject;
					message.what = MsgCodeDefine.MSG_ON_GET_VERSION_RETURN;
					messageHandler.sendMessage(message);

				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}
	};

	public void addTab(TabHost tabHost, String tabStr, int title,
			Class<?> activity, int ic_tab_img) {
		Resources res = getResources(); // Resource object to get Drawables

		Intent intent = new Intent().setClass(this, activity);
//		TabHost.TabSpec spec = tabHost
//				.newTabSpec("more")
//				.setIndicator(getResources().getString(title),
//						res.getDrawable(ic_tab_img)).setContent(intent);
		View tabIndi = LayoutInflater.from(this).inflate(R.layout.tab_indicator_view, null);
		ImageView tabIcon = (ImageView) tabIndi.findViewById(R.id.tab_icon);
		TextView tabText = (TextView)tabIndi.findViewById(R.id.tab_text);
		tabText.setText(title);
		tabIcon.setImageResource(ic_tab_img);
		TabHost.TabSpec spec = tabHost.newTabSpec("more").setIndicator(tabIndi).setContent(intent);
		tabHost.addTab(spec);
	}

	class MessageHandler extends Handler {
		public MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MsgCodeDefine.MSG_ON_GET_VERSION_RETURN:
				JSONObject verObj = (JSONObject) message.obj;
				try {
					VersionManager.serverVerion = verObj
							.getString(CommonConstants.version.name());
					if (VersionManager.compareVersion(
							VersionManager.serverVerion,
							VersionManager.localVersion) > 0
							&& VersionManager.updateURL != null
							&& !VersionManager.updateURL.equals("")) {
						// prompt update dialog
						String detectNewVersion = getString(R.string.detect_new_version);
						detectNewVersion = String.format(detectNewVersion,
								VersionManager.serverVerion);

						new AlertDialog.Builder(FeiYingMainActivity.this)
								.setTitle(R.string.alert_title)
								.setMessage(detectNewVersion)
								.setPositiveButton(R.string.upgrade,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												startActivity(new Intent(
														Intent.ACTION_VIEW,
														Uri.parse(VersionManager.updateURL)));
											}
										})
								.setNegativeButton(R.string.cancel, null)
								.show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}
	}
}