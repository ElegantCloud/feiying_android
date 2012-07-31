package com.ivyinfo.feiying.activity.system;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ivyinfo.feiying.activity.FeiYingMainActivity;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.AccountState;
import com.ivyinfo.feiying.constant.BusinessStatus;
import com.ivyinfo.feiying.constant.CommonConstants;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;
import com.ivyinfo.feiying.utity.Utity;
import com.ivyinfo.user.User;
import com.ivyinfo.user.UserBean;
import com.ivyinfo.user.UserManager;

public class RegisterAndLoginActivity extends Activity {
	private Handler messageHandler;
	private ProgressDialog progressDialog;
	private SharedPreferences userInfoSettings;
	private String accState;

	private String getAuthCodeUrl;
	private String regLoginUrl;

	private EditText phoneNumberET;
	private AlertDialog codeVerifyDlg;
	private HttpUtils httpUtil;

	private String phoneNumber;
	private String host;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reglogin_view);

		Intent intent = getIntent();
		accState = intent.getStringExtra(CommonConstants.account_state.name());
		if (accState == null || accState.equals("")) {
			accState = AccountState.login.name();
		}
		Button exitBt = (Button) findViewById(R.id.reglogin_exit_bt);
		if (accState.equals(AccountState.login.name())) {
			exitBt.setText(R.string.exit);
		} else {
			exitBt.setText(R.string.sBackButton);
		}

		userInfoSettings = getSharedPreferences(
				CommonConstants.user_info.name(), 0);

		messageHandler = new MessageHandler(Looper.myLooper());

		host = getString(R.string.host);
		getAuthCodeUrl = host + getString(R.string.get_auth_code_url);
		regLoginUrl = host + getString(R.string.reglogin_url);

		phoneNumberET = (EditText) findViewById(R.id.reglogin_phone_number);

		initRegisterAndLoginView();
	}

	private void initRegisterAndLoginView() {
		String userName = userInfoSettings.getString(User.username, "");
		String userkey = userInfoSettings.getString(User.userkey, "");
		String status = userInfoSettings.getString(User.status, "unopened");
		if (accState.equals(AccountState.login.name())) {
			// login automatically
			if (!userName.equals("") && !userkey.equals("")) {
				UserBean user = UserManager.getInstance().setUserInfo(userName,
						userkey);
				user.setStatus(status);
				jumpToFeiyingMain();
				return;
			}
		}

		// set account manually
		TelephonyManager telephoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String number = telephoneManager.getLine1Number();
		if (number != null) {
			if (number.startsWith("+") && number.length() > 3) {
				number = number.substring(3);
			}
			phoneNumberET.setText(number);
		} else {
			phoneNumberET.setText(userName);
		}

	}

	private void jumpToFeiyingMain() {
		if (accState.equals(AccountState.login.name())) {
			Intent intent = new Intent(this, FeiYingMainActivity.class);
			startActivity(intent);
		}
		finish();
	}

	private void saveUserAccount() {
		UserBean user = UserManager.getInstance().getUser();
		userInfoSettings.edit().putString(User.username, user.getName())
				.putString(User.userkey, user.getUserkey())
				.putString(User.status, user.getStatus()).commit();
	}
	
	private void setUserAccount(String name, String userkey) {
		UserBean user = UserManager.getInstance().getUser();
		user.setName(name);
		user.setUserkey(userkey);
	}

	public void onExit(View v) {
		finish();
	}

	public void onGetAuthCode(View v) {
		phoneNumber = phoneNumberET.getText().toString().trim();
		if (phoneNumber.equals("")) {
			Toast.makeText(this, R.string.number_cannot_be_null,
					Toast.LENGTH_SHORT).show();
		} else {
			progressDialog = ProgressDialog.show(this, null,
					getString(R.string.getting_auth_code), true);
			HashMap<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("phone", phoneNumber);
			httpUtil = HttpUtils.startHttpPostRequest(getAuthCodeUrl, paramMap,
					getCodeRL, null);
		}
	}

	private ResponseListener getCodeRL = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Message msg = Message.obtain();
			switch (status) {
			case 200:
				try {
					JSONObject resObj = new JSONObject(responseText);
					msg.obj = resObj;
					msg.what = MsgCodeDefine.MSG_ON_GET_AUTH_CODE_RETURN;
				} catch (JSONException e) {
					e.printStackTrace();
					msg.what = MsgCodeDefine.MSG_ERROR;
				}
				break;

			default:
				msg.what = MsgCodeDefine.MSG_ERROR;
				break;
			}
			messageHandler.sendMessage(msg);
		}
	};

	/**
	 * process messages in UI thread
	 * 
	 * @author sk
	 * 
	 */
	class MessageHandler extends Handler {
		public MessageHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message message) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			switch (message.what) {
			case MsgCodeDefine.MSG_ON_GET_AUTH_CODE_RETURN: {
				JSONObject retObj = (JSONObject) message.obj;
				onGetAuthCodeReturn(retObj);
			}
				break;
			case MsgCodeDefine.MSG_ON_CHECK_AUTH_CODE_RETURN: {
				JSONObject retObj = (JSONObject) message.obj;
				onCheckAuthCodeReturn(retObj);
			}
				break;
			default:
				Toast.makeText(RegisterAndLoginActivity.this,
						R.string.server_error, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}

	private void onGetAuthCodeReturn(JSONObject retObj) {
		try {
			String result = retObj.getString("result");
			if (result.equals("0")) {
				// get auth code ok, show code input dialog
				View codeVerifyDialogView = LayoutInflater.from(
						RegisterAndLoginActivity.this).inflate(
						R.layout.codeverify_dialog, null);
				codeVerifyDlg = new AlertDialog.Builder(
						RegisterAndLoginActivity.this).setView(
						codeVerifyDialogView).show();

				final Button nextButton = (Button) codeVerifyDialogView
						.findViewById(R.id.code_check_next_button);
				nextButton.setEnabled(false);
				Button cancelButton = (Button) codeVerifyDialogView
						.findViewById(R.id.code_check_cancelButton);

				cancelButton.setOnClickListener(new OnClickListener() {
					public void onClick(View _view) {
						if (codeVerifyDlg != null)
							codeVerifyDlg.dismiss();
					}
				});

				final EditText codeInputET = (EditText) codeVerifyDialogView
						.findViewById(R.id.codeinput);
				codeInputET.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int arg1,
							int arg2, int count) {
						int len = s.length();
						if (len > 0) {
							nextButton.setEnabled(true);
						} else {
							nextButton.setEnabled(false);
						}
					}

					@Override
					public void afterTextChanged(Editable editable) {
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
					}
				});

				nextButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// do real register
						progressDialog = ProgressDialog.show(
								RegisterAndLoginActivity.this, null,
								getString(R.string.checking_auth_code), true);
						String code = codeInputET.getText().toString().trim();

						HashMap<String, String> params = new HashMap<String, String>();
						params.put("code", code);
						params.put("brand", Build.BRAND);
						params.put("model", Build.MODEL);
						params.put("release", Build.VERSION.RELEASE);
						params.put("sdk", Build.VERSION.SDK);
						
						DisplayMetrics dm = new DisplayMetrics();
						getWindowManager().getDefaultDisplay().getMetrics(dm);
						int nowWidth = dm.widthPixels; // 当前分辨率 宽度
						int nowHeigth = dm.heightPixels; // 当前分辨率高度
						params.put("width", Integer.toString(nowWidth));
						params.put("height", Integer.toString(nowHeigth));
						
						HttpUtils.startHttpPostRequest(regLoginUrl, params,
								checkCodeRL, httpUtil);
					}
				});

			} else if (result.equals("1")) {
				Toast.makeText(RegisterAndLoginActivity.this,
						R.string.number_cannot_be_null, Toast.LENGTH_SHORT)
						.show();
			} else if (result.equals("2")) {
				Toast.makeText(RegisterAndLoginActivity.this,
						R.string.wrong_phone_number, Toast.LENGTH_SHORT).show();
			} else if (result.equals("3")) {
				Toast.makeText(RegisterAndLoginActivity.this,
						R.string.phone_number_existed, Toast.LENGTH_SHORT)
						.show();
			}

		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(RegisterAndLoginActivity.this,
					R.string.server_error, Toast.LENGTH_SHORT).show();
		}
	}

	private ResponseListener checkCodeRL = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Message msg = Message.obtain();
			switch (status) {
			case 200:
				try {
					JSONObject resObj = new JSONObject(responseText);
					msg.obj = resObj;
					msg.what = MsgCodeDefine.MSG_ON_CHECK_AUTH_CODE_RETURN;
				} catch (JSONException e) {
					e.printStackTrace();
					msg.what = MsgCodeDefine.MSG_ERROR;
				}
				break;

			default:
				msg.what = MsgCodeDefine.MSG_ERROR;
				break;
			}
			messageHandler.sendMessage(msg);
		}
	};

	/**
	 * process when check auth code returns
	 * 
	 * @param obj
	 */
	private void onCheckAuthCodeReturn(JSONObject obj) {
		try {
			String result = obj.getString("result");
			if (result.equals("0")) {
				// correct auth code, do registration and login
				if (codeVerifyDlg != null) {
					codeVerifyDlg.dismiss();
				}
				JSONObject info = obj.getJSONObject("info");
				String userkey = info.getString(User.userkey);
				String status = info.getString(User.status);
				
				setUserAccount(phoneNumber, userkey);
				saveUserAccount();

//				if (status.equals(BusinessStatus.opened.name())) {
					// jump to main
					jumpToFeiyingMain();
					return;
//				}

//				// show dialog
//				if (status.equals(BusinessStatus.unopened.name())) {
//					// show open business dialog
//					new AlertDialog.Builder(RegisterAndLoginActivity.this)
//							.setTitle(R.string.alert_title)
//							.setMessage(R.string.open_business_info)
//							.setPositiveButton(R.string.open_business,
//									new DialogInterface.OnClickListener() {
//
//										@Override
//										public void onClick(
//												DialogInterface dialog,
//												int which) {
//											// send sms to open business
//											openBusiness();
//										}
//
//									})
//							.setNegativeButton(R.string.not_open_now,
//									new DialogInterface.OnClickListener() {
//
//										@Override
//										public void onClick(
//												DialogInterface dialog,
//												int which) {
//											jumpToFeiyingMain();
//										}
//
//									}).show();
//				} else if (status.equals(BusinessStatus.processing.name())) {
//					jumpToFeiyingMain();
//				}

			} else if (result.equals("2")) {
				// wrong auth code
				Toast.makeText(RegisterAndLoginActivity.this,
						R.string.wrong_auth_code, Toast.LENGTH_SHORT).show();
				if (codeVerifyDlg != null)
					codeVerifyDlg.dismiss();
			} else if (result.equals("6")) {
				// session timeout, re-get auth code
				Toast.makeText(RegisterAndLoginActivity.this,
						R.string.session_timeout_reget_auth_code,
						Toast.LENGTH_SHORT).show();
				if (codeVerifyDlg != null)
					codeVerifyDlg.dismiss();
			}

		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(RegisterAndLoginActivity.this,
					R.string.server_error, Toast.LENGTH_SHORT).show();
		}
	}

	private void openBusiness() {
		progressDialog = ProgressDialog.show(RegisterAndLoginActivity.this,
				null, getString(R.string.submitting_open_request), true);

		final BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				int msg = R.string.business_open_req_submitted;
				Log.d("feiying", "result code: " + getResultCode());
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Log.d("feiying", "send ok");
					String notifyUrl = host
							+ getString(R.string.business_open_notify_url);
					HttpUtils.startHttpPostRequestWithSignature(notifyUrl, null, null,
							null);

					break;
				default:
					msg = R.string.submit_failed;
					break;

				}
				
				new AlertDialog.Builder(RegisterAndLoginActivity.this)
						.setTitle(R.string.alert_title)
						.setMessage(msg)
						.setNeutralButton(R.string.ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										jumpToFeiyingMain();
									}

								}).show();

			}
		};

		String number = getString(R.string.business_number);
		String content = getString(R.string.business_open_content);
		Utity.sendSMS(number, content, RegisterAndLoginActivity.this, receiver);
	}

}
