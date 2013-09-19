package walker;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.Network;

import org.w3c.dom.Document;

import walker.Info.TimeoutEntry;
import action.ActionRegistry.Action;
import action.AddArea;
import action.Explore;
import action.GetFairyList;
import action.GetFairyReward;
import action.GetFloorInfo;
import action.GotoFloor;
import action.GuildBattle;
import action.GuildTop;
import action.Login;
import action.LvUp;
import action.PFBGood;
import action.PrivateFairyBattle;
import action.RecvPFBGood;
import action.SellCard;

public class Process {
	public static Info info;
	public static Network network;
	
	public Process() {
		info = new Info();
		network = new Network();
	}
	
	public void auto() throws Exception {
		try {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) {
				rescue();
			} else {
				long start = System.currentTimeMillis();
				execute(Think.doIt(getPossibleAction()));
				long delta = System.currentTimeMillis() - start;
				if (delta < 5000) Thread.sleep(5000 - delta);
				if (info.events.empty() && info.NoFairy) Thread.sleep(600000); // 半夜速度慢点
			}
		} catch (Exception ex) {
			throw ex;
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
			switch(info.events.pop()) {
			case notLoggedIn:
			case cookieOutOfDate:
				result.add(Action.LOGIN);
				break;
			case fairyTransform:
				Go.log("Rare Fairy Appear");
			case privateFairyAppear:
			case fairyCanBattle:
				result.add(Action.PRIVATE_FAIRY_BATTLE);
				break;
			case fairyReward:
				if (info.ticket > 0) {
					result.add(Action.GUILD_TOP);
				} else if (info.ticket < 0) {
					Go.log("Keep reward");
				} else {
					result.add(Action.GET_FAIRY_REWARD);
				}
				break;
			case innerMapJump:
				Go.log("Map Status Changed!");
			case needFloorInfo:	
				result.add(Action.GET_FLOOR_INFO);
				break;
			case areaComplete:
				result.add(Action.ADD_AREA);
				break;
			case cardFull:
				result.add(Action.SELL_CARD);
				break;
			case fairyAppear:
				result.add(Action.GET_FAIRY_LIST);
				break;
			case getFairyReward:
				break;
			case guildBattle:
				result.add(Action.GUILD_BATTLE);
				break;
			case guildTopRetry:
			case guildTop:
			case ticketFull:
				result.add(Action.GUILD_TOP);
				break;
			case needAPBCInfo:
				result.add(Action.GOTO_FLOOR);
				break;
			case levelUp:
				if (Info.AutoAddp == false) {
					Go.log("自动加点已关闭");
				} else {
					result.add(Action.LV_UP);				
				}
			case fairyBattleEnd:
			case fairyBattleLose:
			case fairyBattleWin:			
				break;
			case PFBGood:
				result.add(Action.PFB_GOOD);
				break;
			case recvPFBGood:
				result.add(Action.RECV_PFB_GOOD);
				break;
			case gotoFloor:
				result.add(Action.GOTO_FLOOR);
				break;
			}
			if (!result.isEmpty())	return result;
		}
		ArrayList<TimeoutEntry> te = info.CheckTimeout();
		for (TimeoutEntry e : te) {
			switch (e) {
			case apbc:
				Process.info.events.push(Info.EventType.needAPBCInfo);
				break;
			case fairy:
				Process.info.events.push(Info.EventType.fairyAppear);
				break;
			case login:
				Process.info.events.push(Info.EventType.cookieOutOfDate);
				break;
			case map:
				Process.info.events.push(Info.EventType.needFloorInfo);
				break;
			case ticket:
				if (info.ticket > 0) Process.info.events.push(Info.EventType.cardFull);
				break;
			case reward:
			default:
				break;
			}				
		}
		result.add(Action.EXPLORE);
		// result.add(Action.GOTO_FLOOR);
		if (!Process.info.OwnFairyBattleKilled){
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			result.add(Action.GET_FAIRY_LIST);
		}
		if (Info.FairyBattleFirst)
			result.add(Action.GET_FAIRY_LIST);
		return result;
	}
	
	private void execute(Action action) throws Exception {
		switch (action) {
		case LOGIN:
			try {
				if (Login.run()) {
					Go.log(String.format("User: %s, AP: %d/%d, BC: %d/%d, Card: %d/%d, ticket: %d",
							info.username, info.ap, info.apMax, info.bc, info.bcMax,
							info.cardList.size(), info.cardMax, info.ticket));	
					info.events.push(Info.EventType.needFloorInfo);
				} else {
					info.events.push(Info.EventType.notLoggedIn);
				}
			} catch (Exception ex) {
				info.events.push(Info.EventType.notLoggedIn);
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
					throw ex;
				}
			}
			break;
		case GET_FLOOR_INFO:
			try {
				if (GetFloorInfo.run()) {
					if (Process.info.AllClear) Process.info.front = Process.info.floor.get(1);
					Go.log(String.format("Area(%d) Front: %s>%s@c=%d", 
							info.area.size(), 
							info.area.get(Integer.parseInt(info.front.areaId)).areaName, 
							info.front.floorId, 
							info.front.cost));
				}
				
			} catch (Exception ex) {
				if (ex.getMessage() != null && ex.getMessage().equals("302")) {
					info.events.push(Info.EventType.innerMapJump);
					ErrorData.clear();
				} else {
					if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
				}
			}
			break;
		case ADD_AREA:
			try {
				if (AddArea.run()) {
					Go.log(String.format("Area(%d) Front: %s>%s@c=%d", 
							info.area.size(), 
							info.area.get(Integer.parseInt(info.front.areaId)).areaName, 
							info.front.floorId, 
							info.front.cost));
				}
				
			} catch (Exception ex) {
				if (ex.getMessage().equals("302")) {
					info.events.push(Info.EventType.innerMapJump);
					ErrorData.clear();
				} else {
					if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
				}
			}
			break;
		case GET_FAIRY_LIST:
			try {
				if (GetFairyList.run()) {
					if (!info.events.empty() && info.events.peek() == Info.EventType.fairyCanBattle) {
						Go.log("Other's fairy found!");
					} else {
						Go.log("No fairy found.");
					}
				} else {
					if (Info.FairyBattleFirst) info.events.push(Info.EventType.fairyAppear);
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.ConnectionError) {
					info.events.push(Info.EventType.fairyAppear); // 再次检测
					Go.log("Retry@GetFairyList");
					ErrorData.clear();
				} else if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
					throw ex;
				}
			}
			
			break;
		case GOTO_FLOOR:
			try {
				if (GotoFloor.run()) {
					Go.log(String.format("Goto: AP: %d/%d, BC: %d/%d, Front:%s>%s",
							info.ap, info.apMax, info.bc, info.bcMax,
							info.area.get(Integer.parseInt(info.front.areaId)).areaName, 
							info.front.floorId));	
				} else {
					
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
				
			}
			
			break;
		case PRIVATE_FAIRY_BATTLE:
			try {
				if (PrivateFairyBattle.run()) {
					String result = "";
					if (!info.events.empty()) {
						switch (info.events.peek()) {
						case fairyBattleEnd:
							result = "Too Late";
							info.events.pop();
							break;
						case fairyBattleLose:
							result = "Lose";
							info.events.pop();
							break;
						case fairyBattleWin:
							result = "Win";
							info.events.pop();
							break;
						default:
							break;
						}
					}
					String str = String.format("PFB name=%s, Lv: %s, bc: %d/%d, ap: %d/%d, ticket: %d, %s",
							info.fairy.FairyName, info.fairy.FairyLevel, info.bc, info.bcMax, info.ap, info.apMax, 
							info.ticket, result);
					if (info.gather != -1) str += String.format(", gather=%d", info.gather);
					Go.log(str);
				} else {
					
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
			}
			break;
		case EXPLORE:
			try {
				if (Explore.run()) {
					Go.log(String.format("Explore[%s>%s]: AP: %d, Gold+%s, Exp+%s, Progress:%s, Result: %s.", 
							info.area.get(Integer.parseInt(info.front.areaId)).areaName, info.front.floorId,info.ap,
							info.ExploreGold, info.ExploreExp, info.ExploreProgress, info.ExploreResult));
				} else {
					
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
			}
			break;
		case GUILD_BATTLE:
			try {
				if (GuildBattle.run()) {
					String result = "";
					if (!info.events.empty()) {
						switch (info.events.peek()) {
						case guildTopRetry:
							result = "Too Late";
							break;
						case fairyBattleLose:
							result = "Lose";
							info.events.pop();
							break;
						case fairyBattleWin:
							result = "Win";
							info.events.pop();
							break;
						default:
							break;
						}
					}
					String str = String.format("PFB name=%s, Lv: %s, bc: %d/%d, ap: %d/%d, ticket: %d, week:%s, %s",
							info.gfairy.FairyName, info.gfairy.FairyLevel, info.bc, info.bcMax, info.ap, info.apMax, 
							info.ticket, info.week, result);
					Thread.sleep(5000);
					Go.log(str);
				} else {
					
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
			}
			break;
		case GUILD_TOP:
			try {
				if (GuildTop.run()) {
					// nothing to do
				} else {
					if (info.NoFairy) Go.log("Night Mode");
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
			}
			break;
		case GET_FAIRY_REWARD:
			try {
				if (GetFairyReward.run()) {
					Go.log(ErrorData.text);
					ErrorData.clear();
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
			}
			break;
		case LV_UP:
			try {
				if (LvUp.run()) {
					Go.log(String.format("Level UP! AP:%d BC:%d", Process.info.apMax, Process.info.bcMax));
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
			}
			break;
		
		case SELL_CARD:
			try {
				if (SellCard.run()) {
					Go.log(ErrorData.text);
					ErrorData.clear();
				} else {
					Go.log("Something wrong");
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
			}
			break;
		case PFB_GOOD:
			try {
				if (PFBGood.run()) {
					Go.log(ErrorData.text);
					ErrorData.clear();
				} else {
					Go.log("Something wrong");
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
				
			}
			break;
		case RECV_PFB_GOOD:
			try {
				if (RecvPFBGood.run()) {
					Go.log(ErrorData.text);
					ErrorData.clear();
				} else {
					Go.log("Something wrong");
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
			}
			break;
		case NOTHING:
			Thread.sleep(30000); // 无事可做休息30秒
			break;
		default:
			break;
		
		}
	}
	
	public static Document ParseXMLBytes(byte[] in) throws Exception {
		ByteArrayInputStream bais = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			bais = new ByteArrayInputStream(in);
			Document document = builder.parse(bais);
			return document;
		} catch (Exception e) {
			throw e;
		}
	}
	
	
}
