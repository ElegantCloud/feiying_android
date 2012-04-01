package com.ivyinfo.feiying.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.ListView;

import com.ivyinfo.feiying.adapter.VideoListAdapter;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.Channels;

public class HomeActivity extends BaseVideoListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_view);

		listAdapter = new VideoListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.home_video_list);
		listView.addFooterView(moreBtView);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(videoListCL);

		url = host + getString(R.string.video_list) + "/" + Channels.video.value();
		loadUrl(url);

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