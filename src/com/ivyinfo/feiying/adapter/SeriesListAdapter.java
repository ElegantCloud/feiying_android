package com.ivyinfo.feiying.adapter;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.listitemholder.ViewSeries;
import com.ivyinfo.feiying.utity.AsyncImageLoader;
import com.ivyinfo.feiying.utity.ImageCallback;
import com.ivyinfo.user.UserManager;

public class SeriesListAdapter extends BaseVideoListAdapter {

	private Context context;

	public SeriesListAdapter(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewSeries series = new ViewSeries();
		// if (convertView == null) {
		convertView = mInflater.inflate(R.layout.serieslist, null);

		series.img_btn = (ImageButton) convertView
				.findViewById(R.id.series_btn_img);
		series.title = (TextView) convertView.findViewById(R.id.series_title);
		series.episode = (TextView) convertView
				.findViewById(R.id.series_episode);
		series.actor = (TextView) convertView.findViewById(R.id.series_actor);
		series.releaseDate = (TextView) convertView
				.findViewById(R.id.series_release_date);
		// convertView.setTag(series);
		// } else {
		// series = (ViewSeries) convertView.getTag();
		// }

		series.img_btn.setImageBitmap(null);
		final JSONObject jsonSeries = (JSONObject) getItem(position);
		try {
			String imgURL = "";

			if (UserManager.getInstance().getUser().getUserkey().equals("")) {
				imgURL = jsonSeries.getString(VideoConstants.image_url.name());
			} else {
				String sourceID = jsonSeries.getString(VideoConstants.source_id
						.name());
				imgURL = context.getString(R.string.host_2) + "/" + sourceID
						+ ".jpg";
			}
			if (imgURL != "") {
				Bitmap img = AsyncImageLoader.getInstance().loadImage(imgURL,
						new ImageCallback(series.img_btn));
				series.img_btn.setImageBitmap(img);
			}

			series.title.setText(jsonSeries.getString(VideoConstants.title
					.name()));
			String count = jsonSeries.getString(VideoConstants.episode_count
					.name());
			String isHotPlaying = jsonSeries
					.getString(VideoConstants.episode_all.name());
			if (isHotPlaying.equals("0")) {
				count = context.getString(R.string.publishing);
			}
			series.episode.setText(count);
			series.actor.setText(jsonSeries.getString(VideoConstants.actor
					.name()));
			series.releaseDate.setText(jsonSeries
					.getString(VideoConstants.release_date.name()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
