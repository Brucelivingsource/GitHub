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
package Lsimulator.server.server.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.LsimulatorDragonSlayer;
import Lsimulator.server.server.model.LsimulatorNpcDeleteTimer;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_CharVisualUpdate;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_NPCPack;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

public class LsimulatorSpawnUtil {
	private static Logger _log = Logger.getLogger(LsimulatorSpawnUtil.class.getName());

	public static void spawn(LsimulatorPcInstance pc, int npcId, int randomRange,
			int timeMillisToDelete) {
		try {
			LsimulatorNpcInstance npc = NpcTable.getInstance().newNpcInstance(npcId);
			npc.setId(IdFactory.getInstance().nextId());
			npc.setMap(pc.getMapId());
			if (randomRange == 0) {
				npc.getLocation().set(pc.getLocation());
				npc.getLocation().forward(pc.getHeading());
			} else {
				int tryCount = 0;
				do {
					tryCount++;
					npc.setX(pc.getX() + Random.nextInt(randomRange) - Random.nextInt(randomRange));
					npc.setY(pc.getY() + Random.nextInt(randomRange) - Random.nextInt(randomRange));
					if (npc.getMap().isInMap(npc.getLocation())
							&& npc.getMap().isPassable(npc.getLocation())) {
						break;
					}
					Thread.sleep(1);
				} while (tryCount < 50);

				if (tryCount >= 50) {
					npc.getLocation().set(pc.getLocation());
					npc.getLocation().forward(pc.getHeading());
				}
			}

			npc.setHomeX(npc.getX());
			npc.setHomeY(npc.getY());
			npc.setHeading(pc.getHeading());
			// 紀錄龍之門扉編號
			if (npc.getNpcId() == 81273) {
				for (int i = 0; i < 6; i++) {
					if (!LsimulatorDragonSlayer.getInstance().getPortalNumber()[i]) {
						LsimulatorDragonSlayer.getInstance().setPortalNumber(i, true);
						// 重置副本
						LsimulatorDragonSlayer.getInstance().resetDragonSlayer(i);
						npc.setPortalNumber(i);
						LsimulatorDragonSlayer.getInstance().portalPack()[i] = npc;
						break;
					}
				}
			} else if (npc.getNpcId() == 81274) {
				for (int i = 6; i < 12; i++) {
					if (!LsimulatorDragonSlayer.getInstance().getPortalNumber()[i]) {
						LsimulatorDragonSlayer.getInstance().setPortalNumber(i, true);
						// 重置副本
						LsimulatorDragonSlayer.getInstance().resetDragonSlayer(i);
						npc.setPortalNumber(i);
						LsimulatorDragonSlayer.getInstance().portalPack()[i] = npc;
						break;
					}
				}
			}
			LsimulatorWorld.getInstance().storeObject(npc);
			LsimulatorWorld.getInstance().addVisibleObject(npc);

			if (npc.getTempCharGfx() == 7548 || npc.getTempCharGfx() == 7550 || npc.getTempCharGfx() == 7552
					|| npc.getTempCharGfx() == 7554 || npc.getTempCharGfx() == 7585 || npc.getTempCharGfx() == 7591) {
				npc.broadcastPacket(new S_NPCPack(npc));
				npc.broadcastPacket(new S_DoActionGFX(npc.getId(), ActionCodes.ACTION_AxeWalk));
			} else if (npc.getTempCharGfx() == 7539 || npc.getTempCharGfx() == 7557 || npc.getTempCharGfx() == 7558
					|| npc.getTempCharGfx() == 7864 || npc.getTempCharGfx() == 7869 || npc.getTempCharGfx() == 7870) {
				for (LsimulatorPcInstance _pc : LsimulatorWorld.getInstance().getVisiblePlayer(npc, 50)) {
					if (npc.getTempCharGfx() == 7539) {
						_pc.sendPackets(new S_ServerMessage(1570));
					} else if (npc.getTempCharGfx() == 7864) {
						_pc.sendPackets(new S_ServerMessage(1657));
					}
					npc.onPerceive(_pc);
					S_DoActionGFX gfx = new S_DoActionGFX(npc.getId(), ActionCodes.ACTION_AxeWalk);
					_pc.sendPackets(gfx);
				}
				npc.npcSleepTime(ActionCodes.ACTION_AxeWalk, LsimulatorNpcInstance.ATTACK_SPEED);
			} else if (npc.getTempCharGfx() == 145) { // 史巴托
				npc.setStatus(11);
				npc.broadcastPacket(new S_NPCPack(npc));
				npc.broadcastPacket(new S_DoActionGFX(npc.getId(), ActionCodes.ACTION_Appear));
				npc.setStatus(0);
				npc.broadcastPacket(new S_CharVisualUpdate(npc, npc.getStatus()));
			}

			npc.turnOnOffLight();
			npc.startChat(LsimulatorNpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
			if (0 < timeMillisToDelete) {
				LsimulatorNpcDeleteTimer timer = new LsimulatorNpcDeleteTimer(npc,
						timeMillisToDelete);
				timer.begin();
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
