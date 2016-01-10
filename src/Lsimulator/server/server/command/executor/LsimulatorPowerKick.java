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

import Lsimulator.server.server.datatables.IpTable;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_Disconnect;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorPowerKick implements LsimulatorCommandExecutor {
	private LsimulatorPowerKick() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorPowerKick();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			LsimulatorPcInstance target = LsimulatorWorld.getInstance().getPlayer(arg);

			IpTable iptable = IpTable.getInstance();
			if (target != null) {
				iptable.banIp(target.getNetConnection().getIp()); // 加入IP至BAN名單
				pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(target.getName()).append("被您強制踢除遊戲並封鎖IP。").toString()));
				target.sendPackets(new S_Disconnect());
			}
			else {
				pc.sendPackets(new S_SystemMessage("您指定的腳色名稱不存在。"));
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 : " + cmdName + " 玩家名稱。"));
		}
	}
}
