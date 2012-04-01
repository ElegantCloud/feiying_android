package com.ivyinfo.feiying.activity.subviews;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ivyinfo.feiying.activity.BaseVideoListActivity;
import com.ivyinfo.feiying.adapter.VideoListAdapter;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.constant.VideoTag;
import com.ivyinfo.feiying.listitemholder.Channel;

public class ChanneVideoListActivity extends BaseVideoListActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_video_list_view);

		Intent intent = getIntent();
		int channelID =	intent.getIntExtra(Channel.CHANNEL_ID, 0);
		String channelTitle = intent.getStringExtra("channeltitle");
		String videoListPath = intent.getStringExtra("path");
		videoTag = intent.getStringExtra(VideoConstants.video_tag.name());
		if (videoTag == null) {
			videoTag = VideoTag.normal.name();
		}
		
		TextView channelTitleTV = (TextView) findViewById(R.id.channel_video_list_channelname);
		channelTitleTV.setText(channelTitle);

		listAdapter = new VideoListAdapter(this);
		
		ListView listView = (ListView) findViewById(R.id.channel_video_list);
		listView.addFooterView(moreBtView);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(videoListCL);

		url = host + videoListPath + "/" + channelID;
		loadUrl(url);

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
}