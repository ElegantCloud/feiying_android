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

public class EpisodeListAdapter extends BaseAdapter {
	private Context context;

	private JSONArray episodeList;

	public EpisodeListAdapter(Context context) {
		this.context = context;
		episodeList = new JSONArray();
	}

	public void setEpisodeList(JSONArray episodeList) {
		this.episodeList = episodeList;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return episodeList.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			return episodeList.getJSONObject(position);
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
		JSONObject episode = (JSONObject) getItem(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.episode_listitem_view, null);
			
			viewHolder.text = (TextView) convertView.findViewById(R.id.episode_number_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		if (episode != null) {
			try {
				int index = episode.getInt("episode_index");
				String episodeNumber = context.getString(R.string.episode_num_x);
				episodeNumber = String.format(episodeNumber, index);
				viewHolder.text.setText(episodeNumber);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return convertView;
	}

	final class ViewHolder {
		public TextView text;
	}
}
