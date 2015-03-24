package magic.yuyong.extend;

import magic.yuyong.app.AppConstant;
import android.content.Context;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;

public class StatusesAPI_E extends StatusesAPI {
	
	 private static final String SERVER_URL_PRIX = API_SERVER + "/statuses";

	public StatusesAPI_E(Context context, String appKey,
			Oauth2AccessToken accessToken) {
		super(context, appKey, accessToken);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param uids 
	 * @param count
	 * @param page
	 * @param base_app
	 * @param feature
	 * @param listener
	 */
	public void timelineBatch(String uids, int count,
			int page, boolean base_app, int feature,
			RequestListener listener) {
		
		WeiboParameters params = new WeiboParameters(AppConstant.CONSUMER_KEY);
		params.put("uids", uids);
		params.put("count", count);
		params.put("page", page);
		if (base_app) {
			params.put("base_app", 1);
		} else {
			params.put("base_app", 0);
		}
		params.put("feature", feature);
		requestAsync(SERVER_URL_PRIX + "/timeline_batch.json", params,
				HTTPMETHOD_GET, listener);
	}

}
