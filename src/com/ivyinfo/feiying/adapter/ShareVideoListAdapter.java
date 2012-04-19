package com.ivyinfo.feiying.adapter;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ivyinfo.contact.ContactManager;
import com.ivyinfo.contact.ContactManagerFactory;
import com.ivyinfo.contact.beans.Contact;
import com.ivyinfo.feiying.android.R;
import com.ivyinfo.feiying.constant.ShareCategory;
import com.ivyinfo.feiying.constant.VideoConstants;
import com.ivyinfo.feiying.utity.AsyncImageLoader;
import com.ivyinfo.feiying.utity.ImageCallback;
import com.ivyinfo.user.UserManager;

public class ShareVideoListAdapter extends BaseVideoListAdapter {

	private Context context;
	private ShareCategory shareCategory;

	public ShareVideoListAdapter(Context context) {
		super(context);
		this.context = context;
	}

	public void setShareCategory(ShareCategory category) {
		this.shareCategory = category;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		JSONObject video = (JSONObject) getItem(position);
		// if (convertView == null) {
		viewHolder = new ViewHolder();
		convertView = mInflater.inflate(R.layout.share_video_listitem_view,
				null);

		viewHolder.imgBt = (ImageButton) convertView
				.findViewById(R.id.share_video_btn_img);
		viewHolder.titleTV = (TextView) convertView
				.findViewById(R.id.share_video_title);
		viewHolder.fromToTV = (TextView) convertView
				.findViewById(R.id.share_from_to_field);
		viewHolder.nameTV = (TextView) convertView
				.findViewById(R.id.share_person_name);
		viewHolder.dateTV = (TextView) convertView
				.findViewById(R.id.share_video_share_time);
		convertView.setTag(viewHolder);
		// } else {
		// viewHolder = (ViewHolder) convertView.getTag();
		// }

		viewHolder.imgBt.setImageBitmap(null);
		if (video != null) {
			try {
				String title = video.getString(VideoConstants.title.name());
				String imgURL = "";
				if (UserManager.getInstance().getUser().getUserkey().equals("")) {
					imgURL = video.getString(VideoConstants.image_url.name());
				} else {
					String sourceID = video.getString(VideoConstants.source_id
							.name());
					imgURL = context.getString(R.string.host_2) + "/" + sourceID
							+ ".jpg";
				}
				
				if (imgURL != "") {
					Bitmap img = AsyncImageLoader.getInstance().loadImage(
							imgURL, new ImageCallback(viewHolder.imgBt));
					viewHolder.imgBt.setImageBitmap(img);
				}
				
				long dateTime = video.getLong(VideoConstants.share_time.name());
				dateTime *= 1000;
				Log.d("feiying", "share time: " + dateTime);
				

				switch (shareCategory) {
				case share_receive:
					viewHolder.fromToTV.setVisibility(View.VISIBLE);
					viewHolder.nameTV.setVisibility(View.VISIBLE);
					viewHolder.fromToTV.setText(R.string.from);
					String senderNum = video.getString(VideoConstants.send
							.name());

					ContactManager cm = ContactManagerFactory
							.getContactManager();
					Contact sender = cm.getContactByPhone(senderNum);
					viewHolder.nameTV.setText(sender.getDisplayName());
					break;
				case share_send:
					viewHolder.fromToTV.setVisibility(View.GONE);
					viewHolder.nameTV.setVisibility(View.GONE);
					break;

				default:
					break;
				}

				viewHolder.titleTV.setText(title);
				viewHolder.dateTV.setText(DateUtils.formatDateTime(context,
						dateTime, DateUtils.FORMAT_NUMERIC_DATE));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return convertView;
	}

	final class ViewHolder {
		public ImageButton imgBt;
		public TextView titleTV;
		public TextView fromToTV;
		public TextView nameTV;
		public TextView dateTV;
	}
}
