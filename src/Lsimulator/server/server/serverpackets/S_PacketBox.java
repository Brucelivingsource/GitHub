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
package Lsimulator.server.server.serverpackets;

import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

import Lsimulator.server.Config;
import Lsimulator.server.server.Account;
import Lsimulator.server.server.Opcodes;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;

/**
 * スキルアイコンや遮断リストの表示など複数の用途に使われるパケットのクラス
 */
public class S_PacketBox extends ServerBasePacket {
	private static final String S_PACKETBOX = "[S] S_PacketBox";

	private byte[] _byte = null;

	// *** S_107 sub code list ***
	// 1:Kent 2:Orc 3:WW 4:Giran 5:Heine 6:Dwarf 7:Aden 8:Diad 9:城名9 ...
	/** C(id) H(?): %sの攻城戦が始まりました。 */
	public static final int MSG_WAR_BEGIN = 0;

	/** C(id) H(?): %sの攻城戦が終了しました。 */
	public static final int MSG_WAR_END = 1;

	/** C(id) H(?): %sの攻城戦が進行中です。 */
	public static final int MSG_WAR_GOING = 2;

	/** -: 城の主導権を握りました。 (音楽が変わる) */
	public static final int MSG_WAR_INITIATIVE = 3;

	/** -: 城を占拠しました。 */
	public static final int MSG_WAR_OCCUPY = 4;

	/** ?: 決闘が終りました。 (音楽が変わる) */
	public static final int MSG_DUEL = 5;

	/** C(count): SMSの送信に失敗しました。 / 全部で%d件送信されました。 */
	public static final int MSG_SMS_SENT = 6;

	/** -: 祝福の中、2人は夫婦として結ばれました。 (音楽が変わる) */
	public static final int MSG_MARRIED = 9;

	/** C(weight): 重量(30段階) */
	public static final int WEIGHT = 10;

	/** C(food): 満腹度(30段階) */
	public static final int FOOD = 11;

	/** C(0) C(level): このアイテムは%dレベル以下のみ使用できます。 (0~49以外は表示されない) */
	public static final int MSG_LEVEL_OVER = 12;

	/** UB情報HTML */
	public static final int HTML_UB = 14;

	/**
	 * C(id)<br>
	 * 1:身に込められていた精霊の力が空気の中に溶けて行くのを感じました。<br>
	 * 2:体の隅々に火の精霊力が染みこんできます。<br>
	 * 3:体の隅々に水の精霊力が染みこんできます。<br>
	 * 4:体の隅々に風の精霊力が染みこんできます。<br>
	 * 5:体の隅々に地の精霊力が染みこんできます。<br>
	 */
	public static final int MSG_ELF = 15;

	/** C(count) S(name)...: 遮断リスト複数追加 */
	public static final int ADD_EXCLUDE2 = 17;

	/** S(name): 遮断リスト追加 */
	public static final int ADD_EXCLUDE = 18;

	/** S(name): 遮断解除 */
	public static final int REM_EXCLUDE = 19;

	/** スキルアイコン */
	public static final int ICONS1 = 20;

	/** スキルアイコン */
	public static final int ICONS2 = 21;

	/** オーラ系のスキルアイコン */
	public static final int ICON_AURA = 22;

	/** S(name): タウンリーダーに%sが選ばれました。 */
	public static final int MSG_TOWN_LEADER = 23;

	/** 血盟推薦 接受玩家加入*/
	public static final int HTML_PLEDGE_RECOMMENDATION_ACCEPT = 25;
	/**
	 * C(id): あなたのランクが%sに変更されました。<br>
	 * id - 1:見習い 2:一般 3:ガーディアン
	 */
	public static final int MSG_RANK_CHANGED = 27;

	/** D(?) S(name) S(clanname): %s血盟の%sがラスタバド軍を退けました。 */
	public static final int MSG_WIN_LASTAVARD = 30;

	/** -: \f1気分が良くなりました。 */
	public static final int MSG_FEEL_GOOD = 31;

	/** 不明。C_30パケットが飛ぶ */
	public static final int SOMETHING1 = 33;

