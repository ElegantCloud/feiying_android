package com.ivyinfo.feiying.constant;

import com.ivyinfo.feiying.android.R;

public enum Channels {
	video(0, R.string.video), movie(1, R.string.movie), series(2, R.string.series), news(3,
			R.string.information), fun(4, R.string.fun), music(5,
			R.string.music), sports(6, R.string.sports), fashion(7,
			R.string.fashion), entertainment(8, R.string.entertainment), variety(
			9, R.string.variety);

	private int channelID;
	private int resID;

	private Channels(int channelID, int resID) {
		this.channelID = channelID;
		this.resID = resID;
	}

	public int channelID() {
		return channelID;
	}

	public int stringResID() {
		return resID;
	}

	public static int getResIDByChannelID(int channelID) {
		int resid = 0;
		switch (channelID) {
		case 1:
			resid = movie.resID;
			break;
		case 2:
			resid = series.resID;
			break;
		case 3:
			resid = news.resID;
			break;
		case 4:
			resid = fun.resID;
			break;
		case 5:
			resid = music.resID;
			break;
		case 6:
			resid = sports.resID;
			break;
		case 7:
			resid = fashion.resID;
			break;
		case 8:
			resid = entertainment.resID;
			break;
		case 9:
			resid = variety.resID;
			break;
		default:
			resid = video.resID;
			break;
		}
		return resid;
	}
}
