package com.ivyinfo.feiying.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.ivyinfo.contact.ContactManager;
import com.ivyinfo.contact.ContactManagerFactory;
import com.ivyinfo.contact.beans.Contact;
import com.ivyinfo.feiying.activity.subviews.MovieDetailActivity;
import com.ivyinfo.feiying.activity.subviews.SeriesDetailActivity;
import com.ivyinfo.feiying.activity.subviews.VideoDetailActivity;
import com.ivyinfo.feiying.activity.system.RegisterAndLoginActivity;
import com.ivyinfo.feiying.adapter.ShareVideoListAdapter;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.AccountState;
import com.ivyinfo.feiying.constant.ActivityRequests;
import com.ivyinfo.feiying.constant.Channels;
import com.ivyinfo.feiying.constant.CommonConstants;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.ShareCategory;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.constant.VideoTag;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;
import com.ivyinfo.user.UserBean;
import com.ivyinfo.user.UserManager;

public class ShareActivity extends BaseListActivity {
	protected Handler messageHandler;
	private Button shareComeBt;
	private Button shareGoBt;

	private ListView listView;
	private ShareVideoListAdapter receiveListAdapter;
	private ShareVideoListAdapter sendListAdapter;

	private ShareCategory shareCategory;
	private String receivePeopleListUrl;
	private String sendPeopleListUrl;

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

		setContentView(R.layout.share_view);
		messageHandler = new MessageHandler(Looper.myLooper());

		shareComeBt = (Button) findViewById(R.id.share_come_bt);
		shareGoBt = (Button) findViewById(R.id.share_go_bt);
		shareComeBt.setOnTouchListener(btOTL);
		shareGoBt.setOnTouchListener(btOTL);

		sendPeopleListUrl = host
				+ getString(R.string.share_send_video_list_url);
		receivePeopleListUrl = host
				+ getString(R.string.share_receive_video_list_url);

		listView = (ListView) findViewById(R.id.share_video_list);
		listView.addFooterView(moreBtView);
		listView.setOnItemClickListener(listOICL);

		receiveListAdapter = new ShareVideoListAdapter(this);
		sendListAdapter = new ShareVideoListAdapter(this);

