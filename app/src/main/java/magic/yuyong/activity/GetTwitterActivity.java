package magic.yuyong.activity;

import org.json.JSONObject;

import magic.yuyong.R;
import magic.yuyong.app.AppConstant;
import magic.yuyong.app.MagicApplication;
import magic.yuyong.request.RequestState;
import magic.yuyong.util.Debug;
import magic.yuyong.util.JsonUtil;
import android.os.Handler;
import android.widget.Toast;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;

public abstract class GetTwitterActivity extends BaseActivity {

	protected class TwitterRequestListener implements RequestListener {
		private RequestState requestState;

		public TwitterRequestListener(RequestState requestState) {
			super();
			this.requestState = requestState;
		}

		@Override
		public void onComplete(String response) {
			requestState.response = response;
			onRequestComplete(requestState);
			
			android.os.Message msg = new android.os.Message();
			msg.what = AppConstant.MSG_UPDATE_VIEW;
			msg.obj = requestState;
			handler.sendMessage(msg);
		}
		
		@Override
		public void onWeiboException(WeiboException e) {
			requestState.response = e.getMessage();
			android.os.Message msg = new android.os.Message();
			msg.what = AppConstant.MSG_NETWORK_EXCEPTION;
			msg.obj = requestState;
			handler.sendMessage(msg);
			Debug.e(e.getMessage());
		}

	}

	protected Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			RequestState requestState = (RequestState) msg.obj;
			switch (msg.what) {
			case AppConstant.MSG_UPDATE_VIEW:
				onUpdate(requestState);
				break;
			case AppConstant.MSG_NETWORK_EXCEPTION:
				try {
					JSONObject jsonObj = new JSONObject(requestState.response);
					int error_code = JsonUtil.getInt(jsonObj, "error_code");
					if(error_code == 21332){
						//invalid_access_token
						MagicApplication.getInstance().exit(GetTwitterActivity.this);
					}
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.text_network_exception),
							Toast.LENGTH_SHORT).show();
				}
				onError(requestState);
				break;
			}

			requestState.isRefresh = false;
			requestState.isRequest = false;
			requestState.response = null;
		}
	};

	protected abstract void onUpdate(RequestState requestState);
	
	protected abstract void onRequestComplete(RequestState requestState);

	protected abstract void onError(RequestState requestState);
}
