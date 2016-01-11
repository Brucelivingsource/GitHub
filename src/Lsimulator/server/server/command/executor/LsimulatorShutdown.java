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

import Lsimulator.server.server.GameServer;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorShutdown implements LsimulatorCommandExecutor {
	private LsimulatorShutdown() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorShutdown();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			if (arg.equalsIgnoreCase("now")) {
				GameServer.getInstance().shutdown();
				return;
			}
			if (arg.equalsIgnoreCase("abort")) {
				GameServer.getInstance().abortShutdown();
				return;
			}
			int sec = Math.max(5, Integer.parseInt(arg));
			GameServer.getInstance().shutdownWithCountdown(sec);
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入: .shutdown sec|now|abort 。"));
		}
	}
}
