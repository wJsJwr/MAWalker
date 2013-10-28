
package walker;

import info.Area;
import info.Card;
import info.Deck;
import info.FairyBattleInfo;
import info.FairySelectUser;
import info.Floor;
import info.PFBGood;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import action.ActionRegistry;

public class Info {
	// INFO: static variants are need to be configured in configure file
	
	// login info
	public static String LoginId = "";
	public static String LoginPw = "";
	public static int Profile = 1;
	
	// user info
	public String username = "";
	public int ap = 0;
	public int bc = 0;
	public int apMax = 0;
	public int bcMax = 0;
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
	public long money = 0;
	
	/**
	 * 优先进行妖精战
	 */
	public static boolean FairyBattleFirst = true;
	/**
	 * 允许deck不满足的情况下依旧走图和攻击
	 */
	public static boolean AllowBCInsuffient = false;
	/**
	 * 设置最低跑图AP
	 */
	public static int ThisAPOnly = -1;
	/**
	 * 自动加点
	 */
	public static boolean AutoAddp = true;
	/**
	 * 允许舔同一个怪
	 */
	public static boolean AllowAttackSameFairy = true;
	/**
	 * debug输出xml
	 */
	public static boolean debug = false;
	/**
	 * night mode 开关
	 */
	public static boolean nightModeSwitch = true;
	
	/**
	 * 自动收集妖精战礼物
	 */
	public static boolean receiveBattlePresent = true;
	
	// 吃药相关的开关
	public static boolean autoUseAp = true;
	public static boolean autoUseBc = true;
	public enum autoUseType {
		HALF_ONLY,
		FULL_ONLY,
		ALL
	}
	public static autoUseType autoApType = autoUseType.HALF_ONLY;
	public static autoUseType autoBcType = autoUseType.HALF_ONLY;
	public static int autoApLow = 1;
	public static int autoBcLow = 50;
	public static int autoApFullLow = 10;
	public static int autoBcFullLow = 10;
	
	// card list
	public ArrayList<Card> cardList;
	public static ArrayList<String> CanBeSold = new ArrayList<String>();
	public static ArrayList<String> KeepCard;
	public String toSell = "";
	
	// deck
	public static Deck FriendFairyBattleRare = new Deck();
	public static Deck PublicFairyBattle = new Deck();
	public static Deck PrivateFairyBattleNormal = new Deck();
	public static Deck PrivateFairyBattleRare = new Deck();
	public static Deck FriendFairyBattleNormal = new Deck();
	
	
	// area
	public Hashtable<Integer,Area> area;
	public boolean InnerMap = false;
	public int MinMapAP = Integer.MAX_VALUE;
	
	
	// floor
	public Hashtable<Integer,Floor> floor;
	public Floor front;
	public boolean AllClear = false;
	
	// fairy
	public FairyBattleInfo fairy;
	public FairyBattleInfo gfairy;
	public boolean NoFairy = false;
	public Queue<FairyBattleInfo> LatestFairyList = new LinkedList<FairyBattleInfo>();
	public Stack<PFBGood> PFBGoodList;
	public Hashtable<String,FairySelectUser> FairySelectUserList;
	
	// explore
	public String ExploreResult = "";
	public String ExploreProgress = "";
	public String ExploreGold = "";
	public String ExploreExp = "";
	
	
	//吃药相关
	public int fullBc = 0;
	public int fullAp = 0;
	public int halfBc = 0;
	public int halfAp = 0;
	public int halfBcToday = 0;
	public int halfApToday = 0;
	public String toUse = "";
	
	
	// timeout
	public enum TimeoutEntry {
		apbc,
		fairy,
		login,
		reward,
		map,
		ticket,
	}
	private Hashtable<TimeoutEntry,Long> timeout;
	public long GetTimeout(TimeoutEntry te) {
		return System.currentTimeMillis() - timeout.get(te);
	}
	public void SetTimeoutByEntry(TimeoutEntry te) {
		timeout.put(te, System.currentTimeMillis());
	}
	public void SetTimeoutByAction(ActionRegistry.Action act) {
		switch (act) {
		case LOGIN:	
			this.SetTimeoutByEntry(TimeoutEntry.fairy);
			this.SetTimeoutByEntry(TimeoutEntry.login);
		case PRIVATE_FAIRY_BATTLE:
		case GUILD_BATTLE:
			this.SetTimeoutByEntry(TimeoutEntry.ticket);
		case GOTO_FLOOR:
		case EXPLORE:
		case LV_UP:
			this.SetTimeoutByEntry(TimeoutEntry.apbc);
			break;
		case PFB_GOOD:
		case ADD_AREA:
		case GET_FLOOR_INFO:
			this.SetTimeoutByEntry(TimeoutEntry.map);
			break;
		case GET_FAIRY_LIST:
			this.SetTimeoutByEntry(TimeoutEntry.fairy);
			break;
		default:
			break;
		}
	}
	public ArrayList<TimeoutEntry> CheckTimeout() {
		ArrayList<TimeoutEntry> te = new ArrayList<TimeoutEntry>();
		if (GetTimeout(TimeoutEntry.apbc) > 180000) te.add(TimeoutEntry.apbc);
		if (GetTimeout(TimeoutEntry.fairy) > 60000) te.add(TimeoutEntry.fairy);
		if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 1) {
			if (GetTimeout(TimeoutEntry.login) > 3600000) te.add(TimeoutEntry.login);
		}
		if (GetTimeout(TimeoutEntry.reward) > 86400000l) te.add(TimeoutEntry.reward);
		if (GetTimeout(TimeoutEntry.map) > 86400000l) te.add(TimeoutEntry.map);
		if (GetTimeout(TimeoutEntry.ticket) > 600000) te.add(TimeoutEntry.ticket); 
		return te;
	}
	
	// event
	public enum EventType {
		notLoggedIn,
		cookieOutOfDate,
		needFloorInfo,
		innerMapJump,
		areaComplete,
		fairyAppear,
		fairyTransform,
		fairyReward,
		fairyCanBattle,
		fairyBattleWin,
		fairyBattleLose,
		fairyBattleEnd,
		cardFull,
		privateFairyAppear,
		guildTopRetry,
		guildBattle,
		guildTop,
		ticketFull,
		getFairyReward,
		needAPBCInfo,
		levelUp,
		PFBGood,
		recvPFBGood, gotoFloor
	}
	public Stack<EventType> events;
	
	
	public Info() {
		cardList = new ArrayList<Card>();
		area = new Hashtable<Integer,Area>();
		floor = new Hashtable<Integer,Floor>();
		front = new Floor();
		PFBGoodList = new Stack<PFBGood>();
		events = new Stack<EventType>();
		events.push(EventType.notLoggedIn);
		KeepCard = new ArrayList<String>();
		FairySelectUserList = new Hashtable<String,FairySelectUser>();
		
		timeout = new Hashtable<TimeoutEntry,Long>();
		timeout.put(TimeoutEntry.apbc, (long) 0);
		timeout.put(TimeoutEntry.fairy, (long) 0);
		timeout.put(TimeoutEntry.login, (long) 0);
		timeout.put(TimeoutEntry.reward, (long) 0);
		timeout.put(TimeoutEntry.map, (long) 0);
		
		fairy = new FairyBattleInfo();
		gfairy = new FairyBattleInfo();
	}
	
}
