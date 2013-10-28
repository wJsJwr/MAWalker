package walker;

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
			Network.UserAgent = xpath.evaluate("/config/user_agent", doc);
			
			Info.Profile = Integer.parseInt(xpath.evaluate("/config/profile", doc));
			
			switch (Info.Profile) {
			case 1:
				NodeList idl = (NodeList)xpath.evaluate("/config/sell_card/id", doc, XPathConstants.NODESET);
				Info.CanBeSold = new ArrayList<String>();
				for (int i = 0; i< idl.getLength(); i++) {
					Node idx = idl.item(i);
					try {
						Info.CanBeSold.add(idx.getFirstChild().getNodeValue());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				Info.FairyBattleFirst = xpath.evaluate("/config/option/fairy_battle_first", doc).equals("1");
				Info.AllowBCInsuffient = xpath.evaluate("/config/option/allow_bc_insuffient", doc).equals("1");
				Info.ThisAPOnly = Integer.parseInt(xpath.evaluate("/config/option/this_ap_only", doc));
				Info.AutoAddp = xpath.evaluate("/config/option/auto_add_point", doc).equals("1");
				Info.AllowAttackSameFairy = xpath.evaluate("/config/option/allow_attack_same_fairy", doc).equals("1");
				Info.debug = xpath.evaluate("/config/option/debug", doc).equals("1");
				Info.nightModeSwitch = xpath.evaluate("/config/option/night_mode", doc).equals("1");
				Info.receiveBattlePresent = xpath.evaluate("/config/option/receive_battle_present", doc).equals("1");
				
				Info.autoUseAp = xpath.evaluate("/config/use/auto_use_ap", doc).equals("1");
				if (Info.autoUseAp) {
					String half = xpath.evaluate("/config/use/strategy/ap/half", doc);
					if (half.equals("0")) {
						Info.autoApType = Info.autoUseType.FULL_ONLY;
					} else if (half.equals("1")) {
						Info.autoApType = Info.autoUseType.HALF_ONLY;
					} else {
						Info.autoApType = Info.autoUseType.ALL;
					}
					Info.autoApLow = Integer.parseInt(xpath.evaluate("/config/use/strategy/ap/low",doc));
					Info.autoApFullLow = Integer.parseInt(xpath.evaluate("/config/use/strategy/ap/full_low",doc));
				}
				Info.autoUseBc = xpath.evaluate("/config/use/auto_use_bc", doc).equals("1");
				if (Info.autoUseBc) {
					String half = xpath.evaluate("/config/use/strategy/bc/half", doc);
					if (half.equals("0")) {
						Info.autoBcType = Info.autoUseType.FULL_ONLY;
					} else if (half.equals("1")) {
						Info.autoBcType = Info.autoUseType.HALF_ONLY;
					} else {
						Info.autoBcType = Info.autoUseType.ALL;
					}
					Info.autoBcLow = Integer.parseInt(xpath.evaluate("/config/use/strategy/bc/low",doc));
					Info.autoBcFullLow = Integer.parseInt(xpath.evaluate("/config/use/strategy/bc/full_low",doc));
				}
				
				
				
				
				

				Info.FriendFairyBattleRare.No = xpath.evaluate("/config/deck/deck_profile[name='FriendFairyBattleRare']/no", doc);
				Info.FriendFairyBattleRare.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='FriendFairyBattleRare']/bc", doc));
				
				Info.FriendFairyBattleNormal.No = xpath.evaluate("/config/deck/deck_profile[name='FriendFairyBattleNormal']/no", doc);
				Info.FriendFairyBattleNormal.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='FriendFairyBattleNormal']/bc", doc));
				
				Info.PublicFairyBattle.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='GuildFairyDeck']/bc", doc));
				Info.PublicFairyBattle.No = xpath.evaluate("/config/deck/deck_profile[name='GuildFairyDeck']/no", doc);

				Info.PrivateFairyBattleNormal.No = xpath.evaluate("/config/deck/deck_profile[name='FairyDeck']/no", doc);
				Info.PrivateFairyBattleNormal.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='FairyDeck']/bc", doc));
				
				Info.PrivateFairyBattleRare.No = xpath.evaluate("/config/deck/deck_profile[name='RareFairyDeck']/no", doc);
				Info.PrivateFairyBattleRare.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='RareFairyDeck']/bc", doc));
				
				
				break;
			case 2:
				
				Info.ThisAPOnly = Integer.parseInt(xpath.evaluate("/config/option/this_ap_only", doc));
				Info.AllowBCInsuffient = true;
				Info.FairyBattleFirst = false;
				
				Info.FriendFairyBattleRare.No = "0";
				Info.FriendFairyBattleRare.BC = 0;
				
				Info.PublicFairyBattle.BC = 0;
				Info.PublicFairyBattle.No = "0";

				Info.PrivateFairyBattleNormal.No = "1";
				Info.PrivateFairyBattleNormal.BC = 97;
				
				Info.PrivateFairyBattleRare.No = "2";
				Info.PrivateFairyBattleRare.BC = 2;
				
				break;
			}
			
			
		} catch (Exception ex) {
			if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
				throw ex;
			}
		}
		
	}
}
