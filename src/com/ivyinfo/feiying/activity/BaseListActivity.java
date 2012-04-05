package com.ivyinfo.feiying.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.ActivityRequests;
import com.ivyinfo.feiying.constant.MsgCodeDefine;
import com.ivyinfo.feiying.constant.VideoTag;

/**
 * control show more item button in the list
 */
public class BaseListActivity extends Activity {
	private Handler blMsgHandler;

	protected View moreBtView;

	protected String host;
	protected String nextPageURL = "";
	protected boolean hasNextPage;
	protected int currentListStatus;
	
	/** indicate current video state: normal, favored, shared etc.*/
	protected String videoTag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		blMsgHandler = new BLMessageHandler(Looper.myLooper());
		moreBtView = LayoutInflater.from(this).inflate(
				R.layout.more_item_button, null);

		host = getString(R.string.host);

		videoTag = VideoTag.normal.name();
		currentListStatus = ActivityRequests.ON_NORMAL;
		hasNextPage = true;
	}

	protected void showLoadingMoreProgressbar() {
		Message msg = Message.obtain();
		msg.what = MsgCodeDefine.MSG_SHOW_MORE_PB;
		blMsgHandler.sendMessage(msg);
	}

	protected void hideLoadingMoreProgressbar() {
		Message msg = Message.obtain();
		msg.what = MsgCodeDefine.MSG_HIDE_MORE_PB;
		blMsgHandler.sendMessage(msg);
	}

	protected void showNoMoreItemInfo() {
		Message msg = Message.obtain();
		msg.what = MsgCodeDefine.MSG_SHOW_NO_MORE_ITEM_INFO;
		blMsgHandler.sendMessage(msg);
	}
	
	class BLMessageHandler extends Handler {
		public BLMessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MsgCodeDefine.MSG_SHOW_MORE_PB: {
				View loadingPb = moreBtView.findViewById(R.id.more_loading_pb);
				loadingPb.setVisibility(View.VISIBLE);

				TextView textInfo = (TextView) moreBtView
						.findViewById(R.id.more_item_text);
				textInfo.setText(R.string.loading);
			}
				break;
			case MsgCodeDefine.MSG_HIDE_MORE_PB: {
				View loadingPb = moreBtView.findViewById(R.id.more_loading_pb);
				loadingPb.setVisibility(View.GONE);

				TextView textInfo = (TextView) moreBtView
						.findViewById(R.id.more_item_text);
				textInfo.setText(R.string.more);
			}
				break;
			case MsgCodeDefine.MSG_SHOW_NO_MORE_ITEM_INFO: {
				View loadingPb = moreBtView.findViewById(R.id.more_loading_pb);
				loadingPb.setVisibility(View.GONE);

				TextView textInfo = (TextView) moreBtView
						.findViewById(R.id.more_item_text);
				textInfo.setText(R.string.no_more);
			}
				break;
			}
		}
	}
}
