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
package Lsimulator.server.server.model.skill;

import Lsimulator.server.Config;
import Lsimulator.server.server.model.LsimulatorCharacter;

public class LsimulatorSkillTimerCreator {
	public static LsimulatorSkillTimer create(LsimulatorCharacter cha, int skillId,
			int timeMillis) {
		if (Config.SKILLTIMER_IMPLTYPE == 1) {
			return new LsimulatorSkillTimerTimerImpl(cha, skillId, timeMillis);
		} else if (Config.SKILLTIMER_IMPLTYPE == 2) {
			return new LsimulatorSkillTimerThreadImpl(cha, skillId, timeMillis);
		}

		// 不正な値の場合は、とりあえずTimer
		return new LsimulatorSkillTimerTimerImpl(cha, skillId, timeMillis);
	}
}
