/**
 *                            License
 * THE WORK (AS DEFINED BELOW) IS PROVIDED UNDER THE TERMS OF THIS  
 * CREATIVE COMMONS PUBLIC LICENSE ("CCPL" OR "LICENSE"). 
 * THE WORK IS PROTECTED BY COPYRIGHT AND/OR OTHER APPLICABLE LAW.  
 * ANY USE OF THE WORK OTHER THAN AS AUTHORIZED UNDER THIS LICENSE OR  
 * COPYRIGHT LAW IS PROHIBITED.
 * 
 * BY EXERCISING ANY RIGHTS TO THE WORK PROVIDED HERE, YOU ACCEPT AND  
 * AGREE TO BE BOUND BY THE TERMS OF THIS LICENSE. TO THE EXTENT THIS LICENSE  
 * MAY BE CONSIDERED TO BE A CONTRACT, THE LICENSOR GRANTS YOU THE RIGHTS CONTAINED 
 * HERE IN CONSIDERATION OF YOUR ACCEPTANCE OF SUCH TERMS AND CONDITIONS.
 * 
 */
package Lsimulator.server.server.utils.Internationalization;

import java.util.ListResourceBundle;

/**
 * @category 預設 台灣-正體中文<br>
 *           國際化的英文是Internationalization 因為單字中總共有18個字母，簡稱I18N，
 *           目的是讓應用程式可以應地區不同而顯示不同的訊息。
 */
public class messages extends ListResourceBundle {
	static final Object[][] contents = { 
		{ "Lsimulator.server.memoryUse", "使用了: " },
		{ "Lsimulator.server.memory", "MB 的記憶體" },
		{ "Lsimulator.server.server.model.onGroundItem", "地上的物品" },
		{ "Lsimulator.server.server.model.seconds", "10秒後將被清除" },
		{ "Lsimulator.server.server.model.deleted", "已經被清除了" },
		{ "Lsimulator.server.server.GameServer.ver"," " },
		{ "Lsimulator.server.server.GameServer.settingslist","●●●●〈伺服器設置清單〉●●●●"},
		{ "Lsimulator.server.server.GameServer.exp","「經驗值」"},
		{ "Lsimulator.server.server.GameServer.x","【倍】"},
		{ "Lsimulator.server.server.GameServer.level","【級】"},
		{ "Lsimulator.server.server.GameServer.justice","「正義值」"},
		{ "Lsimulator.server.server.GameServer.karma","「友好度」"},
		{ "Lsimulator.server.server.GameServer.dropitems","「物品掉落」"},
		{ "Lsimulator.server.server.GameServer.dropadena","「金幣掉落」"},
		{ "Lsimulator.server.server.GameServer.enchantweapon","「衝武」"},
		{ "Lsimulator.server.server.GameServer.enchantarmor","「衝防」"},
		{ "Lsimulator.server.server.GameServer.chatlevel","「廣播頻道可用等級」"},
		{ "Lsimulator.server.server.GameServer.nonpvp1","「Non-PvP設定」: 【無效 (PvP可能)】"},
		{ "Lsimulator.server.server.GameServer.nonpvp2","「Non-PvP設定」: 【有效 (PvP不可)】"},
		{ "Lsimulator.server.server.GameServer.maxplayer","連線人數上限為 "},
		{ "Lsimulator.server.server.GameServer.player"," 人 "},
		{ "Lsimulator.server.server.GameServer.waitingforuser","等待客戶端連接中..."},
		{ "Lsimulator.server.server.GameServer.from","從 "},
		{ "Lsimulator.server.server.GameServer.attempt"," 試圖連線"},
		{ "Lsimulator.server.server.GameServer.setporton","伺服器成功建立在 port "},
		{ "Lsimulator.server.server.GameServer.initialfinished","初始化完畢"}};

	@Override
	protected Object[][] getContents() {
		return contents;
	}

}
