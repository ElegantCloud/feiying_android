package com.ivyinfo.feiying.activity.subviews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivyinfo.feiying.activity.other.ContactMultiSelectListAcitivity;
import com.ivyinfo.feiying.activity.system.RegisterAndLoginActivity;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.AccountState;
import com.ivyinfo.feiying.constant.ActivityRequests;
import com.ivyinfo.feiying.constant.CommonConstants;
import com.ivyinfo.feiying.constant.ContactConstants;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;
import com.ivyinfo.feiying.utity.TextUtility;
import com.ivyinfo.feiying.utity.Utity;
import com.ivyinfo.feiying.utity.ValidatePattern;

/**
 * SendShareActivity takes charge of displaying the UI of share editor and
 * sending the share info
 * 
 * @author sk
 * 
 */
public class VideoShareEditorActivity extends Activity {
	private EditText receiverET;

	private String shareURL;

	private String sourceId;
	private int channel;
	private Handler messageHandler;
	private ProgressDialog progressDialog;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_share_editor_view);

		messageHandler = new MessageHandler(Looper.myLooper());

		Intent intent = getIntent();
		String title = intent.getStringExtra(VideoConstants.title.name());
		sourceId = intent.getStringExtra(VideoConstants.source_id.name());
		channel = intent.getIntExtra(VideoConstants.channel.name(), 0);

		TextView videoInfoTV = (TextView) findViewById(R.id.share_editor_video_info);
		videoInfoTV.setText(title);

		receiverET = (EditText) findViewById(R.id.share_editor_receiver_et);

		shareURL = getString(R.string.host) + getString(R.string.share_add_url);
	}

	public void onBack(View view) {
		finish();
	}

	public void onSelectContact(View v) {
		Intent intent = new Intent(this, ContactMultiSelectListAcitivity.class);
		startActivityForResult(intent, ActivityRequests.REQ_SELECT_CONTACTS);
	}

	private String composeMsg() {
		StringBuffer shareInfoSB = new StringBuffer();

		String playURL = getString(R.string.host)
				+ getString(R.string.web_play_url) + "/" + channel + "/"
				+ sourceId;
		shareInfoSB.append(getString(R.string.share_video)).append(playURL)
				.append("\r\n");

		EditText userMessageET = (EditText) findViewById(R.id.share_editor_user_message);
		String userMessage = userMessageET.getText().toString();
		if (userMessage.length() > 0) {
			shareInfoSB.append(getString(R.string.user_message)).append(":");
			shareInfoSB.append(userMessage);
		}
		return shareInfoSB.toString();
	}

	public void onSendShare(View v) {
		String shareInfo = composeMsg();
		
		try {
			String receiver = getReceiver();
			List<String> receiverList = getReceivers();
			Utity.sendMultiSMS(receiverList, shareInfo, this, null);

			progressDialog = ProgressDialog.show(this, null,
					getString(R.string.sending_share), true);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("sourceId", sourceId);
			params.put("phoneStr", receiver);
			params.put("channel", Integer.toString(channel));
			params.put("info", shareInfo);
			HttpUtils.startHttpPostRequestWithSignature(shareURL, params,
					shareSendRL, null);
		} catch (Exception e) {
			String info = e.getMessage();
			new AlertDialog.Builder(this).setTitle(R.string.alert_title)
					.setMessage(info).setNegativeButton(R.string.ok, null)
					.show();
		}
	}

	private ResponseListener shareSendRL = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Log.d("feiying", "shareSendRL - onComplete - status: " + status
					+ " response: " + responseText);
			Message message = Message.obtain();
			switch (status) {
			case 200:
				try {
					JSONObject jsonObject = new JSONObject(responseText);

					message.obj = jsonObject;
					message.what = MsgCodeDefine.MSG_ON_SHARE_RETURN;

				} catch (JSONException e) {
					e.printStackTrace();
					message.what = MsgCodeDefine.MSG_ERROR;
				}
				break;
			case 400:
				// auth failed, alert user to re-login
				message.what =MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN;
				break;
			default:
				message.what = MsgCodeDefine.MSG_ERROR;
				break;
			}
			messageHandler.sendMessage(message);
		}
	};

	/**
	 * get receiver from receiver input field
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getReceiver() throws Exception {
		String receiverTmp = receiverET.getText().toString();
		String[] receivers = TextUtility.splitText(receiverTmp, ",");
		StringBuffer receiverSB = new StringBuffer();
		if (receivers != null) {
			for (String receiver : receivers) {
				if (ValidatePattern.isValidMobilePhone(receiver)) {
					receiverSB.append(receiver).append(',');
				} else {
					// extract the phone number from receiver
					String[] numbers = TextUtility
							.splitText(receiver, "<", ">");
					if (numbers.length > 0) {
						String number = numbers[0];
						if (ValidatePattern.isValidMobilePhone(number)) {
							receiverSB.append(number).append(',');
						} else {
							String info = getString(R.string.invalid_receiver)
									+ receiver
									+ getString(R.string.pls_correct_receiver);
							throw new Exception(info);
						}
					} else {
						String info = getString(R.string.invalid_receiver)
								+ receiver
								+ getString(R.string.pls_correct_receiver);
						throw new Exception(info);
					}
				}
			}
		} else {
			throw new Exception(getString(R.string.no_receiver_found));
		}
		if (receiverSB.toString().endsWith(",")) {
			receiverSB.deleteCharAt(receiverSB.length() - 1);
		}
		return receiverSB.toString();
	}

	private List<String> getReceivers() throws Exception {
		String receiverTmp = receiverET.getText().toString();
		String[] receivers = TextUtility.splitText(receiverTmp, ",");
		List<String> recevierList = new ArrayList<String>();
		if (receivers != null) {
			for (String receiver : receivers) {
				if (ValidatePattern.isValidMobilePhone(receiver)) {
					recevierList.add(receiver);
				} else {
					// extract the phone number from receiver
					String[] numbers = TextUtility
							.splitText(receiver, "<", ">");
					if (numbers.length > 0) {
						String number = numbers[0];
						if (ValidatePattern.isValidMobilePhone(number)) {
							recevierList.add(number);
						} else {
							String info = getString(R.string.invalid_receiver)
									+ receiver
									+ getString(R.string.pls_correct_receiver);
							throw new Exception(info);
						}
					} else {
						String info = getString(R.string.invalid_receiver)
								+ receiver
								+ getString(R.string.pls_correct_receiver);
						throw new Exception(info);
					}
				}
			}
		} else {
			throw new Exception(getString(R.string.no_receiver_found));
		}
		return recevierList;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case ActivityRequests.REQ_SELECT_CONTACTS:
			if (resultCode == ActivityRequests.RESULT_OK) {
				Bundle bundle = data.getExtras();
				ArrayList<String> selectedContacts = bundle
						.getStringArrayList("selected_contacts");
				fillReceiverField(selectedContacts);
			}
			break;

		default:
			break;
		}

	}

	/**
	 * fill up the receiver input field by selected contacts
	 * 
	 * @param contacts
	 */
	private void fillReceiverField(ArrayList<String> contacts) {
		StringBuffer sb = new StringBuffer();
		for (String contactInfo : contacts) {
			try {
				JSONObject obj = new JSONObject(contactInfo);
				String name = obj.getString(ContactConstants.name.name());
				String phone = obj.getString(ContactConstants.phone_number
						.name());

				if (phone.startsWith("+") && phone.length() > 3) {
					phone = phone.substring(3);
				}

				sb.append(name).append('<').append(phone).append('>')
						.append(',');

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		Editable editor = receiverET.getEditableText();
		String origReceiver = editor.toString();
		if (origReceiver.length() > 0 && !origReceiver.endsWith(",")) {
			editor.append(',');
		}
		editor.append(sb.toString());
	}

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
			case MsgCodeDefine.MSG_ON_SHARE_RETURN:
				JSONObject obj = (JSONObject) message.obj;
				try {
					String result = obj.getString("result");
					if (result.equals("0")) {
						Toast.makeText(VideoShareEditorActivity.this,
								R.string.add_share_ok, Toast.LENGTH_SHORT)
								.show();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						VideoShareEditorActivity.this.finish();
						return;
					} else {
						Toast.makeText(VideoShareEditorActivity.this,
								R.string.add_share_failed, Toast.LENGTH_SHORT)
								.show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(VideoShareEditorActivity.this,
							R.string.add_share_failed, Toast.LENGTH_SHORT)
							.show();
				}
				break;
			case MsgCodeDefine.MSG_ACCOUNT_NEED_RELOGIN:
				new AlertDialog.Builder(VideoShareEditorActivity.this)
						.setTitle(R.string.alert_title)
						.setMessage(R.string.account_need_relogin)
						.setPositiveButton(R.string.account_setting,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										login();
									}
								}).show();
				break;
			default:
				Toast.makeText(VideoShareEditorActivity.this,
						R.string.add_share_failed, Toast.LENGTH_SHORT).show();
				break;
			}

		}
	}
	
	protected void login() {
		Intent intent = new Intent();
		intent.setClass(this, RegisterAndLoginActivity.class);
		intent.putExtra(CommonConstants.account_state.name(),
				AccountState.account_set.name());
		startActivityForResult(intent, ActivityRequests.REQ_LOGIN);
	}
}
