package walker;

import java.util.ArrayList;
import java.util.List;

import walker.Info.TimeoutEntry;
import action.ActionRegistry;
import action.Explore;
import action.GetFloorInfo;
import action.GotoFloor;
import action.Login;
import action.LvUp;
import action.PFBGood;
import action.PrivateFairyBattle;
import action.RecvPFBGood;
import action.SellCard;
import action.ActionRegistry.Action;

public class Profile2 {
	
	public Profile2() {
	}
	
	public void auto() throws Exception {
		try {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) {
				rescue();
			} else {
				long start = System.currentTimeMillis();
				execute(Think.doIt(getPossibleAction()));
				long delta = System.currentTimeMillis() - start;
				if (delta < 15000) Thread.sleep(15000 - delta);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	private void rescue() {
		Go.log(ErrorData.currentErrorType.toString());
		switch (ErrorData.currentDataType) {
		case bytes:
			Go.log(new String(ErrorData.bytes));
			break;
		case text:
			Go.log(new String(ErrorData.text));
			break;
		default:
			break;
		}
		ErrorData.clear();
	}
	
	private List<ActionRegistry.Action> getPossibleAction() {
		ArrayList<ActionRegistry.Action> result = new ArrayList<ActionRegistry.Action>();
		if (Process.info.events.size() != 0) {
			switch(Process.info.events.peek()) {
			case notLoggedIn:
			case cookieOutOfDate:
				result.add(ActionRegistry.Action.LOGIN);
				break;
			case fairyTransform:
				Go.log("Rare Fairy Appear");
			case privateFairyAppear:
			case fairyCanBattle:
				result.add(ActionRegistry.Action.PRIVATE_FAIRY_BATTLE);
				break;
			case innerMapJump:
				Go.log("Map Status Changed!");
			case needFloorInfo:	
				result.add(ActionRegistry.Action.GET_FLOOR_INFO);
				break;
			case cardFull:
				result.add(ActionRegistry.Action.SELL_CARD);
				break;
			case needAPBCInfo:
				result.add(ActionRegistry.Action.GOTO_FLOOR);
				break;
			case fairyReward:
				result.add(ActionRegistry.Action.GET_FAIRY_REWARD);
				break;
			case levelUp:
				result.add(Action.LV_UP);
				break;
			case PFBGood:
				result.add(Action.PFB_GOOD);
				break;
			case recvPFBGood:
				result.add(Action.RECV_PFB_GOOD);
				break;
			case gotoFloor:
				result.add(Action.GOTO_FLOOR);
			default:
				Go.log("Profile2 Ignore: " + Process.info.events.peek());
				break;
			}
			Process.info.events.pop();
			return result;
		}
		ArrayList<TimeoutEntry> te = Process.info.CheckTimeout();
		for (TimeoutEntry e : te) {
			switch (e) {
			case apbc:
				Process.info.events.push(Info.EventType.needAPBCInfo);
				break;
			case login:
				Process.info.events.push(Info.EventType.cookieOutOfDate);
				break;
			case map:
				Process.info.events.push(Info.EventType.needFloorInfo);
				break;
			case fairy:
			case reward:
			default:
				break;
			}				
		}
		result.add(ActionRegistry.Action.EXPLORE);
		return result;
	}
	
	private void execute(ActionRegistry.Action action) throws Exception {
		switch (action) {
		case LOGIN:
			try {
				if (Login.run()) {
					Go.log(String.format("User: %s, AP: %d/%d, BC: %d/%d, Card: %d/%d, ticket: %d",
							Process.info.username, Process.info.ap, Process.info.apMax, Process.info.bc, Process.info.bcMax,
							Process.info.cardList.size(), Process.info.cardMax, Process.info.ticket));	
					Process.info.events.push(Info.EventType.needFloorInfo);
				} else {
					Process.info.events.push(Info.EventType.notLoggedIn);
				}
			} catch (Exception ex) {
				Process.info.events.push(Info.EventType.notLoggedIn);
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
					throw ex;
				}
			}
			break;
		case GET_FLOOR_INFO:
			try {
				if (GetFloorInfo.run()) {
					Go.log(String.format("Area(%d) Front: %s>%s@c=%d", 
							Process.info.area.size(), 
							Process.info.area.get(Integer.parseInt(Process.info.front.areaId)).areaName, 
							Process.info.front.floorId, 
							Process.info.front.cost));
				}
				
			} catch (Exception ex) {
				if (ex.getMessage().equals("302")) {
					Process.info.events.push(Info.EventType.innerMapJump);
					ErrorData.clear();
				} else {
					if (ErrorData.currentErrorType == ErrorData.ErrorType.none) throw ex;
				}
			}
			break;
		case GOTO_FLOOR:
			try {
				if (GotoFloor.run()) {
					Go.log(String.format("Goto: AP: %d/%d, BC: %d/%d, Front:%s>%s",
							Process.info.ap, Process.info.apMax, Process.info.bc, Process.info.bcMax,
							Process.info.area.get(Integer.parseInt(Process.info.front.areaId)).areaName, 
							Process.info.front.floorId));	
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
					if (!Process.info.events.empty()) {
						switch (Process.info.events.peek()) {
						case fairyBattleEnd:
							result = "Too Late";
							Process.info.events.pop();
							break;
						case fairyBattleLose:
							result = "Lose";
							Process.info.events.pop();
							break;
						case fairyBattleWin:
							result = "Win";
							Process.info.events.pop();
							break;
						default:
							break;
						}
					}
					String str = String.format("PFB name=%s, Lv: %s, bc: %d/%d, ap: %d/%d, ticket: %d, %s",
							Process.info.fairy.FairyName, Process.info.fairy.FairyLevel, Process.info.bc, Process.info.bcMax, Process.info.ap, Process.info.apMax, 
							Process.info.ticket, result);
					if (Process.info.gather != -1) str += String.format(", gather=%d", Process.info.gather);
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
					Go.log(String.format("Explore: AP: %d, Gold+%s, Exp+%s, Progress:%s, Result: %s.", Process.info.ap,
							Process.info.ExploreGold, Process.info.ExploreExp, Process.info.ExploreProgress, Process.info.ExploreResult));
				} else {
					
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
		default:
			break;
		}
	}
	
}
