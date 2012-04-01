package com.ivyinfo.feiying.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.CommonConstants;

public class KeywordListAdapter extends BaseAdapter {
	protected LayoutInflater mInflater;
	private JSONArray keywordList;

	public KeywordListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		keywordList = new JSONArray();
	}

	public void setData(JSONArray dataList) {
		this.keywordList = dataList;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return keywordList.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			JSONObject obj = keywordList.getJSONObject(position);
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
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
			convertView = mInflater.inflate(R.layout.keyword_listitem_view,
					null);
			viewHolder.keywordTV = (TextView) convertView
					.findViewById(R.id.keyword_text);
			viewHolder.countTV = (TextView) convertView
					.findViewById(R.id.keyword_count);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		JSONObject obj = (JSONObject) getItem(position);
		if (obj != null) {
			try {
				viewHolder.keywordTV.setText(obj
						.getString(CommonConstants.keyword.name()));
				viewHolder.countTV.setText(obj.getString(CommonConstants.count
						.name()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return convertView;
	}

	final class ViewHolder {
		public TextView keywordTV;
		public TextView countTV;
	}
}
