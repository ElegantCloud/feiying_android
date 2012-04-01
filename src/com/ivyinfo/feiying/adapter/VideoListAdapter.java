package com.ivyinfo.feiying.adapter;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.listitemholder.ViewVideo;
import com.ivyinfo.feiying.utity.AsyncImageLoader;
import com.ivyinfo.feiying.utity.ImageCallback;

public class VideoListAdapter extends BaseVideoListAdapter {

	public VideoListAdapter(Activity context) {
		super(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewVideo video = new ViewVideo();
//		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.videolist, null);

			video.img_btn = (ImageButton) convertView
					.findViewById(R.id.video_btn_img);
			video.title = (TextView) convertView.findViewById(R.id.video_title);
			video.time = (TextView) convertView.findViewById(R.id.video_time);
			video.size = (TextView) convertView.findViewById(R.id.video_size);

//			convertView.setTag(video);
//		} else {
//			video = (ViewVideo) convertView.getTag();
//		}

		final JSONObject jsonVideo = (JSONObject) getItem(position);
		video.img_btn.setImageBitmap(null);
		try {
			String imgUrl = jsonVideo
					.getString(VideoConstants.image_url.name());
			if (imgUrl != "") {
				Bitmap img = AsyncImageLoader.getInstance().loadImage(imgUrl,
						new ImageCallback(video.img_btn));
				video.img_btn.setImageBitmap(img);
			}

			video.title
					.setText(jsonVideo.getString(VideoConstants.title.name()));
			video.time.setText(jsonVideo.getString(VideoConstants.time.name()));
			video.size.setText(jsonVideo.getString(VideoConstants.size.name()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
