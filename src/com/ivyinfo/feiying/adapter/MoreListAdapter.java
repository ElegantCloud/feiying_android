package com.ivyinfo.feiying.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.CommonConstants;

public class MoreListAdapter extends BaseAdapter {

	LayoutInflater mInflater;
	JSONArray jsonMoreList;

	public MoreListAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
		jsonMoreList = new JSONArray();
	}

	public void setListData(JSONArray list) {
		this.jsonMoreList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return jsonMoreList.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			return jsonMoreList.getJSONObject(position);
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
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.more_listitem_view, null);

			viewHolder.icon = (ImageView)convertView.findViewById(R.id.more_item_icon);
			viewHolder.nameTV = (TextView)convertView.findViewById(R.id.more_item_name);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		JSONObject jsonMoreItem = (JSONObject) getItem(position);

		try {
			viewHolder.icon.setImageResource(jsonMoreItem.getInt(CommonConstants.img.name()));
			viewHolder.nameTV.setText(jsonMoreItem.getString(CommonConstants.name.name()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return convertView;
	}
	

	final class ViewHolder {
		public ImageView icon;
		public TextView nameTV;
	}
}
