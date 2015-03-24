package magic.yuyong.extend;

import magic.yuyong.app.AppConstant;
import android.content.Context;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.openapi.AbsOpenAPI;

public class UnReadAPI extends AbsOpenAPI {
	private static final String SERVER_URL_PRIX = "https://api.weibo.com/2";
	
	public static final int TYPE_FOLLOWER = 0;
	public static final int TYPE_CMT = 1;
	public static final int TYPE_MENTION_STATUS = 2;
	public static final int TYPE_MENTION_CMT = 3;

	
	public UnReadAPI(Context context, String appKey,
			Oauth2AccessToken accessToken) {
		super(context, appKey, accessToken);
	}

	public void unRead(RequestListener listener) {
		WeiboParameters params = new WeiboParameters(AppConstant.CONSUMER_KEY);
		requestAsync( SERVER_URL_PRIX + "/remind/unread_count.json", params, HTTPMETHOD_GET, listener);
	}
	
	public void clear(RequestListener listener, int type_id) {
		String type = null;
		switch (type_id) {
		case TYPE_FOLLOWER:
			type = "follower";
			break;
		case TYPE_CMT:
			type = "cmt";
			break;
		case TYPE_MENTION_STATUS:
			type = "mention_status";
			break;
		case TYPE_MENTION_CMT:
			type = "mention_cmt";
			break;
		}
		WeiboParameters params = new WeiboParameters(AppConstant.CONSUMER_KEY);
		params.put("type", type);
		requestAsync( SERVER_URL_PRIX + "/remind/set_count.json", params, HTTPMETHOD_GET, listener);
	}
}
