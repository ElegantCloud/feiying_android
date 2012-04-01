package com.ivyinfo.feiying.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.listitemholder.Channel;

public class ChannelListAdapter extends BaseAdapter {

	LayoutInflater mInflater;
	JSONArray jsonChannelList;

	public ChannelListAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
		jsonChannelList = new JSONArray();
	}

	public void setChannelData(JSONArray channelList) {
		this.jsonChannelList = channelList;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return jsonChannelList.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			return jsonChannelList.getJSONObject(position);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Channel channel = new Channel();
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.channellist, null);

			channel.img_btn = (ImageButton) convertView
					.findViewById(R.id.channel_img);
			channel.title = (TextView) convertView
					.findViewById(R.id.channel_name);

			convertView.setTag(channel);
		} else {
			channel = (Channel) convertView.getTag();
		}

		JSONObject jsonChannel = (JSONObject) getItem(position);

		try {
			channel.title.setText(jsonChannel.getString(Channel.TITLE));
			channel.img_btn.setImageResource(jsonChannel.getInt(Channel.IMGPATH));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return convertView;
	}
}
