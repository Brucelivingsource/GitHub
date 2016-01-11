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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.GMSTATUS_HPBAR;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.Instance.MonsterInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.Instance.PetInstance;
import Lsimulator.server.server.model.Instance.SummonInstance;
import Lsimulator.server.server.serverpackets.S_HPMeter;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorHpBar implements LsimulatorCommandExecutor {
	private LsimulatorHpBar() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorHpBar();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		if (arg.equalsIgnoreCase("on")) {
			pc.setSkillEffect(GMSTATUS_HPBAR, 0);
		}
		else if (arg.equalsIgnoreCase("off")) {
			pc.removeSkillEffect(GMSTATUS_HPBAR);

			for (LsimulatorObject obj : pc.getKnownObjects()) {
				if (isHpBarTarget(obj)) {
					pc.sendPackets(new S_HPMeter(obj.getId(), 0xFF));
				}
			}
		}
		else {
			pc.sendPackets(new S_SystemMessage("請輸入 : " + cmdName + " on|off 。"));
		}
	}

	public static boolean isHpBarTarget(LsimulatorObject obj) {
		if (obj instanceof MonsterInstance) {
			return true;
		}
		if (obj instanceof PcInstance) {
			return true;
		}
		if (obj instanceof SummonInstance) {
			return true;
		}
		if (obj instanceof PetInstance) {
			return true;
		}
		return false;
	}
}
