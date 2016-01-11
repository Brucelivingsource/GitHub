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

import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorToPC implements LsimulatorCommandExecutor {
	private LsimulatorToPC() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorToPC();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			PcInstance target = LsimulatorWorld.getInstance().getPlayer(arg);

			if (target != null) {
				LsimulatorTeleport.teleport(pc, target.getX(), target.getY(), target.getMapId(), 5, false);
				pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(arg).append("移動到玩家身邊。").toString()));
			}
			else {
				pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(arg).append("不在線上。").toString()));
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入: " + cmdName + " 玩家名稱 。"));
		}
	}
}
