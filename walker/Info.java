package walker;

import info.Area;
import info.Card;
import info.Deck;
import info.FairyBattleInfo;
import info.FairySelectUser;
import info.Floor;
import info.FairyDianzanInfo;
import info.GuildFairyBattleForce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Info {
	// INFO: static variants are need to be configured in configure file

	// login info
	public static String LoginId = "";
	public static String LoginPw = "";

	// user info
	public String username = "";
	public String guildteamname = "";
	public int ap = 0;
	public int bc = 0;
	public int apMax = 0;
	public int bcMax = 0;
	public int gold = 0;
	public int friendshippoint = 0;
	public int exp = 0;
	public int gather = 0;
	public int rankPerson = 0;
	public int rankGroup = 0;
	public int rankGather = 0;
	public int lv = 0;
	public int cardMax = 0;
	public int rewardCount = 0;
	public int rewardMax = 0;
	public String guildId = "";
	public String userId = "";
	public int ticket = 0;
	public String week = "";
	public int pointToAdd = 0;
	public int apUp = 0;
	public int bcUp = 0;
	public boolean myFairyStillAlive = true;

	/**
	 * 允许deck不满足的情况下依旧走图和攻击
	 */
	public static boolean AllowBCInsuffient = false;
	/**
	 * 只走cost1的图
	 */
	public static boolean OneAPOnly = false;
	/**
	 * 自动加点
	 */
	public static boolean AutoAddp = true;
	/**
	 * 强制跑图AP上限
	 */
	public static double ApForceGo = 0.8;
	/**
	 * 跑无event图
	 */
	public static boolean GoNoEventArea = false;
	/**
	 * 跑每日秘境
	 */
	public static boolean GoDailyArea = true;
	/**
	 * 调试输出
	 */
	public static boolean Debug = false;
	/**
	 * 不显示一些输出
	 */
	public static boolean Nolog = false;
	/**
	 * 强制外敌战挑战书上限
	 */
	public static int ticket_max = 0;
	public static double battle_win_scale = 1;
	/**
	 * night mode 开关
	 */
	public static boolean nightModeSwitch = true;
	/**
	 * 自动卖卡
	 */
	public static boolean autoSellCard = false;
	public static boolean sell1star = false;
	public static boolean sell2star = false;
	public static boolean sell3star = false;
	public static boolean sell4star = false;
	public static int levelCardNotSell = 0;
	public static int cardFull = 200;
	/**
	 * 吃药相关的开关
	 */
	public static boolean autoUseAp = true;
	public static boolean autoUseBc = true;
	/**
	 * cookie登陆的sessionId
	 */
	public static String sessionId = "";
	/**
	 * 妖精战优先
	 */
	public static boolean FairyBattleFirst = false;
	/**
	 * 自动加点，AP/BC选择
	 */
	public static boolean AutoAddAP = false;
	/**
	 * 检测间隔
	 */
	public static long sleep_time = 10;
	/**
	 * 外敌战BC Buff
	 */
	public static boolean OnlyBcBuff = false;
	/**
	 * 尾刀卡组出击bc下限
	 */
	public static double KillFairyScale = 0;
	/**
	 * 定时任务
	 */
	public static boolean useSleep = false;
	public static Calendar startTime = Calendar.getInstance();
	public static Calendar stopTime = Calendar.getInstance();

	/**
	 * 自动吃药方式
	 */
	public enum autoUseType {
		HALF_ONLY, FULL_ONLY, ALL
	}

	public static autoUseType autoApType = autoUseType.HALF_ONLY;
	public static autoUseType autoBcType = autoUseType.HALF_ONLY;
	public static int autoApLow = 1;
	public static int autoBcLow = 50;
	public static int autoApFullLow = 10;
	public static int autoBcFullLow = 10;

	// card list
	public ArrayList<Card> cardList;
	public Hashtable<String, Card> myCardList;
	public static ArrayList<String> CanNotBeSold = new ArrayList<String>();
	public static ArrayList<String> KeepCard;
	public String toSell = "";

	// deck
	public static Deck FriendFairyBattleRare = new Deck(
			"Friend's Rare Fairy Deck");
	public static Deck PublicFairyBattle = new Deck("Guild Fairy Deck");
	public static Deck PrivateFairyBattleNormal = new Deck("My Fairy Deck");
	public static Deck PrivateFairyBattleRare = new Deck("My Rare Fairy Deck");
	public static Deck FriendFairyBattleNormal = new Deck("Friend's Fairy Deck");
	public static Deck BcInsufficientDeck = new Deck("BcInsufficientDeck");
	public static Deck KillFairyDeck = new Deck("Kill Fairy Deck");
	public Deck CurrentDeck = new Deck();
	public static Deck MyDeckA1 = new Deck();
	public static Deck MyDeckA2 = new Deck();
	public static Deck MyDeckA3 = new Deck();
	public static Deck MyDeckA4 = new Deck();
	public static Deck MyDeckA5 = new Deck();
	public static Deck MyDeck0 = new Deck();
	public static Deck MyDeck1 = new Deck();
	public static Deck MyDeck2 = new Deck();
	public static Deck LastDeck = new Deck();

	// area
	public Hashtable<Integer, Area> area;
	public boolean InnerMap = false;

	// floor
	public Hashtable<Integer, Floor> floor;
	public ArrayList<Floor> allFloors;
	public Floor front;
	public boolean AllClear = false;

	// fairy
	public FairyBattleInfo pfairy;
	public FairyBattleInfo gfairy;
	public GuildFairyBattleForce gfbforce;
	public boolean NoFairy = false;
	public Queue<FairyBattleInfo> LatestFairyList = new LinkedList<FairyBattleInfo>();
	public Queue<FairyBattleInfo> PrivateFairyList = new LinkedList<FairyBattleInfo>();
	public Stack<FairyDianzanInfo> FairyDianzanList = new Stack<FairyDianzanInfo>();
	public Hashtable<String, FairySelectUser> FairySelectUserList;

	// user
	public static ArrayList<String> specUser = new ArrayList<String>();

	// explore
	public String ExploreResult = "";
	public String ExploreProgress = "";
	public String ExploreGold = "";
	public String ExploreExp = "";
	public String AreaProgress = "";

	// 吃药相关
	public int fullBc = 0;
	public int fullAp = 0;
	public int halfBc = 0;
	public int halfAp = 0;
	public int halfBcToday = 0;
	public int halfApToday = 0;
	public String toUse = "";

	// event
	public enum EventType {
		notLoggedIn, cookieOutOfDate, needFloorInfo, innerMapJump, areaComplete, getFairyList, fairyReward, fairyCanBattle, cardFull, guildTop, needAPBCInfo, levelUp, fairyDianzan, gotoFloor, autoMedicine, autoExplore, cookieLogin, getCardDeck
	}

	public Stack<EventType> events;

	public Info() {
		cardList = new ArrayList<Card>();
		myCardList = new Hashtable<String, Card>();
		area = new Hashtable<Integer, Area>();
		floor = new Hashtable<Integer, Floor>();
		allFloors = new ArrayList<Floor>();
		front = new Floor();
		PrivateFairyList = new LinkedList<FairyBattleInfo>();
		FairyDianzanList = new Stack<FairyDianzanInfo>();
		events = new Stack<EventType>();
		events.push(EventType.cookieLogin);
		KeepCard = new ArrayList<String>();
		FairySelectUserList = new Hashtable<String, FairySelectUser>();

		pfairy = new FairyBattleInfo();
		gfairy = new FairyBattleInfo();
		gfbforce = new GuildFairyBattleForce();

		CurrentDeck = new Deck();
	}

}
