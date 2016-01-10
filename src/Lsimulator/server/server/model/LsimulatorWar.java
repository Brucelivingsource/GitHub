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
package Lsimulator.server.server.model;

import java.util.Calendar;
import java.util.List;

import Lsimulator.server.Config;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.WarTimeController;
import Lsimulator.server.server.datatables.CastleTable;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.serverpackets.S_War;
import Lsimulator.server.server.templates.LsimulatorCastle;
import Lsimulator.server.server.utils.collections.Lists;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorWar

public class LsimulatorWar {
	private String _param1 = null;

	private String _param2 = null;

	private final List<String> _attackClanList = Lists.newList();

	private String _defenceClanName = null;

	private int _warType = 0;

	private LsimulatorCastle _castle = null;

	private Calendar _warEndTime;

	private boolean _isWarTimerDelete = false;

	public LsimulatorWar() {
	}

	class CastleWarTimer implements Runnable {
		public CastleWarTimer() {
		}

		@Override
		public void run() {
			for (;;) {
				try {
					Thread.sleep(1000);
					if (_warEndTime.before(WarTimeController.getInstance().getRealTime())) {
						break;
					}
				}
				catch (Exception exception) {
					break;
				}
				if (_isWarTimerDelete) { // 戦争が終結していたらタイマー終了
					return;
				}
			}
			CeaseCastleWar(); // 攻城戦終結処理
			delete();
		}
	}

	class SimWarTimer implements Runnable {
		public SimWarTimer() {
		}

		@Override
		public void run() {
			for (int loop = 0; loop < 240; loop++) { // 240分
				try {
					Thread.sleep(60000);
				}
				catch (Exception exception) {
					break;
				}
				if (_isWarTimerDelete) { // 戦争が終結していたらタイマー終了
					return;
				}
			}
			CeaseWar(_param1, _param2); // 終結
			delete();
		}
	}

	public void handleCommands(int war_type, String attack_clan_name, String defence_clan_name) {
		// war_type - 1:攻城戦 2:模擬戦
		// attack_clan_name - 布告したクラン名
		// defence_clan_name - 布告されたクラン名（攻城戦時は、城主クラン）

		SetWarType(war_type);

		DeclareWar(attack_clan_name, defence_clan_name);

		_param1 = attack_clan_name;
		_param2 = defence_clan_name;
		InitAttackClan();
		AddAttackClan(attack_clan_name);
		SetDefenceClanName(defence_clan_name);

		if (war_type == 1) { // 攻城戦
			GetCastleId();
			_castle = GetCastle();
			if (_castle != null) {
				Calendar cal = (Calendar) _castle.getWarTime().clone();
				cal.add(Config.ALT_WAR_TIME_UNIT, Config.ALT_WAR_TIME);
				_warEndTime = cal;
			}

			CastleWarTimer castle_war_timer = new CastleWarTimer();
			GeneralThreadPool.getInstance().execute(castle_war_timer); // タイマー開始
		}
		else if (war_type == 2) { // 模擬戦
			SimWarTimer sim_war_timer = new SimWarTimer();
			GeneralThreadPool.getInstance().execute(sim_war_timer); // タイマー開始
		}
		LsimulatorWorld.getInstance().addWar(this); // 戦争リストに追加
	}

	private void RequestCastleWar(int type, String clan1_name, String clan2_name) {
		if ((clan1_name == null) || (clan2_name == null)) {
			return;
		}

		LsimulatorClan clan1 = LsimulatorWorld.getInstance().getClan(clan1_name);
		if (clan1 != null) {
			LsimulatorPcInstance clan1_member[] = clan1.getOnlineClanMember();
			for (LsimulatorPcInstance element : clan1_member) {
				element.sendPackets(new S_War(type, clan1_name, clan2_name));
			}
		}

		int attack_clan_num = GetAttackClanListSize();

		if ((type == 1) || (type == 2) || (type == 3)) { // 宣戦布告、降伏、終結
			LsimulatorClan clan2 = LsimulatorWorld.getInstance().getClan(clan2_name);
			if (clan2 != null) {
				LsimulatorPcInstance clan2_member[] = clan2.getOnlineClanMember();
				for (LsimulatorPcInstance element : clan2_member) {
					if (type == 1) { // 宣戦布告
						element.sendPackets(new S_War(type, clan1_name, clan2_name));
					}
					else if (type == 2) { // 降伏
						element.sendPackets(new S_War(type, clan1_name, clan2_name));
						if (attack_clan_num == 1) { // 攻撃側クランが一つ
							element.sendPackets(new S_War(4, clan2_name, clan1_name));
						}
						else {
							element.sendPackets(new S_ServerMessage( // %0血盟が%1血盟に降伏しました。
									228, clan1_name, clan2_name));
							RemoveAttackClan(clan1_name);
						}
					}
					else if (type == 3) { // 終結
						element.sendPackets(new S_War(type, clan1_name, clan2_name));
						if (attack_clan_num == 1) { // 攻撃側クランが一つ
							element.sendPackets(new S_War(4, clan2_name, clan1_name));
						}
						else {
							element.sendPackets(new S_ServerMessage( // %0血盟と%1血盟間の戦争が終結しました。
									227, clan1_name, clan2_name));
							RemoveAttackClan(clan1_name);
						}
					}
				}
			}
		}

		if (((type == 2) || (type == 3)) && (attack_clan_num >= 1)) { // 投降、終止後攻擊方大於或等於一
			_isWarTimerDelete = true;
			delete();
		}
	}

