package walker;

import info.Card;

import java.util.List;

import action.ActionRegistry.Action;

public class Think {
	private static final String AP_HALF = "101";
	private static final String BC_HALF = "111";
	private static final String AP_FULL = "1";
	private static final String BC_FULL = "2";

	public static Action doIt(List<Action> possible) {

		if (possible.size() > 0) {
			switch (possible.get(0)) {
			case LOGIN:
				return Action.LOGIN;
			case COOKIELOGIN:
				return Action.COOKIELOGIN;
			case ADD_AREA:
				return Action.ADD_AREA;
			case GET_FLOOR_INFO:
				return Action.GET_FLOOR_INFO;
			case GET_FAIRY_LIST:
				return Action.GET_FAIRY_LIST;
			case GOTO_MAIN_MENU:
				return Action.GOTO_MAIN_MENU;
			case GOTO_FLOOR:
				return Action.GOTO_FLOOR;
			case FAIRY_DIANZAN:
				return Action.FAIRY_DIANZAN;
			case PRIVATE_FAIRY_BATTLE:
				Process.info.pfairy = Process.info.PrivateFairyList.poll();
				if (canBattle())
					return Action.PRIVATE_FAIRY_BATTLE;
				break;
			case EXPLORE:
				if (canExplore())
					return Action.EXPLORE;
				else
					break;
			case GUILD_TOP:
				return Action.GUILD_TOP;
			case GET_FAIRY_REWARD:
				return Action.GET_FAIRY_REWARD;
			case NOTHING:
				break;
			case SELL_CARD:
				if (cardsToSell())
					return Action.SELL_CARD;
				break;
			case LV_UP:
				decideUpPoint();
				return Action.LV_UP;
			case AUTO_MEDICINE:
				if (decideAutoMedicine())
					return Action.AUTO_MEDICINE;
				break;
			case GET_CARD_DECK:
				return Action.GET_CARD_DECK;
			default:
				break;
			}
		}
		return Action.NOTHING;
	}

	private static boolean decideAutoMedicine() {
		if (Info.autoUseAp) {
			if (Process.info.ap < Info.autoApLow) {
				switch (Info.autoApType) {
				case ALL:
					if (Process.info.halfApToday > 0 && Process.info.halfAp > 0) {
						Process.info.toUse = AP_HALF;
						return true;
					} else {
						if (Process.info.fullAp > Info.autoApFullLow) {
							Process.info.toUse = AP_FULL;
							return true;
						}
					}
					break;
				case FULL_ONLY:
					if (Process.info.fullAp > Info.autoApFullLow) {
						Process.info.toUse = AP_FULL;
						return true;
					}
					break;
				case HALF_ONLY:
					if (Process.info.halfApToday > 0 && Process.info.halfAp > 0) {
						Process.info.toUse = AP_HALF;
						return true;
					}
					break;
				default:
					break;

				}
			}
		}
		if (Info.autoUseBc) {
			if (Process.info.bc < Info.autoBcLow) {
				switch (Info.autoBcType) {
				case ALL:
					if (Process.info.halfBcToday > 0 && Process.info.halfBc > 0) {
						Process.info.toUse = BC_HALF;
						return true;
					} else {
						if (Process.info.fullBc > Info.autoBcFullLow) {
							Process.info.toUse = BC_FULL;
							return true;
						}
					}
					break;
				case FULL_ONLY:
					if (Process.info.fullBc > Info.autoBcFullLow) {
						Process.info.toUse = BC_FULL;
						return true;
					}
					break;
				case HALF_ONLY:
					if (Process.info.halfBcToday > 0 && Process.info.halfBc > 0) {
						Process.info.toUse = BC_HALF;
						return true;
					}
					break;
				default:
					break;

				}
			}
		}
		return false;
	}

