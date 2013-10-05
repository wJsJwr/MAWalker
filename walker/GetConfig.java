package walker;

import info.Deck;

import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.Network;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GetConfig {
	public static void parse(Document doc) throws Exception {
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			Info.LoginId = xpath.evaluate("/config/username", doc);
			Info.LoginPw = xpath.evaluate("/config/password", doc);
			Info.sessionId = xpath.evaluate("/config/sessionId", doc);
			Network.UserAgent = xpath.evaluate("/config/UserAgent", doc).trim();
			Network.myProxy = xpath.evaluate("/config/proxy", doc);
			String tmpPort = xpath.evaluate("/config/proxy_port", doc);
			if (!tmpPort.isEmpty())
				Network.myProxyPort = Integer.parseInt(tmpPort);

			NodeList idl = (NodeList) xpath.evaluate("/config/sell_card/id",
					doc, XPathConstants.NODESET);
			Info.CanBeSold = new ArrayList<String>();
			for (int i = 0; i < idl.getLength(); i++) {
				Node idx = idl.item(i);
				try {
					Info.CanBeSold.add(idx.getFirstChild().getNodeValue());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			Info.FairyBattleFirst = xpath.evaluate(
					"/config/option/fairy_battle_first", doc).equals("1");
			Info.AllowBCInsuffient = xpath.evaluate(
					"/config/option/allow_bc_insuffient", doc).equals("1");
			Info.OneAPOnly = xpath.evaluate("/config/option/one_ap_only", doc)
					.equals("1");
			Info.AutoAddp = xpath
					.evaluate("/config/option/auto_add_point", doc).equals("1");
			Info.AutoAddAP = xpath.evaluate("/config/option/auto_add_ap", doc)
					.equals("1");
			Info.OnlyBcBuff = xpath
					.evaluate("/config/option/only_bc_buff", doc).equals("1");

			Info.nightModeSwitch = xpath.evaluate("/config/option/night_mode",
					doc).equals("1");
			Info.sellcard = xpath.evaluate("/config/sell_card/on_off", doc)
					.equals("1");
			Info.sellallcard = xpath
					.evaluate("/config/sell_card/sell_all", doc).equals("1");

			Info.autoUseAp = xpath.evaluate("/config/use/auto_use_ap", doc)
					.equals("1");

			String aphalf = xpath.evaluate("/config/use/strategy/ap/half", doc);
			if (aphalf.equals("0")) {
				Info.autoApType = Info.autoUseType.FULL_ONLY;
			} else if (aphalf.equals("1")) {
				Info.autoApType = Info.autoUseType.HALF_ONLY;
			} else {
				Info.autoApType = Info.autoUseType.ALL;
			}
			Info.autoApLow = Integer.parseInt(xpath.evaluate(
					"/config/use/strategy/ap/low", doc));
			Info.autoApFullLow = Integer.parseInt(xpath.evaluate(
					"/config/use/strategy/ap/full_low", doc));

			Info.autoUseBc = xpath.evaluate("/config/use/auto_use_bc", doc)
					.equals("1");

			String bchalf = xpath.evaluate("/config/use/strategy/bc/half", doc);
			if (bchalf.equals("0")) {
				Info.autoBcType = Info.autoUseType.FULL_ONLY;
			} else if (bchalf.equals("1")) {
				Info.autoBcType = Info.autoUseType.HALF_ONLY;
			} else {
				Info.autoBcType = Info.autoUseType.ALL;
			}
			Info.autoBcLow = Integer.parseInt(xpath.evaluate(
					"/config/use/strategy/bc/low", doc));
			Info.autoBcFullLow = Integer.parseInt(xpath.evaluate(
					"/config/use/strategy/bc/full_low", doc));
			Info.sleep_time = Long.parseLong(xpath.evaluate(
					"/config/option/sleep_time", doc));
			if (Info.sleep_time < 5)
				Info.sleep_time = 5;
			Info.ApForceGo = Double.parseDouble(xpath.evaluate(
					"/config/option/ap_force_go", doc));
			Info.GoNoEventArea = xpath.evaluate(
					"/config/option/go_no_event_area", doc).equals("1");
			Info.GoDailyArea = xpath.evaluate("/config/option/go_daily_area",
					doc).equals("1");
			Info.useSleep= xpath.evaluate("/config/sleep/use_timer", doc)
					.equals("1");
			if (Info.useSleep) {
				int startHour = Integer.parseInt(xpath.evaluate(
						"/config/sleep/time[lable='StartTime']/hour", doc));
				int startMinute = Integer.parseInt(xpath.evaluate(
						"/config/sleep/time[lable='StartTime']/minute", doc));
				int stopHour = Integer.parseInt(xpath.evaluate(
						"/config/sleep/time[lable='StopTime']/hour", doc));
				int stopMinute = Integer.parseInt(xpath.evaluate(
						"/config/sleep/time[lable='StopTime']/minute", doc));
				Info.startTime = Calendar.getInstance();
				Info.stopTime = Calendar.getInstance();
				Info.startTime.set(Calendar.HOUR_OF_DAY, startHour);
				Info.startTime.set(Calendar.MINUTE, startMinute);
				Info.startTime.set(Calendar.SECOND, 0);
				int tmpdate = Info.startTime.get(Calendar.DAY_OF_YEAR);
				if (Calendar.getInstance().getTimeInMillis() > Info.startTime
						.getTimeInMillis()) {
					tmpdate++;
					Info.startTime.set(Calendar.DAY_OF_YEAR, tmpdate);
					Info.stopTime.set(Calendar.DAY_OF_YEAR, tmpdate);
				}
				Info.stopTime.set(Calendar.HOUR_OF_DAY, stopHour);
				Info.stopTime.set(Calendar.MINUTE, stopMinute);
				Info.stopTime.set(Calendar.SECOND, 0);
				tmpdate = Info.startTime.get(Calendar.DAY_OF_YEAR);
				if (Info.startTime.getTimeInMillis() > Info.stopTime
						.getTimeInMillis()) {
					tmpdate++;
					Info.stopTime.set(Calendar.DAY_OF_YEAR, tmpdate);
				}
			}

			// 自己普通妖精
			Info.PrivateFairyBattleNormal.No = xpath.evaluate(
					"/config/fairy/fairy_profile[name='FairyDeck']/no", doc);
			Info.PrivateFairyBattleNormal.UseBcInsufficientDeck = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FairyDeck']/use_bc_insufficient_deck",
							doc).equals("1");
			Info.PrivateFairyBattleNormal.BcInsufficientScale = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FairyDeck']/bc_insufficient_scale",
									doc));
			Info.PrivateFairyBattleNormal.BcInsufficientHpMax = Long
					.parseLong(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FairyDeck']/bc_insufficient_hp_max",
									doc));
			Info.PrivateFairyBattleNormal.UseKillFairyDeck = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FairyDeck']/use_kill_fairy_deck",
							doc).equals("1");
			Info.PrivateFairyBattleNormal.KillFairyHpMax = Long
					.parseLong(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FairyDeck']/kill_fairy_hp_max",
									doc));

			// 自己觉醒妖精
			Info.PrivateFairyBattleRare.No = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='RareFairyDeck']/no",
							doc);
			Info.PrivateFairyBattleRare.UseBcInsufficientDeck = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='RareFairyDeck']/use_bc_insufficient_deck",
							doc).equals("1");
			Info.PrivateFairyBattleRare.BcInsufficientScale = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='RareFairyDeck']/bc_insufficient_scale",
									doc));
			Info.PrivateFairyBattleRare.BcInsufficientHpMax = Long
					.parseLong(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='RareFairyDeck']/bc_insufficient_hp_max",
									doc));
			Info.PrivateFairyBattleRare.UseKillFairyDeck = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='RareFairyDeck']/use_kill_fairy_deck",
							doc).equals("1");
			Info.PrivateFairyBattleRare.KillFairyHpMax = Long
					.parseLong(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='RareFairyDeck']/kill_fairy_hp_max",
									doc));

			// 外敌
			Info.PublicFairyBattle.No = xpath.evaluate(
					"/config/fairy/fairy_profile[name='GuildFairyDeck']/no",
					doc);
			Info.ticket_max = Integer
					.parseInt(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='GuildFairyDeck']/ticket_max",
									doc));
			Info.battle_win_scale = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='GuildFairyDeck']/battle_win_scale",
									doc));

			// 基友普通妖精
			Info.FriendFairyBattleNormal.No = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FriendFairyBattleNormal']/no",
							doc);
			Info.FriendFairyBattleNormal.UseBcInsufficientDeck = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FriendFairyBattleNormal']/use_bc_insufficient_deck",
							doc).equals("1");
			Info.FriendFairyBattleNormal.BcInsufficientScale = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FriendFairyBattleNormal']/bc_insufficient_scale",
									doc));
			Info.FriendFairyBattleNormal.BcInsufficientHpMax = Long
					.parseLong(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FriendFairyBattleNormal']/bc_insufficient_hp_max",
									doc));
			Info.FriendFairyBattleNormal.UseKillFairyDeck = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FriendFairyBattleNormal']/use_kill_fairy_deck",
							doc).equals("1");
			Info.FriendFairyBattleNormal.KillFairyHpMax = Long
					.parseLong(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FriendFairyBattleNormal']/kill_fairy_hp_max",
									doc));

			// 基友觉醒妖精
			Info.FriendFairyBattleRare.No = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FriendFairyBattleRare']/no",
							doc);
			Info.FriendFairyBattleRare.UseBcInsufficientDeck = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FriendFairyBattleRare']/use_bc_insufficient_deck",
							doc).equals("1");
			Info.FriendFairyBattleRare.BcInsufficientScale = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FriendFairyBattleRare']/bc_insufficient_scale",
									doc));
			Info.FriendFairyBattleRare.BcInsufficientHpMax = Long
					.parseLong(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FriendFairyBattleRare']/bc_insufficient_hp_max",
									doc));
			Info.FriendFairyBattleRare.UseKillFairyDeck = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FriendFairyBattleRare']/use_kill_fairy_deck",
							doc).equals("1");
			Info.FriendFairyBattleRare.KillFairyHpMax = Long
					.parseLong(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FriendFairyBattleRare']/kill_fairy_hp_max",
									doc));

			// BC不足出击卡组
			Info.BcInsufficientDeck.No = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='BcInsufficientDeck']/no",
							doc);

			// 尾刀卡组
			Info.KillFairyDeck.No = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='KillFairyDeck']/no",
							doc);
			Info.KillFairyScale = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='KillFairyDeck']/kill_fairy_scale",
									doc));

			Info.MyDeck0.No = "0";
			Info.MyDeck0.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=0]/bc", doc));
			Info.MyDeck0.CustomDeckName = xpath.evaluate(
					"/config/deck/deck_profile[no=0]/custom_name", doc);

			Info.MyDeck1.No = "1";
			Info.MyDeck1.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=1]/bc", doc));
			Info.MyDeck1.CustomDeckName = xpath.evaluate(
					"/config/deck/deck_profile[no=1]/custom_name", doc);

			Info.MyDeck2.No = "2";

			Info.MyDeckA1.No = "201";
			Info.MyDeckA1.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=201]/bc", doc));
			Info.MyDeckA1.card = xpath.evaluate(
					"/config/deck/deck_profile[no=201]/card", doc);
			for (String i : Info.MyDeckA1.card.split(",")) {
				if (!i.equals("empty")) {
					Info.MyDeckA1.leader = i;
					break;
				}
			}
			Info.MyDeckA1.CustomDeckName = xpath.evaluate(
					"/config/deck/deck_profile[no=201]/custom_name", doc);

			Info.MyDeckA2.No = "202";
			Info.MyDeckA2.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=202]/bc", doc));
			Info.MyDeckA2.card = xpath.evaluate(
					"/config/deck/deck_profile[no=202]/card", doc);
			for (String i : Info.MyDeckA2.card.split(",")) {
				if (!i.equals("empty")) {
					Info.MyDeckA2.leader = i;
					break;
				}
			}
			Info.MyDeckA2.CustomDeckName = xpath.evaluate(
					"/config/deck/deck_profile[no=202]/custom_name", doc);

			Info.MyDeckA3.No = "203";
			Info.MyDeckA3.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=203]/bc", doc));
			Info.MyDeckA3.card = xpath.evaluate(
					"/config/deck/deck_profile[no=203]/card", doc);
			for (String i : Info.MyDeckA3.card.split(",")) {
				if (!i.equals("empty")) {
					Info.MyDeckA3.leader = i;
					break;
				}
			}
			Info.MyDeckA3.CustomDeckName = xpath.evaluate(
					"/config/deck/deck_profile[no=203]/custom_name", doc);

			Info.MyDeckA4.No = "204";
			Info.MyDeckA4.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=204]/bc", doc));
			Info.MyDeckA4.card = xpath.evaluate(
					"/config/deck/deck_profile[no=204]/card", doc);
			for (String i : Info.MyDeckA4.card.split(",")) {
				if (!i.equals("empty")) {
					Info.MyDeckA4.leader = i;
					break;
				}
			}
			Info.MyDeckA4.CustomDeckName = xpath.evaluate(
					"/config/deck/deck_profile[no=204]/custom_name", doc);

			Info.MyDeckA5.No = "205";
			Info.MyDeckA5.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=205]/bc", doc));
			Info.MyDeckA5.card = xpath.evaluate(
					"/config/deck/deck_profile[no=205]/card", doc);
			for (String i : Info.MyDeckA5.card.split(",")) {
				if (!i.equals("empty")) {
					Info.MyDeckA5.leader = i;
					break;
				}
			}
			Info.MyDeckA5.CustomDeckName = xpath.evaluate(
					"/config/deck/deck_profile[no=205]/custom_name", doc);

			Deck tempDeck = chooseCardDeck(Info.PrivateFairyBattleNormal.No);
			Info.PrivateFairyBattleNormal.No = tempDeck.No;
			Info.PrivateFairyBattleNormal.BC = tempDeck.BC;
			Info.PrivateFairyBattleNormal.card = tempDeck.card;
			Info.PrivateFairyBattleNormal.leader = tempDeck.leader;
			Info.PrivateFairyBattleNormal.CustomDeckName = tempDeck.CustomDeckName;

			tempDeck = chooseCardDeck(Info.PrivateFairyBattleRare.No);
			Info.PrivateFairyBattleRare.No = tempDeck.No;
			Info.PrivateFairyBattleRare.BC = tempDeck.BC;
			Info.PrivateFairyBattleRare.card = tempDeck.card;
			Info.PrivateFairyBattleRare.leader = tempDeck.leader;
			Info.PrivateFairyBattleRare.CustomDeckName = tempDeck.CustomDeckName;

			tempDeck = chooseCardDeck(Info.FriendFairyBattleNormal.No);
			Info.FriendFairyBattleNormal.No = tempDeck.No;
			Info.FriendFairyBattleNormal.BC = tempDeck.BC;
			Info.FriendFairyBattleNormal.card = tempDeck.card;
			Info.FriendFairyBattleNormal.leader = tempDeck.leader;
			Info.FriendFairyBattleNormal.CustomDeckName = tempDeck.CustomDeckName;

			tempDeck = chooseCardDeck(Info.FriendFairyBattleRare.No);
			Info.FriendFairyBattleRare.No = tempDeck.No;
			Info.FriendFairyBattleRare.BC = tempDeck.BC;
			Info.FriendFairyBattleRare.card = tempDeck.card;
			Info.FriendFairyBattleRare.leader = tempDeck.leader;
			Info.FriendFairyBattleRare.CustomDeckName = tempDeck.CustomDeckName;

			tempDeck = chooseCardDeck(Info.BcInsufficientDeck.No);
			Info.BcInsufficientDeck.No = tempDeck.No;
			Info.BcInsufficientDeck.BC = tempDeck.BC;
			Info.BcInsufficientDeck.card = tempDeck.card;
			Info.BcInsufficientDeck.leader = tempDeck.leader;
			Info.BcInsufficientDeck.CustomDeckName = tempDeck.CustomDeckName;

			tempDeck = chooseCardDeck(Info.KillFairyDeck.No);
			Info.KillFairyDeck.No = tempDeck.No;
			Info.KillFairyDeck.BC = tempDeck.BC;
			Info.KillFairyDeck.card = tempDeck.card;
			Info.KillFairyDeck.leader = tempDeck.leader;
			Info.KillFairyDeck.CustomDeckName = tempDeck.CustomDeckName;

			tempDeck = chooseCardDeck(Info.PublicFairyBattle.No);
			Info.PublicFairyBattle.CustomDeckName = tempDeck.CustomDeckName;

		} catch (Exception ex) {
			if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
				throw ex;
			}
		}

	}

	private static Deck chooseCardDeck(String Number) {
		int n = Integer.parseInt(Number);
		Deck defaultDeck = new Deck();
		defaultDeck.BC = 0;
		defaultDeck.No = "3";
		defaultDeck.card = "";
		defaultDeck.CustomDeckName = "自动配卡";
		switch (n) {
		case 0:
			return Info.MyDeck0;
		case 1:
			return Info.MyDeck1;
		case 201:
			return Info.MyDeckA1;
		case 202:
			return Info.MyDeckA2;
		case 203:
			return Info.MyDeckA3;
		case 204:
			return Info.MyDeckA4;
		case 205:
			return Info.MyDeckA5;
		default:
			return defaultDeck;
		}
	}
}
