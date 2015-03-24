package magic.yuyong.activity;

import java.util.ArrayList;
import java.util.List;

import magic.yuyong.R;
import magic.yuyong.adapter.CommentMeAdapter;
import magic.yuyong.adapter.MyPagerAdapter;
import magic.yuyong.adapter.TwitterListAdapter;
import magic.yuyong.app.AppConstant;
import magic.yuyong.app.MagicApplication;
import magic.yuyong.extend.FriendshipsAPI_E;
import magic.yuyong.extend.UnReadAPI;
import magic.yuyong.model.Comment;
import magic.yuyong.model.Group;
import magic.yuyong.model.Twitter;
import magic.yuyong.persistence.Persistence;
import magic.yuyong.request.RequestState;
import magic.yuyong.util.Debug;
import magic.yuyong.util.SystemUtil;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.legacy.FavoritesAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.umeng.update.UmengUpdateAgent;

public class TimeLineModeActivity extends GetTwitterActivity implements
		OnRefreshListener {

	private ViewPager mPager;
	private TextView alertText;
	private List<View> listViews;
	private List<Group> groups = new ArrayList<Group>();
	private boolean gettingGroups;

	private LayoutInflater mInflater;

	public static final int VIEW_BILATERAL = 0;
	public static final int VIEW_HOME = 1;
	public static final int VIEW_AT_ME = 2;
	public static final int VIEW_COMMENT = 3;
	private MyRequestState current;

	private boolean isHomeRequestAll = true;
	private long groupId;
	private int bilateralFeature = StatusesAPI.FEATURE_ALL;
	private int atMeFilter = StatusesAPI.AUTHOR_FILTER_ALL;
	private int commentFilter = StatusesAPI.AUTHOR_FILTER_ALL;

	private class MyRequestState extends RequestState {

		public MyRequestState(int requestType) {
			super(requestType);
		}

		boolean dataNeedSave;
		boolean localDataDirty;
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AppConstant.MSG_UPDATA_GROUP:
				if (current.requestType == VIEW_HOME) {
					invalidateOptionsMenu();
				}
				break;
			case AppConstant.MSG_FAVORITE_SUCCEED:
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.text_fav_succeed),
						Toast.LENGTH_SHORT).show();
				break;
			case AppConstant.MSG_FAVORITE_CANCEL_SUCCEED:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.text_fav_cancel_succeed),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private void getGroup() {
		gettingGroups = true;
		FriendshipsAPI_E friendshipsApi = new FriendshipsAPI_E(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
		friendshipsApi.group(new RequestListener() {

			@Override
			public void onWeiboException(WeiboException arg0) {
				gettingGroups = false;
			}

			@Override
			public void onComplete(String response) {
				gettingGroups = false;
				groups.addAll(Group.parseGroup(response));
				mHandler.sendEmptyMessage(AppConstant.MSG_UPDATA_GROUP);
			}
		});
	}

	private void requestForTwitters(final MyRequestState requestState) {
		new Thread(new Runnable() {

			@Override
			public void run() {

				if (requestState.isFirstTime && !requestState.localDataDirty) {
					String data = getPersistenceData(requestState);
					if (!TextUtils.isEmpty(data)) {
						requestState.response = data;
						Message msg = handler
								.obtainMessage(AppConstant.MSG_UPDATE_VIEW);
						msg.obj = requestState;
						handler.sendMessageDelayed(msg, 300);
						return;
					}
				}

				if (requestState.localDataDirty) {
					requestState.localDataDirty = false;
					cleanPersistenceData(requestState);
				}

				switch (requestState.requestType) {
				case VIEW_BILATERAL:
					if (bilateralFeature == StatusesAPI.FEATURE_ALL
							&& requestState.isRefresh) {
						requestState.dataNeedSave = true;
					}

					StatusesAPI bilateralAPI = new StatusesAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
					bilateralAPI.bilateralTimeline(0, requestState.maxId,
							AppConstant.PAGE_NUM, requestState.page, false,
							bilateralFeature, false,
							new TwitterRequestListener(requestState));
					break;

				case VIEW_AT_ME:
					if (atMeFilter == StatusesAPI.AUTHOR_FILTER_ALL
							&& requestState.isRefresh) {
						requestState.dataNeedSave = true;
					}

					StatusesAPI atMeAPI = new StatusesAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
					atMeAPI.mentions(0, requestState.maxId,
							AppConstant.PAGE_NUM, requestState.page,
							atMeFilter, StatusesAPI.SRC_FILTER_ALL,
							StatusesAPI.TYPE_FILTER_ALL, false,
							new TwitterRequestListener(requestState));
					break;

				case VIEW_HOME:
					if (isHomeRequestAll) {
						if (requestState.isRefresh) {
							requestState.dataNeedSave = true;
						}

						StatusesAPI homeAPI = new StatusesAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
						homeAPI.homeTimeline(0, requestState.maxId,
								AppConstant.PAGE_NUM, requestState.page, false,
								StatusesAPI.FEATURE_ALL, false,
								new TwitterRequestListener(requestState));
					} else {
						FriendshipsAPI_E friendshipsApi = new FriendshipsAPI_E(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
						friendshipsApi.groupTimeline(groupId, 0,
								requestState.maxId, AppConstant.PAGE_NUM,
								requestState.page, false, StatusesAPI.FEATURE_ALL,
								new TwitterRequestListener(requestState));
					}

					break;

				case VIEW_COMMENT:
					if (commentFilter == StatusesAPI.AUTHOR_FILTER_ALL
							&& requestState.isRefresh) {
						requestState.dataNeedSave = true;
					}

					CommentsAPI commentsAPI = new CommentsAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
					commentsAPI.toME(0, requestState.maxId,
							AppConstant.PAGE_NUM, requestState.page,
							commentFilter, StatusesAPI.SRC_FILTER_ALL,
							new TwitterRequestListener(requestState));
					break;
				}
			}
		}).start();
	}

	private void getTwitter(boolean refresh) {
		final MyRequestState requestState = current;
		if (!requestState.isRequest) {
			if (refresh || !requestState.isBottom) {
				requestState.isRequest = true;
				requestState.isRefresh = refresh;

				View tagView = listViews.get(requestState.requestType);
				ListView listView = (ListView) tagView
						.findViewById(R.id.list_view);
				View footView = listView.findViewById(R.id.load_more);

				if (refresh) {
					requestState.isBottom = false;
					requestState.maxId = 0;
					requestState.page = 1;
					SystemUtil.stopListViewFling(listView);
					listView.setSelectionAfterHeaderView();
				} else {
					footView.setVisibility(View.VISIBLE);
				}
				requestForTwitters(requestState);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.timeline, menu);
		MenuItem sort = menu.findItem(R.id.sort);
		SubMenu subMenu = null;
		if (sort != null) {
			subMenu = sort.getSubMenu();
		}
		if (current != null && subMenu != null) {
			switch (current.requestType) {
			case VIEW_BILATERAL:
				subMenu.addSubMenu(R.id.sort, StatusesAPI.FEATURE_ALL,
						StatusesAPI.FEATURE_ALL, R.string.but_all);
				subMenu.addSubMenu(R.id.sort, StatusesAPI.FEATURE_ORIGINAL,
						StatusesAPI.FEATURE_ORIGINAL, R.string.but_original);
				break;
			case VIEW_HOME:
				if (groups.size() != 0) {
					for (int i = 0; i < groups.size(); i++) {
						Group group = groups.get(i);

						subMenu.addSubMenu(R.id.sort, i, i, group.getName());

					}

					subMenu.addSubMenu(R.id.sort, R.id.menu_group_all,
							groups.size(),
							getResources().getString(R.string.but_all));
				}
				break;
			case VIEW_AT_ME:
			case VIEW_COMMENT:
				subMenu.addSubMenu(R.id.sort, StatusesAPI.AUTHOR_FILTER_ALL,
						StatusesAPI.AUTHOR_FILTER_ALL, R.string.but_all);
				subMenu.addSubMenu(R.id.sort,
						StatusesAPI.AUTHOR_FILTER_ATTENTIONS,
						StatusesAPI.AUTHOR_FILTER_ATTENTIONS,
						R.string.but_attentions);
				break;
			}
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.post:
			Intent postIntent = new Intent(getApplicationContext(),
					NewPostActivity.class);
			startActivity(postIntent);
			break;
		case R.id.refresh:
			PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) listViews
					.get(current.requestType);
			mPullToRefreshLayout.setRefreshing(true);
			break;
		case R.id.profile:
			Intent profileIntent = new Intent(getApplicationContext(),
					ProfileActivity.class);
			profileIntent.putExtra("uid",
					Persistence.getUID(getApplicationContext()));
			startActivity(profileIntent);
			break;
		case R.id.sort:
			if (current.requestType == VIEW_HOME && groups.size() == 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.text_loading),
						Toast.LENGTH_SHORT).show();
				if (!gettingGroups) {
					getGroup();
				}
			}
			break;
		}

		if (item.getGroupId() == R.id.sort) {
			PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) listViews
					.get(current.requestType);
			ListView listView = (ListView) mPullToRefreshLayout
					.findViewById(R.id.list_view);
			if (current.requestType == VIEW_COMMENT) {
				HeaderViewListAdapter headAdapter = (HeaderViewListAdapter) listView
						.getAdapter();
				CommentMeAdapter adapter = (CommentMeAdapter) headAdapter
						.getWrappedAdapter();
				adapter.getComments().clear();
				adapter.notifyDataSetChanged();
			} else {
				HeaderViewListAdapter headAdapter = (HeaderViewListAdapter) listView
						.getAdapter();
				TwitterListAdapter adapter = (TwitterListAdapter) headAdapter
						.getWrappedAdapter();
				adapter.getData().clear();
				adapter.notifyDataSetChanged();
			}
			switch (current.requestType) {
			case VIEW_BILATERAL:
				if (item.getItemId() == StatusesAPI.FEATURE_ALL) {
					bilateralFeature = StatusesAPI.FEATURE_ALL;
				} else if (item.getItemId() == StatusesAPI.FEATURE_ORIGINAL) {
					bilateralFeature = StatusesAPI.FEATURE_ORIGINAL;
				}
				break;
			case VIEW_HOME:
				int id = item.getItemId();
				if (id == R.id.menu_group_all) {
					isHomeRequestAll = true;
				} else {
					Group group = groups.get(id);
					groupId = group.getId();
					isHomeRequestAll = false;
				}
				break;
			case VIEW_AT_ME:
				if (item.getItemId() == StatusesAPI.AUTHOR_FILTER_ALL) {
					atMeFilter = StatusesAPI.AUTHOR_FILTER_ALL;
				} else if (item.getItemId() == StatusesAPI.AUTHOR_FILTER_ATTENTIONS) {
					atMeFilter = StatusesAPI.AUTHOR_FILTER_ATTENTIONS;
				}
				break;
			case VIEW_COMMENT:
				if (item.getItemId() == StatusesAPI.AUTHOR_FILTER_ALL) {
					commentFilter = StatusesAPI.AUTHOR_FILTER_ALL;
				} else if (item.getItemId() == StatusesAPI.AUTHOR_FILTER_ATTENTIONS) {
					commentFilter = StatusesAPI.AUTHOR_FILTER_ATTENTIONS;
				}
				break;
			}

			mPullToRefreshLayout.setRefreshing(true);
		}

		return super.onOptionsItemSelected(item);
	}

	private void checkUpdate() {
		boolean checkUpdate = getIntent()
				.getBooleanExtra("check_update", false);
		if (checkUpdate) {
			UmengUpdateAgent.update(this);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		checkUpdate();

		setContentView(R.layout.timeline_mode);
		mInflater = getLayoutInflater();
		initViewPager();
		alertText = (TextView)findViewById(R.id.alert);

		Tab tabBilateral = actionBar.newTab().setText(R.string.label_bilateral);
		Tab tabHome = actionBar.newTab().setText(R.string.label_home);
		Tab tabAtme = actionBar.newTab().setText(R.string.label_at_me);
		Tab tabComment = actionBar.newTab().setText(R.string.label_comment);
		tabBilateral.setTabListener(listener);
		tabHome.setTabListener(listener);
		tabAtme.setTabListener(listener);
		tabComment.setTabListener(listener);

		int pos = getIntent().getIntExtra("pos", VIEW_HOME);
		if (pos == VIEW_AT_ME
				&& Persistence.getMention_status(getApplicationContext()) != 0) {
			MyRequestState state = (MyRequestState) listViews.get(pos).getTag();
			state.localDataDirty = true;
			clearState(UnReadAPI.TYPE_MENTION_STATUS);
		} else if (pos == VIEW_COMMENT
				&& Persistence.getCmt(getApplicationContext()) != 0) {
			MyRequestState state = (MyRequestState) listViews.get(pos).getTag();
			state.localDataDirty = true;
			clearState(UnReadAPI.TYPE_CMT);
		}

		actionBar.addTab(tabBilateral, VIEW_BILATERAL, pos == VIEW_BILATERAL);
		actionBar.addTab(tabHome, VIEW_HOME, pos == VIEW_HOME);
		actionBar.addTab(tabAtme, VIEW_AT_ME, pos == VIEW_AT_ME);
		actionBar.addTab(tabComment, VIEW_COMMENT, pos == VIEW_COMMENT);

		getGroup();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		int pos = intent.getIntExtra("pos", VIEW_HOME);
		if (pos == VIEW_AT_ME) {
			clearState(UnReadAPI.TYPE_MENTION_STATUS);
		} else if (pos == VIEW_COMMENT) {
			clearState(UnReadAPI.TYPE_CMT);
		}

		if (current.requestType == pos) {
			PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) listViews
					.get(pos);
			mPullToRefreshLayout.setRefreshing(true);
		} else {
			current = (MyRequestState) listViews.get(pos).getTag();
			if (!current.isFirstTime) {
				PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) listViews
						.get(pos);
				mPullToRefreshLayout.setRefreshing(true);
			} else {
				current.localDataDirty = true;
			}
			mPager.setCurrentItem(pos);
		}
	}

	private void clearState(final int type) {
		UnReadAPI unReadAPI = new UnReadAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
		unReadAPI.clear(new RequestListener() {

			@Override
			public void onComplete(String arg0) {
				if (type == UnReadAPI.TYPE_CMT) {
					Persistence.setCmt(getApplicationContext(), 0);
				} else if (type == UnReadAPI.TYPE_MENTION_STATUS) {
					Persistence.setMention_status(getApplicationContext(), 0);
				}
				sendBroadcast(new Intent(
						AppConstant.ACTION_UNREAD_STATE_CHANGE_BROADCAST));
			}

			@Override
			public void onWeiboException(WeiboException arg0) {
				// TODO Auto-generated method stub
				
			}
		}, type);
	}

	private TabListener listener = new TabListener() {

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {

		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			int index = actionBar.getSelectedNavigationIndex();
			if (mPager.getCurrentItem() != index) {
				mPager.setCurrentItem(index);
			}
			invalidateOptionsMenu();
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {

		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void alert(String text){
		alertText.setVisibility(View.VISIBLE);
		alertText.setText(text);
		final Animation out = new AlphaAnimation(1, 0);
		out.setDuration(300);
		out.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				alertText.setVisibility(View.GONE);
			}
		});
		Animation in = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, -1, 
				Animation.RELATIVE_TO_SELF, 0);
		in.setDuration(300);
		in.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				alertText.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						alertText.startAnimation(out);
					}
				}, 2000);
			}
		});
		alertText.startAnimation(in);
	}

	private void alertNewTwitterNum(List<Twitter> older, List<Twitter> twitters) {
		int newTwitterNum = 0;
		for (Twitter t : twitters) {
			boolean has = false;
			for (int i = 0; i < older.size() && i < twitters.size(); i++) {
				Twitter twitter = older.get(i);
				if (twitter.getId().longValue() == t.getId().longValue()) {
					has = true;
				}
			}
			if (!has) {
				newTwitterNum++;
			}
		}
		if (newTwitterNum != 0) {
			alert(newTwitterNum+getResources().getString(R.string.text_alert_new_twitter));
		}else{
			alert(getResources().getString(R.string.text_alert_no_new_twitter));
		}
	}
	
	private void alertNewCommentNum(List<Comment> older, List<Comment> comments) {
		int newCommentNum = 0;
		for (Comment c : comments) {
			boolean has = false;
			for (int i = 0; i < older.size() && i < comments.size(); i++) {
				Comment comment = older.get(i);
				if (comment.getId().longValue() == c.getId().longValue()) {
					has = true;
				}
			}
			if (!has) {
				newCommentNum++;
			}
		}
		if (newCommentNum != 0) {
			alert(newCommentNum+getResources().getString(R.string.text_alert_new_comment));
		}else{
			alert(getResources().getString(R.string.text_alert_no_new_comment));
		}
	}

	@Override
	protected void onUpdate(RequestState requestState) {
		View tagView = listViews.get(requestState.requestType);
		ListView listView = (ListView) tagView.findViewById(R.id.list_view);
		View footView = listView.findViewById(R.id.load_more);
		if (requestState.requestType == VIEW_COMMENT) {
			HeaderViewListAdapter headAdapter = (HeaderViewListAdapter) listView
					.getAdapter();
			CommentMeAdapter adapter = (CommentMeAdapter) headAdapter
					.getWrappedAdapter();
			List<Comment> comments = Comment
					.parseComment(requestState.response);
			
			if (requestState.isRefresh) {
				alertNewCommentNum(adapter.getComments(), comments);
				adapter.getComments().clear();
			}
			
			if (comments.size() == 0) {
				requestState.isBottom = true;
			} else {
				adapter.getComments().addAll(comments);
				if (requestState.maxId == 0) {
					requestState.maxId = comments.get(0).getId();
				}
				requestState.page++;
			}
			adapter.notifyDataSetChanged();
		} else {
			HeaderViewListAdapter headAdapter = (HeaderViewListAdapter) listView
					.getAdapter();
			TwitterListAdapter adapter = (TwitterListAdapter) headAdapter
					.getWrappedAdapter();
			List<Twitter> datas = adapter.getData();
			List<Twitter> twitters = Twitter
					.parseTwitter(requestState.response);

			if (requestState.isRefresh) {
				alertNewTwitterNum(datas, twitters);
				datas.clear();
			}

			if (twitters.size() == 0) {
				requestState.isBottom = true;
			} else {
				while (twitters.size() != 0) {
					Twitter twitter = twitters.remove(0);
					if (!twitter.isDeleted()) {
						datas.add(twitter);
					}
				}
				if (requestState.maxId == 0) {
					requestState.maxId = datas.get(0).getId();
				}
				requestState.page++;
			}
			adapter.notifyDataSetChanged();
		}
		requestState.isFirstTime = false;
		footView.setVisibility(View.INVISIBLE);
		PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) tagView;
		mPullToRefreshLayout.setRefreshComplete();
	}

	@Override
	protected void onError(RequestState requestState) {
		View tagView = listViews.get(requestState.requestType);
		ListView listView = (ListView) tagView.findViewById(R.id.list_view);
		View footView = listView.findViewById(R.id.load_more);
		PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) tagView;
		mPullToRefreshLayout.setRefreshComplete();
		footView.setVisibility(View.INVISIBLE);
	}

	private void setListScrollListener(ListView listView,
			final MyRequestState MyRequestState) {
		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						getTwitter(false);
					}
				}
			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}

	private Intent createShareIntent(String text) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain"); // 纯文本
		shareIntent.putExtra(Intent.EXTRA_TEXT, text);
		return shareIntent;
	}

	private void prepareView(final View view, int view_type) {
		final ListView list_view = (ListView) view.findViewById(R.id.list_view);
		list_view.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				final Twitter twitter = (Twitter) list_view.getAdapter()
						.getItem(position);
				HeaderViewListAdapter headAdapter = (HeaderViewListAdapter) list_view
						.getAdapter();
				final TwitterListAdapter adapter = (TwitterListAdapter) headAdapter
						.getWrappedAdapter();
				adapter.setItemOnSelected(position);

				ActionMode.Callback callback = new ActionMode.Callback() {

					@Override
					public boolean onPrepareActionMode(ActionMode mode,
							Menu menu) {
						MenuItem actionItem = menu.findItem(R.id.favorite);
						if (twitter.isFavorited()) {
							actionItem.setIcon(R.drawable.rating_important);
						} else {
							actionItem.setIcon(R.drawable.rating_not_important);
						}
						return true;
					}

					@Override
					public void onDestroyActionMode(ActionMode mode) {
						adapter.setItemOnSelected(TwitterListAdapter.INVALID_POSTION);
					}

					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						MenuInflater inflater = mode.getMenuInflater();
						inflater.inflate(R.menu.action_mode_timeline, menu);

						MenuItem actionItem = menu.findItem(R.id.share);
						ShareActionProvider actionProvider = (ShareActionProvider) actionItem
								.getActionProvider();
						actionProvider
								.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

						String text = twitter.getText();
						if (twitter.getOrigin() != null) {
							text = text + "//" + twitter.getOrigin().getText();
						}
						actionProvider.setShareIntent(createShareIntent(text));
						return true;
					}

					@Override
					public boolean onActionItemClicked(ActionMode mode,
							MenuItem item) {
						Twitter twitter = (Twitter) list_view.getAdapter()
								.getItem(position);
						switch (item.getItemId()) {
						case R.id.copy:
							ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
							String text = twitter.getText();
							if (twitter.getOrigin() != null) {
								text = text + "//"
										+ twitter.getOrigin().getText();
							}
							cmb.setText(text);
							mode.finish();
							Toast.makeText(getApplicationContext(),
									R.string.text_copy_success,
									Toast.LENGTH_SHORT).show();
							break;
						case R.id.comment:
							Intent commentIntent = new Intent(
									getApplicationContext(),
									NewPostActivity.class);
							commentIntent.putExtra("type",
									AppConstant.TYPE_COMMENT);
							commentIntent.putExtra("twitter_id",
									twitter.getId());
							startActivity(commentIntent);
							mode.finish();
							break;
						case R.id.forward:
							Intent repostIntent = new Intent(
									getApplicationContext(),
									NewPostActivity.class);
							repostIntent.putExtra("type",
									AppConstant.TYPE_REPOST);
							repostIntent.putExtra("twitter_id", twitter.getId());
							if (twitter.getOrigin() != null) {
								repostIntent.putExtra("initText", "//@"
										+ twitter.getUser().getScreen_name()
										+ ":" + twitter.getText());
							}
							startActivity(repostIntent);
							mode.finish();
							break;
						case R.id.favorite:
							changeFavorite(!twitter.isFavorited(), twitter);
							mode.finish();
							break;
						}
						return false;
					}
				};
				startActionMode(callback);
				return true;
			}
		});

		setFootView(list_view);
		TwitterListAdapter adapter = new TwitterListAdapter(this);
		list_view.setAdapter(adapter);
		adapter.setData(new ArrayList<Twitter>());
		list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				TwitterListAdapter.ViewHolder holder = (TwitterListAdapter.ViewHolder) view
						.getTag();
				if (holder != null) {
					Twitter twitter = holder.twitter;
					if (twitter.getId() != 0) {
						Intent intent = new Intent(getApplicationContext(),
								TwitterShowActivity.class);
						intent.putExtra("twitter", twitter);
						startActivity(intent);
					}
				}
			}
		});

		PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) view
				.findViewById(R.id.ptr_layout);
		ActionBarPullToRefresh.from(this)
				.theseChildrenArePullable(R.id.list_view, android.R.id.empty)
				.listener(this).setup(mPullToRefreshLayout);

		MyRequestState requestState = new MyRequestState(view_type);
		view.setTag(requestState);
		setListScrollListener(list_view, requestState);
	}

	private void changeFavorite(final boolean favorite, final Twitter twitter) {
		FavoritesAPI favoritesAPI = new FavoritesAPI(getApplicationContext(), AppConstant.CONSUMER_KEY, MagicApplication.getInstance().getAccessToken());
		RequestListener listener = new RequestListener() {

			@Override
			public void onComplete(String response) {
				android.os.Message msg = new android.os.Message();
				msg.what = favorite ? AppConstant.MSG_FAVORITE_SUCCEED
						: AppConstant.MSG_FAVORITE_CANCEL_SUCCEED;
				mHandler.sendMessage(msg);
				twitter.setFavorited(favorite);
			}

			@Override
			public void onWeiboException(WeiboException arg0) {
				
			}
		};
		if (!favorite) {
			favoritesAPI.destroy(twitter.getId(), listener);
		} else {
			favoritesAPI.create(twitter.getId(), listener);
		}
	}

	private void prepareCommentView(final View view) {
		final ListView list_view = (ListView) view.findViewById(R.id.list_view);
		setFootView(list_view);
		CommentMeAdapter adapter = new CommentMeAdapter(this);
		list_view.setAdapter(adapter);
		adapter.addData(new ArrayList<Comment>());

		list_view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int i, long l) {
				final Comment comment = (Comment) list_view.getAdapter()
						.getItem(i);
				HeaderViewListAdapter headAdapter = (HeaderViewListAdapter) list_view
						.getAdapter();
				final CommentMeAdapter adapter = (CommentMeAdapter) headAdapter
						.getWrappedAdapter();
				adapter.setItemOnSelected(i);

				ActionMode.Callback callback = new ActionMode.Callback() {

					@Override
					public boolean onPrepareActionMode(ActionMode mode,
							Menu menu) {
						return true;
					}

					@Override
					public void onDestroyActionMode(ActionMode mode) {
						adapter.setItemOnSelected(TwitterListAdapter.INVALID_POSTION);
					}

					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						MenuInflater inflater = mode.getMenuInflater();
						inflater.inflate(R.menu.action_mode_timeline_comment,
								menu);
						return true;
					}

					@Override
					public boolean onActionItemClicked(ActionMode mode,
							MenuItem item) {
						switch (item.getItemId()) {
						case R.id.reply:
							Intent commentIntent = new Intent(
									getApplicationContext(),
									NewPostActivity.class);
							commentIntent.putExtra("twitter_id", comment
									.getTwitter().getId());
							commentIntent.putExtra("cid", comment.getId());
							commentIntent.putExtra("cu", comment.getUser()
									.getScreen_name());
							commentIntent.putExtra("cc", comment.getText());
							commentIntent.putExtra("type",
									AppConstant.TYPE_REPLY_COMMENT);
							startActivity(commentIntent);
							mode.finish();
							break;
						case R.id.show:
							Intent intent = new Intent(getApplicationContext(),
									TwitterShowActivity.class);
							Twitter twitter = comment.getTwitter();
							intent.putExtra("twitter_id", twitter.getId());
							startActivity(intent);
							mode.finish();
							break;
						case R.id.copy:
							ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
							String text = comment.getText();
							cmb.setText(text);
							mode.finish();
							Toast.makeText(getApplicationContext(),
									R.string.text_copy_success,
									Toast.LENGTH_SHORT).show();
							break;
						}
						return false;
					}
				};
				startActionMode(callback);
			}
		});

		PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) view
				.findViewById(R.id.ptr_layout);
		ActionBarPullToRefresh.from(this)
				.theseChildrenArePullable(R.id.list_view, android.R.id.empty)
				.listener(this).setup(mPullToRefreshLayout);

		MyRequestState MyRequestState = new MyRequestState(VIEW_COMMENT);
		view.setTag(MyRequestState);
		setListScrollListener(list_view, MyRequestState);
	}

	private void initViewPager() {
		mPager = (ViewPager) findViewById(R.id.vPager);
		mPager.setOffscreenPageLimit(3);
		listViews = new ArrayList<View>();

		View bilateralList = mInflater.inflate(R.layout.timeline_list, null);
		prepareView(bilateralList, VIEW_BILATERAL);
		listViews.add(bilateralList);

		View homeList = mInflater.inflate(R.layout.timeline_list, null);
		prepareView(homeList, VIEW_HOME);
		listViews.add(homeList);

		View mentionsList = mInflater.inflate(R.layout.timeline_list, null);
		prepareView(mentionsList, VIEW_AT_ME);
		listViews.add(mentionsList);

		View commentMeList = mInflater.inflate(R.layout.timeline_list, null);
		prepareCommentView(commentMeList);
		listViews.add(commentMeList);

		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mPager.setAdapter(new MyPagerAdapter(listViews));
	}

	private void cleanPersistenceData(RequestState requestState) {

		switch (requestState.requestType) {
		case VIEW_BILATERAL:
			Persistence.setBilateralData(getApplicationContext(), null);
			break;

		case VIEW_HOME:
			Persistence.setHomeData(getApplicationContext(), null);
			break;

		case VIEW_AT_ME:
			Persistence.setAtMeData(getApplicationContext(), null);
			break;

		case VIEW_COMMENT:
			Persistence.setCommentData(getApplicationContext(), null);
			break;
		}

	}

	private String getPersistenceData(RequestState requestState) {
		String data = null;
		switch (requestState.requestType) {
		case VIEW_BILATERAL:
			data = Persistence.getBilateralData(getApplicationContext());
			break;

		case VIEW_HOME:
			data = Persistence.getHomeData(getApplicationContext());
			break;

		case VIEW_AT_ME:
			data = Persistence.getAtMeData(getApplicationContext());
			break;

		case VIEW_COMMENT:
			data = Persistence.getCommentData(getApplicationContext());
			break;
		}
		return data;
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int pos) {
			View view = listViews.get(pos);
			current = (MyRequestState) view.getTag();
			if (current.isFirstTime) {
				getTwitter(false);
			}
			if (actionBar.getSelectedNavigationIndex() != pos) {
				actionBar.setSelectedNavigationItem(pos);
			}
		}
	}

	private void setFootView(ListView list_view) {
		View foot_view = mInflater.inflate(R.layout.loadmorelayout, null);
		foot_view.setVisibility(View.VISIBLE);
		list_view.addFooterView(foot_view);
	}

	@Override
	public void onRefreshStarted(View view) {
		getTwitter(true);
	}

	@Override
	protected void onRequestComplete(RequestState requestState) {
		if (TextUtils.isEmpty(requestState.response)) {
			return;
		}
		String data = getPersistenceData(requestState);
		MyRequestState state = (MyRequestState) requestState;
		if (TextUtils.isEmpty(data) || state.dataNeedSave) {
			Debug.e("save data.............");
			switch (requestState.requestType) {
			case VIEW_BILATERAL:
				Persistence.setBilateralData(getApplicationContext(),
						requestState.response);
				break;
			case VIEW_HOME:
				Persistence.setHomeData(getApplicationContext(),
						requestState.response);
				break;
			case VIEW_AT_ME:
				Persistence.setAtMeData(getApplicationContext(),
						requestState.response);
				break;
			case VIEW_COMMENT:
				Persistence.setCommentData(getApplicationContext(),
						requestState.response);
				break;
			}
		}
		state.dataNeedSave = false;
	}

}
