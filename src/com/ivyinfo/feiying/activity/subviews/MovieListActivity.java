package com.ivyinfo.feiying.activity.subviews;

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

import com.ivyinfo.feiying.activity.BaseListActivity;
import com.ivyinfo.feiying.adapter.MovieListAdapter;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.ActivityRequests;
import com.ivyinfo.feiying.constant.Channels;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.constant.VideoTag;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;

public class MovieListActivity extends BaseListActivity {
	protected MovieListAdapter listAdapter;

	protected Handler messageHandler;

	protected int currentListStatus;

	private String url;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.channel_movie_list_view);
		messageHandler = new MessageHandler(Looper.myLooper());

		Intent intent = getIntent();
		String channelTitle = intent.getStringExtra("channeltitle");
		String movieListPath = intent.getStringExtra("path");
		videoTag = intent.getStringExtra(VideoConstants.video_tag.name());
		if (videoTag == null) {
			videoTag = VideoTag.normal.name();
		}

		TextView channelTitleTV = (TextView) findViewById(R.id.channel_movie_list_channelname);
		channelTitleTV.setText(channelTitle);

		listAdapter = new MovieListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.channel_movie_list);
		listView.addFooterView(moreBtView);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(movieListCL);

		currentListStatus = ActivityRequests.ON_NORMAL;

		url = host + movieListPath + "/" + Channels.movie.value();
		loadUrl(url);
	}

	protected void loadUrl(String url) {
		if (videoTag.equals(VideoTag.normal.name())) {
			HttpUtils.startHttpPostRequest(url, null, resLis, null);
		} else {
			HttpUtils.startHttpPostRequestWithSignature(url, null, resLis, null);
		}
	}

	/**
	 * re-get movie list and refresh UI
	 */
	protected void refreshList() {
		showLoadingMoreProgressbar();
		listAdapter.clear();
		loadUrl(url);
	}

	protected OnItemClickListener movieListCL = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position < listAdapter.getCount()) {

				JSONObject obj = (JSONObject) listAdapter.getItem(position);

				Intent intent = new Intent(MovieListActivity.this,
						MovieDetailActivity.class);
				Bundle bundle = new Bundle();
				try {
					bundle.putString(VideoConstants.source_id.name(),
							obj.getString(VideoConstants.source_id.name()));
					bundle.putString(VideoConstants.video_tag.name(), videoTag);
					intent.putExtras(bundle);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				startActivityForResult(intent, ActivityRequests.REQ_OPEN_DETAIL);
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

	class MessageHandler extends Handler {
		public MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MsgCodeDefine.MSG_REFRESH_VIDEO_LIST:
				try {
					refreshMovieList((JSONObject) message.obj);
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

	private void refreshMovieList(JSONObject jsonObject) throws JSONException {
		Log.d("feiying", "refreshMovieList");

		JSONArray jsonMovieList = jsonObject.getJSONArray("list");
		listAdapter.addVideoList(jsonMovieList);
	}

	public void onSearchClick(View view) {
		Intent intent = new Intent(this, VideoSearchActivity.class);
		startActivity(intent);
	}

	public void onBack(View v) {
		Intent intent = this.getIntent();
		setResult(currentListStatus, intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		onBack(null);
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