	private static boolean canBattle() {
		if (Process.info.pfairy.ForceKill) {
			if (Process.info.bc >= Info.KillFairyDeck.BC
					&& Process.info.bc > Process.info.bcMax
							* Info.KillFairyScale) {
				Process.info.CurrentDeck = Info.KillFairyDeck;
			} else {
				return false;
			}
		} else {
			switch (Process.info.pfairy.Type) {
			case 4:
				if (Info.FriendFairyBattleNormal.UseBcInsufficientDeck) {
					if (Process.info.bc >= Info.FriendFairyBattleNormal.BC
							&& Process.info.bc >= Process.info.bcMax
									* Info.FriendFairyBattleNormal.BcInsufficientScale
							&& Process.info.pfairy.FairyHp >= Info.FriendFairyBattleNormal.BcInsufficientHpMax) {
						Process.info.CurrentDeck = Info.FriendFairyBattleNormal;
					} else if (Process.info.bc >= Info.BcInsufficientDeck.BC) {
						Process.info.CurrentDeck = Info.BcInsufficientDeck;
					} else {
						return false;
					}
				} else {
					if (Process.info.bc >= Info.FriendFairyBattleNormal.BC) {
						Process.info.CurrentDeck = Info.FriendFairyBattleNormal;
					} else {
						return false;
					}
				}
				break;
			case 5:
				if (Info.FriendFairyBattleRare.UseBcInsufficientDeck) {
					if (Process.info.bc >= Info.FriendFairyBattleRare.BC
							&& Process.info.bc >= Process.info.bcMax
									* Info.FriendFairyBattleRare.BcInsufficientScale
							&& Process.info.pfairy.FairyHp >= Info.FriendFairyBattleRare.BcInsufficientHpMax) {
						Process.info.CurrentDeck = Info.FriendFairyBattleRare;
					} else if (Process.info.bc >= Info.BcInsufficientDeck.BC) {
						Process.info.CurrentDeck = Info.BcInsufficientDeck;
					} else {
						return false;
					}
				} else {
					if (Process.info.bc >= Info.FriendFairyBattleRare.BC) {
						Process.info.CurrentDeck = Info.FriendFairyBattleRare;
					} else {
						return false;
					}
				}
				break;
			case 6:
				if (Info.PrivateFairyBattleNormal.UseBcInsufficientDeck) {
					if (Process.info.bc >= Info.PrivateFairyBattleNormal.BC
							&& Process.info.bc >= Process.info.bcMax
									* Info.PrivateFairyBattleNormal.BcInsufficientScale
							&& Process.info.pfairy.FairyHp >= Info.PrivateFairyBattleNormal.BcInsufficientHpMax) {
						Process.info.CurrentDeck = Info.PrivateFairyBattleNormal;
					} else if (Process.info.bc >= Info.BcInsufficientDeck.BC) {
						Process.info.CurrentDeck = Info.BcInsufficientDeck;
					} else {
						return false;
					}
				} else {
					if (Process.info.bc >= Info.PrivateFairyBattleNormal.BC) {
						Process.info.CurrentDeck = Info.PrivateFairyBattleNormal;
					} else {
						return false;
					}
				}
				break;
			case 7:
				if (Info.PrivateFairyBattleRare.UseBcInsufficientDeck) {
					if (Process.info.bc >= Info.PrivateFairyBattleRare.BC
							&& Process.info.bc >= Process.info.bcMax
									* Info.PrivateFairyBattleRare.BcInsufficientScale
							&& Process.info.pfairy.FairyHp >= Info.PrivateFairyBattleRare.BcInsufficientHpMax) {
						Process.info.CurrentDeck = Info.PrivateFairyBattleRare;
					} else if (Process.info.bc >= Info.BcInsufficientDeck.BC) {
						Process.info.CurrentDeck = Info.BcInsufficientDeck;
					} else {
						return false;
					}
				} else {
					if (Process.info.bc >= Info.PrivateFairyBattleRare.BC) {
						Process.info.CurrentDeck = Info.PrivateFairyBattleRare;
					} else {
						return false;
					}
				}
				break;
			default:
				return false;
			}
		}
		return true;
	}

	private static void decideUpPoint() {
		if (Info.AutoAddAP) {
			Process.info.apUp = Process.info.pointToAdd;
			Process.info.bcUp = 0;
		} else {
			Process.info.apUp = 0;
			Process.info.bcUp = Process.info.pointToAdd;
		}
	}

	private static boolean canExplore() {
		try {
			// 首先确定楼层
			if (Process.info.floor.isEmpty())
				return false;

			if (Process.info.AllClear)
				Process.info.front = Process.info.floor.get(1);

			if (Info.OneAPOnly)
				Process.info.front = Process.info.floor.get(1);

			// 判断是否可以行动
			if (Process.info.front == null)
				Process.info.front = Process.info.floor.get(1);

			if (Process.info.ap < Process.info.front.cost)
				return false;

			if (Process.info.ap > Process.info.apMax - 5)
				return true;

			if (Process.info.myFairyStillAlive)
				return false;

			if (!Info.AllowBCInsuffient
					&& Process.info.bc < Info.PrivateFairyBattleNormal.BC)
				return false;

			if (Process.info.ap >= Process.info.apMax * Info.ApForceGo)
				return true;
			else
				return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private static boolean cardsToSell() {
		int count = 0;
		String toSell = "";
		for (Card c : Process.info.cardList) {
			if (!c.exist)
				continue;
			if (c.holo && c.hp >= 3500)
				continue; // 闪卡不卖，但是低等级的闪卡照样要卖
			if (c.hp > 6000)
				continue; // 防止不小心把贵重卡片卖了
			if (Info.CanBeSold.contains(c.cardId)) {
				if (toSell.isEmpty()) {
					toSell = c.serialId;
				} else {
					toSell += "," + c.serialId;
				}
				count++;
				c.exist = false;
			}
			if (count >= 30)
				break;
		}

		Process.info.toSell = toSell;
		return !toSell.isEmpty();
	}

}
