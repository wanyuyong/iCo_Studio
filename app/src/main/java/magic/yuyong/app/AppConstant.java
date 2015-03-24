package magic.yuyong.app;

public class AppConstant {
	
	public static final int PAGE_NUM = 40;
	public static final int GET_UNREAD_PERIOD = 60 * 1000;

	public static final String DONATE_HOME = "https://me.alipay.com/wanyuyong";

	public static final int NORMAL_MODE = 0;
	public static final int EXTEND_MODE = 1;

	public static final String SUFFIX_WIDE = "wide";
	public static final String SUFFIX_NARROW = "narrow";

	// 0：娱乐、1：搞笑、2：美女、3：旅行、4：星座、5：情感、6：名车、7：美食、8：音乐。
	public static final int TYPE_ENTERTAINMENT = 0;
	public static final int TYPE_FUNNY = 1;
	public static final int TYPE_BEAUTY = 2;
	public static final int TYPE_TRAVEL = 3;
	public static final int TYPE_CONSTELLATION = 4;
	public static final int TYPE_EMOTION = 5;
	public static final int TYPE_CARS = 6;
	public static final int TYPE_FOOD = 7;
	public static final int TYPE_MUSIC = 8;

	/**
	 * 南都娱乐周刊 id : 1216431741 新浪音乐 id : 1266269835 搜狐娱乐 id : 1843633441 韩流图库 id
	 * : 2665043382 韩国思蜜达 id : 2244525523 韩国me2day id : 2482557597 娱乐新闻快报 id :
	 * 1870252767 新浪娱乐 id : 1642591402 欧美音悦Tai id : 1834100153 日韩潮风尚 id :
	 * 2570481764 蔚蓝日本娱乐播报 id : 1237743061
	 */
	public static final String UIDS_ENTERTAINMENT = "1216431741,1266269835,1843633441,2665043382,2244525523,2482557597,1870252767,1642591402,1834100153,2570481764,1237743061";

	/**
	 * 内裤都笑松了 id : 2301726604 冷笑话精选荟 id : 2561535853 糗事大百科 id :
	 * 1720084970 
	 */
	public static final String UIDS_FUNNY = "2301726604,2561535853,1720084970";

	/**
	 * 中国美女报告 id : 1666559280 中国美腿 id : 2317382462 美腿团 id : 2716523850 全球美女研究 id
	 * : 1497246097 美女写真照 id : 1669899263 美女热门榜 id : 3858679729 女神阁 id :
	 * 3075537743 美女美图营 id : 3814895924 全球时尚性感美女 id : 2740378344 校园美女大本营 id :
	 * 2393383161 厦门校花 id : 2731172954  美女胸 id : 1763276065
	 * 模特美女集中营   id : 1925266324
	 */
	public static final String UIDS_BEAUTY = "1925266324,1666559280,2317382462,2716523850,1497246097,1669899263,3858679729,3075537743,3814895924,2740378344,2393383161,2731172954,1763276065";

	/**
	 * 雷永辉 id : 1074898101 冰城馨子 id : 1235305362 菜尾蝗-旅游养生禅 id : 1240183873 七月娃娃
	 * id : 1489734185 弹指间行摄 id : 1250590793 简白 id : 1272394180 徐铁人 id :
	 * 1255082647 七色地图 id : 1236937620
	 */
	public static final String UIDS_TRAVEL = "1074898101,1235305362,1240183873,1489734185,1250590793,1272394180,1255082647,1236937620";

	/**
	 *时尚丶星座丶旅游2815226784
	 *占星小巫爱星座2154332097
	 *天生爱星座1815169397
	 */
	public static final String UIDS_CONSTELLATION = "2815226784,2154332097,1815169397";

	/**
	 * 教你看穿男人的心 id : 2602484450 围脖心情心语 id : 1497162881 999个改变你的爱情故事 id :
	 * 2230427205 岁月笔记_ id : 2812049784 8090心计学 id : 2122282851 提拉米苏的1001夜情书 id
	 * : 2329784855 情感漫画屋 id : 1765676605 女人专属心语 id : 2442177240 气质女子训练营 id :
	 * 2646679797 一本书一首诗一段情 id : 1864146860 解密人性 id : 3096522655 经典心理学 id :
	 * 1718622107 和姐一起做霸气范儿 id : 2968590850 一句情话签名 id : 3837776976 语录库 id :
	 * 1850240642
	 */
	public static final String UIDS_EMOTION = "2602484450,1497162881,2230427205,2812049784,2122282851,2329784855,1765676605,2442177240,2646679797,1864146860,3096522655,1718622107,2968590850,3837776976,1850240642";

	/**
	 * 车迷秀秀 id : 1784897432 全球车迷世界 id : 1731209283 车迷闲聊 id : 1706146655 车迷杂志 id
	 * : 2101763120 车迷编辑 id : 2460972822 最IN车迷 id : 2346365882 汽车族孙刚 id :
	 * 1275182592 裴达军 id : 1964158257 GTGT id : 1400806871
	 */
	public static final String UIDS_CARS = "1784897432,1731209283,1706146655,2101763120,2460972822,2346365882,1275182592,1964158257,1400806871";

