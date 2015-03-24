package magic.yuyong.activity;

import java.util.ArrayList;
import java.util.List;

import magic.yuyong.R;
import magic.yuyong.adapter.CommentOrRepostAdapter;
import magic.yuyong.app.AppConstant;
import magic.yuyong.app.MagicApplication;
import magic.yuyong.app.MagicDialog;
import magic.yuyong.model.Comment;
import magic.yuyong.model.Repost;
import magic.yuyong.model.Twitter;
import magic.yuyong.persistence.Persistence;
import magic.yuyong.request.RequestState;
import magic.yuyong.transformation.CircleTransformation;
import magic.yuyong.transformation.MaxHeightTransformation;
import magic.yuyong.util.StringUtil;
import magic.yuyong.view.TwitterContent;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.legacy.FavoritesAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.squareup.picasso.Picasso;

public class TwitterShowActivity extends BaseActivity implements
		OnClickListener, OnRefreshListener {

	private PullToRefreshLayout mPullToRefreshLayout;
	private View header, footer;
	private ListView listView;
	private TextView comment_but, repost_but;

	private ImageView twitter_user_avatar;
	private TextView twitter_user_name;
	private TextView twitter_time;
	private TextView twitter_from;
	private TwitterContent twitter_text;
	private ImageView twitter_img;

	private View origin_layout;
	private ImageView origin_user_avatar;
	private TextView origin_user_name;
	private TextView origin_time;
	private TextView origin_from;
	private TwitterContent origin_text;
	private ImageView origin_img;

	private CommentOrRepostAdapter adapter;
	private List<Object> comments = new ArrayList<Object>();
	private List<Object> reposts = new ArrayList<Object>();
	private RequestState commentState, repostState;

	private int currentType = CommentOrRepostAdapter.TYPE_INVALID;

	private Twitter twitter;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			setProgressBarIndeterminateVisibility(false);

			switch (msg.what) {
			case AppConstant.MSG_SHOW_COMMENTS:
				List<Comment> commentList = Comment
						.parseComment(commentState.response);
				if (commentState.isRefresh) {
					comments.clear();
				}
				if (commentList.size() > 0) {
					if (commentState.maxId == 0) {
						long max_id = commentList.get(0).getId();
						commentState.maxId = max_id;
					}
					comments.addAll(commentList);
				} else {
					commentState.isBottom = true;
				}
				if (currentType == commentState.requestType) {
					adapter.notifyDataSetChanged();
				}
				commentState.page++;
				commentState.isRequest = false;
				commentState.isRefresh = false;
				commentState.response = "";
				footer.setVisibility(View.INVISIBLE);
				mPullToRefreshLayout.setRefreshComplete();
				break;
			case AppConstant.MSG_SHOW_REPOSTS:
				List<Repost> repostsList = Repost
						.parseRepost(repostState.response);
				if (repostState.isRefresh) {
					reposts.clear();
				}
				if (repostsList.size() > 0) {
					if (repostState.maxId == 0) {
						long max_id = repostsList.get(0).getId();
						repostState.maxId = max_id;
					}
					reposts.addAll(repostsList);
				} else {
					repostState.isBottom = true;
				}
				if (currentType == repostState.requestType) {
					adapter.notifyDataSetChanged();
				}
				repostState.page++;
				repostState.isRequest = false;
				repostState.isRefresh = false;
				repostState.response = "";
				footer.setVisibility(View.INVISIBLE);
				mPullToRefreshLayout.setRefreshComplete();
				break;
			case AppConstant.MSG_FAVORITE_SUCCEED:
				twitter.setFavorited(true);
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.text_fav_succeed),
						Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;
			case AppConstant.MSG_FAVORITE_CANCEL_SUCCEED:
				twitter.setFavorited(false);
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.text_fav_cancel_succeed),
						Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;
			case AppConstant.MSG_DELETE_SUCCEED:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.text_del_twitter_success),
						Toast.LENGTH_SHORT).show();
				finish();
				break;
			case AppConstant.MSG_DELETE_FAILD:
				Toast.makeText(
						getApplicationContext(),
						getResources()
								.getString(R.string.text_del_twitter_fail),
						Toast.LENGTH_SHORT).show();
				break;
			case AppConstant.MSG_NETWORK_EXCEPTION:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.text_network_exception),
						Toast.LENGTH_SHORT).show();
			case AppConstant.MSG_TOKEN_INVALID:
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.text_token_invalid),
						Toast.LENGTH_SHORT).show();
				MagicApplication.getInstance().exit(TwitterShowActivity.this);
				break;
			case AppConstant.MSG_SHOW_TWITTER:
				try {
					twitter = Twitter.parseTwitter(new JSONObject(msg.obj
							.toString()));
					invalidateOptionsMenu();
					initInfo();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}

		}

	};

	private void changeType(int type) {
		currentType = type;
		switch (currentType) {
		case CommentOrRepostAdapter.TYPE_COMMENTS:
			comment_but.setTextColor(getResources().getColor(
					R.color.theme_color));
			repost_but.setTextColor(Color.parseColor("#FFCCCCCC"));
			if (adapter.getType() != currentType) {
				adapter.setType(currentType);
				adapter.setData(comments);
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView,
							View view, int i, long l) {
						Object object = adapterView.getAdapter().getItem(i);
						if (object instanceof Comment) {
							Comment comment = (Comment) object;
							final Long cid = comment.getId();
							final String cu = comment.getUser()
									.getScreen_name();
							final String cc = comment.getText();
							Intent commentIntent = new Intent(
									getApplicationContext(),
									NewPostActivity.class);
							commentIntent.putExtra("twitter_id",
									twitter.getId());
							commentIntent.putExtra("cid", cid);
							commentIntent.putExtra("cu", cu);
							commentIntent.putExtra("cc", cc);
							commentIntent.putExtra("type",
									AppConstant.TYPE_REPLY_COMMENT);

							startActivityForResult(commentIntent,
									AppConstant.REQUESTCODE_COMMENT);
						}
					}
				});
			}
			if (comments.size() == 0) {
				getData(false, commentState);
			}
			break;
		case CommentOrRepostAdapter.TYPE_REPOSTS:
			repost_but.setTextColor(getResources()
					.getColor(R.color.theme_color));
			comment_but.setTextColor(Color.parseColor("#FFCCCCCC"));
			if (adapter.getType() != currentType) {
				adapter.setType(currentType);
				adapter.setData(reposts);
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView,
							View view, int i, long l) {
						Object object = adapterView.getAdapter().getItem(i);
						if (object instanceof Repost) {
							Repost repost = (Repost) object;
							Intent intent = new Intent(getApplicationContext(),
									TwitterShowActivity.class);
							intent.putExtra("twitter_id", repost.getId());
							startActivity(intent);
						}
					}
				});
			}
			if (reposts.size() == 0) {
				getData(false, repostState);
			}
			break;
		}
	}

	private void initView() {
		setContentView(R.layout.twitter_show);
		mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);

		// init list
		listView = (ListView) findViewById(R.id.list_view);
		header = getLayoutInflater().inflate(R.layout.twitter_show_head, null);
		header.setVisibility(View.INVISIBLE);
		listView.addHeaderView(header);
		footer = getLayoutInflater().inflate(R.layout.loadmorelayout, null);
		footer.setVisibility(View.INVISIBLE);
		listView.addFooterView(footer);
		adapter = new CommentOrRepostAdapter(this);
		listView.setAdapter(adapter);
		setListScrollListener(listView);

		// init button
		comment_but = (TextView) header.findViewById(R.id.comment_but);
		comment_but.setOnClickListener(this);
		repost_but = (TextView) header.findViewById(R.id.repost_but);
		repost_but.setOnClickListener(this);

		// init Twitter Layout
		twitter_user_avatar = (ImageView) header
				.findViewById(R.id.twitter_user_avatar);
		twitter_user_avatar.setImageResource(R.drawable.avatar);
		twitter_user_name = (TextView) header
				.findViewById(R.id.twitter_user_name);
		twitter_time = (TextView) header.findViewById(R.id.twitter_time);
		twitter_from = (TextView) header.findViewById(R.id.twitter_from);
		twitter_text = (TwitterContent) header.findViewById(R.id.twitter_text);
		twitter_img = (ImageView) header.findViewById(R.id.twitter_img);

		// init Origin Twitter Layout
		origin_layout = header.findViewById(R.id.origin_layout);
		origin_user_avatar = (ImageView) header
				.findViewById(R.id.origin_user_avatar);
		origin_user_avatar.setImageResource(R.drawable.avatar);
		origin_user_name = (TextView) header
				.findViewById(R.id.origin_user_name);
		origin_time = (TextView) header.findViewById(R.id.origin_time);
		origin_from = (TextView) header.findViewById(R.id.origin_from);
		origin_text = (TwitterContent) header.findViewById(R.id.origin_text);
		origin_img = (ImageView) header.findViewById(R.id.origin_img);

		ActionBarPullToRefresh.from(this)
				.theseChildrenArePullable(R.id.list_view, android.R.id.empty)
				.listener(this).setup(mPullToRefreshLayout);
	}

	private void setListScrollListener(ListView listView) {
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						WrapperListAdapter wrapperListAdapter = (WrapperListAdapter) view
								.getAdapter();
						Adapter adapter = wrapperListAdapter
								.getWrappedAdapter();
						RequestState requestState = currentType == CommentOrRepostAdapter.TYPE_COMMENTS ? commentState
								: repostState;
						getData(false, requestState);
					}

				}
			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);
		actionBar.setDisplayHomeAsUpEnabled(true);

		initView();

		commentState = new RequestState(CommentOrRepostAdapter.TYPE_COMMENTS);
		repostState = new RequestState(CommentOrRepostAdapter.TYPE_REPOSTS);

		twitter = getIntent().getParcelableExtra("twitter");
		if (twitter != null) {
			initInfo();
		} else {
			Long twitter_id = getIntent().getLongExtra("twitter_id", -1);
			if (twitter_id != -1) {
				getTwitter(twitter_id);
			}
		}
	}

	private void getTwitter(long twitter_id) {
		setProgressBarIndeterminateVisibility(true);
		StatusesAPI statusesAPI = new StatusesAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
		statusesAPI.show(twitter_id, new RequestListener() {

			@Override
			public void onWeiboException(WeiboException arg0) {
				mHandler.sendEmptyMessage(AppConstant.MSG_NETWORK_EXCEPTION);
			}

			@Override
			public void onComplete(String response) {
				android.os.Message msg = new android.os.Message();
				msg.what = AppConstant.MSG_SHOW_TWITTER;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}

	private void initInfo() {
		header.setVisibility(View.VISIBLE);
		twitter_text.setData(twitter.getText());
		twitter_text.setAction(true);
		if (!twitter.isDeleted()) {
			twitter_user_name.setText(twitter.getUser().getScreen_name());
			twitter_time.setText(twitter.getCreated_at());
			twitter_from.setText(getResources().getString(R.string.text_source)
					+ twitter.getSource());
			String avatar_url = twitter.getUser().getProfile_image_url();
			Picasso.with(getApplicationContext()).load(avatar_url)
					.placeholder(R.drawable.avatar)
					.transform(new CircleTransformation())
					.into(twitter_user_avatar);

			long uid = twitter.getUser().getId();
			setImageViewOnClickListener(twitter_user_avatar, uid);
			String bmiddle_pic_url = twitter.getBmiddle_pic();
			if (!StringUtil.isEmpty(bmiddle_pic_url)) {
				twitter_img.setVisibility(View.VISIBLE);
				Picasso.with(getApplicationContext()).load(bmiddle_pic_url)
						.transform(new MaxHeightTransformation())
						.into(twitter_img);
				setImgOnClickListener(twitter_img, twitter,
						twitter.getPic_urls());
			}
		}
		if (twitter.getOrigin() != null) {
			origin_layout.setVisibility(View.VISIBLE);
			origin_layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(getApplicationContext(),
							TwitterShowActivity.class);
					intent.putExtra("twitter", twitter.getOrigin());
					startActivity(intent);
				}
			});
			Twitter origin = twitter.getOrigin();
			origin_text.setData(origin.getText());
			origin_text.setAction(true);
			if (!origin.isDeleted()) {
				origin_user_name.setText(origin.getUser().getScreen_name());
				origin_time.setText(origin.getCreated_at());
				origin_from.setText(getResources().getString(
						R.string.text_source)
						+ origin.getSource());
				String avatar_url = origin.getUser().getProfile_image_url();
				Picasso.with(getApplicationContext()).load(avatar_url)
						.placeholder(R.drawable.avatar)
						.transform(new CircleTransformation())
						.into(origin_user_avatar);
				long origin_uid = origin.getUser().getId();
				setImageViewOnClickListener(origin_user_avatar, origin_uid);
				String origin_bmiddle_pic_url = origin.getBmiddle_pic();
				if (!StringUtil.isEmpty(origin_bmiddle_pic_url)) {
					origin_img.setVisibility(View.VISIBLE);
					Picasso.with(getApplicationContext())
							.load(origin_bmiddle_pic_url)
							.transform(new MaxHeightTransformation())
							.into(origin_img);
					setImgOnClickListener(origin_img, origin,
							origin.getPic_urls());
				}
			}
		}
		comment_but.setText(getResources().getText(R.string.but_comment) + " "
				+ twitter.getComments_count());
		repost_but.setText(getResources().getText(R.string.but_repost) + " "
				+ twitter.getReposts_count());

		changeType(CommentOrRepostAdapter.TYPE_COMMENTS);
	}

	private void setImageViewOnClickListener(View avatar, final long uid) {
		avatar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						ProfileActivity.class);
				intent.putExtra("uid", uid);
				startActivity(intent);
			}
		});
	}

	private void setImgOnClickListener(final ImageView imageView,
			final Twitter t, final String[] pics) {
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = t.getBmiddle_pic();
				String originalUrl = t.getOriginal_pic();
				Intent showPic = new Intent(getApplicationContext(),
						ShowPics.class);
				showPic.putExtra("url", url);
				showPic.putExtra("originalUrl", originalUrl);
				showPic.putExtra("pics", pics);
				startActivity(showPic);
			}
		});
	}

	private void getData(boolean refresh, final RequestState requestState) {
		if (!requestState.isRequest) {
			if (refresh || !requestState.isBottom) {
				requestState.isRequest = true;
				requestState.isRefresh = refresh;
				if (refresh) {
					requestState.isBottom = false;
					requestState.maxId = 0;
					requestState.page = 1;
				} else {
					footer.setVisibility(View.VISIBLE);
				}

				if (requestState.requestType == CommentOrRepostAdapter.TYPE_COMMENTS) {
					CommentsAPI commentAPI = new CommentsAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
					commentAPI.show(twitter.getId(), 0, requestState.maxId, 15,
							requestState.page, StatusesAPI.AUTHOR_FILTER_ALL,
							new RequestListener() {

								@Override
								public void onWeiboException(WeiboException arg0) {
									mHandler.sendEmptyMessage(AppConstant.MSG_NETWORK_EXCEPTION);
								}

								@Override
								public void onComplete(String response) {
									requestState.response = response;
									android.os.Message msg = new android.os.Message();
									msg.what = AppConstant.MSG_SHOW_COMMENTS;
									msg.obj = requestState;
									mHandler.sendMessage(msg);
								}

							});
				} else if (requestState.requestType == CommentOrRepostAdapter.TYPE_REPOSTS) {
					StatusesAPI statusesAPI = new StatusesAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
					statusesAPI.repostTimeline(twitter.getId(), 0,
							requestState.maxId, 15, requestState.page,
							StatusesAPI.AUTHOR_FILTER_ALL,
							new RequestListener() {
								@Override
								public void onComplete(String response) {
									requestState.response = response;
									android.os.Message msg = new android.os.Message();
									msg.what = AppConstant.MSG_SHOW_REPOSTS;
									msg.obj = requestState;
									mHandler.sendMessage(msg);
								}

								@Override
								public void onWeiboException(WeiboException arg0) {
									mHandler.sendEmptyMessage(AppConstant.MSG_NETWORK_EXCEPTION);
								}

							});
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			boolean needShowRefreshProgress = (requestCode == AppConstant.REQUESTCODE_COMMENT && currentType == commentState.requestType)
					|| (requestCode == AppConstant.REQUESTCODE_FORWARD && currentType == repostState.requestType);
			if (needShowRefreshProgress) {
				mPullToRefreshLayout.setRefreshing(true);
				listView.post(new Runnable() {

					@Override
					public void run() {
						if (adapter.getCount() != 0) {
							listView.setSelectionFromTop(1, header
									.findViewById(R.id.buttons_lay).getHeight());
						}
					}
				});
				refresh();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (twitter == null) {
			return true;
		}
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.twitter_show, menu);
		MenuItem favoriteItem = menu.findItem(R.id.favorite);
		MenuItem deleteItem = menu.findItem(R.id.delete);
		MenuItem shareItem = menu.findItem(R.id.share);

		favoriteItem
				.setIcon(twitter.isFavorited() ? R.drawable.rating_important
						: R.drawable.rating_not_important);

		if (Persistence.getUID(getApplicationContext()) != twitter.getUser()
				.getId()) {
			deleteItem.setVisible(false);
		}

		ShareActionProvider actionProvider = (ShareActionProvider) shareItem
				.getActionProvider();
		actionProvider
				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

		String text = twitter.getText();
		if (twitter.getOrigin() != null) {
			text = text + "//" + twitter.getOrigin().getText();
		}
		actionProvider.setShareIntent(createShareIntent(text));

		return super.onCreateOptionsMenu(menu);
	}

	private Intent createShareIntent(String text) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain"); // 纯文本
		shareIntent.putExtra(Intent.EXTRA_TEXT, text);
		return shareIntent;
	}

	private void refresh() {
		if (currentType == CommentOrRepostAdapter.TYPE_COMMENTS) {
			getData(true, commentState);
		} else if (currentType == CommentOrRepostAdapter.TYPE_REPOSTS) {
			getData(true, repostState);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.refresh:
			mPullToRefreshLayout.setRefreshing(true);
			refresh();
			if (adapter.getCount() != 0) {
				listView.setSelectionFromTop(1,
						header.findViewById(R.id.buttons_lay).getHeight());
			}
			break;
		case R.id.comment:
			Intent commentIntent = new Intent(getApplicationContext(),
					NewPostActivity.class);
			commentIntent.putExtra("type", AppConstant.TYPE_COMMENT);
			commentIntent.putExtra("twitter_id", twitter.getId());
			startActivityForResult(commentIntent,
					AppConstant.REQUESTCODE_COMMENT);

			break;
		case R.id.forward:
			Intent repostIntent = new Intent(getApplicationContext(),
					NewPostActivity.class);
			repostIntent.putExtra("type", AppConstant.TYPE_REPOST);
			repostIntent.putExtra("twitter_id", twitter.getId());
			if (twitter.getOrigin() != null) {
				repostIntent.putExtra("initText",
						"//@" + twitter.getUser().getScreen_name() + ":"
								+ twitter.getText());
			}
			startActivityForResult(repostIntent,
					AppConstant.REQUESTCODE_FORWARD);
			break;
		case R.id.favorite:
			favoriteTwitter(!twitter.isFavorited());
			break;
		case R.id.copy:
			ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			String text = twitter.getText();
			if (twitter.getOrigin() != null) {
				text = text + "//" + twitter.getOrigin().getText();
			}
			cmb.setText(text);
			Toast.makeText(getApplicationContext(), R.string.text_copy_success,
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.delete:
			final MagicDialog del_dialog = new MagicDialog(this,
					R.style.magic_dialog);
			del_dialog.setMessage(
					getResources().getString(R.string.title_del_twitter),
					getResources().getString(R.string.text_del_twitter));
			del_dialog.addButton(R.string.but_ok, new OnClickListener() {

				@Override
				public void onClick(View v) {
					delTwitter(twitter.getId());
				}
			});
			del_dialog.addButton(R.string.but_cancel, new OnClickListener() {

				@Override
				public void onClick(View v) {
					del_dialog.dismiss();
				}
			});
			del_dialog.show();

			break;
		}
		return true;
	}

	private void delTwitter(long id) {
		setProgressBarIndeterminateVisibility(true);
		StatusesAPI statusesAPI = new StatusesAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
		statusesAPI.destroy(id, new RequestListener() {

			@Override
			public void onWeiboException(WeiboException arg0) {
				mHandler.sendEmptyMessage( AppConstant.MSG_DELETE_FAILD);
			}

			@Override
			public void onComplete(String arg0) {
				mHandler.sendEmptyMessage(AppConstant.MSG_DELETE_SUCCEED);
			}

		});
	}

	private void favoriteTwitter(final boolean favorite) {
		setProgressBarIndeterminateVisibility(true);
		FavoritesAPI favoritesAPI = new FavoritesAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
		long id = twitter.getId();
		RequestListener listener = new RequestListener() {
			
			@Override
			public void onWeiboException(WeiboException arg0) {
				android.os.Message msg = new android.os.Message();
				msg.what = favorite ? AppConstant.MSG_FAVORITE_SUCCEED
						: AppConstant.MSG_FAVORITE_CANCEL_SUCCEED;
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				android.os.Message msg = new android.os.Message();
				msg.what = favorite ? AppConstant.MSG_FAVORITE_SUCCEED
						: AppConstant.MSG_FAVORITE_CANCEL_SUCCEED;
				mHandler.sendMessage(msg);
			}
		};

		if (favorite) {
			favoritesAPI.create(id, listener);
		} else {
			favoritesAPI.destroy(id, listener);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.comment_but:
			if (currentType != CommentOrRepostAdapter.TYPE_COMMENTS) {
				changeType(CommentOrRepostAdapter.TYPE_COMMENTS);
			}
			break;
		case R.id.repost_but:
			if (currentType != CommentOrRepostAdapter.TYPE_REPOSTS) {
				changeType(CommentOrRepostAdapter.TYPE_REPOSTS);
			}
			break;

		}
	}

	@Override
	public void onRefreshStarted(View view) {
		refresh();
	}
}
