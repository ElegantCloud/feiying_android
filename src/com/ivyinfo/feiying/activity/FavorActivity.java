package com.ivyinfo.feiying.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.ivyinfo.feiying.activity.subviews.ChanneVideoListActivity;
import com.ivyinfo.feiying.activity.subviews.MovieListActivity;
import com.ivyinfo.feiying.activity.subviews.SeriesListActivity;
import com.ivyinfo.feiying.activity.system.RegisterAndLoginActivity;
import com.ivyinfo.feiying.adapter.FavChannelListAdapter;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.AccountState;
import com.ivyinfo.feiying.constant.ActivityRequests;
import com.ivyinfo.feiying.constant.Channels;
import com.ivyinfo.feiying.constant.CommonConstants;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.constant.VideoTag;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;
import com.ivyinfo.feiying.listitemholder.Channel;
import com.ivyinfo.user.UserBean;
import com.ivyinfo.user.UserManager;

public class FavorActivity extends Activity {

	private FavChannelListAdapter listAdapter;

	private String favListUrl;
	private Handler messageHandler;

	private ProgressDialog progressDialog;

	int[] channelIDs = { Channels.movie.value(), Channels.series.value(),
			Channels.news.value(), Channels.fun.value(),
			Channels.music.value(), Channels.sports.value(),
			Channels.fashion.value(), Channels.entertainment.value(),
			Channels.variety.value() };
	int[] channelTitle = { R.string.movie, R.string.tv_series,
			R.string.information, R.string.fun, R.string.music,
			R.string.sports, R.string.fashion, R.string.entertainment,
			R.string.variety };
	int[] imgs = { R.drawable.dianying, R.drawable.dianshi, R.drawable.zixun,
			R.drawable.gaoxiao, R.drawable.music, R.drawable.tiyu,
			R.drawable.shishang, R.drawable.yule, R.drawable.zongyi };

	private JSONArray channelList;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	private void init() {
		UserBean user = UserManager.getInstance().getUser();
		if (user.getUserkey().equals("")) {
			setContentView(R.layout.fy_register_before_using_view);
			return;
		}

		setContentView(R.layout.favor_view);
		messageHandler = new MessageHandler(Looper.myLooper());

		listAdapter = new FavChannelListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.fav_list);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(channelListCL);

		favListUrl = getString(R.string.host)
				+ getString(R.string.favor_channel_getlist_url);

		loadUrl(favListUrl);
	}

	public void onLogin(View v) {
		Intent intent = new Intent();
		intent.setClass(FavorActivity.this, RegisterAndLoginActivity.class);
		intent.putExtra(CommonConstants.account_state.name(),
				AccountState.account_set.name());
		startActivityForResult(intent, ActivityRequests.REQ_LOGIN);
	}

	private void refreshChannelList(JSONArray jsonArrFromServer) {
		channelList = new JSONArray();
		for (int i = 0; i < channelIDs.length; i++) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put(Channel.CHANNEL_ID, channelIDs[i]);
				jsonObject.put(Channel.TITLE, getString(channelTitle[i]));
				jsonObject.put(Channel.IMGPATH, imgs[i]);
				jsonObject.put(Channel.COUNT,
						getCountByChannel(channelIDs[i], jsonArrFromServer));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			channelList.put(jsonObject);
		}
	}

	private int getCountByChannel(int channelID, JSONArray jsonArrFromServer) {
		int count = 0;
		if (jsonArrFromServer != null) {
			try {
				for (int i = 0; i < jsonArrFromServer.length(); i++) {
					JSONObject obj = jsonArrFromServer.getJSONObject(i);
					int channel = obj.getInt(VideoConstants.channel.name());
					if (channelID == channel) {
						count = obj.getInt(Channel.COUNT);
						break;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return count;

	}

	protected void loadUrl(String url) {
		Log.d("feiying", "loadUrl: " + url);
		progressDialog = ProgressDialog.show(this, null,
				getString(R.string.getting_fav_data), true);
		HttpUtils.startHttpPostRequestWithSignature(url, null, resLis, null);
	}

	protected void refreshList() {
		listAdapter.removeAll();
		loadUrl(favListUrl);
	}

	private ResponseListener resLis = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Log.d("feiying", "resLis - onComplete - status: " + status
					+ " response: " + responseText);
			Message message = Message.obtain();
			switch (status) {
			case 200:
				try {
					JSONObject jsonobj = new JSONObject(responseText);

					JSONArray channelArr = jsonobj.getJSONArray("list");
					refreshChannelList(channelArr);

					message.obj = channelList;
					message.what = MsgCodeDefine.MSG_ON_GET_DATA_RETURN;

				} catch (JSONException e) {
					e.printStackTrace();
					message.what = MsgCodeDefine.MSG_ERROR;
				}
				break;
			case 400:
				// alert user to re-login
				messageHandler
						.sendEmptyMessage(MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN);

				break;
			default:
				message.what = MsgCodeDefine.MSG_ERROR;
				break;
			}
			messageHandler.sendMessage(message);
		}

	};

	private OnItemClickListener channelListCL = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			JSONObject obj = (JSONObject) listAdapter.getItem(position);
			try {
				String title = getString(R.string.fav) + "-"
						+ obj.getString(Channel.TITLE);
				int channelID = obj.getInt(Channel.CHANNEL_ID);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("channeltitle", title);
				bundle.putString("path",
						getString(R.string.favor_video_list_url));
				bundle.putString(VideoConstants.video_tag.name(),
						VideoTag.favor.name());
				if (channelID == Channels.movie.value()) {
					intent.setClass(FavorActivity.this, MovieListActivity.class);
				} else if (channelID == Channels.series.value()) {
					intent.setClass(FavorActivity.this,
							SeriesListActivity.class);
				} else {
					intent.setClass(FavorActivity.this,
							ChanneVideoListActivity.class);
					bundle.putInt(Channel.CHANNEL_ID, channelID);
				}
				intent.putExtras(bundle);
				startActivityForResult(intent, ActivityRequests.REQ_OPEN_LIST);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	};

	class MessageHandler extends Handler {
		public MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			switch (message.what) {
			case MsgCodeDefine.MSG_ON_GET_DATA_RETURN:
				JSONArray channelArr = (JSONArray) message.obj;
				listAdapter.setChannelData(channelArr);
				break;
			case MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN:
				new AlertDialog.Builder(FavorActivity.this)
						.setTitle(R.string.alert_title)
						.setMessage(R.string.account_need_relogin)
						.setPositiveButton(R.string.account_setting,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										onLogin(null);
									}
								}).setNegativeButton(R.string.cancel, null)
						.show();
				break;
			default:
				Toast.makeText(FavorActivity.this, R.string.get_data_failed,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ActivityRequests.REQ_OPEN_LIST:
			if (resultCode == ActivityRequests.ON_UNFAVORED) {
				refreshList();
			}
			break;
		case ActivityRequests.REQ_LOGIN:
			init();
			break;
		}
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle(R.string.alert_title)
				.setMessage(R.string.if_exit)
				.setPositiveButton(R.string.exit, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}
}
