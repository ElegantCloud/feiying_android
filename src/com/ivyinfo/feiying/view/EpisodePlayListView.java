package com.ivyinfo.feiying.view;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ivyinfo.feiying.adapter.EpisodeListAdapter;
import com.ivyinfo.feiying.android.R;

public class EpisodePlayListView extends ListView {
	private PlayListAdapter adapter;
	private PlayListItemListener playListener;

	public EpisodePlayListView(Context context) {
		super(context);
		init(context);
	}

	public EpisodePlayListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EpisodePlayListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		adapter = new PlayListAdapter(context);
		setAdapter(adapter);
		setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				adapter.toggle(position);
			}
		});
	}

	public void setEpisodeList(JSONArray list) {
		adapter.setPlaySections(list);
	}

	/**
	 * set play list item listener, triggered when an episode is clicked
	 * 
	 * @param lis
	 */
	public void setPlayListItemListener(PlayListItemListener lis) {
		playListener = lis;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

	class PlayListAdapter extends BaseAdapter {
		private static final int SECTION_SIZE = 20;
		private Context context;

		private List<String> titles;
		private List<JSONArray> sections;
		private List<Boolean> expanded;

		public PlayListAdapter(Context context) {
			this.context = context;
			sections = new ArrayList<JSONArray>();
			titles = new ArrayList<String>();
			expanded = new ArrayList<Boolean>();
		}

		public void setPlaySections(JSONArray episodeList) {
			sections = new ArrayList<JSONArray>();
			titles = new ArrayList<String>();
			for (int i = 0; i < episodeList.length(); i += SECTION_SIZE) {
				JSONArray section = new JSONArray();
				int j = 0;
				for (; j < SECTION_SIZE && i + j < episodeList.length(); j++) {
					try {
						JSONObject obj = episodeList.getJSONObject(i + j);
						section.put(obj);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				String fromTo = context.getString(R.string.episode_from_x_to_y);
				fromTo = String.format(fromTo, i + 1, i + j);
				expanded.add(false);
				titles.add(fromTo);
				sections.add(section);
			}
			if (expanded.size() > 0) {
				expanded.set(0, true);
			}
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return sections.size();
		}

		@Override
		public Object getItem(int position) {
			JSONArray section = sections.get(position);
			return section;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private ViewHolder viewHolder;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String title = titles.get(position);
			final JSONArray section = sections.get(position);
			Boolean expand = expanded.get(position);
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.play_section_view, null);

				viewHolder.titleTV = (TextView) convertView
						.findViewById(R.id.playlist_title_tv);
				viewHolder.secGridView = (FYGridView) convertView
						.findViewById(R.id.play_section_gridview);

				viewHolder.secAdapter = new EpisodeListAdapter(context);
				viewHolder.secGridView.setAdapter(viewHolder.secAdapter);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.secGridView
					.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							if (playListener != null) {
								try {
									JSONObject obj = section
											.getJSONObject(position);
									playListener.onItemClicked(obj);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					});
			viewHolder.titleTV.setText(title);
			viewHolder.secGridView
					.setVisibility(expand.booleanValue() ? VISIBLE : GONE);
			viewHolder.secAdapter.setEpisodeList(section);

			return convertView;
		}

		public void toggle(int position) {
			expanded.set(position, !expanded.get(position).booleanValue());
			notifyDataSetChanged();
		}
	}

	final class ViewHolder {
		public TextView titleTV;
		public FYGridView secGridView;
		public EpisodeListAdapter secAdapter;
	}

}