	private void RequestSimWar(int type, String clan1_name, String clan2_name) {
		if ((clan1_name == null) || (clan2_name == null)) {
			return;
		}

		LsimulatorClan clan1 = LsimulatorWorld.getInstance().getClan(clan1_name);
		if (clan1 != null) {
			LsimulatorPcInstance clan1_member[] = clan1.getOnlineClanMember();
			for (LsimulatorPcInstance element : clan1_member) {
				element.sendPackets(new S_War(type, clan1_name, clan2_name));
			}
		}

		if ((type == 1) || (type == 2) || (type == 3)) { // 宣戦布告、降伏、終結
			LsimulatorClan clan2 = LsimulatorWorld.getInstance().getClan(clan2_name);
			if (clan2 != null) {
				LsimulatorPcInstance clan2_member[] = clan2.getOnlineClanMember();
				for (LsimulatorPcInstance element : clan2_member) {
					if (type == 1) { // 宣戦布告
						element.sendPackets(new S_War(type, clan1_name, clan2_name));
					}
					else if ((type == 2) || (type == 3)) { // 降伏、終結
						element.sendPackets(new S_War(type, clan1_name, clan2_name));
						element.sendPackets(new S_War(4, clan2_name, clan1_name));
					}
				}
			}
		}

		if ((type == 2) || (type == 3)) { // 降伏、終結
			_isWarTimerDelete = true;
			delete();
		}
	}

	public void WinCastleWar(String clan_name) { // クラウンを奪取して、攻撃側クランが勝利
		String defence_clan_name = GetDefenceClanName();
		// %0血盟が%1血盟との戦争で勝利しました。
		LsimulatorWorld.getInstance().broadcastPacketToAll(new S_ServerMessage(231, clan_name, defence_clan_name));

		LsimulatorClan defence_clan = LsimulatorWorld.getInstance().getClan(defence_clan_name);
		if (defence_clan != null) {
			LsimulatorPcInstance defence_clan_member[] = defence_clan.getOnlineClanMember();
			for (LsimulatorPcInstance element : defence_clan_member) {
				for (String clanName : GetAttackClanList()) {
					element.sendPackets(new S_War(3, defence_clan_name, clanName));
				}
			}
		}

		String clanList[] = GetAttackClanList();
		for (String element : clanList) {
			if (element != null) {
				LsimulatorWorld.getInstance().broadcastPacketToAll(new S_ServerMessage( // %0血盟と%1血盟間の戦争が終結しました。
						227, defence_clan_name, element));
				LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(element);
				if (clan != null) {
					LsimulatorPcInstance clan_member[] = clan.getOnlineClanMember();
					for (LsimulatorPcInstance element2 : clan_member) {
						element2.sendPackets(new S_War(3, element, defence_clan_name));
					}
				}
			}
		}

		_isWarTimerDelete = true;
		delete();
	}

	public void CeaseCastleWar() { // 戦争時間満了し、防衛側クランが勝利
		String defence_clan_name = GetDefenceClanName();
		String clanList[] = GetAttackClanList();
		if (defence_clan_name != null) {
			LsimulatorWorld.getInstance().broadcastPacketToAll(new S_ServerMessage( // %0血盟が%1血盟との戦争で勝利しました。
					231, defence_clan_name, clanList[0]));
		}

		LsimulatorClan defence_clan = LsimulatorWorld.getInstance().getClan(defence_clan_name);
		if (defence_clan != null) {
			LsimulatorPcInstance defence_clan_member[] = defence_clan.getOnlineClanMember();
			for (LsimulatorPcInstance element : defence_clan_member) {
				element.sendPackets(new S_War(4, defence_clan_name, clanList[0]));
			}
		}

		for (String element : clanList) {
			if (element != null) {
				LsimulatorWorld.getInstance().broadcastPacketToAll(new S_ServerMessage( // %0血盟と%1血盟間の戦争が終結しました。
						227, defence_clan_name, element));
				LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(element);
				if (clan != null) {
					LsimulatorPcInstance clan_member[] = clan.getOnlineClanMember();
					for (LsimulatorPcInstance element2 : clan_member) {
						element2.sendPackets(new S_War(3, element, defence_clan_name));
					}
				}
			}
		}

		_isWarTimerDelete = true;
		delete();
	}

