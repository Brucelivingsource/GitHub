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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.GMSTATUS_SHOWTRAPS;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorTrapInstance;
import Lsimulator.server.server.serverpackets.S_RemoveObject;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorShowTrap implements LsimulatorCommandExecutor {
	private LsimulatorShowTrap() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorShowTrap();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		if (arg.equalsIgnoreCase("on")) {
			pc.setSkillEffect(GMSTATUS_SHOWTRAPS, 0);
		}
		else if (arg.equalsIgnoreCase("off")) {
			pc.removeSkillEffect(GMSTATUS_SHOWTRAPS);

			for (LsimulatorObject obj : pc.getKnownObjects()) {
				if (obj instanceof LsimulatorTrapInstance) {
					pc.removeKnownObject(obj);
					pc.sendPackets(new S_RemoveObject(obj));
				}
			}
		}
		else {
			pc.sendPackets(new S_SystemMessage("請輸入: " + cmdName + " on|off 。"));
		}
	}
}
