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
import com.ivyinfo.feiying.utity.AsyncImageLoader;
import com.ivyinfo.feiying.utity.ImageCallback;

public class VideoSearchResultListAdapter extends BaseVideoListAdapter {

	public VideoSearchResultListAdapter(Context context) {
		super(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.video_search_result_listitem_view, null);
			viewHolder.imgBt = (ImageButton) convertView
					.findViewById(R.id.search_video_btn_img);
			viewHolder.titleTV = (TextView) convertView
					.findViewById(R.id.search_video_title);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		JSONObject video = (JSONObject) getItem(position);
		if (video != null) {
			try {
				String title = video.getString(VideoConstants.title.name());
				viewHolder.titleTV.setText(title);

				String imgURL = video
						.getString(VideoConstants.image_url.name());
				if (imgURL != "") {
					Bitmap img = AsyncImageLoader.getInstance().loadImage(
							imgURL, new ImageCallback(viewHolder.imgBt));
					viewHolder.imgBt.setImageBitmap(img);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return convertView;
	}

	final class ViewHolder {
		public ImageButton imgBt;
		public TextView titleTV;
	}
}
