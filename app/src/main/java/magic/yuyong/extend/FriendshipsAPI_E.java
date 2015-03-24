package magic.yuyong.extend;

import magic.yuyong.app.AppConstant;
import android.content.Context;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.openapi.legacy.FriendshipsAPI;

public class FriendshipsAPI_E extends FriendshipsAPI {
	private static final String SERVER_URL_PRIX = API_SERVER + "/friendships";

	public FriendshipsAPI_E(Context context, String appKey,
			Oauth2AccessToken accessToken) {
		super(context, appKey, accessToken);
	}

	public void group(RequestListener listener) {
		WeiboParameters params = new WeiboParameters(AppConstant.CONSUMER_KEY);
		requestAsync( SERVER_URL_PRIX + "/groups.json", params, HTTPMETHOD_GET, listener);
	}

	public void groupTimeline(long list_id, long since_id, long max_id_group,
			int count, int page, boolean base_app, int feature, RequestListener listener) {
		WeiboParameters params = new WeiboParameters(AppConstant.CONSUMER_KEY);
		params.put("list_id", list_id);
		params.put("since_id", since_id);
		params.put("max_id", max_id_group);
		params.put("count", count);
		params.put("page", page);
		params.put("base_app", base_app ? 1 : 0);
		params.put("feature", feature);
		requestAsync( SERVER_URL_PRIX + "/groups/timeline.json", params, HTTPMETHOD_GET, listener);
	}
	
}
