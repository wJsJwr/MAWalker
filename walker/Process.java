package walker;

import java.io.ByteArrayInputStream;
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
import action.GetFairyList;
import action.GetFairyReward;
import action.GetFloorInfo;
import action.GotoFloor;
import action.GotoMainMenu;
import action.GuildBattle;
import action.GuildTop;
import action.Login;
import action.LvUp;
import action.PrivateFairyBattle;
import action.SellCard;

public class Process {
	public static Info info;
	public static Network network;
	public static Timer TaskTimer;

	public Process() {
		info = new Info();
		network = new Network();
		TaskTimer = new Timer();
	}

	public void run() {

		while (true) {
			try {
				auto();
			} catch (Exception ex) {
				Go.log(ex.getMessage());
				Process.AddUrgentTask(Info.EventType.cookieOutOfDate);
				Go.log("Restart");
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
				if (delta < 5000)
					Thread.sleep(5000 - delta);
				if (Info.nightModeSwitch && info.events.empty() && info.NoFairy)
					Thread.sleep(60000); // 半夜速度慢点,等待60s
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
		}, 0, 3 * 1000);// 3s
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.guildTop);
			}
		}, 0, 30 * 1000);// 30s
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.autoExplore);
			}
		}, 0, 30 * 1000l);// 30s
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.needFloorInfo);
			}
		}, 0, 3 * 60 * 1000);// 3min
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.needAPBCInfo);
			}
		}, 0, 3 * 60 * 1000);// 3min
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddTask(Info.EventType.autoMedicine);
			}
		}, 0, 3 * 60 * 1000); // 3min
		Calendar myCal = Calendar.getInstance();
		if(myCal.get(Calendar.HOUR_OF_DAY)>=1) {
			int date = myCal.get(Calendar.DAY_OF_YEAR);
			if(date == 365)
				myCal.set(Calendar.DAY_OF_YEAR, 1);
			else
				myCal.set(Calendar.DAY_OF_YEAR, date + 1);
		}
		myCal.set(Calendar.HOUR_OF_DAY,1);
		myCal.set(Calendar.MINUTE,0);
		myCal.set(Calendar.SECOND,0);
		TaskTimer.schedule(new TimerTask() {
			public void run() {
				AddUrgentTask(Info.EventType.notLoggedIn);
			}
		}, myCal.getTime());//relogin at 1:00
	}

	public static void AddTask(Info.EventType _Task) {
		if (!Process.info.events.contains(_Task)) {
			Process.info.events.add(0, _Task);
			if (Info.Debug)
				Go.log(String.format("Add Task: %s", _Task));
		}
	}

	public static boolean AddUrgentTask(Info.EventType Task) {
		if (!Process.info.events.contains(Task)) {
			Process.info.events.push(Task);
			if (Info.Debug)
				Go.log(String.format("Add Urgent Task: %s", Task));
			return true;
		} else {
			return false;
		}
	}

	private void rescue() {
		Go.log(ErrorData.currentErrorType.toString());
		switch (ErrorData.currentDataType) {
		case bytes:
			if (ErrorData.bytes != null) {
				Go.log(new String(ErrorData.bytes));
			} else {
				Go.log("Set type to byte, but no message");
			}
			break;
		case text:
			Go.log(ErrorData.text);
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
			case cookieLogin://cookie登陆
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
				Go.log("Map Status Changed!");
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
			case guildBattle:// 强敌战
				result.add(Action.GUILD_BATTLE);
				break;
			case guildTopRetry:// 强敌战结束重新获取
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
				if (Info.AutoAddp == false) {
					if (Info.Debug)
						Go.log("Auto Adding points is closed.");
				} else {
					result.add(Action.LV_UP);
				}
			case autoMedicine:
				result.add(Action.AUTO_MEDICINE);
				break;
			case autoExplore:
				result.add(Action.EXPLORE);
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
				if (CookieLogin.run()) {
					Go.log(String
							.format("Cookie Login: User: %s, AP: %d/%d, BC: %d/%d, Card: %d/%d, ticket: %d, sessionId: %s",
									info.username, info.ap, info.apMax,
									info.bc, info.bcMax, info.cardList.size(),
									info.cardMax, info.ticket, Info.sessionId));
					Process.info.events.clear();
					AddUrgentTask(Info.EventType.needFloorInfo);
					AddTimerTasks();
				} else {
					Go.log(ErrorData.text);
					Go.log("Cookie Login Failed, waiting to login with username and password.");
					ErrorData.clear();
					AddUrgentTask(Info.EventType.notLoggedIn);
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
				if (Login.run()) {
					Go.log(String
							.format("Normal Login: User: %s, AP: %d/%d, BC: %d/%d, Card: %d/%d, ticket: %d, sessionId: %s",
									info.username, info.ap, info.apMax,
									info.bc, info.bcMax, info.cardList.size(),
									info.cardMax, info.ticket, Info.sessionId));
					Process.info.events.clear();
					AddUrgentTask(Info.EventType.needFloorInfo);
					AddTimerTasks();
				} else {
					AddUrgentTask(Info.EventType.notLoggedIn);
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
					if (Process.info.AllClear)
						Process.info.front = Process.info.floor.get(1);
					if (!Info.Nolog)
						Go.log(String.format("Area(%d) Front: %s>%s@c=%d",
								info.area.size(), info.area.get(Integer
										.parseInt(info.front.areaId)).areaName,
								info.front.floorId, info.front.cost));
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

					if (!Info.Nolog)
						Go.log(String.format("Area(%d) Front: %s>%s@c=%d",
								info.area.size(), info.area.get(Integer
										.parseInt(info.front.areaId)).areaName,
								info.front.floorId, info.front.cost));
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
						if (!Info.Nolog)
							Go.log("Other's fairy found!");
					} else {
						if (!Info.Nolog)
							Go.log("No fairy found.");
					}
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.ConnectionError) {
					AddUrgentTask(Info.EventType.getFairyList); // 再次检测
					Go.log("Connection error.Retry to get fairy list.");
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
					Go.log("Something wrong@DIANZAN.");
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GOTO_FLOOR:
			try {
				if (GotoFloor.run()) {
					String my_state;
					if (!Info.Nolog) {
						my_state = String
								.format("User Name: %s, AP: %d/%d, BC: %d/%d, Level: %d, Exp to level up: %d, Gold: %d, Friendship point: %d.\n",
										info.username, info.ap, info.apMax,
										info.bc, info.bcMax, info.lv, info.exp,
										info.gold, info.friendshippoint);
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
						Go.log(my_state);
					}
				} else {
					Go.log("Something wrong@GOTO_FLOOR.");
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GOTO_MAIN_MENU:
			try {
				if (GotoMainMenu.run()){
					AddTask(Info.EventType.gotoFloor);					
				} else {
					Go.log("Something wrong@GOTO_MAIN_MENU.");
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case PRIVATE_FAIRY_BATTLE:
			try {
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
									info.fairy.FairyName,
									info.fairy.FairyLevel,
									info.fairy.FairyHp,
									info.fairy.FairyHpMax,
									info.FairySelectUserList
											.get(info.fairy.UserId).userName,
									info.bc, info.bcMax, info.ap, info.apMax,
									info.ticket, result);
					if (info.gather != -1)
						str += String.format(", gather=%d", info.gather);
					str += ".\n";
					if (Info.Debug)
						str += String.format(
								"Fairy Info: SerialID: %s, UserID: %s.\n",
								info.fairy.SerialId, info.fairy.UserId);
					str += String.format(
							"Card Deck Info: %s, Number: %s, BC: %d.\n",
							info.CurrentDeck.DeckName, info.CurrentDeck.No,
							info.CurrentDeck.BC);
					Go.log(str);
				} else {

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
									info.ExploreProgress, info.ExploreResult));
				} else {

				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GUILD_BATTLE:
			try {
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
									"Card Deck Info: %s, Number: %s, BC: %d.",
									info.CurrentDeck.DeckName,
									info.CurrentDeck.No, info.CurrentDeck.BC);
					Thread.sleep(5000);
					Go.log(str);
				} else {

				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GUILD_TOP:
			try {
				if (GuildTop.run()) {
					if (!Info.Nolog)
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
										info.gfbforce.chain_counter));
				} else {
					if (info.NoFairy)
						if (!Info.Nolog)
							Go.log("Night Mode.");
				}

			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case GET_FAIRY_REWARD:
			try {
				if (GetFairyReward.run()) {
					Go.log(ErrorData.text);
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
							info.bcMax));
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case SELL_CARD:
			try {
				if (SellCard.run()) {
					Go.log(ErrorData.text);
					ErrorData.clear();
				} else {
					Go.log("Something wrong@SELL_CARD.");
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case AUTO_MEDICINE:
			try {
				if (AutoMedicine.run()) {
					Go.log(ErrorData.text);
					ErrorData.clear();
					Go.log(String
							.format("Bottles: FullAp:%d, HalfAp:%d/Today:%d, FullBc:%d, HalfBc:%d/Today:%d",
									info.fullAp, info.halfAp, info.halfApToday,
									info.fullBc, info.halfBc, info.halfBcToday));
				} else {
					Go.log("Something wrong@MEDICINE.");
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			break;
		case NOTHING:
			if (!Info.Nolog)
				Go.log("Nothing to do, have a rest.");
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

}
