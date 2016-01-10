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

import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.skill.LsimulatorBuffUtil;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorSpeed implements LsimulatorCommandExecutor {
	private LsimulatorSpeed() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorSpeed();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			LsimulatorBuffUtil.haste(pc, 3600 * 1000);
			LsimulatorBuffUtil.brave(pc, 3600 * 1000);
			LsimulatorBuffUtil.thirdSpeed(pc);
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(".speed 指令錯誤"));
		}
	}
}