	public void DeclareWar(String clan1_name, String clan2_name) { // _血盟が_血盟に宣戦布告しました。
		if (GetWarType() == 1) { // 攻城戦
			RequestCastleWar(1, clan1_name, clan2_name);
		}
		else { // 模擬戦
			RequestSimWar(1, clan1_name, clan2_name);
		}
	}

	public void SurrenderWar(String clan1_name, String clan2_name) { // _血盟が_血盟に降伏しました。
		if (GetWarType() == 1) {
			RequestCastleWar(2, clan1_name, clan2_name);
		}
		else {
			RequestSimWar(2, clan1_name, clan2_name);
		}
	}

	public void CeaseWar(String clan1_name, String clan2_name) { // _血盟と_血盟との戦争が終結しました。
		if (GetWarType() == 1) {
			RequestCastleWar(3, clan1_name, clan2_name);
		}
		else {
			RequestSimWar(3, clan1_name, clan2_name);
		}
	}

	public void WinWar(String clan1_name, String clan2_name) { // _血盟が_血盟との戦争で勝利しました。
		if (GetWarType() == 1) {
			RequestCastleWar(4, clan1_name, clan2_name);
		}
		else {
			RequestSimWar(4, clan1_name, clan2_name);
		}
	}

	public boolean CheckClanInWar(String clan_name) { // クランが戦争に参加しているかチェックする
		boolean ret;
		if (GetDefenceClanName().toLowerCase().equals(clan_name.toLowerCase())) { // 防衛側クランをチェック
			ret = true;
		}
		else {
			ret = CheckAttackClan(clan_name); // 攻撃側クランをチェック
		}
		return ret;
	}

	public boolean CheckClanInSameWar(String player_clan_name, String target_clan_name) { // 自クランと相手クランが同じ戦争に参加しているかチェックする（同じクランの場合も含む）
		boolean player_clan_flag;
		boolean target_clan_flag;

		if (GetDefenceClanName().toLowerCase().equals(player_clan_name.toLowerCase())) { // 自クランに対して防衛側クランをチェック
			player_clan_flag = true;
		}
		else {
			player_clan_flag = CheckAttackClan(player_clan_name); // 自クランに対して攻撃側クランをチェック
		}

		if (GetDefenceClanName().toLowerCase().equals(target_clan_name.toLowerCase())) { // 相手クランに対して防衛側クランをチェック
			target_clan_flag = true;
		}
		else {
			target_clan_flag = CheckAttackClan(target_clan_name); // 相手クランに対して攻撃側クランをチェック
		}

		if ( player_clan_flag && target_clan_flag ) {
			return true;
		}
		else {
			return false;
		}
	}

	public String GetEnemyClanName(String player_clan_name) { // 相手のクラン名を取得する
		String enemy_clan_name = null;
		if (GetDefenceClanName().toLowerCase().equals(player_clan_name.toLowerCase())) { // 自クランが防衛側
			String clanList[] = GetAttackClanList();
			for (String element : clanList) {
				if (element != null) {
					enemy_clan_name = element;
					return enemy_clan_name; // リストの先頭のクラン名を返す
				}
			}
		}
		else { // 自クランが攻撃側
			enemy_clan_name = GetDefenceClanName();
			return enemy_clan_name;
		}
		return enemy_clan_name;
	}

	public void delete() {
		LsimulatorWorld.getInstance().removeWar(this); // 戦争リストから削除
	}

	public int GetWarType() {
		return _warType;
	}

	public void SetWarType(int war_type) {
		_warType = war_type;
	}

	public String GetDefenceClanName() {
		return _defenceClanName;
	}

	public void SetDefenceClanName(String defence_clan_name) {
		_defenceClanName = defence_clan_name;
	}

	public void InitAttackClan() {
		_attackClanList.clear();
	}

	public void AddAttackClan(String attack_clan_name) {
		if (!_attackClanList.contains(attack_clan_name)) {
			_attackClanList.add(attack_clan_name);
		}
	}

	public void RemoveAttackClan(String attack_clan_name) {
		if (_attackClanList.contains(attack_clan_name)) {
			_attackClanList.remove(attack_clan_name);
		}
	}

	public boolean CheckAttackClan(String attack_clan_name) {
		if (_attackClanList.contains(attack_clan_name)) {
			return true;
		}
		return false;
	}

	public String[] GetAttackClanList() {
		return _attackClanList.toArray(new String[_attackClanList.size()]);
	}

	public int GetAttackClanListSize() {
		return _attackClanList.size();
	}

	public int GetCastleId() {
		int castle_id = 0;
		if (GetWarType() == 1) { // 攻城戦
			LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(GetDefenceClanName());
			if (clan != null) {
				castle_id = clan.getCastleId();
			}
		}
		return castle_id;
	}

	public LsimulatorCastle GetCastle() {
		LsimulatorCastle l1castle = null;
		if (GetWarType() == 1) { // 攻城戦
			LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(GetDefenceClanName());
			if (clan != null) {
				int castle_id = clan.getCastleId();
				l1castle = CastleTable.getInstance().getCastleTable(castle_id);
			}
		}
		return l1castle;
	}
}
