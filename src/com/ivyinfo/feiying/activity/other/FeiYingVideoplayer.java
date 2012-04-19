package com.ivyinfo.feiying.activity.other;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;

public class FeiYingVideoplayer extends Activity {
	private static final int MSG_PLAY_VIDEO = 100;
	private static final int MSG_CANNOT_PLAY = 101;
	private MessageHandler messageHandler;

	private static final String TAG = "FeiYingVideoplayer";

	private static final String VOLUME_UP = "volume_up";
	private static final String VOLUME_DOWN = "volume_down";

	private static final int MAP_SIZE = 10;

	// media controller hide automatic time(seconds)
	private static final int MAX_TIME = 3;

	// private video uri hash position map
	protected static Map<String, Integer> mVideoUri2Position = new HashMap<String, Integer>(
			MAP_SIZE);

	// video position when video pause or seek to
	private static int mPositionWhenPS = 0;
	// buffer position
	private float mPositionBuffering;

	// media controller show flag
	private static boolean mMediaControllerShow = true;

	// time count
	private static int mTimerCount = 0;

	// main activity message handle
	private Handler handler;

	// system audio manager
	private AudioManager audioManager;

	// scheduled executor service
	private ScheduledExecutorService scheduledExecutorService;
	// scheduled future
	private ScheduledFuture<?> scheduledFuture;

	// header
	private Button mBackButton;
	private TextView mPositionTime;
	private SeekBar mVideoSeekBar;
	private TextView mTotalTime;

	// bottom
	private Button mRewind;
	private Button mPlayPause;
	private Button mFastForward;
	private SeekBar mVolumeSeekBar;

	// videoView
	private VideoView mVideoView;
	// video uri
	private Uri mVideoUri;

	// video media controller relativeLayout
	private RelativeLayout mMediaControllerRL;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set content view
		setContentView(R.layout.video);
		messageHandler = new MessageHandler(Looper.myLooper());
		// set handle
		handler = new Handler();

		// initialize scheduledExecutorService pool
		scheduledExecutorService = Executors.newScheduledThreadPool(1);

		// set system audio manager
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// get intent param and set video uri
		Bundle bundle = getIntent().getExtras();
		String videoUrl = bundle.getString("videoUrl");
		mVideoUri = Uri.parse(videoUrl);

//		HttpUtils.startHttpHead(videoUrl, new HttpUtils.HeadResponseListener() {
//
//			@Override
//			public void onComplete(int status, HttpEntity entity) {
//				Log.d(TAG, "status: " + status);
//				if (entity != null) {
//					Log.d(TAG, "entity is not null, streaming: " + entity.isStreaming());
//					messageHandler.sendEmptyMessage(MSG_PLAY_VIDEO);
//				} else {
//					Log.d(TAG, "entity is null");
//					messageHandler.sendEmptyMessage(MSG_CANNOT_PLAY);
//				}
//			}
//		}, null);

		Log.d(TAG, "video uri = " + mVideoUri);
		if (mVideoUri.toString().equals("")) {
			mVideoUri = Uri.parse("http://192.168.1.233/video/youku_small.mp4");
			Log.d(TAG, "null uri for test, default video uri = " + mVideoUri);
		}

		// get video position from map
		Log.d(TAG, "hash map = " + mVideoUri2Position);
		Integer _tVideoPosition = mVideoUri2Position.get(mVideoUri.toString());
		if (_tVideoPosition == null) {
			Log.d(TAG, "_tVideoPosition is null.");
			mPositionWhenPS = 0;
		} else {
			mPositionWhenPS = _tVideoPosition;
			Log.i(TAG, "_tVideoPosition = " + _tVideoPosition);
		}
		Log.d(TAG, "videoUri = " + mVideoUri + " position = " + mPositionWhenPS);

		// get view component and set attributes
		// set mediaController relativeLayout
		mMediaControllerRL = (RelativeLayout) findViewById(R.id.rlController);

		// set videoView
		mVideoView = (VideoView) findViewById(R.id.videoView);

		// set video focus
		mVideoView.requestFocus();
		// set videoView touch event
		mVideoView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.d(TAG, "video touch down.");
					Log.i(TAG, "screen touch down...");
					if (mMediaControllerShow) {
						mMediaControllerRL.setVisibility(View.INVISIBLE);
						mMediaControllerShow = false;
					} else {
						mMediaControllerRL.setVisibility(View.VISIBLE);
						mMediaControllerShow = true;
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					Log.d(TAG, "video touch up.");
					Log.i(TAG, "" + Log.INFO);
				}