	/** H(time): ブルーポーションのアイコンが表示される。 */
	public static final int ICON_BLUEPOTION = 34;

	/** H(time): 変身のアイコンが表示される。 */
	public static final int ICON_POLYMORPH = 35;

	/** H(time): チャット禁止のアイコンが表示される。 */
	public static final int ICON_CHATBAN = 36;

	/** 不明。C_7パケットが飛ぶ。C_7はペットのメニューを開いたときにも飛ぶ。 */
	public static final int SOMETHING2 = 37;

	/** 血盟情報のHTMLが表示される */
	public static final int HTML_CLAN1 = 38;

	/** H(time): イミュのアイコンが表示される */
	public static final int ICON_I2H = 40;

	/** キャラクターのゲームオプション、ショートカット情報などを送る */
	public static final int CHARACTER_CONFIG = 41;

	/** キャラクター選択画面に戻る */
	public static final int LOGOUT = 42;

	/** 戦闘中に再始動することはできません。 */
	public static final int MSG_CANT_LOGOUT = 43;

	/**
	 * C(count) D(time) S(name) S(info):<br>
	 * [CALL] ボタンのついたウィンドウが表示される。これはBOTなどの不正者チェックに
	 * 使われる機能らしい。名前をダブルクリックするとC_RequestWhoが飛び、クライアントの
	 * フォルダにbot_list.txtが生成される。名前を選択して+キーを押すと新しいウィンドウが開く。
	 */
	public static final int CALL_SOMETHING = 45;

	/**
	 * C(id): バトル コロシアム、カオス大戦がー<br>
	 * id - 1:開始します 2:取り消されました 3:終了します
	 */
	public static final int MSG_COLOSSEUM = 49;

	/** 血盟情報のHTML */
	public static final int HTML_CLAN2 = 51;

	/** 料理ウィンドウを開く */
	public static final int COOK_WINDOW = 52;

	/** C(type) H(time): 料理アイコンが表示される */
	public static final int ICON_COOKING = 53;

	/** 魚がかかったグラフィックが表示される */
	public static final int FISHING = 55;

	/** 魔法娃娃狀態圖示*/
	public static final int ICON_MAGIC_DOLL = 56;
	
	/** 
	 * 戰爭結束佔領公告<br>
	 * C(count) S(name)<br> 
	 */
	public static final int MSG_WAR_OCCUPY_ALL = 79;
	
	/** 攻城戰進行中*/
	public static final int MSG_WAR_IS_GOING_ALL = 80; //TODO
	
	/** 閃避率 正*/
	public static final int DODGE_RATE_PLUS = 88;
	
	/** 閃避率 負*/
	public static final int DODGE_RATE_MINUS = 101;
	
	/** Updating */
	public static final int UPDATE_OLD_PART_MEMBER = 104;

	/** 3.3 組隊系統(更新新加入的隊員信息) */
	public static final int PATRY_UPDATE_MEMBER = 105;

	/** 3.3組隊系統(委任新隊長) */
	public static final int PATRY_SET_MASTER = 106;

	/** 3.3 組隊系統(更新隊伍信息,所有隊員) */
	public static final int PATRY_MEMBERS = 110;
	
	/** 3.8血盟倉庫使用紀錄 */
	public static final int HTML_CLAN_WARHOUSE_RECORD = 117;

	/** 3.8 地圖倒數計時器 */
	public static final int MAP_TIMER = 153;
	
	/** 3.8 地圖剩餘時間 */
	public static final int MAP_TIME = 159;
	
	/** 3.8 血盟查詢盟友 (顯示公告) */
	public static final int HTML_PLEDGE_ANNOUNCE = 167;
	
	/** 3.8 血盟查詢盟友 (寫入公告) */
	public static final int HTML_PLEDGE_REALEASE_ANNOUNCE = 168;
	
	/** 3.8 血盟查詢盟友 (寫入備註) */
	public static final int HTML_PLEDGE_WRITE_NOTES = 169;
	
	/** 3.8 血盟查詢盟友 (顯示盟友) */
	public static final int HTML_PLEDGE_MEMBERS = 170;
	
