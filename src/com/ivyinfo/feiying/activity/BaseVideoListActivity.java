package com.ivyinfo.feiying.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.ivyinfo.feiying.activity.subviews.VideoDetailActivity;
import com.ivyinfo.feiying.activity.subviews.VideoSearchActivity;
import com.ivyinfo.feiying.adapter.VideoListAdapter;
import com.ivyinfo.feiying.constant.ActivityRequests;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.constant.VideoTag;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;

public class BaseVideoListActivity extends BaseListActivity {

	protected VideoListAdapter listAdapter;

	protected Handler messageHandler;

	protected String url = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		messageHandler = new BVLMessageHandler(Looper.myLooper());

	}

	protected void loadUrl(String url) {
		Log.d("feiying", "loadUrl: " + url);
		if (videoTag.equals(VideoTag.normal.name())) {
			HttpUtils.startHttpPostRequest(url, null, resLis, null);
		} else {
			HttpUtils
					.startHttpPostRequestWithSignature(url, null, resLis, null);
		}
	}

	protected void refreshList() {
		showLoadingMoreProgressbar();
		listAdapter.clear();
		loadUrl(url);
	}

	protected OnItemClickListener videoListCL = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position < listAdapter.getCount()) {

				JSONObject obj = (JSONObject) listAdapter.getItem(position);

				Intent intent = new Intent(BaseVideoListActivity.this,
						VideoDetailActivity.class);
				Bundle bundle = new Bundle();
				try {
					bundle.putString(VideoConstants.source_id.name(),
							obj.getString(VideoConstants.source_id.name()));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				bundle.putString(VideoConstants.video_tag.name(), videoTag);
				intent.putExtras(bundle);
				startActivityForResult(intent, ActivityRequests.REQ_OPEN_DETAIL);
			} else if (position >= listAdapter.getCount()) {
				// if (!UserManager.getInstance().getUser().getStatus()
				// .equals(BusinessStatus.opened.name())) {
				// new AlertDialog.Builder(BaseVideoListActivity.this)
				// .setTitle(R.string.alert_title)
				// .setMessage(R.string.register_before_using)
				// .setPositiveButton(R.string.account_setting,
				// new DialogInterface.OnClickListener() {
				//
				// @Override
				// public void onClick(DialogInterface dialog,
				// int which) {
				// Intent intent = new Intent();
				// intent.setClass(BaseVideoListActivity.this,
				// RegisterAndLoginActivity.class);
				// intent.putExtra(
				// CommonConstants.account_state
				// .name(),
				// AccountState.account_set.name());
				// startActivityForResult(intent,
				// ActivityRequests.REQ_LOGIN);
				// }
				// }).setNegativeButton(R.string.cancel, null)
				// .show();
				//
				// return;
				// }

				// load more videos
				showLoadingMoreProgressbar();

				if (hasNextPage) {
					loadUrl(host + nextPageURL);
				} else {
					showNoMoreItemInfo();
				}

			}
		}
	};

	private ResponseListener resLis = new ResponseListener() {

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
					message.what = MsgCodeDefine.MSG_REFRESH_VIDEO_LIST;
					messageHandler.sendMessage(message);

				} catch (JSONException e) {
					e.printStackTrace();
					nextPageURL = "";
					showNoMoreItemInfo();
				}
				break;

			default:
				showNoMoreItemInfo();
				break;
			}
		}
	};

	class BVLMessageHandler extends Handler {
		public BVLMessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MsgCodeDefine.MSG_REFRESH_VIDEO_LIST:
				try {
					refreshVideoList((JSONObject) message.obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (!hasNextPage) {
					showNoMoreItemInfo();
				} else {
					hideLoadingMoreProgressbar();
				}
				break;

			default:
				break;
			}
		}
	}

	private void refreshVideoList(JSONObject jsonObject) throws JSONException {
		Log.d("feiying", "refreshVideoList");

		JSONArray jsonVideoList = jsonObject.getJSONArray("list");
		listAdapter.addVideoList(jsonVideoList);
	}

	public void onSearchClick(View view) {
		Intent intent = new Intent(this, VideoSearchActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ActivityRequests.REQ_OPEN_DETAIL:
			if (resultCode == ActivityRequests.ON_UNFAVORED) {
				currentListStatus = ActivityRequests.ON_UNFAVORED;
				refreshList();
			}
			break;
		}
	}
}
