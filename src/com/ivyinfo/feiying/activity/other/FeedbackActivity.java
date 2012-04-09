package com.ivyinfo.feiying.activity.other;

import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.FeedbackTypes;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;
import com.ivyinfo.user.UserManager;

public class FeedbackActivity extends Activity {
	private EditText userET;
	private EditText commentET;
	private TextView charsTV;
	private MessageHandler messageHandler;
	private FeedbackTypes type = FeedbackTypes.problem;
	private ProgressDialog progressDialog;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_view);

		messageHandler = new MessageHandler(Looper.myLooper());

		userET = (EditText) findViewById(R.id.feedback_user);
		charsTV = (TextView) findViewById(R.id.feedback_chars);
		commentET = (EditText) findViewById(R.id.feedback_comment);
		commentET.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				charsTV.setText(Integer.toString(s.length()));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		String username = UserManager.getInstance().getUser().getName();
		if (username != null) {
			userET.setText(username);
		}

		Spinner spinner = (Spinner) findViewById(R.id.feedback_types_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.feedback_types,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(selLis);
	}

	private OnItemSelectedListener selLis = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			switch (pos) {
			case 0:
				type = FeedbackTypes.problem;
				break;
			case 1:
				type = FeedbackTypes.advice;
				break;
			case 2:
				type = FeedbackTypes.requirement;
				break;
			case 3:
				type = FeedbackTypes.consult;
				break;
			case 4:
				type = FeedbackTypes.other;
				break;

			default:
				break;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	};

	public void onSubmit(View v) {

		String comment = commentET.getText().toString().trim();
		String user = userET.getText().toString().trim();
		if (comment.equals("")) {
			Toast.makeText(this, R.string.pls_input_feedback_comment,
					Toast.LENGTH_SHORT).show();
			return;
		}

		progressDialog = ProgressDialog.show(this, null,
				getString(R.string.submitting_feedback), true);

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("user", user);
		params.put("comment", comment);
		params.put("type", type.name());
		HttpUtils.startHttpPostRequest(getString(R.string.host)
				+ getString(R.string.feedback_url), params, feedbackRL, null);
	}

	private ResponseListener feedbackRL = new ResponseListener() {

		@Override
		public void onComplete(int status, String responseText) {
			Message message = Message.obtain();
			message.what = MsgCodeDefine.MSG_ON_FEEDBACK_RETURN;
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
			case MsgCodeDefine.MSG_ON_FEEDBACK_RETURN:
				Toast.makeText(FeedbackActivity.this,
						R.string.feedback_submit_ok, Toast.LENGTH_SHORT).show();
				finish();
				break;

			default:
				break;
			}
		}
	}

	public void onBack(View v) {
		finish();
	}

}
