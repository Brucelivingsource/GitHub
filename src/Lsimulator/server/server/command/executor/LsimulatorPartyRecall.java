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
package Lsimulator.server.server.command.executor;

import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.model.LsimulatorParty;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorPartyRecall implements LsimulatorCommandExecutor {
	private static Logger _log = Logger
			.getLogger(LsimulatorPartyRecall.class.getName());

	private LsimulatorPartyRecall() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorPartyRecall();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		LsimulatorPcInstance target = LsimulatorWorld.getInstance().getPlayer(arg);

		if (target != null) {
			LsimulatorParty party = target.getParty();
			if (party != null) {
				int x = pc.getX();
				int y = pc.getY() + 2;
				short map = pc.getMapId();
				LsimulatorPcInstance[] players = party.getMembers();
				for (LsimulatorPcInstance pc2 : players) {
					try {
						LsimulatorTeleport.teleport(pc2, x, y, map, 5, true);
						pc2
								.sendPackets(new S_SystemMessage(
										"您被傳喚到GM身邊。"));
					} catch (Exception e) {
						_log.log(Level.SEVERE, "", e);
					}
				}
			} else {
				pc.sendPackets(new S_SystemMessage("請輸入要召喚的角色名稱。"));
			}
		} else {
			pc.sendPackets(new S_SystemMessage("不再線上。"));
		}
	}
}
