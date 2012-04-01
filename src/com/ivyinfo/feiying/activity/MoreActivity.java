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

import com.ivyinfo.feiying.activity.other.AboutActivity;
import com.ivyinfo.feiying.activity.other.FeedbackActivity;
import com.ivyinfo.feiying.activity.system.RegisterAndLoginActivity;
import com.ivyinfo.feiying.adapter.MoreListAdapter;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.AccountState;
import com.ivyinfo.feiying.constant.CommonConstants;
import com.ivyinfo.feiying.constant.SystemFunctions;

public class MoreActivity extends Activity {
	private MoreListAdapter listAdapter;

	String[] items = { SystemFunctions.account_setting.name(), SystemFunctions.about.name(), SystemFunctions.feedback.name() };
	int[] titles = { R.string.account_setting, R.string.about, R.string.feedback };
	int[] imgs = { R.drawable.account, R.drawable.about, R.drawable.feedback };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_view);

		listAdapter = new MoreListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.more_list);
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < items.length; i++) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put(SystemFunctions.func_name.name(), items[i]);
				jsonObject.put(CommonConstants.name.name(),
						getString(titles[i]));
				jsonObject.put(CommonConstants.img.name(), imgs[i]);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsonArray.put(jsonObject);
		}
		listAdapter.setListData(jsonArray);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(listCL);

	}

	private OnItemClickListener listCL = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			JSONObject obj = (JSONObject) listAdapter.getItem(position);
			try {
				String funcName = obj.getString(SystemFunctions.func_name
						.name());
				Intent intent = new Intent();
				if (funcName.equals(SystemFunctions.account_setting.name())) {
					intent.setClass(MoreActivity.this, RegisterAndLoginActivity.class);
					intent.putExtra(CommonConstants.account_state.name(), AccountState.account_set.name());
				} else if (funcName.equals(SystemFunctions.about.name())) {
					intent.setClass(MoreActivity.this, AboutActivity.class);
				} else if (funcName.equals(SystemFunctions.feedback.name())) {
					intent.setClass(MoreActivity.this, FeedbackActivity.class);
				}
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

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