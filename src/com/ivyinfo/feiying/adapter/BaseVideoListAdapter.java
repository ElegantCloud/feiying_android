package com.ivyinfo.feiying.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

public abstract class BaseVideoListAdapter extends BaseAdapter {
	protected LayoutInflater mInflater;
	protected List<JSONObject> videoList;

	public BaseVideoListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		videoList = new ArrayList<JSONObject>();
	}

	/**
	 * append new items to the list
	 * 
	 * @param newList
	 */
	public void addVideoList(JSONArray newList) {
		if (newList != null) {
			for (int i = 0; i < newList.length(); i++) {
				try {
					JSONObject obj = newList.getJSONObject(i);
					videoList.add(obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			notifyDataSetChanged();
		}
	}

	/**
	 * remove all the items in the list
	 */
	public void clear() {
		videoList.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return videoList.size();
	}

	@Override
	public Object getItem(int position) {
		return videoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
