package com.ivyinfo.feiying.activity.subviews;

import java.util.HashMap;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivyinfo.feiying.activity.BaseListActivity;
import com.ivyinfo.feiying.adapter.VideoSearchResultListAdapter;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.ActivityRequests;
import com.ivyinfo.feiying.constant.Channels;
import com.ivyinfo.feiying.constant.CommonConstants;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;

public class VideoSearchResultActivity extends BaseListActivity {
	private String keyword;
	private String searchUrl;

	private Handler messageHandler;

	private VideoSearchResultListAdapter listAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_search_result_list_view);
		messageHandler = new MessageHandler(Looper.myLooper());

		Intent intent = getIntent();
		keyword = intent.getStringExtra(CommonConstants.keyword.name());

		TextView titleTV = (TextView) findViewById(R.id.video_search_title);
		titleTV.setText(getString(R.string.search) + "：" + keyword);

		searchUrl = host + getString(R.string.search_url);

		listAdapter = new VideoSearchResultListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.video_search_result_list);
		listView.addFooterView(moreBtView);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(videoListCL);

		refreshList();
	}

	private void loadUrl(String url) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("searchTitle", keyword);
		HttpUtils.startHttpPostRequest(url, params, resLis, null);
	}

	private void refreshList() {
		showLoadingMoreProgressbar();
		listAdapter.clear();
		loadUrl(searchUrl);
	}

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

	private OnItemClickListener videoListCL = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position < listAdapter.getCount()) {

				JSONObject obj = (JSONObject) listAdapter.getItem(position);
				try {
					openVideoDetail(obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (position >= listAdapter.getCount()) {
				// load more movies
				showLoadingMoreProgressbar();

				if (!nextPageURL.equals("")) {
					loadUrl(host + nextPageURL);
				} else {
					showNoMoreItemInfo();
				}

			}

		}
	};
	private void openVideoDetail(JSONObject videoInfo) throws JSONException {
		String sourceId = videoInfo.getString(VideoConstants.source_id
				.name());
		int channel = videoInfo.getInt(VideoConstants.channel.name());

		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString(VideoConstants.source_id.name(), sourceId);

		if (channel == Channels.movie.value()) {
			// movie
			intent.setClass(VideoSearchResultActivity.this, MovieDetailActivity.class);
		} else if (channel == Channels.series.value()) {
			// tv series
			intent.setClass(VideoSearchResultActivity.this, SeriesDetailActivity.class);
		} else {
			// other video
			intent.setClass(VideoSearchResultActivity.this, VideoDetailActivity.class);
		}
		intent.putExtras(bundle);
		startActivityForResult(intent, ActivityRequests.REQ_OPEN_DETAIL);
	}
	class MessageHandler extends Handler {
		public MessageHandler(Looper looper) {
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
				if (nextPageURL.equals("")) {
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

	private void refreshVideoList(JSONObject obj) throws JSONException {
		JSONArray jsonVideoList = obj.getJSONArray("list");
		if (jsonVideoList.length() == 0) {
			Toast.makeText(VideoSearchResultActivity.this, R.string.no_search_result, Toast.LENGTH_SHORT).show();
		} else {
			listAdapter.addVideoList(jsonVideoList);
		}
	}

	public void onBack(View v) {
		finish();
	}

}
