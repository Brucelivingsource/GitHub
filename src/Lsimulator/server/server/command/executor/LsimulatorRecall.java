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

import java.util.Collection;

import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.utils.collections.Lists;

public class LsimulatorRecall implements LsimulatorCommandExecutor {
	private LsimulatorRecall() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorRecall();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			Collection<PcInstance> targets = null;
			if (arg.equalsIgnoreCase("all")) {
				targets = LsimulatorWorld.getInstance().getAllPlayers();
			}
			else {
				targets = Lists.newList();
				PcInstance tg = LsimulatorWorld.getInstance().getPlayer(arg);
				if (tg == null) {
					pc.sendPackets(new S_SystemMessage("ID不存在。"));
					return;
				}
				targets.add(tg);
			}

			for (PcInstance target : targets) {
				if (target.isGm()) {
					continue;
				}
				LsimulatorTeleport.teleportToTargetFront(target, pc, 2);
				pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(target.getName()).append("成功被您召喚回來。").toString()));
				target.sendPackets(new S_SystemMessage("您被召喚到GM身邊。"));
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入: " + cmdName + " all|玩家名稱。"));
		}
	}
}
