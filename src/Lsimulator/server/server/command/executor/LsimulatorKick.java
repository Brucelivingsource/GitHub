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

import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_Disconnect;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorKick implements LsimulatorCommandExecutor {
	private LsimulatorKick() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorKick();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			LsimulatorPcInstance target = LsimulatorWorld.getInstance().getPlayer(arg);

			if (target != null) {
				pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(target.getName()).append("已被您強制踢除遊戲。").toString()));
				target.sendPackets(new S_Disconnect());
			}
			else {
				pc.sendPackets(new S_SystemMessage("您指定的該玩家名稱不存在。"));
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 : " + cmdName + " 玩家名稱。"));
		}
	}
}
