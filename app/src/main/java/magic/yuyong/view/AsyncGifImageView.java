/**
 * 
 */
package magic.yuyong.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Future;

import magic.yuyong.util.Cache;
import magic.yuyong.util.MagicExecutorService;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * @title:AsyncGifImageView.java
 * @description:
 * @company: 美丽说（北京）网络科技有限公司
 * @author wanyuyong
 * @version
 * @created Oct 22, 2013
 */
public class AsyncGifImageView extends GifImageView {

	public AsyncGifImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AsyncGifImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AsyncGifImageView(Context context) {
		super(context);
	}

	public static interface ImageLoadingCallback {

		void onImageRequestStarted();

		void onImageRequestFailed();

		void onImageRequestEnded(String path);

		void onImageRequestCancelled();

		void onImageRequestLoading(float percent);
	}

	private static final int ON_START = 0x100;
	private static final int ON_FAIL = 0x101;
	private static final int ON_END = 0x102;
	private static final int ON_LOADING = 0x103;

	private String mUrl;

	private Future<?> mFuture;

	private ImageLoadingCallback mCallBack;

	public ImageLoadingCallback getImageLoadingCallback() {
		return mCallBack;
	}

	public void setImageLoadingCallback(ImageLoadingCallback mCallBack) {
		this.mCallBack = mCallBack;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case ON_START:
				if (mCallBack != null) {
					mCallBack.onImageRequestStarted();
				}
				break;

			case ON_FAIL:
				if (mCallBack != null) {
					mCallBack.onImageRequestFailed();
				}
				break;

			case ON_END:
				String path = (String) msg.obj;
				try {
					setImageDrawable(new GifDrawable(path));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (mCallBack != null) {
					mCallBack.onImageRequestEnded(path);
				}
				break;

			case ON_LOADING:
				float percent = (Float) msg.obj;
				if (mCallBack != null) {
					mCallBack.onImageRequestLoading(percent);
				}
				break;

			default:
				super.handleMessage(msg);
				break;
			}
		}
	};

	public void cancel() {
		if (mFuture != null && !mFuture.isCancelled()) {
			mFuture.cancel(false);
			if (mCallBack != null) {
				mCallBack.onImageRequestCancelled();
			}
		}
	}

	public void setUrl(String url) {

		if (url != null && url.equals(mUrl)) {
			return;
		}

		cancel();

		setImageDrawable(null);

		mUrl = url;

		if (!TextUtils.isEmpty(mUrl)) {
			mFuture = MagicExecutorService.getExecutor().submit(
					new ImageFetcher());
		}
	}

	private class ImageFetcher implements Runnable {

		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			String path = "";
			mHandler.sendMessage(Message.obtain(mHandler, ON_START));
			String cacheFilePath = Cache.getPicassoCacheFilePath(mUrl,
					getContext());
			File cacheFile = new File(cacheFilePath);
			if (cacheFile.exists()) {
				path = cacheFilePath;
			} else if (!TextUtils.isEmpty(mUrl)) {
				path = downLoadPic(mUrl, mHandler);
			}
			if (TextUtils.isEmpty(path)) {
				mHandler.sendMessage(Message.obtain(mHandler, ON_FAIL));
				mUrl = null;
			} else {
				mHandler.sendMessage(Message.obtain(mHandler, ON_END, path));
			}
		}
	}

	private String downLoadPic(String mUrl, Handler h) {
		InputStream inputStream = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(mUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setReadTimeout(10 * 1000);
			inputStream = conn.getInputStream();
			float size = conn.getContentLength();
			byte[] buffer = new byte[1024];
			int len = 0;
			int sum = 0;
			while ((len = inputStream.read(buffer)) != -1) {
				sum += len;
				Message msg = Message.obtain(h, ON_LOADING);
				msg.obj = sum / size;
				h.sendMessage(msg);
			}
			String path = Cache.getPicassoCacheFilePath(mUrl, getContext());
			return path;
		} catch (IOException e) {

		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != conn) {
				conn.disconnect();
			}
		}
		return null;
	}

}
