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

import Lsimulator.server.server.GMCommandsConfig;
import Lsimulator.server.server.model.LsimulatorLocation;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorGMRoom implements LsimulatorCommandExecutor {
	private LsimulatorGMRoom() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorGMRoom();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			int i = 0;
			try {
				i = Integer.parseInt(arg);
			}
			catch (NumberFormatException e) {}

			if (i == 1) {
				LsimulatorTeleport.teleport(pc, 32737, 32796, (short) 99, 5, false);
			}
			else if (i == 2) {
				LsimulatorTeleport.teleport(pc, 32734, 32799, (short) 17100, 5, false); // 17100!?
			}
			else if (i == 3) {
				LsimulatorTeleport.teleport(pc, 32644, 32955, (short) 0, 5, false);
			}
			else if (i == 4) {
				LsimulatorTeleport.teleport(pc, 33429, 32814, (short) 4, 5, false);
			}
			else if (i == 5) {
				LsimulatorTeleport.teleport(pc, 32894, 32535, (short) 300, 5, false);
			}
			else {
				LsimulatorLocation loc = GMCommandsConfig.ROOMS.get(arg.toLowerCase());
				if (loc == null) {
					pc.sendPackets(new S_SystemMessage(arg + " 未定義的Room～"));
					return;
				}
				LsimulatorTeleport.teleport(pc, loc.getX(), loc.getY(), (short) loc.getMapId(), 5, false);
			}
		}
		catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage("請輸入 .gmroom1～.gmroom5 or .gmroom name 。"));
		}
	}
}