	/** 3.8 血盟查詢盟友 (顯示上線盟友) */
	public static final int HTML_PLEDGE_ONLINE_MEMBERS = 171;
	
	/** 3.8 血盟 識別盟徽狀態 */
	public static final int PLEDGE_EMBLEM_STATUS = 173;
	
	/** 3.8 村莊便利傳送 */
	public static final int TOWN_TELEPORT = 176;
	

	public S_PacketBox(int subCode) {
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(subCode);

		switch (subCode) {
			case MSG_WAR_INITIATIVE:
			case MSG_WAR_OCCUPY:
			case MSG_MARRIED:
			case MSG_FEEL_GOOD:
			case MSG_CANT_LOGOUT:
			case LOGOUT:
			case FISHING:
				break;
			case CALL_SOMETHING:
				callSomething();
			default:
				break;
		}
	}
	
	public S_PacketBox(int subCode, PcInstance pc){
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(subCode);
		switch(subCode){
		case TOWN_TELEPORT:
			writeC(0x01);
			writeH(pc.getX());
			writeH(pc.getY());
			break;
		}
		
	}

	public S_PacketBox(int subCode, int value) {
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(subCode);

		switch (subCode) {
			case ICON_BLUEPOTION:
			case ICON_CHATBAN:
			case ICON_I2H:
			case ICON_POLYMORPH:
			case MAP_TIMER:
				writeH(value); // time
				break;
			case MSG_WAR_BEGIN:
			case MSG_WAR_END:
			case MSG_WAR_GOING:
				writeC(value); // castle id
				writeH(0); // ?
				break;
			case MSG_SMS_SENT:
			case WEIGHT:
			case FOOD:
				writeC(value);
				break;
			case MSG_ELF: // 忽然全身充滿了%s的靈力。
			case MSG_COLOSSEUM: // 大圓形競技場，混沌的大戰開始！結束！取消！
				writeC(value); // msg id
				writeC(0);
				break;
			case MSG_LEVEL_OVER:
				writeC(0); // ?
				writeC(value); // 0-49以外は表示されない
				break;
			case COOK_WINDOW:
				writeC(0xdb); // ?
				writeC(0x31);
				writeC(0xdf);
				writeC(0x02);
				writeC(0x01);
				writeC(value); // level
				break;
			case DODGE_RATE_PLUS: // + 閃避率
				writeC(value);
				writeC(0x00);
				break;
			case DODGE_RATE_MINUS: // - 閃避率
				writeC(value);
				break;
			case 21: // 狀態圖示
				writeC(0x00);
				writeC(0x00);
				writeC(0x00);
				writeC(value); // 閃避圖示 (幻術:鏡像、黑妖:闇影閃避)
				break;
			case PLEDGE_EMBLEM_STATUS: 
				writeC(1);
				if(value == 0){ // 0:關閉 1:開啟
					writeC(0);
				} else if(value == 1){
					writeC(1);
				}
				writeD(0x00);
				break;
			default:
				break;
		}
	}

	public S_PacketBox(int subCode, int type, int time) {
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(subCode);

		switch (subCode) {
			case ICON_COOKING:
				if (type == 54) { // 象牙塔妙藥
					writeC(0x12);
					writeC(0x0c);
					writeC(0x0c);
					writeC(0x07);
					writeC(0x12);
					writeC(0x08);
					writeH(0x0000); // 飽和度 值:2000，飽和度100%
					writeC(type); // 類型
					writeC(0x2a);
					writeH(time); // 時間
					writeC(0x0); // 負重度 值:242，負重度100%
				} else if (type != 7) {
					writeC(0x12);
					writeC(0x0b);
					writeC(0x0c);
					writeC(0x0b);
					writeC(0x0f);
					writeC(0x08);
					writeH(0x0000); // 飽和度 值:2000，飽和度100%
					writeC(type); // 類型
					writeC(0x24);
					writeH(time); // 時間
					writeC(0x00); // 負重度 值:242，負重度100%
				} else {
					writeC(0x12);
					writeC(0x0b);
					writeC(0x0c);
					writeC(0x0b);
					writeC(0x0f);
					writeC(0x08);
					writeH(0x0000); // 飽和度 值:2000，飽和度100%
					writeC(type); // 類型
					writeC(0x26);
					writeH(time); // 時間
					writeC(0x00); // 負重度 值:240，負重度100%
				}
				break;
			case MSG_DUEL:
				writeD(type); // 相手のオブジェクトID
				writeD(time); // 自分のオブジェクトID
				break;
			case ICON_MAGIC_DOLL:
				if (type == 32) { // 愛心圖示
					writeH(time);
					writeC(type);
					writeC(12);
				} else { // 魔法娃娃圖示
					writeH(time);
					writeC(0);
					writeC(0);
				}
				break;
			default:
				break;
		}
	}