		refreshReceiveList();
	}

	public void onLogin(View v) {
		Intent intent = new Intent();
		intent.setClass(ShareActivity.this, RegisterAndLoginActivity.class);
		intent.putExtra(CommonConstants.account_state.name(),
				AccountState.account_set.name());
		startActivityForResult(intent, ActivityRequests.REQ_LOGIN);
	}

	protected void loadUrl(String url, ResponseListener resRL) {
		Log.d("feiying", "loadUrl: " + url);
		showLoadingMoreProgressbar();
		HttpUtils.startHttpPostRequestWithSignature(url, null, resRL, null);
	}

	private void refreshSendList() {
		shareCategory = ShareCategory.share_send;
		videoTag = VideoTag.share_go.name();
		listView.setAdapter(sendListAdapter);
		sendListAdapter.clear();
		sendListAdapter.setShareCategory(ShareCategory.share_send);
		loadUrl(sendPeopleListUrl, sendListResLis);
	}

	private void refreshReceiveList() {
		shareCategory = ShareCategory.share_receive;
		videoTag = VideoTag.share_come.name();
		listView.setAdapter(receiveListAdapter);
		receiveListAdapter.clear();
		receiveListAdapter.setShareCategory(ShareCategory.share_receive);
		loadUrl(receivePeopleListUrl, receiveListResLis);
	}

	private ResponseListener sendListResLis = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Log.d("feiying", "resLis - onComplete - status: " + status
					+ " response: " + responseText);
			switch (status) {
			case 200:
				try {
					JSONObject jsonObject = new JSONObject(responseText);
					JSONObject jsonPager = jsonObject.getJSONObject("pager");
					nextPageURL = jsonPager.getString(VideoConstants.nextPage
							.name());
					hasNextPage = jsonPager.getBoolean("hasNext");
					
					Message message = Message.obtain();
					message.obj = jsonObject;
					message.what = MsgCodeDefine.MSG_REFRESH_SEND_LIST;
					messageHandler.sendMessage(message);

				} catch (JSONException e) {
					e.printStackTrace();
					nextPageURL = "";
					showNoMoreItemInfo();
				}
				break;

			case 400:
				// alert user to re-login
				messageHandler
						.sendEmptyMessage(MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN);
				break;

			default:
				showNoMoreItemInfo();
				break;
			}
		}
	};

	private ResponseListener receiveListResLis = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Log.d("feiying", "resLis - onComplete - status: " + status
					+ " response: " + responseText);
			switch (status) {
			case 200:
				try {
					JSONObject jsonObject = new JSONObject(responseText);
					JSONObject jsonPager = jsonObject.getJSONObject("pager");
					nextPageURL = jsonPager.getString(VideoConstants.nextPage
							.name());
					hasNextPage = jsonPager.getBoolean("hasNext");
					
					Message message = Message.obtain();
					message.obj = jsonObject;
					message.what = MsgCodeDefine.MSG_REFRESH_RECEIVE_LIST;
					messageHandler.sendMessage(message);

				} catch (JSONException e) {
					e.printStackTrace();
					nextPageURL = "";
					showNoMoreItemInfo();
				}
				break;
			case 400:
				// alert user to re-login
				messageHandler
						.sendEmptyMessage(MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN);

				break;
			default:
				showNoMoreItemInfo();
				break;
			}
		}
	};

	private OnItemClickListener listOICL = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (shareCategory) {
			case share_receive: {
				if (position < receiveListAdapter.getCount()) {

					JSONObject video = (JSONObject) receiveListAdapter
							.getItem(position);
					try {
						openVideoDetail(video);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else if (position >= receiveListAdapter.getCount()) {
					// load more videos
					showLoadingMoreProgressbar();

					if (hasNextPage) {
						loadUrl(host + nextPageURL, receiveListResLis);
					} else {
						showNoMoreItemInfo();
					}

				}
			}
				break;
			case share_send: {
				if (position < sendListAdapter.getCount()) {
					JSONObject video = (JSONObject) sendListAdapter
							.getItem(position);
					try {
						openVideoDetail(video);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else if (position >= sendListAdapter.getCount()) {
					// load more videos
					showLoadingMoreProgressbar();

					if (hasNextPage) {
						loadUrl(host + nextPageURL, sendListResLis);
					} else {
						showNoMoreItemInfo();
					}

				}
			}
				break;
			}

		}

		private void openVideoDetail(JSONObject videoInfo) throws JSONException {
			String sourceId = videoInfo.getString(VideoConstants.source_id
					.name());
			int channel = videoInfo.getInt(VideoConstants.channel.name());
			int shareID = videoInfo.getInt(VideoConstants.share_id.name());
			String sender = "";
			try {
				long send = videoInfo.getLong(VideoConstants.send.name());
				sender = Long.toString(send);
			} catch (JSONException e) {
				sender = UserManager.getInstance().getUser().getName();
			}
			ContactManager cm = ContactManagerFactory.getContactManager();
			Contact senderContact = cm.getContactByPhone(sender);

			long dateTime = videoInfo.getLong(VideoConstants.share_time.name());
			dateTime *= 1000;

			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString(VideoConstants.source_id.name(), sourceId);
			bundle.putString(VideoConstants.video_tag.name(), videoTag);
			bundle.putInt(VideoConstants.share_id.name(), shareID);
			bundle.putString(VideoConstants.send.name(),
					senderContact.getDisplayName());
			bundle.putString(VideoConstants.date.name(), DateUtils
					.formatDateTime(ShareActivity.this, dateTime,
							DateUtils.FORMAT_NUMERIC_DATE));

			if (channel == Channels.movie.channelID()) {
				// movie
				intent.setClass(ShareActivity.this, MovieDetailActivity.class);
			} else if (channel == Channels.series.channelID()) {
				// tv series
				intent.setClass(ShareActivity.this, SeriesDetailActivity.class);
			} else {
				// other video
				intent.setClass(ShareActivity.this, VideoDetailActivity.class);
			}
			intent.putExtras(bundle);
			startActivityForResult(intent, ActivityRequests.REQ_OPEN_DETAIL);
		}

	};

	private OnTouchListener btOTL = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				v.setBackgroundResource(R.drawable.blue_bg);
			} else if (event.getAction() == MotionEvent.ACTION_CANCEL
					|| event.getAction() == MotionEvent.ACTION_UP) {
				switch (shareCategory) {
				case share_receive:
					setShareComeBtSelected();
					break;
				case share_send:
					setShareGoBtSelected();
					break;
				}
			}
			return false;
		}
	};

	class MessageHandler extends Handler {
		public MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MsgCodeDefine.MSG_REFRESH_RECEIVE_LIST:
				try {
					refreshReceiveList((JSONObject) message.obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (!hasNextPage) {
					showNoMoreItemInfo();
				} else {
					hideLoadingMoreProgressbar();
				}
				break;
			case MsgCodeDefine.MSG_REFRESH_SEND_LIST:
				try {
					refreshSendList((JSONObject) message.obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (!hasNextPage) {
					showNoMoreItemInfo();
				} else {
					hideLoadingMoreProgressbar();
				}
				break;
			case MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN:
				showNoMoreItemInfo();
				new AlertDialog.Builder(ShareActivity.this)
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
				break;
			}
		}
	}

	private void refreshSendList(JSONObject peopleObj) throws JSONException {
		JSONArray jsonList = peopleObj.getJSONArray("list");
		sendListAdapter.addVideoList(jsonList);
	}

	private void refreshReceiveList(JSONObject peopleObj) throws JSONException {
		JSONArray jsonList = peopleObj.getJSONArray("list");
		receiveListAdapter.addVideoList(jsonList);
	}

	public void onShareReceive(View v) {
		if (shareCategory == ShareCategory.share_receive) {
			return;
		}
		setShareComeBtSelected();
		refreshReceiveList();
	}

	public void onShareSend(View v) {
		if (shareCategory == ShareCategory.share_send) {
			return;
		}
		setShareGoBtSelected();
		refreshSendList();
	}

	private void setShareComeBtSelected() {
		shareComeBt.setBackgroundResource(R.drawable.gray_bg);
		shareComeBt.setTextColor(getResources().getColor(R.color.black));
		shareGoBt.setBackgroundResource(R.color.transparent);
		shareGoBt.setTextColor(getResources().getColor(R.color.white));
	}

	private void setShareGoBtSelected() {
		shareComeBt.setBackgroundResource(R.color.transparent);
		shareComeBt.setTextColor(getResources().getColor(R.color.white));
		shareGoBt.setBackgroundResource(R.drawable.gray_bg);
		shareGoBt.setTextColor(getResources().getColor(R.color.black));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ActivityRequests.REQ_OPEN_DETAIL:
			if (resultCode == ActivityRequests.ON_DELETED) {
				switch (shareCategory) {
				case share_receive:
					refreshReceiveList();
					break;

				case share_send:
					refreshSendList();
					break;
				default:
					break;
				}

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