	/**
	 * 成都美食 id : 1615913053 君之 id : 1246792191 十全菜谱的美食心情 id : 1627572147 上海美食 id
	 * : 1702872420 文怡 id : 1420174783 美食天下 id : 1642625033 微美食 id : 2300267615
	 * 大众点评网-上海美食 id : 1853946117 微吃货 id : 2188014311 美食 id : 1828872085
	 * 菜尾蝗-旅游养生禅 id : 1240183873 京城美食探店 id : 2597465527
	 */
	public static final String UIDS_FOOD = "1615913053,1246792191,1627572147,1702872420,1420174783,1642625033,2300267615,1853946117,2188014311,1828872085,1240183873,2597465527";

	/**
	 * 小柯 id : 1254745922 多米音乐 id : 1779785750 咪咕音乐 id : 1867028705 音乐人 id :
	 * 1852855013 原创音乐基地 id : 1750294477 MusicRadio音乐之声 id : 1668662863 百威为音乐而创
	 * id : 3237407213 微博音乐盒 id : 2299824385 高晓松 id : 1191220232 虾米音乐 id :
	 * 1718436033 微乐迷 id : 2189743085
	 */
	public static final String UIDS_MUSIC = "1254745922,1779785750,1867028705,1852855013,1750294477,1668662863,3237407213,2299824385,1191220232,1718436033,2189743085";

	// Post type
	public static final int TYPE_POST_TEXT = 0;
	public static final int TYPE_POST_TEXT_IMG = 1;
	public static final int TYPE_REPOST = 2;
	public static final int TYPE_COMMENT = 3;
	public static final int TYPE_REPLY_COMMENT = 4;

	// handler msg
	public static final int MSG_UPDATE_VIEW = 1;
	public static final int MSG_NETWORK_EXCEPTION = 2;
	public static final int MSG_TOKEN_INVALID = 3;
	public static final int MSG_SHOW_TWITTER = 4;
	public static final int MSG_FAVORITE_SUCCEED = 5;
	public static final int MSG_FAVORITE_CANCEL_SUCCEED = 6;
	public static final int MSG_SHOW_COMMENTS = 7;
	public static final int MSG_DELETE_SUCCEED = 8;
	public static final int MSG_DELETE_FAILD = 9;
	public static final int MSG_FOLLOW_SUCCEED = 10;
	public static final int MSG_FOLLOW_FAILD = 11;
	public static final int MSG_UPDATA_GROUP = 12;
	public static final int MSG_POST_SUCCEED = 13;
	public static final int MSG_POST_FAILD = 14;
	public static final int MSG_UPDATA_FRIENDS = 15;
	public static final int MSG_UPDATA_SEARCH_FRIENDS = 16;
	public static final int MSG_UNFOLLOW_SUCCEED = 17;
	public static final int MSG_UNFOLLOW_FAILD = 18;
	public static final int MSG_LOGIN_OUT_SUCCEED = 19;
	public static final int MSG_LOGIN_OUT_FAILD = 20;
	public static final int MSG_SAVE_PIC_SUCCEED = 21;
	public static final int MSG_SAVE_PIC_FAILD = 22;
	public static final int MSG_SHOW_GIF = 23;
	public static final int MSG_DOWN_PERCENTAGE = 24;
	public static final int MSG_DOWN_BEGIN = 25;
	public static final int MSG_DOWN_END = 26;
	public static final int MSG_SHOW_REPOSTS = 27;
	public static final int MSG_SHOW_POST_PIC = 28;
	public static final int MSG_CLEAN_CACHE_SUCCEED = 29;

	// requestCode
	public static final int REQUESTCODE_COMMENT = 0;
	public static final int REQUESTCODE_FORWARD = 1;

	// broadcast
	public static final String ACTION_REFRESH_COMMENT = "action_refresh_comment";
	public static final String ACTION_REFRESH_CACHE = "action_refresh_cache";
	public static final String ACTION_UNREAD_STATE_CHANGE_BROADCAST = "action_unread_state_change_broadcast";
	public static final String ACTION_SHUT_DOWN_BROADCAST = "action_shut_down_broadcast";
	public static final String ACTION_TOPVIEW_SCROLL = "action_topview_scroll";

	// Sina
	public static final String CONSUMER_KEY = "577780336";
	public static final String CONSUMER_SECRET = "534de6e04d0ae028f956b21707bc5dda";
	public static final String REDIRECT_URL = "http://weibo.com/u/1069103203";
	public static final String SCOPE = "all";

	// domob
	public static final String DOMOB_KEY = "56OJzndIuMkKkAVNWU";
	
	//wandoujia
	public static final String ADS_APP_ID = "100002267";
	public static final String ADS_SECRET_KEY = "1105569891cb4c0ebce7e20fc0913a4e";
}
