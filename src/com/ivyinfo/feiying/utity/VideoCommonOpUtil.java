package com.ivyinfo.feiying.utity;

import java.util.HashMap;

import com.ivyinfo.feiying.http.HttpUtils;

/**
 * utility for some common operations of video
 * @author sk
 *
 */
public class VideoCommonOpUtil {
	public static void recordPlayCount(String videoSourceId, String recordURL) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("sourceId", videoSourceId);
		HttpUtils.startHttpPostRequest(recordURL, params, null, null);
	}
}
