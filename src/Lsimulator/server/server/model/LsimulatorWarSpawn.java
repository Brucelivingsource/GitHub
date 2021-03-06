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

import java.lang.reflect.Constructor;

import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_NPCPack;
import Lsimulator.server.server.templates.LsimulatorNpc;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorWarSpawn

public class LsimulatorWarSpawn {
	private static LsimulatorWarSpawn _instance;

	private Constructor<?> _constructor;

	public LsimulatorWarSpawn() {
	}

	public static LsimulatorWarSpawn getInstance() {
		if (_instance == null) {
			_instance = new LsimulatorWarSpawn();
		}
		return _instance;
	}

	public void SpawnTower(int castleId) {
		int npcId = 81111;
		if (castleId == LsimulatorCastleLocation.ADEN_CASTLE_ID) {
			npcId = 81189;
		}
		LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(npcId); // ガーディアンタワー
		int[] loc = new int[3];
		loc = LsimulatorCastleLocation.getTowerLoc(castleId);
		SpawnWarObject(l1npc, loc[0], loc[1], (short) (loc[2]));
		if (castleId == LsimulatorCastleLocation.ADEN_CASTLE_ID) {
			spawnSubTower();
		}
	}

	private void spawnSubTower() {
		LsimulatorNpc l1npc;
		int[] loc = new int[3];
		for (int i = 1; i <= 4; i++) {
			l1npc = NpcTable.getInstance().getTemplate(81189 + i); // サブタワー
			loc = LsimulatorCastleLocation.getSubTowerLoc(i);
			SpawnWarObject(l1npc, loc[0], loc[1], (short) (loc[2]));
		}
	}

	public void SpawnCrown(int castleId) {
		LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(81125); // クラウン
		int[] loc = new int[3];
		loc = LsimulatorCastleLocation.getTowerLoc(castleId);
		SpawnWarObject(l1npc, loc[0], loc[1], (short) (loc[2]));
	}

	public void SpawnFlag(int castleId) {
		LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(81122); // 旗
		int[] loc = new int[5];
		loc = LsimulatorCastleLocation.getWarArea(castleId);
		int x = 0;
		int y = 0;
		int locx1 = loc[0];
		int locx2 = loc[1];
		int locy1 = loc[2];
		int locy2 = loc[3];
		short mapid = (short) loc[4];

		for (x = locx1, y = locy1; x <= locx2; x += 8) {
			SpawnWarObject(l1npc, x, y, mapid);
		}
		for (x = locx2, y = locy1; y <= locy2; y += 8) {
			SpawnWarObject(l1npc, x, y, mapid);
		}
		for (x = locx2, y = locy2; x >= locx1; x -= 8) {
			SpawnWarObject(l1npc, x, y, mapid);
		}
		for (x = locx1, y = locy2; y >= locy1; y -= 8) {
			SpawnWarObject(l1npc, x, y, mapid);
		}
	}

	private void SpawnWarObject(LsimulatorNpc l1npc, int locx, int locy, short mapid) {
		try {
			if (l1npc != null) {
				String s = l1npc.getImpl();
				_constructor = Class.forName(
						(new StringBuilder()).append("Lsimulator.server.server.model.Instance.").append(s).append("Instance").toString()).getConstructors()[0];
				Object aobj[] =
				{ l1npc };
				NpcInstance npc = (NpcInstance) _constructor.newInstance(aobj);
				npc.setId(IdFactory.getInstance().nextId());
				npc.setX(locx);
				npc.setY(locy);
				npc.setHomeX(locx);
				npc.setHomeY(locy);
				npc.setHeading(0);
				npc.setMap(mapid);
				LsimulatorWorld.getInstance().storeObject(npc);
				LsimulatorWorld.getInstance().addVisibleObject(npc);

				for (PcInstance pc : LsimulatorWorld.getInstance().getAllPlayers()) {
					npc.addKnownObject(pc);
					pc.addKnownObject(npc);
					pc.sendPackets(new S_NPCPack(npc));
					pc.broadcastPacket(new S_NPCPack(npc));
				}
			}
		}
		catch (Exception exception) {}
	}
}
