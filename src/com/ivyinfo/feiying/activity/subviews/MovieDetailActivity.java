package com.ivyinfo.feiying.activity.subviews;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.Channels;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;
import com.ivyinfo.feiying.utity.AsyncImageLoader;
import com.ivyinfo.feiying.utity.ImageCallback;
import com.ivyinfo.user.UserManager;

public class MovieDetailActivity extends BaseVideoDetailActivity {

	protected Handler messageHandler;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.movie_detail_view);

		messageHandler = new MessageHandler(Looper.myLooper());

		channel = Channels.movie.value();
		initView();

		String getMovieUrl = host + getString(R.string.video_info_url) + "/"
				+ channel + "/" + sourceId;

		progressDialog = ProgressDialog.show(this, null,
				getString(R.string.getting_data), true);

		HttpUtils.startHttpPostRequest(getMovieUrl, null, movieResLis, null);

		fillShareFromToField();
		
	}

	private ResponseListener movieResLis = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Log.d("feiying", "movieResLis - onComplete - status: " + status
					+ " response: " + responseText);
			Message message = Message.obtain();
			switch (status) {
			case 200:
				try {
					JSONObject jsonObj = new JSONObject(responseText);

					message.obj = jsonObj;
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
					JSONObject jsonObject = (JSONObject) message.obj;
					videoInfoJSONObj = jsonObject;
					String title = videoInfoJSONObj
							.getString(VideoConstants.title.name());
					String time = videoInfoJSONObj
							.getString(VideoConstants.time.name());
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

					TextView titleTV = (TextView) findViewById(R.id.detail_movie_title);
					TextView timeTV = (TextView) findViewById(R.id.detail_movie_time);
					TextView actorTV = (TextView) findViewById(R.id.detail_movie_actor);
					TextView playcountTV = (TextView) findViewById(R.id.detail_movie_playcount);
					TextView sharecountTV = (TextView) findViewById(R.id.detail_movie_sharecount);
					TextView favcountTV = (TextView) findViewById(R.id.detail_movie_favcount);
					TextView descriptionTV = (TextView) findViewById(R.id.detail_movie_description);

					TextView directorTV = (TextView) findViewById(R.id.detail_movie_director);
					TextView regionTV = (TextView) findViewById(R.id.detail_movie_region);
					TextView releaseDateTV = (TextView) findViewById(R.id.detail_movie_release_date);

					titleTV.setText(title);
					timeTV.setText(time);
					actorTV.setText(actor);
					playcountTV.setText(playcount);
					sharecountTV.setText(sharecount);
					favcountTV.setText(favcount);
					descriptionTV.setText(description);
					directorTV.setText(director);
					regionTV.setText(region);
					releaseDateTV.setText(releaseDate);

					ImageButton imgBt = (ImageButton) findViewById(R.id.movie_thumb_img);
					String imgURL = videoInfoJSONObj
							.getString(VideoConstants.image_url.name());
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
				Toast.makeText(MovieDetailActivity.this,
						R.string.get_data_failed, Toast.LENGTH_SHORT).show();
				onBack(null);
				break;
			}
		}
	}

	public void onPlayMovieClick(View view) {
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
									try {
										String videoUrl = videoInfoJSONObj
												.getString(VideoConstants.video_url
														.name());
										play(videoUrl);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}).show();
		} else {
			// first do authentication
			setOnAuthReturnListener(playAuthLis);
			doAuth();
		}

	}

	private OnAuthReturnListener playAuthLis = new OnAuthReturnListener() {
		
		@Override
		public void onAuthReturned(boolean isAuthed) {
			if (isAuthed) {
				// authenticated, just play with
				// internal video url
				try {
					String videoUrl = videoInfoJSONObj
							.getString(VideoConstants.video_url.name());
					play(videoUrl);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};

}
