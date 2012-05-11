package com.ivyinfo.feiying.activity.subviews;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ivyinfo.contact.ContactManager;
import com.ivyinfo.contact.ContactManagerFactory;
import com.ivyinfo.contact.beans.Contact;
import com.ivyinfo.feiying.activity.other.FeiYingVideoplayer;
import com.ivyinfo.feiying.activity.system.RegisterAndLoginActivity;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.AccountState;
import com.ivyinfo.feiying.constant.ActivityRequests;
import com.ivyinfo.feiying.constant.Channels;
import com.ivyinfo.feiying.constant.CommonConstants;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.constant.VideoTag;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;
import com.ivyinfo.feiying.utity.VideoCommonOpUtil;
import com.ivyinfo.user.UserManager;

/**
 * base video detail activity for all kinds of video detail
 * 
 * @author sk
 * 
 */
public abstract class BaseVideoDetailActivity extends Activity {
	protected String videoTag;
	protected String host;
	protected String authUrl;
	/**
	 * user for shared video
	 */

	protected String sourceId;
	protected String sender;
	protected JSONObject videoInfoJSONObj;
	private Handler bvdMsgHandler;

	protected ProgressDialog progressDialog;

	protected int videoShareID;
	protected String shareDate;
	protected int channel;

	private OnAuthReturnListener currentAuthListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bvdMsgHandler = new BVDMessageHandler(Looper.myLooper());

		Intent intent = getIntent();
		sourceId = intent.getStringExtra(VideoConstants.source_id.name());
		videoShareID = intent.getIntExtra(VideoConstants.share_id.name(), -1);
		videoTag = intent.getStringExtra(VideoConstants.video_tag.name());
		sender = intent.getStringExtra(VideoConstants.send.name());
		shareDate = intent.getStringExtra(VideoConstants.date.name());

