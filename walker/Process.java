package walker;

import info.GuildFairyBattleForce;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.Network;

import org.w3c.dom.Document;

import action.ActionRegistry.Action;
import action.AddArea;
import action.AutoMedicine;
import action.CookieLogin;
import action.Explore;
import action.FairyDianzan;
import action.GetCardDeck;
import action.GetFairyList;
import action.GetFairyReward;
import action.GetFloorInfo;
import action.GetRewards;
import action.GotoFloor;
import action.GotoMainMenu;
import action.GuildBattle;
import action.GuildTop;
import action.Login;
import action.LvUp;
import action.PrivateFairyBattle;
import action.RecFairyDianzan;
import action.RewardBox;
import action.SellCard;

public class Process {
	public static Info info;
	public static Network network;
	public static Timer TaskTimer;
	public static CardDataBase CardData;
	private static long lastGuildBattleTime;

	public Process() {
		info = new Info();
		network = new Network();
		TaskTimer = new Timer();
		CardData = new CardDataBase();
		lastGuildBattleTime = System.currentTimeMillis();
	}

	public void run() {
		if (Info.useSleep) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Go.log("Sleep schedule: start time: "
					+ df.format(Info.startTime.getTime()) + ", stop time: "
					+ df.format(Info.stopTime.getTime()) + ".", true);
		}
		while (true) {
			try {
				auto();
			} catch (Exception ex) {
				Go.log(ex.getMessage(), true);
				Process.AddUrgentTask(Info.EventType.cookieOutOfDate);
				Go.log("Restart", true);
			}
		}
	}

	public void auto() throws Exception {
		try {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				rescue();
			else {
				long start = System.currentTimeMillis();
				Action ActionToDo = Action.NOTHING;
				while (true) {
					ActionToDo = Think.doIt(getPossibleAction());
					if (ActionToDo != Action.NOTHING)
						break;
				}
				execute(ActionToDo);
				long delta = System.currentTimeMillis() - start;
				long tmp = getRandom(0, 5000);
				if (delta < Info.sleep_time * 1000 + tmp)
					Thread.sleep(Info.sleep_time * 1000 + tmp - delta);
				if (Info.nightModeSwitch && info.events.empty() && info.NoFairy)
					Thread.sleep(2 * 60000); // 半夜速度慢点,等待60s
				if (Info.useSleep)
					SleepSchedule();
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	private void SleepSchedule() throws Exception {
		try {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			long tmpStartTime = Info.startTime.getTimeInMillis();
			long tmpStopTime = Info.stopTime.getTimeInMillis();
			while (true) {
				currentTime = Calendar.getInstance().getTimeInMillis();
				if (currentTime > tmpStartTime && currentTime < tmpStopTime) {
					Go.log("Sleeping~~~", Info.Nolog);
					long sleepTime = (tmpStopTime - tmpStartTime) / 10;
					if (sleepTime > tmpStopTime - currentTime)
						sleepTime = tmpStopTime - currentTime + 5000;
					Thread.sleep(sleepTime);
				} else {
					break;
				}
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	private void AddTimerTasks() {
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.getFairyList);
			}
		}, 0, 5 * 1000);// 5s
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.guildTop);
			}
		}, 0, 45 * 1000);// 45s
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.autoExplore);
			}
		}, 0, 30 * 1000);// 30s
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.needFloorInfo);
			}
		}, 0, 5 * 60 * 1000);// 5min
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.needAPBCInfo);
			}
		}, 0, 5 * 60 * 1000);// 5min
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.autoMedicine);
			}
		}, 0, 5 * 60 * 1000); // 5min
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.rewardBox);
			}
		}, 0, 10 * 60 * 1000); // 10min
		Calendar myCal = Calendar.getInstance();
		if (myCal.get(Calendar.HOUR_OF_DAY) >= 1) {
			int date = myCal.get(Calendar.DAY_OF_YEAR);
			myCal.set(Calendar.DAY_OF_YEAR, date + 1);
		}
		myCal.set(Calendar.HOUR_OF_DAY, 1);
		myCal.set(Calendar.MINUTE, 0);
		myCal.set(Calendar.SECOND, 0);
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddUrgentTask(Info.EventType.notLoggedIn);
			}
		}, myCal.getTime(), 24 * 60 * 60 * 1000l);// relogin at 1:00
	}

	public static void AddTask(Info.EventType _Task) {
		if (!Process.info.events.contains(_Task)) {
			Process.info.events.add(0, _Task);
			if (Info.Debug)
				Go.log(String.format("Add Task: %s", _Task), Info.Debug);
		}
	}

	public static boolean AddUrgentTask(Info.EventType _Task) {
		if (!Process.info.events.contains(_Task)) {
			Process.info.events.push(_Task);
			Go.log(String.format("Add Urgent Task: %s", _Task), Info.Debug);
			return true;
		} else {
			Process.info.events.remove(_Task);
			Process.info.events.push(_Task);
			Go.log(String.format("Change and Add Urgent Task: %s", _Task),
					Info.Debug);
			return false;
		}
	}

	private void rescue() {
		Go.log(ErrorData.currentErrorType.toString(), true);
		switch (ErrorData.currentDataType) {
		case bytes:
			if (ErrorData.bytes != null) {
				Go.log(new String(ErrorData.bytes), true);
			} else {
				Go.log("Set type to byte, but no message", true);
			}
			break;
		case text:
			Go.log(ErrorData.text, true);
			break;
		default:
			break;
		}
		ErrorData.clear();
	}

	private List<Action> getPossibleAction() {
		ArrayList<Action> result = new ArrayList<Action>();
		if (info.events.size() != 0) {
			switch (info.events.pop()) {
			case notLoggedIn:// 未登录
			case cookieOutOfDate:// cookie失效
				result.add(Action.LOGIN);
				break;
			case cookieLogin:// cookie登陆
				result.add(Action.COOKIELOGIN);
				break;
			case fairyDianzan:// 点赞
				result.add(Action.FAIRY_DIANZAN);
				break;
			case fairyCanBattle:// 妖精战
				result.add(Action.PRIVATE_FAIRY_BATTLE);
				break;
			case fairyReward:// 获取奖励
				result.add(Action.GET_FAIRY_REWARD);
				break;
			case innerMapJump:// 里图
				Go.log("Map Status Changed!", true);
			case needFloorInfo:// 获取秘境信息
				result.add(Action.GET_FLOOR_INFO);
				break;
			case areaComplete:// 一个图跑完
				result.add(Action.ADD_AREA);
				break;
			case cardFull:// 卡满了
				result.add(Action.SELL_CARD);
				break;
			case getFairyList:// 获取妖精战列表
				result.add(Action.GET_FAIRY_LIST);
				break;
			case guildTop:// 强敌站界面
				result.add(Action.GUILD_TOP);
				break;
			case needAPBCInfo:// 更新apbc信息
				result.add(Action.GOTO_MAIN_MENU);
				break;
			case gotoFloor:
				result.add(Action.GOTO_FLOOR);
				break;
			case levelUp:// 升级
				if (!Info.AutoAddp) {
					Go.log("Auto Adding points is closed.", !Info.Nolog);
				} else {
					result.add(Action.LV_UP);
				}
			case autoMedicine:
				result.add(Action.AUTO_MEDICINE);
				break;
			case autoExplore:
				result.add(Action.EXPLORE);
			case getCardDeck:
				result.add(Action.GET_CARD_DECK);
			case rewardBox:
				result.add(Action.REWARD_BOX);
			case getRewards:
				result.add(Action.GET_REWARDS);
			}
			if (!result.isEmpty())
				return result;
		}
		return result;
	}

	private void execute(Action action) throws Exception {
		switch (action) {
		case COOKIELOGIN:
			try {
				switch (CookieLogin.run()) {
				case 1:
					Go.log(String
							.format("Cookie Login: User: %s, AP: %d/%d, BC: %d/%d, Card: %d/%d, ticket: %d, sessionId: %s",
									info.username, info.ap, info.apMax,
									info.bc, info.bcMax, info.cardList.size(),
									info.cardMax, info.ticket, Info.sessionId),
							true);
					Process.info.events.clear();
					AddUrgentTask(Info.EventType.getCardDeck);
					AddUrgentTask(Info.EventType.needFloorInfo);
					AddTimerTasks();
					break;
				case 0:
					Go.log(ErrorData.text, Info.Debug);
					Go.log("Cookie Login Failed, waiting to login with username and password.",
							true);
					ErrorData.clear();
					AddUrgentTask(Info.EventType.notLoggedIn);
					break;
				case 2:
					AddUrgentTask(Info.EventType.cookieLogin);
					break;
				}
			} catch (Exception ex) {
				AddUrgentTask(Info.EventType.cookieOutOfDate);
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
					throw ex;
				}
			}
			break;
		case LOGIN:
			try {
				switch (Login.run()) {
				case 1:
					Go.log(String
							.format("Normal Login: User: %s, AP: %d/%d, BC: %d/%d, Card: %d/%d, ticket: %d, sessionId: %s",
									info.username, info.ap, info.apMax,
									info.bc, info.bcMax, info.cardList.size(),
									info.cardMax, info.ticket, Info.sessionId),
							true);
					Process.info.events.clear();
					AddUrgentTask(Info.EventType.getCardDeck);
					AddUrgentTask(Info.EventType.needFloorInfo);
					AddTimerTasks();
					break;
				case 0:
					AddUrgentTask(Info.EventType.notLoggedIn);
					break;
				case 2:
					AddUrgentTask(Info.EventType.cookieLogin);
					break;
				}
			} catch (Exception ex) {
				AddUrgentTask(Info.EventType.notLoggedIn);
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
					throw ex;
				}
			}
			break;
		case GET_FLOOR_INFO:
			try {
				if (GetFloorInfo.run()) {
					Go.log(String.format(
							"Area(%d) Front: %s>%s@c=%d",
							info.area.size(),
							info.area.get(Integer.parseInt(info.front.areaId)).areaName,
							info.front.floorId, info.front.cost), true);
				}

			} catch (Exception ex) {
				if (ex.getMessage() != null && ex.getMessage().equals("302")) {
					AddUrgentTask(Info.EventType.innerMapJump);
					ErrorData.clear();
				} else {
					if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
						throw ex;
				}
			}
			break;
		case ADD_AREA:
			try {
				if (AddArea.run()) {

					Go.log(String.format(
							"Area(%d) Front: %s>%s@c=%d",
							info.area.size(),
							info.area.get(Integer.parseInt(info.front.areaId)).areaName,
							info.front.floorId, info.front.cost), true);
				}

			} catch (Exception ex) {
				if (ex.getMessage().equals("302")) {
					AddUrgentTask(Info.EventType.innerMapJump);
					ErrorData.clear();
				} else {
					if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
						throw ex;
				}
			}
			break;
		case GET_FAIRY_LIST:
			try {
				if (GetFairyList.run()) {
					if (!info.events.empty()
							&& info.events
									.contains(Info.EventType.fairyCanBattle)) {
						Go.log(String.format("%d new fairy(s) found!",
								Process.info.PrivateFairyList.size()),
								!Info.Nolog);
					} else {
						Go.log("No fairy found.", !Info.Nolog);
					}
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.ConnectionError) {
					AddUrgentTask(Info.EventType.getFairyList); // 再次检测
					Go.log("Connection error.Retry to get fairy list.", true);
					ErrorData.clear();
				} else if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
					throw ex;
				}
			}

			break;
		case FAIRY_DIANZAN:
			try {
				if (FairyDianzan.run()) {

				} else {
					Go.log("Something wrong@DIANZAN.", !Info.Nolog);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GOTO_FLOOR:
			try {
				if (GotoFloor.run()) {
					String my_state = String
							.format("User Name: %s, AP: %d/%d, BC: %d/%d, Level: %d, Exp to level up: %d, Cards: %d, Gold: %d, Friendship point: %d.\n",
									info.username, info.ap, info.apMax,
									info.bc, info.bcMax, info.lv, info.exp,
									info.cardList.size(), info.gold,
									info.friendshippoint);
					my_state += String.format(
							"Guild Fairy Battle Team: %s. Ticket: %d. ",
							info.guildteamname, info.ticket);
					my_state += String
							.format("Position: %s>%s, Progress: %d%%.\n",
									info.area.get(Integer
											.parseInt(info.front.areaId)).areaName,
									info.front.floorId,
									info.area.get(Integer
											.parseInt(info.front.areaId)).exploreProgress);
					Go.log(my_state, true);
				} else {
					Go.log("Something wrong@GOTO_FLOOR.", !Info.Nolog);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GOTO_MAIN_MENU:
			try {
				switch (GotoMainMenu.run()) {
				case 1:
					AddTask(Info.EventType.gotoFloor);
					break;
				case 0:
					Go.log("Something wrong@GOTO_MAIN_MENU.", !Info.Nolog);
					break;
				case 2:
					AddUrgentTask(Info.EventType.needAPBCInfo);
					break;
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case PRIVATE_FAIRY_BATTLE:
			try {
				if (RecFairyDianzan.run()) {
					if (PrivateFairyBattle.run()) {
						String result = "";
						switch (PrivateFairyBattle.FairyBattleResult) {
						case escape:
							result = "Too Late";
							break;
						case lose:
							result = "Lose";
							break;
						case win:
							result = "Win";
							break;
						default:
							result = "Unknown";
							break;
						}
						String str = String
								.format("Private Fairy Battle, name: %s, Lv: %d, Hp: %d/%d, Finder: %s, bc: %d/%d, ap: %d/%d, ticket: %d, %s",
										info.pfairy.FairyName,
										info.pfairy.FairyLevel,
										info.pfairy.FairyHp,
										info.pfairy.FairyHpMax,
										info.FairySelectUserList
												.get(info.pfairy.UserId).userName,
										info.bc, info.bcMax, info.ap,
										info.apMax, info.ticket, result);
						if (info.gather != -1)
							str += String.format(", gather=%d", info.gather);
						str += ".\n";
						str += String
								.format("Card Deck Info: %s, Custom Name: %s, Number: %s, BC: %d.\n",
										info.CurrentDeck.DeckName,
										info.CurrentDeck.CustomDeckName,
										info.CurrentDeck.No,
										info.CurrentDeck.BC);
						Go.log(str, true);
					} else {
						Go.log("Something wrong@PrivateFairyBattle",
								!Info.Nolog);
					}
				} else {
					Go.log("Something wrong@RecFairyDianzan", !Info.Nolog);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case EXPLORE:
			try {
				if (Explore.run()) {
					Go.log(String
							.format("Explore[%s>%s]: AP: %d, Gold+%s, Exp+%s, Progress:%s, Result: %s.",
									info.area.get(Integer
											.parseInt(info.front.areaId)).areaName,
									info.front.floorId, info.ap,
									info.ExploreGold, info.ExploreExp,
									info.ExploreProgress, info.ExploreResult),
							true);
				} else {
					Go.log("Something wrong@Explore", !Info.Nolog);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GUILD_TOP:
			try {
				switch (GuildTop.run()) {
				case 2:// 需要打，而且要显示
					Go.log(String
							.format("Guild Fairy Info: Max HP: %d, Our Team: %d(%d%%), Rival's Team: %d(%d%%). ",
									info.gfbforce.total, info.gfbforce.own,
									info.gfbforce.ownscale,
									info.gfbforce.rival,
									info.gfbforce.rivalscale)
							+ String.format(
									"Ticket: %d, Score Multiple: %.1f, Defeat Combo: %d.",
									info.ticket,
									info.gfbforce.attack_compensation,
									info.gfbforce.chain_counter), true);
					Process.info.gfairy.No = Info.PublicFairyBattle.No;
					Process.info.CurrentDeck = Info.PublicFairyBattle;
					Long currentGuildBattleTime = System.currentTimeMillis();
					if (currentGuildBattleTime - lastGuildBattleTime < 10000)
						Thread.sleep(10000 + lastGuildBattleTime
								- currentGuildBattleTime);
					if (GuildBattle.run()) {
						String result = "";
						switch (GuildBattle.FairyBattleResult) {
						case escape:
							result = "Too Late";
							break;
						case lose:
							result = "Lose";
							break;
						case win:
							result = "Win";
							break;
						default:
							result = "Unknown";
							break;
						}
						String str = String
								.format("Guild Fairy Battle, name: %s, Lv: %d, bc: %d/%d, ap: %d/%d, ticket: %d, week:%s, %s.\n",
										info.gfairy.FairyName,
										info.gfairy.FairyLevel, info.bc,
										info.bcMax, info.ap, info.apMax,
										info.ticket, info.week, result)
								+ String.format(
										"Card Deck Info: %s, Custom Name: %s, Number: %s, BC: %d.",
										info.CurrentDeck.DeckName,
										info.CurrentDeck.CustomDeckName,
										info.CurrentDeck.No,
										info.CurrentDeck.BC);
						Go.log(str, true);
						if (Process.info.ticket > 0) // 连续出击直至获胜
							Process.AddUrgentTask(Info.EventType.guildTop);
						if (Info.FairyBattleFirst)
							Process.AddUrgentTask(Info.EventType.getFairyList);
					} else {
						Go.log("Something wrong@GUILD_BATTLE.", !Info.Nolog);
					}
					lastGuildBattleTime = System.currentTimeMillis();
					break;
				case 1:// 不能打，但需要显示
					Go.log(String
							.format("Guild Fairy Info: Max HP: %d, Our Team: %d(%d%%), Rival's Team: %d(%d%%). ",
									info.gfbforce.total, info.gfbforce.own,
									info.gfbforce.ownscale,
									info.gfbforce.rival,
									info.gfbforce.rivalscale)
							+ String.format(
									"Ticket: %d, Score Multiple: %.1f, Defeat Combo: %d.",
									info.ticket,
									info.gfbforce.attack_compensation,
									info.gfbforce.chain_counter), !Info.Nolog);
					break;
				case 3:// 需要重新获取
					Process.info.gfbforce = new GuildFairyBattleForce();
					Process.AddUrgentTask(Info.EventType.guildTop);
					break;
				case 0:// 什么都不做
					if (info.NoFairy)
						Go.log("Night Mode.", !Info.Nolog);
					break;
				}

			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GET_FAIRY_REWARD:
			try {
				if (GetFairyReward.run()) {
					Go.log(ErrorData.text, true);
					ErrorData.clear();
					AddUrgentTask(Info.EventType.needAPBCInfo);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case LV_UP:
			try {
				if (LvUp.run()) {
					Go.log(String.format("Level UP! AP:%d BC:%d", info.apMax,
							info.bcMax), true);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case SELL_CARD:
			try {
				if (SellCard.run()) {
					Go.log(ErrorData.text, true);
					ErrorData.clear();
				} else {
					Go.log("Something wrong@SELL_CARD.", !Info.Nolog);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case AUTO_MEDICINE:
			try {
				if (AutoMedicine.run()) {
					Go.log(ErrorData.text, true);
					ErrorData.clear();
					Go.log(String
							.format("Bottles: FullAp:%d, HalfAp:%d/Today:%d, FullBc:%d, HalfBc:%d/Today:%d",
									info.fullAp, info.halfAp, info.halfApToday,
									info.fullBc, info.halfBc, info.halfBcToday),
							true);
				} else {
					Go.log("Something wrong@MEDICINE.", !Info.Nolog);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GET_CARD_DECK:
			try {
				if (GetCardDeck.run()) {
					Go.log("Succeed to get card deck info.", true);
				} else {
					Go.log("Something wrong@GET_CARD_DECK.", !Info.Nolog);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case REWARD_BOX:
			try {
				if (RewardBox.run()) {
					Go.log("Succeed to get reward box info.", !Info.Nolog);
				} else {
					Go.log("Something wrong@REWARD_BOX.", !Info.Nolog);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GET_REWARDS:
			try {
				if (GetRewards.run()) {
					Go.log(ErrorData.text, true);
					ErrorData.clear();
				} else {
					Go.log("Something wrong@GET_REWARDS.", !Info.Nolog);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case NOTHING:
			Go.log("Nothing to do, have a rest.", !Info.Nolog);
			break;
		default:
			break;

		}
	}

	public static Document ParseXMLBytes(byte[] in) throws Exception {
		ByteArrayInputStream bais = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			bais = new ByteArrayInputStream(in);
			Document document = builder.parse(bais);
			return document;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static long getRandom(long time1, long time2) {
		return Math.round(time1 + (time2 - time1) * Math.random());
	}

}
