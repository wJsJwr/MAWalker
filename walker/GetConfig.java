package walker;

import info.Deck;

import java.util.ArrayList;

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
			Info.AllowAttackSameFairy = xpath.evaluate(
					"/config/option/allow_attack_same_fairy", doc).equals("1");
			Info.OnlyBcBuff = xpath.evaluate(
					"/config/option/only_bc_buff", doc).equals("1");

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

			Info.FriendFairyBattleRare.No = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FriendFairyBattleRare']/no",
							doc);
			Info.FriendFairyBattleRare.ForceBattle = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FriendFairyBattleRare']/force_battle",
							doc).equals("1");
			Info.FriendFairyBattleRare.BcForceBattle = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FriendFairyBattleRare']/bc_force_battle",
									doc));

			Info.FriendFairyBattleNormal.No = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FriendFairyBattleNormal']/no",
							doc);
			Info.FriendFairyBattleNormal.ForceBattle = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FriendFairyBattleNormal']/force_battle",
							doc).equals("1");
			Info.FriendFairyBattleNormal.BcForceBattle = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FriendFairyBattleNormal']/bc_force_battle",
									doc));

			Info.PublicFairyBattle.No = xpath.evaluate(
					"/config/fairy/fairy_profile[name='GuildFairyDeck']/no",
					doc);
			Info.ticket_max = Integer
					.parseInt(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='GuildFairyDeck']/ticket_max",
									doc));
			Info.battlewinscale = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='GuildFairyDeck']/battlewinscale",
									doc));

			Info.PrivateFairyBattleNormal.No = xpath.evaluate(
					"/config/fairy/fairy_profile[name='FairyDeck']/no", doc);
			Info.PrivateFairyBattleNormal.ForceBattle = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='FairyDeck']/force_battle",
							doc).equals("1");
			Info.PrivateFairyBattleNormal.BcForceBattle = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='FairyDeck']/bc_force_battle",
									doc));

			Info.PrivateFairyBattleRare.No = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='RareFairyDeck']/no",
							doc);
			Info.PrivateFairyBattleRare.ForceBattle = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='RareFairyDeck']/force_battle",
							doc).equals("1");
			Info.PrivateFairyBattleRare.BcForceBattle = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='RareFairyDeck']/bc_force_battle",
									doc));

			Info.BCFullBattleDeck.No = xpath.evaluate(
					"/config/fairy/fairy_profile[name='BCFullBattleDeck']/no",
					doc);

			Info.KillFairyDeck.No = xpath
					.evaluate(
							"/config/fairy/fairy_profile[name='KillFairyDeck']/no",
							doc);
			Info.killFairyHp = Long
					.parseLong(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='KillFairyDeck']/hp_kill",
									doc));
			Info.KillFairyDeck.BcKillFairy = Double
					.parseDouble(xpath
							.evaluate(
									"/config/fairy/fairy_profile[name='KillFairyDeck']/bc_kill_fairy",
									doc));

			Info.MyDeck0.No = "0";
			Info.MyDeck0.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=0]/bc", doc));

			Info.MyDeck1.No = "1";
			Info.MyDeck1.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=1]/bc", doc));

			Info.MyDeck2.No = "2";

			Info.MyDeckA1.No = "101";
			Info.MyDeckA1.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=101]/bc", doc));
			Info.MyDeckA1.card = xpath.evaluate(
					"/config/deck/deck_profile[no=101]/card", doc);
			Info.MyDeckA1.leader = Info.MyDeckA1.card.split(",")[0];

			Info.MyDeckA2.No = "102";
			Info.MyDeckA2.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=102]/bc", doc));
			Info.MyDeckA2.card = xpath.evaluate(
					"/config/deck/deck_profile[no=102]/card", doc);
			Info.MyDeckA3.leader = Info.MyDeckA1.card.split(",")[0];

			Info.MyDeckA3.No = "103";
			Info.MyDeckA3.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=103]/bc", doc));
			Info.MyDeckA3.card = xpath.evaluate(
					"/config/deck/deck_profile[no=103]/card", doc);
			Info.MyDeckA3.leader = Info.MyDeckA1.card.split(",")[0];

			Info.MyDeckA4.No = "104";
			Info.MyDeckA4.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=104]/bc", doc));
			Info.MyDeckA4.card = xpath.evaluate(
					"/config/deck/deck_profile[no=104]/card", doc);
			Info.MyDeckA4.leader = Info.MyDeckA1.card.split(",")[0];

			Info.MyDeckA5.No = "105";
			Info.MyDeckA5.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[no=105]/bc", doc));
			Info.MyDeckA5.card = xpath.evaluate(
					"/config/deck/deck_profile[no=105]/card", doc);
			Info.MyDeckA5.leader = Info.MyDeckA5.card.split(",")[0];

			Deck tempDeck = chooseCardDeck(Info.PrivateFairyBattleNormal.No);
			Info.PrivateFairyBattleNormal.No = tempDeck.No;
			Info.PrivateFairyBattleNormal.BC = tempDeck.BC;
			Info.PrivateFairyBattleNormal.card = tempDeck.card;
			Info.PrivateFairyBattleNormal.leader = tempDeck.leader;

			tempDeck = chooseCardDeck(Info.PrivateFairyBattleRare.No);
			Info.PrivateFairyBattleRare.No = tempDeck.No;
			Info.PrivateFairyBattleRare.BC = tempDeck.BC;
			Info.PrivateFairyBattleRare.card = tempDeck.card;
			Info.PrivateFairyBattleRare.leader = tempDeck.leader;

			tempDeck = chooseCardDeck(Info.FriendFairyBattleNormal.No);
			Info.FriendFairyBattleNormal.No = tempDeck.No;
			Info.FriendFairyBattleNormal.BC = tempDeck.BC;
			Info.FriendFairyBattleNormal.card = tempDeck.card;
			Info.FriendFairyBattleNormal.leader = tempDeck.leader;

			tempDeck = chooseCardDeck(Info.FriendFairyBattleRare.No);
			Info.FriendFairyBattleRare.No = tempDeck.No;
			Info.FriendFairyBattleRare.BC = tempDeck.BC;
			Info.FriendFairyBattleRare.card = tempDeck.card;
			Info.FriendFairyBattleRare.leader = tempDeck.leader;

			tempDeck = chooseCardDeck(Info.BCFullBattleDeck.No);
			Info.BCFullBattleDeck.No = tempDeck.No;
			Info.BCFullBattleDeck.BC = tempDeck.BC;
			Info.BCFullBattleDeck.card = tempDeck.card;
			Info.BCFullBattleDeck.leader = tempDeck.leader;

			tempDeck = chooseCardDeck(Info.KillFairyDeck.No);
			Info.KillFairyDeck.No = tempDeck.No;
			Info.KillFairyDeck.BC = tempDeck.BC;
			Info.KillFairyDeck.card = tempDeck.card;
			Info.KillFairyDeck.leader = tempDeck.leader;

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
		switch (n) {
		case 0:
			return Info.MyDeck0;
		case 1:
			return Info.MyDeck1;
		case 101:
			return Info.MyDeckA1;
		case 102:
			return Info.MyDeckA2;
		case 103:
			return Info.MyDeckA3;
		case 104:
			return Info.MyDeckA4;
		case 105:
			return Info.MyDeckA5;
		default:
			return defaultDeck;
		}
	}
}
