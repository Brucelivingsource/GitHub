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
 * @category 日本-日本語<br>
 *           國際化的英文是Internationalization 因為單字中總共有18個字母，簡稱I18N，
 *           目的是讓應用程式可以應地區不同而顯示不同的訊息。
 */
public class messages_ja_JP extends ListResourceBundle {
	static final Object[][] contents = { 
		{ "Lsimulator.server.memoryUse", "利用メモリ: " },
		{ "Lsimulator.server.memory", "MB" },
		{ "Lsimulator.server.server.model.onGroundItem", "ワールドマップ上のアイテム" },
		{ "Lsimulator.server.server.model.seconds", "10秒後に削除されます" },
		{ "Lsimulator.server.server.model.deleted", "削除されました" },
		{ "Lsimulator.server.server.GameServer.ver","バージョン: Lineage 3.80C 開発  By LsimulatorJ For All User" },
		{ "Lsimulator.server.server.GameServer.settingslist","●●●●〈サーバー設定〉●●●●"},
		{ "Lsimulator.server.server.GameServer.exp","「経験値」"},
		{ "Lsimulator.server.server.GameServer.x","【倍】"},
		{ "Lsimulator.server.server.GameServer.level",""},
		{ "Lsimulator.server.server.GameServer.justice","「アライメント」"},
		{ "Lsimulator.server.server.GameServer.karma","「カルマ」"},
		{ "Lsimulator.server.server.GameServer.dropitems","「ドロップ率」"},
		{ "Lsimulator.server.server.GameServer.dropadena","「取得アデナ」"},
		{ "Lsimulator.server.server.GameServer.enchantweapon","「武器エンチャント成功率」"},
		{ "Lsimulator.server.server.GameServer.enchantarmor","「防具エンチャント成功率」"},
		{ "Lsimulator.server.server.GameServer.chatlevel","「全体チャット可能Lv」"},
		{ "Lsimulator.server.server.GameServer.nonpvp1","「Non-PvP設定」: 無効（PvP可能）"},
		{ "Lsimulator.server.server.GameServer.nonpvp2","「Non-PvP設定」: 有効（PvP不可）"},
		{ "Lsimulator.server.server.GameServer.maxplayer","接続人数制限： 最大 "},
		{ "Lsimulator.server.server.GameServer.player"," 人 "},
		{ "Lsimulator.server.server.GameServer.waitingforuser","クライアント接続待機中..."},
		{ "Lsimulator.server.server.GameServer.from","接続試行中IP "},
		{ "Lsimulator.server.server.GameServer.attempt",""},
		{ "Lsimulator.server.server.GameServer.setporton","サーバーセッティング: サーバーソケット生成 "},
		{ "Lsimulator.server.server.GameServer.initialfinished","ローディング完了"}};

	@Override
	protected Object[][] getContents() {
		return contents;
	}

}
