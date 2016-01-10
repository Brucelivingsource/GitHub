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

import Lsimulator.server.server.datatables.LightSpawnTable;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorFieldObjectInstance;
import Lsimulator.server.server.model.gametime.LsimulatorGameTimeClock;

public class LightTimeController implements Runnable {
	private static LightTimeController _instance;

	private boolean isSpawn = false;

	public static LightTimeController getInstance() {
		if (_instance == null) {
			_instance = new LightTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				checkLightTime();
				Thread.sleep(60000);
			}
		} catch (Exception e1) {
		}
	}

	private void checkLightTime() {
		int serverTime = LsimulatorGameTimeClock.getInstance().currentTime()
				.getSeconds();
		int nowTime = serverTime % 86400;
		if ((nowTime >= ((5 * 3600) + 3300))
				&& (nowTime < ((17 * 3600) + 3300))) { // 5:55~17:55
			if (isSpawn) {
				isSpawn = false;
				for (LsimulatorObject object : LsimulatorWorld.getInstance().getObject()) {
					if (object instanceof LsimulatorFieldObjectInstance) {
						LsimulatorFieldObjectInstance npc = (LsimulatorFieldObjectInstance) object;
						if (((npc.getNpcTemplate().get_npcId() == 81177)
								|| (npc.getNpcTemplate().get_npcId() == 81178)
								|| (npc.getNpcTemplate().get_npcId() == 81179)
								|| (npc.getNpcTemplate().get_npcId() == 81180) || (npc
								.getNpcTemplate().get_npcId() == 81181))
								&& ((npc.getMapId() == 0) || (npc.getMapId() == 4))) {
							npc.deleteMe();
						}
					}
				}
			}
		} else if (((nowTime >= ((17 * 3600) + 3300)) && (nowTime <= 24 * 3600))
				|| ((nowTime >= 0 * 3600) && (nowTime < ((5 * 3600) + 3300)))) { // 17:55~24:00,0:00~5:55
			if (!isSpawn) {
				isSpawn = true;
				LightSpawnTable.getInstance();
			}
		}
	}

}
