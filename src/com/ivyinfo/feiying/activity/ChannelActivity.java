package com.ivyinfo.feiying.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ivyinfo.feiying.activity.subviews.ChanneVideoListActivity;
import com.ivyinfo.feiying.activity.subviews.MovieListActivity;
import com.ivyinfo.feiying.activity.subviews.SeriesListActivity;
import com.ivyinfo.feiying.activity.subviews.VideoSearchActivity;
import com.ivyinfo.feiying.adapter.ChannelListAdapter;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.Channels;
import com.ivyinfo.feiying.listitemholder.Channel;

public class ChannelActivity extends Activity {
	private ChannelListAdapter listAdapter;
	int[] channelIDs = {/* Channels.movie.channelID(), Channels.series.channelID(),*/
			Channels.news.channelID(), Channels.fun.channelID(),
			Channels.music.channelID(), Channels.sports.channelID(),
			Channels.fashion.channelID(), Channels.entertainment.channelID(),
			Channels.variety.channelID() };
	int[] channelTitle = {/* R.string.movie, R.string.tv_series,*/
			R.string.information, R.string.fun, R.string.music,
			R.string.sports, R.string.fashion, R.string.entertainment,
			R.string.variety };
	int[] imgs = { /*R.drawable.dianying, R.drawable.dianshi, */ R.drawable.zixun,
			R.drawable.gaoxiao, R.drawable.music, R.drawable.tiyu,
			R.drawable.shishang, R.drawable.yule, R.drawable.zongyi };

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_list_view);

		listAdapter = new ChannelListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.channel_list);
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < channelIDs.length; i++) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put(Channel.CHANNEL_ID, channelIDs[i]);
				jsonObject.put(Channel.TITLE, getString(channelTitle[i]));
				jsonObject.put(Channel.IMGPATH, imgs[i]);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsonArray.put(jsonObject);
		}
		listAdapter.setChannelData(jsonArray);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(channelListCL);
	}

	private OnItemClickListener channelListCL = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
//			if (!UserManager.getInstance().getUser().getStatus()
//					.equals(BusinessStatus.opened.name())) {
//				new AlertDialog.Builder(ChannelActivity.this)
//						.setTitle(R.string.alert_title)
//						.setMessage(R.string.register_before_using)
//						.setPositiveButton(R.string.account_setting,
//								new DialogInterface.OnClickListener() {
//
//									@Override
//									public void onClick(DialogInterface dialog,
//											int which) {
//										Intent intent = new Intent();
//										intent.setClass(ChannelActivity.this,
//												RegisterAndLoginActivity.class);
//										intent.putExtra(
//												CommonConstants.account_state
//														.name(),
//												AccountState.account_set.name());
//										startActivityForResult(intent,
//												ActivityRequests.REQ_LOGIN);
//									}
//								}).setNegativeButton(R.string.cancel, null)
//						.show();
//
//				return;
//			}

			JSONObject obj = (JSONObject) listAdapter.getItem(position);
			try {
				int channelID = obj.getInt(Channel.CHANNEL_ID);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("channeltitle", obj.getString(Channel.TITLE));
				bundle.putString("path", getString(R.string.video_list));
				if (channelID == Channels.movie.channelID()) {
					intent.setClass(ChannelActivity.this,
							MovieListActivity.class);
				} else if (channelID == Channels.series.channelID()) {
					intent.setClass(ChannelActivity.this,
							SeriesListActivity.class);
				} else {
					intent.setClass(ChannelActivity.this,
							ChanneVideoListActivity.class);
					bundle.putInt(Channel.CHANNEL_ID, channelID);
				}
				intent.putExtras(bundle);
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	public void onSearch(View v) {
		Intent intent = new Intent(this, VideoSearchActivity.class);
		startActivity(intent);
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