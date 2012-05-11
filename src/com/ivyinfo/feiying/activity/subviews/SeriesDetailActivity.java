package com.ivyinfo.feiying.activity.subviews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ivyinfo.feiying.activity.other.FeiYingVideoplayer;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.BusinessStatus;
import com.ivyinfo.feiying.constant.Channels;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;
import com.ivyinfo.feiying.utity.AsyncImageLoader;
import com.ivyinfo.feiying.utity.ImageCallback;
import com.ivyinfo.feiying.utity.VideoCommonOpUtil;
import com.ivyinfo.feiying.view.EpisodePlayListView;
import com.ivyinfo.feiying.view.PlayListItemListener;
import com.ivyinfo.user.UserManager;

public class SeriesDetailActivity extends BaseVideoDetailActivity {

	private JSONArray episodeList;

	protected Handler messageHandler;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.series_detail_view);

		messageHandler = new MessageHandler(Looper.myLooper());

		channel = Channels.series.channelID();
		initView();

		String getSeriesUrl = host + getString(R.string.video_info_url) + "/"
				+ channel + "/" + sourceId;

		progressDialog = ProgressDialog.show(this, null,
				getString(R.string.getting_data), true);

		HttpUtils.startHttpPostRequest(getSeriesUrl, null, seriesResLis, null);

		fillShareFromToField();
	}

	private ResponseListener seriesResLis = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Log.d("feiying", "seriesResLis - onComplete - status: " + status
					+ " response: " + responseText);
			Message message = Message.obtain();
			switch (status) {
			case 200:
				try {
					JSONObject jsonObject = new JSONObject(responseText);

					message.obj = jsonObject;
					message.what = MsgCodeDefine.MSG_ON_GET_DATA_RETURN;

				} catch (JSONException e) {
					e.printStackTrace();
					message.what = MsgCodeDefine.MSG_ERROR;
				}
				break;
			default:
				message.what = MsgCodeDefine.MSG_ERROR;
				break;
			}
			messageHandler.sendMessage(message);
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
				try {
					JSONObject obj = (JSONObject) message.obj;
					videoInfoJSONObj = obj;
					episodeList = videoInfoJSONObj.getJSONArray("list");
					String title = videoInfoJSONObj
							.getString(VideoConstants.title.name());
					int seriesCount = videoInfoJSONObj
							.getInt(VideoConstants.episode_count.name());
					String actor = videoInfoJSONObj
							.getString(VideoConstants.actor.name());
					String playcount = videoInfoJSONObj
							.getString(VideoConstants.play_count.name());
					String sharecount = videoInfoJSONObj
							.getString(VideoConstants.share_count.name());
					String favcount = videoInfoJSONObj
							.getString(VideoConstants.fav_count.name());
					String description = videoInfoJSONObj
							.getString(VideoConstants.description.name());
					String director = videoInfoJSONObj
							.getString(VideoConstants.director.name());
					String region = videoInfoJSONObj
							.getString(VideoConstants.origin.name());
					String releaseDate = videoInfoJSONObj
							.getString(VideoConstants.release_date.name());

					generateEpisodes(episodeList);
					TextView titleTV = (TextView) findViewById(R.id.detail_series_title);
					TextView seriesCountTV = (TextView) findViewById(R.id.detail_series_episode_count);
					TextView actorTV = (TextView) findViewById(R.id.detail_series_actor);
					TextView playcountTV = (TextView) findViewById(R.id.detail_series_playcount);
					TextView sharecountTV = (TextView) findViewById(R.id.detail_series_sharecount);
					TextView favcountTV = (TextView) findViewById(R.id.detail_series_favcount);
					TextView descriptionTV = (TextView) findViewById(R.id.detail_series_description);

					TextView directorTV = (TextView) findViewById(R.id.detail_series_director);
					TextView regionTV = (TextView) findViewById(R.id.detail_series_region);
					TextView releaseDateTV = (TextView) findViewById(R.id.detail_series_release_date);

					titleTV.setText(title);
					seriesCountTV.setText("" + seriesCount);
					actorTV.setText(actor);
					playcountTV.setText(playcount);
					sharecountTV.setText(sharecount);
					favcountTV.setText(favcount);
					descriptionTV.setText(description);
					directorTV.setText(director);
					regionTV.setText(region);
					releaseDateTV.setText(releaseDate);

					ImageButton imgBt = (ImageButton) findViewById(R.id.series_thumb_img);

					String imgURL = "";
					if (UserManager.getInstance().getUser().getUserkey()
							.equals("")) {
						imgURL = videoInfoJSONObj
								.getString(VideoConstants.image_url.name());
					} else {
						imgURL = getString(R.string.host_2) + "/" + sourceId
								+ ".jpg";
					}

					if (imgURL != "") {
						Bitmap img = AsyncImageLoader.getInstance().loadImage(
								imgURL, new ImageCallback(imgBt));

						if (img != null) {
							imgBt.setImageBitmap(img);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			default:
				Toast.makeText(SeriesDetailActivity.this,
						R.string.get_data_failed, Toast.LENGTH_SHORT).show();
				onBack(null);
				break;
			}
		}
	}

	private void generateEpisodes(JSONArray episodeArr) {

		EpisodePlayListView playListView = (EpisodePlayListView) findViewById(R.id.series_episode_playlist);
		playListView.setPlayListItemListener(new PlayListItemListener() {

			@Override
			public void onItemClicked(JSONObject obj) {
				if (obj != null) {
					playEpisode(obj);
				}
			}
		});
		playListView.setEpisodeList(episodeArr);
	}

	/**
	 * play tv episode by selected index
	 * 
	 * @param selectedIndex
	 * @deprecated
	 * 
	 */
	private void playEpisode(int selectedIndex) {
		try {
			VideoCommonOpUtil
					.recordPlayCount(videoInfoJSONObj
							.getString(VideoConstants.source_id.name()),
							getString(R.string.host)
									+ getString(R.string.record_play_count_url));

			JSONObject episode = episodeList.getJSONObject(selectedIndex);
			String episodePlayURL = episode.getString(VideoConstants.video_url
					.name());
			Intent intent = new Intent();
			intent.setClass(this, FeiYingVideoplayer.class);
			Bundle bundle = new Bundle();
			bundle.putString("videoUrl", episodePlayURL);
			intent.putExtras(bundle);
			startActivity(intent);

		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.error_video_url, Toast.LENGTH_SHORT)
					.show();
		}

	}

	private JSONObject clickedEpisode;

	private void playEpisode(JSONObject episode) {
		clickedEpisode = episode;
		if (UserManager.getInstance().getUser().getUserkey().equals("")) {
			// for non-login user, alert to login, otherwise it will consume
			// his/her data stream
			new AlertDialog.Builder(this)
					.setTitle(R.string.alert_title)
					.setMessage(R.string.non_login_user_play_video_alert_info)
					.setPositiveButton(R.string.account_setting,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									login();
								}
							})
					.setNegativeButton(R.string.still_play,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									playOuterVideo();
								}
							}).show();
		} else {
			// first do authentication
			setOnAuthReturnListener(playAuthLis);
			doAuth();
		}

	}

	private void playOuterVideo() {
		if (clickedEpisode != null) {
			try {
				String videoUrl = clickedEpisode
						.getString(VideoConstants.video_url.name());
				play(videoUrl);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private OnAuthReturnListener playAuthLis = new OnAuthReturnListener() {

		@Override
		public void onAuthReturned(boolean isAuthed, JSONObject jobj) {
			if (isAuthed) {
				String status = "opened";
				if (jobj != null) {
					try {
						status = jobj.getString("status");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				if (status.equals(BusinessStatus.opened.name())) {
					// authenticated, just play with
					// internal video url
					try {
						if (clickedEpisode != null) {
							int index = clickedEpisode.getInt("episode_index");
							String videoUrl = getString(R.string.host_2) + "/"
									+ sourceId + "_" + index + ".mp4";
							play(videoUrl);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else if (status.equals(BusinessStatus.processing.name())) {
					// business processing
					new AlertDialog.Builder(SeriesDetailActivity.this)
							.setTitle(R.string.alert_title)
							.setMessage(
									R.string.processing_user_play_video_alert_info)

							.setPositiveButton(R.string.still_play,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											playOuterVideo();
										}
									}).setNegativeButton(R.string.cancel, null)
							.show();
				} else {
					// business unopened
					new AlertDialog.Builder(SeriesDetailActivity.this)
							.setTitle(R.string.alert_title)
							.setMessage(
									R.string.unopen_user_play_video_alert_info)
							.setPositiveButton(R.string.account_setting,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											login();
										}
									})
							.setNegativeButton(R.string.still_play,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											playOuterVideo();
										}
									}).show();
				}
			}
		}

	};

}
