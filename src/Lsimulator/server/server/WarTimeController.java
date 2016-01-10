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
package Lsimulator.server.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import Lsimulator.server.Config;
import Lsimulator.server.server.datatables.CastleTable;
import Lsimulator.server.server.datatables.DoorTable;
import Lsimulator.server.server.model.LsimulatorCastleLocation;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.LsimulatorWarSpawn;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorCrownInstance;
import Lsimulator.server.server.model.Instance.LsimulatorDoorInstance;
import Lsimulator.server.server.model.Instance.LsimulatorFieldObjectInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorTowerInstance;
import Lsimulator.server.server.serverpackets.S_PacketBox;
import Lsimulator.server.server.templates.LsimulatorCastle;

public class WarTimeController implements Runnable {
	private static WarTimeController _instance;

	private LsimulatorCastle[] _l1castle = new LsimulatorCastle[8];

	private Calendar[] _war_start_time = new Calendar[8];

	private Calendar[] _war_end_time = new Calendar[8];

	private boolean[] _is_now_war = new boolean[8];

	private WarTimeController() {
		for (int i = 0; i < _l1castle.length; i++) {
			_l1castle[i] = CastleTable.getInstance().getCastleTable(i + 1);
			_war_start_time[i] = _l1castle[i].getWarTime();
			_war_end_time[i] = (Calendar) _l1castle[i].getWarTime().clone();
			_war_end_time[i].add(Config.ALT_WAR_TIME_UNIT, Config.ALT_WAR_TIME);
		}
	}

	public static WarTimeController getInstance() {
		if (_instance == null) {
			_instance = new WarTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				checkWarTime(); // 檢查攻城時間
				Thread.sleep(1000);
			}
		} catch (Exception e1) {
		}
	}

	public Calendar getRealTime() {
		TimeZone _tz = TimeZone.getTimeZone(Config.TIME_ZONE);
		Calendar cal = Calendar.getInstance(_tz);
		return cal;
	}

	public boolean isNowWar(int castle_id) {
		return _is_now_war[castle_id - 1];
	}

	// TODO 
	public void checkCastleWar(LsimulatorPcInstance player) {
		List<String> castle = new ArrayList<String>();
		for (int i = 0; i < 8; i++) {
			if (_is_now_war[i]) {
				castle.add(CastleTable.getInstance().getCastleTable(i+1).getName());
				// 攻城戰進行中。
				player.sendPackets(new S_PacketBox(S_PacketBox.MSG_WAR_IS_GOING_ALL, castle.toArray()));
			}
		}
	}

	private void checkWarTime() {
		for (int i = 0; i < 8; i++) {
			if (_war_start_time[i].before(getRealTime()) // 攻城開始
					&& _war_end_time[i].after(getRealTime())) {
				if (_is_now_war[i] == false) {
					_is_now_war[i] = true;
					// 招出攻城的旗子
					LsimulatorWarSpawn warspawn = new LsimulatorWarSpawn();
					warspawn.SpawnFlag(i + 1);
					// 修理城門並設定為關閉
					for (LsimulatorDoorInstance door : DoorTable.getInstance().getDoorList()) {
						if (LsimulatorCastleLocation.checkInWarArea(i + 1, door)) {
							door.repairGate();
						}
					}

					LsimulatorWorld.getInstance().broadcastPacketToAll(new S_PacketBox(S_PacketBox.MSG_WAR_BEGIN, i + 1)); // %sの攻城戦が始まりました。
					int[] loc = new int[3];
					for (LsimulatorPcInstance pc : LsimulatorWorld.getInstance().getAllPlayers()) {
						int castleId = i + 1;
						if (LsimulatorCastleLocation.checkInWarArea(castleId, pc)&& !pc.isGm()) { // 剛好在攻城範圍內
							LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
							if (clan != null) {
								if (clan.getCastleId() == castleId) { // 如果是城血盟
									continue;
								}
							}
							loc = LsimulatorCastleLocation.getGetBackLoc(castleId);
							LsimulatorTeleport.teleport(pc, loc[0], loc[1],(short) loc[2], 5, true);
						}
					}
				}
			} else if (_war_end_time[i].before(getRealTime())) { // 攻城結束
				if (_is_now_war[i] == true) {
					_is_now_war[i] = false;
					LsimulatorWorld.getInstance().broadcastPacketToAll(
							new S_PacketBox(S_PacketBox.MSG_WAR_END, i + 1)); // %sの攻城戦が終了しました。
					// 更新攻城時間
					WarUpdate(i);

					int castle_id = i + 1;
					for (LsimulatorObject l1object : LsimulatorWorld.getInstance().getObject()) {
						// 取消攻城的旗子
						if (l1object instanceof LsimulatorFieldObjectInstance) {
							LsimulatorFieldObjectInstance flag = (LsimulatorFieldObjectInstance) l1object;
							if (LsimulatorCastleLocation.checkInWarArea(castle_id, flag)) {
								flag.deleteMe();
							}
						}
						// 移除皇冠
						if (l1object instanceof LsimulatorCrownInstance) {
							LsimulatorCrownInstance crown = (LsimulatorCrownInstance) l1object;
							if (LsimulatorCastleLocation.checkInWarArea(castle_id,
									crown)) {
								crown.deleteMe();
							}
						}
						// 移除守護塔
						if (l1object instanceof LsimulatorTowerInstance) {
							LsimulatorTowerInstance tower = (LsimulatorTowerInstance) l1object;
							if (LsimulatorCastleLocation.checkInWarArea(castle_id,tower)) {
								tower.deleteMe();
							}
						}
					}
					// 塔重新出現
					LsimulatorWarSpawn warspawn = new LsimulatorWarSpawn();
					warspawn.SpawnTower(castle_id);

					// 移除城門
					for (LsimulatorDoorInstance door : DoorTable.getInstance().getDoorList()) {
						if (LsimulatorCastleLocation.checkInWarArea(castle_id, door)) {
							door.repairGate();
						}
					}
				} else { // 更新過期的攻城時間
					_war_start_time[i] = getRealTime();
					_war_end_time[i] = (Calendar) _war_start_time[i].clone();
					WarUpdate(i);
				} 
			}

		}
	}

	private void WarUpdate(int i) {
		_war_start_time[i].add(Config.ALT_WAR_INTERVAL_UNIT,Config.ALT_WAR_INTERVAL);
		_war_end_time[i].add(Config.ALT_WAR_INTERVAL_UNIT,Config.ALT_WAR_INTERVAL);
		_l1castle[i].setWarTime(_war_start_time[i]);
		_l1castle[i].setTaxRate(10); // 稅率10%
		_l1castle[i].setPublicMoney(0); // 清除城堡稅收
		CastleTable.getInstance().updateCastle(_l1castle[i]);
	}
}
