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
import com.ivyinfo.feiying.constant.BusinessStatus;
import com.ivyinfo.feiying.constant.Channels;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;
import com.ivyinfo.feiying.utity.AsyncImageLoader;
import com.ivyinfo.feiying.utity.ImageCallback;
import com.ivyinfo.user.UserManager;

public class VideoDetailActivity extends BaseVideoDetailActivity {
	protected Handler messageHandler;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_detail_view);
		messageHandler = new MessageHandler(Looper.myLooper());

		initView();

		String getVideoUrl = host + getString(R.string.video_info_url) + "/"
				+ channel + "/" + sourceId;

		progressDialog = ProgressDialog.show(this, null,
				getString(R.string.getting_data), true);
		HttpUtils.startHttpPostRequest(getVideoUrl, null, videoResLis, null);

		fillShareFromToField();

	}

	private ResponseListener videoResLis = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Log.d("feiying", "videoResLis - onComplete - status: " + status
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

	public void onPlayVideoClick(View view) {
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
		try {
			String videoUrl = videoInfoJSONObj
					.getString(VideoConstants.video_url.name());
			play(videoUrl);
		} catch (JSONException e) {
			e.printStackTrace();
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
					// business opened
					// authenticated, just play with
					// internal video url
					String videoUrl = getString(R.string.host_2) + "/"
							+ sourceId + ".mp4";
					play(videoUrl);
				} else if (status.equals(BusinessStatus.processing.name())) {
					// business processing
					new AlertDialog.Builder(VideoDetailActivity.this)
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
					new AlertDialog.Builder(VideoDetailActivity.this)
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
			case MsgCodeDefine.MSG_ON_GET_DATA_RETURN: {
				JSONObject jsonObject = (JSONObject) message.obj;
				try {
					videoInfoJSONObj = jsonObject;

					channel = videoInfoJSONObj.getInt(VideoConstants.channel
							.name());
					String title = videoInfoJSONObj
							.getString(VideoConstants.title.name());
					String time = videoInfoJSONObj
							.getString(VideoConstants.time.name());
					String size = videoInfoJSONObj
							.getString(VideoConstants.size.name());
					String playcount = videoInfoJSONObj
							.getString(VideoConstants.play_count.name());
					String sharecount = videoInfoJSONObj
							.getString(VideoConstants.share_count.name());
					String favcount = videoInfoJSONObj
							.getString(VideoConstants.fav_count.name());
					int channelID = videoInfoJSONObj
							.getInt(VideoConstants.channel.name());

					TextView titleTV = (TextView) findViewById(R.id.detail_video_title);
					TextView timeTV = (TextView) findViewById(R.id.detail_video_time);
					TextView sizeTV = (TextView) findViewById(R.id.detail_video_size);
					TextView playcountTV = (TextView) findViewById(R.id.detail_video_playcount);
					TextView sharecountTV = (TextView) findViewById(R.id.detail_video_sharecount);
					TextView favcountTV = (TextView) findViewById(R.id.detail_video_favcount);
					TextView channelTV = (TextView) findViewById(R.id.detail_video_channel);

					titleTV.setText(title);
					timeTV.setText(time);
					sizeTV.setText(size);
					playcountTV.setText(playcount);
					sharecountTV.setText(sharecount);
					favcountTV.setText(favcount);
					channelTV.setText(Channels.getResIDByChannelID(channelID));

					ImageButton imgBt = (ImageButton) findViewById(R.id.video_thumb_img);

					String imgUrl = "";
					if (UserManager.getInstance().getUser().getUserkey()
							.equals("")) {
						imgUrl = videoInfoJSONObj
								.getString(VideoConstants.image_url.name());
					} else {
						imgUrl = getString(R.string.host_2) + "/" + sourceId
								+ ".jpg";
					}

					if (imgUrl != "") {
						Bitmap img = AsyncImageLoader.getInstance().loadImage(
								imgUrl, new ImageCallback(imgBt));

						if (img != null) {
							imgBt.setImageBitmap(img);
						}
					}

				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
				break;

			default:
				Toast.makeText(VideoDetailActivity.this,
						R.string.get_data_failed, Toast.LENGTH_SHORT).show();
				onBack(null);
				break;
			}
		}
	}

}