		channel = Channels.video.channelID();
		host = getString(R.string.host);
		authUrl = host + getString(R.string.authenticate_url);
	}

	protected void fillShareFromToField() {
		if (videoTag != null) {
			if (videoTag.equals(VideoTag.share_go.name())) {
				String shareReceiversURL = host
						+ getString(R.string.share_video_receivers_url);
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("shareId", Integer.toString(videoShareID));
				HttpUtils.startHttpPostRequestWithSignature(shareReceiversURL,
						params, shareReceiversRL, null);
			} else if (videoTag.equals(VideoTag.share_come.name())) {
				TextView nameTV = (TextView) findViewById(R.id.detail_from_to_names);
				nameTV.setText(sender);
			}
		}
	}

	private ResponseListener shareReceiversRL = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Log.d("feiying", "shareReceiversRL - onComplete - status: "
					+ status + " response: " + responseText);
			Message message = Message.obtain();
			switch (status) {
			case 200:
				try {
					JSONObject jsonObj = new JSONObject(responseText);

					message.obj = jsonObj;
					message.what = MsgCodeDefine.MSG_ON_GET_SHARE_RECEIVERS_RETURN;

				} catch (JSONException e) {
					e.printStackTrace();
					message.what = MsgCodeDefine.MSG_ON_GET_SHARE_RECEIVERS_FAILED;
				}
				break;
			default:
				message.what = MsgCodeDefine.MSG_ON_GET_SHARE_RECEIVERS_FAILED;
				break;
			}
			bvdMsgHandler.sendMessage(message);
		}
	};

	protected void initView() {
		if (videoTag != null) {
			View favBt = findViewById(R.id.fav_bt);
			View unfavBt = findViewById(R.id.unfav_bt);
			if (videoTag.equals(VideoTag.favor.name())) {
				favBt.setVisibility(View.GONE);
				unfavBt.setVisibility(View.VISIBLE);
			} else if (videoTag.equals(VideoTag.share_come.name())
					|| videoTag.equals(VideoTag.share_go.name())) {
				View delBt = findViewById(R.id.del_bt);
				delBt.setVisibility(View.VISIBLE);
				View fromToLayout = findViewById(R.id.detail_from_to_layout);
				fromToLayout.setVisibility(View.VISIBLE);
				TextView fromToFieldTV = (TextView) findViewById(R.id.detail_from_to_field);
				TextView dateTV = (TextView) findViewById(R.id.detail_share_date_tv);
				dateTV.setText(shareDate);
				if (videoTag.equals(VideoTag.share_come.name())) {
					fromToFieldTV.setText(R.string.from);
				} else {
					fromToFieldTV.setText(R.string.to);
				}

			} else {
				favBt.setVisibility(View.VISIBLE);
				unfavBt.setVisibility(View.GONE);
			}
		}
	}

	public void onBack(View view) {
		finish();
	}

	/**
	 * alert to register and login
	 */
	private void alertAccountRegAndLoginDlg() {
		new AlertDialog.Builder(this).setTitle(R.string.alert_title)
				.setMessage(R.string.pls_register_account)
				.setPositiveButton(R.string.login, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						login();
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}

	protected void login() {
		Intent intent = new Intent();
		intent.setClass(this, RegisterAndLoginActivity.class);
		intent.putExtra(CommonConstants.account_state.name(),
				AccountState.account_set.name());
		startActivityForResult(intent, ActivityRequests.REQ_LOGIN);
	}

	public void onFavor(View v) {
		if (UserManager.getInstance().getUser().getUserkey().equals("")) {
			alertAccountRegAndLoginDlg();
			return;
		}

		String url = host + getString(R.string.favor_op_url);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("sourceId", sourceId);
		params.put("action", "add");
		HttpUtils.startHttpPostRequestWithSignature(url, params, favorRL, null);
	}

	private ResponseListener favorRL = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Message message = Message.obtain();
			switch (status) {
			case 200:
				try {
					JSONObject jsonObject = new JSONObject(responseText);

					message.obj = jsonObject;
					message.what = MsgCodeDefine.MSG_ON_FAVOR_RETURN;

				} catch (JSONException e) {
					e.printStackTrace();
					message.what = MsgCodeDefine.MSG_ERROR;
				}
				break;
			case 400:
				// alert user to re-login
				message.what = MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN;
				break;
			default:
				message.what = MsgCodeDefine.MSG_ERROR;
				break;
			}
			bvdMsgHandler.sendMessage(message);
		}
	};

	public void onUnFavor(View v) {
		if (UserManager.getInstance().getUser().getUserkey().equals("")) {
			alertAccountRegAndLoginDlg();
			return;
		}

		new AlertDialog.Builder(this)
				.setTitle(R.string.alert_title)
				.setMessage(R.string.del_fav_request_info)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String url = host
										+ getString(R.string.favor_op_url);
								HashMap<String, String> params = new HashMap<String, String>();
								params.put("sourceId", sourceId);
								params.put("action", "del");
								HttpUtils.startHttpPostRequestWithSignature(
										url, params, unfavorRL, null);
							}
						}).setNegativeButton(R.string.sBackButton, null).show();
	}

	private ResponseListener unfavorRL = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Message message = Message.obtain();
			switch (status) {
			case 200:
				try {
					JSONObject jsonObject = new JSONObject(responseText);

					message.obj = jsonObject;
					message.what = MsgCodeDefine.MSG_ON_UNFAVOR_RETURN;

				} catch (JSONException e) {
					e.printStackTrace();
					message.what = MsgCodeDefine.MSG_ERROR;
				}
				break;
			case 400:
				// alert user to re-login
				message.what = MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN;
				break;
			default:
				message.what = MsgCodeDefine.MSG_ERROR;
				break;
			}
			bvdMsgHandler.sendMessage(message);
		}
	};

	public void onShare(View v) {
		if (UserManager.getInstance().getUser().getUserkey().equals("")) {
			alertAccountRegAndLoginDlg();
			return;
		}

		setOnAuthReturnListener(shareOnAuthLis);
		doAuth();

	}

	private OnAuthReturnListener shareOnAuthLis = new OnAuthReturnListener() {

		@Override
		public void onAuthReturned(boolean isAuthed, JSONObject jobj) {
			if (isAuthed) {
				Intent intent = new Intent(BaseVideoDetailActivity.this,
						VideoShareEditorActivity.class);
				try {
					String title = videoInfoJSONObj
							.getString(VideoConstants.title.name());
					intent.putExtra(VideoConstants.title.name(), title);
					intent.putExtra(VideoConstants.source_id.name(), sourceId);
					intent.putExtra(VideoConstants.channel.name(), channel);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				startActivity(intent);
			}
		}

	};

	public void onDelShare(View v) {
		if (UserManager.getInstance().getUser().getUserkey().equals("")) {
			alertAccountRegAndLoginDlg();
			return;
		}

		new AlertDialog.Builder(this)
				.setTitle(R.string.alert_title)
				.setMessage(R.string.del_share_request_info)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String delUrl = "";
								if (videoTag != null) {
									HashMap<String, String> params = new HashMap<String, String>();
									params.put("shareId",
											Integer.toString(videoShareID));
									if (videoTag.equals(VideoTag.share_come
											.name())) {
										delUrl = host
												+ getString(R.string.share_receive_del_url);
									} else if (videoTag
											.equals(VideoTag.share_go.name())) {
										delUrl = host
												+ getString(R.string.share_send_del_url);
									}

									HttpUtils
											.startHttpPostRequestWithSignature(
													delUrl, params, delShareRL,
													null);
								}
							}
						}).setNegativeButton(R.string.sBackButton, null).show();
	}

	private ResponseListener delShareRL = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Message message = Message.obtain();
			switch (status) {
			case 200:
				try {
					JSONObject jsonObject = new JSONObject(responseText);

					message.obj = jsonObject;
					message.what = MsgCodeDefine.MSG_ON_DEL_SHARE_RETURN;

				} catch (JSONException e) {
					e.printStackTrace();
					message.what = MsgCodeDefine.MSG_ERROR;
				}
				break;
			case 400:
				// auth failed, alert user to re-login
				message.what = MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN;
				break;
			default:
				message.what = MsgCodeDefine.MSG_ERROR;
				break;
			}
			bvdMsgHandler.sendMessage(message);
		}
	};

	class BVDMessageHandler extends Handler {
		public BVDMessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MsgCodeDefine.MSG_ON_FAVOR_RETURN: {
				JSONObject obj = (JSONObject) message.obj;
				int infoText = R.string.add_fav_ok;
				try {
					String result = obj.getString("result");
					if (result.equals("0")) {
						infoText = R.string.add_fav_ok;
					} else if (result.equals("1")) {
						infoText = R.string.already_favored;
					} else {
						infoText = R.string.add_fav_failed;
					}
				} catch (JSONException e) {
					e.printStackTrace();
					infoText = R.string.add_fav_failed;
				}
				Toast.makeText(BaseVideoDetailActivity.this, infoText,
						Toast.LENGTH_SHORT).show();
			}
				break;
			case MsgCodeDefine.MSG_ON_UNFAVOR_RETURN: {
				JSONObject obj = (JSONObject) message.obj;
				int infoText = R.string.del_fav_ok;
				try {
					String result = obj.getString("result");
					if (result.equals("0")) {
						infoText = R.string.del_fav_ok;
						Toast.makeText(BaseVideoDetailActivity.this, infoText,
								Toast.LENGTH_SHORT).show();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Intent intent = BaseVideoDetailActivity.this
								.getIntent();
						setResult(ActivityRequests.ON_UNFAVORED, intent);
						BaseVideoDetailActivity.this.finish();
						return;
					} else {
						infoText = R.string.del_fav_failed;
					}
				} catch (JSONException e) {
					e.printStackTrace();
					infoText = R.string.del_fav_failed;
				}
				Toast.makeText(BaseVideoDetailActivity.this, infoText,
						Toast.LENGTH_SHORT).show();
			}
				break;
			case MsgCodeDefine.MSG_ON_DEL_SHARE_RETURN: {
				JSONObject obj = (JSONObject) message.obj;
				int infoText = R.string.delete_ok;
				try {
					String result = obj.getString("result");
					if (result.equals("0")) {
						infoText = R.string.delete_ok;
						Toast.makeText(BaseVideoDetailActivity.this, infoText,
								Toast.LENGTH_SHORT).show();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Intent intent = BaseVideoDetailActivity.this
								.getIntent();
						setResult(ActivityRequests.ON_DELETED, intent);
						BaseVideoDetailActivity.this.finish();
						return;
					} else {
						infoText = R.string.deletion_failed;
					}
				} catch (JSONException e) {
					e.printStackTrace();
					infoText = R.string.deletion_failed;
				}
				Toast.makeText(BaseVideoDetailActivity.this, infoText,
						Toast.LENGTH_SHORT).show();
			}
				break;
			case MsgCodeDefine.MSG_ON_GET_SHARE_RECEIVERS_RETURN: {
				JSONObject jsonObject = (JSONObject) message.obj;
				try {
					JSONArray recvs = jsonObject.getJSONArray("list");
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < recvs.length(); i++) {
						JSONObject recv = recvs.getJSONObject(i);
						String receiver = recv.getString("receive");
						ContactManager cm = ContactManagerFactory
								.getContactManager();
						Contact contact = cm.getContactByPhone(receiver);

						sb.append(contact.getDisplayName()).append(',');
					}
					if (sb.toString().endsWith(",")) {
						sb.deleteCharAt(sb.length() - 1);
					}
					TextView namesTV = (TextView) findViewById(R.id.detail_from_to_names);
					namesTV.setText(sb.toString());
				} catch (JSONException e) {
					e.printStackTrace();
					TextView namesTV = (TextView) findViewById(R.id.detail_from_to_names);
					namesTV.setText(R.string.no_receivers);
				}
			}
				break;
			case MsgCodeDefine.MSG_ON_GET_SHARE_RECEIVERS_FAILED:
				TextView namesTV = (TextView) findViewById(R.id.detail_from_to_names);
				namesTV.setText(R.string.no_receivers);
				break;
			case MsgCodeDefine.MSG_AUTHENTICATED:
				onAuthReturned(true, (JSONObject) message.obj);
				break;
			case MsgCodeDefine.MSG_AUTH_FAILED:
				onAuthReturned(false, null);
				break;
			case MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN:
				alertRelogin();
				break;
			default:
				Toast.makeText(BaseVideoDetailActivity.this,
						R.string.server_operation_failed, Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	}

	/**
	 * alert to re-login
	 */
	protected void alertRelogin() {
		new AlertDialog.Builder(BaseVideoDetailActivity.this)
				.setTitle(R.string.alert_title)
				.setMessage(R.string.account_need_relogin)
				.setPositiveButton(R.string.account_setting,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								login();
							}
						}).setNegativeButton(R.string.cancel, null).show();
	}

	/**
	 * do authentication with server
	 */
	protected void doAuth() {
		HttpUtils
				.startHttpPostRequestWithSignature(authUrl, null, authRL, null);
	}

	private ResponseListener authRL = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			switch (status) {
			case 200:
				try {
					JSONObject jsonObject = new JSONObject(responseText);
					Message msg = Message.obtain();
					msg.what = MsgCodeDefine.MSG_AUTHENTICATED;
					msg.obj = jsonObject;
					bvdMsgHandler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
					bvdMsgHandler.sendEmptyMessage(MsgCodeDefine.MSG_ERROR);
				}
				break;
			case 400:
				bvdMsgHandler.sendEmptyMessage(MsgCodeDefine.MSG_AUTH_FAILED);
				break;
			default:
				bvdMsgHandler.sendEmptyMessage(MsgCodeDefine.MSG_ERROR);
				break;
			}
		}
	};

	/**
	 * invoked when authentication returns
	 * 
	 * @param isAuthed
	 *            - true : authenticated, false: auth failed
	 */
	private void onAuthReturned(boolean isAuthed, JSONObject jobj) {
		if (!isAuthed) {
			// authentication failed, alert to re-login
			alertRelogin();
		}

		if (currentAuthListener != null) {
			currentAuthListener.onAuthReturned(isAuthed, jobj);
		}
	}

	/**
	 * play the video
	 * 
	 * @param url
	 */
	protected void play(String url) {
		VideoCommonOpUtil.recordPlayCount(sourceId, host
				+ getString(R.string.record_play_count_url));

		Intent intent = new Intent();
		intent.setClass(this, FeiYingVideoplayer.class);
		Bundle bundle = new Bundle();
		bundle.putString("videoUrl", url);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	/**
	 * set OnAuthReturnListener
	 * 
	 * @param lis
	 */
	public void setOnAuthReturnListener(OnAuthReturnListener lis) {
		currentAuthListener = lis;
	}

	/**
	 * interface to listen authentication, onAuthReturned will be called when
	 * auth returned
	 * 
	 * @author sk
	 * 
	 */
	interface OnAuthReturnListener {
		public void onAuthReturned(boolean isAuthed, JSONObject jobj);
	}
}
