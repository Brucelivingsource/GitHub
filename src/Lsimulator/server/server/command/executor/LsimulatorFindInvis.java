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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.GMSTATUS_FINDINVIS;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_RemoveObject;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorFindInvis implements LsimulatorCommandExecutor {
	private LsimulatorFindInvis() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorFindInvis();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		if (arg.equalsIgnoreCase("on")) {
			pc.setSkillEffect(GMSTATUS_FINDINVIS, 0);
			pc.removeAllKnownObjects();
			pc.updateObject();
		}
		else if (arg.equalsIgnoreCase("off")) {
			pc.removeSkillEffect(GMSTATUS_FINDINVIS);
			for (PcInstance visible : LsimulatorWorld.getInstance().getVisiblePlayer(pc)) {
				if (visible.isInvisble()) {
					pc.sendPackets(new S_RemoveObject(visible));
				}
			}
		}
		else {
			pc.sendPackets(new S_SystemMessage(cmdName + "請輸入  on|off 。"));
		}
	}

}