	public S_PacketBox(int subCode, String name) {
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(subCode);

		switch (subCode) {
			case ADD_EXCLUDE:
			case REM_EXCLUDE:
			case MSG_TOWN_LEADER:
			case HTML_PLEDGE_REALEASE_ANNOUNCE:
				writeS(name);
				break;
			default:
				break;
		}
	}

	public S_PacketBox(int subCode, int id, String name, String clanName) {
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(subCode);

		switch (subCode) {
			case MSG_WIN_LASTAVARD:
				writeD(id); // クランIDか何か？
				writeS(name);
				writeS(clanName);
				break;
				
			default:
				break;
		}
	}
	
	public S_PacketBox(int subCode, int rank, String name) {
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(subCode);

		switch (subCode) {
		case MSG_RANK_CHANGED: // 你的階級變更為%s
			writeC(rank);
			writeS(name);
			break;
		}

	}

	public S_PacketBox(int subCode, Object[] names) {
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(subCode);

		switch (subCode) {
			case ADD_EXCLUDE2:
				writeC(names.length);
				for (Object name : names) {
					writeS(name.toString());
				}
				break;
			case MSG_WAR_OCCUPY_ALL:
				writeC(names.length);
				for (Object name : names) {
					writeS(name.toString());
				}
				break;
			case MSG_WAR_IS_GOING_ALL:
				writeC(names.length);
				for (Object name : names) {
					writeS(name.toString());
				}
				break;
			case HTML_PLEDGE_ONLINE_MEMBERS:
				writeH(names.length);
				for(Object name : names){
					PcInstance pc = (PcInstance)name;
					writeS(pc.getName());
				}
				break;
			default:
				break;
		}
	}
	
	/**
	 * 3.80C 地圖入場剩餘時間
	 * @param subCode MAP_TIME
	 * @param names 地圖名稱
	 * @param value 時間
	 */
	public S_PacketBox(int subCode, Object[] names, int[] value) {
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(subCode);

		switch (subCode) {
		case MAP_TIME:
			writeD(names.length);
			int i = 1;
			for (Object name : names) {
				writeD(i);
				writeS(String.valueOf(name));
				writeD(Integer.valueOf(value[i-1]));
				i++;
			}
			break;
		default:
			break;
		}
	}

	private void callSomething() {
		Iterator<PcInstance> itr = LsimulatorWorld.getInstance().getAllPlayers().iterator();

		writeC(LsimulatorWorld.getInstance().getAllPlayers().size());

		while (itr.hasNext()) {
			PcInstance pc = itr.next();
			Account acc = Account.load(pc.getAccountName());

			// 時間情報 とりあえずログイン時間を入れてみる
			if (acc == null) {
				writeD(0);
			}
			else {
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Config.TIME_ZONE));
				long lastactive = acc.getLastActive().getTime();
				cal.setTimeInMillis(lastactive);
				cal.set(Calendar.YEAR, 1970);
				int time = (int) (cal.getTimeInMillis() / 1000);
				writeD(time); // JST 1970 1/1 09:00 が基準
			}

			// キャラ情報
			writeS(pc.getName()); // 半角12字まで
			writeS(pc.getClanname()); // []内に表示される文字列。半角12字まで
		}
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}

		return _byte;
	}

	@Override
	public String getType() {
		return S_PACKETBOX;
	}
}
