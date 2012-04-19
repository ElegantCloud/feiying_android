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
import com.ivyinfo.feiying.listitemholder.ViewMovie;
import com.ivyinfo.feiying.utity.AsyncImageLoader;
import com.ivyinfo.feiying.utity.ImageCallback;
import com.ivyinfo.user.UserManager;

public class MovieListAdapter extends BaseVideoListAdapter {
	private Context context;

	public MovieListAdapter(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewMovie movie = new ViewMovie();
		// if (convertView == null) {
		convertView = mInflater.inflate(R.layout.movielist, null);

		movie.img_btn = (ImageButton) convertView
				.findViewById(R.id.movie_btn_img);
		movie.title = (TextView) convertView.findViewById(R.id.movie_title);
		movie.region = (TextView) convertView.findViewById(R.id.movie_region);
		movie.actor = (TextView) convertView.findViewById(R.id.movie_actor);
		movie.releaseDate = (TextView) convertView
				.findViewById(R.id.movie_release_date);
		// convertView.setTag(movie);
		// } else {
		// movie = (ViewMovie) convertView.getTag();
		// }

		movie.img_btn.setImageBitmap(null);
		final JSONObject jsonMovie = (JSONObject) getItem(position);
		try {

			String imgURL = "";

			if (UserManager.getInstance().getUser().getUserkey().equals("")) {
				imgURL = jsonMovie.getString(VideoConstants.image_url.name());
			} else {
				String sourceID = jsonMovie.getString(VideoConstants.source_id
						.name());
				imgURL = context.getString(R.string.host_2) + "/" + sourceID
						+ ".jpg";
			}
			if (imgURL != "") {
				Bitmap img = AsyncImageLoader.getInstance().loadImage(imgURL,
						new ImageCallback(movie.img_btn));
				movie.img_btn.setImageBitmap(img);
			}

			movie.title
					.setText(jsonMovie.getString(VideoConstants.title.name()));
			movie.region.setText(jsonMovie.getString(VideoConstants.origin
					.name()));
			movie.actor
					.setText(jsonMovie.getString(VideoConstants.actor.name()));
			movie.releaseDate.setText(jsonMovie
					.getString(VideoConstants.release_date.name()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
