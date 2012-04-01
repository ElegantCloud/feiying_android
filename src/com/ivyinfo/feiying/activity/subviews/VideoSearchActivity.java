package com.ivyinfo.feiying.activity.subviews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ivyinfo.feiying.adapter.KeywordListAdapter;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.CommonConstants;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.http.HttpUtils;
import com.ivyinfo.feiying.http.HttpUtils.ResponseListener;

public class VideoSearchActivity extends Activity {
	private KeywordListAdapter listAdapter;
	private MessageHandler messageHandler;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_search_view);
		messageHandler = new MessageHandler(Looper.myLooper());
		
		listAdapter = new KeywordListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.video_keywords_list);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(kwCL);
		refreshKeywords();
	}

	private void refreshKeywords() {
		String url = getString(R.string.host) + getString(R.string.keywords_url);
		HttpUtils.startHttpPostRequest(url, null, kwRL, null);
	}
	
	private OnItemClickListener kwCL = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			JSONObject obj = (JSONObject) listAdapter.getItem(position);
			if (obj != null) {
				try {
					String keyword = obj.getString(CommonConstants.keyword.name());
					search(keyword);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	private ResponseListener kwRL = new ResponseListener() {
		
		@Override
		public void onComplete(int status, String responseText) {
			Log.d("feiying", "resLis - onComplete - status: " + status
					+ " response: " + responseText);
			switch (status) {
			case 200:
				try {
					JSONObject jsonObject = new JSONObject(responseText);
					Message message = Message.obtain();
					message.obj = jsonObject;
					message.what = MsgCodeDefine.MSG_ON_GET_DATA_RETURN;
					messageHandler.sendMessage(message);

				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}
	};

	public void onSearch(View v) {
		EditText searchET = (EditText) findViewById(R.id.video_search_input_field);
		String keyword = searchET.getText().toString().trim();
		if (keyword.equals("")) {
			Toast.makeText(this, R.string.input_search_keyword,
					Toast.LENGTH_SHORT).show();
			return;
		}
		search(keyword);
	}
	
	private void search(String keyword) {
		Intent intent = new Intent(this, VideoSearchResultActivity.class);
		intent.putExtra(CommonConstants.keyword.name(), keyword);
		startActivity(intent);
	}

	public void onBack(View view) {
		finish();
	}
	
	class MessageHandler extends Handler {
		public MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MsgCodeDefine.MSG_ON_GET_DATA_RETURN:
				JSONObject obj = (JSONObject)message.obj;
				try {
					JSONArray arr = obj.getJSONArray("list");
					listAdapter.setData(arr);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				break;

			default:
				break;
			}
		}
	}
}
