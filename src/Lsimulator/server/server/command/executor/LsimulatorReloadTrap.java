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

import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.trap.LsimulatorWorldTraps;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorReloadTrap implements LsimulatorCommandExecutor {
	private LsimulatorReloadTrap() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorReloadTrap();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		LsimulatorWorldTraps.reloadTraps();
		pc.sendPackets(new S_SystemMessage("已重新讀取陷阱資料"));
	}
}