				return false;
			}
		});
		// set videoView error listener
		mVideoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e(TAG, "video play error.");
				cannotPlay();
				return false;
			}
		});
		// set videoView completion listener
		mVideoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.i(TAG, "video play completed.");

				// update video seekBar position
				mPositionWhenPS = 0;

				// set play/pause background
				mPlayPause.setBackgroundResource(R.drawable.p_play);
				// show media controller
				mMediaControllerRL.setVisibility(View.VISIBLE);
				mMediaControllerShow = true;
			}
		});
		// set videoView prepared listener
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d(TAG, "video prepared.");

				// get frontScreen and set gone first
				final RelativeLayout _frontScreenRL = (RelativeLayout) findViewById(R.id.rlFrontScreen);
				_frontScreenRL.setVisibility(View.GONE);

				// start play video
				mVideoView.start();
				Log.i(TAG, "Video start to play.");

				// set video total time
				mTotalTime.setText(getTimeFormatValue(mVideoView.getDuration()));

				// set video volume
				mVolumeSeekBar.setProgress((int) ((float) audioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC)
						/ audioManager
								.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * mVolumeSeekBar
						.getMax()));

				scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(
						new Runnable() {

							@Override
							public void run() {
								handler.post(new Runnable() {
									@Override
									public void run() {
										//
										mTimerCount++;
										Log.d(TAG, "time count = "
												+ mTimerCount);
										if (!mMediaControllerShow) {
											Log.d(TAG,
													"the media controller is hide currently.");
											// reset mTimerCount
											mTimerCount = 0;
										} else if (mMediaControllerShow
												&& mTimerCount == 2 * MAX_TIME + 1) {
											// hide media controller
											Log.i(TAG,
													"media controller hide automatic.");
											mMediaControllerRL
													.setVisibility(View.INVISIBLE);
											mMediaControllerShow = false;

											// reset mTimerCount
											mTimerCount = 0;
										}

										// set video seekBar secondary progress
										mVideoSeekBar
												.setSecondaryProgress(mVideoView
														.getBufferPercentage());

										if (mVideoView.isPlaying()) {
											float position = mVideoView
													.getCurrentPosition();

											Log.d(TAG,
													"video is playing, position = "
															+ position);

											// judge if videoView is buffering
											if (mPositionBuffering == position
													&& position != 0) {
												Log.d(TAG, "video buffering...");

												// update progress message
												TextView _progressMsg = (TextView) findViewById(R.id.progressMSG);
												_progressMsg
														.setText(R.string.sVideoBuffering);
												_progressMsg.setTextColor(Color
														.argb(255, 255, 255,
																255));
												// set bgColor clear and show it
												_frontScreenRL
														.setBackgroundColor(Color
																.argb(100, 0,
																		0, 0));
												_frontScreenRL
														.setVisibility(View.VISIBLE);
											} else {
												Log.d(TAG,
														"progressBar dismiss.");

												// hide frontScreen
												_frontScreenRL
														.setVisibility(View.GONE);
												mPositionBuffering = position;
											}

											// set video current position time
											mPositionTime
													.setText(getTimeFormatValue((int) position));

											// set video seekBar progress
											mVideoSeekBar.setProgress((int) (position
													/ mVideoView.getDuration() * mVideoSeekBar
													.getMax()));
										} else {
											Log.d(TAG, "video is not playing.");
										}
									}
								});

							}
						}, 0, 500, TimeUnit.MILLISECONDS);
			}
		});

		// header section
		// set back done button
		mBackButton = (Button) findViewById(R.id.doneButton);
		mBackButton.setOnClickListener(buttonClickedListener);

		// set position time textView
		mPositionTime = (TextView) findViewById(R.id.positionTime);
		// set total time textView
		mTotalTime = (TextView) findViewById(R.id.totalTime);

		// set video seekBar
		mVideoSeekBar = (SeekBar) findViewById(R.id.videoSeekBar);
		mVideoSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

		// bottom section
		// set rewind button
		mRewind = (Button) findViewById(R.id.rewindButton);
		mRewind.setOnClickListener(buttonClickedListener);

		// set play and pause button
		mPlayPause = (Button) findViewById(R.id.playPauseButton);
		mPlayPause.setOnClickListener(buttonClickedListener);

		// set fastForward button
		mFastForward = (Button) findViewById(R.id.fastForwardButton);
		mFastForward.setOnClickListener(buttonClickedListener);

		// set volume seekBar
		mVolumeSeekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
		mVolumeSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
		
		messageHandler.sendEmptyMessage(MSG_PLAY_VIDEO);
	}

	private void cannotPlay() {
		Toast toast = Toast.makeText(FeiYingVideoplayer.this,
				R.string.play_video_error, Toast.LENGTH_LONG);
		toast.show();

		finish();
	}
	
	// set button clicked listener
	private OnClickListener buttonClickedListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.doneButton:
				Log.d(TAG, "back button clicked.");
				// stop scheduled
				stopScheduled(scheduledFuture, scheduledExecutorService);
				// activity finish
				finish();

				break;

			case R.id.refreshButton:
				break;

			case R.id.rewindButton:
				Log.d(TAG, "rewind button clicked.");
				// reset mTimerCount
				mTimerCount = 0;
				// video rewind 5 seconds
				mVideoView.seekTo(mVideoView.getCurrentPosition() - 5000);

				break;

			case R.id.playPauseButton:
				Log.d(TAG, "play and pause button clicked.");
				// reset mTimerCount
				mTimerCount = 0;
				if (mVideoView.isPlaying()) {
					Log.d(TAG, "pause pressed.");

					// set play/pause background
					mPlayPause.setBackgroundResource(R.drawable.p_play);

					mVideoView.pause();
					mPositionWhenPS = mVideoView.getCurrentPosition();
				} else {
					Log.d(TAG, "play pressed.");

					// set play/pause background
					mPlayPause.setBackgroundResource(R.drawable.p_pause);

					mVideoView.seekTo(mPositionWhenPS);
					mVideoView.start();
				}

				break;

			case R.id.fastForwardButton:
				Log.d(TAG, "fastForward button clicked.");
				// reset mTimerCount
				mTimerCount = 0;
				// fastForward 5 seconds
				mVideoView.seekTo(mVideoView.getCurrentPosition() + 5000);

				break;

			default:
				Log.e(TAG, "error, * button clicked.");
				break;
			}
		}
	};

	// set seekBar change listener
	private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			switch (seekBar.getId()) {
			case R.id.videoSeekBar:
				Log.d(TAG, "videoSeekBar onStopTrackingTouch.");
				// reset mTimerCount
				mTimerCount = 0;

				// to end
				if (mPositionWhenPS == 100) {
					mPositionTime.setText(getTimeFormatValue(mVideoView
							.getDuration()));
				}
				mVideoView.seekTo(mVideoView.getDuration() * mPositionWhenPS
						/ mVideoSeekBar.getMax());
				break;

			default:
				Log.e(TAG, "error, * seekBar changed stop.");
				break;
			}

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.d(TAG, "seekBar onStartTrackingTouch.");
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			switch (seekBar.getId()) {
			case R.id.volumeSeekBar:
				Log.d(TAG, "volumeSeekBar onProgressChanged.");
				// reset mTimerCount
				mTimerCount = 0;

				audioManager
						.setStreamVolume(
								AudioManager.STREAM_MUSIC,
								(int) ((float) seekBar.getProgress()
										/ mVolumeSeekBar.getMax() * audioManager
										.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),
								0);
				Log.i(TAG,
						"volume seekBar current position: "
								+ seekBar.getProgress());

				break;

			case R.id.videoSeekBar:
				Log.d(TAG, "videoSeekBar onProgressChanged.");
				mPositionWhenPS = seekBar.getProgress();
				Log.i(TAG, "video seekBar current position: " + mPositionWhenPS);

				break;

			default:
				Log.e(TAG, "error, * seekBar changed.");
				break;
			}
		}
	};

	// get time format
	private String getTimeFormatValue(long time) {
		return MessageFormat.format("{0,number,00}:{1,number,00}",
				time / 1000 / 60, time / 1000 % 60);
	}

	// stop scheduled service
	private void stopScheduled(ScheduledFuture<?> _future,
			ScheduledExecutorService _executorService) {
		if (_future != null) {
			// cancel scheduled future
			_future.cancel(true);

			// shut down scheduledExecutorService
			_executorService.shutdown();
		}
	}

	// update volume according to direction
	private void updateVolume(String volumeChangeDirection) {
		float currentVolume = (float) audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		int volumeMax = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int volumeSeekBarMax = mVolumeSeekBar.getMax();

		if (VOLUME_UP.equals(volumeChangeDirection)) {
			mVolumeSeekBar
					.setProgress((int) ((currentVolume + 1) / volumeMax * volumeSeekBarMax));
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					(int) currentVolume + 1, 0);
		} else if (VOLUME_DOWN.equals(volumeChangeDirection)) {
			mVolumeSeekBar
					.setProgress((int) ((currentVolume - 1) / volumeMax * volumeSeekBarMax));
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					(int) currentVolume - 1, 0);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// stop scheduled
			stopScheduled(scheduledFuture, scheduledExecutorService);
			// activity finish
			finish();

			return true;

		case KeyEvent.KEYCODE_VOLUME_UP:
			Log.d(TAG, "system volume change up.");
			updateVolume(VOLUME_UP);

			return true;

		case KeyEvent.KEYCODE_VOLUME_DOWN:
			Log.d(TAG, "system volume change down.");
			updateVolume(VOLUME_DOWN);

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mVideoView.seekTo(mPositionWhenPS);
		mVideoView.start();
	}

	@Override
	protected void onPause() {
		super.onPause();

		mVideoView.pause();
		mPositionWhenPS = (mVideoSeekBar.getProgress() >= 99) ? mPositionWhenPS
				: mVideoView.getCurrentPosition();

		// mVideoUri2Position map add
		Log.d(TAG, "map add, uri = " + mVideoUri + " and position = "
				+ mPositionWhenPS);
		mVideoUri2Position.put(mVideoUri.toString(), new Integer(
				mPositionWhenPS));
	}

	class MessageHandler extends Handler {
		public MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MSG_PLAY_VIDEO:
				mVideoView.setVideoURI(mVideoUri);
				break;
			case MSG_CANNOT_PLAY:
				cannotPlay();
				break;
			default:
				break;
			}
		}
	}
}
