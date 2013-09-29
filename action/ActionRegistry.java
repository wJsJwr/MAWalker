package action;

public class ActionRegistry {
	public static enum Action {
		NOTHING,
		/**
		 * 登录
		 */
		LOGIN,
		/**
		 * 将会刷新area和floor
		 */
		GET_FLOOR_INFO, ADD_AREA,
		/**
		 * 快速取得AP，BC以及经验值和物品等信息
		 */
		GOTO_FLOOR,
		/**
		 * 获取妖精列表
		 */
		GET_FAIRY_LIST,
		/**
		 * 妖精战
		 */
		PRIVATE_FAIRY_BATTLE, EXPLORE,
		/**
		 * 获取妖精报酬
		 */
		GET_FAIRY_REWARD,
		/**
		 * 外敌迎击战首页
		 */
		GUILD_TOP,
		/**
		 * 外敌迎击战
		 */
		GUILD_BATTLE,
		/**
		 * 卖卡
		 */
		SELL_CARD,
		/**
		 * 升级
		 */
		LV_UP,
		/**
		 * 点赞
		 */
		FAIRY_DIANZAN,
		/**
		 * 吃恢复药
		 */
		AUTO_MEDICINE,
		/**
		 * cookie登陆
		 */
		COOKIELOGIN,
		/**
		 * 主城界面
		 */
		GOTO_MAIN_MENU,
		/**
		 * 获得当前卡组信息
		 */
		GET_CARD_DECK
	}
}
