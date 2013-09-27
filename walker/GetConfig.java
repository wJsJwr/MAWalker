package walker;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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
			Info.AllowBCInsuffient = xpath.evaluate(
					"/config/option/allow_bc_insuffient", doc).equals("1");
			Info.OneAPOnly = xpath.evaluate("/config/option/one_ap_only", doc)
					.equals("1");
			Info.AutoAddp = xpath
					.evaluate("/config/option/auto_add_point", doc).equals("1");
			Info.AllowAttackSameFairy = xpath.evaluate(
					"/config/option/allow_attack_same_fairy", doc).equals("1");

			Info.nightModeSwitch = xpath.evaluate("/config/option/night_mode",
					doc).equals("1");
			Info.sellcard = xpath.evaluate("/config/sell_card/on_off", doc)
					.equals("1");
			Info.sellallcard = xpath.evaluate("/config/sell_card/sell_all", doc)
					.equals("1");

			Info.autoUseAp = xpath.evaluate("/config/use/auto_use_ap", doc)
					.equals("1");
			// if (Info.autoUseAp) {
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
			// }
			Info.autoUseBc = xpath.evaluate("/config/use/auto_use_bc", doc)
					.equals("1");
			// if (Info.autoUseBc) {
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
			// }

			Info.ApForceGo = Double.parseDouble(xpath.evaluate(
					"/config/option/ap_force_go", doc));
			Info.GoNoEventArea = xpath.evaluate(
					"/config/option/go_no_event_area", doc).equals("1");
			Info.GoDailyArea = xpath.evaluate("/config/option/go_daily_area",
					doc).equals("1");

			Info.FriendFairyBattleRare.No = xpath
					.evaluate(
							"/config/deck/deck_profile[name='FriendFairyBattleRare']/no",
							doc);
			Info.FriendFairyBattleRare.BC = Integer
					.parseInt(xpath
							.evaluate(
									"/config/deck/deck_profile[name='FriendFairyBattleRare']/bc",
									doc));
			Info.FriendFairyBattleRare.ForceBattle = xpath
					.evaluate(
							"/config/deck/deck_profile[name='FriendFairyBattleRare']/force_battle",
							doc).equals("1");
			Info.FriendFairyBattleRare.BcForceBattle = Double
					.parseDouble(xpath
							.evaluate(
									"/config/deck/deck_profile[name='FriendFairyBattleRare']/bc_force_battle",
									doc));

			Info.FriendFairyBattleNormal.No = xpath
					.evaluate(
							"/config/deck/deck_profile[name='FriendFairyBattleNormal']/no",
							doc);
			Info.FriendFairyBattleNormal.BC = Integer
					.parseInt(xpath
							.evaluate(
									"/config/deck/deck_profile[name='FriendFairyBattleNormal']/bc",
									doc));
			Info.FriendFairyBattleNormal.ForceBattle = xpath
					.evaluate(
							"/config/deck/deck_profile[name='FriendFairyBattleNormal']/force_battle",
							doc).equals("1");
			Info.FriendFairyBattleNormal.BcForceBattle = Double
					.parseDouble(xpath
							.evaluate(
									"/config/deck/deck_profile[name='FriendFairyBattleNormal']/bc_force_battle",
									doc));

			Info.PublicFairyBattle.BC = Integer
					.parseInt(xpath
							.evaluate(
									"/config/deck/deck_profile[name='GuildFairyDeck']/bc",
									doc));
			Info.PublicFairyBattle.No = xpath.evaluate(
					"/config/deck/deck_profile[name='GuildFairyDeck']/no", doc);
			Info.ticket_max = Integer
					.parseInt(xpath
							.evaluate(
									"/config/deck/deck_profile[name='GuildFairyDeck']/ticket_max",
									doc));
			Info.battlewinscale = Double
					.parseDouble(xpath
							.evaluate(
									"/config/deck/deck_profile[name='GuildFairyDeck']/battlewinscale",
									doc));

			Info.PrivateFairyBattleNormal.No = xpath.evaluate(
					"/config/deck/deck_profile[name='FairyDeck']/no", doc);
			Info.PrivateFairyBattleNormal.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[name='FairyDeck']/bc", doc));
			Info.PrivateFairyBattleNormal.ForceBattle = xpath.evaluate(
					"/config/deck/deck_profile[name='FairyDeck']/force_battle",
					doc).equals("1");
			Info.PrivateFairyBattleNormal.BcForceBattle = Double
					.parseDouble(xpath
							.evaluate(
									"/config/deck/deck_profile[name='FairyDeck']/bc_force_battle",
									doc));

			Info.PrivateFairyBattleRare.No = xpath.evaluate(
					"/config/deck/deck_profile[name='RareFairyDeck']/no", doc);
			Info.PrivateFairyBattleRare.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[name='RareFairyDeck']/bc", doc));
			Info.PrivateFairyBattleRare.ForceBattle = xpath
					.evaluate(
							"/config/deck/deck_profile[name='RareFairyDeck']/force_battle",
							doc).equals("1");
			Info.PrivateFairyBattleRare.BcForceBattle = Double
					.parseDouble(xpath
							.evaluate(
									"/config/deck/deck_profile[name='RareFairyDeck']/bc_force_battle",
									doc));

			Info.BCFullBattleDeck.No = xpath.evaluate(
					"/config/deck/deck_profile[name='BCFullBattleDeck']/no",
					doc);
			Info.BCFullBattleDeck.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[name='BCFullBattleDeck']/bc",
					doc));
			
			Info.KillFairyDeck.No = xpath.evaluate(
					"/config/deck/deck_profile[name='KillFairyDeck']/no",
					doc);
			Info.KillFairyDeck.BC = Integer.parseInt(xpath.evaluate(
					"/config/deck/deck_profile[name='KillFairyDeck']/bc",
					doc));
			Info.killFairyHp = Long.parseLong(xpath.evaluate(
					"/config/deck/deck_profile[name='KillFairyDeck']/hp_kill",
					doc));

		} catch (Exception ex) {
			if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
				throw ex;
			}
		}

	}
}
