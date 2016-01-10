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

import Lsimulator.server.server.utils.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.MobGroupTable;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.LsimulatorMobGroupInfo;
import Lsimulator.server.server.model.Instance.LsimulatorMonsterInstance;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.templates.LsimulatorMobGroup;
import Lsimulator.server.server.templates.LsimulatorNpcCount;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorMobGroupSpawn

public class LsimulatorMobGroupSpawn {

	private static final Logger _log = Logger.getLogger(LsimulatorMobGroupSpawn.class
			.getName());

	private static LsimulatorMobGroupSpawn _instance;

	private boolean _isRespawnScreen;

	private boolean _isInitSpawn;

	private LsimulatorMobGroupSpawn() {
	}

	public static LsimulatorMobGroupSpawn getInstance() {
		if (_instance == null) {
			_instance = new LsimulatorMobGroupSpawn();
		}
		return _instance;
	}

	public void doSpawn(LsimulatorNpcInstance leader, int groupId,
			boolean isRespawnScreen, boolean isInitSpawn) {

		LsimulatorMobGroup mobGroup = MobGroupTable.getInstance().getTemplate(groupId);
		if (mobGroup == null) {
			return;
		}

		LsimulatorNpcInstance mob;
		_isRespawnScreen = isRespawnScreen;
		_isInitSpawn = isInitSpawn;

		LsimulatorMobGroupInfo mobGroupInfo = new LsimulatorMobGroupInfo();

		mobGroupInfo.setRemoveGroup(mobGroup.isRemoveGroupIfLeaderDie());
		mobGroupInfo.addMember(leader);

		for (LsimulatorNpcCount minion : mobGroup.getMinions()) {
			if (minion.isZero()) {
				continue;
			}
			for (int i = 0; i < minion.getCount(); i++) {
				mob = spawn(leader, minion.getId());
				if (mob != null) {
					mobGroupInfo.addMember(mob);
				}
			}
		}
	}

	private LsimulatorNpcInstance spawn(LsimulatorNpcInstance leader, int npcId) {
		LsimulatorNpcInstance mob = null;
		try {
			mob = NpcTable.getInstance().newNpcInstance(npcId);

			mob.setId(IdFactory.getInstance().nextId());

			mob.setHeading(leader.getHeading());
			mob.setMap(leader.getMapId());
			mob.setMovementDistance(leader.getMovementDistance());
			mob.setRest(leader.isRest());

			mob.setX(leader.getX() + Random.nextInt(5) - 2);
			mob.setY(leader.getY() + Random.nextInt(5) - 2);
			// マップ外、障害物上、画面内沸き不可で画面内にPCがいる場合、リーダーと同じ座標
			if (!canSpawn(mob)) {
				mob.setX(leader.getX());
				mob.setY(leader.getY());
			}
			mob.setHomeX(mob.getX());
			mob.setHomeY(mob.getY());

			if (mob instanceof LsimulatorMonsterInstance) {
				((LsimulatorMonsterInstance) mob).initHideForMinion(leader);
			}

			mob.setSpawn(leader.getSpawn());
			mob.setreSpawn(leader.isReSpawn());
			mob.setSpawnNumber(leader.getSpawnNumber());

			if (mob instanceof LsimulatorMonsterInstance) {
				if (mob.getMapId() == 666) {
					((LsimulatorMonsterInstance) mob).set_storeDroped(true);
				}
			}

			LsimulatorWorld.getInstance().storeObject(mob);
			LsimulatorWorld.getInstance().addVisibleObject(mob);

			if (mob instanceof LsimulatorMonsterInstance) {
				if (!_isInitSpawn && mob.getHiddenStatus() == 0) {
					mob.onNpcAI(); // モンスターのＡＩを開始
				}
			}
			mob.turnOnOffLight();
			mob.startChat(LsimulatorNpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return mob;
	}

	private boolean canSpawn(LsimulatorNpcInstance mob) {
		if (mob.getMap().isInMap(mob.getLocation())
				&& mob.getMap().isPassable(mob.getLocation())) {
			if (_isRespawnScreen) {
				return true;
			}
			if (LsimulatorWorld.getInstance().getVisiblePlayer(mob).isEmpty()) {
				return true;
			}
		}
		return false;
	}

}
