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

import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.serverpackets.S_WhoAmount;

public class LsimulatorWho implements LsimulatorCommandExecutor {
	private LsimulatorWho() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorWho();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			Collection<PcInstance> players = LsimulatorWorld.getInstance().getAllPlayers();
			String amount = String.valueOf(players.size());
			S_WhoAmount s_whoamount = new S_WhoAmount(amount);
			pc.sendPackets(s_whoamount);

			// オンラインのプレイヤーリストを表示
			if (arg.equalsIgnoreCase("all")) {
				pc.sendPackets(new S_SystemMessage("-- 線上玩家 --"));
				StringBuffer buf = new StringBuffer();
				for (PcInstance each : players) {
					buf.append(each.getName());
					buf.append(" / ");
					if (buf.length() > 50) {
						pc.sendPackets(new S_SystemMessage(buf.toString()));
						buf.delete(0, buf.length() - 1);
					}
				}
				if (buf.length() > 0) {
					pc.sendPackets(new S_SystemMessage(buf.toString()));
				}
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入: .who [all] 。"));
		}
	}
}
