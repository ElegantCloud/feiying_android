package com.ivyinfo.feiying.activity.other;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.ivyinfo.feiying.android.R;

public class AboutActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_view);
	}
	
	public void onBack(View v) {
		finish();
	}
}
