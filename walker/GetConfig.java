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
				Info.RareFairyUseNormalDeck = xpath.evaluate("/config/option/rare_fairy_use_normal_deck", doc).equals("1");
				Info.AllowBCInsuffient = xpath.evaluate("/config/option/allow_bc_insuffient", doc).equals("1");
				Info.OneAPOnly = xpath.evaluate("/config/option/one_ap_only", doc).equals("1");

				Info.FriendFairyBattle0BC.No = xpath.evaluate("/config/deck/deck_profile[name='FairyOfFriend']/no", doc);
				Info.FriendFairyBattle0BC.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='FairyOfFriend']/bc", doc));
				
				Info.PublicFairyBattle.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='GuildFairyDeck']/bc", doc));
				Info.PublicFairyBattle.No = xpath.evaluate("/config/deck/deck_profile[name='GuildFairyDeck']/no", doc);

				Info.PrivateFairyBattleNormal.No = xpath.evaluate("/config/deck/deck_profile[name='FairyDeck']/no", doc);
				Info.PrivateFairyBattleNormal.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='FairyDeck']/bc", doc));
				
				Info.PrivateFairyBattleRare.No = xpath.evaluate("/config/deck/deck_profile[name='RareFairyDeck']/no", doc);
				Info.PrivateFairyBattleRare.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='RareFairyDeck']/bc", doc));
				
				
				break;
			case 2:
				
				Info.OneAPOnly = true;
				Info.AllowBCInsuffient = true;
				Info.FairyBattleFirst = false;
				Info.RareFairyUseNormalDeck = false;
				
				Info.FriendFairyBattle0BC.No = "0";
				Info.FriendFairyBattle0BC.BC = 0;
				
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
