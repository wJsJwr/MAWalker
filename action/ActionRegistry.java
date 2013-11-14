package action;

public class ActionRegistry {
	public static enum Action {
		NOTHING,
		LOGIN,
		GET_FLOOR_INFO, // 灏嗕細鍒锋柊area鍜宖loor
		ADD_AREA,
		GOTO_FLOOR,	// 蹇�熷彇寰桝P锛孊C浠ュ強缁忛獙鍊煎拰鐗╁搧绛変俊鎭�
		GET_FAIRY_LIST,
		PRIVATE_FAIRY_BATTLE,
		EXPLORE,
		GET_FAIRY_REWARD,
		GUILD_TOP,
		GUILD_BATTLE,
		SELL_CARD,
		LV_UP,
		PFB_GOOD,
		RECV_PFB_GOOD,
		USE, 
		GET_REWARD_BOX,
		PARTY_RANK
	}
}
